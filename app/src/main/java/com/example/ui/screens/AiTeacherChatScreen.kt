package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import coil.compose.AsyncImage
import androidx.compose.ui.geometry.Offset
import com.example.ui.components.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import com.example.data.local.ChatSessionEntity
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.UiMessage
import com.example.ui.components.AITeacherFullScreenLayer
import com.example.ui.theme.*

@Composable
fun AiTeacherChatScreen(
    messages: List<UiMessage>,
    isAiThinking: Boolean,
    isSpeaking: Boolean,
    isListening: Boolean,
    audioAmplitude: Float,
    waveformBands: List<Float>,
    selectedSubject: String,
    speechSpeed: Float = 0.95f,
    speechPitch: Float = 1.0f,
    mainTitle: String = "MITHILA ACADEMY",
    subTitle: String = "AI DOUBTS • AI TEACHER",
    customMessage: String = "...",
    aiTeacherTitle: String = "SPA AI Teacher",
    customTeacherImageUri: String? = null,
    customLogoImageUri: String? = null,
    customBgImageUri: String? = null,
    chatSessions: List<ChatSessionEntity> = emptyList(),
    currentSessionId: String = "",
    currentSessionTitle: String = "",
    isEditMode: Boolean = false,
    selectedElementId: String? = null,
    elementTransforms: Map<String, TouchElementTransform> = emptyMap(),
    onToggleEditMode: () -> Unit = {},
    onSelectElement: (String?) -> Unit = {},
    onUpdateTransform: (String, Float, Float, Float, Float) -> Unit = { _, _, _, _, _ -> },
    onUpdateImage: (String, String) -> Unit = { _, _ -> },
    onUpdateTeacherImage: (String?) -> Unit = {},
    onUpdateLogoImage: (String?) -> Unit = {},
    onResetBranding: () -> Unit = {},
    onResetElement: (String) -> Unit = {},
    onToggleVisibility: (String) -> Unit = {},
    onSaveLayout: () -> Unit = {},
    onResetAllLayouts: () -> Unit = {},
    onAddSticker: (String) -> Unit = {},
    onSelectSession: (ChatSessionEntity) -> Unit = {},
    onCreateNewSession: (String) -> Unit = {},
    onDeleteSession: (String) -> Unit = {},
    onRenameSession: (String, String) -> Unit = { _, _ -> },
    onUpdateSpeechSpeed: (Float) -> Unit = {},
    onUpdateSpeechPitch: (Float) -> Unit = {},
    onResetVoiceSettings: () -> Unit = {},
    onUpdateCustomText: (String, String, String) -> Unit = { _, _, _ -> },
    onSubjectSelected: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onToggleVoice: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var isChatOverlayVisible by remember { mutableStateOf(false) }
    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var showSessionsModal by remember { mutableStateOf(false) }
    var showCustomTextEditDialog by remember { mutableStateOf(false) }
    var editMainTitle by remember(mainTitle) { mutableStateOf(mainTitle) }
    var editSubTitle by remember(subTitle) { mutableStateOf(subTitle) }
    var editCustomMessage by remember(customMessage) { mutableStateOf(customMessage) }
    var sessionToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var activeImagePickerTarget by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Helper to get active transform for element
    val defaults = remember { getDefaultElementTransforms() }
    fun getElementTransform(id: String): TouchElementTransform {
        return elementTransforms[id] ?: defaults[id] ?: TouchElementTransform(elementId = id, displayName = id)
    }

    // Photo and Sticker Pickers for Touch Editing Mode
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUpdateImage(activeImagePickerTarget ?: TouchElementIds.BG_PHOTO, it.toString()) }
    }

    val stickerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddSticker(it.toString()) }
    }

    val subjects = listOf(
        "All", "Physics", "Chemistry", "Biology", "Mathematics", "GK", "Railway", "SSC", "BPSC", "Bihar Police"
    )

    val quickActions = listOf(
        QuickActionItem("Explain Physics", "Physics: दर्पण सूत्र और परावर्तन के मुख्य नियम विस्तार से समझाएं"),
        QuickActionItem("Math Formulas", "Math: त्रिकोणमिति और बीजगणित के महत्वपूर्ण सूत्र बताइए"),
        QuickActionItem("Bihar GK", "Bihar GK: बिहार का प्राचीन इतिहास और प्रमुख तथ्य"),
        QuickActionItem("Give me a lesson", "आज के लिए एक महत्वपूर्ण कॉन्सेप्ट और परीक्षा में आने वाले प्रश्न समझाएं"),
        QuickActionItem("Open YouTube", "", isExternal = true, url = "https://youtube.com/@mithilaacademy"),
        QuickActionItem("Open WhatsApp", "", isExternal = true, url = "https://wa.me/919931398862")
    )

    val lastAiSpeech = remember(messages) {
        messages.findLast { it.isAi }?.text
    }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size, isAiThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Pulse animation for mic button when active
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micAuraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_aura_scale"
    )

    val micAuraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_aura_alpha"
    )

    // ROOT 100% FULL-VIEWPORT LAYOUT (No black space, no margins, no padding)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("ai_teacher_main_screen")
    ) {
        // === 1. LAYER: 100% FULL-SCREEN COVER PHOTO WITH SMART FACE/LOGO POSITIONING ===
        val bgTransform = getElementTransform(TouchElementIds.BG_PHOTO)
        val effectiveBgImage = bgTransform.customImageUri ?: customTeacherImageUri ?: customBgImageUri
        AITeacherFullScreenLayer(
            isSpeaking = isSpeaking,
            isListening = isListening,
            audioAmplitude = audioAmplitude,
            waveformBands = waveformBands,
            customImageUri = effectiveBgImage,
            modifier = Modifier.fillMaxSize()
        )

        // === 2. FLOATING CUSTOM STICKER / LOGO (IF ADDED BY USER) ===
        val stickerTransform = getElementTransform(TouchElementIds.CUSTOM_STICKER)
        if (stickerTransform.isVisible && !stickerTransform.customImageUri.isNullOrBlank()) {
            TouchEditableWrapper(
                elementId = TouchElementIds.CUSTOM_STICKER,
                transform = stickerTransform,
                isEditMode = isEditMode,
                isSelected = selectedElementId == TouchElementIds.CUSTOM_STICKER,
                onSelect = { onSelectElement(TouchElementIds.CUSTOM_STICKER) },
                onTransformChange = { x, y, scale, rot ->
                    onUpdateTransform(TouchElementIds.CUSTOM_STICKER, x, y, scale, rot)
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                AsyncImage(
                    model = Uri.parse(stickerTransform.customImageUri),
                    contentDescription = "Custom Sticker",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        // === 3. LAYER: PREMIUM DARK GLASS UI ON TOP ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP REGION: TOUCH EDITOR TOOLBAR + PREMIUM DARK GLASS HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                // Top Touch Editor Mode Bar
                TouchEditorTopToolbar(
                    isEditMode = isEditMode,
                    onToggleEditMode = onToggleEditMode,
                    onSaveLayout = onSaveLayout,
                    onResetAll = onResetAllLayouts,
                    onAddSticker = { stickerPickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Header Glass Card (Wrapped for Touch Edit)
                val headerTransform = getElementTransform(TouchElementIds.HEADER_BAR)
                TouchEditableWrapper(
                    elementId = TouchElementIds.HEADER_BAR,
                    transform = headerTransform,
                    isEditMode = isEditMode,
                    isSelected = selectedElementId == TouchElementIds.HEADER_BAR,
                    onSelect = { onSelectElement(TouchElementIds.HEADER_BAR) },
                    onTransformChange = { x, y, scale, rot ->
                        onUpdateTransform(TouchElementIds.HEADER_BAR, x, y, scale, rot)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xD90A0E1A),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Back button & Title Branding
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Back to Home Button
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_back_home")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Home",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                val logoTransform = getElementTransform(TouchElementIds.LOGO_BADGE)
                                val effectiveLogoUri = customLogoImageUri ?: logoTransform.customImageUri
                                TouchEditableWrapper(
                                    elementId = TouchElementIds.LOGO_BADGE,
                                    transform = logoTransform,
                                    isEditMode = isEditMode,
                                    isSelected = selectedElementId == TouchElementIds.LOGO_BADGE,
                                    onSelect = { onSelectElement(TouchElementIds.LOGO_BADGE) },
                                    onTransformChange = { x, y, scale, rot ->
                                        onUpdateTransform(TouchElementIds.LOGO_BADGE, x, y, scale, rot)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(BluePrimary, PurpleAi))
                                            )
                                            .border(1.dp, BlueSecondary.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!effectiveLogoUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = effectiveLogoUri,
                                                contentDescription = "Academy Logo",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_app_icon),
                                                contentDescription = "Academy Logo",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = mainTitle.ifBlank { "MITHILA ACADEMY" },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "AI DOUBTS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AmberAccent,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Text(
                                            text = "•",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = "AI TEACHER",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = BlueSecondary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Right controls: Customize text, Chat View toggle, Voice Settings & Sessions
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Customize Text Banner Button
                                IconButton(
                                    onClick = { showCustomTextEditDialog = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("btn_customize_text")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Customize Text",
                                        tint = AmberAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Voice Settings Button
                                IconButton(
                                    onClick = { showVoiceSettingsDialog = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("voice_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Voice Settings",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                // Chat Stream Toggle Button
                                FilledTonalButton(
                                    onClick = { isChatOverlayVisible = !isChatOverlayVisible },
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isChatOverlayVisible) BluePrimary else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isChatOverlayVisible) Icons.Default.VisibilityOff else Icons.Default.Chat,
                                        contentDescription = "Toggle Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isChatOverlayVisible) "Pure" else "Chat",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                // Saved Sessions History Button
                                IconButton(
                                    onClick = { showSessionsModal = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("chat_sessions_history_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (chatSessions.isNotEmpty()) {
                                                Badge(
                                                    containerColor = BluePrimary,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = chatSessions.size.toString(),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Saved Chat Sessions",
                                            tint = AmberAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (messages.isNotEmpty()) {
                                    IconButton(
                                        onClick = onClearChat,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CleaningServices,
                                            contentDescription = "Clear Chat",
                                            tint = Color.White.copy(alpha = 0.75f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Translucent Subject Chips Row (Wrapped for Touch Edit)
                val chipsTransform = getElementTransform(TouchElementIds.SUBJECT_CHIPS)
                TouchEditableWrapper(
                    elementId = TouchElementIds.SUBJECT_CHIPS,
                    transform = chipsTransform,
                    isEditMode = isEditMode,
                    isSelected = selectedElementId == TouchElementIds.SUBJECT_CHIPS,
                    onSelect = { onSelectElement(TouchElementIds.SUBJECT_CHIPS) },
                    onTransformChange = { x, y, scale, rot ->
                        onUpdateTransform(TouchElementIds.SUBJECT_CHIPS, x, y, scale, rot)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(subjects) { subj ->
                            val isSelected = selectedSubject == subj
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) BluePrimary else Color(0xB30A0E1A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BlueSecondary else Color.White.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier
                                    .clickable { onSubjectSelected(subj) }
                                    .testTag("chip_${subj.lowercase()}")
                            ) {
                                Text(
                                    text = subj,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Active Session Indicator & Quick Switch Banner (Wrapped for Touch Edit)
                val sessionBannerTransform = getElementTransform(TouchElementIds.ACTIVE_SESSION_BANNER)
                TouchEditableWrapper(
                    elementId = TouchElementIds.ACTIVE_SESSION_BANNER,
                    transform = sessionBannerTransform,
                    isEditMode = isEditMode,
                    isSelected = selectedElementId == TouchElementIds.ACTIVE_SESSION_BANNER,
                    onSelect = { onSelectElement(TouchElementIds.ACTIVE_SESSION_BANNER) },
                    onTransformChange = { x, y, scale, rot ->
                        onUpdateTransform(TouchElementIds.ACTIVE_SESSION_BANNER, x, y, scale, rot)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xD90E1726),
                        border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .testTag("active_session_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clickable { showSessionsModal = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Active Session",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = currentSessionTitle.ifBlank { "Live Doubt Session" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // New Session Button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BluePrimary.copy(alpha = 0.3f),
                                    border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .clickable { onCreateNewSession(selectedSubject) }
                                        .testTag("new_session_chip_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "New Session",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "New",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                // Sessions History Sheet Trigger
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clickable { showSessionsModal = true }
                                        .testTag("open_sessions_modal_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Sessions",
                                            tint = AmberAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "${chatSessions.size} saved",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AmberAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- MID REGION: AI TEACHER STATUS BOX (POSITIONED AROUND CHEST / LOWER-MID AREA, NOT COVERING FACE) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // If chat is toggled ON, show the floating transcript stream
                if (isChatOverlayVisible) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(messages) { msg ->
                            FloatingGlassChatItem(
                                message = msg,
                                teacherAvatarUri = customTeacherImageUri,
                                onSpeak = { onSpeakMessage(msg.text) },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(msg.text))
                                }
                            )
                        }

                        if (isAiThinking) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xD90A0E1A),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier.padding(end = 40.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = BluePrimary
                                        )
                                        Text(
                                            text = "SPA AI Teacher उत्तर तैयार कर रहा है...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // PURE VIEW MODE: Clean, compact semi-transparent glass status box below face
                    Spacer(modifier = Modifier.weight(0.6f))

                    // Customizable Academy Branding & Quote Banner (Wrapped for Touch Edit)
                    val customTextTransform = getElementTransform(TouchElementIds.CUSTOM_TEXT)
                    TouchEditableWrapper(
                        elementId = TouchElementIds.CUSTOM_TEXT,
                        transform = customTextTransform,
                        isEditMode = isEditMode,
                        isSelected = selectedElementId == TouchElementIds.CUSTOM_TEXT,
                        onSelect = { onSelectElement(TouchElementIds.CUSTOM_TEXT) },
                        onTransformChange = { x, y, scale, rot ->
                            onUpdateTransform(TouchElementIds.CUSTOM_TEXT, x, y, scale, rot)
                        },
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xB3080F24),
                            border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.45f)),
                            shadowElevation = 10.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustomTextEditDialog = true }
                                .testTag("custom_text_banner")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = mainTitle.ifBlank { "MITHILA ACADEMY" },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                if (customMessage.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "\"$customMessage\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = AmberAccent.copy(alpha = 0.95f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        ),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subTitle.ifBlank { "AI DOUBTS • AI TEACHER" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compact Semi-Transparent Glass Status Badge (Wrapped for Touch Edit)
                    val statusTransform = getElementTransform(TouchElementIds.STATUS_CARD)
                    TouchEditableWrapper(
                        elementId = TouchElementIds.STATUS_CARD,
                        transform = statusTransform,
                        isEditMode = isEditMode,
                        isSelected = selectedElementId == TouchElementIds.STATUS_CARD,
                        onSelect = { onSelectElement(TouchElementIds.STATUS_CARD) },
                        onTransformChange = { x, y, scale, rot ->
                            onUpdateTransform(TouchElementIds.STATUS_CARD, x, y, scale, rot)
                        },
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xD9070D1E),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val statusColor = when {
                                        isAiThinking -> BluePrimary
                                        isSpeaking -> PurpleAi
                                        isListening -> RedLive
                                        else -> GreenSuccess
                                    }
                                    val statusLabel = when {
                                        isAiThinking -> "THINKING • GENERATING ANSWER"
                                        isSpeaking -> "SPEAKING • AI EXPLAINING"
                                        isListening -> "LISTENING • SPEAK NOW"
                                        else -> "IDLE • TAP TO ASK A DOUBT"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }

                                // Dynamic Live Audio Waveform Equalizer (Wrapped for Touch Edit)
                                val waveformTransform = getElementTransform(TouchElementIds.WAVEFORM)
                                TouchEditableWrapper(
                                    elementId = TouchElementIds.WAVEFORM,
                                    transform = waveformTransform,
                                    isEditMode = isEditMode,
                                    isSelected = selectedElementId == TouchElementIds.WAVEFORM,
                                    onSelect = { onSelectElement(TouchElementIds.WAVEFORM) },
                                    onTransformChange = { x, y, scale, rot ->
                                        onUpdateTransform(TouchElementIds.WAVEFORM, x, y, scale, rot)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp)
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        val barCount = waveformBands.size.coerceAtLeast(1)
                                        val totalWidth = size.width
                                        val barWidth = 3.5.dp.toPx()
                                        val availableSpace = totalWidth - (barWidth * barCount)
                                        val spacing = if (barCount > 1) availableSpace / (barCount - 1) else 0f
                                        val centerY = size.height / 2f

                                        waveformBands.forEachIndexed { index, bandValue ->
                                            val currentBarHeight = if (isSpeaking || isListening)
                                                (bandValue * size.height).coerceIn(3.dp.toPx(), size.height)
                                            else 3.dp.toPx()

                                            val x = index * (barWidth + spacing) + (barWidth / 2f)
                                            val startY = centerY - (currentBarHeight / 2f)
                                            val endY = centerY + (currentBarHeight / 2f)

                                            val topColor = if (isSpeaking) RedLive else if (isListening) AmberAccent else BlueSecondary.copy(alpha = 0.6f)
                                            val bottomColor = if (isSpeaking) PurpleAi else if (isListening) RedLive else BluePrimary.copy(alpha = 0.6f)

                                            drawLine(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(topColor, bottomColor),
                                                    startY = startY,
                                                    endY = endY
                                                ),
                                                start = Offset(x, startY),
                                                end = Offset(x, endY),
                                                strokeWidth = barWidth,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                                            )
                                        }
                                    }
                                }

                                // Subtitle Speech / Status Text (Wrapped for Touch Edit)
                                val subtitleTransform = getElementTransform(TouchElementIds.SUBTITLE_TEXT)
                                TouchEditableWrapper(
                                    elementId = TouchElementIds.SUBTITLE_TEXT,
                                    transform = subtitleTransform,
                                    isEditMode = isEditMode,
                                    isSelected = selectedElementId == TouchElementIds.SUBTITLE_TEXT,
                                    onSelect = { onSelectElement(TouchElementIds.SUBTITLE_TEXT) },
                                    onTransformChange = { x, y, scale, rot ->
                                        onUpdateTransform(TouchElementIds.SUBTITLE_TEXT, x, y, scale, rot)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (!lastAiSpeech.isNullOrBlank()) lastAiSpeech
                                        else "Real-time AI voice stream powered by Gemini & Mithila Academy Knowledge Base",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- BOTTOM REGION: QUICK ACTION BUTTONS, PREMIUM LIVE VOICE BUTTON, INPUT BAR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(bottom = 6.dp)
            ) {
                // 1. Quick Action Glass Buttons (Wrapped for Touch Edit)
                val quickActionsTransform = getElementTransform(TouchElementIds.QUICK_ACTIONS)
                TouchEditableWrapper(
                    elementId = TouchElementIds.QUICK_ACTIONS,
                    transform = quickActionsTransform,
                    isEditMode = isEditMode,
                    isSelected = selectedElementId == TouchElementIds.QUICK_ACTIONS,
                    onSelect = { onSelectElement(TouchElementIds.QUICK_ACTIONS) },
                    onTransformChange = { x, y, scale, rot ->
                        onUpdateTransform(TouchElementIds.QUICK_ACTIONS, x, y, scale, rot)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickActions) { action ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xD90F172A),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .clickable {
                                        if (action.isExternal && action.url != null) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Fallback
                                            }
                                        } else {
                                            onSendMessage(action.prompt)
                                        }
                                    }
                                    .testTag("quick_action_${action.label.lowercase().replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (action.label.contains("YouTube")) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleOutline,
                                            contentDescription = null,
                                            tint = RedLive,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else if (action.label.contains("WhatsApp")) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = GreenSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = AmberAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Premium Circular Live Voice Button with Outer Glowing Aura Ring (Wrapped for Touch Edit)
                val micTransform = getElementTransform(TouchElementIds.MIC_BUTTON)
                TouchEditableWrapper(
                    elementId = TouchElementIds.MIC_BUTTON,
                    transform = micTransform,
                    isEditMode = isEditMode,
                    isSelected = selectedElementId == TouchElementIds.MIC_BUTTON,
                    onSelect = { onSelectElement(TouchElementIds.MIC_BUTTON) },
                    onTransformChange = { x, y, scale, rot ->
                        onUpdateTransform(TouchElementIds.MIC_BUTTON, x, y, scale, rot)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            // Outer glowing pulse ring when active
                            if (isListening || isSpeaking) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scale(micAuraScale)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSpeaking) PurpleAi.copy(alpha = micAuraAlpha * 0.4f)
                                            else RedLive.copy(alpha = micAuraAlpha * 0.4f)
                                        )
                                )
                            }

                            // Main Circular Glass Mic Button (Large 54dp touch target)
                            Surface(
                                onClick = onToggleVoice,
                                shape = CircleShape,
                                color = if (isListening) RedLive else if (isSpeaking) PurpleAi else BluePrimary,
                                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.4f)),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .size(54.dp)
                                    .testTag("live_voice_button")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                        contentDescription = "Live Voice Session",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Compact Glass Text Input Bar (Wrapped for Touch Edit)
                val inputBarTransform = getElementTransform(TouchElementIds.INPUT_BAR)
                TouchEditableWrapper(
                    elementId = TouchElementIds.INPUT_BAR,
                    transform = inputBarTransform,
                    isEditMode = isEditMode,
                    isSelected = selectedElementId == TouchElementIds.INPUT_BAR,
                    onSelect = { onSelectElement(TouchElementIds.INPUT_BAR) },
                    onTransformChange = { x, y, scale, rot ->
                        onUpdateTransform(TouchElementIds.INPUT_BAR, x, y, scale, rot)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color(0xD90B132B),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = "Ask SPA AI Teacher (Hindi/English)...",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 13.sp
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                maxLines = 2
                            )

                            // Send Button
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        val msg = inputText
                                        inputText = ""
                                        onSendMessage(msg)
                                    }
                                },
                                enabled = inputText.isNotBlank() && !isAiThinking,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (inputText.isNotBlank() && !isAiThinking) BluePrimary
                                        else Color.White.copy(alpha = 0.12f)
                                    )
                                    .testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank() && !isAiThinking) Color.White else Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // === 4. BOTTOM FLOATING SELECTED ELEMENT INSPECTOR TOOLBAR IN EDIT MODE ===
        if (isEditMode && selectedElementId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                TouchEditorSelectedToolbar(
                    selectedTransform = selectedElementId?.let { getElementTransform(it) },
                    onUpdateTransform = { x, y, scale, rot ->
                        selectedElementId?.let { onUpdateTransform(it, x, y, scale, rot) }
                    },
                    onReplaceImage = {
                        activeImagePickerTarget = selectedElementId
                        imagePickerLauncher.launch("image/*")
                    },
                    onResetElement = {
                        selectedElementId?.let { onResetElement(it) }
                    },
                    onToggleVisibility = {
                        selectedElementId?.let { onToggleVisibility(it) }
                    },
                    onDeselect = {
                        onSelectElement(null)
                    }
                )
            }
        }

        // Voice Settings Dialog
        if (showVoiceSettingsDialog) {
            VoiceSettingsDialog(
                currentSpeed = speechSpeed,
                currentPitch = speechPitch,
                onSpeedChange = onUpdateSpeechSpeed,
                onPitchChange = onUpdateSpeechPitch,
                onReset = onResetVoiceSettings,
                onTestVoice = {
                    onSpeakMessage("नमस्ते, मैं SPA AI Teacher हूँ। यह आवाज़ की स्पीड और पिच की टेस्ट ऑडियो है।")
                },
                onDismiss = { showVoiceSettingsDialog = false }
            )
        }

        // Saved Chat Sessions Modal (Room Database Persistence)
        if (showSessionsModal) {
            ChatSessionsBottomSheet(
                sessions = chatSessions,
                currentSessionId = currentSessionId,
                selectedSubject = selectedSubject,
                onDismiss = { showSessionsModal = false },
                onSelectSession = { session ->
                    onSelectSession(session)
                    showSessionsModal = false
                },
                onCreateNewSession = { subj ->
                    onCreateNewSession(subj)
                    showSessionsModal = false
                },
                onRenameRequest = { session ->
                    sessionToRename = session
                    renameInputText = session.title
                },
                onDeleteRequest = { session ->
                    sessionToDelete = session
                }
            )
        }

        // Rename Session Dialog
        sessionToRename?.let { session ->
            RenameSessionDialog(
                currentTitle = renameInputText,
                onTitleChange = { renameInputText = it },
                onConfirm = {
                    if (renameInputText.isNotBlank()) {
                        onRenameSession(session.sessionId, renameInputText.trim())
                    }
                    sessionToRename = null
                },
                onDismiss = { sessionToRename = null }
            )
        }

        // Delete Session Confirmation Dialog
        sessionToDelete?.let { session ->
            DeleteSessionConfirmDialog(
                session = session,
                onConfirm = {
                    onDeleteSession(session.sessionId)
                    sessionToDelete = null
                },
                onDismiss = { sessionToDelete = null }
            )
        }

        // Custom Text / Academy Branding Edit Dialog
        if (showCustomTextEditDialog) {
            CustomTextEditDialog(
                mainTitle = editMainTitle,
                subTitle = editSubTitle,
                customMessage = editCustomMessage,
                onMainTitleChange = { editMainTitle = it },
                onSubTitleChange = { editSubTitle = it },
                onCustomMessageChange = { editCustomMessage = it },
                onSave = {
                    onUpdateCustomText(editMainTitle.trim(), editSubTitle.trim(), editCustomMessage.trim())
                    showCustomTextEditDialog = false
                },
                onDismiss = {
                    editMainTitle = mainTitle
                    editSubTitle = subTitle
                    editCustomMessage = customMessage
                    showCustomTextEditDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomTextEditDialog(
    mainTitle: String,
    subTitle: String,
    customMessage: String,
    onMainTitleChange: (String) -> Unit,
    onSubTitleChange: (String) -> Unit,
    onCustomMessageChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF111827),
            border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("custom_text_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Customize AI Doubts UI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Update the brand text and message displayed on the full-screen AI Teacher Doubts screen.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                )

                OutlinedTextField(
                    value = mainTitle,
                    onValueChange = onMainTitleChange,
                    label = { Text("Main Title (e.g. MITHILA ACADEMY)", color = AmberAccent) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_main_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = onCustomMessageChange,
                    label = { Text("Custom Quote / Message (e.g. ...)", color = AmberAccent) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_message"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = subTitle,
                    onValueChange = onSubTitleChange,
                    label = { Text("Subtitle (e.g. AI DOUBTS • AI TEACHER)", color = AmberAccent) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sub_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_custom_text"),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Save & Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceSettingsDialog(
    currentSpeed: Float,
    currentPitch: Float,
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onReset: () -> Unit,
    onTestVoice: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF141926),
            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("voice_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AmberAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Voice Settings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "आवाज़ की स्पीड और पिच सेटिंग्स",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // 1. Voice Speed (बोलने की गति)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = BlueSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Voice Speed (गति)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BluePrimary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, BlueSecondary.copy(alpha = 0.3f))
                        ) {
                            val speedLabel = when {
                                currentSpeed < 0.85f -> "धीमी (Slow)"
                                currentSpeed > 1.25f -> "तेज़ (Fast)"
                                else -> "सामान्य (Normal)"
                            }
                            Text(
                                text = "%.2fx • %s".format(currentSpeed, speedLabel),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BlueSecondary,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Slider(
                        value = currentSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = BlueSecondary,
                            activeTrackColor = BluePrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_speed_slider")
                    )

                    // Speed Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0.75f, 0.95f, 1.25f, 1.5f).forEach { preset ->
                            val label = if (preset == 0.95f) "1.0x Normal" else "${preset}x"
                            val isSelected = kotlin.math.abs(currentSpeed - preset) < 0.05f
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSpeedChange(preset) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) BlueSecondary else Color.White.copy(alpha = 0.1f),
                                    borderWidth = 1.dp
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                // 2. Voice Pitch (आवाज़ का सुर / पिच)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Voice Pitch (पिच/सुर)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberAccent.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.35f))
                        ) {
                            val pitchLabel = when {
                                currentPitch < 0.85f -> "गहरी (Deep)"
                                currentPitch > 1.25f -> "सुरीली (High)"
                                else -> "सामान्य (Natural)"
                            }
                            Text(
                                text = "%.2fx • %s".format(currentPitch, pitchLabel),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AmberAccent,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Slider(
                        value = currentPitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberAccent,
                            activeTrackColor = AmberAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_pitch_slider")
                    )

                    // Pitch Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0.8f, 1.0f, 1.25f, 1.45f).forEach { preset ->
                            val label = when (preset) {
                                0.8f -> "0.8x Deep"
                                1.0f -> "1.0x Normal"
                                1.25f -> "1.25x High"
                                else -> "1.45x Crisp"
                            }
                            val isSelected = kotlin.math.abs(currentPitch - preset) < 0.05f
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPitchChange(preset) },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = AmberAccent,
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) AmberAccent else Color.White.copy(alpha = 0.1f),
                                    borderWidth = 1.dp
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Action Buttons: Test Voice & Reset & Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.8f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }

                    // Test Voice Button
                    Button(
                        onClick = onTestVoice,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("test_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Test Voice",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Audio",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Done Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

data class QuickActionItem(
    val label: String,
    val prompt: String,
    val isExternal: Boolean = false,
    val url: String? = null
)

@Composable
fun FloatingGlassChatItem(
    message: UiMessage,
    teacherAvatarUri: String? = null,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    if (message.isAi) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BluePrimary, PurpleAi)))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!teacherAvatarUri.isNullOrBlank()) {
                    AsyncImage(
                        model = teacherAvatarUri,
                        contentDescription = "SPA AI Teacher Avatar",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_teacher_avatar),
                        contentDescription = "SPA AI Teacher Avatar",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "SPA AI TEACHER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BlueSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 14.dp,
                        bottomEnd = 14.dp,
                        bottomStart = 14.dp
                    ),
                    color = Color(0xD9070D1E),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 19.sp,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 48.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 4.dp,
                        bottomEnd = 14.dp,
                        bottomStart = 14.dp
                    ),
                    color = BluePrimary.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            lineHeight = 18.sp,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ChatSessionsBottomSheet(
    sessions: List<ChatSessionEntity>,
    currentSessionId: String,
    selectedSubject: String,
    onDismiss: () -> Unit,
    onSelectSession: (ChatSessionEntity) -> Unit,
    onCreateNewSession: (String) -> Unit,
    onRenameRequest: (ChatSessionEntity) -> Unit,
    onDeleteRequest: (ChatSessionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterSubject by remember { mutableStateOf("All") }

    val filteredSessions = remember(sessions, searchQuery, filterSubject) {
        sessions.filter { session ->
            val matchesSearch = searchQuery.isBlank() ||
                session.title.contains(searchQuery, ignoreCase = true) ||
                session.previewSnippet.contains(searchQuery, ignoreCase = true) ||
                session.subject.contains(searchQuery, ignoreCase = true)
            val matchesSubject = filterSubject == "All" || session.subject.equals(filterSubject, ignoreCase = true)
            matchesSearch && matchesSubject
        }
    }

    val availableSubjects = remember(sessions) {
        listOf("All") + sessions.map { it.subject }.filter { it.isNotBlank() && it != "All" }.distinct()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF101726),
            border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp)
                .shadow(28.dp, RoundedCornerShape(24.dp))
                .testTag("chat_sessions_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BluePrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = BlueSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Saved Chat Sessions",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 17.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BluePrimary.copy(alpha = 0.3f),
                                    contentColor = BlueSecondary
                                ) {
                                    Text(
                                        text = "${sessions.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Room Database Persistent Sessions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar: "+ Start New Chat" Button
                Button(
                    onClick = { onCreateNewSession(if (filterSubject != "All") filterSubject else selectedSubject) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("create_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start New Doubt Session",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search previous doubts & topics...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 12.sp
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = BlueSecondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("search_chat_sessions_input")
                )

                // Subject Filter Chips Row
                if (availableSubjects.size > 1) {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSubjects) { subj ->
                            val isSelected = filterSubject == subj
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterSubject = subj },
                                label = {
                                    Text(
                                        text = subj,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary.copy(alpha = 0.4f),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) BlueSecondary else Color.White.copy(alpha = 0.1f),
                                    borderWidth = 1.dp
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Sessions List
                if (filteredSessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "No sessions matching \"$searchQuery\"" else "No saved chat sessions yet",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredSessions,
                            key = { session: ChatSessionEntity -> session.sessionId }
                        ) { session: ChatSessionEntity ->
                            val isActive = session.sessionId == currentSessionId
                            val formattedDate = remember(session.updatedAt) {
                                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                                sdf.format(Date(session.updatedAt))
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isActive) Color(0xFF19253C) else Color(0xFF141C2B),
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) AmberAccent.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSession(session) }
                                    .testTag("session_item_${session.sessionId}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Row 1: Active Badge, Subject, Date, Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isActive) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = AmberAccent,
                                                    contentColor = Color.Black
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 9.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = BluePrimary.copy(alpha = 0.25f),
                                                border = BorderStroke(0.5.dp, BluePrimary.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = session.subject.ifBlank { "General" },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = BlueSecondary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                )
                                            }

                                            Text(
                                                text = formattedDate,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            // Rename
                                            IconButton(
                                                onClick = { onRenameRequest(session) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Rename Session",
                                                    tint = Color.White.copy(alpha = 0.65f),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            // Delete
                                            IconButton(
                                                onClick = { onDeleteRequest(session) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Session",
                                                    tint = Color(0xFFEF5350).copy(alpha = 0.8f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Row 2: Title
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Row 3: Preview snippet & message count
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = session.previewSnippet.ifBlank { "No preview text" },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "${session.messageCount} msg${if (session.messageCount != 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AmberAccent.copy(alpha = 0.85f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenameSessionDialog(
    currentTitle: String,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF141C2B),
            border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp))
                .testTag("rename_session_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Rename Session",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = currentTitle,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = BlueSecondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_session_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black),
                        modifier = Modifier.testTag("confirm_rename_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteSessionConfirmDialog(
    session: ChatSessionEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF5350)
                )
                Text(text = "Delete Chat Session?", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete \"${session.title}\" and its messages from local storage?",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_delete_session_button")
            ) {
                Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF141C2B),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("delete_session_confirm_dialog")
    )
}
