package com.privatecloset.app.data.remote

import com.privatecloset.app.data.remote.dto.AnnouncementDto
import com.privatecloset.app.data.remote.dto.AuthResponseDto
import com.privatecloset.app.data.remote.dto.CourseDetailResponseDto
import com.privatecloset.app.data.remote.dto.CourseLessonsResponseDto
import com.privatecloset.app.data.remote.dto.CourseListResponseDto
import com.privatecloset.app.data.remote.dto.EnrollmentResponseDto
import com.privatecloset.app.data.remote.dto.HealthResponseDto
import com.privatecloset.app.data.remote.dto.LessonDetailResponseDto
import com.privatecloset.app.data.remote.dto.LessonMaterialDto
import com.privatecloset.app.data.remote.dto.LessonProgressRequest
import com.privatecloset.app.data.remote.dto.LoginRequest
import com.privatecloset.app.data.remote.dto.MessageDto
import com.privatecloset.app.data.remote.dto.MyEnrollmentsResponseDto
import com.privatecloset.app.data.remote.dto.MyProgressResponseDto
import com.privatecloset.app.data.remote.dto.ProgressResponseDto
import com.privatecloset.app.data.remote.dto.RegisterRequest
import com.privatecloset.app.data.remote.dto.SendMessageRequest
import com.privatecloset.app.data.remote.dto.StudentDashboardResponseDto
import com.privatecloset.app.data.remote.dto.UpdateProfileRequest
import com.privatecloset.app.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface representing the backend API endpoints of keyboard-learning-system.
 */
interface ApiService {

    // --- Health ---
    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponseDto>

    // --- Authentication ---
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponseDto>

    @PATCH("api/auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponseDto>

    // --- Courses ---
    @GET("api/courses")
    suspend fun getCourses(
        @Query("status") status: String? = "PUBLISHED",
        @Query("level") level: String? = null,
        @Query("search") search: String? = null,
    ): Response<CourseListResponseDto>

    @GET("api/courses/{id}")
    suspend fun getCourseDetail(@Path("id") courseId: String): Response<CourseDetailResponseDto>

    @GET("api/courses/{id}/lessons")
    suspend fun getCourseLessons(@Path("id") courseId: String): Response<CourseLessonsResponseDto>

    @POST("api/courses/{id}/enroll")
    suspend fun enrollCourse(@Path("id") courseId: String): Response<EnrollmentResponseDto>

    // --- Student Progress & Enrollments (/api/me) ---
    @GET("api/me/enrollments")
    suspend fun getMyEnrollments(): Response<MyEnrollmentsResponseDto>

    @GET("api/me/progress")
    suspend fun getMyProgress(): Response<MyProgressResponseDto>

    // --- Lessons & Materials ---
    @GET("api/lessons/{id}")
    suspend fun getLessonDetail(@Path("id") lessonId: String): Response<LessonDetailResponseDto>

    @POST("api/lessons/{id}/progress")
    suspend fun updateLessonProgress(
        @Path("id") lessonId: String,
        @Body request: LessonProgressRequest,
    ): Response<ProgressResponseDto>

    @GET("api/lessons/{id}/materials")
    suspend fun getLessonMaterials(@Path("id") lessonId: String): Response<List<LessonMaterialDto>>

    // --- Student Dashboard & Activity ---
    @GET("api/student/dashboard")
    suspend fun getStudentDashboard(): Response<StudentDashboardResponseDto>

    // --- Announcements & Messaging ---
    @GET("api/announcements")
    suspend fun getAnnouncements(): Response<List<AnnouncementDto>>

    @GET("api/messages")
    suspend fun getMessages(): Response<List<MessageDto>>

    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<MessageDto>
}
