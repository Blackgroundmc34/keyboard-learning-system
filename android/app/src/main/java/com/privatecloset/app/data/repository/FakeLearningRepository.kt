package com.privatecloset.app.data.repository

import com.privatecloset.app.data.model.BackendConnectionState
import com.privatecloset.app.data.model.CommunityChannel
import com.privatecloset.app.data.model.CommunityMessage
import com.privatecloset.app.data.model.Course
import com.privatecloset.app.data.model.CourseLevel
import com.privatecloset.app.data.model.Lesson
import com.privatecloset.app.data.model.LessonTopic
import com.privatecloset.app.data.model.MarketplaceCategory
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class FakeLearningRepository : LearningRepository {

    private var currentRole: UserRole = UserRole.STUDENT

    private var student = Student(
        id = "student_1",
        name = "Mthuthuzeli",
        lessonsCompleted = 12,
        currentStreakDays = 7,
        certificatesEarned = 2,
        role = UserRole.STUDENT,
    )

    private val courses = mutableListOf(
        Course(
            id = "c1",
            title = "Keyboard Beginner Level",
            level = CourseLevel.BEGINNER,
            lessonCount = 8,
            progressPercent = 60,
            instructorName = "Marcus Vance",
            notesSummary = "Foundations of piano posture, 5-finger patterns, and root-position major & minor triads.",
            keySignature = "C Major (No accidentals)",
            scaleFormula = "W-W-H-W-W-W-H (C - D - E - F - G - A - B - C)",
            chordVoicings = listOf(
                "C Major (I): C - E - G (Fingers 1-3-5)",
                "G Major (V): G - B - D (Fingers 1-3-5)",
                "A Minor (vi): A - C - E (Fingers 1-3-5)",
                "F Major (IV): F - A - C (Fingers 1-3-5)"
            ),
            practiceDrills = listOf(
                "Hand Arch: Maintain natural curve without tension",
                "5-Finger Scale: 60 BPM with metronome",
                "Triad Inversions: Root -> 1st Inversion -> 2nd Inversion"
            )
        ),
        Course(
            id = "c2",
            title = "Keyboard Intermediate Mastery",
            level = CourseLevel.INTERMEDIATE,
            lessonCount = 12,
            progressPercent = 20,
            instructorName = "Elena Rostova",
            notesSummary = "Major 7th, Minor 7th, and Dominant 7th extensions with smooth voice leading and pedal control.",
            keySignature = "G Major (1 Sharp: F#) & D Minor (1 Flat: Bb)",
            scaleFormula = "Major 7th: 1 - 3 - 5 - 7 | Minor 7th: 1 - b3 - 5 - b7",
            chordVoicings = listOf(
                "Cmaj7: C - E - G - B (Rootless LH C-G, RH E-B)",
                "Dm7: D - F - A - C (LH D-A, RH F-C)",
                "Em7: E - G - B - D (LH E-B, RH G-D)",
                "G7: G - B - D - F (Dominant tension)"
            ),
            practiceDrills = listOf(
                "Shell Voicings: Root + 7th LH, 3rd + 5th RH",
                "Sustain Pedal Sync: Clear on harmonic change"
            )
        ),
        Course(
            id = "c3",
            title = "Worship & Gospel Keyboard",
            level = CourseLevel.INTERMEDIATE,
            lessonCount = 10,
            progressPercent = 0,
            instructorName = "David K.",
            notesSummary = "2-5-1 passing chord movements, tritone substitutions, and expressive gospel grace notes.",
            keySignature = "Eb Major & Ab Major",
            scaleFormula = "Gospel 2-5-1 and Diminished Passing Resolutions",
            chordVoicings = listOf(
                "Fm9 (ii): LH F-C | RH Eb-Ab-C-G",
                "Bb13 (V): LH Bb-Ab | RH D-G-C",
                "Ebmaj9 (I): LH Eb-Bb | RH G-Bb-D-F",
                "G7(#9) Passing: LH G-F | RH B-E-Bb"
            ),
            practiceDrills = listOf(
                "Grace Note Slides: Half-step slide into minor 3rd",
                "Tritone Inversions: F-B resolving to E-Bb"
            )
        ),
        Course(
            id = "c4",
            title = "Jazz Harmony & Improvisation",
            level = CourseLevel.ADVANCED,
            lessonCount = 6,
            progressPercent = 0,
            instructorName = "Julian Cole",
            notesSummary = "Rootless Type A & B voicings, altered dominants, and bebop enclosure improvisation.",
            keySignature = "All 12 Keys (Cycle of Fourths)",
            scaleFormula = "Dorian Mode -> Altered Dominant -> Lydian Resolution",
            chordVoicings = listOf(
                "Dm9 (Type A): F - A - C - E (3 - 5 - 7 - 9)",
                "G13(b9): F - Ab - B - E (7 - b9 - 3 - 13)",
                "Cmaj9 (Type B): B - D - E - G (7 - 9 - 3 - 5)"
            ),
            practiceDrills = listOf(
                "Cycle of 4ths: Transpose ii-V-I through all 12 keys",
                "Charleston Swing comping rhythm"
            )
        ),
    )

    private val lessons = mutableListOf(
        Lesson(
            id = "l1",
            courseId = "c1",
            title = "Finger Placement",
            subtitle = "Lesson 3 · Beginner Level",
            durationLabel = "06:12",
            description = "Learn the correct hand position and finger numbering for smooth, effortless playing.",
            topics = listOf(
                LessonTopic("Hand Position & Arch", isDone = true),
                LessonTopic("Finger Numbering (1 to 5)", isDone = true),
                LessonTopic("5-Finger Scale Exercise", isDone = false),
            ),
            notesUrl = "https://privatecloset.music/notes/Lesson3_Finger_Placement.pdf",
            notesText = "Right hand fingers are numbered 1 through 5 starting from thumb (1) to pinky (5). Left hand mirrors this with pinky (5) and thumb (1). Keep wrists level with keyboard surface and never collapse knuckles while pressing keys.",
            chordDiagrams = listOf(
                "C Major: C - E - G (RH: 1 - 3 - 5)",
                "G Major: G - B - D (RH: 1 - 3 - 5)",
                "F Major: F - A - C (RH: 1 - 3 - 5)"
            ),
        ),
        Lesson(
            id = "l2",
            courseId = "c1",
            title = "Major & Minor Triads",
            subtitle = "Lesson 4 · Beginner Level",
            durationLabel = "08:45",
            description = "Constructing 3-note foundational chords across all 12 musical keys.",
            topics = listOf(
                LessonTopic("Root, Third & Fifth", isDone = true),
                LessonTopic("Inversion Drills", isDone = false),
            ),
            notesUrl = "https://privatecloset.music/notes/Lesson4_Triads.pdf",
            notesText = "A major triad consists of a Root + Major Third (4 semitones) + Perfect Fifth (7 semitones). A minor triad lowers the third by a half step (3 semitones). Practice playing root position, 1st inversion, and 2nd inversion with relaxed wrists.",
            chordDiagrams = listOf(
                "C Major Root: C - E - G",
                "C Major 1st Inv: E - G - C",
                "C Major 2nd Inv: G - C - E"
            ),
        ),
    )

    private val practiceExercise = PracticeExercise(
        id = "p1",
        title = "Play this pattern",
        chordLabel = "C Major Scale",
        highlightedNotes = listOf("C", "E", "G"),
        progressPercent = 80,
    )

    private val progressSnapshot = ProgressSnapshot(
        overallPercent = 75,
        lessonsCompleted = 30,
        lessonsTotal = 40,
        currentStreakDays = 7,
        certificatesEarned = 2,
        monthlyPoints = listOf(140, 260, 410, 580, 820),
    )

    private val plans = listOf(
        Plan("p1", "Basic Access", "$19/mo", 19.0, "Monthly", listOf("Access to 20 beginner lessons", "Basic synthesized keyboard practice", "Community student chat")),
        Plan("p2", "Pro Pianist", "$39/mo", 39.0, "Monthly", listOf("Unlimited courses & lesson videos", "Full 64-key responsive keyboard", "Marketplace discounts (20% off)", "Direct Tutor messaging & reviews"), isMostPopular = true),
        Plan("p3", "Mastery VIP", "$79/mo", 79.0, "Monthly", listOf("Everything in Pro", "Two 1-on-1 tutor sessions monthly", "Exclusive sample pack downloads", "Verified creator marketplace store")),
    )

    private val sessions = listOf(
        OneOnOneSession("s1", "30 Minutes", "$35", 35.0, listOf("Technique diagnosis", "Repertoire review", "Live video call")),
        OneOnOneSession("s2", "60 Minutes", "$60", 60.0, listOf("Full improvisational coaching", "Custom exercise plan", "Recorded session archive")),
    )

    // Community Channels & Messages
    private val channels = listOf(
        CommunityChannel("c_general", "general-lounge", "General discussions, introductions and music talk", "🎹"),
        CommunityChannel("c_beginners", "beginner-help", "Ask questions about hand posture, scales and basic chords", "🌱", 2),
        CommunityChannel("c_chords", "chords-and-progressions", "Share 2-5-1 voicings, Gospel passing chords & reharmonizations", "🎼"),
        CommunityChannel("c_sound", "synth-and-sound-design", "Preset creation, patch sharing, and keyboard audio beats", "🎛️"),
        CommunityChannel("c_tutors", "ask-the-tutors", "Direct answers and feedback from verified instructors", "🎓", 1),
    )

    private val messages = mutableMapOf(
        "c_general" to mutableListOf(
            CommunityMessage("m1", "c_general", "u1", "Marcus Vance", UserRole.INSTRUCTOR, "Welcome everyone to Private Closet! Don't forget our weekly live Q&A this Thursday.", "10:15 AM", isPinned = true),
            CommunityMessage("m2", "c_general", "u2", "Sophia Chen", UserRole.STUDENT, "Hey guys! I just completed the C Major scale speed challenge at 120 BPM!", "11:30 AM"),
            CommunityMessage("m3", "c_general", "u3", "David K.", UserRole.INSTRUCTOR, "Great work Sophia! Focus on keeping the thumb relaxed under on the F note.", "11:34 AM"),
        ),
        "c_beginners" to mutableListOf(
            CommunityMessage("m4", "c_beginners", "u4", "Liam N.", UserRole.STUDENT, "How do you stop your pinky finger from curling when playing octaves?", "09:00 AM"),
            CommunityMessage("m5", "c_beginners", "u1", "Marcus Vance", UserRole.INSTRUCTOR, "Keep your wrist elevated and let the weight of your arm drop naturally into the keybed.", "09:12 AM"),
        ),
        "c_chords" to mutableListOf(
            CommunityMessage("m6", "c_chords", "u5", "Julian Cole", UserRole.INSTRUCTOR, "Check out this smooth Dm9 -> G13b9 -> Cmaj7#11 progression!", "Yesterday"),
            CommunityMessage("m7", "c_chords", "student_1", "Mthuthuzeli", UserRole.STUDENT, "The rootless voicing in the left hand sounds so warm and lush.", "Today 08:20 AM"),
        ),
        "c_sound" to mutableListOf(
            CommunityMessage("m8", "c_sound", "u6", "Kofi Beats", UserRole.STUDENT, "Uploaded a new Amapiano log drum & keys preset in the marketplace!", "Yesterday"),
        ),
        "c_tutors" to mutableListOf(
            CommunityMessage("m9", "c_tutors", "u1", "Marcus Vance", UserRole.INSTRUCTOR, "Instructors are online to review your lesson recordings and practice exercises.", "Yesterday"),
        ),
    )

    // Marketplace Items
    private val marketplaceItems = mutableListOf(
        MarketplaceItem(
            id = "mk1",
            title = "Neo-Soul Gospel Chord Matrix",
            description = "48 lush neo-soul chord progressions in MIDI & WAV format with secondary dominants and passing chords.",
            category = MarketplaceCategory.CHORD_PACKS,
            priceUsd = 19.99,
            sellerId = "u1",
            sellerName = "Marcus Vance",
            sellerRole = UserRole.INSTRUCTOR,
            rating = 4.9,
            salesCount = 142,
            tags = listOf("MIDI", "WAV", "Neo-Soul", "Chords"),
            bpm = 82,
            keyNote = "Eb Major",
        ),
        MarketplaceItem(
            id = "mk2",
            title = "Analog Velvet Synth Voices & Leads",
            description = "32 expressive synthesizer patches inspired by vintage Rhodes, Moog Model D, and modern analog polysynths.",
            category = MarketplaceCategory.VOICES,
            priceUsd = 24.99,
            sellerId = "u5",
            sellerName = "Julian Cole",
            sellerRole = UserRole.INSTRUCTOR,
            rating = 4.8,
            salesCount = 98,
            tags = listOf("Presets", "Analog", "Leads", "Pads"),
        ),
        MarketplaceItem(
            id = "mk3",
            title = "Amapiano & Afro-Fusion Drum & Key Beats",
            description = "High-energy rhythm stems with rolling shaker loops, deep log drums, and bright chord chops ready for live jam.",
            category = MarketplaceCategory.BEATS,
            priceUsd = 14.99,
            sellerId = "u6",
            sellerName = "Kofi Beats",
            sellerRole = UserRole.STUDENT,
            rating = 4.7,
            salesCount = 210,
            tags = listOf("Beats", "Stems", "113 BPM", "WAV"),
            bpm = 113,
            keyNote = "F# Minor",
        ),
        MarketplaceItem(
            id = "mk4",
            title = "Silk R&B Melody Pack Vol. 1",
            description = "Soulful piano hooks and vocal chops recorded through tube preamps. 100% royalty-free for productions.",
            category = MarketplaceCategory.MELODIES,
            priceUsd = 18.00,
            sellerId = "u3",
            sellerName = "David K.",
            sellerRole = UserRole.INSTRUCTOR,
            rating = 5.0,
            salesCount = 87,
            tags = listOf("Melodies", "Vocals", "Soul"),
            bpm = 90,
            keyNote = "Ab Major",
        ),
        MarketplaceItem(
            id = "mk5",
            title = "Vintage Electric Piano Sample Library",
            description = "Multi-sampled Mark I and Suitcase Rhodes at 8 velocity layers for ultimate touch sensitivity and bark.",
            category = MarketplaceCategory.SAMPLE_PACKS,
            priceUsd = 29.99,
            sellerId = "u1",
            sellerName = "Marcus Vance",
            sellerRole = UserRole.INSTRUCTOR,
            rating = 4.9,
            salesCount = 65,
            tags = listOf("Rhodes", "Multi-sample", "24-bit"),
        ),
    )

    private val transactions = mutableListOf<PaymentTransaction>()

    override suspend fun getStudent(): Student {
        delay(100)
        return student.copy(role = currentRole)
    }

    override suspend fun getCourses(): List<Course> {
        delay(100)
        return courses
    }

    override suspend fun getCourse(courseId: String): Course? {
        delay(80)
        return courses.find { it.id == courseId }
    }

    override suspend fun getLessonsForCourse(courseId: String): List<Lesson> {
        delay(100)
        return lessons.filter { it.courseId == courseId }
    }

    override suspend fun getLesson(lessonId: String): Lesson? {
        delay(80)
        return lessons.find { it.id == lessonId }
    }

    override suspend fun getContinueLearningLesson(): Lesson {
        delay(80)
        return lessons.first()
    }

    override suspend fun getPracticeExercise(): PracticeExercise {
        delay(80)
        return practiceExercise
    }

    override suspend fun getProgressSnapshot(): ProgressSnapshot {
        delay(100)
        return progressSnapshot
    }

    override suspend fun getPlans(): List<Plan> {
        delay(80)
        return plans
    }

    override suspend fun getOneOnOneSessions(): List<OneOnOneSession> {
        delay(80)
        return sessions
    }

    override suspend fun markLessonComplete(lessonId: String) {
        delay(100)
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx != -1) {
            lessons[idx] = lessons[idx].copy(isComplete = true)
        }
    }

    private val connectionState = MutableStateFlow(
        BackendConnectionState(
            isConnected = false,
            isChecking = false,
            baseUrl = "http://10.0.2.2:3000/",
            message = "Offline (Cached Data Mode)"
        )
    )

    override fun getConnectionState(): StateFlow<BackendConnectionState> = connectionState

    override suspend fun testConnection(): Boolean {
        delay(200)
        return false
    }

    override fun setBackendBaseUrl(url: String) {
        connectionState.value = connectionState.value.copy(baseUrl = url)
    }

    override fun getBackendBaseUrl(): String = connectionState.value.baseUrl

    override suspend fun login(email: String, password: String): Result<Student> {
        delay(200)
        val role = if (email.contains("tutor") || email.contains("instructor")) UserRole.INSTRUCTOR else UserRole.STUDENT
        currentRole = role
        student = student.copy(role = role)
        return Result.success(student)
    }

    override suspend fun register(username: String, email: String, password: String, fullName: String?): Result<Student> {
        delay(200)
        currentRole = UserRole.STUDENT
        student = student.copy(name = fullName ?: username, role = UserRole.STUDENT)
        return Result.success(student)
    }

    override fun logout() {
        currentRole = UserRole.STUDENT
    }

    override fun isLoggedIn(): Boolean = true

    override fun getCurrentRole(): UserRole = currentRole

    override fun setUserRole(role: UserRole) {
        currentRole = role
        student = student.copy(role = role)
    }

    // Community
    override suspend fun getCommunityChannels(): List<CommunityChannel> {
        delay(80)
        return channels
    }

    override suspend fun getCommunityMessages(channelId: String): List<CommunityMessage> {
        delay(80)
        return messages[channelId] ?: emptyList()
    }

    override suspend fun sendCommunityMessage(channelId: String, text: String): CommunityMessage {
        delay(100)
        val newMsg = CommunityMessage(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            senderId = student.id,
            senderName = student.name,
            senderRole = currentRole,
            messageText = text,
            timestamp = "Just now",
        )
        val list = messages.getOrPut(channelId) { mutableListOf() }
        list.add(newMsg)
        return newMsg
    }

    // Marketplace
    override suspend fun getMarketplaceItems(): List<MarketplaceItem> {
        delay(100)
        return marketplaceItems
    }

    override suspend fun purchaseMarketplaceItem(
        itemId: String,
        method: PaymentMethod,
        registeredDetails: String?,
    ): Result<PaymentTransaction> {
        delay(300)
        val item = marketplaceItems.find { it.id == itemId } ?: return Result.failure(Exception("Item not found"))
        val tx = PaymentTransaction(
            id = "TX-" + UUID.randomUUID().toString().take(8).uppercase(),
            itemTitle = item.title,
            amountUsd = item.priceUsd,
            paymentMethod = method,
            status = "COMPLETED",
            timestamp = "Just now",
            registeredDetails = registeredDetails,
        )
        transactions.add(tx)
        val idx = marketplaceItems.indexOfFirst { it.id == itemId }
        if (idx != -1) {
            marketplaceItems[idx] = item.copy(isPurchased = true, salesCount = item.salesCount + 1)
        }
        return Result.success(tx)
    }

    override suspend fun publishMarketplaceItem(item: MarketplaceItem): Result<MarketplaceItem> {
        delay(200)
        val newItem = item.copy(
            id = "mk_" + UUID.randomUUID().toString().take(6),
            sellerId = student.id,
            sellerName = student.name,
            sellerRole = currentRole,
        )
        marketplaceItems.add(0, newItem)
        return Result.success(newItem)
    }

    // Tutor Studio
    override suspend fun getTutorStats(): TutorStats {
        delay(100)
        return TutorStats(
            totalStudents = 148,
            activeCourses = courses.size,
            totalEarningsUsd = 2840.50,
            studentReviewScore = 4.95,
        )
    }

    override suspend fun uploadTutorCourse(draft: TutorCourseDraft): Result<Course> {
        delay(300)
        val newCourse = Course(
            id = "c_" + UUID.randomUUID().toString().take(6),
            title = draft.title,
            level = draft.level,
            lessonCount = 1,
            progressPercent = 0,
            instructorName = student.name,
            priceUsd = draft.priceUsd,
        )
        courses.add(0, newCourse)
        val newLesson = Lesson(
            id = "l_" + UUID.randomUUID().toString().take(6),
            courseId = newCourse.id,
            title = "Introduction & Overview",
            subtitle = "Lesson 1 · ${draft.level.name}",
            durationLabel = "10:00",
            description = draft.description,
            topics = listOf(
                LessonTopic("Video Lecture: " + draft.videoUrl.take(20), isDone = false),
                LessonTopic("Course Notes: " + draft.notesFilename, isDone = false),
                LessonTopic(draft.materialsNote.takeIf { it.isNotBlank() } ?: "Practice Exercise", isDone = false),
            ),
            videoUrl = draft.videoUrl,
            notesUrl = draft.notesFilename,
        )
        lessons.add(newLesson)
        return Result.success(newCourse)
    }

    override fun validateTutorInviteCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        return trimmed == "MASTER-TUTOR-2026" || trimmed == "KEYBOARD-MASTER" || trimmed == "TUTOR-VIP"
    }

    override suspend fun registerTutor(
        username: String,
        email: String,
        password: String,
        fullName: String,
        inviteCode: String,
    ): Result<Student> {
        delay(250)
        if (!validateTutorInviteCode(inviteCode)) {
            return Result.failure(IllegalArgumentException("Invalid Tutor Invitation Code. Contact administrator to obtain instructor authorization."))
        }
        currentRole = UserRole.INSTRUCTOR
        student = Student(
            id = "tutor_" + UUID.randomUUID().toString().take(6),
            name = fullName.ifBlank { username },
            lessonsCompleted = 45,
            currentStreakDays = 14,
            certificatesEarned = 5,
            role = UserRole.INSTRUCTOR,
        )
        return Result.success(student)
    }
}
