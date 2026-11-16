package com.privatecloset.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LessonDto(
    @Json(name = "id") val id: String,
    @Json(name = "courseId") val courseId: String? = null,
    @Json(name = "course_id") val courseIdLegacy: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "lessonOrder") val lessonOrder: Int? = 1,
    @Json(name = "lesson_order") val lessonOrderLegacy: Int? = null,
    @Json(name = "durationMinutes") val durationMinutes: Int? = 10,
    @Json(name = "duration_minutes") val durationMinutesLegacy: Int? = null,
    @Json(name = "isPreview") val isPreview: Boolean? = false,
    @Json(name = "is_free_preview") val isFreePreview: Boolean? = null,
    @Json(name = "is_completed") val isCompleted: Boolean? = false,
    @Json(name = "videoUrl") val videoUrl: String? = null,
    @Json(name = "materials") val materials: List<LessonMaterialDto>? = null,
) {
    val effectiveOrder: Int
        get() = lessonOrder ?: lessonOrderLegacy ?: 1

    val effectiveDuration: Int
        get() = durationMinutes ?: durationMinutesLegacy ?: 10

    val effectiveCourseId: String?
        get() = courseId ?: courseIdLegacy
}

@JsonClass(generateAdapter = true)
data class CourseLessonsResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "course") val course: CourseShortDto? = null,
    @Json(name = "lessons") val lessons: List<LessonDto>? = null,
)

@JsonClass(generateAdapter = true)
data class LessonDetailResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "lesson") val lesson: LessonDto? = null,
)

@JsonClass(generateAdapter = true)
data class LessonMaterialDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "lessonId") val lessonId: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String? = "PDF",
    @Json(name = "material_type") val materialTypeLegacy: String? = null,
    @Json(name = "fileUrl") val fileUrl: String? = null,
    @Json(name = "file_url") val fileUrlLegacy: String? = null,
    @Json(name = "textContent") val textContent: String? = null,
    @Json(name = "isDownloadable") val isDownloadable: Boolean? = true,
) {
    val effectiveFileUrl: String?
        get() = fileUrl ?: fileUrlLegacy

    val effectiveType: String
        get() = type ?: materialTypeLegacy ?: "PDF"
}

@JsonClass(generateAdapter = true)
data class LessonProgressRequest(
    @Json(name = "progressPercentage") val progressPercentage: Double? = 100.0,
    @Json(name = "completed") val completed: Boolean? = true,
    @Json(name = "watch_time_seconds") val watchTimeSeconds: Int? = null,
    @Json(name = "last_position_seconds") val lastPositionSeconds: Int? = null,
)

@JsonClass(generateAdapter = true)
data class ProgressDetailDto(
    @Json(name = "lessonId") val lessonId: String? = null,
    @Json(name = "lessonTitle") val lessonTitle: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "progressPercentage") val progressPercentage: Double? = null,
    @Json(name = "courseProgressPercentage") val courseProgressPercentage: Double? = null,
)

@JsonClass(generateAdapter = true)
data class ProgressResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "progress") val progress: ProgressDetailDto? = null,
    @Json(name = "progress_percent") val progressPercentLegacy: Int? = null,
)
