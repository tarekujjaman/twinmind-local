package com.twinmindlocal.data.db

import androidx.room.*
import com.twinmindlocal.data.model.Session
import com.twinmindlocal.data.model.Transcript
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    // Sessions
    @Insert
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): Session?

    @Query("SELECT * FROM sessions WHERE title LIKE '%' || :query || '%' ORDER BY startedAt DESC")
    fun searchSessions(query: String): Flow<List<Session>>

    @Delete
    suspend fun deleteSession(session: Session)

    // Transcripts
    @Insert
    suspend fun insertTranscript(transcript: Transcript): Long

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    fun getTranscriptsForSession(sessionId: Long): Flow<List<Transcript>>

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun getTranscriptsForSessionOnce(sessionId: Long): List<Transcript>

    @Query("DELETE FROM transcripts WHERE sessionId = :sessionId")
    suspend fun deleteTranscriptsForSession(sessionId: Long)
}
