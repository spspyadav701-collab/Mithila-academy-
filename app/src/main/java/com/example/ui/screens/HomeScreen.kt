package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.AiTeacherViewModel
import com.example.ui.AppTab

@Composable
fun HomeScreen(
    viewModel: AiTeacherViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headerCategory by viewModel.headerCategory.collectAsState()
    val showCategorySelector by viewModel.showCategorySelector.collectAsState()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val liveStreams by viewModel.liveStreams.collectAsState()

    val scrollState = rememberScrollState()

    // Pulsing animation for LIVE badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFD))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("home_screen")
    ) {
        // --- 1. TOP HEADER SECTION ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_hamburger_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Center-Left: Course / Category Selector Pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { viewModel.toggleCategorySelector(true) }
                    .testTag("btn_category_selector")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small circular avatar / icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SP",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = headerCategory,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Category",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (headerCategory == "Spoken") "Spoken English" else "$headerCategory Special",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Notification Bell
            Box {
                IconButton(
                    onClick = { viewModel.toggleNotificationsDialog(true) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_notifications")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(26.dp)
                    )
                }
                // Notification unread indicator
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. LIVE CLASS CARD ---
        val activeStream = liveStreams.firstOrNull()
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF7CB6EB),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    viewModel.setTab(AppTab.LIVE_CLASS)
                }
                .testTag("card_live_class")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Stream Icon with pulsing badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFECEC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "Live Class",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing red dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE11D48).copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE CLASS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (activeStream != null) "${activeStream.title} • Tap to Join" else "Join now • Limited seats",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Join button pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEBF3FE),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "Join Now",
                        color = Color(0xFF1A73E8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- 3. MAIN 8-BUTTON COURSE GRID (2 Columns) ---
        val gridItems = listOf(
            GridButtonData(
                title = "All Courses",
                icon = Icons.Default.School,
                iconBg = Color(0xFFF3E8FF),
                iconTint = Color(0xFF9333EA),
                destination = AppTab.ALL_COURSES,
                testTag = "btn_all_courses"
            ),
            GridButtonData(
                title = "Notes",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                destination = AppTab.NOTES,
                testTag = "btn_notes"
            ),
            GridButtonData(
                title = "My Courses",
                icon = Icons.Default.Assignment,
                iconBg = Color(0xFFEDE9FE),
                iconTint = Color(0xFF7C3AED),
                destination = AppTab.MY_COURSES,
                testTag = "btn_my_courses"
            ),
            GridButtonData(
                title = "Social",
                icon = Icons.Default.Public,
                iconBg = Color(0xFFE0F7FA),
                iconTint = Color(0xFF00ACC1),
                destination = AppTab.SOCIAL,
                testTag = "btn_social"
            ),
            GridButtonData(
                title = "Test",
                icon = Icons.Default.Quiz,
                iconBg = Color(0xFFEBF3FE),
                iconTint = Color(0xFF1A73E8),
                destination = AppTab.TEST,
                testTag = "btn_test"
            ),
            GridButtonData(
                title = "Free Videos",
                icon = Icons.Default.PlayCircle,
                iconBg = Color(0xFFFFEDD5),
                iconTint = Color(0xFFEA580C),
                destination = AppTab.FREE_VIDEOS,
                testTag = "btn_free_videos"
            ),
            GridButtonData(
                title = "Free Test",
                icon = Icons.Default.FactCheck,
                iconBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0369A1),
                destination = AppTab.FREE_TEST,
                testTag = "btn_free_test"
            ),
            GridButtonData(
                title = "Free Notes",
                icon = Icons.Default.LibraryBooks,
                iconBg = Color(0xFFDCFCE7),
                iconTint = Color(0xFF16A34A),
                destination = AppTab.FREE_NOTES,
                testTag = "btn_free_notes"
            )
        )

        // Render 4 rows of 2 buttons
        for (i in gridItems.indices step 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Button 1
                EducationalGridCard(
                    data = gridItems[i],
                    onClick = { viewModel.setTab(gridItems[i].destination) },
                    modifier = Modifier.weight(1f)
                )

                // Button 2 (if exists)
                if (i + 1 < gridItems.size) {
                    EducationalGridCard(
                        data = gridItems[i + 1],
                        onClick = { viewModel.setTab(gridItems[i + 1].destination) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // --- 4. AI DOUBTS SECTION ---
        Text(
            text = "AI Doubts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Surface(
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable { viewModel.setTab(AppTab.AI_CHAT) }
                .testTag("card_ai_doubts")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF064E3B), // Rich dark emerald
                                Color(0xFF0F766E), // Deep cyan-teal
                                Color(0xFF0284C7)  // Vibrant sky blue
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left "Ai" Badge icon in soft red/orange rounded container
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ai",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ask AI Doubts",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Get instant voice & text help for your questions",
                            fontSize = 12.sp,
                            color = Color(0xFFE0F2FE),
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Circular Arrow Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open AI Doubts",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- CATEGORY SELECTOR DIALOG ---
    if (showCategorySelector) {
        Dialog(onDismissRequest = { viewModel.toggleCategorySelector(false) }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Select Course Stream",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val categories = listOf(
                        "Spoken" to "Spoken English Fluency & Vocabulary",
                        "Physics" to "Physics Master Series (Class 10-12 & Comp)",
                        "Mathematics" to "Maths Speed & High Calculation",
                        "BPSC" to "BPSC 2026 & Bihar Police Special GS",
                        "Biology" to "Biology & NEET Foundations"
                    )

                    categories.forEach { (cat, desc) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (headerCategory == cat) Color(0xFFE8F1FC) else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setHeaderCategory(cat) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = headerCategory == cat,
                                    onClick = { viewModel.setHeaderCategory(cat) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = cat,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- NOTIFICATIONS DIALOG ---
    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { viewModel.toggleNotificationsDialog(false) }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📢 Notifications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { viewModel.toggleNotificationsDialog(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (notices.isEmpty()) {
                        Text("No notifications available right now.", color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            notices.take(3).forEach { notice ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = notice.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notice.content,
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notice.dateText,
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
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

data class GridButtonData(
    val title: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val destination: AppTab,
    val testTag: String
)

@Composable
fun EducationalGridCard(
    data: GridButtonData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier
            .height(68.dp)
            .border(
                width = 1.2.dp,
                color = Color(0xFF7CB6EB),
                shape = RoundedCornerShape(26.dp)
            )
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .testTag(data.testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(data.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.title,
                    tint = data.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = data.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
