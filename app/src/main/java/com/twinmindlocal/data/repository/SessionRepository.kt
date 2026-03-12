package com.twinmindlocal.data.repository

import com.twinmindlocal.data.db.SessionDao
import com.twinmindlocal.data.model.Session
import com.twinmindlocal.data.model.Transcript
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionRepository(private val dao: SessionDao) {

    fun getAllSessions(): Flow<List<Session>> = dao.getAllSessions()

    fun searchSessions(query: String): Flow<List<Session>> = dao.searchSessions(query)

    fun getTranscriptsForSession(sessionId: Long): Flow<List<Transcript>> =
        dao.getTranscriptsForSession(sessionId)

    suspend fun getSessionById(id: Long): Session? = dao.getSessionById(id)

    suspend fun startSession(): Long {
        val title = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
        return dao.insertSession(Session(title = title))
    }

    suspend fun endSession(sessionId: Long) {
        val session = dao.getSessionById(sessionId) ?: return
        dao.updateSession(session.copy(endedAt = System.currentTimeMillis()))
    }

    suspend fun addTranscript(sessionId: Long, text: String) {
        dao.insertTranscript(Transcript(sessionId = sessionId, text = text))
    }

    suspend fun saveSummary(sessionId: Long, summary: String) {
        val session = dao.getSessionById(sessionId) ?: return
        dao.updateSession(session.copy(summary = summary))
    }

    suspend fun deleteSession(session: Session) {
        dao.deleteSession(session)
    }

    suspend fun getFullTranscriptText(sessionId: Long): String {
        return dao.getTranscriptsForSessionOnce(sessionId).joinToString(" ") { it.text }
    }
}
