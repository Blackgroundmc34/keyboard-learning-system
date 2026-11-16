package com.privatecloset.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.privatecloset.app.data.model.ProgressSnapshot
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun ProgressScreen(uiState: UiState<ProgressSnapshot>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text("Progress", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        StateScaffold(uiState) { snapshot ->
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Overall Progress", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(12.dp))
                        ProgressRing(percent = snapshot.overallPercent)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Lessons Completed", "${snapshot.lessonsCompleted}/${snapshot.lessonsTotal}", Modifier.weight(1f))
                    StatTile("Current Streak", "${snapshot.currentStreakDays} Days", Modifier.weight(1f))
                    StatTile("Certificates Earned", "${snapshot.certificatesEarned}", Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))
                Text("Your Progress", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                ProgressLineChart(points = snapshot.monthlyPoints)
            }
        }
    }
}

@Composable
private fun ProgressRing(percent: Int) {
    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 10.dp.toPx()
            drawArc(
                color = BgStroke,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = PurpleLight,
                startAngle = -90f,
                sweepAngle = 360f * (percent / 100f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$percent%", color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
            Text("Complete", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Gold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProgressLineChart(points: List<Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(BgCard, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        if (points.isEmpty()) return@Box
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxValue = (points.maxOrNull() ?: 1).coerceAtLeast(1)
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val path = Path()
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value.toFloat() / maxValue) * size.height
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = PurpleLight, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value.toFloat() / maxValue) * size.height
                drawCircle(color = Gold, radius = 3.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}
