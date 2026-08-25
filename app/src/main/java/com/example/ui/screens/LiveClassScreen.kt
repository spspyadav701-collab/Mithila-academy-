package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.local.LiveStreamEntity
import com.example.data.local.SubscriptionEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LiveClassScreen(
    liveStreams: List<LiveStreamEntity>,
    selectedStream: LiveStreamEntity?,
    chats: List<ChatMessageEntity>,
    subscriptions: List<SubscriptionEntity>,
    onSelectStream: (LiveStreamEntity) -> Unit,
    onSendComment: (String, String) -> Unit,
    onToggleSubscription: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine active stream
    val activeStream = selectedStream ?: liveStreams.firstOrNull()
    var commentText by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("1080p 60fps") }
    var showQualityMenu by remember { mutableStateOf(false) }

    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_badge_pulse"
    )

    // Auto-scroll chat on new message
    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            chatListState.animateScrollToItem(chats.size - 1)
        }
    }

    // Auto-select first stream if none selected
    LaunchedEffect(liveStreams) {
        if (selectedStream == null && liveStreams.isNotEmpty()) {
            onSelectStream(liveStreams.first())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeStream == null) {
            // No Live Streams Available View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RedLive.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = RedLive,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Live Class Currently Active",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Teachers can start live streaming from the 'Create' tab. Check back shortly for scheduled sessions!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // 1. Live Video Streaming Player (Top Component)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .testTag("live_video_player")
            ) {
                // Video Backdrop Artwork
                Image(
                    painter = painterResource(
                        id = when {
                            activeStream.subject.contains("Physics", ignoreCase = true) -> R.drawable.img_physics_lecture
                            activeStream.subject.contains("Math", ignoreCase = true) -> R.drawable.img_math_live
                            else -> R.drawable.img_physics_lecture
                        }
                    ),
                    contentDescription = "Live Video Stream",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark Video Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Live Top Overlay: Badge, Viewers & Quality
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RedLive,
                            modifier = Modifier.scale(pulseScale)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${activeStream.viewerCount} watching",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // HD Quality Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberAccent.copy(alpha = 0.9f),
                        modifier = Modifier.clickable { showQualityMenu = !showQualityMenu }
                    ) {
                        Text(
                            text = selectedQuality,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Video Player Center / Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Active Stream Header & Multi-Stream Switcher
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // Stream Title & Instructor Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeStream.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = activeStream.teacherName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = BluePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "• ${activeStream.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Subscribe to Teacher button
                        val isSubscribed = subscriptions.any { it.teacherId == activeStream.teacherId }
                        FilledTonalButton(
                            onClick = { onToggleSubscription(activeStream.teacherId, activeStream.teacherName) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else RedLive,
                                contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSubscribed) Icons.Default.Check else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSubscribed) "Subscribed" else "Subscribe",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Multi-stream selector chips (if more than 1 live class available)
                    if (liveStreams.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(liveStreams) { stream ->
                                val isSelected = stream.streamId == activeStream.streamId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectStream(stream) },
                                    label = {
                                        Text(
                                            text = "${stream.subject} Live",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 10.sp
                                            )
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) RedLive else Color.Gray)
                                        )
                                    },
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // 3. Real-Time Rolling Chat Section (Directly Underneath Player)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Rolling Chat Header Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Live Classroom Chat",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BluePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SPA AI Assistant Active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BluePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Messages Stream
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (chats.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Welcome to the live class! Ask your doubts in the chat below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    items(chats, key = { it.messageId }) { msg ->
                        LiveChatItem(chat = msg)
                    }
                }

                // Quick Doubt Helper Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickPrompts = listOf(
                        "Sir, please repeat this step 🙏",
                        "Formula doubt in derivation ✍️",
                        "Concept is 100% clear! ✅",
                        "Please give one example problem 💡"
                    )
                    items(quickPrompts) { prompt ->
                        SuggestionChip(
                            onClick = {
                                onSendComment(activeStream.streamId, prompt)
                            },
                            label = {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Bottom Chat Input Box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = {
                                Text(
                                    text = "Ask a question in live chat...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .testTag("live_chat_input")
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onSendComment(activeStream.streamId, commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (commentText.isNotBlank()) BluePrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("send_live_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Chat",
                                tint = if (commentText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveChatItem(chat: ChatMessageEntity) {
    val isAi = chat.isAiResponse || chat.senderRole == "ai"
    val isTeacher = chat.senderRole == "teacher"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isAi -> PurpleAi
                        isTeacher -> BluePrimary
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAi) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = chat.senderName.firstOrNull()?.toString() ?: "U",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isTeacher) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Message Body
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = chat.senderName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isAi -> PurpleAi
                            isTeacher -> BluePrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 11.sp
                    )
                )

                if (isAi) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PurpleAi.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "AI BOT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PurpleAi,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else if (isTeacher) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BluePrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TEACHER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BluePrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Text(
                text = chat.messageText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            )
        }
    }
}
