package com.syme.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.syme.data.repository.ApplianceRepository
import com.syme.data.repository.BillRepository
import com.syme.data.repository.CircuitRepository
import com.syme.data.repository.ConsumptionRepository
import com.syme.data.repository.InstallationRepository
import com.syme.data.repository.MeterRepository
import com.syme.data.repository.UserRepository
import com.syme.domain.model.ChatMessage
import com.syme.domain.model.Conversation
import com.syme.domain.model.UserContext
import com.syme.domain.state.BotUiState
import com.syme.ui.screen.bot.network.callMistralAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BotViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val installationRepository: InstallationRepository,
    private val applianceRepository: ApplianceRepository,
    private val billRepository: BillRepository,
    private val consumptionRepository: ConsumptionRepository,
    private val circuitRepository: CircuitRepository,
    private val meterRepository: MeterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BotUiState())
    val uiState: StateFlow<BotUiState> = _uiState.asStateFlow()

    // ── String placeholders (set from Composable) ─────────────────────────────
    var defaultConvTitle: String = "Nouvelle conversation"
    var fileAnalysisTitle: String = "Analyse de fichier"
    var fileJoinedPrefix: String = "Fichier joint :"
    var fileAnalysisNote: String = "Analyse en cours…"
    var fileReceivedFmt: String = "Fichier reçu : {fileName}"
    var errorPrefix: String = "Erreur :"

    init { loadUserContext() }

    // ── Load user context ─────────────────────────────────────────────────────
    private fun loadUserContext() {
        val ownerId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val user = userRepository.getOnce(ownerId)
            val installations = installationRepository.getAllOnce(ownerId)
            val installation = installations.firstOrNull()
            if (installation == null) {
                _uiState.update { it.copy(userContext = UserContext(userName = user?.firstName)) }
                return@launch
            }
            val installationId = installation.installationId
            val appliances   = applianceRepository.getAllOnce(ownerId, installationId)
            val bills        = billRepository.getBillsOnce(ownerId, installationId)
            val consumptions = consumptionRepository.getAllOnce(ownerId, installationId)
            val circuits     = circuitRepository.getAllOnce(ownerId, installationId)
            val meters       = meterRepository.getAggregatedMeasurementsOnce(ownerId, installationId)
            val lastBill         = bills.maxByOrNull { it.periodEnd }
            val lastConsumption  = consumptions.maxByOrNull { it.periodEnd }
            val applianceSummary = appliances
                .sortedByDescending { it.powerWatt }.take(5)
                .joinToString(", ") { "${it.name} ${it.powerWatt}W" }.ifBlank { null }
            _uiState.update { state ->
                state.copy(
                    userContext = UserContext(
                        userName             = user?.firstName,
                        installationName     = installation.name,
                        installationType     = installation.type.name,
                        totalEnergyWh        = installation.energyWh,
                        applianceCount       = appliances.size.takeIf { it > 0 },
                        applianceSummary     = applianceSummary,
                        lastBillAmountXaf    = lastBill?.amountToPay,
                        lastConsumptionKwh   = lastConsumption?.totalEnergy_kWhConsummed,
                        tariffXafPerKwh      = null,
                        circuitCount         = circuits.size.takeIf { it > 0 },
                        meterCount           = meters.size.takeIf { it > 0 }
                    )
                )
            }
        }
    }

    // ── UI events ─────────────────────────────────────────────────────────────

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text) }

    fun onToggleDrawer()  = _uiState.update { it.copy(showDrawer = !it.showDrawer) }
    fun onCloseDrawer()   = _uiState.update { it.copy(showDrawer = false) }
    fun onToggleAttach()  = _uiState.update { it.copy(showAttachOptions = !it.showAttachOptions) }
    fun onCloseAttach()   = _uiState.update { it.copy(showAttachOptions = false) }

    // ── Recording ─────────────────────────────────────────────────────────────

    fun startRecording() {
        // Clear the text field so new dictation starts fresh
        _uiState.update { it.copy(isRecording = true, inputText = "") }
    }

    /**
     * Appended by the continuous recognizer for each recognised utterance.
     * Segments are joined with a space so the user sees their full message build up.
     */
    fun appendTranscribedSegment(segment: String) {
        _uiState.update { state ->
            val current = state.inputText.trimEnd()
            val joined  = if (current.isBlank()) segment else "$current $segment"
            state.copy(inputText = joined)
        }
    }

    fun stopRecording(transcribedText: String? = null) {
        _uiState.update { state ->
            state.copy(
                isRecording = false,
                inputText   = transcribedText ?: state.inputText
            )
        }
    }

    /**
     * Called once when the user taps Stop.
     * Clears the recording flag and sends whatever text was accumulated.
     */
    fun stopRecordingAndSend() {
        val accumulated = _uiState.value.inputText.trim()
        _uiState.update { it.copy(isRecording = false, inputText = "") }
        if (accumulated.isNotBlank()) {
            sendMessage(accumulated)
        }
    }

    // Keep for backward-compat
    fun onToggleRecording() = _uiState.update { it.copy(isRecording = !it.isRecording) }

    // ── Conversations ─────────────────────────────────────────────────────────

    fun selectConversation(id: String) =
        _uiState.update { it.copy(currentConvId = id, showDrawer = false) }

    fun newConversation(): String {
        val conv = Conversation(title = defaultConvTitle)
        _uiState.update { state ->
            state.copy(
                conversations = state.conversations + conv,
                currentConvId = conv.id,
                showDrawer    = false
            )
        }
        return conv.id
    }

    fun deleteConversation(id: String) {
        _uiState.update { state ->
            val updated = state.conversations.filter { it.id != id }
            val newCurrentId = if (state.currentConvId == id) {
                updated.firstOrNull()?.id ?: run {
                    val fallback = Conversation(title = defaultConvTitle)
                    return@update state.copy(
                        conversations = listOf(fallback),
                        currentConvId = fallback.id
                    )
                }
            } else state.currentConvId
            state.copy(conversations = updated, currentConvId = newCurrentId)
        }
    }

    // ── Send message ──────────────────────────────────────────────────────────

    fun sendMessage(text: String = _uiState.value.inputText) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _uiState.value.isLoading) return
        val botMsgId = UUID.randomUUID().toString()
        val userMsg    = ChatMessage(content = trimmed, isUser = true)
        val loadingMsg = ChatMessage(id = botMsgId, content = "", isUser = false, isLoading = true)
        _uiState.update { state ->
            val convId = state.currentConvId
            state.copy(
                inputText = "",
                isLoading = true,
                conversations = state.conversations.map { conv ->
                    if (conv.id != convId) return@map conv
                    val title = if (conv.messages.isEmpty()) trimmed.take(40) else conv.title
                    conv.copy(messages = conv.messages + userMsg + loadingMsg, title = title)
                }
            )
        }
        viewModelScope.launch {
            val convId  = _uiState.value.currentConvId
            val history = _uiState.value.conversations
                .find { it.id == convId }?.messages?.filter { !it.isLoading } ?: emptyList()
            val reply = callMistralAPI(
                messages    = history,
                userContext = _uiState.value.userContext,
                onError     = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            conversations = state.conversations.map { conv ->
                                if (conv.id != convId) return@map conv
                                conv.copy(messages = conv.messages.map { m ->
                                    if (m.id == botMsgId)
                                        m.copy(content = "$errorPrefix $error", isLoading = false, isError = true)
                                    else m
                                })
                            }
                        )
                    }
                }
            )
            if (reply != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        conversations = state.conversations.map { conv ->
                            if (conv.id != convId) return@map conv
                            conv.copy(messages = conv.messages.map { m ->
                                if (m.id == botMsgId) m.copy(content = reply, isLoading = false) else m
                            })
                        }
                    )
                }
            }
        }
    }

    // ── File attachment ───────────────────────────────────────────────────────

    fun handleFilePicked(uri: Uri) {
        val fileName   = uri.lastPathSegment ?: "file"
        val botMsgId   = UUID.randomUUID().toString()
        val userMsg    = ChatMessage(
            content        = "$fileJoinedPrefix $fileName\n$fileAnalysisNote",
            isUser         = true,
            attachmentUri  = uri,
            attachmentName = fileName
        )
        val loadingMsg = ChatMessage(id = botMsgId, content = "", isUser = false, isLoading = true)
        _uiState.update { state ->
            val convId = state.currentConvId
            state.copy(
                isLoading = true,
                conversations = state.conversations.map { conv ->
                    if (conv.id != convId) return@map conv
                    val title = if (conv.messages.isEmpty()) fileAnalysisTitle else conv.title
                    conv.copy(messages = conv.messages + userMsg + loadingMsg, title = title)
                }
            )
        }
        viewModelScope.launch {
            val convId = _uiState.value.currentConvId
            delay(1500)
            val reply = fileReceivedFmt.replace("{fileName}", fileName)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    conversations = state.conversations.map { conv ->
                        if (conv.id != convId) return@map conv
                        conv.copy(messages = conv.messages.map { m ->
                            if (m.id == botMsgId) m.copy(content = reply, isLoading = false) else m
                        })
                    }
                )
            }
        }
    }
}
