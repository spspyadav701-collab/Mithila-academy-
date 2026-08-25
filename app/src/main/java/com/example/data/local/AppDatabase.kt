package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        VideoEntity::class,
        SubscriptionEntity::class,
        LiveStreamEntity::class,
        ChatMessageEntity::class,
        KnowledgeEntity::class,
        ChatSessionEntity::class,
        AiChatMessageEntity::class,
        TouchElementEntity::class,
        CourseEntity::class,
        CourseLessonEntity::class,
        NoteEntity::class,
        TestEntity::class,
        TestQuestionEntity::class,
        TestResultEntity::class,
        DownloadedItemEntity::class,
        NoticeEntity::class,
        SocialPostEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun videoDao(): VideoDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun liveStreamDao(): LiveStreamDao
    abstract fun chatDao(): ChatDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun touchElementDao(): TouchElementDao
    abstract fun courseDao(): CourseDao
    abstract fun noteDao(): NoteDao
    abstract fun testDao(): TestDao
    abstract fun downloadDao(): DownloadDao
    abstract fun noticeDao(): NoticeDao
    abstract fun socialDao(): SocialDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spa_ai_teacher_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedDatabase(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            // Seed Users
            val initialUsers = listOf(
                UserEntity(
                    userId = "teacher_sp_01",
                    name = "SP Sir (Founder)",
                    email = "sp@mithilaacademy.edu",
                    role = "teacher",
                    profilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    createdAt = "2026-08-20T10:00:00Z",
                    subscriberCount = 28500,
                    bio = "Founder of Mithila Academy • Expert in Physics, Mathematics & Competitive Exams (BPSC, SSC, Railway)",
                    subjectSpecialty = "Physics, Mathematics & GK"
                ),
                UserEntity(
                    userId = "teacher_priya_02",
                    name = "Dr. Priya Sharma",
                    email = "priya@mithilaacademy.edu",
                    role = "teacher",
                    profilePic = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200",
                    createdAt = "2026-08-21T09:00:00Z",
                    subscriberCount = 14200,
                    bio = "Senior Faculty • Biology & Chemistry Specialist for Board & Competitive Exams",
                    subjectSpecialty = "Biology & Chemistry"
                ),
                UserEntity(
                    userId = "student_amit_101",
                    name = "Amit Kumar",
                    email = "amit@email.com",
                    role = "student",
                    profilePic = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                    createdAt = "2026-08-22T11:26:00Z",
                    subscriberCount = 0,
                    bio = "BPSC & Board Exam Aspirant",
                    subjectSpecialty = "General Studies"
                )
            )
            database.userDao().insertUsers(initialUsers)

            // Seed Videos
            val initialVideos = listOf(
                VideoEntity(
                    videoId = "vid_phy_001",
                    teacherId = "teacher_sp_01",
                    teacherName = "SP Sir",
                    teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    title = "Class 10 - Physics Chapter 1: Light Reflection & Refraction",
                    description = "Comprehensive masterclass on spherical mirrors, Snell's law, refractive index, and ray diagrams with practical examples.",
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/physics_light.mp4",
                    thumbnailUrl = "",
                    views = 12450,
                    likes = 1840,
                    isLiked = true,
                    createdAt = "2026-08-22T08:00:00Z",
                    subject = "Physics",
                    durationMinutes = 42,
                    resolution = "1080p FHD",
                    className = "Class 10",
                    courseId = "crs_phy_10",
                    chapter = "Chapter 1: Light & Optics",
                    teacher = "SP Sir (Mithila Academy)",
                    duration = "42 mins",
                    freeOrPaid = "Free",
                    isPaid = false,
                    isPublished = true,
                    updatedAt = "2026-08-22T08:00:00Z",
                    orderIndex = 1
                ),
                VideoEntity(
                    videoId = "vid_math_002",
                    teacherId = "teacher_sp_01",
                    teacherName = "SP Sir",
                    teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    title = "Mathematics Live Crash Course: Trigonometry & Calculus Shortcuts",
                    description = "Master trigonometric identities, height and distance problems, and calculus derivatives for SSC, Railway, and Board exams.",
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/math_shortcuts.mp4",
                    thumbnailUrl = "",
                    views = 18900,
                    likes = 2950,
                    isLiked = false,
                    createdAt = "2026-08-21T14:30:00Z",
                    subject = "Mathematics",
                    durationMinutes = 35,
                    resolution = "1080p FHD",
                    className = "Class 12",
                    courseId = "crs_math_12",
                    chapter = "Chapter 3: Trigonometric Shortcuts",
                    teacher = "SP Sir (Mithila Academy)",
                    duration = "35 mins",
                    freeOrPaid = "Free",
                    isPaid = false,
                    isPublished = true,
                    updatedAt = "2026-08-21T14:30:00Z",
                    orderIndex = 2
                ),
                VideoEntity(
                    videoId = "vid_gk_003",
                    teacherId = "teacher_sp_01",
                    teacherName = "SP Sir",
                    teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    title = "BPSC & Bihar Police GK Special: Bihar History & Geography",
                    description = "Top 100 expected questions for Bihar Police Constable, SI, and BPSC prelims. Includes ancient Mithila and Magadha history.",
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/bihar_gk.mp4",
                    thumbnailUrl = "",
                    views = 24100,
                    likes = 4120,
                    isLiked = true,
                    createdAt = "2026-08-20T16:00:00Z",
                    subject = "BPSC & Bihar Police",
                    durationMinutes = 55,
                    resolution = "4K Ultra",
                    className = "Competitive / BPSC",
                    courseId = "crs_bpsc_01",
                    chapter = "Chapter 2: Bihar Special History",
                    teacher = "SP Sir",
                    duration = "55 mins",
                    freeOrPaid = "Free",
                    isPaid = false,
                    isPublished = true,
                    updatedAt = "2026-08-20T16:00:00Z",
                    orderIndex = 3
                ),
                VideoEntity(
                    videoId = "vid_bio_004",
                    teacherId = "teacher_priya_02",
                    teacherName = "Dr. Priya Sharma",
                    teacherProfilePic = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200",
                    title = "Biology Class 12: Human Circulatory & Respiratory Systems",
                    description = "Detailed explanation of heart chambers, double circulation, oxygen transport in blood, and expected board exam diagrams.",
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/bio_circulation.mp4",
                    thumbnailUrl = "",
                    views = 9800,
                    likes = 1250,
                    isLiked = false,
                    createdAt = "2026-08-19T11:00:00Z",
                    subject = "Biology",
                    durationMinutes = 38,
                    resolution = "1080p FHD",
                    className = "Class 12",
                    courseId = "crs_bio_12",
                    chapter = "Chapter 4: Human Physiology",
                    teacher = "Dr. Priya Sharma",
                    duration = "38 mins",
                    freeOrPaid = "Free",
                    isPaid = false,
                    isPublished = true,
                    updatedAt = "2026-08-19T11:00:00Z",
                    orderIndex = 4
                )
            )
            database.videoDao().insertVideos(initialVideos)

            // Seed Live Streams
            val initialStreams = listOf(
                LiveStreamEntity(
                    streamId = "live_stream_001",
                    teacherId = "teacher_sp_01",
                    teacherName = "SP Sir (Mithila Academy)",
                    teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    title = "Mathematics & Physics Live Doubt Clearing Session",
                    streamKey = "live_pk_mithila_math_8891",
                    status = "active",
                    startedAt = "2026-08-22T11:00:00Z",
                    viewerCount = 342,
                    subject = "Mathematics"
                ),
                LiveStreamEntity(
                    streamId = "live_stream_002",
                    teacherId = "teacher_sp_01",
                    teacherName = "SP Sir",
                    teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    title = "SSC & Bihar Police Constable 2026 GS Live Practice",
                    streamKey = "live_pk_mithila_police_7721",
                    status = "active",
                    startedAt = "2026-08-22T10:30:00Z",
                    viewerCount = 512,
                    subject = "Bihar Police"
                )
            )
            database.liveStreamDao().insertStreams(initialStreams)

            // Seed Subscriptions
            val initialSubscriptions = listOf(
                SubscriptionEntity(
                    subscriptionId = "sub_001",
                    studentId = "student_amit_101",
                    teacherId = "teacher_sp_01",
                    subscribedAt = "2026-08-20T12:00:00Z"
                )
            )
            database.subscriptionDao().insertSubscription(initialSubscriptions[0])

            // Seed Chats
            val initialChats = listOf(
                ChatMessageEntity(
                    messageId = "msg_001",
                    streamId = "live_stream_001",
                    senderId = "student_amit_101",
                    senderName = "Amit Kumar",
                    senderRole = "student",
                    messageText = "Sir, please explain question number 5 (convex mirror formula) again.",
                    timestamp = "2026-08-22T11:15:30Z",
                    isAiResponse = false
                ),
                ChatMessageEntity(
                    messageId = "msg_002",
                    streamId = "live_stream_001",
                    senderId = "teacher_sp_01",
                    senderName = "SP Sir",
                    senderRole = "teacher",
                    messageText = "Sure Amit! Remember 1/f = 1/v + 1/u. For a convex mirror, focal length f is always positive.",
                    timestamp = "2026-08-22T11:16:10Z",
                    isAiResponse = false
                ),
                ChatMessageEntity(
                    messageId = "msg_003",
                    streamId = "live_stream_001",
                    senderId = "ai_spa",
                    senderName = "SPA AI TEACHER",
                    senderRole = "ai",
                    messageText = "💡 AI Quick Tip: In spherical mirrors, magnification m = -v/u = h_i / h_o.",
                    timestamp = "2026-08-22T11:16:30Z",
                    isAiResponse = true
                )
            )
            database.chatDao().insertChats(initialChats)

            // Seed Knowledge Base
            val initialKnowledge = listOf(
                KnowledgeEntity(
                    id = "kn_identity_01",
                    title = "Creator & Institute Identity",
                    subject = "General",
                    content = "Name: SPA AI TEACHER. Created by: 'मुझे SP ने बनाया है और यह मिथिला एकेडमी द्वारा निर्मित किया गया है।' (Mithila Academy by SP). Greeting: 'नमस्ते, मैं SPA, आपकी क्या मदद कर सकता हूँ?'",
                    keywords = "who made you, kisne banaya, creator, founder, mithila academy, sp sir, name, পরিচয়"
                ),
                KnowledgeEntity(
                    id = "kn_physics_01",
                    title = "Physics: Laws of Reflection and Refraction",
                    subject = "Physics",
                    content = "1. Angle of incidence equals angle of reflection (∠i = ∠r).\n2. Incident ray, reflected ray, and normal lie in the same plane.\n3. Snell's Law: sin(i)/sin(r) = n2/n1 = constant (Refractive Index).\n4. Mirror formula: 1/f = 1/v + 1/u.\n5. Lens formula: 1/f = 1/v - 1/u.",
                    keywords = "physics, reflection, refraction, snell law, mirror formula, lens formula, प्रकाश का परावर्तन"
                ),
                KnowledgeEntity(
                    id = "kn_chem_01",
                    title = "Chemistry: Periodic Table & Chemical Reactions",
                    subject = "Chemistry",
                    content = "Modern Periodic Table is based on atomic number (Henry Moseley). Important reactions: Combination, Decomposition, Displacement, Double Displacement, Redox reactions (Oxidation & Reduction). pH of pure water is 7, acids have pH < 7, bases have pH > 7.",
                    keywords = "chemistry, periodic table, ph scale, chemical reaction, redox, acid base, रसायन विज्ञान"
                ),
                KnowledgeEntity(
                    id = "kn_bio_01",
                    title = "Biology: Photosynthesis & Human Heart",
                    subject = "Biology",
                    content = "Photosynthesis: 6CO2 + 6H2O + Sunlight -> C6H12O6 + 6O2 (takes place in chloroplasts containing chlorophyll). Human Heart has 4 chambers (Right Atrium, Right Ventricle, Left Atrium, Left Ventricle). Deoxygenated blood is pumped to lungs via pulmonary artery; oxygenated blood returns via pulmonary vein.",
                    keywords = "biology, photosynthesis, heart, circulation, cell, जीव विज्ञान, प्रकाश संश्लेषण"
                ),
                KnowledgeEntity(
                    id = "kn_math_01",
                    title = "Mathematics: Essential Formulas & Trigonometry",
                    subject = "Mathematics",
                    content = "Trigonometric identities: sin²θ + cos²θ = 1, 1 + tan²θ = sec²θ, 1 + cot²θ = cosec²θ. Quadratic formula: x = [-b ± √(b² - 4ac)] / (2a). Area of circle = πr², Volume of cylinder = πr²h, Volume of sphere = 4/3 πr³.",
                    keywords = "mathematics, trigonometry, formulas, algebra, calculus, geometry, गणित"
                ),
                KnowledgeEntity(
                    id = "kn_bihar_gk_01",
                    title = "BPSC & Bihar Police: Bihar GK & History Essentials",
                    subject = "BPSC",
                    content = "Bihar was established on March 22, 1912 (Bihar Diwas). Capital: Patna (ancient Pataliputra founded by Udayin). Major rivers: Ganga, Gandak, Kosi (Sorrow of Bihar), Sone. First President of India: Dr. Rajendra Prasad (from Ziradei, Siwan, Bihar). Ancient universities: Nalanda (founded by Kumaragupta I) and Vikramshila (founded by Dharmapala). Mithila region is renowned for Madhubani painting and rich cultural scholarship.",
                    keywords = "bpsc, bihar police, bihar gk, patna, nalanda, mithila, madhubani, rajendra prasad, bihar diwas"
                ),
                KnowledgeEntity(
                    id = "kn_railway_ssc_01",
                    title = "Railway & SSC Exam: General Studies & Static GK",
                    subject = "Railway",
                    content = "First Train in India ran on April 16, 1853 (Bombay to Thane, 34 km). Father of Indian Railways: Lord Dalhousie. Indian Constitution adopted on 26 Nov 1949, enforced on 26 Jan 1950. Fundamental Rights are in Part III (Articles 12-35). Highest peak in India: Kanchenjunga (8586m).",
                    keywords = "railway, ssc, static gk, constitution, first train, fundamental rights, geography"
                )
            )
            database.knowledgeDao().insertKnowledgeList(initialKnowledge)

            // Seed Initial Chat Sessions
            val sampleSessions = listOf(
                ChatSessionEntity(
                    sessionId = "session_phy_01",
                    title = "Physics: Reflection & Mirror Formula",
                    subject = "Physics",
                    createdAt = System.currentTimeMillis() - 86400000L,
                    updatedAt = System.currentTimeMillis() - 86400000L,
                    messageCount = 3,
                    previewSnippet = "1/f = 1/v + 1/u (Mirror Formula explained with sign conventions)"
                ),
                ChatSessionEntity(
                    sessionId = "session_math_02",
                    title = "Math: Trigonometric Identities",
                    subject = "Mathematics",
                    createdAt = System.currentTimeMillis() - 43200000L,
                    updatedAt = System.currentTimeMillis() - 43200000L,
                    messageCount = 3,
                    previewSnippet = "sin²θ + cos²θ = 1, 1 + tan²θ = sec²θ shortcuts"
                ),
                ChatSessionEntity(
                    sessionId = "session_bpsc_03",
                    title = "BPSC GK: Bihar History & Rivers",
                    subject = "BPSC",
                    createdAt = System.currentTimeMillis() - 7200000L,
                    updatedAt = System.currentTimeMillis() - 7200000L,
                    messageCount = 3,
                    previewSnippet = "Bihar Diwas 22 March 1912, Nalanda & Vikramshila history"
                )
            )
            database.chatSessionDao().insertSessions(sampleSessions)

            // Seed Initial Messages for the sessions
            val sampleMessages = listOf(
                AiChatMessageEntity(
                    messageId = "msg_phy_1",
                    sessionId = "session_phy_01",
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = "नमस्ते, मैं SPA AI Teacher हूँ। आपकी क्या मदद कर सकता हूँ?",
                    isAi = true,
                    timestampFormatted = "Yesterday, 10:00 AM",
                    timestampEpoch = System.currentTimeMillis() - 86400000L,
                    subjectTag = "Physics"
                ),
                AiChatMessageEntity(
                    messageId = "msg_phy_2",
                    sessionId = "session_phy_01",
                    sender = "You",
                    role = "user",
                    text = "Sir, please explain concave mirror vs convex mirror ray diagrams and focal length signs.",
                    isAi = false,
                    timestampFormatted = "Yesterday, 10:01 AM",
                    timestampEpoch = System.currentTimeMillis() - 86390000L,
                    subjectTag = "Physics"
                ),
                AiChatMessageEntity(
                    messageId = "msg_phy_3",
                    sessionId = "session_phy_01",
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = "भौतिक विज्ञान में दर्पण सूत्र (Mirror Formula):\n1/f = 1/v + 1/u\n\n📌 मुख्य नियम (Sign Conventions):\n1. अवतल दर्पण (Concave Mirror): फोकस दूरी (f) हमेशा ऋणात्मक (-) होती है।\n2. उत्तल दर्पण (Convex Mirror): फोकस दूरी (f) हमेशा धनात्मक (+) होती है।\n3. आवर्धन (Magnification): m = -v/u = h_i/h_o।",
                    isAi = true,
                    timestampFormatted = "Yesterday, 10:02 AM",
                    timestampEpoch = System.currentTimeMillis() - 86380000L,
                    subjectTag = "Physics"
                ),
                AiChatMessageEntity(
                    messageId = "msg_math_1",
                    sessionId = "session_math_02",
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = "नमस्ते! गणित की किसी भी समस्या या फॉर्मूला के लिए पूछें।",
                    isAi = true,
                    timestampFormatted = "12 hours ago",
                    timestampEpoch = System.currentTimeMillis() - 43200000L,
                    subjectTag = "Mathematics"
                ),
                AiChatMessageEntity(
                    messageId = "msg_math_2",
                    sessionId = "session_math_02",
                    sender = "You",
                    role = "user",
                    text = "Trigonometry के मुख्य तीन सर्वसमिकाएँ (Identities) क्या हैं?",
                    isAi = false,
                    timestampFormatted = "12 hours ago",
                    timestampEpoch = System.currentTimeMillis() - 43190000L,
                    subjectTag = "Mathematics"
                ),
                AiChatMessageEntity(
                    messageId = "msg_math_3",
                    sessionId = "session_math_02",
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = "त्रिकोणमिति (Trigonometry) के 3 मूलभूत सर्वसमिकाएँ:\n1. sin²θ + cos²θ = 1\n2. 1 + tan²θ = sec²θ\n3. 1 + cot²θ = cosec²θ\n\n📌 त्वरित ट्रिक: tan θ = sin θ / cos θ और cot θ = cos θ / sin θ.",
                    isAi = true,
                    timestampFormatted = "12 hours ago",
                    timestampEpoch = System.currentTimeMillis() - 43180000L,
                    subjectTag = "Mathematics"
                ),
                AiChatMessageEntity(
                    messageId = "msg_bpsc_1",
                    sessionId = "session_bpsc_03",
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = "नमस्ते! BPSC, Bihar Police और बिहार सामान्य ज्ञान (GK) तैयारी सत्र में आपका स्वागत है।",
                    isAi = true,
                    timestampFormatted = "2 hours ago",
                    timestampEpoch = System.currentTimeMillis() - 7200000L,
                    subjectTag = "BPSC"
                ),
                AiChatMessageEntity(
                    messageId = "msg_bpsc_2",
                    sessionId = "session_bpsc_03",
                    sender = "You",
                    role = "user",
                    text = "बिहार दिवस कब मनाया जाता है और नालंदा विश्वविद्यालय की स्थापना किसने की थी?",
                    isAi = false,
                    timestampFormatted = "2 hours ago",
                    timestampEpoch = System.currentTimeMillis() - 7190000L,
                    subjectTag = "BPSC"
                ),
                AiChatMessageEntity(
                    messageId = "msg_bpsc_3",
                    sessionId = "session_bpsc_03",
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = "🎯 BPSC एवं बिहार पुलिस परीक्षा हेतु मुख्य तथ्य:\n1. बिहार दिवस: प्रत्येक वर्ष 22 मार्च को मनाया जाता है (22 मार्च 1912 को बंगाल प्रेसीडेंसी से पृथक हुआ)।\n2. नालंदा विश्वविद्यालय: इसकी स्थापना गुप्त शासक कुमारगुप्त प्रथम ने 5वीं शताब्दी में की थी।\n3. विक्रमशिला विश्वविद्यालय: पाल वंश के राजा धर्मपाल ने स्थापित किया था।",
                    isAi = true,
                    timestampFormatted = "2 hours ago",
                    timestampEpoch = System.currentTimeMillis() - 7180000L,
                    subjectTag = "BPSC"
                )
            )
            database.chatSessionDao().insertMessages(sampleMessages)

            // Seed Courses
            val initialCourses = listOf(
                CourseEntity(
                    courseId = "course_spoken_01",
                    title = "Spoken English Fluency & Vocabulary Masterclass",
                    category = "Spoken English",
                    teacherName = "Prof. Ananya Sen",
                    description = "Master fluent everyday English speaking, accent clarity, public presentation, grammar essentials, and interview readiness.",
                    duration = "45 Hours • 60 Lessons",
                    lessonsCount = 60,
                    price = "Free",
                    isFree = true,
                    thumbnailUrl = "https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=400",
                    isEnrolled = true,
                    progressPercent = 65,
                    completedLessons = 39,
                    rating = 4.9f,
                    totalEnrolled = 4850
                ),
                CourseEntity(
                    courseId = "course_phy_02",
                    title = "Complete Physics Master Series (Class 10 & 12 / Competitive)",
                    category = "Physics",
                    teacherName = "SP Sir (Founder)",
                    description = "Deep conceptual understanding of Mechanics, Optics, Electromagnetism, Modern Physics, and numerical shortcuts.",
                    duration = "60 Hours • 75 Lessons",
                    lessonsCount = 75,
                    price = "₹499",
                    isFree = false,
                    thumbnailUrl = "https://images.unsplash.com/photo-1636466497217-26a8cbeaf0aa?w=400",
                    isEnrolled = true,
                    progressPercent = 40,
                    completedLessons = 30,
                    rating = 5.0f,
                    totalEnrolled = 3920
                ),
                CourseEntity(
                    courseId = "course_math_03",
                    title = "Mathematics Speed & Accuracy Mastery Batch",
                    category = "Mathematics",
                    teacherName = "SP Sir",
                    description = "Vedic math shortcuts, Trigonometry, Calculus, Coordinate Geometry, and Algebra for Board and competitive exams.",
                    duration = "50 Hours • 55 Lessons",
                    lessonsCount = 55,
                    price = "₹399",
                    isFree = false,
                    thumbnailUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=400",
                    isEnrolled = false,
                    progressPercent = 0,
                    completedLessons = 0,
                    rating = 4.8f,
                    totalEnrolled = 2890
                ),
                CourseEntity(
                    courseId = "course_bpsc_04",
                    title = "BPSC & Bihar Police 2026 Comprehensive GS Batch",
                    category = "BPSC",
                    teacherName = "SP Sir & Faculty Team",
                    description = "Complete coverage of Bihar History, Geography, Polity, Economy, Current Affairs, and previous 10 years question solutions.",
                    duration = "80 Hours • 90 Lessons",
                    lessonsCount = 90,
                    price = "₹699",
                    isFree = false,
                    thumbnailUrl = "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=400",
                    isEnrolled = true,
                    progressPercent = 25,
                    completedLessons = 22,
                    rating = 4.9f,
                    totalEnrolled = 5400
                ),
                CourseEntity(
                    courseId = "course_bio_05",
                    title = "Biology Class 12 & NEET Foundation Mastery",
                    category = "Biology",
                    teacherName = "Dr. Priya Sharma",
                    description = "Genetics, Human Physiology, Ecology, Cell Biology with high-definition diagrams and previous board questions.",
                    duration = "40 Hours • 48 Lessons",
                    lessonsCount = 48,
                    price = "Free",
                    isFree = true,
                    thumbnailUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400",
                    isEnrolled = false,
                    progressPercent = 0,
                    completedLessons = 0,
                    rating = 4.7f,
                    totalEnrolled = 1950
                )
            )
            database.courseDao().insertCourses(initialCourses)

            // Seed Course Lessons
            val sampleLessons = listOf(
                CourseLessonEntity(
                    lessonId = "les_sp_01",
                    courseId = "course_spoken_01",
                    lessonNumber = 1,
                    title = "Lesson 1: Daily Conversation Openers & Introductions",
                    durationMinutes = 25,
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/spoken_01.mp4",
                    isCompleted = true,
                    notesSummary = "Master greeting phrases, expressing hobbies, family background, and overcoming hesitation."
                ),
                CourseLessonEntity(
                    lessonId = "les_sp_02",
                    courseId = "course_spoken_01",
                    lessonNumber = 2,
                    title = "Lesson 2: Sentence Structures & Common Grammar Pitfalls",
                    durationMinutes = 30,
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/spoken_02.mp4",
                    isCompleted = true,
                    notesSummary = "Present continuous vs Simple present, prepositions of place and time (in, on, at)."
                ),
                CourseLessonEntity(
                    lessonId = "les_sp_03",
                    courseId = "course_spoken_01",
                    lessonNumber = 3,
                    title = "Lesson 3: Confident Pronunciation & Tongue Twisters",
                    durationMinutes = 28,
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/spoken_03.mp4",
                    isCompleted = false,
                    notesSummary = "Vowel sounds, silent letters (doubt, subtle, receipt), and rhythm training."
                ),
                CourseLessonEntity(
                    lessonId = "les_phy_01",
                    courseId = "course_phy_02",
                    lessonNumber = 1,
                    title = "Lesson 1: Light Reflection & Spherical Mirrors",
                    durationMinutes = 40,
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/physics_light.mp4",
                    isCompleted = true,
                    notesSummary = "Laws of reflection, focal point, center of curvature, sign conventions."
                ),
                CourseLessonEntity(
                    lessonId = "les_phy_02",
                    courseId = "course_phy_02",
                    lessonNumber = 2,
                    title = "Lesson 2: Refraction through Glass Prism & Total Internal Reflection",
                    durationMinutes = 35,
                    videoUrl = "https://storage.googleapis.com/bucket_name/videos/physics_prism.mp4",
                    isCompleted = false,
                    notesSummary = "Snell's law, critical angle, optical fibers, and rainbow formation."
                )
            )
            database.courseDao().insertLessons(sampleLessons)

            // Seed Notes
            val initialNotes = listOf(
                NoteEntity(
                    noteId = "note_sp_01",
                    title = "500 Essential Spoken English Vocabulary & Daily Idioms",
                    subject = "Spoken English",
                    category = "Vocabulary",
                    chapter = "Chapter 1: Spoken Fluency",
                    author = "Prof. Ananya Sen",
                    downloadSize = "3.2 MB",
                    isFree = true,
                    contentText = """
# 500 Essential Spoken English Words & Phrases

## 1. Professional Greetings
- "Pleasure to meet you" - Formal introduction
- "I appreciate your assistance" - Expressing gratitude
- "Could you please elaborate?" - Asking for clarification

## 2. Common Phrasal Verbs
- **Look forward to**: Eagerly anticipating something.
- **Call off**: To cancel an event.
- **Figure out**: To find a solution or understand.
- **Run into**: Meet unexpectedly.

## 3. Daily Expressions
- "It slipped my mind" (I forgot)
- "Cost an arm and a leg" (Very expensive)
- "Piece of cake" (Very easy)
                    """.trimIndent(),
                    isDownloaded = true
                ),
                NoteEntity(
                    noteId = "note_phy_02",
                    title = "Physics Formula Cheat Sheet & Derivations (Class 10-12)",
                    subject = "Physics",
                    category = "Formulas",
                    chapter = "Chapter 2: Optics & Electromagnetism",
                    author = "SP Sir",
                    downloadSize = "4.8 MB",
                    isFree = true,
                    contentText = """
# Physics Key Formulas & Laws

## Optics
1. Mirror Formula: 1/f = 1/v + 1/u
2. Lens Formula: 1/f = 1/v - 1/u
3. Magnification: m = -v/u (Mirrors), m = v/u (Lenses)
4. Power of Lens: P = 1/f (in meters, unit: Dioptre D)

## Electricity & Magnetism
1. Ohm's Law: V = I * R
2. Resistance in Series: R_eq = R1 + R2 + R3
3. Resistance in Parallel: 1/R_eq = 1/R1 + 1/R2 + 1/R3
4. Joule's Heating Law: H = I²Rt
                    """.trimIndent(),
                    isDownloaded = true
                ),
                NoteEntity(
                    noteId = "note_math_03",
                    title = "Vedic Mathematics & Trigonometry High-Speed Shortcuts",
                    subject = "Mathematics",
                    category = "Shortcuts",
                    chapter = "Chapter 3: Fast Calculation",
                    author = "SP Sir",
                    downloadSize = "2.9 MB",
                    isFree = true,
                    contentText = """
# Mathematics High-Speed Shortcuts

## Trigonometry Identities
- sin²θ + cos²θ = 1
- 1 + tan²θ = sec²θ
- 1 + cot²θ = cosec²θ
- sin(2θ) = 2 sin θ cos θ
- cos(2θ) = cos²θ - sin²θ = 2cos²θ - 1

## Speed Calculation Tricks
- Multiply any number by 11: Sum adjacent digits.
- Squaring numbers ending in 5: (N)(N+1) followed by 25.
                    """.trimIndent(),
                    isDownloaded = false
                ),
                NoteEntity(
                    noteId = "note_bpsc_04",
                    title = "Bihar GK Special: Ancient to Modern History & Rivers",
                    subject = "BPSC",
                    category = "History & GK",
                    chapter = "Chapter 1: Bihar Special",
                    author = "SP Sir",
                    downloadSize = "5.1 MB",
                    isFree = false,
                    contentText = """
# BPSC & Bihar Police GS Handbook

## 1. Historical Facts
- Bihar formed on 22 March 1912 (Bihar Diwas).
- First President: Dr. Rajendra Prasad (Siwan).
- Nalanda University established by Kumaragupta I (5th Century).
- Vikramshila University established by King Dharmapala.

## 2. Geography & Rivers
- Ganga flows through 12 districts of Bihar.
- North Bihar Rivers: Gandak, Burhi Gandak, Bagmati, Kosi (Sorrow of Bihar).
- South Bihar Rivers: Sone, Punpun, Phalgu.
                    """.trimIndent(),
                    isDownloaded = false
                ),
                NoteEntity(
                    noteId = "note_bio_05",
                    title = "Biology: Human Organ Systems & Genetics Summary",
                    subject = "Biology",
                    category = "Anatomy",
                    chapter = "Chapter 4: Human Physiology",
                    author = "Dr. Priya Sharma",
                    downloadSize = "3.8 MB",
                    isFree = true,
                    contentText = """
# Human Biology Essentials

## Circulatory System
- Human heart: 4 chambers (2 Atria, 2 Ventricles).
- SA Node: Natural pacemaker of the heart.
- Blood Group: Karl Landsteiner discovered ABO system. O- is universal donor, AB+ is universal recipient.

## Genetics
- Mendel's Laws: Law of Segregation & Law of Independent Assortment.
- DNA Double Helix: Watson and Crick (1953).
                    """.trimIndent(),
                    isDownloaded = false
                )
            )
            database.noteDao().insertNotes(initialNotes)

            // Seed Tests
            val initialTests = listOf(
                TestEntity(
                    testId = "test_sp_01",
                    title = "Spoken English Fluency & Grammar Mock Test",
                    subject = "Spoken English",
                    durationMinutes = 15,
                    totalQuestions = 5,
                    isFree = true,
                    totalMarks = 50,
                    attemptsCount = 1420
                ),
                TestEntity(
                    testId = "test_phy_02",
                    title = "Physics Light & Electricity Master Quiz",
                    subject = "Physics",
                    durationMinutes = 20,
                    totalQuestions = 5,
                    isFree = true,
                    totalMarks = 50,
                    attemptsCount = 980
                ),
                TestEntity(
                    testId = "test_bpsc_03",
                    title = "BPSC 2026 Prelims Special GS Mock Test",
                    subject = "BPSC",
                    durationMinutes = 25,
                    totalQuestions = 5,
                    isFree = false,
                    totalMarks = 50,
                    attemptsCount = 2300
                ),
                TestEntity(
                    testId = "test_math_04",
                    title = "Mathematics Trigonometry & Calculus Test",
                    subject = "Mathematics",
                    durationMinutes = 15,
                    totalQuestions = 5,
                    isFree = true,
                    totalMarks = 50,
                    attemptsCount = 1150
                )
            )
            database.testDao().insertTests(initialTests)

            // Seed Test Questions for Spoken English Test
            val initialQuestions = listOf(
                TestQuestionEntity(
                    questionId = "q_sp_01",
                    testId = "test_sp_01",
                    questionIndex = 1,
                    questionText = "Which sentence uses the correct preposition of time?",
                    optionA = "The lecture starts at 10:00 AM.",
                    optionB = "The lecture starts in 10:00 AM.",
                    optionC = "The lecture starts on 10:00 AM.",
                    optionD = "The lecture starts into 10:00 AM.",
                    correctOption = 0,
                    explanation = "We use 'at' for specific times of the day (e.g., at 10:00 AM, at noon)."
                ),
                TestQuestionEntity(
                    questionId = "q_sp_02",
                    testId = "test_sp_01",
                    questionIndex = 2,
                    questionText = "What does the idiom 'Call it a day' mean?",
                    optionA = "To start a new project immediately",
                    optionB = "To stop working on something for the rest of the day",
                    optionC = "To make a phone call in daylight",
                    optionD = "To celebrate a festival",
                    correctOption = 1,
                    explanation = "'Call it a day' means deciding to finish working for the day."
                ),
                TestQuestionEntity(
                    questionId = "q_sp_03",
                    testId = "test_sp_01",
                    questionIndex = 3,
                    questionText = "Choose the correct sentence in Present Perfect Continuous tense:",
                    optionA = "She is living here since 5 years.",
                    optionB = "She has been living here for 5 years.",
                    optionC = "She had lived here since 5 years.",
                    optionD = "She lives here from 5 years.",
                    correctOption = 1,
                    explanation = "Present Perfect Continuous uses 'has/have been + verb-ing' and 'for' for a duration (5 years)."
                ),
                TestQuestionEntity(
                    questionId = "q_sp_04",
                    testId = "test_sp_01",
                    questionIndex = 4,
                    questionText = "Which word is an antonym (opposite) of 'Meticulous'?",
                    optionA = "Careful",
                    optionB = "Careless",
                    optionC = "Thorough",
                    optionD = "Precise",
                    correctOption = 1,
                    explanation = "Meticulous means very careful and precise; its opposite is careless or negligent."
                ),
                TestQuestionEntity(
                    questionId = "q_sp_05",
                    testId = "test_sp_01",
                    questionIndex = 5,
                    questionText = "Complete the sentence: 'If I were you, I _____ accept the scholarship.'",
                    optionA = "will",
                    optionB = "would",
                    optionC = "can",
                    optionD = "shall",
                    correctOption = 1,
                    explanation = "Second conditional hypothetical statements use 'If + were ..., would + base verb'."
                ),
                // Physics Test Questions
                TestQuestionEntity(
                    questionId = "q_phy_01",
                    testId = "test_phy_02",
                    questionIndex = 1,
                    questionText = "What is the focal length of a plane mirror?",
                    optionA = "Zero",
                    optionB = "Infinity",
                    optionC = "25 cm",
                    optionD = "-10 cm",
                    correctOption = 1,
                    explanation = "A plane mirror has no curvature, hence its radius of curvature and focal length are infinite."
                ),
                TestQuestionEntity(
                    questionId = "q_phy_02",
                    testId = "test_phy_02",
                    questionIndex = 2,
                    questionText = "The SI unit of electric resistivity (ρ) is:",
                    optionA = "Ohm (Ω)",
                    optionB = "Ohm-meter (Ω·m)",
                    optionC = "Ampere / Volt",
                    optionD = "Watt-hour",
                    correctOption = 1,
                    explanation = "Resistivity ρ = (R * A) / L = (Ω * m²) / m = Ω·m."
                ),
                TestQuestionEntity(
                    questionId = "q_phy_03",
                    testId = "test_phy_02",
                    questionIndex = 3,
                    questionText = "When light enters from air into glass, which property remains unchanged?",
                    optionA = "Speed",
                    optionB = "Wavelength",
                    optionC = "Frequency",
                    optionD = "Amplitude",
                    correctOption = 2,
                    explanation = "Frequency depends purely on the source of light and remains constant during refraction."
                ),
                TestQuestionEntity(
                    questionId = "q_phy_04",
                    testId = "test_phy_02",
                    questionIndex = 4,
                    questionText = "A convex lens of power +4.0 D has a focal length of:",
                    optionA = "+25 cm",
                    optionB = "-25 cm",
                    optionC = "+40 cm",
                    optionD = "+100 cm",
                    correctOption = 0,
                    explanation = "f = 1/P = 1/4 m = 0.25 m = 25 cm."
                ),
                TestQuestionEntity(
                    questionId = "q_phy_05",
                    testId = "test_phy_02",
                    questionIndex = 5,
                    questionText = "Which device converts mechanical energy into electrical energy using electromagnetic induction?",
                    optionA = "Electric Motor",
                    optionB = "Electric Generator (Dynamo)",
                    optionC = "Transformer",
                    optionD = "Galvanometer",
                    correctOption = 1,
                    explanation = "An electric generator (dynamo) converts mechanical rotation into electricity via Faraday's law."
                ),
                // BPSC Questions
                TestQuestionEntity(
                    questionId = "q_bpsc_01",
                    testId = "test_bpsc_03",
                    questionIndex = 1,
                    questionText = "In which year was Bihar separated from the Bengal Presidency?",
                    optionA = "1905",
                    optionB = "1912",
                    optionC = "1936",
                    optionD = "1947",
                    correctOption = 1,
                    explanation = "Bihar was created as a separate province on March 22, 1912."
                ),
                TestQuestionEntity(
                    questionId = "q_bpsc_02",
                    testId = "test_bpsc_03",
                    questionIndex = 2,
                    questionText = "Who among the following founded the ancient Nalanda University?",
                    optionA = "Samudragupta",
                    optionB = "Kumaragupta I",
                    optionC = "Chandragupta Maurya",
                    optionD = "Harshavardhana",
                    correctOption = 1,
                    explanation = "Kumaragupta I of the Gupta dynasty founded Nalanda Mahavihara in the 5th century."
                ),
                TestQuestionEntity(
                    questionId = "q_bpsc_03",
                    testId = "test_bpsc_03",
                    questionIndex = 3,
                    questionText = "Which river is widely known as the 'Sorrow of Bihar' due to frequent flooding?",
                    optionA = "Gandak",
                    optionB = "Bagmati",
                    optionC = "Kosi",
                    optionD = "Mahananda",
                    correctOption = 2,
                    explanation = "The Kosi river frequently shifts its course causing massive flooding, earning the title 'Sorrow of Bihar'."
                ),
                TestQuestionEntity(
                    questionId = "q_bpsc_04",
                    testId = "test_bpsc_03",
                    questionIndex = 4,
                    questionText = "Mahavira, the 24th Tirthankara of Jainism, attained Nirvana at:",
                    optionA = "Kundagrama",
                    optionB = "Pawapuri",
                    optionC = "Rajgir",
                    optionD = "Vaishali",
                    correctOption = 1,
                    explanation = "Lord Mahavira attained Nirvana at Pawapuri (near Nalanda, Bihar)."
                ),
                TestQuestionEntity(
                    questionId = "q_bpsc_05",
                    testId = "test_bpsc_03",
                    questionIndex = 5,
                    questionText = "The Champaran Satyagraha of Mahatma Gandhi in 1917 was related to:",
                    optionA = "Salt Tax",
                    optionB = "Tinkathia Indigo Farming System",
                    optionC = "Mill Workers strike",
                    optionD = "Forest laws",
                    correctOption = 1,
                    explanation = "Gandhiji launched Champaran Satyagraha against the oppressive Tinkathia system where farmers had to grow indigo on 3/20th of their land."
                ),
                // Math Test Questions
                TestQuestionEntity(
                    questionId = "q_math_01",
                    testId = "test_math_04",
                    questionIndex = 1,
                    questionText = "What is the value of sin²(30°) + cos²(30°)?",
                    optionA = "0.5",
                    optionB = "1.0",
                    optionC = "0.75",
                    optionD = "2.0",
                    correctOption = 1,
                    explanation = "For any angle θ, sin²θ + cos²θ = 1 identically."
                ),
                TestQuestionEntity(
                    questionId = "q_math_02",
                    testId = "test_math_04",
                    questionIndex = 2,
                    questionText = "If tan θ = 4/3, what is the value of sec θ?",
                    optionA = "5/3",
                    optionB = "3/5",
                    optionC = "5/4",
                    optionD = "4/5",
                    correctOption = 0,
                    explanation = "sec²θ = 1 + tan²θ = 1 + 16/9 = 25/9 => sec θ = 5/3."
                ),
                TestQuestionEntity(
                    questionId = "q_math_03",
                    testId = "test_math_04",
                    questionIndex = 3,
                    questionText = "The derivative of sin(2x) with respect to x is:",
                    optionA = "cos(2x)",
                    optionB = "2 cos(2x)",
                    optionC = "-2 cos(2x)",
                    optionD = "-cos(2x)",
                    correctOption = 1,
                    explanation = "d/dx [sin(2x)] = cos(2x) * d/dx(2x) = 2 cos(2x)."
                ),
                TestQuestionEntity(
                    questionId = "q_math_04",
                    testId = "test_math_04",
                    questionIndex = 4,
                    questionText = "What is the roots of quadratic equation x² - 5x + 6 = 0?",
                    optionA = "2 and 3",
                    optionB = "-2 and -3",
                    optionC = "1 and 6",
                    optionD = "-1 and -6",
                    correctOption = 0,
                    explanation = "(x - 2)(x - 3) = 0 => x = 2, 3."
                ),
                TestQuestionEntity(
                    questionId = "q_math_05",
                    testId = "test_math_04",
                    questionIndex = 5,
                    questionText = "The sum of the first 20 natural numbers (1 + 2 + ... + 20) is:",
                    optionA = "190",
                    optionB = "200",
                    optionC = "210",
                    optionD = "220",
                    correctOption = 2,
                    explanation = "Sum = n(n+1)/2 = (20 * 21)/2 = 210."
                )
            )
            database.testDao().insertQuestions(initialQuestions)

            // Seed Notices
            val initialNotices = listOf(
                NoticeEntity(
                    noticeId = "not_01",
                    title = "🔴 Live Special Doubt Masterclass with SP Sir this Sunday at 10 AM",
                    category = "Live Class",
                    dateText = "24 Aug 2026",
                    content = "All enrolled and guest students are invited to join the 2-hour Live Doubt Clearing session covering Spoken English sentence structures and Physics optics.",
                    isImportant = true
                ),
                NoticeEntity(
                    noticeId = "not_02",
                    title = "📢 New Batch Alert: BPSC 71st Prelims & Bihar Police SI 2026 Batch Started",
                    category = "Course",
                    dateText = "22 Aug 2026",
                    content = "Registrations are now open for the comprehensive GS batch with daily mock tests, PDF chapter notes, and personalized AI mentor support.",
                    isImportant = true
                ),
                NoticeEntity(
                    noticeId = "not_03",
                    title = "📝 Free All-India Spoken English & Physics Scholarship Test Live Now",
                    category = "Exam",
                    dateText = "20 Aug 2026",
                    content = "Participate in the online mock test from the 'Free Test' tab to evaluate your score and unlock course fee discounts up to 100%.",
                    isImportant = false
                ),
                NoticeEntity(
                    noticeId = "not_04",
                    title = "✨ Offline Lecture & PDF Notes Downloading Now Available",
                    category = "General",
                    dateText = "18 Aug 2026",
                    content = "Students can now download video lectures and high-yield notes for offline learning without consuming mobile data on repeat study.",
                    isImportant = false
                )
            )
            database.noticeDao().insertNotices(initialNotices)

            // Seed Social Community Posts
            val initialPosts = listOf(
                SocialPostEntity(
                    postId = "post_01",
                    authorName = "SP Sir (Mithila Academy)",
                    authorRole = "Teacher",
                    authorAvatar = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                    timeAgo = "1 hour ago",
                    content = "🌟 Tip of the day for Spoken English: Practice thinking directly in English for 15 minutes daily instead of translating from Hindi. Ask your doubts in the AI Doubts tab!",
                    likesCount = 142,
                    isLiked = true,
                    commentsCount = 28,
                    subjectTag = "Spoken English"
                ),
                SocialPostEntity(
                    postId = "post_02",
                    authorName = "Rahul Verma",
                    authorRole = "Student",
                    authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                    timeAgo = "3 hours ago",
                    content = "Hello friends! I just solved the Physics Optics mock test and scored 40/50. The ray diagram explanations by SP Sir are super clear. Anyone preparing for BPSC Prelims?",
                    likesCount = 89,
                    isLiked = false,
                    commentsCount = 15,
                    subjectTag = "Physics"
                ),
                SocialPostEntity(
                    postId = "post_03",
                    authorName = "Dr. Priya Sharma",
                    authorRole = "Teacher",
                    authorAvatar = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200",
                    timeAgo = "5 hours ago",
                    content = "Biology Class 12 handwritten chapter summary for Genetics is now uploaded in the Notes section. Make sure to download and practice the monohybrid cross diagrams.",
                    likesCount = 210,
                    isLiked = true,
                    commentsCount = 42,
                    subjectTag = "Biology"
                )
            )
            database.socialDao().insertPosts(initialPosts)

            // Seed Initial Downloaded Items
            val initialDownloads = listOf(
                DownloadedItemEntity(
                    downloadId = "dl_note_01",
                    title = "500 Essential Spoken English Vocabulary & Daily Idioms",
                    type = "Note",
                    subject = "Spoken English",
                    sizeText = "3.2 MB",
                    downloadedAt = System.currentTimeMillis() - 3600000L,
                    referenceId = "note_sp_01"
                ),
                DownloadedItemEntity(
                    downloadId = "dl_note_02",
                    title = "Physics Formula Cheat Sheet & Derivations (Class 10-12)",
                    type = "Note",
                    subject = "Physics",
                    sizeText = "4.8 MB",
                    downloadedAt = System.currentTimeMillis() - 7200000L,
                    referenceId = "note_phy_02"
                )
            )
            initialDownloads.forEach { dl ->
                database.downloadDao().insertDownload(dl)
            }
        }
    }
}

