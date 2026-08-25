package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.AiTeacherViewModel
import com.example.ui.AppTab
import com.example.ui.components.AdminPasscodeDialog
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AiTeacherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: AiTeacherViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val currentTab by viewModel.currentTab.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()

    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val ttsEnabled by viewModel.voiceManager.ttsEnabled.collectAsState()
    val audioAmplitude by viewModel.voiceManager.audioAmplitude.collectAsState()
    val waveformBands by viewModel.voiceManager.waveformBands.collectAsState()
    val speechSpeed by viewModel.voiceManager.speechSpeed.collectAsState()
    val speechPitch by viewModel.voiceManager.speechPitch.collectAsState()

    val videos by viewModel.videos.collectAsState()
    val liveStreams by viewModel.liveStreams.collectAsState()
    val subscriptions by viewModel.studentSubscriptions.collectAsState()
    val allKnowledge by viewModel.allKnowledge.collectAsState()
    val downloadedVideoIds by viewModel.downloadedVideoIds.collectAsState()

    val teacherProfileName by viewModel.teacherProfileName.collectAsState()
    val teacherSpecialization by viewModel.teacherSpecialization.collectAsState()
    val teacherBio by viewModel.teacherBio.collectAsState()
    val teacherQualification by viewModel.teacherQualification.collectAsState()

    val customTeacherImageUri by viewModel.customTeacherImageUri.collectAsState()
    val customLogoImageUri by viewModel.customLogoImageUri.collectAsState()
    val customBgImageUri by viewModel.customBgImageUri.collectAsState()

    val mainTitle by viewModel.mainTitle.collectAsState()
    val subTitle by viewModel.subTitle.collectAsState()
    val customMessage by viewModel.customMessage.collectAsState()
    val aiTeacherTitle by viewModel.aiTeacherTitle.collectAsState()

    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val currentSessionTitle by viewModel.currentSessionTitle.collectAsState()

    val isTouchEditMode by viewModel.isEditMode.collectAsState()
    val selectedTouchElementId by viewModel.selectedElementId.collectAsState()
    val touchElementTransforms by viewModel.elementTransforms.collectAsState()

    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val selectedLiveStream by viewModel.selectedLiveStream.collectAsState()
    val liveStreamChats by viewModel.liveStreamChats.collectAsState()

    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val showAdminPasscodeDialog by viewModel.showAdminPasscodeDialog.collectAsState()
    val adminPasscodeInput by viewModel.adminPasscodeInput.collectAsState()
    val adminPasscodeError by viewModel.adminPasscodeError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice chat.", Toast.LENGTH_SHORT).show()
        }
    }

    val onVoiceClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.toggleVoiceInput()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Handle Hardware Back Button
    BackHandler(enabled = currentTab != AppTab.HOME) {
        viewModel.setTab(AppTab.HOME)
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentTab = currentTab,
                currentLanguage = selectedLanguage,
                onTabSelected = { tab ->
                    if (tab == AppTab.CREATE) {
                        viewModel.requestAdminAccess(AppTab.CREATE)
                    } else {
                        viewModel.setTab(tab)
                    }
                },
                onOpenLanguagePicker = { viewModel.showLanguagePicker(true) },
                onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // TopBar for standard screens (not Home, AI Voice Teacher, or Live Class which have specialized headers)
                if (currentTab != AppTab.HOME &&
                    currentTab != AppTab.AI_CHAT &&
                    currentTab != AppTab.LIVE_CLASS &&
                    currentTab != AppTab.ALL_COURSES &&
                    currentTab != AppTab.MY_COURSES &&
                    currentTab != AppTab.NOTES &&
                    currentTab != AppTab.FREE_NOTES &&
                    currentTab != AppTab.TEST &&
                    currentTab != AppTab.FREE_TEST &&
                    currentTab != AppTab.SOCIAL &&
                    currentTab != AppTab.DOWNLOADS &&
                    currentTab != AppTab.NOTICE_BOARD &&
                    currentTab != AppTab.SETTINGS
                ) {
                    AppTopBar(
                        currentTab = currentTab,
                        ttsEnabled = ttsEnabled,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        isOwnerAuthenticated = isAdminAuthenticated,
                        onToggleTts = { viewModel.voiceManager.toggleTts() },
                        onNavigateToOwner = { viewModel.requestAdminAccess(AppTab.CREATE) }
                    )
                }
            },
            bottomBar = {
                // Persistent 4-Tab Bottom Navigation Bar matching design
                AppBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == AppTab.CREATE) {
                            viewModel.requestAdminAccess(AppTab.CREATE)
                        } else {
                            viewModel.setTab(tab)
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        if (currentTab == AppTab.HOME || currentTab == AppTab.AI_CHAT || currentTab == AppTab.LIVE_CLASS)
                            PaddingValues(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        else innerPadding
                    )
            ) {
                when (currentTab) {
                    AppTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }

                    AppTab.ALL_COURSES -> {
                        AllCoursesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.MY_COURSES -> {
                        MyCoursesScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.NOTES -> {
                        NotesScreen(
                            viewModel = viewModel,
                            isFreeOnly = false,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.FREE_NOTES -> {
                        NotesScreen(
                            viewModel = viewModel,
                            isFreeOnly = true,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.TEST -> {
                        TestSystemScreen(
                            viewModel = viewModel,
                            isFreeOnly = false,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.FREE_TEST -> {
                        TestSystemScreen(
                            viewModel = viewModel,
                            isFreeOnly = true,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.SOCIAL -> {
                        SocialScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.DOWNLOADS -> {
                        DownloadsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.NOTICE_BOARD -> {
                        NoticeBoardScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.FREE_VIDEOS -> {
                        VideosScreen(
                            videos = videos.filter { it.views >= 0 },
                            selectedSubject = selectedSubject,
                            downloadedVideoIds = downloadedVideoIds,
                            subscriptions = subscriptions,
                            onSubjectSelected = { subj -> viewModel.setSubject(subj) },
                            onVideoSelected = { video -> viewModel.selectVideo(video) },
                            onToggleLike = { id, liked -> viewModel.toggleVideoLike(id, liked) },
                            onToggleDownload = { id, title -> viewModel.toggleVideoDownload(id, title) },
                            onToggleSubscription = { teacherId, teacherName ->
                                viewModel.toggleSubscription(teacherId, teacherName)
                            }
                        )
                    }

                    AppTab.AI_CHAT -> {
                        AiTeacherChatScreen(
                            messages = chatMessages,
                            isAiThinking = isAiThinking,
                            isSpeaking = isSpeaking,
                            isListening = isListening,
                            audioAmplitude = audioAmplitude,
                            waveformBands = waveformBands,
                            selectedSubject = selectedSubject,
                            speechSpeed = speechSpeed,
                            speechPitch = speechPitch,
                            mainTitle = mainTitle,
                            subTitle = subTitle,
                            customMessage = customMessage,
                            aiTeacherTitle = aiTeacherTitle,
                            customTeacherImageUri = customTeacherImageUri,
                            customLogoImageUri = customLogoImageUri,
                            customBgImageUri = customBgImageUri,
                            chatSessions = chatSessions,
                            currentSessionId = currentSessionId,
                            currentSessionTitle = currentSessionTitle,
                            isEditMode = isTouchEditMode,
                            selectedElementId = selectedTouchElementId,
                            elementTransforms = touchElementTransforms,
                            onToggleEditMode = { viewModel.toggleTouchEditMode() },
                            onSelectElement = { id -> viewModel.selectTouchElement(id) },
                            onUpdateTransform = { id, x, y, scale, rot -> viewModel.updateTouchElementTransform(id, x, y, scale, rot) },
                            onUpdateImage = { id, uri -> viewModel.updateTouchElementImage(id, uri) },
                            onUpdateTeacherImage = { uri -> viewModel.updateCustomTeacherImage(uri) },
                            onUpdateLogoImage = { uri -> viewModel.updateCustomLogoImage(uri) },
                            onResetBranding = { viewModel.resetToDefaultBranding() },
                            onResetElement = { id -> viewModel.resetTouchElement(id) },
                            onToggleVisibility = { id -> viewModel.toggleTouchElementVisibility(id) },
                            onSaveLayout = { viewModel.saveTouchLayout() },
                            onResetAllLayouts = { viewModel.resetAllTouchLayouts() },
                            onAddSticker = { uri -> viewModel.addCustomSticker(uri) },
                            onSelectSession = { session -> viewModel.loadChatSession(session) },
                            onCreateNewSession = { subject -> viewModel.createNewChatSession(subject) },
                            onDeleteSession = { sessionId -> viewModel.deleteChatSession(sessionId) },
                            onRenameSession = { sessionId, newTitle -> viewModel.renameChatSession(sessionId, newTitle) },
                            onUpdateSpeechSpeed = { speed -> viewModel.voiceManager.setSpeechSpeed(speed) },
                            onUpdateSpeechPitch = { pitch -> viewModel.voiceManager.setSpeechPitch(pitch) },
                            onResetVoiceSettings = { viewModel.voiceManager.resetVoiceSettings() },
                            onUpdateCustomText = { main, sub, msg ->
                                viewModel.updateCustomConfiguration(main = main, sub = sub, message = msg)
                            },
                            onSubjectSelected = { subj -> viewModel.setSubject(subj) },
                            onSendMessage = { text -> viewModel.sendUserMessage(text) },
                            onToggleVoice = onVoiceClick,
                            onSpeakMessage = { text -> viewModel.voiceManager.speak(text, force = true) },
                            onClearChat = { viewModel.clearChat() },
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }

                    AppTab.LIVE_CLASS -> {
                        LiveClassScreen(
                            liveStreams = liveStreams,
                            selectedStream = selectedLiveStream,
                            chats = liveStreamChats,
                            subscriptions = subscriptions,
                            onSelectStream = { stream -> viewModel.selectLiveStream(stream) },
                            onSendComment = { streamId, comment -> viewModel.sendLiveComment(streamId, comment) },
                            onToggleSubscription = { teacherId, teacherName ->
                                viewModel.toggleSubscription(teacherId, teacherName)
                            }
                        )
                    }

                    AppTab.VIDEOS -> {
                        VideosScreen(
                            videos = videos,
                            selectedSubject = selectedSubject,
                            downloadedVideoIds = downloadedVideoIds,
                            subscriptions = subscriptions,
                            onSubjectSelected = { subj -> viewModel.setSubject(subj) },
                            onVideoSelected = { video -> viewModel.selectVideo(video) },
                            onToggleLike = { id, liked -> viewModel.toggleVideoLike(id, liked) },
                            onToggleDownload = { id, title -> viewModel.toggleVideoDownload(id, title) },
                            onToggleSubscription = { teacherId, teacherName ->
                                viewModel.toggleSubscription(teacherId, teacherName)
                            }
                        )
                    }

                    AppTab.CREATE -> {
                        CreateScreen(viewModel = viewModel)
                    }

                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.setTab(AppTab.HOME) }
                        )
                    }
                }

                // Video Detail Modal (if video tapped in feed)
                selectedVideo?.let { video ->
                    VideoDetailModal(
                        video = video,
                        onDismiss = { viewModel.selectVideo(null) },
                        onToggleLike = { viewModel.toggleVideoLike(video.videoId, video.isLiked) },
                        onAskDoubtInAi = { prompt ->
                            viewModel.setTab(AppTab.AI_CHAT)
                            viewModel.sendUserMessage(prompt)
                        }
                    )
                }

                // Live Stream Detail Modal
                selectedLiveStream?.let { stream ->
                    if (currentTab != AppTab.LIVE_CLASS) {
                        LiveStreamDetailModal(
                            stream = stream,
                            chats = liveStreamChats,
                            onDismiss = { viewModel.selectLiveStream(null) },
                            onSendComment = { text -> viewModel.sendLiveComment(stream.streamId, text) }
                        )
                    }
                }

                // Master Admin Passcode Security Modal (Cryptographic Verification)
                if (showAdminPasscodeDialog) {
                    AdminPasscodeDialog(
                        passcodeInput = adminPasscodeInput,
                        passcodeError = adminPasscodeError,
                        onPasscodeChange = { viewModel.setAdminPasscodeInput(it) },
                        onVerify = { viewModel.verifyAdminPasscode() },
                        onDismiss = { viewModel.dismissAdminPasscodeDialog() }
                    )
                }

                // Multilingual App Language Selection Modal
                if (showLanguageDialog) {
                    LanguageSelectionDialog(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { lang ->
                            viewModel.setAppLanguage(lang)
                        },
                        onDismiss = { viewModel.showLanguagePicker(false) }
                    )
                }
            }
        }
    }
}

