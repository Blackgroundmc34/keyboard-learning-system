package com.privatecloset.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null,
)

@JsonClass(generateAdapter = true)
data class MyEnrollmentDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "status") val status: String? = "ACTIVE",
    @Json(name = "progressPercentage") val progressPercentage: Double? = 0.0,
    @Json(name = "enrolledAt") val enrolledAt: String? = null,
    @Json(name = "completedAt") val completedAt: String? = null,
    @Json(name = "course") val course: CourseDto? = null,
)

@JsonClass(generateAdapter = true)
data class MyEnrollmentsResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "enrollments") val enrollments: List<MyEnrollmentDto>? = null,
)

@JsonClass(generateAdapter = true)
data class MyProgressLessonDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "lessonOrder") val lessonOrder: Int? = 1,
    @Json(name = "durationMinutes") val durationMinutes: Int? = 10,
    @Json(name = "status") val status: String? = "NOT_STARTED",
    @Json(name = "progressPercentage") val progressPercentage: Double? = 0.0,
)

@JsonClass(generateAdapter = true)
data class MyProgressCourseDto(
    @Json(name = "enrollmentId") val enrollmentId: String? = null,
    @Json(name = "status") val status: String? = "ACTIVE",
    @Json(name = "progressPercentage") val progressPercentage: Double? = 0.0,
    @Json(name = "course") val course: CourseShortDto? = null,
    @Json(name = "lessons") val lessons: List<MyProgressLessonDto>? = null,
)

@JsonClass(generateAdapter = true)
data class MyProgressResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "progress") val progress: List<MyProgressCourseDto>? = null,
)

@JsonClass(generateAdapter = true)
data class StudentSummaryDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "lessons_completed") val lessonsCompleted: Int? = 0,
    @Json(name = "current_streak_days") val currentStreakDays: Int? = 0,
    @Json(name = "certificates_earned") val certificatesEarned: Int? = 0,
    @Json(name = "overall_progress_percent") val overallProgressPercent: Int? = 0,
)

@JsonClass(generateAdapter = true)
data class StudentDashboardDto(
    @Json(name = "student") val student: StudentSummaryDto? = null,
    @Json(name = "enrolled_courses") val enrolledCourses: List<CourseDto>? = null,
    @Json(name = "continue_lesson") val continueLesson: LessonDto? = null,
    @Json(name = "announcements") val announcements: List<AnnouncementDto>? = null,
)

@JsonClass(generateAdapter = true)
data class StudentDashboardResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "data") val data: StudentDashboardDto? = null,
)

@JsonClass(generateAdapter = true)
data class AnnouncementDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "author_name") val authorName: String? = "Private Closet Team",
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "course_id") val courseId: String? = null,
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "sender_id") val senderId: String? = null,
    @Json(name = "receiver_id") val receiverId: String? = null,
    @Json(name = "sender_name") val senderName: String? = null,
    @Json(name = "message_text") val messageText: String,
    @Json(name = "sent_at") val sentAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "receiver_id") val receiverId: String? = null,
    @Json(name = "message_text") val messageText: String,
)
