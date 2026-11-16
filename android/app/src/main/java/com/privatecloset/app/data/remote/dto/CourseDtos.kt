package com.privatecloset.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "level") val level: String? = "BEGINNER",
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrlLegacy: String? = null,
    @Json(name = "status") val status: String? = "PUBLISHED",
    @Json(name = "estimatedDurationMinutes") val estimatedDurationMinutes: Int? = null,
    @Json(name = "lessonCount") val lessonCount: Int? = null,
    @Json(name = "lesson_count") val lessonCountLegacy: Int? = null,
    @Json(name = "progress_percent") val progressPercent: Int? = 0,
    @Json(name = "instructorName") val instructorName: String? = null,
) {
    val totalLessons: Int
        get() = lessonCount ?: lessonCountLegacy ?: 8

    val effectiveThumbnail: String?
        get() = thumbnailUrl ?: thumbnailUrlLegacy
}

@JsonClass(generateAdapter = true)
data class CourseListResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "courses") val courses: List<CourseDto>? = null,
    @Json(name = "message") val message: String? = null,
)

@JsonClass(generateAdapter = true)
data class CourseShortDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "slug") val slug: String? = null,
)

@JsonClass(generateAdapter = true)
data class CourseDetailDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "level") val level: String? = "BEGINNER",
    @Json(name = "thumbnailUrl") val thumbnailUrl: String? = null,
    @Json(name = "lessons") val lessons: List<LessonDto>? = null,
    @Json(name = "instructors") val instructors: List<InstructorDto>? = null,
    @Json(name = "is_enrolled") val isEnrolled: Boolean? = false,
    @Json(name = "progress_percent") val progressPercent: Int? = 0,
)

@JsonClass(generateAdapter = true)
data class CourseDetailResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "course") val course: CourseDetailDto? = null,
)

@JsonClass(generateAdapter = true)
data class InstructorDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "fullName") val fullName: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "avatarUrl") val avatarUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class EnrollmentDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "courseId") val courseId: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "progressPercentage") val progressPercentage: Double? = 0.0,
    @Json(name = "status") val status: String? = "ACTIVE",
    @Json(name = "enrolledAt") val enrolledAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class EnrollmentResponseDto(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "message") val message: String? = null,
    @Json(name = "enrollment") val enrollment: EnrollmentDto? = null,
)
