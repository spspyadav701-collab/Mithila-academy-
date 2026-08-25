package com.example.data.model

import com.squareup.moshi.JsonClass

/**
 * 1. users collection model
 */
@JsonClass(generateAdapter = true)
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val role: String, // "student" or "teacher" / "admin"
    val profilePic: String = "",
    val createdAt: String = "2026-08-22T11:26:00Z",
    val subscriberCount: Int = 0,
    val bio: String = "",
    val subjectSpecialty: String = ""
)

/**
 * Role-Based Access Control (RBAC) System
 */
enum class UserRole(
    val roleId: String,
    val displayName: String,
    val badgeText: String,
    val description: String
) {
    STUDENT(
        roleId = "student",
        displayName = "Student",
        badgeText = "Student Mode",
        description = "Learner role: Access courses, watch video lectures, join live classes, take tests, and ask AI doubts."
    ),
    ADMIN(
        roleId = "admin",
        displayName = "Administrator (SP Sir)",
        badgeText = "Admin Pro",
        description = "Administrative role: Upload videos, manage content, schedule live classes, configure AI settings & academy branding."
    );

    val permissions: RolePermissions
        get() = when (this) {
            ADMIN -> RolePermissions(
                canUploadVideos = true,
                canManageContent = true,
                canScheduleLiveClasses = true,
                canConfigureAiSettings = true,
                canAccessAdminPanel = true,
                canCustomizeBranding = true,
                canManageKnowledgeBase = true,
                canDeleteContent = true
            )
            STUDENT -> RolePermissions(
                canUploadVideos = false,
                canManageContent = false,
                canScheduleLiveClasses = false,
                canConfigureAiSettings = false,
                canAccessAdminPanel = false,
                canCustomizeBranding = false,
                canManageKnowledgeBase = false,
                canDeleteContent = false
            )
        }
}

data class RolePermissions(
    val canUploadVideos: Boolean,
    val canManageContent: Boolean,
    val canScheduleLiveClasses: Boolean,
    val canConfigureAiSettings: Boolean,
    val canAccessAdminPanel: Boolean,
    val canCustomizeBranding: Boolean,
    val canManageKnowledgeBase: Boolean,
    val canDeleteContent: Boolean
)

/**
 * 2. videos collection model
 */
@JsonClass(generateAdapter = true)
data class Video(
    val videoId: String,
    val teacherId: String,
    val teacherName: String = "",
    val teacherProfilePic: String = "",
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val views: Int = 0,
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: String = "2026-08-22T10:00:00Z",
    val subject: String = "General",
    val durationMinutes: Int = 24,
    val resolution: String = "1080p HD"
)

/**
 * 3. subscriptions collection model
 */
@JsonClass(generateAdapter = true)
data class Subscription(
    val subscriptionId: String,
    val studentId: String,
    val teacherId: String,
    val subscribedAt: String = "2026-08-22T11:20:00Z"
)

/**
 * 4. live_streams collection model
 */
@JsonClass(generateAdapter = true)
data class LiveStream(
    val streamId: String,
    val teacherId: String,
    val teacherName: String = "",
    val teacherProfilePic: String = "",
    val title: String,
    val streamKey: String, // WebRTC / Agora streaming key
    val status: String, // "active" or "ended"
    val startedAt: String = "2026-08-22T11:00:00Z",
    val viewerCount: Int = 120,
    val subject: String = "General"
)

/**
 * 5. chats collection model (Live Class Chat & AI Teacher Chat)
 */
@JsonClass(generateAdapter = true)
data class ChatMessage(
    val messageId: String,
    val streamId: String, // streamId or videoId or ai_chat_id
    val senderId: String,
    val senderName: String,
    val senderRole: String = "student", // "student", "teacher", "ai"
    val messageText: String,
    val timestamp: String = "2026-08-22T11:15:30Z",
    val isAiResponse: Boolean = false
)
