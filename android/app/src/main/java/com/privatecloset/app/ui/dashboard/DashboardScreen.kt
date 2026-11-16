package com.privatecloset.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecloset.app.data.model.BackendConnectionState
import com.privatecloset.app.data.model.Lesson
import com.privatecloset.app.data.model.Student
import com.privatecloset.app.ui.common.BackendConnectionChip
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    uiState: UiState<DashboardUiData>,
    connectionState: BackendConnectionState? = null,
    onConfigureBackend: () -> Unit = {},
    onOpenCourses: () -> Unit,
    onOpenLessons: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenCommunity: () -> Unit,
    onOpenMarketplace: () -> Unit = {},
    onOpenTutorStudio: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Dashboard", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (connectionState != null) {
                    BackendConnectionChip(
                        state = connectionState,
                        onClick = onConfigureBackend,
                    )
                }
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(20.dp))

        StateScaffold(uiState) { data ->
            Column {
                Text(
                    text = "Hi, ${data.student.name} \uD83D\uDC4B",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(Modifier.height(16.dp))
                ContinueLearningCard(data.continueLearningLesson)

                Spacer(Modifier.height(20.dp))
                Text(text = "My Progress", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                StatRow(data.student)

                Spacer(Modifier.height(20.dp))
                Text(text = "Quick Access & Hubs", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                QuickAccessGrid(
                    onOpenCourses = onOpenCourses,
                    onOpenLessons = onOpenLessons,
                    onOpenPractice = onOpenPractice,
                    onOpenCommunity = onOpenCommunity,
                    onOpenMarketplace = onOpenMarketplace,
                    onOpenTutorStudio = onOpenTutorStudio,
                )
            }
        }
    }
}

@Composable
private fun ContinueLearningCard(lesson: Lesson) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Text(text = "Continue Learning", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Purple, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Piano, contentDescription = null, tint = TextPrimary)
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(lesson.subtitle, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(lesson.title, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = 0.8f,
            color = PurpleLight,
            trackColor = BgStroke,
            modifier = Modifier.fillMaxWidth().height(6.dp),
        )
    }
}

@Composable
private fun StatRow(student: Student) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatPill(value = "${student.lessonsCompleted}", label = "Lessons Done", modifier = Modifier.weight(1f))
        StatPill(value = "${student.currentStreakDays}d", label = "Streak", modifier = Modifier.weight(1f))
        StatPill(value = "${student.certificatesEarned}", label = "Certificates", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, color = Gold, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(2.dp))
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

private data class QuickAccessItem(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
private fun QuickAccessGrid(
    onOpenCourses: () -> Unit,
    onOpenLessons: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenCommunity: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenTutorStudio: () -> Unit,
) {
    val items = listOf(
        QuickAccessItem(Icons.Filled.MenuBook, "Courses", onOpenCourses),
        QuickAccessItem(Icons.Filled.Piano, "64-Key Synth", onOpenPractice),
        QuickAccessItem(Icons.Filled.Chat, "Community", onOpenCommunity),
        QuickAccessItem(Icons.Filled.ShoppingBag, "Marketplace", onOpenMarketplace),
        QuickAccessItem(Icons.Filled.School, "Tutor Studio", onOpenTutorStudio),
        QuickAccessItem(Icons.Filled.PlayCircle, "Lessons", onOpenLessons),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(180.dp),
    ) {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(BgCard, RoundedCornerShape(14.dp))
                    .clickable(onClick = item.onClick)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BgStroke, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(item.icon, contentDescription = item.label, tint = Gold, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text(item.label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
