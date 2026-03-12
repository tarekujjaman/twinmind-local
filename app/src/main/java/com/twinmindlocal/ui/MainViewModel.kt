package com.twinmindlocal.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.twinmindlocal.TwinMindApp
import com.twinmindlocal.data.model.Session
import com.twinmindlocal.data.model.Transcript
import com.twinmindlocal.data.repository.SessionRepository
import com.twinmindlocal.service.RecordingService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: SessionRepository = (app as TwinMindApp).repository

    val isRecording: StateFlow<Boolean> = RecordingService.isRecording
    val liveText: StateFlow<String> = RecordingService.liveText
    val currentSessionId: StateFlow<Long?> = RecordingService.sessionId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val sessions: StateFlow<List<Session>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllSessions()
            else repository.searchSessions(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun startRecording() {
        val ctx = getApplication<Application>()
        Intent(ctx, RecordingService::class.java).also {
            it.action = RecordingService.ACTION_START
            ctx.startForegroundService(it)
        }
    }

    fun stopRecording() {
        val ctx = getApplication<Application>()
        Intent(ctx, RecordingService::class.java).also {
            it.action = RecordingService.ACTION_STOP
            ctx.startService(it)
        }
    }

    fun getTranscripts(sessionId: Long): Flow<List<Transcript>> =
        repository.getTranscriptsForSession(sessionId)

    fun deleteSession(session: Session) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    fun getSession(sessionId: Long): Flow<Session?> = flow {
        emit(repository.getSessionById(sessionId))
    }
}
