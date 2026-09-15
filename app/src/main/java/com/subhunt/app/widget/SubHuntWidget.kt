package com.subhunt.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.subhunt.app.MainActivity
import com.subhunt.app.R
import com.subhunt.app.domain.model.DashboardStats
import com.subhunt.app.domain.model.computeDashboardStats
import dagger.hilt.android.EntryPointAccessors
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first

class SubHuntWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SmallSize, TallSize)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
        val repository = entryPoint.repository()

        val stats = computeDashboardStats(repository.getActiveSubscriptions().first())

        provideContent {
            GlanceTheme {
                if (LocalSize.current.height >= TallSize.height) {
                    TallContent(stats)
                } else {
                    SmallContent(stats)
                }
            }
        }
    }

    companion object {
        val SmallSize = DpSize(120.dp, 120.dp)
        val TallSize = DpSize(200.dp, 260.dp)
    }
}

@Composable
private fun WidgetFrame(clickAction: Action, content: @Composable () -> Unit) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(12.dp)
            .clickable(clickAction),
        contentAlignment = Alignment.TopStart
    ) {
        content()
    }
}

@Composable
private fun SmallContent(stats: DashboardStats) {
    WidgetFrame(actionStartActivity<MainActivity>()) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = "SubHunt",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(R.color.widget_muted)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "$${"%.2f".format(stats.totalMonthlyCost)}",
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(R.color.widget_text)
                )
            )
            Text(
                text = "${stats.activeCount} active /mo",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(R.color.widget_muted)
                )
            )
        }
    }
}

@Composable
private fun TallContent(stats: DashboardStats) {
    WidgetFrame(actionStartActivity<MainActivity>()) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = "SubHunt",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(R.color.widget_muted)
                )
            )
            Text(
                text = "$${"%.2f".format(stats.totalMonthlyCost)}/mo",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(R.color.widget_text)
                )
            )
            Text(
                text = "${stats.activeCount} active",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(R.color.widget_muted)
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Due next",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(R.color.widget_accent)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            val bills = stats.upcomingBills.take(3)
            if (bills.isEmpty()) {
                Text(
                    text = "Nothing due soon",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ColorProvider(R.color.widget_muted)
                    )
                )
            } else {
                val dateFormat = DateTimeFormatter.ofPattern("MMM d")
                bills.forEach { bill ->
                    Text(
                        text = "${bill.subscription.name} · $${"%.2f".format(bill.subscription.cost)} · ${bill.chargeDate.format(dateFormat)}",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = ColorProvider(R.color.widget_text)
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }
            }
        }
    }
}


