package com.syme.ui.screen.bot

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syme.BuildConfig
import com.syme.R
import com.syme.ui.screen.bot.components.BotTopBar
import com.syme.ui.screen.bot.components.ChatInputBar
import com.syme.ui.screen.bot.components.ConversationHistory
import com.syme.ui.screen.bot.components.MessageBubble
import com.syme.ui.screen.bot.components.WelcomeScreen
import com.syme.ui.viewmodel.BotViewModel

@Composable
fun BotScreen(viewModel: BotViewModel) {
    val uiState      by viewModel.uiState.collectAsState()
    val messages      = uiState.messages
    val focusManager  = LocalFocusManager.current
    val listState     = rememberLazyListState()
    val context       = LocalContext.current

    // ── String resources → ViewModel ─────────────────────────────────────────
    viewModel.defaultConvTitle  = stringResource(R.string.conversation_default_title)
    viewModel.fileAnalysisTitle = stringResource(R.string.conversation_file_analysis_title)
    viewModel.fileJoinedPrefix  = stringResource(R.string.message_file_joined_prefix)
    viewModel.fileAnalysisNote  = stringResource(R.string.message_file_analysis_note)
    viewModel.fileReceivedFmt   = stringResource(R.string.message_file_received)
    viewModel.errorPrefix       = stringResource(R.string.error_prefix)

    // ── RMS amplitude (0f–1f) for the live waveform ───────────────────────────
    var voiceAmplitude by remember { mutableFloatStateOf(0f) }

    // ── Voice helper ──────────────────────────────────────────────────────────
    val voiceHelper = remember { VoiceRecognitionHelper(context) }
    DisposableEffect(Unit) { onDispose { voiceHelper.destroy() } }

    /**
     * Starts continuous dictation.
     *
     * The recognizer restarts automatically after each silence / utterance.
     * Each recognised segment is APPENDED to the current input text so the
     * user builds up a full message across multiple breath groups.
     *
     * When the user taps Stop, [stopListening] fires [onDone] once →
     * [viewModel.stopRecordingAndSend] sends whatever was accumulated.
     */
    fun launchVoice() {
        viewModel.startRecording()
        voiceHelper.startListening(
            onRmsChanged = { rms ->
                // Normalise [-2, 10] → [0, 1]
                voiceAmplitude = ((rms + 2f) / 12f).coerceIn(0f, 1f)
            },
            onResult = { segment ->
                // Append each dictated segment (continuous mode produces many)
                viewModel.appendTranscribedSegment(segment)
            },
            onDone = {
                // Called once when user taps Stop
                voiceAmplitude = 0f
                viewModel.stopRecordingAndSend()
            },
            onError = { /* optional snackbar */ }
        )
    }

    // ── RECORD_AUDIO permission ───────────────────────────────────────────────
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchVoice() }

    // ── File picker ───────────────────────────────────────────────────────────
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.handleFilePicked(it) } }

    // ── Auto-scroll ───────────────────────────────────────────────────────────
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) focusManager.clearFocus()
    }

    Log.d("API_KEY", BuildConfig.MISTRAL_API_KEY)

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                BotTopBar(
                    onOpenHistory     = { viewModel.onToggleDrawer() },
                    onNewConversation = { viewModel.newConversation() }
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText         = uiState.inputText,
                    isLoading         = uiState.isLoading,
                    isRecording       = uiState.isRecording,
                    voiceAmplitude    = voiceAmplitude,
                    showAttachOptions = uiState.showAttachOptions,
                    onInputChange     = { viewModel.onInputChange(it) },
                    onSend            = { viewModel.sendMessage() },
                    onToggleAttach    = { viewModel.onToggleAttach() },
                    onPickPdf         = { filePickerLauncher.launch("application/pdf"); viewModel.onCloseAttach() },
                    onPickImage       = { filePickerLauncher.launch("image/*");         viewModel.onCloseAttach() },
                    onPickDocument    = { filePickerLauncher.launch("*/*");             viewModel.onCloseAttach() },
                    onToggleRecording = {
                        if (uiState.isRecording) {
                            // User tapped Stop → stopListening fires onDone → stopRecordingAndSend
                            voiceHelper.stopListening()
                            voiceAmplitude = 0f
                            // stopListening already calls onDone; no need to call stopRecording here
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (messages.isEmpty()) {
                WelcomeScreen(
                    modifier  = Modifier.padding(paddingValues),
                    onSuggest = { viewModel.sendMessage(it) }
                )
            } else {
                LazyColumn(
                    state               = listState,
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }

        // ── Conversation drawer ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.showDrawer,
            enter   = fadeIn() + slideInHorizontally(),
            exit    = fadeOut() + slideOutHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.onCloseDrawer() }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .clickable(enabled = false) {},
                    shape          = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
                    tonalElevation = 8.dp
                ) {
                    ConversationHistory(
                        conversations = uiState.conversations,
                        currentConvId = uiState.currentConvId,
                        onSelect      = { viewModel.selectConversation(it) },
                        onDelete      = { viewModel.deleteConversation(it) },
                        onNewConv     = { viewModel.newConversation() }
                    )
                }
            }
        }
    }
}