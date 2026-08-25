package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.*
import com.example.ui.AiTeacherViewModel
import com.example.ui.AppTab
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AdminWebSection(val title: String, val badge: String) {
    OVERVIEW("Overview", "Live"),
    VIDEOS("Videos", "Manage"),
    LIVE_STREAMS("Live Class", "Broadcast"),
    COURSES("Courses", "Batches"),
    STUDY_NOTES("Notes & PDFs", "Files"),
    NOTICES("Notices", "Alerts"),
    AI_CONFIG("AI Teacher & KB", "Gemini"),
    BRANDING("Branding", "Custom")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: AiTeacherViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val adminPasscodeInput by viewModel.adminPasscodeInput.collectAsState()
    val adminPasscodeError by viewModel.adminPasscodeError.collectAsState()

    val teacherName by viewModel.teacherProfileName.collectAsState()
    val teacherSpecialization by viewModel.teacherSpecialization.collectAsState()
    val teacherBio by viewModel.teacherBio.collectAsState()
    val teacherQualification by viewModel.teacherQualification.collectAsState()

    val customTeacherImageUri by viewModel.customTeacherImageUri.collectAsState()
    val customLogoImageUri by viewModel.customLogoImageUri.collectAsState()
    val customBgImageUri by viewModel.customBgImageUri.collectAsState()

    val videos by viewModel.videos.collectAsState()
    val liveStreams by viewModel.liveStreams.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allKnowledge by viewModel.allKnowledge.collectAsState()

    val speechSpeed by viewModel.voiceManager.speechSpeed.collectAsState()
    val speechPitch by viewModel.voiceManager.speechPitch.collectAsState()

    var activeSection by remember { mutableStateOf(AdminWebSection.OVERVIEW) }
    val coroutineScope = rememberCoroutineScope()

    if (!isAdminAuthenticated) {
        // Locked Screen: Web Admin Security Challenge
        AdminLockScreen(
            passcodeInput = adminPasscodeInput,
            passcodeError = adminPasscodeError,
            onPasscodeChange = { viewModel.setAdminPasscodeInput(it) },
            onVerify = { viewModel.verifyAdminPasscode() },
            onReturnHome = { viewModel.setTab(AppTab.HOME) }
        )
    } else {
        // Authenticated Web-Style Admin Dashboard
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .testTag("admin_dashboard_web_screen")
        ) {
            // Simulated Web Browser Header Bar
            WebBrowserHeaderBar(
                teacherName = teacherName,
                onLockSession = { viewModel.lockAdminSession() },
                onReturnToApp = { viewModel.setTab(AppTab.HOME) }
            )

            // Web Navigation Pills
            WebNavigationPillRow(
                activeSection = activeSection,
                onSelectSection = { activeSection = it }
            )

            // Web Dashboard Body Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
            ) {
                when (activeSection) {
                    AdminWebSection.OVERVIEW -> {
                        AdminOverviewContent(
                            videosCount = videos.size,
                            liveStreamsCount = liveStreams.size,
                            coursesCount = allCourses.size,
                            notesCount = allNotes.size,
                            noticesCount = allNotices.size,
                            knowledgeCount = allKnowledge.size,
                            teacherName = teacherName,
                            onNavigateToSection = { activeSection = it }
                        )
                    }

                    AdminWebSection.VIDEOS -> {
                        AdminVideosManager(
                            videos = videos,
                            onPublishVideo = { title, desc, subject, duration ->
                                viewModel.addNewVideo(title, desc, subject, duration)
                            },
                            onDeleteVideo = { videoId ->
                                viewModel.deleteVideo(videoId)
                            }
                        )
                    }

                    AdminWebSection.LIVE_STREAMS -> {
                        AdminLiveStreamManager(
                            liveStreams = liveStreams,
                            onStartLive = { title, subject ->
                                viewModel.startNewLiveStream(title, subject)
                            },
                            onEndStream = { streamId ->
                                viewModel.endLiveStream(streamId)
                            },
                            onDeleteStream = { streamId ->
                                viewModel.deleteLiveStream(streamId)
                            }
                        )
                    }

                    AdminWebSection.COURSES -> {
                        AdminCoursesManager(
                            courses = allCourses,
                            onAddCourse = { title, cat, price, isFree, desc, dur ->
                                viewModel.addCourse(title, cat, price, isFree, desc, dur)
                            },
                            onDeleteCourse = { courseId ->
                                viewModel.deleteCourse(courseId)
                            }
                        )
                    }

                    AdminWebSection.STUDY_NOTES -> {
                        AdminNotesManager(
                            notes = allNotes,
                            onAddNote = { title, subj, cat, chapter, content, isFree ->
                                viewModel.addNote(title, subj, cat, chapter, content, isFree)
                            },
                            onDeleteNote = { noteId ->
                                viewModel.deleteNote(noteId)
                            }
                        )
                    }

                    AdminWebSection.NOTICES -> {
                        AdminNoticesManager(
                            notices = allNotices,
                            onAddNotice = { title, content, cat, isImportant ->
                                viewModel.addNotice(title, content, cat, isImportant)
                            },
                            onDeleteNotice = { noticeId ->
                                viewModel.deleteNotice(noticeId)
                            }
                        )
                    }

                    AdminWebSection.AI_CONFIG -> {
                        AdminAiTeacherConfig(
                            teacherName = teacherName,
                            teacherSpecialization = teacherSpecialization,
                            teacherBio = teacherBio,
                            teacherQualification = teacherQualification,
                            speechSpeed = speechSpeed,
                            speechPitch = speechPitch,
                            allKnowledge = allKnowledge,
                            onUpdateProfile = { n, s, b, q ->
                                viewModel.updateTeacherProfile(n, s, b, q)
                            },
                            onUpdateVoiceSpeed = { viewModel.voiceManager.setSpeechSpeed(it) },
                            onUpdateVoicePitch = { viewModel.voiceManager.setSpeechPitch(it) },
                            onTestVoice = {
                                viewModel.voiceManager.speak(
                                    "Namaste! Main SP Sir hoon. Mithila Academy AI Doubt System active hai.",
                                    force = true
                                )
                            },
                            onAddKnowledge = { t, s, c, k ->
                                viewModel.addKnowledgeItem(t, s, c, k)
                            },
                            onDeleteKnowledge = {
                                viewModel.deleteKnowledgeItem(it)
                            }
                        )
                    }

                    AdminWebSection.BRANDING -> {
                        AdminBrandingManager(
                            teacherName = teacherName,
                            customTeacherImageUri = customTeacherImageUri,
                            customLogoImageUri = customLogoImageUri,
                            customBgImageUri = customBgImageUri,
                            onUpdateTeacherImage = { viewModel.updateCustomTeacherImage(it) },
                            onUpdateLogoImage = { viewModel.updateCustomLogoImage(it) },
                            onUpdateBgImage = { viewModel.updateCustomBgImage(it) },
                            onResetBranding = { viewModel.resetToDefaultBranding() }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. WEB BROWSER SIMULATION HEADER
// -------------------------------------------------------------
@Composable
private fun WebBrowserHeaderBar(
    teacherName: String,
    onLockSession: () -> Unit,
    onReturnToApp: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Simulated URL bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SSL Secure",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "https://admin.mithilaacademy.edu/portal/sp-sir",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "HTTPS SECURE",
                        color = Color(0xFF4ADE80),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Web Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Pro",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Admin Control Portal",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Logged in: $teacherName (Admin Pro)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReturnToApp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF93C5FD)),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Student View",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Student View", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onLockSession,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Session",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lock", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. WEB NAVIGATION PILL BAR
// -------------------------------------------------------------
@Composable
private fun WebNavigationPillRow(
    activeSection: AdminWebSection,
    onSelectSection: (AdminWebSection) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = activeSection.ordinal,
        containerColor = Color(0xFF0F172A),
        contentColor = Color(0xFF60A5FA),
        edgePadding = 12.dp,
        divider = { HorizontalDivider(color = Color(0xFF1E293B)) },
        modifier = Modifier.fillMaxWidth()
    ) {
        AdminWebSection.values().forEach { section ->
            val isSelected = activeSection == section
            Tab(
                selected = isSelected,
                onClick = { onSelectSection(section) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = section.title,
                            color = if (isSelected) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.3f) else Color(0xFF334155),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = section.badge,
                                color = if (isSelected) Color(0xFF93C5FD) else Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 3. ADMIN OVERVIEW TAB
// -------------------------------------------------------------
@Composable
private fun AdminOverviewContent(
    videosCount: Int,
    liveStreamsCount: Int,
    coursesCount: Int,
    notesCount: Int,
    noticesCount: Int,
    knowledgeCount: Int,
    teacherName: String,
    onNavigateToSection: (AdminWebSection) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back, $teacherName 👋",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mithila Academy Web Management Center • Full RBAC Control",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SYSTEM ONLINE",
                                    color = Color(0xFF4ADE80),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid (2 columns)
        item {
            Text(
                text = "Live Academy Metrics",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Video Lectures",
                    count = "$videosCount Lectures",
                    subtext = "Uploaded & Ready",
                    icon = Icons.Default.VideoLibrary,
                    accentColor = Color(0xFF38BDF8),
                    onClick = { onNavigateToSection(AdminWebSection.VIDEOS) },
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Live Classes",
                    count = "$liveStreamsCount Active",
                    subtext = "Broadcast Studio",
                    icon = Icons.Default.LiveTv,
                    accentColor = Color(0xFFF87171),
                    onClick = { onNavigateToSection(AdminWebSection.LIVE_STREAMS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Active Batches",
                    count = "$coursesCount Courses",
                    subtext = "Enrolled Students",
                    icon = Icons.Default.School,
                    accentColor = Color(0xFFA78BFA),
                    onClick = { onNavigateToSection(AdminWebSection.COURSES) },
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Study Notes & PDFs",
                    count = "$notesCount Notes",
                    subtext = "Download Handouts",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = Color(0xFF34D399),
                    onClick = { onNavigateToSection(AdminWebSection.STUDY_NOTES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    title = "Notice Board",
                    count = "$noticesCount Notices",
                    subtext = "Exam & Class Alerts",
                    icon = Icons.Default.Campaign,
                    accentColor = Color(0xFFFBBF24),
                    onClick = { onNavigateToSection(AdminWebSection.NOTICES) },
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "AI Vector Knowledge",
                    count = "$knowledgeCount Concepts",
                    subtext = "Gemini 2.5 Engine",
                    icon = Icons.Default.Psychology,
                    accentColor = Color(0xFFEC4899),
                    onClick = { onNavigateToSection(AdminWebSection.AI_CONFIG) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Admin Actions
        item {
            Text(
                text = "Quick Publishing Tools",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminActionButton(
                        title = "Upload New Video Lecture",
                        desc = "Add MP4 video, title, tags, and assign subject",
                        icon = Icons.Default.CloudUpload,
                        buttonColor = Color(0xFF0284C7),
                        onClick = { onNavigateToSection(AdminWebSection.VIDEOS) }
                    )
                    AdminActionButton(
                        title = "Schedule Live Broadcast",
                        desc = "Launch instant live class with chat and stream key",
                        icon = Icons.Default.LiveTv,
                        buttonColor = Color(0xFFDC2626),
                        onClick = { onNavigateToSection(AdminWebSection.LIVE_STREAMS) }
                    )
                    AdminActionButton(
                        title = "Create New Course Batch",
                        desc = "Add comprehensive curriculum with pricing/free tags",
                        icon = Icons.Default.AddBox,
                        buttonColor = Color(0xFF7C3AED),
                        onClick = { onNavigateToSection(AdminWebSection.COURSES) }
                    )
                    AdminActionButton(
                        title = "Post Official Notice",
                        desc = "Publish important notifications for all enrolled students",
                        icon = Icons.Default.NotificationAdd,
                        buttonColor = Color(0xFFD97706),
                        onClick = { onNavigateToSection(AdminWebSection.NOTICES) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. ADMIN VIDEOS MANAGER
// -------------------------------------------------------------
@Composable
private fun AdminVideosManager(
    videos: List<VideoEntity>,
    onPublishVideo: (String, String, String, Int) -> Unit,
    onDeleteVideo: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var durationMinutes by remember { mutableStateOf(40) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload Card Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Upload Video Lecture",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Video Lecture Title") },
                        placeholder = { Text("e.g. Ray Optics: Lens Maker Formula") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Lecture Description / Summary") },
                        placeholder = { Text("Key formulas, derivation, and concepts...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Subject Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Physics", "Mathematics", "Spoken English", "Chemistry", "Biology").forEach { subj ->
                            val isSelected = subject == subj
                            FilterChip(
                                selected = isSelected,
                                onClick = { subject = subj },
                                label = { Text(subj, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF334155),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    // Upload Progress Bar if active
                    if (isUploading) {
                        Column {
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155)
                            )
                            Text(
                                text = "Encoding & Publishing: ${(uploadProgress * 100).toInt()}%",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                isUploading = true
                                coroutineScope.launch {
                                    for (i in 1..10) {
                                        delay(100)
                                        uploadProgress = i / 10f
                                    }
                                    onPublishVideo(title, description, subject, durationMinutes)
                                    title = ""
                                    description = ""
                                    isUploading = false
                                    uploadProgress = 0f
                                }
                            }
                        },
                        enabled = title.isNotBlank() && !isUploading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUploading) "Publishing..." else "Publish Video Lecture",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Existing Videos Management Table
        item {
            Text(
                text = "Manage Published Videos (${videos.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(videos, key = { it.videoId }) { video ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${video.subject} • ${video.durationMinutes} mins • ${video.views} views",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDeleteVideo(video.videoId) },
                        modifier = Modifier.testTag("btn_delete_video_${video.videoId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Video",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. ADMIN LIVE STREAM STUDIO
// -------------------------------------------------------------
@Composable
private fun AdminLiveStreamManager(
    liveStreams: List<LiveStreamEntity>,
    onStartLive: (String, String) -> Unit,
    onEndStream: (String) -> Unit,
    onDeleteStream: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start Live Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Broadcast Live Class",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Live Session Title") },
                        placeholder = { Text("e.g. Physics Class 12: Live Problem Solving") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF87171),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Physics", "Mathematics", "Spoken English", "BPSC").forEach { subj ->
                            val isSelected = subject == subj
                            FilterChip(
                                selected = isSelected,
                                onClick = { subject = subj },
                                label = { Text(subj, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDC2626),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF334155),
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onStartLive(title, subject)
                                title = ""
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Live Broadcast Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Active & Ended Streams Table
        item {
            Text(
                text = "Live Stream Broadcasts (${liveStreams.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(liveStreams, key = { it.streamId }) { stream ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (stream.status == "live") Color(0xFFDC2626) else Color(0xFF475569)
                            ) {
                                Text(
                                    text = stream.status.uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stream.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "Key: ${stream.streamKey} • ${stream.viewerCount} Viewers",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row {
                        if (stream.status == "live") {
                            IconButton(onClick = { onEndStream(stream.streamId) }) {
                                Icon(Icons.Default.StopCircle, contentDescription = "End Stream", tint = Color(0xFFFBBF24))
                            }
                        }
                        IconButton(onClick = { onDeleteStream(stream.streamId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. ADMIN COURSES MANAGER
// -------------------------------------------------------------
@Composable
private fun AdminCoursesManager(
    courses: List<CourseEntity>,
    onAddCourse: (String, String, String, Boolean, String, String) -> Unit,
    onDeleteCourse: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Physics (Class XII)") }
    var price by remember { mutableStateOf("₹999") }
    var isFree by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Create Course Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Create New Course Batch",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Course Title") },
                        placeholder = { Text("e.g. Physics Target Batch 2026") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price (e.g. ₹499)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Batch Curriculum Overview") },
                        placeholder = { Text("Complete NCERT + Advanced Problem solving...") },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFA78BFA),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddCourse(title, category, price, isFree, description, "45 Hours")
                                title = ""
                                description = ""
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.AddBox, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Course to Catalog", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Existing Courses List
        item {
            Text(
                text = "Course Catalog (${courses.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(courses, key = { it.courseId }) { course ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = course.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${course.category} • ${course.price} • ${course.lessonsCount} Lessons",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(onClick = { onDeleteCourse(course.courseId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Course", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. ADMIN NOTES & PDFS MANAGER
// -------------------------------------------------------------
@Composable
private fun AdminNotesManager(
    notes: List<NoteEntity>,
    onAddNote: (String, String, String, String, String, Boolean) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("Chapter 1: Electrostatics") }
    var contentText by remember { mutableStateOf("") }
    var isFree by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload Note Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Upload Study Handout / Chapter Note",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g. Formula Sheet: Electric Field & Potential") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF34D399),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = chapter,
                            onValueChange = { chapter = it },
                            label = { Text("Chapter") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF34D399),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        label = { Text("Note Content / Key Summary Text") },
                        placeholder = { Text("Full explanation, derivations, theorems...") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (title.isNotBlank() && contentText.isNotBlank()) {
                                onAddNote(title, subject, "Full Chapter", chapter, contentText, isFree)
                                title = ""
                                contentText = ""
                            }
                        },
                        enabled = title.isNotBlank() && contentText.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish Study Note", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Existing Notes List
        item {
            Text(
                text = "Study Material Library (${notes.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(notes, key = { it.noteId }) { note ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${note.subject} • ${note.chapter} • ${note.downloadSize}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(onClick = { onDeleteNote(note.noteId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. ADMIN NOTICES MANAGER
// -------------------------------------------------------------
@Composable
private fun AdminNoticesManager(
    notices: List<NoticeEntity>,
    onAddNotice: (String, String, String, Boolean) -> Unit,
    onDeleteNotice: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Examination") }
    var content by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Publish Official Notice",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notice Headline") },
                        placeholder = { Text("e.g. Class 12 Board Practical Exam Dates Announced") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFBBF24),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Notice Details") },
                        placeholder = { Text("Detailed circular message for students...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFBBF24),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isImportant,
                                onCheckedChange = { isImportant = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B))
                            )
                            Text("Mark as High Priority Alert", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onAddNotice(title, content, category, isImportant)
                                title = ""
                                content = ""
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Post Notice Circular", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        items(notices, key = { it.noticeId }) { notice ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (notice.isImportant) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "URGENT",
                                        color = Color(0xFFF87171),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = notice.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${notice.category} • ${notice.dateText}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    IconButton(onClick = { onDeleteNotice(notice.noticeId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Notice", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. ADMIN AI TEACHER CONFIG & KNOWLEDGE BASE
// -------------------------------------------------------------
@Composable
private fun AdminAiTeacherConfig(
    teacherName: String,
    teacherSpecialization: String,
    teacherBio: String,
    teacherQualification: String,
    speechSpeed: Float,
    speechPitch: Float,
    allKnowledge: List<KnowledgeEntity>,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onUpdateVoiceSpeed: (Float) -> Unit,
    onUpdateVoicePitch: (Float) -> Unit,
    onTestVoice: () -> Unit,
    onAddKnowledge: (String, String, String, String) -> Unit,
    onDeleteKnowledge: (KnowledgeEntity) -> Unit
) {
    var editName by remember { mutableStateOf(teacherName) }
    var editSpecialization by remember { mutableStateOf(teacherSpecialization) }
    var editBio by remember { mutableStateOf(teacherBio) }
    var editQualification by remember { mutableStateOf(teacherQualification) }

    var kTitle by remember { mutableStateOf("") }
    var kSubject by remember { mutableStateOf("Physics") }
    var kContent by remember { mutableStateOf("") }
    var kKeywords by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher AI Identity Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AI Teacher Persona & Credentials",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Teacher Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editSpecialization,
                            onValueChange = { editSpecialization = it },
                            label = { Text("Specialization") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFEC4899),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editQualification,
                            onValueChange = { editQualification = it },
                            label = { Text("Qualification") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFEC4899),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Teacher Biography") },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onUpdateProfile(editName, editSpecialization, editBio, editQualification)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Teacher Profile Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Voice Synthesizer Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "AI Voice Synthesizer (TTS)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Speech Speed: ${"%.2f".format(speechSpeed)}x",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Slider(
                        value = speechSpeed,
                        onValueChange = onUpdateVoiceSpeed,
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFEC4899),
                            activeTrackColor = Color(0xFFEC4899)
                        )
                    )

                    Text(
                        text = "Speech Pitch: ${"%.2f".format(speechPitch)}x",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Slider(
                        value = speechPitch,
                        onValueChange = onUpdateVoicePitch,
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFEC4899),
                            activeTrackColor = Color(0xFFEC4899)
                        )
                    )

                    OutlinedButton(
                        onClick = onTestVoice,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF472B6)),
                        border = BorderStroke(1.dp, Color(0xFFEC4899)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test AI Voice Output")
                    }
                }
            }
        }

        // Add Knowledge Vector Entry Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add Concept to AI Vector Knowledge Base",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = kTitle,
                        onValueChange = { kTitle = it },
                        label = { Text("Concept Topic") },
                        placeholder = { Text("e.g. Total Internal Reflection") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = kContent,
                        onValueChange = { kContent = it },
                        label = { Text("Core Teaching Explanation") },
                        placeholder = { Text("Detailed explanation, formula, conditions...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (kTitle.isNotBlank() && kContent.isNotBlank()) {
                                onAddKnowledge(kTitle, kSubject, kContent, kKeywords)
                                kTitle = ""
                                kContent = ""
                            }
                        },
                        enabled = kTitle.isNotBlank() && kContent.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Vector Knowledge Base", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        items(allKnowledge, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(item.content, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 2)
                    }
                    IconButton(onClick = { onDeleteKnowledge(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. ADMIN BRANDING MANAGER
// -------------------------------------------------------------
@Composable
private fun AdminBrandingManager(
    teacherName: String,
    customTeacherImageUri: String?,
    customLogoImageUri: String?,
    customBgImageUri: String?,
    onUpdateTeacherImage: (String?) -> Unit,
    onUpdateLogoImage: (String?) -> Unit,
    onUpdateBgImage: (String?) -> Unit,
    onResetBranding: () -> Unit
) {
    var teacherUrlInput by remember { mutableStateOf("") }
    var logoUrlInput by remember { mutableStateOf("") }

    val teacherImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> onUpdateTeacherImage(uri.toString()) }
    }
    val logoImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> onUpdateLogoImage(uri.toString()) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Academy Visual Identity & Branding",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Teacher Avatar Image Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SP Sir Teacher Avatar Photo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Shown on Live Avatar & Voice Header", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        Button(
                            onClick = { teacherImagePicker.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Upload Photo", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = teacherUrlInput,
                        onValueChange = {
                            teacherUrlInput = it
                            if (it.startsWith("http")) onUpdateTeacherImage(it)
                        },
                        label = { Text("Or Paste Teacher Image URL (https://...)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color(0xFF334155))

                    // Academy Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Mithila Academy Official Logo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Custom branding for drawer & top bar", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        Button(
                            onClick = { logoImagePicker.launch("image/*") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Upload Logo", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = logoUrlInput,
                        onValueChange = {
                            logoUrlInput = it
                            if (it.startsWith("http")) onUpdateLogoImage(it)
                        },
                        label = { Text("Or Paste Logo Image URL (https://...)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onResetBranding,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Default Academy Branding")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 11. LOCK SCREEN (SECURITY CHALLENGE)
// -------------------------------------------------------------
@Composable
private fun AdminLockScreen(
    passcodeInput: String,
    passcodeError: String?,
    onPasscodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onReturnHome: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF1E1B4B))
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top SSL Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MITHILA ACADEMY WEB PORTAL",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "PORTAL ID: SP-9631",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                            )
                        )
                        .border(3.dp, Color(0xFF60A5FA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Shield",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Web Admin Dashboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Role-Based Access Control: Administrator mode is secured with a master passcode. Only authorized faculty (SP Sir) can access.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                OutlinedTextField(
                    value = passcodeInput,
                    onValueChange = onPasscodeChange,
                    label = { Text("Admin Master Passcode") },
                    placeholder = { Text("Enter secret passcode...") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Passcode Key",
                            tint = Color(0xFF60A5FA)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Passcode",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    },
                    isError = passcodeError != null,
                    supportingText = {
                        if (passcodeError != null) {
                            Text(
                                text = passcodeError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF60A5FA),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = Color(0xFF60A5FA),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_lock_passcode_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnHome,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Student App")
                    }

                    Button(
                        onClick = onVerify,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_unlock_admin_portal")
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unlock Dashboard", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER METRIC & ACTION CARD COMPONENTS
// -------------------------------------------------------------
@Composable
private fun AdminMetricCard(
    title: String,
    count: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = count,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtext,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun AdminActionButton(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(buttonColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = buttonColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = desc,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }
    }
}
