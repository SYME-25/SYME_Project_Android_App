package com.syme.ui.screen.bot.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.syme.R

@Composable
fun ChatInputBar(
    inputText: String,
    isLoading: Boolean,
    isRecording: Boolean,
    voiceAmplitude: Float,           // ← NEW: 0f–1f, driven by onRmsChanged
    showAttachOptions: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleRecording: () -> Unit,
    onToggleAttach: () -> Unit,
    onPickPdf: () -> Unit,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape          = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color          = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
        )
    ) {
        Column {
            // ── Attach chips ──────────────────────────────────────────────────
            AnimatedVisibility(visible = showAttachOptions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AttachChip(icon = Icons.Default.PictureAsPdf, label = stringResource(R.string.attach_pdf),      onClick = onPickPdf)
                    AttachChip(icon = Icons.Default.Image,         label = stringResource(R.string.attach_image),    onClick = onPickImage)
                    AttachChip(icon = Icons.Default.Description,   label = stringResource(R.string.attach_document), onClick = onPickDocument)
                }
            }

            // ── Input row ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onToggleAttach, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = if (showAttachOptions) Icons.Default.Close else Icons.Default.AttachFile,
                        contentDescription = stringResource(R.string.cd_attach),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedContent(
                    targetState = isRecording,
                    modifier    = Modifier.weight(1f),
                    label       = "input_recording_switch"
                ) { recording ->
                    if (recording) {
                        // ── Live waveform while recording ─────────────────────
                        Surface(
                            modifier       = Modifier.fillMaxWidth(),
                            shape          = RoundedCornerShape(28.dp),
                            tonalElevation = 1.dp,
                            color          = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text  = stringResource(R.string.listening),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Pass real amplitude — bars now respond to your voice
                                VoiceWaveIndicator(
                                    modifier  = Modifier.weight(1f),
                                    color     = MaterialTheme.colorScheme.primary,
                                    amplitude = voiceAmplitude   // ← KEY FIX
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value          = inputText,
                            onValueChange  = onInputChange,
                            modifier       = Modifier.fillMaxWidth(),
                            placeholder    = { Text(stringResource(R.string.input_placeholder)) },
                            shape          = RoundedCornerShape(28.dp),
                            maxLines       = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() }),
                            colors         = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                AnimatedContent(targetState = inputText.isBlank(), label = "send_voice") { showVoice ->
                    if (showVoice) {
                        // Pulse only when recording, driven by amplitude so it breathes naturally
                        val pulseScale by animateFloatAsState(
                            targetValue = if (isRecording) 1f + voiceAmplitude * 0.25f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "voice_pulse"
                        )
                        FilledIconButton(
                            onClick  = onToggleRecording,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(pulseScale),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isRecording)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector        = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = stringResource(R.string.cd_voice),
                                tint = if (isRecording)
                                    MaterialTheme.colorScheme.onError
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    } else {
                        FilledIconButton(
                            onClick  = onSend,
                            modifier = Modifier.size(48.dp),
                            enabled  = !isLoading
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.cd_send))
                        }
                    }
                }
            }
        }
    }
}
