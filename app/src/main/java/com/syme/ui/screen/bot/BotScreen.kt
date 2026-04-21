package com.syme.ui.screen.bot

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.syme.ui.screen.bot.components.*
import com.syme.ui.viewmodel.BotViewModel

@Composable
fun BotScreen(
    viewModel: BotViewModel,
    contentPadding: PaddingValues,
) {
    val uiState        by viewModel.uiState.collectAsState()
    val messages       = uiState.messages
    val listState      = rememberLazyListState()
    val focusManager   = LocalFocusManager.current
    val context        = LocalContext.current
    var voiceAmplitude by remember { mutableFloatStateOf(0f) }
    val voiceHelper    = remember { VoiceRecognitionHelper(context) }

    DisposableEffect(Unit) {
        onDispose { voiceHelper.destroy() }
    }

    // ── Permissions ──────────────────────────────────────────────────────────
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleFilePicked(it) }
    }

    // ── Auto scroll ──────────────────────────────────────────────────────────
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) focusManager.clearFocus()
    }

    // ── ROOT ─────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {

        // ── CHAT ─────────────────────────────────────────────────────────────
        if (messages.isEmpty()) {
            // WelcomeScreen reçoit toute la taille disponible MOINS l'espace
            // réservé en bas pour la barre de saisie, via un padding bottom.
            Box(
                modifier        = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                WelcomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp), // ← évite que le contenu passe sous le ChatInputBar
                    onSuggest = { viewModel.sendMessage(it) }
                )
            }
        } else {
            LazyColumn(
                state   = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start  = 12.dp,
                    end    = 12.dp,
                    bottom = 110.dp // espace pour la barre de saisie
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(contentPadding.calculateBottomPadding() + 112.dp)
                    )
                }
            }
        }

        // ── INPUT BAR FIXE EN BAS ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            ChatInputBar(
                inputText         = uiState.inputText,
                isLoading         = uiState.isLoading,
                isRecording       = uiState.isRecording,
                voiceAmplitude    = voiceAmplitude,
                showAttachOptions = uiState.showAttachOptions,
                onInputChange     = { viewModel.onInputChange(it) },
                onSend            = { viewModel.sendMessage() },
                onToggleAttach    = { viewModel.onToggleAttach() },
                onPickPdf = {
                    filePickerLauncher.launch("application/pdf")
                    viewModel.onCloseAttach()
                },
                onPickImage = {
                    filePickerLauncher.launch("image/*")
                    viewModel.onCloseAttach()
                },
                onPickDocument = {
                    filePickerLauncher.launch("*/*")
                    viewModel.onCloseAttach()
                },
                onToggleRecording = {
                    if (uiState.isRecording) {
                        voiceHelper.stopListening()
                        voiceAmplitude = 0f
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }

        // ── DRAWER ────────────────────────────────────────────────────────────
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
                        conversations  = uiState.conversations,
                        currentConvId  = uiState.currentConvId,
                        onSelect       = { viewModel.selectConversation(it) },
                        onDelete       = { viewModel.deleteConversation(it) },
                        onNewConv      = { viewModel.newConversation() }
                    )
                }
            }
        }
    }
}