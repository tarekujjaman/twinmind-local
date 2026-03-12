package com.twinmindlocal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twinmindlocal.data.model.Transcript
import com.twinmindlocal.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val session by viewModel.getSession(sessionId).collectAsState(initial = null)
    val transcripts by viewModel.getTranscripts(sessionId).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Transcript", "Summary")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.title ?: "Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TranscriptTab(transcripts = transcripts)
                1 -> SummaryTab(summary = session?.summary)
            }
        }
    }
}

@Composable
private fun TranscriptTab(transcripts: List<Transcript>) {
    if (transcripts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transcript yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(transcripts, key = { it.id }) { transcript ->
            TranscriptEntry(transcript = transcript)
        }
    }
}

@Composable
private fun TranscriptEntry(transcript: Transcript) {
    Column {
        Text(
            formatTime(transcript.timestampMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            transcript.text,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
    }
}

@Composable
private fun SummaryTab(summary: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = if (summary == null) Alignment.Center else Alignment.TopStart
    ) {
        if (summary == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No summary available yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "AI summarization coming in Phase 2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    summary,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String =
    SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(ms))
