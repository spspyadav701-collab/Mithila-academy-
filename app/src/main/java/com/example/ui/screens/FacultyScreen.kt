package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SubscriptionEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.*

@Composable
fun FacultyScreen(
    teachers: List<UserEntity>,
    subscriptions: List<SubscriptionEntity>,
    onToggleSubscribe: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Faculty Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Mithila Academy Faculty & Teachers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Subscribe to get instant alerts on live classes & new lectures",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(teachers, key = { it.userId }) { teacher ->
                val isSubscribed = subscriptions.any { it.teacherId == teacher.userId }
                FacultyCard(
                    teacher = teacher,
                    isSubscribed = isSubscribed,
                    onToggleSubscribe = { onToggleSubscribe(teacher.userId, teacher.name) }
                )
            }
        }
    }
}

@Composable
fun FacultyCard(
    teacher: UserEntity,
    isSubscribed: Boolean,
    onToggleSubscribe: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("faculty_card_${teacher.userId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(BluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teacher.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = teacher.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (teacher.userId.contains("sp")) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Founder",
                                tint = AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = teacher.subjectSpecialty,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${formatNumber(teacher.subscriberCount)} subscribers",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = teacher.bio,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggleSubscribe,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else RedLive,
                    contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("subscribe_button_${teacher.userId}")
            ) {
                if (isSubscribed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Subscribed",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SUBSCRIBED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                } else {
                    Text("SUBSCRIBE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1000000 -> String.format("%.1fM", number / 1000000.0)
        number >= 1000 -> String.format("%.1fK", number / 1000.0)
        else -> number.toString()
    }
}
