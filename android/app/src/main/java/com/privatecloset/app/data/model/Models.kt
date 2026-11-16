package com.privatecloset.app.data.model

data class Student(
    val id: String,
    val name: String,
    val lessonsCompleted: Int,
    val currentStreakDays: Int,
    val certificatesEarned: Int,
    val role: UserRole = UserRole.STUDENT,
)

enum class UserRole {
    STUDENT,
    INSTRUCTOR,
    ADMIN,
}

enum class CourseLevel { BEGINNER, INTERMEDIATE, ADVANCED }

data class Course(
    val id: String,
    val title: String,
    val level: CourseLevel,
    val lessonCount: Int,
    val progressPercent: Int = 0,
    val instructorName: String? = null,
    val priceUsd: Double = 0.0,
    val notesSummary: String? = null,
    val keySignature: String? = null,
    val scaleFormula: String? = null,
    val chordVoicings: List<String> = emptyList(),
    val practiceDrills: List<String> = emptyList(),
)

data class Lesson(
    val id: String,
    val courseId: String,
    val title: String,
    val subtitle: String,
    val durationLabel: String,
    val description: String,
    val topics: List<LessonTopic>,
    val isComplete: Boolean = false,
    val videoUrl: String? = null,
    val notesUrl: String? = null,
    val notesText: String? = null,
    val chordDiagrams: List<String> = emptyList(),
)

data class LessonTopic(
    val label: String,
    val isDone: Boolean,
)

data class PracticeExercise(
    val id: String,
    val title: String,
    val chordLabel: String,
    val highlightedNotes: List<String>,
    val progressPercent: Int,
)

data class ProgressSnapshot(
    val overallPercent: Int,
    val lessonsCompleted: Int,
    val lessonsTotal: Int,
    val currentStreakDays: Int,
    val certificatesEarned: Int,
    val monthlyPoints: List<Int>,
)

data class Plan(
    val id: String,
    val name: String,
    val priceLabel: String,
    val priceAmount: Double = 29.0,
    val billingCycle: String,
    val features: List<String>,
    val isMostPopular: Boolean = false,
)

data class OneOnOneSession(
    val id: String,
    val durationLabel: String,
    val priceLabel: String,
    val priceAmount: Double = 35.0,
    val features: List<String>,
)

data class BackendConnectionState(
    val isConnected: Boolean = false,
    val isChecking: Boolean = false,
    val baseUrl: String = "http://10.0.2.2:3000/",
    val message: String = "Offline Mode (Using Cached Content)",
)

// --- Community Chat ---
data class CommunityChannel(
    val id: String,
    val name: String,
    val topic: String,
    val icon: String = "💬",
    val unreadCount: Int = 0,
)

data class CommunityMessage(
    val id: String,
    val channelId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val messageText: String,
    val timestamp: String,
    val isPinned: Boolean = false,
)

// --- Marketplace (Melodies, Voices, Beats, Presets) ---
enum class MarketplaceCategory(val displayName: String) {
    ALL("All Assets"),
    MELODIES("Melodies & Loops"),
    VOICES("Synth Voices & Presets"),
    BEATS("Beats & Drum Kits"),
    CHORD_PACKS("MIDI Chords & Progressions"),
    SAMPLE_PACKS("Sample Packs"),
}

data class MarketplaceItem(
    val id: String,
    val title: String,
    val description: String,
    val category: MarketplaceCategory,
    val priceUsd: Double,
    val sellerId: String,
    val sellerName: String,
    val sellerRole: UserRole,
    val rating: Double,
    val salesCount: Int,
    val tags: List<String>,
    val bpm: Int? = null,
    val keyNote: String? = null,
    val isPurchased: Boolean = false,
)

enum class PaymentMethod(val title: String, val subtitle: String, val iconRes: String) {
    CARD("Credit / Debit Card", "Visa, Mastercard, Amex via Stripe", "💳"),
    GOOGLE_PAY("Google Pay", "Fast 1-tap checkout", "⚡"),
    PAYPAL("PayPal", "PayPal Balance or Linked Account", "🅿️"),
    MPESA("M-Pesa / Mobile Money", "Direct mobile money transfer", "📱"),
    CRYPTO("Crypto Wallet", "USDT, BTC, ETH via Web3 Gateway", "🪙"),
}

data class PaymentTransaction(
    val id: String,
    val itemTitle: String,
    val amountUsd: Double,
    val paymentMethod: PaymentMethod,
    val status: String = "COMPLETED",
    val timestamp: String,
    val registeredDetails: String? = null,
)

// --- Tutor / Instructor Studio ---
data class TutorCourseDraft(
    val title: String,
    val description: String,
    val level: CourseLevel,
    val priceUsd: Double,
    val videoUrl: String,
    val notesFilename: String,
    val materialsNote: String,
)

data class TutorStats(
    val totalStudents: Int,
    val activeCourses: Int,
    val totalEarningsUsd: Double,
    val studentReviewScore: Double,
)
