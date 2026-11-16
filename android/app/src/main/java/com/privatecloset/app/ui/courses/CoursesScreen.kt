package com.privatecloset.app.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.Course
import com.privatecloset.app.data.model.CourseLevel
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun CoursesScreen(
    uiState: UiState<List<Course>>,
    onCourseClick: (Course) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCourseForNotes by remember { mutableStateOf<Course?>(null) }
    val filters = listOf("All") + CourseLevel.entries.map { it.label() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Text("Courses & Study Curriculum", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Box(modifier = Modifier.padding(start = 10.dp)) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("Search courses or notes", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                        inner()
                    },
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { filter ->
                FilterChip(text = filter, selected = filter == selectedFilter) { selectedFilter = filter }
            }
        }

        Spacer(Modifier.height(18.dp))

        StateScaffold(uiState) { courses ->
            val filtered = courses.filter {
                (selectedFilter == "All" || it.level.label() == selectedFilter) &&
                    (query.isBlank() || it.title.contains(query, ignoreCase = true) || (it.notesSummary?.contains(query, ignoreCase = true) == true))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filtered) { course ->
                    CourseCard(
                        course = course,
                        onOpenCourse = { onCourseClick(course) },
                        onOpenNotes = { selectedCourseForNotes = course }
                    )
                }
            }
        }
    }

    selectedCourseForNotes?.let { course ->
        CourseNotesDialog(
            course = course,
            onDismiss = { selectedCourseForNotes = null }
        )
    }
}

private fun CourseLevel.label(): String = when (this) {
    CourseLevel.BEGINNER -> "Beginner"
    CourseLevel.INTERMEDIATE -> "Intermediate"
    CourseLevel.ADVANCED -> "Advanced"
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) Purple else BgCard, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, color = if (selected) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CourseCard(
    course: Course,
    onOpenCourse: () -> Unit,
    onOpenNotes: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LevelBadge(course.level)
            Text("${course.lessonCount} Lessons", color = Gold, style = MaterialTheme.typography.labelMedium)
        }

        Spacer(Modifier.height(8.dp))
        Text(course.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        course.instructorName?.let {
            Text("Instructor: $it", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        course.notesSummary?.let { notes ->
            Spacer(Modifier.height(6.dp))
            Text(notes, color = TextSecondary.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.Button(
                onClick = onOpenCourse,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Gold, contentColor = androidx.compose.ui.graphics.Color.Black),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text("Start Lessons", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
            }

            androidx.compose.material3.OutlinedButton(
                onClick = onOpenNotes,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = com.privatecloset.app.ui.theme.GoldLight),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Text("📄 Course Notes", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CourseNotesDialog(
    course: Course,
    onDismiss: () -> Unit,
) {
    var isDownloaded by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LevelBadge(course.level)
                    Text(course.instructorName ?: "Faculty", color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(course.title, color = TextPrimary, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Key Signature & Scale Formula:", color = com.privatecloset.app.ui.theme.GoldLight, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(androidx.compose.ui.graphics.Color(0xFF110820), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Key: ${course.keySignature ?: "C Major"}", color = TextPrimary, fontSize = 11.sp)
                            Text("Formula: ${course.scaleFormula ?: "Major scale interval progression"}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                if (course.chordVoicings.isNotEmpty()) {
                    item {
                        Text("Core Chord Voicings & Fingerings:", color = com.privatecloset.app.ui.theme.GoldLight, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
                    }
                    items(course.chordVoicings) { voicing ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color(0xFF1A1030), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("🎹 $voicing", color = TextPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                }

                if (course.practiceDrills.isNotEmpty()) {
                    item {
                        Text("Recommended Practice Drills:", color = com.privatecloset.app.ui.theme.GoldLight, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
                    }
                    items(course.practiceDrills) { drill ->
                        Text("• $drill", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = { isDownloaded = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Gold, contentColor = androidx.compose.ui.graphics.Color.Black)
            ) {
                Text(if (isDownloaded) "✓ Downloaded (PDF)" else "⬇️ Download Notes PDF", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun LevelBadge(level: CourseLevel) {
    Box(
        modifier = Modifier
            .background(BgStroke, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(level.label(), color = Gold, style = MaterialTheme.typography.labelMedium)
    }
}
