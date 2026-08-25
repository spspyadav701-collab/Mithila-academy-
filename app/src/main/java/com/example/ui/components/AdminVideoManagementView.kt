package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CourseEntity
import com.example.data.local.VideoEntity
import com.example.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVideoManagementView(
    videos: List<VideoEntity>,
    courses: List<CourseEntity>,
    teacherName: String,
    isUploading: Boolean,
    uploadProgress: Float,
    uploadStatusMessage: String,
    onSaveVideo: (
        videoId: String?,
        title: String,
        description: String,
        videoUriOrUrl: String,
        thumbnailUriOrUrl: String,
        subject: String,
        className: String,
        courseId: String,
        chapter: String,
        teacher: String,
        durationMinutes: Int,
        freeOrPaid: String,
        isPublished: Boolean,
        orderIndex: Int
    ) -> Unit,
    onTogglePublish: (String, Boolean) -> Unit,
    onUpdateOrder: (String, Int) -> Unit,
    onDeleteVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Form state
    var isFormExpanded by remember { mutableStateOf(true) }
    var editingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var deleteConfirmVideoId by remember { mutableStateOf<String?>(null) }

    var videoTitle by remember { mutableStateOf("") }
    var videoDescription by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var selectedClass by remember { mutableStateOf("Class 10") }
    var selectedCourseId by remember { mutableStateOf("crs_phy_10") }
    var chapterName by remember { mutableStateOf("Chapter 1: Light Reflection & Refraction") }
    var teacherInput by remember { mutableStateOf(teacherName.ifBlank { "SP Sir (Mithila Academy)" }) }
    var durationMinutes by remember { mutableStateOf(45) }
    var freeOrPaid by remember { mutableStateOf("Free") }
    var isPublished by remember { mutableStateOf(true) }
    var orderIndex by remember { mutableStateOf((videos.size + 1).coerceAtLeast(1)) }

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf<String?>(null) }
    var selectedThumbUri by remember { mutableStateOf<Uri?>(null) }
    var selectedThumbName by remember { mutableStateOf<String?>(null) }
    var customVideoUrl by remember { mutableStateOf("") }
    var customThumbUrl by remember { mutableStateOf("") }

    // Search and filter state for video inventory
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("All") } // All, Published, Draft, Free, Paid
    var filterSubject by remember { mutableStateOf("All") }

    val subjectList = listOf("Physics", "Mathematics", "Biology", "Chemistry", "Spoken English", "BPSC & Bihar Police", "GK & Current Affairs")
    val classList = listOf("Class 9", "Class 10", "Class 11", "Class 12", "Competitive / BPSC", "Bihar Police SI & Constable", "General / All")

    // Video File Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedVideoUri = it
            selectedVideoName = it.lastPathSegment ?: "selected_lecture.mp4"
        }
    }

    // Thumbnail Image Picker Launcher
    val thumbPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedThumbUri = it
            selectedThumbName = it.lastPathSegment ?: "thumbnail.jpg"
        }
    }

    // Filtered Video list
    val filteredVideos = videos.filter { video ->
        val matchesSearch = searchQuery.isBlank() ||
                video.title.contains(searchQuery, ignoreCase = true) ||
                video.chapter.contains(searchQuery, ignoreCase = true) ||
                video.teacherName.contains(searchQuery, ignoreCase = true) ||
                video.className.contains(searchQuery, ignoreCase = true)

        val matchesSubject = filterSubject == "All" || video.subject.contains(filterSubject, ignoreCase = true)

        val matchesStatus = when (filterStatus) {
            "Published" -> video.isPublished
            "Draft" -> !video.isPublished
            "Free" -> !video.isPaid
            "Paid" -> video.isPaid
            else -> true
        }

        matchesSearch && matchesSubject && matchesStatus
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Top Admin Video Management Header ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Video Lecture Management System",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Remote Cloud Storage & Real-Time Student Sync",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { isFormExpanded = !isFormExpanded },
                            modifier = Modifier.testTag("toggle_upload_form_btn")
                        ) {
                            Icon(
                                imageVector = if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Form",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }

                    // Stat Counters
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val publishedCount = videos.count { it.isPublished }
                        val draftCount = videos.count { !it.isPublished }
                        val freeCount = videos.count { !it.isPaid }
                        val paidCount = videos.count { it.isPaid }

                        MiniStatBadge("Total", "${videos.size}", Color(0xFF38BDF8), Modifier.weight(1f))
                        MiniStatBadge("Live", "$publishedCount", Color(0xFF4ADE80), Modifier.weight(1f))
                        MiniStatBadge("Drafts", "$draftCount", Color(0xFFFBBF24), Modifier.weight(1f))
                        MiniStatBadge("Free", "$freeCount", Color(0xFFA78BFA), Modifier.weight(1f))
                        MiniStatBadge("Paid", "$paidCount", Color(0xFFF472B6), Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 2. Video Upload Form Card (Collapsible) ---
        item {
            AnimatedVisibility(visible = isFormExpanded) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_video_upload_card")
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
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Upload & Publish New Lecture",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // File Selector Buttons (Phone / PC File Picker)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Video File Picker
                            OutlinedButton(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedVideoUri != null) Color(0xFF0369A1).copy(alpha = 0.25f) else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("select_video_file_btn")
                            ) {
                                Icon(
                                    imageVector = if (selectedVideoUri != null) Icons.Default.CheckCircle else Icons.Default.VideoFile,
                                    contentDescription = null,
                                    tint = if (selectedVideoUri != null) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedVideoName != null) selectedVideoName!!.take(12) + "..." else "Select Video",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Thumbnail Image Picker
                            OutlinedButton(
                                onClick = { thumbPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFA78BFA)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedThumbUri != null) Color(0xFF7C3AED).copy(alpha = 0.25f) else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("select_thumbnail_file_btn")
                            ) {
                                Icon(
                                    imageVector = if (selectedThumbUri != null) Icons.Default.CheckCircle else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = if (selectedThumbUri != null) Color(0xFF4ADE80) else Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedThumbName != null) selectedThumbName!!.take(12) + "..." else "Select Thumb",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Video Title
                        OutlinedTextField(
                            value = videoTitle,
                            onValueChange = { videoTitle = it },
                            label = { Text("Video Lecture Title *") },
                            placeholder = { Text("e.g. Class 10 Physics Chapter 1: Light Reflection & Refraction") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_video_title")
                        )

                        // Video Description
                        OutlinedTextField(
                            value = videoDescription,
                            onValueChange = { videoDescription = it },
                            label = { Text("Description / Lecture Summary") },
                            placeholder = { Text("Detailed concepts, formulas, and board/competitive exam tips...") },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_input_video_desc")
                        )

                        // Chapter Name & Video Order
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = chapterName,
                                onValueChange = { chapterName = it },
                                label = { Text("Chapter / Module") },
                                placeholder = { Text("e.g. Chapter 1: Ray Optics") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier
                                    .weight(2f)
                                    .testTag("admin_input_video_chapter")
                            )

                            OutlinedTextField(
                                value = if (orderIndex > 0) orderIndex.toString() else "",
                                onValueChange = { orderIndex = it.toIntOrNull() ?: 1 },
                                label = { Text("Order #") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_input_video_order")
                            )
                        }

                        // Subject Selection Chips
                        Column {
                            Text(
                                text = "Select Subject:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(subjectList) { subj ->
                                    val isSelected = selectedSubject == subj
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSubject = subj },
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
                        }

                        // Class / Grade Level Selection Chips
                        Column {
                            Text(
                                text = "Select Class / Target Exam:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(classList) { cls ->
                                    val isSelected = selectedClass == cls
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedClass = cls },
                                        label = { Text(cls, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF7C3AED),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF334155),
                                            labelColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }
                            }
                        }

                        // Course ID / Linking & Teacher Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedCourseId,
                                onValueChange = { selectedCourseId = it },
                                label = { Text("Course ID") },
                                placeholder = { Text("e.g. crs_phy_10") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_input_video_course_id")
                            )

                            OutlinedTextField(
                                value = teacherInput,
                                onValueChange = { teacherInput = it },
                                label = { Text("Teacher Name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_input_video_teacher")
                            )
                        }

                        // Duration (Minutes) & Optional Video URL Fallback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = durationMinutes.toString(),
                                onValueChange = { durationMinutes = it.toIntOrNull() ?: 40 },
                                label = { Text("Duration (Mins)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = customVideoUrl,
                                onValueChange = { customVideoUrl = it },
                                label = { Text("Cloud Video URL (Optional)") },
                                placeholder = { Text("https://storage.../video.mp4") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(2f)
                            )
                        }

                        // Status Controls: Free vs Paid & Published vs Draft
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Free / Paid Toggle
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Access Type: ",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilterChip(
                                    selected = freeOrPaid == "Free",
                                    onClick = { freeOrPaid = if (freeOrPaid == "Free") "Paid" else "Free" },
                                    label = {
                                        Text(
                                            text = if (freeOrPaid == "Free") "🟢 FREE" else "🔒 PAID",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (freeOrPaid == "Free") Color(0xFF059669) else Color(0xFFDC2626),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF334155),
                                        labelColor = Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.testTag("admin_toggle_free_paid")
                                )
                            }

                            // Publish Immediately Switch
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isPublished) "Published" else "Draft",
                                    color = if (isPublished) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = isPublished,
                                    onCheckedChange = { isPublished = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF22C55E),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFF334155)
                                    ),
                                    modifier = Modifier.testTag("admin_toggle_publish_switch")
                                )
                            }
                        }

                        // Upload Progress Indicator
                        if (isUploading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0B1120))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uploadStatusMessage.ifBlank { "Uploading video to Cloud Storage..." },
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${(uploadProgress * 100).toInt()}%",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color(0xFF334155)
                                )
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (videoTitle.isNotBlank() && !isUploading) {
                                    val videoPath = selectedVideoUri?.toString() ?: customVideoUrl
                                    val thumbPath = selectedThumbUri?.toString() ?: customThumbUrl

                                    onSaveVideo(
                                        null, // new video
                                        videoTitle,
                                        videoDescription,
                                        videoPath,
                                        thumbPath,
                                        selectedSubject,
                                        selectedClass,
                                        selectedCourseId,
                                        chapterName,
                                        teacherInput,
                                        durationMinutes,
                                        freeOrPaid,
                                        isPublished,
                                        orderIndex
                                    )

                                    // Reset fields after submit
                                    videoTitle = ""
                                    videoDescription = ""
                                    selectedVideoUri = null
                                    selectedVideoName = null
                                    selectedThumbUri = null
                                    selectedThumbName = null
                                    customVideoUrl = ""
                                    customThumbUrl = ""
                                    orderIndex = videos.size + 2
                                }
                            },
                            enabled = videoTitle.isNotBlank() && !isUploading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_submit_video_button")
                        ) {
                            Icon(
                                imageVector = if (isPublished) Icons.Default.Publish else Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUploading) "Uploading & Syncing..." else if (isPublished) "Publish Video Lecture Now" else "Save Video as Draft",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Video Inventory Hub & Search/Filters ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Video Content Inventory (${filteredVideos.size})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, chapter, class, or teacher...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_video_inventory_search")
                )

                // Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val filterOptions = listOf("All", "Published", "Draft", "Free", "Paid")
                    items(filterOptions) { opt ->
                        val isSelected = filterStatus == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterStatus = opt },
                            label = { Text(opt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }

        // --- 4. Video Items List ---
        if (filteredVideos.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No videos matching criteria",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredVideos, key = { it.videoId }) { video ->
                AdminVideoItemCard(
                    video = video,
                    onEdit = { editingVideo = video },
                    onTogglePublish = { onTogglePublish(video.videoId, video.isPublished) },
                    onUpdateOrder = { newOrder -> onUpdateOrder(video.videoId, newOrder) },
                    onDelete = { deleteConfirmVideoId = video.videoId }
                )
            }
        }
    }

    // --- 5. Edit Video Metadata Modal Dialog ---
    editingVideo?.let { v ->
        AdminEditVideoDialog(
            video = v,
            subjectList = subjectList,
            classList = classList,
            onDismiss = { editingVideo = null },
            onSave = { id, title, desc, subj, cls, courseId, ch, teacher, dur, freePaid, pub, order ->
                onSaveVideo(
                    id,
                    title,
                    desc,
                    v.videoUrl,
                    v.thumbnailUrl,
                    subj,
                    cls,
                    courseId,
                    ch,
                    teacher,
                    dur,
                    freePaid,
                    pub,
                    order
                )
                editingVideo = null
            }
        )
    }

    // --- 6. Delete Confirmation Dialog ---
    deleteConfirmVideoId?.let { vId ->
        AlertDialog(
            onDismissRequest = { deleteConfirmVideoId = null },
            title = { Text("Delete Video Lecture?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this video lecture? This will remove it from all student feeds.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVideo(vId)
                        deleteConfirmVideoId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Video", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmVideoId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// HELPER SUB-COMPONENTS
// -------------------------------------------------------------

@Composable
private fun MiniStatBadge(
    label: String,
    count: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun AdminVideoItemCard(
    video: VideoEntity,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit,
    onUpdateOrder: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, if (video.isPublished) Color(0xFF334155) else Color(0xFFFBBF24).copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_video_card_${video.videoId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail / Play Box
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 62.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (video.subject.contains("Physics", ignoreCase = true)) R.drawable.img_physics_lecture
                            else R.drawable.img_math_live
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Duration overlay
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "${video.durationMinutes}m",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Info column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Order index badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "#${video.orderIndex}",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Free / Paid Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (video.isPaid) Color(0xFFDC2626).copy(alpha = 0.2f) else Color(0xFF16A34A).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (video.isPaid) "PAID" else "FREE",
                                color = if (video.isPaid) Color(0xFFF87171) else Color(0xFF4ADE80),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Class tag
                        Text(
                            text = video.className,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${video.subject} • ${video.chapter}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Teacher: ${video.teacherName} • ${video.views} views",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(6.dp))

            // Action row: Status toggle, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Publish status toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onTogglePublish() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (video.isPublished) Color(0xFF22C55E) else Color(0xFFFBBF24))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (video.isPublished) "Live for Students" else "Draft (Hidden)",
                        color = if (video.isPublished) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_edit_video_${video.videoId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Video",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_video_${video.videoId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Video",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminEditVideoDialog(
    video: VideoEntity,
    subjectList: List<String>,
    classList: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        videoId: String,
        title: String,
        description: String,
        subject: String,
        className: String,
        courseId: String,
        chapter: String,
        teacher: String,
        durationMinutes: Int,
        freeOrPaid: String,
        isPublished: Boolean,
        orderIndex: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf(video.title) }
    var description by remember { mutableStateOf(video.description) }
    var subject by remember { mutableStateOf(video.subject) }
    var className by remember { mutableStateOf(video.className) }
    var courseId by remember { mutableStateOf(video.courseId) }
    var chapter by remember { mutableStateOf(video.chapter) }
    var teacher by remember { mutableStateOf(video.teacherName) }
    var durationMinutes by remember { mutableStateOf(video.durationMinutes) }
    var freeOrPaid by remember { mutableStateOf(video.freeOrPaid) }
    var isPublished by remember { mutableStateOf(video.isPublished) }
    var orderIndex by remember { mutableStateOf(video.orderIndex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Edit Video Lecture Metadata",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Update properties without re-uploading file",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Video Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chapter,
                            onValueChange = { chapter = it },
                            label = { Text("Chapter") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = orderIndex.toString(),
                            onValueChange = { orderIndex = it.toIntOrNull() ?: 1 },
                            label = { Text("Order") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Class") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = freeOrPaid == "Free",
                            onClick = { freeOrPaid = if (freeOrPaid == "Free") "Paid" else "Free" },
                            label = { Text(if (freeOrPaid == "Free") "Free Access" else "Paid Enrolled", fontSize = 11.sp) }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPublished) "Published" else "Draft",
                                color = if (isPublished) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isPublished,
                                onCheckedChange = { isPublished = it }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }

                        Button(
                            onClick = {
                                onSave(
                                    video.videoId,
                                    title,
                                    description,
                                    subject,
                                    className,
                                    courseId,
                                    chapter,
                                    teacher,
                                    durationMinutes,
                                    freeOrPaid,
                                    isPublished,
                                    orderIndex
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Changes", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
