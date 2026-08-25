package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TouchElementEntity
import com.example.data.model.UserRole
import com.example.ui.components.TouchElementIds
import com.example.ui.components.TouchElementTransform
import com.example.ui.components.getDefaultElementTransforms
import com.example.data.local.AppDatabase
import com.example.data.local.AiChatMessageEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.KnowledgeEntity
import com.example.data.local.LiveStreamEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.local.UserEntity
import com.example.data.local.VideoEntity
import com.example.data.repository.ChatSessionRepository
import com.example.data.service.GeminiService
import com.example.util.SecurityHelper
import com.example.util.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppTab(val title: String) {
    HOME("Home"),
    MY_COURSES("My Courses"),
    DOWNLOADS("Downloads"),
    NOTICE_BOARD("Notice Board"),
    ALL_COURSES("All Courses"),
    NOTES("Notes"),
    SOCIAL("Social"),
    TEST("Test"),
    FREE_VIDEOS("Free Videos"),
    FREE_TEST("Free Test"),
    FREE_NOTES("Free Notes"),
    AI_CHAT("AI Voice Teacher"),
    LIVE_CLASS("Live Class Room"),
    VIDEOS("Videos"),
    CREATE("Teacher Panel")
}

data class ActiveTestState(
    val test: com.example.data.local.TestEntity,
    val questions: List<com.example.data.local.TestQuestionEntity>,
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex (0..3)
    val isSubmitted: Boolean = false,
    val result: com.example.data.local.TestResultEntity? = null,
    val remainingSeconds: Int = 900
)

data class UiMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "SPA AI TEACHER" or "You" or student name
    val role: String, // "ai", "user", "teacher", "student"
    val text: String,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
    val isAi: Boolean = false,
    val subjectTag: String = ""
)

class AiTeacherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val geminiService = GeminiService()
    val voiceManager = VoiceManager(application)
    val chatSessionRepository = ChatSessionRepository(db.chatSessionDao())

    // Active Navigation Tab - Defaults directly to HOME Screen
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Header Course/Category Dropdown Selection (e.g. "Spoken", "Physics", "Mathematics", "BPSC")
    private val _headerCategory = MutableStateFlow("Spoken")
    val headerCategory: StateFlow<String> = _headerCategory.asStateFlow()

    private val _showCategorySelector = MutableStateFlow(false)
    val showCategorySelector: StateFlow<Boolean> = _showCategorySelector.asStateFlow()

    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    // Downloaded offline video IDs
    private val _downloadedVideoIds = MutableStateFlow<Set<String>>(setOf("vid_bpsc_01"))
    val downloadedVideoIds: StateFlow<Set<String>> = _downloadedVideoIds.asStateFlow()

    // Teacher Profile Info (Editable from Create Tab)
    private val _teacherProfileName = MutableStateFlow("SP Sir")
    val teacherProfileName: StateFlow<String> = _teacherProfileName.asStateFlow()

    private val _teacherSpecialization = MutableStateFlow("Physics & Mathematics")
    val teacherSpecialization: StateFlow<String> = _teacherSpecialization.asStateFlow()

    private val _teacherBio = MutableStateFlow("Senior Educator at Mithila Academy. Guiding students for Board Exams, BPSC & Competitive Excellence.")
    val teacherBio: StateFlow<String> = _teacherBio.asStateFlow()

    private val _teacherQualification = MutableStateFlow("M.Sc. Physics (Gold Medalist), B.Ed.")
    val teacherQualification: StateFlow<String> = _teacherQualification.asStateFlow()

    private val brandingPrefs = application.getSharedPreferences("spa_branding_prefs", Context.MODE_PRIVATE)

    // Customizable Full-Screen AI Doubts Configuration (Owner/Admin Editable)
    private val _mainTitle = MutableStateFlow(brandingPrefs.getString("custom_main_title", "MITHILA ACADEMY") ?: "MITHILA ACADEMY")
    val mainTitle: StateFlow<String> = _mainTitle.asStateFlow()

    private val _subTitle = MutableStateFlow(brandingPrefs.getString("custom_sub_title", "AI DOUBTS • AI TEACHER") ?: "AI DOUBTS • AI TEACHER")
    val subTitle: StateFlow<String> = _subTitle.asStateFlow()

    private val _customMessage = MutableStateFlow(brandingPrefs.getString("custom_message", "...") ?: "...")
    val customMessage: StateFlow<String> = _customMessage.asStateFlow()

    private val _aiTeacherTitle = MutableStateFlow(brandingPrefs.getString("custom_ai_title", "SPA AI Teacher") ?: "SPA AI Teacher")
    val aiTeacherTitle: StateFlow<String> = _aiTeacherTitle.asStateFlow()

    private val _customBgImageUri = MutableStateFlow<String?>(brandingPrefs.getString("custom_bg_image_uri", null))
    val customBgImageUri: StateFlow<String?> = _customBgImageUri.asStateFlow()

    private val _customTeacherImageUri = MutableStateFlow<String?>(brandingPrefs.getString("custom_teacher_image_uri", null))
    val customTeacherImageUri: StateFlow<String?> = _customTeacherImageUri.asStateFlow()

    private val _customLogoImageUri = MutableStateFlow<String?>(brandingPrefs.getString("custom_logo_image_uri", null))
    val customLogoImageUri: StateFlow<String?> = _customLogoImageUri.asStateFlow()

    fun updateCustomTeacherImage(uriString: String?) {
        _customTeacherImageUri.value = uriString
        brandingPrefs.edit().putString("custom_teacher_image_uri", uriString).apply()
        _statusMessage.value = if (!uriString.isNullOrBlank()) "✅ Teacher photo updated!" else "Reset teacher photo"
    }

    fun updateCustomLogoImage(uriString: String?) {
        _customLogoImageUri.value = uriString
        brandingPrefs.edit().putString("custom_logo_image_uri", uriString).apply()
        _statusMessage.value = if (!uriString.isNullOrBlank()) "✅ Academy logo updated!" else "Reset academy logo"
    }

    fun updateCustomBgImage(uriString: String?) {
        _customBgImageUri.value = uriString
        brandingPrefs.edit().putString("custom_bg_image_uri", uriString).apply()
        _statusMessage.value = if (!uriString.isNullOrBlank()) "✅ Wallpaper image updated!" else "Reset wallpaper"
    }

    fun resetToDefaultBranding() {
        _customTeacherImageUri.value = null
        _customLogoImageUri.value = null
        _customBgImageUri.value = null
        brandingPrefs.edit()
            .remove("custom_teacher_image_uri")
            .remove("custom_logo_image_uri")
            .remove("custom_bg_image_uri")
            .apply()
        _statusMessage.value = "Restored default teacher image and logo"
    }

    fun updateCustomConfiguration(
        main: String? = null,
        sub: String? = null,
        message: String? = null,
        aiTitle: String? = null,
        bgUri: String? = null,
        teacherImageUri: String? = null,
        logoImageUri: String? = null
    ) {
        val editor = brandingPrefs.edit()
        main?.let {
            _mainTitle.value = it
            editor.putString("custom_main_title", it)
        }
        sub?.let {
            _subTitle.value = it
            editor.putString("custom_sub_title", it)
        }
        message?.let {
            _customMessage.value = it
            editor.putString("custom_message", it)
        }
        aiTitle?.let {
            _aiTeacherTitle.value = it
            editor.putString("custom_ai_title", it)
        }
        bgUri?.let {
            _customBgImageUri.value = it
            editor.putString("custom_bg_image_uri", it)
        }
        teacherImageUri?.let {
            _customTeacherImageUri.value = it
            editor.putString("custom_teacher_image_uri", it)
        }
        logoImageUri?.let {
            _customLogoImageUri.value = it
            editor.putString("custom_logo_image_uri", it)
        }
        editor.apply()
    }

    // Selected Subject filter for AI chat / Videos / Notes
    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    // Notes Search and Filter
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    private val _notesSubjectFilter = MutableStateFlow("All")
    val notesSubjectFilter: StateFlow<String> = _notesSubjectFilter.asStateFlow()

    // Active Note Reader
    private val _activeReadingNote = MutableStateFlow<com.example.data.local.NoteEntity?>(null)
    val activeReadingNote: StateFlow<com.example.data.local.NoteEntity?> = _activeReadingNote.asStateFlow()

    // Active Course Details & Lessons
    private val _activeCourseDetail = MutableStateFlow<com.example.data.local.CourseEntity?>(null)
    val activeCourseDetail: StateFlow<com.example.data.local.CourseEntity?> = _activeCourseDetail.asStateFlow()

    private val _courseLessons = MutableStateFlow<List<com.example.data.local.CourseLessonEntity>>(emptyList())
    val courseLessons: StateFlow<List<com.example.data.local.CourseLessonEntity>> = _courseLessons.asStateFlow()

    // Active Test State
    private val _activeTestState = MutableStateFlow<ActiveTestState?>(null)
    val activeTestState: StateFlow<ActiveTestState?> = _activeTestState.asStateFlow()

    // Room Database Chat Sessions Flow
    val chatSessions: StateFlow<List<ChatSessionEntity>> = chatSessionRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat Session Info
    private val _currentSessionId = MutableStateFlow("session_phy_01")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _currentSessionTitle = MutableStateFlow("Physics: Reflection & Mirror Formula")
    val currentSessionTitle: StateFlow<String> = _currentSessionTitle.asStateFlow()

    // Chat Conversation List
    private val _chatMessages = MutableStateFlow<List<UiMessage>>(
        listOf(
            UiMessage(
                sender = "SPA AI Teacher",
                role = "ai",
                text = "नमस्ते, मैं SPA AI Teacher हूँ। आपकी क्या मदद कर सकता हूँ?",
                isAi = true
            )
        )
    )
    val chatMessages: StateFlow<List<UiMessage>> = _chatMessages.asStateFlow()

    // Loading / Thinking state for AI
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Status / Toast feedback message
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Database Flows
    val allCourses: StateFlow<List<com.example.data.local.CourseEntity>> = db.courseDao().getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enrolledCourses: StateFlow<List<com.example.data.local.CourseEntity>> = db.courseDao().getEnrolledCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<com.example.data.local.NoteEntity>> = db.noteDao().getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val freeNotes: StateFlow<List<com.example.data.local.NoteEntity>> = db.noteDao().getFreeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTests: StateFlow<List<com.example.data.local.TestEntity>> = db.testDao().getAllTests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val freeTests: StateFlow<List<com.example.data.local.TestEntity>> = db.testDao().getFreeTests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<com.example.data.local.NoticeEntity>> = db.noticeDao().getAllNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialPosts: StateFlow<List<com.example.data.local.SocialPostEntity>> = db.socialDao().getAllPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDownloads: StateFlow<List<com.example.data.local.DownloadedItemEntity>> = db.downloadDao().getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoEntity>> = db.videoDao().getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveStreams: StateFlow<List<LiveStreamEntity>> = db.liveStreamDao().getActiveStreams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<UserEntity>> = db.userDao().getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKnowledge: StateFlow<List<KnowledgeEntity>> = db.knowledgeDao().getAllKnowledge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentStudentId = "student_amit_101"
    val studentSubscriptions: StateFlow<List<SubscriptionEntity>> = db.subscriptionDao()
        .getSubscriptionsForStudent(currentStudentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Active Live Stream selected for viewing & its live chat
    private val _selectedLiveStream = MutableStateFlow<LiveStreamEntity?>(null)
    val selectedLiveStream: StateFlow<LiveStreamEntity?> = _selectedLiveStream.asStateFlow()

    private val _liveStreamChats = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val liveStreamChats: StateFlow<List<ChatMessageEntity>> = _liveStreamChats.asStateFlow()

    // Selected Video for playback details
    private val _selectedVideo = MutableStateFlow<VideoEntity?>(null)
    val selectedVideo: StateFlow<VideoEntity?> = _selectedVideo.asStateFlow()

    // --- ROLE-BASED ACCESS CONTROL (RBAC) & ADMIN AUTHENTICATION ---
    private val _currentUserRole = MutableStateFlow(UserRole.STUDENT)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _showAdminPasscodeDialog = MutableStateFlow(false)
    val showAdminPasscodeDialog: StateFlow<Boolean> = _showAdminPasscodeDialog.asStateFlow()

    private val _adminPasscodeInput = MutableStateFlow("")
    val adminPasscodeInput: StateFlow<String> = _adminPasscodeInput.asStateFlow()

    private val _adminPasscodeError = MutableStateFlow<String?>(null)
    val adminPasscodeError: StateFlow<String?> = _adminPasscodeError.asStateFlow()

    private var pendingAdminTab: AppTab? = null

    // Owner / Legacy Learning Panel State
    val isOwnerAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated
    val ownerPinInput: StateFlow<String> = _adminPasscodeInput
    val ownerPinError: StateFlow<String?> = _adminPasscodeError

    // --- TOUCH EDITING MODE FOR MOBILE INTERFACE ---
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedElementId = MutableStateFlow<String?>(null)
    val selectedElementId: StateFlow<String?> = _selectedElementId.asStateFlow()

    private val _elementTransforms = MutableStateFlow<Map<String, TouchElementTransform>>(getDefaultElementTransforms())
    val elementTransforms: StateFlow<Map<String, TouchElementTransform>> = _elementTransforms.asStateFlow()

    init {
        // Initial setup and database check
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.seedDatabase(db)
            // Load messages for initial active session from Room
            val initialDbMessages = chatSessionRepository.getMessagesListForSession(_currentSessionId.value)
            if (initialDbMessages.isNotEmpty()) {
                _chatMessages.value = initialDbMessages.map { entity ->
                    UiMessage(
                        id = entity.messageId,
                        sender = entity.sender,
                        role = entity.role,
                        text = entity.text,
                        timestamp = entity.timestampFormatted,
                        isAi = entity.isAi,
                        subjectTag = entity.subjectTag
                    )
                }
            }

            // Load saved Touch Layout transforms from Room Database
            val savedTransforms = db.touchElementDao().getAllElementsList()
            if (savedTransforms.isNotEmpty()) {
                val currentDefaults = getDefaultElementTransforms().toMutableMap()
                savedTransforms.forEach { saved ->
                    currentDefaults[saved.elementId] = TouchElementTransform(
                        elementId = saved.elementId,
                        displayName = saved.displayName,
                        offsetX = saved.offsetX,
                        offsetY = saved.offsetY,
                        scale = saved.scale,
                        rotation = saved.rotation,
                        customImageUri = saved.customImageUri,
                        isVisible = saved.isVisible,
                        zIndex = saved.zIndex
                    )
                }
                _elementTransforms.value = currentDefaults
            }
        }
    }

    // --- Touch Editing Mode Actions ---
    fun toggleTouchEditMode() {
        _isEditMode.value = !_isEditMode.value
        if (!_isEditMode.value) {
            _selectedElementId.value = null
            _statusMessage.value = "Normal Mode Active: Changes applied"
        } else {
            _statusMessage.value = "Touch Edit Mode Active: Drag, pinch & rotate elements"
        }
    }

    fun selectTouchElement(elementId: String?) {
        _selectedElementId.value = elementId
    }

    fun updateTouchElementTransform(elementId: String, offsetX: Float, offsetY: Float, scale: Float, rotation: Float) {
        val current = _elementTransforms.value.toMutableMap()
        val existing = current[elementId] ?: return
        current[elementId] = existing.copy(
            offsetX = offsetX,
            offsetY = offsetY,
            scale = scale,
            rotation = rotation
        )
        _elementTransforms.value = current
    }

    fun updateTouchElementImage(elementId: String, imageUri: String) {
        val current = _elementTransforms.value.toMutableMap()
        val existing = current[elementId] ?: return
        current[elementId] = existing.copy(
            customImageUri = imageUri,
            isVisible = true
        )
        _elementTransforms.value = current
        _statusMessage.value = "Image updated for ${existing.displayName}"
        saveTouchLayout()
    }

    fun resetTouchElement(elementId: String) {
        val defaults = getDefaultElementTransforms()
        val defaultTransform = defaults[elementId] ?: return
        val current = _elementTransforms.value.toMutableMap()
        current[elementId] = defaultTransform
        _elementTransforms.value = current
        _statusMessage.value = "Reset ${defaultTransform.displayName} to default position"
    }

    fun toggleTouchElementVisibility(elementId: String) {
        val current = _elementTransforms.value.toMutableMap()
        val existing = current[elementId] ?: return
        current[elementId] = existing.copy(isVisible = !existing.isVisible)
        _elementTransforms.value = current
    }

    fun saveTouchLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            val entities = _elementTransforms.value.values.map { transform ->
                TouchElementEntity(
                    elementId = transform.elementId,
                    displayName = transform.displayName,
                    offsetX = transform.offsetX,
                    offsetY = transform.offsetY,
                    scale = transform.scale,
                    rotation = transform.rotation,
                    customImageUri = transform.customImageUri,
                    isVisible = transform.isVisible,
                    zIndex = transform.zIndex
                )
            }
            db.touchElementDao().insertElements(entities)
            _statusMessage.value = "💾 Layout saved successfully to local database!"
        }
    }

    fun resetAllTouchLayouts() {
        viewModelScope.launch(Dispatchers.IO) {
            db.touchElementDao().deleteAllElements()
            _elementTransforms.value = getDefaultElementTransforms()
            _selectedElementId.value = null
            _statusMessage.value = "🔄 Restored all elements to original layout"
        }
    }

    fun addCustomSticker(imageUri: String) {
        val current = _elementTransforms.value.toMutableMap()
        val stickerTransform = current[TouchElementIds.CUSTOM_STICKER] ?: TouchElementTransform(
            elementId = TouchElementIds.CUSTOM_STICKER,
            displayName = "Custom Sticker",
            isImageElement = true
        )
        current[TouchElementIds.CUSTOM_STICKER] = stickerTransform.copy(
            customImageUri = imageUri,
            isVisible = true,
            scale = 1.0f,
            offsetX = 0f,
            offsetY = -50f
        )
        _elementTransforms.value = current
        _selectedElementId.value = TouchElementIds.CUSTOM_STICKER
        _statusMessage.value = "Sticker added! Drag and pinch to position."
    }


    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setHeaderCategory(category: String) {
        _headerCategory.value = category
        _showCategorySelector.value = false
        _statusMessage.value = "Selected: $category"
    }

    fun toggleCategorySelector(show: Boolean? = null) {
        _showCategorySelector.value = show ?: !_showCategorySelector.value
    }

    fun toggleNotificationsDialog(show: Boolean? = null) {
        _showNotificationsDialog.value = show ?: !_showNotificationsDialog.value
    }

    fun setNotesSearchQuery(query: String) {
        _notesSearchQuery.value = query
    }

    fun setNotesSubjectFilter(subject: String) {
        _notesSubjectFilter.value = subject
    }

    fun openNoteReader(note: com.example.data.local.NoteEntity) {
        _activeReadingNote.value = note
    }

    fun closeNoteReader() {
        _activeReadingNote.value = null
    }

    fun downloadNote(note: com.example.data.local.NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.noteDao().updateNoteDownloadStatus(note.noteId, true)
            val downloadItem = com.example.data.local.DownloadedItemEntity(
                downloadId = "dl_${note.noteId}",
                title = note.title,
                type = "Note",
                subject = note.subject,
                sizeText = note.downloadSize,
                downloadedAt = System.currentTimeMillis(),
                referenceId = note.noteId
            )
            db.downloadDao().insertDownload(downloadItem)
            _statusMessage.value = "✅ Downloaded for offline access: ${note.title}"
        }
    }

    fun deleteDownload(downloadId: String, referenceId: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            db.downloadDao().deleteDownload(downloadId)
            if (referenceId.isNotBlank()) {
                db.noteDao().updateNoteDownloadStatus(referenceId, false)
            }
            _statusMessage.value = "Download removed from local storage"
        }
    }

    fun openCourseDetail(course: com.example.data.local.CourseEntity) {
        _activeCourseDetail.value = course
        viewModelScope.launch(Dispatchers.IO) {
            db.courseDao().getLessonsForCourse(course.courseId).collect { lessons ->
                _courseLessons.value = lessons
            }
        }
    }

    fun closeCourseDetail() {
        _activeCourseDetail.value = null
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.courseDao().enrollInCourse(courseId)
            _statusMessage.value = "🎉 Congratulations! Enrolled successfully."
        }
    }

    fun markLessonComplete(lessonId: String, courseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.courseDao().markLessonCompleted(lessonId, true)
            val currentLessons = _courseLessons.value
            val total = currentLessons.size.coerceAtLeast(1)
            val completed = currentLessons.count { it.lessonId == lessonId || it.isCompleted }
            val progress = ((completed.toFloat() / total) * 100).toInt()
            db.courseDao().updateCourseProgress(courseId, progress, completed)
            _statusMessage.value = "Lesson completed! Progress: $progress%"
        }
    }

    // --- TEST SYSTEM ---
    fun startTest(test: com.example.data.local.TestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val questions = db.testDao().getQuestionsForTest(test.testId)
            _activeTestState.value = ActiveTestState(
                test = test,
                questions = questions,
                currentQuestionIndex = 0,
                userAnswers = emptyMap(),
                isSubmitted = false,
                result = null,
                remainingSeconds = test.durationMinutes * 60
            )
        }
    }

    fun selectTestAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _activeTestState.value ?: return
        val updatedAnswers = current.userAnswers.toMutableMap()
        updatedAnswers[questionIndex] = optionIndex
        _activeTestState.value = current.copy(userAnswers = updatedAnswers)
    }

    fun clearCurrentQuestionAnswer(questionIndex: Int) {
        val current = _activeTestState.value ?: return
        val updatedAnswers = current.userAnswers.toMutableMap()
        updatedAnswers.remove(questionIndex)
        _activeTestState.value = current.copy(userAnswers = updatedAnswers)
    }

    fun setTestQuestionIndex(index: Int) {
        val current = _activeTestState.value ?: return
        if (index in current.questions.indices) {
            _activeTestState.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun submitTest() {
        val current = _activeTestState.value ?: return
        val questions = current.questions
        var correct = 0
        var incorrect = 0
        questions.forEachIndexed { idx, q ->
            val userSelected = current.userAnswers[idx]
            if (userSelected != null) {
                if (userSelected == q.correctOption) {
                    correct++
                } else {
                    incorrect++
                }
            }
        }
        val total = questions.size.coerceAtLeast(1)
        val score = (correct * (current.test.totalMarks / total))
        val percentage = ((correct.toFloat() / total) * 100).toInt()

        val resultEntity = com.example.data.local.TestResultEntity(
            resultId = "res_${UUID.randomUUID()}",
            testId = current.test.testId,
            testTitle = current.test.title,
            score = score,
            totalQuestions = total,
            correctCount = correct,
            incorrectCount = incorrect,
            percentage = percentage,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            db.testDao().insertTestResult(resultEntity)
            _activeTestState.value = current.copy(
                isSubmitted = true,
                result = resultEntity
            )
            _statusMessage.value = "Test Submitted! Score: $score/${current.test.totalMarks} ($percentage%)"
        }
    }

    fun closeTest() {
        _activeTestState.value = null
    }

    // --- SOCIAL / COMMUNITY ---
    fun likeSocialPost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = socialPosts.value.find { it.postId == postId }
            if (post != null) {
                if (post.isLiked) {
                    db.socialDao().unlikePost(postId)
                } else {
                    db.socialDao().likePost(postId)
                }
            }
        }
    }

    fun createSocialPost(content: String, tag: String = "General") {
        if (content.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val newPost = com.example.data.local.SocialPostEntity(
                postId = "post_${UUID.randomUUID()}",
                authorName = "Amit Kumar (You)",
                authorRole = "Student",
                authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                timeAgo = "Just now",
                content = content.trim(),
                likesCount = 1,
                isLiked = true,
                commentsCount = 0,
                subjectTag = tag
            )
            db.socialDao().insertPost(newPost)
            _statusMessage.value = "Post shared with study community!"
        }
    }

    fun setSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }


    // --- Session Management (Room Database Persistence) ---

    fun loadChatSession(session: ChatSessionEntity) {
        _currentSessionId.value = session.sessionId
        _currentSessionTitle.value = session.title
        _selectedSubject.value = session.subject
        viewModelScope.launch(Dispatchers.IO) {
            val dbMessages = chatSessionRepository.getMessagesListForSession(session.sessionId)
            if (dbMessages.isNotEmpty()) {
                _chatMessages.value = dbMessages.map { entity ->
                    UiMessage(
                        id = entity.messageId,
                        sender = entity.sender,
                        role = entity.role,
                        text = entity.text,
                        timestamp = entity.timestampFormatted,
                        isAi = entity.isAi,
                        subjectTag = entity.subjectTag
                    )
                }
            } else {
                val defaultGreeting = UiMessage(
                    sender = "SPA AI Teacher",
                    role = "ai",
                    text = "नमस्ते, मैं SPA AI Teacher हूँ। आपकी क्या मदद कर सकता हूँ?",
                    isAi = true,
                    subjectTag = session.subject
                )
                _chatMessages.value = listOf(defaultGreeting)
                chatSessionRepository.saveMessage(
                    AiChatMessageEntity(
                        messageId = defaultGreeting.id,
                        sessionId = session.sessionId,
                        sender = defaultGreeting.sender,
                        role = defaultGreeting.role,
                        text = defaultGreeting.text,
                        isAi = defaultGreeting.isAi,
                        timestampFormatted = defaultGreeting.timestamp,
                        subjectTag = defaultGreeting.subjectTag
                    )
                )
            }
        }
    }

    fun createNewChatSession(subject: String = _selectedSubject.value) {
        val newSessionId = "session_" + UUID.randomUUID().toString().take(8)
        val initialTitle = if (subject == "All") "New Study Session" else "$subject Doubt Session"
        val greeting = UiMessage(
            sender = "SPA AI Teacher",
            role = "ai",
            text = "नमस्ते, मैं SPA AI Teacher हूँ। ${if (subject != "All") "$subject से संबंधित " else ""}आपकी क्या मदद कर सकता हूँ?",
            isAi = true,
            subjectTag = subject
        )

        _currentSessionId.value = newSessionId
        _currentSessionTitle.value = initialTitle
        _chatMessages.value = listOf(greeting)

        viewModelScope.launch(Dispatchers.IO) {
            val sessionEntity = ChatSessionEntity(
                sessionId = newSessionId,
                title = initialTitle,
                subject = subject,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                messageCount = 1,
                previewSnippet = greeting.text.take(60)
            )
            chatSessionRepository.insertSession(sessionEntity)
            chatSessionRepository.saveMessage(
                AiChatMessageEntity(
                    messageId = greeting.id,
                    sessionId = newSessionId,
                    sender = greeting.sender,
                    role = greeting.role,
                    text = greeting.text,
                    isAi = greeting.isAi,
                    timestampFormatted = greeting.timestamp,
                    subjectTag = greeting.subjectTag
                )
            )
        }
    }

    fun deleteChatSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatSessionRepository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                // If deleted active session, load next available or create a new one
                val remaining = chatSessionRepository.allSessions.firstOrNull()
                val nextSession = remaining?.firstOrNull()
                if (nextSession != null) {
                    loadChatSession(nextSession)
                } else {
                    createNewChatSession(_selectedSubject.value)
                }
            }
        }
    }

    fun renameChatSession(sessionId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isBlank()) return
        if (_currentSessionId.value == sessionId) {
            _currentSessionTitle.value = trimmed
        }
        viewModelScope.launch(Dispatchers.IO) {
            chatSessionRepository.updateSessionTitle(sessionId, trimmed)
        }
    }

    // --- AI Teacher Chat Action ---
    fun sendUserMessage(inputText: String) {
        val text = inputText.trim()
        if (text.isBlank() || _isAiThinking.value) return

        val activeSessionId = _currentSessionId.value
        val userMsg = UiMessage(
            sender = "You",
            role = "user",
            text = text,
            isAi = false,
            subjectTag = _selectedSubject.value
        )
        val updatedMessages = _chatMessages.value + userMsg
        _chatMessages.value = updatedMessages

        _isAiThinking.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check & ensure session exists or update title if initial
                val isFirstUserQuery = updatedMessages.count { !it.isAi } == 1
                val sessionTitle = if (isFirstUserQuery) {
                    val promptExcerpt = if (text.length > 32) text.take(30) + "..." else text
                    val prefix = if (_selectedSubject.value != "All") "${_selectedSubject.value}: " else ""
                    "$prefix$promptExcerpt"
                } else {
                    _currentSessionTitle.value
                }

                if (isFirstUserQuery) {
                    _currentSessionTitle.value = sessionTitle
                }

                // Save or update session entity in Room
                val sessionEntity = ChatSessionEntity(
                    sessionId = activeSessionId,
                    title = sessionTitle,
                    subject = _selectedSubject.value,
                    updatedAt = System.currentTimeMillis(),
                    messageCount = updatedMessages.size,
                    previewSnippet = text.take(60)
                )
                chatSessionRepository.insertSession(sessionEntity)

                // Save user message in Room
                chatSessionRepository.saveMessage(
                    AiChatMessageEntity(
                        messageId = userMsg.id,
                        sessionId = activeSessionId,
                        sender = userMsg.sender,
                        role = userMsg.role,
                        text = userMsg.text,
                        isAi = userMsg.isAi,
                        timestampFormatted = userMsg.timestamp,
                        subjectTag = userMsg.subjectTag
                    )
                )

                // 1. Search local knowledge items for RAG grounding
                val keywords = text.split(" ", "?", ",", ".").filter { it.length > 2 }
                val retrievedKnowledge = mutableListOf<KnowledgeEntity>()
                for (kw in keywords.take(5)) {
                    val items = db.knowledgeDao().searchKnowledge(kw)
                    retrievedKnowledge.addAll(items)
                }
                val distinctKnowledge = retrievedKnowledge.distinctBy { it.id }

                // 2. Prepare conversation history
                val history = updatedMessages.takeLast(6).map { it.role to it.text }

                // 3. Generate response
                val aiResponse = geminiService.generateAiTeacherResponse(
                    userPrompt = text,
                    retrievedKnowledge = distinctKnowledge,
                    conversationHistory = history
                )

                val aiMsg = UiMessage(
                    sender = "SPA AI TEACHER",
                    role = "ai",
                    text = aiResponse,
                    isAi = true,
                    subjectTag = _selectedSubject.value
                )

                val finalMessages = updatedMessages + aiMsg
                _chatMessages.value = finalMessages
                _isAiThinking.value = false

                // Save AI response message in Room
                chatSessionRepository.saveMessage(
                    AiChatMessageEntity(
                        messageId = aiMsg.id,
                        sessionId = activeSessionId,
                        sender = aiMsg.sender,
                        role = aiMsg.role,
                        text = aiMsg.text,
                        isAi = aiMsg.isAi,
                        timestampFormatted = aiMsg.timestamp,
                        subjectTag = aiMsg.subjectTag
                    )
                )

                // Update session metadata in Room
                chatSessionRepository.updateSessionMetadata(
                    sessionId = activeSessionId,
                    count = finalMessages.size,
                    snippet = aiResponse.take(60)
                )

                // 4. Voice output (if TTS enabled)
                voiceManager.speak(aiResponse)

            } catch (e: Exception) {
                _isAiThinking.value = false
                val errorMsg = UiMessage(
                    sender = "SPA AI Teacher",
                    role = "ai",
                    text = "नमस्ते, एक तकनीकी त्रुटि हुई है। कृपया पुनः प्रयास करें।\nत्रुटि विवरण: ${e.localizedMessage ?: "Unknown error"}",
                    isAi = true
                )
                _chatMessages.value = updatedMessages + errorMsg
            }
        }
    }

    fun clearChat() {
        val activeSessionId = _currentSessionId.value
        val resetGreeting = UiMessage(
            sender = "SPA AI Teacher",
            role = "ai",
            text = "नमस्ते, मैं SPA AI Teacher हूँ। आपकी क्या मदद कर सकता हूँ?",
            isAi = true,
            subjectTag = _selectedSubject.value
        )
        _chatMessages.value = listOf(resetGreeting)
        viewModelScope.launch(Dispatchers.IO) {
            chatSessionRepository.deleteSession(activeSessionId)
            val newSession = ChatSessionEntity(
                sessionId = activeSessionId,
                title = _currentSessionTitle.value,
                subject = _selectedSubject.value,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                messageCount = 1,
                previewSnippet = resetGreeting.text.take(60)
            )
            chatSessionRepository.insertSession(newSession)
            chatSessionRepository.saveMessage(
                AiChatMessageEntity(
                    messageId = resetGreeting.id,
                    sessionId = activeSessionId,
                    sender = resetGreeting.sender,
                    role = resetGreeting.role,
                    text = resetGreeting.text,
                    isAi = resetGreeting.isAi,
                    timestampFormatted = resetGreeting.timestamp,
                    subjectTag = resetGreeting.subjectTag
                )
            )
        }
    }

    // --- Voice Handling ---
    fun toggleVoiceInput() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening(
                onResult = { recognizedText ->
                    sendUserMessage(recognizedText)
                },
                onError = { error ->
                    _statusMessage.value = error
                }
            )
        }
    }

    // --- Video & Interaction Actions ---
    fun selectVideo(video: VideoEntity?) {
        _selectedVideo.value = video
        if (video != null) {
            viewModelScope.launch(Dispatchers.IO) {
                db.videoDao().incrementViews(video.videoId)
            }
        }
    }

    fun toggleVideoLike(videoId: String, isCurrentlyLiked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isCurrentlyLiked) {
                db.videoDao().unlikeVideo(videoId)
            } else {
                db.videoDao().likeVideo(videoId)
            }
            // Update selected video if open
            _selectedVideo.value?.let { sel ->
                if (sel.videoId == videoId) {
                    _selectedVideo.value = sel.copy(
                        isLiked = !isCurrentlyLiked,
                        likes = if (isCurrentlyLiked) sel.likes - 1 else sel.likes + 1
                    )
                }
            }
        }
    }

    // --- Live Stream Actions ---
    fun selectLiveStream(stream: LiveStreamEntity?) {
        _selectedLiveStream.value = stream
        if (stream != null) {
            viewModelScope.launch(Dispatchers.IO) {
                db.chatDao().getChatsForStream(stream.streamId).collect { chats ->
                    _liveStreamChats.value = chats
                }
            }
        }
    }

    fun sendLiveComment(streamId: String, commentText: String) {
        val text = commentText.trim()
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val userChat = ChatMessageEntity(
                messageId = UUID.randomUUID().toString(),
                streamId = streamId,
                senderId = currentStudentId,
                senderName = "Amit Kumar (Student)",
                senderRole = "student",
                messageText = text,
                timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
                isAiResponse = false
            )
            db.chatDao().insertChat(userChat)

            // Simulated active AI / Teacher Assistant live reply for student engagement
            val lower = text.lowercase()
            if (lower.contains("sir") || lower.contains("question") || lower.contains("explain") || lower.contains("formula") || lower.contains("help")) {
                kotlinx.coroutines.delay(1200)
                val aiLiveReply = ChatMessageEntity(
                    messageId = UUID.randomUUID().toString(),
                    streamId = streamId,
                    senderId = "ai_spa",
                    senderName = "SPA AI TEACHER",
                    senderRole = "ai",
                    messageText = "💡 SP Sir will answer this shortly! Quick note: Keep key formulas ready in your notebook.",
                    timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
                    isAiResponse = true
                )
                db.chatDao().insertChat(aiLiveReply)
            }
        }
    }

    // --- Subscriptions ---
    fun toggleSubscription(teacherId: String, teacherName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isSubscribed = studentSubscriptions.value.any { it.teacherId == teacherId }
            if (isSubscribed) {
                db.subscriptionDao().deleteSubscription(currentStudentId, teacherId)
                val teacher = teachers.value.find { it.userId == teacherId }
                if (teacher != null) {
                    db.userDao().updateSubscriberCount(teacherId, (teacher.subscriberCount - 1).coerceAtLeast(0))
                }
                _statusMessage.value = "Unsubscribed from $teacherName"
            } else {
                val sub = SubscriptionEntity(
                    subscriptionId = "sub_${UUID.randomUUID()}",
                    studentId = currentStudentId,
                    teacherId = teacherId,
                    subscribedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
                )
                db.subscriptionDao().insertSubscription(sub)
                val teacher = teachers.value.find { it.userId == teacherId }
                if (teacher != null) {
                    db.userDao().updateSubscriberCount(teacherId, teacher.subscriberCount + 1)
                }
                _statusMessage.value = "Subscribed to $teacherName!"
            }
        }
    }

    // --- ROLE-BASED ACCESS CONTROL (RBAC) & ADMIN METHODS ---
    fun requestAdminAccess(targetTab: AppTab = AppTab.CREATE) {
        if (_isAdminAuthenticated.value && _currentUserRole.value == UserRole.ADMIN) {
            _currentTab.value = targetTab
        } else {
            pendingAdminTab = targetTab
            _adminPasscodeInput.value = ""
            _adminPasscodeError.value = null
            _showAdminPasscodeDialog.value = true
        }
    }

    fun setAdminPasscodeInput(input: String) {
        _adminPasscodeInput.value = input
        _adminPasscodeError.value = null
    }

    fun setOwnerPinInput(pin: String) {
        setAdminPasscodeInput(pin)
    }

    fun verifyAdminPasscode() {
        val input = _adminPasscodeInput.value.trim()
        if (input.isEmpty()) {
            _adminPasscodeError.value = "Please enter Admin Passcode"
            return
        }
        if (SecurityHelper.verifyAdminPasscode(input)) {
            _currentUserRole.value = UserRole.ADMIN
            _isAdminAuthenticated.value = true
            _adminPasscodeError.value = null
            _showAdminPasscodeDialog.value = false
            _adminPasscodeInput.value = ""
            val destination = pendingAdminTab ?: AppTab.CREATE
            pendingAdminTab = null
            _currentTab.value = destination
            _statusMessage.value = "🛡️ Admin access granted. Welcome SP Sir!"
        } else {
            _adminPasscodeError.value = "Incorrect passcode! Access restricted to authorized Administrator."
        }
    }

    fun verifyOwnerPin() {
        verifyAdminPasscode()
    }

    fun dismissAdminPasscodeDialog() {
        _showAdminPasscodeDialog.value = false
        _adminPasscodeInput.value = ""
        _adminPasscodeError.value = null
        pendingAdminTab = null
    }

    fun lockAdminSession() {
        _currentUserRole.value = UserRole.STUDENT
        _isAdminAuthenticated.value = false
        _adminPasscodeInput.value = ""
        _adminPasscodeError.value = null
        if (_currentTab.value == AppTab.CREATE) {
            _currentTab.value = AppTab.HOME
        }
        _statusMessage.value = "🔒 Admin panel locked. Switched to Student mode."
    }

    fun logoutOwner() {
        lockAdminSession()
    }

    fun switchToStudentRole() {
        lockAdminSession()
    }

    // --- Content Management (Admin Only Operations) ---
    fun deleteVideo(videoId: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can delete videos."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.videoDao().deleteVideo(videoId)
            _statusMessage.value = "Video lecture removed."
        }
    }

    fun deleteLiveStream(streamId: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can delete live streams."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.liveStreamDao().deleteStream(streamId)
            _statusMessage.value = "Live stream removed."
        }
    }

    fun endLiveStream(streamId: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can end streams."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.liveStreamDao().endStream(streamId)
            _statusMessage.value = "Live stream ended."
        }
    }

    fun addNote(
        title: String,
        subject: String,
        category: String,
        chapter: String,
        contentText: String,
        isFree: Boolean = true
    ) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can add study notes."
            return
        }
        if (title.isBlank() || contentText.isBlank()) {
            _statusMessage.value = "Note Title and Content cannot be empty."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newNote = com.example.data.local.NoteEntity(
                noteId = "note_${UUID.randomUUID()}",
                title = title.trim(),
                subject = subject.trim(),
                category = category.trim().ifBlank { "Full Chapter" },
                chapter = chapter.trim().ifBlank { "Chapter 1" },
                author = "SP Sir (Mithila Academy)",
                downloadSize = "1.8 MB",
                isFree = isFree,
                contentText = contentText.trim(),
                isDownloaded = false
            )
            db.noteDao().insertNote(newNote)
            _statusMessage.value = "✅ New Study Note published!"
        }
    }

    fun deleteNote(noteId: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can delete notes."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.noteDao().deleteNote(noteId)
            _statusMessage.value = "Study note removed."
        }
    }

    fun addNotice(
        title: String,
        content: String,
        category: String = "General",
        isImportant: Boolean = false
    ) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can publish notices."
            return
        }
        if (title.isBlank() || content.isBlank()) {
            _statusMessage.value = "Notice Title and Content are required."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val newNotice = com.example.data.local.NoticeEntity(
                noticeId = "not_${UUID.randomUUID()}",
                title = title.trim(),
                category = category.trim(),
                dateText = dateStr,
                content = content.trim(),
                author = "Mithila Academy Admin",
                isImportant = isImportant
            )
            db.noticeDao().insertNotice(newNotice)
            _statusMessage.value = "📢 New Notice published!"
        }
    }

    fun deleteNotice(noticeId: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can delete notices."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.noticeDao().deleteNotice(noticeId)
            _statusMessage.value = "Notice removed."
        }
    }

    fun addCourse(
        title: String,
        category: String,
        price: String,
        isFree: Boolean,
        description: String,
        duration: String = "40 Hours"
    ) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can create courses."
            return
        }
        if (title.isBlank()) {
            _statusMessage.value = "Course title is required."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newCourse = com.example.data.local.CourseEntity(
                courseId = "crs_${UUID.randomUUID()}",
                title = title.trim(),
                category = category.trim(),
                teacherName = _teacherProfileName.value.ifBlank { "SP Sir (Mithila Academy)" },
                description = description.trim().ifBlank { "Comprehensive course package by Mithila Academy." },
                duration = duration,
                lessonsCount = 12,
                price = price,
                isFree = isFree,
                thumbnailUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400",
                isEnrolled = false,
                progressPercent = 0,
                completedLessons = 0,
                rating = 4.9f,
                totalEnrolled = 100
            )
            db.courseDao().insertCourse(newCourse)
            _statusMessage.value = "🎉 New Course added!"
        }
    }

    fun deleteCourse(courseId: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            _statusMessage.value = "Permission Denied: Only Admin can delete courses."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.courseDao().deleteCourse(courseId)
            _statusMessage.value = "Course removed."
        }
    }

    fun addKnowledgeItem(
        title: String,
        subject: String,
        content: String,
        keywords: String
    ) {
        if (title.isBlank() || content.isBlank()) {
            _statusMessage.value = "Title and Content are required."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val item = KnowledgeEntity(
                id = "kn_${UUID.randomUUID()}",
                title = title.trim(),
                subject = subject.trim(),
                content = content.trim(),
                keywords = if (keywords.isNotBlank()) keywords.trim() else "${title.trim()}, ${subject.trim()}",
                addedBy = "Owner / SP (Mithila Academy)",
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
            )
            db.knowledgeDao().insertKnowledge(item)
            _statusMessage.value = "✅ New Knowledge saved to SPA AI Teacher!"
        }
    }

    fun deleteKnowledgeItem(item: KnowledgeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.knowledgeDao().deleteKnowledge(item)
            _statusMessage.value = "Knowledge item removed."
        }
    }

    fun addNewVideo(
        title: String,
        description: String,
        subject: String,
        durationMinutes: Int
    ) {
        if (title.isBlank()) {
            _statusMessage.value = "Video title is required."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newVideo = VideoEntity(
                videoId = "vid_${UUID.randomUUID()}",
                teacherId = "teacher_sp_01",
                teacherName = _teacherProfileName.value.ifBlank { "SP Sir (Mithila Academy)" },
                teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                title = title.trim(),
                description = description.trim().ifEmpty { "Educational video lecture by Mithila Academy." },
                videoUrl = "https://storage.googleapis.com/bucket_name/videos/custom_lecture.mp4",
                thumbnailUrl = "",
                views = 1,
                likes = 0,
                isLiked = false,
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
                subject = subject.trim(),
                durationMinutes = durationMinutes.coerceAtLeast(5),
                resolution = "1080p FHD"
            )
            db.videoDao().insertVideo(newVideo)
            _statusMessage.value = "✅ New Video Lecture published!"
        }
    }

    fun startNewLiveStream(title: String, subject: String) {
        if (title.isBlank()) {
            _statusMessage.value = "Stream title is required."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newStream = LiveStreamEntity(
                streamId = "live_${UUID.randomUUID()}",
                teacherId = "teacher_sp_01",
                teacherName = "${_teacherProfileName.value.ifBlank { "SP Sir" }} (Live)",
                teacherProfilePic = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200",
                title = title.trim(),
                streamKey = "live_pk_mithila_${System.currentTimeMillis() % 10000}",
                status = "active",
                startedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
                viewerCount = 1,
                subject = subject.trim()
            )
            db.liveStreamDao().insertStream(newStream)
            _statusMessage.value = "🔴 Live Stream Broadcast started!"
        }
    }

    fun toggleVideoDownload(videoId: String, videoTitle: String) {
        val current = _downloadedVideoIds.value
        if (current.contains(videoId)) {
            _downloadedVideoIds.value = current - videoId
            _statusMessage.value = "Removed from offline downloads: $videoTitle"
        } else {
            _downloadedVideoIds.value = current + videoId
            _statusMessage.value = "✅ Downloaded for offline viewing: $videoTitle"
        }
    }

    fun updateTeacherProfile(name: String, specialization: String, bio: String, qualification: String) {
        _teacherProfileName.value = name.trim()
        _teacherSpecialization.value = specialization.trim()
        _teacherBio.value = bio.trim()
        _teacherQualification.value = qualification.trim()
        _statusMessage.value = "✅ Teacher profile updated successfully!"
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
