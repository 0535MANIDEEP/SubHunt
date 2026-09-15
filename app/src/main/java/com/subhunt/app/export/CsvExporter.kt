package com.subhunt.app.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.subhunt.app.data.local.SubscriptionDao
import com.subhunt.app.domain.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ExportResult {
    data class Saved(val fileName: String, val uri: Uri) : ExportResult
    data object Empty : ExportResult
    data class Error(val message: String) : ExportResult
}

@Singleton
class CsvExporter @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    suspend fun exportCsv(context: Context): ExportResult =
        export(context, Format.CSV)

    suspend fun exportJson(context: Context): ExportResult =
        export(context, Format.JSON)

    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "SubHunt Subscription Export")
        }
        context.startActivity(Intent.createChooser(intent, "Export Subscriptions"))
    }

    fun shareCsv(context: Context, uri: Uri) = shareFile(context, uri, "text/csv")

    private enum class Format(val extension: String, val mimeType: String) {
        CSV("csv", "text/csv"),
        JSON("json", "application/json")
    }

    private suspend fun export(context: Context, format: Format): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val subscriptions = subscriptionDao.getAllSubscriptionsList()
                    .map { it.toDomain() }
                if (subscriptions.isEmpty()) return@withContext ExportResult.Empty

                val timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
                val fileName = "subhunt_export_$timestamp.${format.extension}"
                val bytes = when (format) {
                    Format.CSV -> buildCsv(subscriptions).toByteArray()
                    Format.JSON -> buildJson(subscriptions).toByteArray()
                }

                val uri = saveToDownloads(context, fileName, format.mimeType, bytes)
                    ?: saveToCache(context, fileName, bytes)
                ExportResult.Saved(fileName, uri)
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Export failed")
            }
        }
    }

    private fun saveToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/SubHunt"
                    )
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return null
                resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return null
                uri
            } else {
                @Suppress("DEPRECATION")
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "SubHunt"
                )
                if (!dir.exists() && !dir.mkdirs()) return null
                val file = File(dir, fileName)
                file.writeBytes(bytes)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToCache(context: Context, fileName: String, bytes: ByteArray): Uri {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun csvCell(value: String): String {
        if (value.isEmpty()) return ""
        val escaped = value.replace("\"", "\"\"")
        return if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"$escaped\""
        } else {
            value
        }
    }

    private fun buildCsv(subscriptions: List<Subscription>): String {
        val costFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
        val sb = StringBuilder()
        sb.appendLine(
            "Name,Cost,Billing Cycle,Category,Start Date,Next Billing Date," +
                "Reminder Days Before,Active,Notes"
        )
        subscriptions.forEach { sub ->
            sb.appendLine(
                listOf(
                    sub.name,
                    costFormat.format(sub.cost),
                    sub.billingCycle.label,
                    sub.category.label,
                    sub.startDate.toString(),
                    sub.nextBillingDate.toString(),
                    sub.reminderDaysBefore.toString(),
                    sub.isActive.toString(),
                    sub.notes
                ).joinToString(",") { csvCell(it) }
            )
        }
        return sb.toString()
    }

    private fun jsonString(value: String): String {
        val sb = StringBuilder("\"")
        value.forEach { c ->
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (c < ' ') sb.append("\\u%04x".format(c.code)) else sb.append(c)
            }
        }
        return sb.append("\"").toString()
    }

    private fun buildJson(subscriptions: List<Subscription>): String {
        val sb = StringBuilder()
        sb.append("{\"app\":\"SubHunt\",\"version\":1,\"exportedAt\":")
        sb.append(jsonString(LocalDateTime.now().toString()))
        sb.append(",\"subscriptions\":[")
        subscriptions.forEachIndexed { i, sub ->
            if (i > 0) sb.append(",")
            sb.append("{")
            sb.append("\"name\":${jsonString(sub.name)},")
            sb.append("\"cost\":${sub.cost},")
            sb.append("\"billingCycle\":${jsonString(sub.billingCycle.name)},")
            sb.append("\"category\":${jsonString(sub.category.name)},")
            sb.append("\"color\":${sub.color},")
            sb.append("\"startDate\":${jsonString(sub.startDate.toString())},")
            sb.append("\"nextBillingDate\":${jsonString(sub.nextBillingDate.toString())},")
            sb.append("\"reminderDaysBefore\":${sub.reminderDaysBefore},")
            sb.append("\"isActive\":${sub.isActive},")
            sb.append("\"notes\":${jsonString(sub.notes)}")
            sb.append("}")
        }
        return sb.append("]}").toString()
    }
}
