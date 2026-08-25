package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val role: String, // "student" or "teacher"
    val profilePic: String,
    val createdAt: String,
    val subscriberCount: Int,
    val bio: String,
    val subjectSpecialty: String
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val videoId: String,
    val teacherId: String,
    val teacherName: String,
    val teacherProfilePic: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val views: Int,
    val likes: Int,
    val isLiked: Boolean,
    val createdAt: String,
    val subject: String,
    val durationMinutes: Int,
    val resolution: String
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val subscriptionId: String,
    val studentId: String,
    val teacherId: String,
    val subscribedAt: String
)

@Entity(tableName = "live_streams")
data class LiveStreamEntity(
    @PrimaryKey val streamId: String,
    val teacherId: String,
    val teacherName: String,
    val teacherProfilePic: String,
    val title: String,
    val streamKey: String,
    val status: String, // "active" or "ended"
    val startedAt: String,
    val viewerCount: Int,
    val subject: String
)

@Entity(tableName = "chats")
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val streamId: String, // streamId or videoId or ai_chat_id
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val messageText: String,
    val timestamp: String,
    val isAiResponse: Boolean
)

@Entity(tableName = "ai_chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val sessionId: String,
    val title: String,
    val subject: String = "All",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 1,
    val previewSnippet: String = ""
)

@Entity(
    tableName = "ai_chat_messages",
    indices = [Index(value = ["sessionId"])]
)
data class AiChatMessageEntity(
    @PrimaryKey val messageId: String,
    val sessionId: String,
    val sender: String,
    val role: String, // "ai", "user"
    val text: String,
    val isAi: Boolean,
    val timestampFormatted: String,
    val timestampEpoch: Long = System.currentTimeMillis(),
    val subjectTag: String = ""
)

@Entity(tableName = "knowledge_items")
data class KnowledgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String, // "Physics", "Chemistry", "Biology", "Mathematics", "GK", "Railway", "SSC", "BPSC", "Bihar Police", "General"
    val content: String,
    val keywords: String,
    val addedBy: String = "Mithila Academy / SP",
    val createdAt: String = "2026-08-22T10:00:00Z"
)

// --- Room DAOs ---

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'teacher'")
    fun getAllTeachers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET subscriberCount = :count WHERE userId = :teacherId")
    suspend fun updateSubscriberCount(teacherId: String, count: Int)
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getVideosByTeacher(teacherId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE videoId = :videoId LIMIT 1")
    fun getVideoById(videoId: String): Flow<VideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM videos WHERE videoId = :videoId")
    suspend fun deleteVideo(videoId: String)

    @Query("UPDATE videos SET likes = likes + 1, isLiked = 1 WHERE videoId = :videoId AND isLiked = 0")
    suspend fun likeVideo(videoId: String)

    @Query("UPDATE videos SET likes = likes - 1, isLiked = 0 WHERE videoId = :videoId AND isLiked = 1")
    suspend fun unlikeVideo(videoId: String)

    @Query("UPDATE videos SET views = views + 1 WHERE videoId = :videoId")
    suspend fun incrementViews(videoId: String)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE studentId = :studentId")
    fun getSubscriptionsForStudent(studentId: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE studentId = :studentId AND teacherId = :teacherId LIMIT 1")
    fun getSubscription(studentId: String, teacherId: String): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE studentId = :studentId AND teacherId = :teacherId")
    suspend fun deleteSubscription(studentId: String, teacherId: String)
}

@Dao
interface LiveStreamDao {
    @Query("SELECT * FROM live_streams WHERE status = 'active' ORDER BY startedAt DESC")
    fun getActiveStreams(): Flow<List<LiveStreamEntity>>

    @Query("SELECT * FROM live_streams ORDER BY startedAt DESC")
    fun getAllStreams(): Flow<List<LiveStreamEntity>>

    @Query("SELECT * FROM live_streams WHERE streamId = :streamId LIMIT 1")
    fun getStreamById(streamId: String): Flow<LiveStreamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: LiveStreamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<LiveStreamEntity>)

    @Query("UPDATE live_streams SET status = 'ended' WHERE streamId = :streamId")
    suspend fun endStream(streamId: String)

    @Query("DELETE FROM live_streams WHERE streamId = :streamId")
    suspend fun deleteStream(streamId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE streamId = :streamId ORDER BY timestamp ASC")
    fun getChatsForStream(streamId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatMessageEntity>)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_items ORDER BY createdAt DESC")
    fun getAllKnowledge(): Flow<List<KnowledgeEntity>>

    @Query("SELECT * FROM knowledge_items WHERE subject = :subject")
    fun getKnowledgeBySubject(subject: String): Flow<List<KnowledgeEntity>>

    @Query("SELECT * FROM knowledge_items WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%'")
    suspend fun searchKnowledge(query: String): List<KnowledgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(item: KnowledgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeList(items: List<KnowledgeEntity>)

    @Delete
    suspend fun deleteKnowledge(item: KnowledgeEntity)
}

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM ai_chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM ai_chat_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun getSessionById(sessionId: String): Flow<ChatSessionEntity?>

    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId ORDER BY timestampEpoch ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<AiChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId ORDER BY timestampEpoch ASC")
    suspend fun getMessagesListForSession(sessionId: String): List<AiChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ChatSessionEntity>)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("UPDATE ai_chat_sessions SET title = :newTitle, updatedAt = :updatedAt WHERE sessionId = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE ai_chat_sessions SET messageCount = :count, previewSnippet = :snippet, updatedAt = :updatedAt WHERE sessionId = :sessionId")
    suspend fun updateSessionMetadata(sessionId: String, count: Int, snippet: String, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<AiChatMessageEntity>)

    @Query("DELETE FROM ai_chat_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM ai_chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM ai_chat_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM ai_chat_messages")
    suspend fun deleteAllMessages()
}

@Entity(tableName = "touch_elements")
data class TouchElementEntity(
    @PrimaryKey val elementId: String,
    val displayName: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val customImageUri: String? = null,
    val isVisible: Boolean = true,
    val zIndex: Float = 1.0f
)

@Dao
interface TouchElementDao {
    @Query("SELECT * FROM touch_elements")
    fun getAllElements(): Flow<List<TouchElementEntity>>

    @Query("SELECT * FROM touch_elements")
    suspend fun getAllElementsList(): List<TouchElementEntity>

    @Query("SELECT * FROM touch_elements WHERE elementId = :elementId LIMIT 1")
    fun getElementById(elementId: String): Flow<TouchElementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElement(element: TouchElementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElements(elements: List<TouchElementEntity>)

    @Query("DELETE FROM touch_elements")
    suspend fun deleteAllElements()
}

// --- Educational Platform Entities ---

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseId: String,
    val title: String,
    val category: String, // "Spoken English", "Physics", "Mathematics", "BPSC", "Biology", "Chemistry", "General"
    val teacherName: String,
    val description: String,
    val duration: String,
    val lessonsCount: Int,
    val price: String, // "Free" or "₹499"
    val isFree: Boolean,
    val thumbnailUrl: String,
    val isEnrolled: Boolean = false,
    val progressPercent: Int = 0,
    val completedLessons: Int = 0,
    val rating: Float = 4.8f,
    val totalEnrolled: Int = 1250
)

@Entity(tableName = "course_lessons")
data class CourseLessonEntity(
    @PrimaryKey val lessonId: String,
    val courseId: String,
    val lessonNumber: Int,
    val title: String,
    val durationMinutes: Int,
    val videoUrl: String,
    val isCompleted: Boolean = false,
    val notesSummary: String = ""
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val noteId: String,
    val title: String,
    val subject: String,
    val category: String,
    val chapter: String,
    val author: String,
    val downloadSize: String,
    val isFree: Boolean = true,
    val contentText: String,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey val testId: String,
    val title: String,
    val subject: String,
    val durationMinutes: Int,
    val totalQuestions: Int,
    val isFree: Boolean = true,
    val totalMarks: Int = 100,
    val attemptsCount: Int = 0
)

@Entity(tableName = "test_questions")
data class TestQuestionEntity(
    @PrimaryKey val questionId: String,
    val testId: String,
    val questionIndex: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Int, // 0: A, 1: B, 2: C, 3: D
    val explanation: String
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey val resultId: String,
    val testId: String,
    val testTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val percentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_items")
data class DownloadedItemEntity(
    @PrimaryKey val downloadId: String,
    val title: String,
    val type: String, // "Note" or "Video"
    val subject: String,
    val sizeText: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val localUri: String = "",
    val referenceId: String = ""
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val noticeId: String,
    val title: String,
    val category: String, // "Exam", "Live Class", "Course", "General"
    val dateText: String,
    val content: String,
    val author: String = "Mithila Academy Admin",
    val isImportant: Boolean = false
)

@Entity(tableName = "social_posts")
data class SocialPostEntity(
    @PrimaryKey val postId: String,
    val authorName: String,
    val authorRole: String, // "Student", "Teacher", "Mentor"
    val authorAvatar: String = "",
    val timeAgo: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val subjectTag: String = "General"
)

// --- Educational Platform DAOs ---

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE isEnrolled = 1")
    fun getEnrolledCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    fun getCourseById(courseId: String): Flow<CourseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE courseId = :courseId")
    suspend fun deleteCourse(courseId: String)

    @Query("UPDATE courses SET isEnrolled = 1 WHERE courseId = :courseId")
    suspend fun enrollInCourse(courseId: String)

    @Query("UPDATE courses SET progressPercent = :progress, completedLessons = :completed WHERE courseId = :courseId")
    suspend fun updateCourseProgress(courseId: String, progress: Int, completed: Int)

    @Query("SELECT * FROM course_lessons WHERE courseId = :courseId ORDER BY lessonNumber ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<CourseLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<CourseLessonEntity>)

    @Query("UPDATE course_lessons SET isCompleted = :completed WHERE lessonId = :lessonId")
    suspend fun markLessonCompleted(lessonId: String, completed: Boolean)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY subject ASC, chapter ASC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isFree = 1")
    fun getFreeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDownloaded = 1")
    fun getDownloadedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE noteId = :noteId LIMIT 1")
    fun getNoteById(noteId: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("UPDATE notes SET isDownloaded = :downloaded WHERE noteId = :noteId")
    suspend fun updateNoteDownloadStatus(noteId: String, downloaded: Boolean)
}

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY title ASC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE isFree = 1")
    fun getFreeTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE testId = :testId LIMIT 1")
    fun getTestById(testId: String): Flow<TestEntity?>

    @Query("SELECT * FROM test_questions WHERE testId = :testId ORDER BY questionIndex ASC")
    suspend fun getQuestionsForTest(testId: String): List<TestQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestEntity>)

    @Query("DELETE FROM tests WHERE testId = :testId")
    suspend fun deleteTest(testId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<TestQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResultEntity)

    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllTestResults(): Flow<List<TestResultEntity>>
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloaded_items ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadedItemEntity)

    @Query("DELETE FROM downloaded_items WHERE downloadId = :downloadId")
    suspend fun deleteDownload(downloadId: String)

    @Query("DELETE FROM downloaded_items WHERE referenceId = :referenceId")
    suspend fun deleteDownloadByRef(referenceId: String)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY isImportant DESC, noticeId DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("DELETE FROM notices WHERE noticeId = :noticeId")
    suspend fun deleteNotice(noticeId: String)
}

@Dao
interface SocialDao {
    @Query("SELECT * FROM social_posts ORDER BY postId DESC")
    fun getAllPosts(): Flow<List<SocialPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPostEntity>)

    @Query("UPDATE social_posts SET likesCount = likesCount + 1, isLiked = 1 WHERE postId = :postId AND isLiked = 0")
    suspend fun likePost(postId: String)

    @Query("UPDATE social_posts SET likesCount = likesCount - 1, isLiked = 0 WHERE postId = :postId AND isLiked = 1")
    suspend fun unlikePost(postId: String)
}

