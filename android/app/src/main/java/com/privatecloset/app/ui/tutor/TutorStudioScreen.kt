package com.privatecloset.app.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.Course
import com.privatecloset.app.data.model.CourseLevel
import com.privatecloset.app.data.model.TutorCourseDraft
import com.privatecloset.app.data.model.UserRole
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary

@Composable
fun TutorStudioScreen(
    uiState: UiState<TutorStudioUiData>,
    onUploadCourse: (TutorCourseDraft, (Result<Course>) -> Unit) -> Unit,
    onVerifyInviteCode: (String, (Boolean) -> Unit) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
) {
    var showUploadModal by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tutor & Instructor Studio",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(Icons.Filled.Verified, contentDescription = "Verified Tutor", tint = Gold, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = "Upload courses, notes, video lessons & review students",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }

            Button(
                onClick = { showUploadModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Upload", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        StateScaffold(uiState) { data ->
            if (!data.isTutorAuthorized) {
                // Unauthorized / Student view banner with invitation code prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgCard)
                        .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold, modifier = Modifier.size(36.dp))
                        Text(
                            text = "Tutor Access Restricted",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        Text(
                            text = "Only authorized instructors can upload courses and notes. Access is granted via the master administrator account or a valid Tutor Invitation Code.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = { showAuthModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Enter Tutor Access Code", fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { onSwitchRole(UserRole.INSTRUCTOR) }) {
                            Text("Switch to Tutor Role (Development Mode)", color = GoldLight, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Tutor Metrics Overview Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TutorMetricCard(
                        title = "Students",
                        value = "${data.stats.totalStudents}",
                        icon = Icons.Filled.People,
                        modifier = Modifier.weight(1f),
                    )
                    TutorMetricCard(
                        title = "Earnings",
                        value = "$${String.format("%.0f", data.stats.totalEarningsUsd)}",
                        icon = Icons.Filled.TrendingUp,
                        modifier = Modifier.weight(1f),
                    )
                    TutorMetricCard(
                        title = "Rating",
                        value = "${data.stats.studentReviewScore} ★",
                        icon = Icons.Filled.Star,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Section Title: Managed Courses & Lessons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "My Published Courses (${data.myCourses.size})",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = { showUploadModal = true }) {
                        Text("+ New Lesson", color = GoldLight, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                ) {
                    items(data.myCourses) { course ->
                        TutorCourseItemCard(course = course)
                    }
                }
            }
        }
    }

    // --- Upload Modal ---
    if (showUploadModal) {
        UploadCourseModal(
            onDismiss = { showUploadModal = false },
            onUpload = { draft ->
                onUploadCourse(draft) {
                    showUploadModal = false
                }
            }
        )
    }

    // --- Master Code Auth Modal ---
    if (showAuthModal) {
        TutorAuthModal(
            onDismiss = { showAuthModal = false },
            onVerify = { code, callback ->
                onVerifyInviteCode(code) { ok ->
                    callback(ok)
                    if (ok) showAuthModal = false
                }
            }
        )
    }
}

@Composable
private fun TutorMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, BgStroke, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = TextSecondary, fontSize = 11.sp)
                Icon(icon, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TutorCourseItemCard(course: Course) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, BgStroke, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${course.lessonCount} Lessons", color = TextSecondary, fontSize = 11.sp)
                    Text("·", color = TextSecondary, fontSize = 11.sp)
                    Text(course.level.name, color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Purple.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text("Active", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UploadCourseModal(
    onDismiss: () -> Unit,
    onUpload: (TutorCourseDraft) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("https://vimeo.com/privatecloset/master-chords") }
    var notesFilename by remember { mutableStateOf("Gospel_Passing_Chords_Chart.pdf") }
    var materialsNote by remember { mutableStateOf("Practice Exercise: 2-5-1 voicings in all 12 keys") }
    var selectedLevel by remember { mutableStateOf(CourseLevel.INTERMEDIATE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Upload Course & Notes", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    placeholder = { Text("e.g. Advanced Worship Improvisation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Syllabus") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Video Lecture URL") },
                    leadingIcon = { Icon(Icons.Filled.Movie, contentDescription = null, tint = GoldLight) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notesFilename,
                    onValueChange = { notesFilename = it },
                    label = { Text("Course Notes (PDF / Doc)") },
                    leadingIcon = { Icon(Icons.Filled.AttachFile, contentDescription = null, tint = GoldLight) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = materialsNote,
                    onValueChange = { materialsNote = it },
                    label = { Text("Practice Homework / Exercise") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val draft = TutorCourseDraft(
                            title = title,
                            description = description.ifBlank { "Instructor-led keyboard course." },
                            level = selectedLevel,
                            priceUsd = 29.99,
                            videoUrl = videoUrl,
                            notesFilename = notesFilename,
                            materialsNote = materialsNote,
                        )
                        onUpload(draft)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
            ) {
                Text("Publish to App", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun TutorAuthModal(
    onDismiss: () -> Unit,
    onVerify: (String, (Boolean) -> Unit) -> Unit,
) {
    var codeInput by remember { mutableStateOf("MASTER-TUTOR-2026") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = { Text("Tutor Master Invitation", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your instructor invitation code issued by the master administrator to unlock tutor privileges.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("Invitation Code") },
                    placeholder = { Text("MASTER-TUTOR-2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Tip: Try default code 'MASTER-TUTOR-2026' or 'KEYBOARD-MASTER'",
                    color = GoldLight,
                    fontSize = 11.sp,
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFF87171), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isChecking = true
                    errorMsg = null
                    onVerify(codeInput) { valid ->
                        isChecking = false
                        if (!valid) {
                            errorMsg = "Invalid code. Please contact administrator."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
            ) {
                Text("Authorize Tutor", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
