package com.privatecloset.app.ui.lesson

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.Lesson
import com.privatecloset.app.data.model.LessonTopic
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.SuccessGreen
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun LessonScreen(
    uiState: UiState<Lesson>,
    onBack: () -> Unit,
    onMarkComplete: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Video, 1 = Study Notes
    var isNotesDownloaded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
            Text("Lesson Player", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(12.dp))

        StateScaffold(uiState) { lesson ->
            Column {
                Text(lesson.title, color = TextPrimary, style = MaterialTheme.typography.headlineMedium)
                Text(lesson.subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(12.dp))

                // Segmented Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == 0) Purple else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶ Video Lecture", color = if (selectedTab == 0) TextPrimary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (selectedTab == 1) Purple else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📄 Lesson Notes & Chords", color = if (selectedTab == 1) TextPrimary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Video View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .background(BgCard, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Purple, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = TextPrimary)
                        }
                        Text(
                            lesson.durationLabel,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("About this lesson", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(lesson.description, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)

                    Spacer(Modifier.height(16.dp))
                    Text("Topics", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lesson.topics.forEach { topic -> TopicRow(topic) }
                    }
                } else {
                    // Comprehensive Notes View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgCard, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text("Study Guide & Theory Notes", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            lesson.notesText ?: "Proper finger assignment: Right hand 1 (thumb) to 5 (pinky). Keep fingers curved and wrists completely relaxed. Avoid flat fingers when pressing keys.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(12.dp))
                        Text("Key Chords & Fingering Formula:", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        val chords = if (lesson.chordDiagrams.isNotEmpty()) lesson.chordDiagrams else listOf("C Major (1-3-5)", "G Major (1-3-5)", "A Minor (1-3-5)")
                        chords.forEach { chord ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(androidx.compose.ui.graphics.Color(0xFF140A26), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("🎹 $chord", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { isNotesDownloaded = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isNotesDownloaded) "✓ Notes PDF Downloaded" else "⬇️ Download Lesson Notes (PDF)", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) {
                        Text("Previous", color = TextPrimary)
                    }
                    Button(
                        onClick = onMarkComplete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleLight),
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) {
                        Text("Mark Complete", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicRow(topic: LessonTopic) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (topic.isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (topic.isDone) SuccessGreen else BgStroke,
            modifier = Modifier.size(18.dp),
        )
        Text(
            topic.label,
            color = if (topic.isDone) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
