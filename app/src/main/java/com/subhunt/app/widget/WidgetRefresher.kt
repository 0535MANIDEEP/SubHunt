package com.subhunt.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val widget = SubHuntWidget()

    fun refresh() {
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                manager.getGlanceIds(SubHuntWidget::class.java).forEach { id ->
                    widget.update(context, id)
                }
            } catch (e: Exception) {
                // Widget not placed or system unavailable — safe to ignore.
            }
        }
    }
}
