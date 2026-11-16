package com.privatecloset.app.data.repository

import com.privatecloset.app.data.model.BackendConnectionState
import com.privatecloset.app.data.model.CommunityChannel
import com.privatecloset.app.data.model.CommunityMessage
import com.privatecloset.app.data.model.Course
import com.privatecloset.app.data.model.Lesson
import com.privatecloset.app.data.model.MarketplaceItem
import com.privatecloset.app.data.model.OneOnOneSession
import com.privatecloset.app.data.model.PaymentMethod
import com.privatecloset.app.data.model.PaymentTransaction
import com.privatecloset.app.data.model.Plan
import com.privatecloset.app.data.model.PracticeExercise
import com.privatecloset.app.data.model.ProgressSnapshot
import com.privatecloset.app.data.model.Student
import com.privatecloset.app.data.model.TutorCourseDraft
import com.privatecloset.app.data.model.TutorStats
import com.privatecloset.app.data.model.UserRole
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface providing data to the ViewModels. Supports both live network
 * synchronization with the backend and transparent offline caching.
 */
interface LearningRepository {
    suspend fun getStudent(): Student
    suspend fun getCourses(): List<Course>
    suspend fun getCourse(courseId: String): Course?
    suspend fun getLessonsForCourse(courseId: String): List<Lesson>
    suspend fun getLesson(lessonId: String): Lesson?
    suspend fun getContinueLearningLesson(): Lesson
    suspend fun getPracticeExercise(): PracticeExercise
    suspend fun getProgressSnapshot(): ProgressSnapshot
    suspend fun getPlans(): List<Plan>
    suspend fun getOneOnOneSessions(): List<OneOnOneSession>
    suspend fun markLessonComplete(lessonId: String)

    // Backend connectivity & configuration
    fun getConnectionState(): StateFlow<BackendConnectionState>
    suspend fun testConnection(): Boolean
    fun setBackendBaseUrl(url: String)
    fun getBackendBaseUrl(): String

    // Authentication & Roles
    suspend fun login(email: String, password: String): Result<Student>
    suspend fun register(username: String, email: String, password: String, fullName: String?): Result<Student>
    fun logout()
    fun isLoggedIn(): Boolean
    fun getCurrentRole(): UserRole
    fun setUserRole(role: UserRole)

    // Community Chat
    suspend fun getCommunityChannels(): List<CommunityChannel>
    suspend fun getCommunityMessages(channelId: String): List<CommunityMessage>
    suspend fun sendCommunityMessage(channelId: String, text: String): CommunityMessage

    // Marketplace
    suspend fun getMarketplaceItems(): List<MarketplaceItem>
    suspend fun purchaseMarketplaceItem(itemId: String, method: PaymentMethod, registeredDetails: String? = null): Result<PaymentTransaction>
    suspend fun publishMarketplaceItem(item: MarketplaceItem): Result<MarketplaceItem>

    // Tutor / Instructor Studio
    suspend fun getTutorStats(): TutorStats
    suspend fun uploadTutorCourse(draft: TutorCourseDraft): Result<Course>
    suspend fun registerTutor(username: String, email: String, password: String, fullName: String, inviteCode: String): Result<Student>
    fun validateTutorInviteCode(code: String): Boolean
}
