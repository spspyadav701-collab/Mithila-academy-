package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KnowledgeEntity
import com.example.ui.theme.*

enum class OwnerSubTab(val title: String) {
    KNOWLEDGE("AI Knowledge"),
    PUBLISH_VIDEO("New Video"),
    START_LIVE("Start Live")
}

@Composable
fun OwnerLearningScreen(
    isAuthenticated: Boolean,
    pinInput: String,
    pinError: String?,
    allKnowledge: List<KnowledgeEntity>,
    onPinChange: (String) -> Unit,
    onVerifyPin: () -> Unit,
    onLogout: () -> Unit,
    onAddKnowledge: (String, String, String, String) -> Unit,
    onDeleteKnowledge: (KnowledgeEntity) -> Unit,
    onPublishVideo: (String, String, String, Int) -> Unit,
    onStartLive: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(OwnerSubTab.KNOWLEDGE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isAuthenticated) {
            // PIN Authentication Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(BluePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Owner / Faculty Portal",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Enter Owner PIN to update AI Teacher knowledge base, upload videos & manage live classes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = onPinChange,
                            label = { Text("Owner PIN") },
                            placeholder = { Text("Enter 4-digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError != null,
                            supportingText = {
                                if (pinError != null) {
                                    Text(text = pinError, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text(text = "Default PIN: 1234", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_pin_field")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onVerifyPin,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("owner_login_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Owner Mode", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        } else {
            // Authenticated Owner Workspace
            // Sub-nav header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Owner Workspace (SP Sir)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        FilledTonalButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("owner_logout_button")
                        ) {
                            Text("Lock", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Tab Selector
                    TabRow(
                        selectedTabIndex = activeSubTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        OwnerSubTab.values().forEach { tab ->
                            Tab(
                                selected = activeSubTab == tab,
                                onClick = { activeSubTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (activeSubTab == tab) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            when (activeSubTab) {
                OwnerSubTab.KNOWLEDGE -> {
                    OwnerKnowledgeView(
                        allKnowledge = allKnowledge,
                        onAddKnowledge = onAddKnowledge,
                        onDeleteKnowledge = onDeleteKnowledge
                    )
                }
                OwnerSubTab.PUBLISH_VIDEO -> {
                    OwnerPublishVideoView(onPublishVideo = onPublishVideo)
                }
                OwnerSubTab.START_LIVE -> {
                    OwnerStartLiveView(onStartLive = onStartLive)
                }
            }
        }
    }
}

@Composable
fun OwnerKnowledgeView(
    allKnowledge: List<KnowledgeEntity>,
    onAddKnowledge: (String, String, String, String) -> Unit,
    onDeleteKnowledge: (KnowledgeEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var content by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var isExpandedForm by remember { mutableStateOf(false) }

    val subjects = listOf("Physics", "Chemistry", "Biology", "Mathematics", "GK", "Railway", "SSC", "BPSC", "Bihar Police", "General")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Knowledge Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PurpleAi,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Teach New Data to SPA AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { isExpandedForm = !isExpandedForm }) {
                            Icon(
                                imageVector = if (isExpandedForm) Icons.Default.Delete else Icons.Default.Add,
                                contentDescription = "Toggle Form",
                                tint = BluePrimary
                            )
                        }
                    }

                    if (isExpandedForm) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Topic Title (उदा: लेंस सूत्र या बिहार का इतिहास)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subject Dropdown / Row
                        Text(
                            text = "Subject Category:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subjects.take(4).forEach { subj ->
                                FilterChip(
                                    selected = subject == subj,
                                    onClick = { subject = subj },
                                    label = { Text(subj, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Detailed Explanation / Notes / Q&A") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 6
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = keywords,
                            onValueChange = { keywords = it },
                            label = { Text("Keywords / Search Tags (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    onAddKnowledge(title, subject, content, keywords)
                                    title = ""
                                    content = ""
                                    keywords = ""
                                    isExpandedForm = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAi)
                        ) {
                            Icon(imageVector = Icons.Default.PostAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save into AI Knowledge Base", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Header for existing items
        item {
            Text(
                text = "Preserved Knowledge Base (${allKnowledge.size} items)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(allKnowledge, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Subject: ${item.subject} • ${item.addedBy}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = BluePrimary
                            )
                        }

                        IconButton(
                            onClick = { onDeleteKnowledge(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tags: ${item.keywords}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerPublishVideoView(
    onPublishVideo: (String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }

    val subjects = listOf("Physics", "Mathematics", "Biology", "Chemistry", "BPSC & Bihar Police", "Railway & SSC")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Publish New Video Lecture",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Video Title") },
            placeholder = { Text("e.g. Class 10 - Electricity & Ohm's Law") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Video Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Text(
            text = "Subject Category:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subjects.take(3).forEach { subj ->
                FilterChip(
                    selected = subject == subj,
                    onClick = { subject = subj },
                    label = { Text(subj, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration in Minutes") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val dur = duration.toIntOrNull() ?: 30
                    onPublishVideo(title, description, subject, dur)
                    title = ""
                    description = ""
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedLive)
        ) {
            Icon(imageVector = Icons.Default.VideoCall, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publish to Student Feed", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun OwnerStartLiveView(
    onStartLive: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Launch Live Classroom Broadcast",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Start a real-time WebRTC / Agora live class stream with student live chat & instant AI assistant.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Live Session Title") },
            placeholder = { Text("e.g. Mathematics Live Doubt & Formula Marathon") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject (e.g. Mathematics, BPSC, Physics)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onStartLive(title, subject)
                    title = ""
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedLive)
        ) {
            Icon(imageVector = Icons.Default.LiveTv, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Go Live Now", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}
