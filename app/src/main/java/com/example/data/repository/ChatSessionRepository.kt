package com.example.data.repository

import com.example.data.local.AiChatMessageEntity
import com.example.data.local.ChatSessionDao
import com.example.data.local.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

class ChatSessionRepository(private val chatSessionDao: ChatSessionDao) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatSessionDao.getAllSessions()

    fun getSessionById(sessionId: String): Flow<ChatSessionEntity?> {
        return chatSessionDao.getSessionById(sessionId)
    }

    fun getMessagesForSession(sessionId: String): Flow<List<AiChatMessageEntity>> {
        return chatSessionDao.getMessagesForSession(sessionId)
    }

    suspend fun getMessagesListForSession(sessionId: String): List<AiChatMessageEntity> {
        return chatSessionDao.getMessagesListForSession(sessionId)
    }

    suspend fun insertSession(session: ChatSessionEntity) {
        chatSessionDao.insertSession(session)
    }

    suspend fun insertSessions(sessions: List<ChatSessionEntity>) {
        chatSessionDao.insertSessions(sessions)
    }

    suspend fun updateSession(session: ChatSessionEntity) {
        chatSessionDao.updateSession(session)
    }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
        chatSessionDao.updateSessionTitle(sessionId, newTitle, System.currentTimeMillis())
    }

    suspend fun updateSessionMetadata(sessionId: String, count: Int, snippet: String) {
        chatSessionDao.updateSessionMetadata(sessionId, count, snippet, System.currentTimeMillis())
    }

    suspend fun saveMessage(message: AiChatMessageEntity) {
        chatSessionDao.insertMessage(message)
    }

    suspend fun saveMessages(messages: List<AiChatMessageEntity>) {
        chatSessionDao.insertMessages(messages)
    }

    suspend fun deleteSession(sessionId: String) {
        chatSessionDao.deleteMessagesForSession(sessionId)
        chatSessionDao.deleteSession(sessionId)
    }

    suspend fun deleteAllSessions() {
        chatSessionDao.deleteAllMessages()
        chatSessionDao.deleteAllSessions()
    }
}
