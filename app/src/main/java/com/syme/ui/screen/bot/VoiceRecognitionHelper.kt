package com.syme.ui.screen.bot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Robust continuous speech recognizer.
 *
 * Android's SpeechRecognizer stops after ~5 s of silence or after the first
 * utterance. This class works around that by restarting the recognizer
 * automatically whenever it stops — as long as [stopListening] / [destroy]
 * have not been called — giving the user the feeling of uninterrupted
 * dictation.
 *
 * Partial results are enabled so the waveform stays alive even during pauses.
 *
 * @param onResult      Called with each recognised utterance (may be called many times).
 * @param onRmsChanged  Called on every dB update — wire to VoiceWaveIndicator.
 * @param onDone        Called ONCE when the user explicitly stops (via [stopListening]).
 * @param onError       Informational; does NOT stop the loop.
 */
class VoiceRecognitionHelper(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())

    // Callbacks stored so the restart loop can reuse them
    private var _onResult: ((String) -> Unit)? = null
    private var _onDone: (() -> Unit)? = null
    private var _onError: ((String) -> Unit)? = null
    private var _onRmsChanged: ((Float) -> Unit)? = null
    private var _locale: String = Locale.getDefault().toLanguageTag()

    /** True while the user intends to be recording (survives restarts). */
    @Volatile private var active = false

    // ── Public API ────────────────────────────────────────────────────────────

    fun startListening(
        locale: String = Locale.getDefault().toLanguageTag(),
        onResult: (String) -> Unit,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {},
        onRmsChanged: (Float) -> Unit = {}
    ) {
        _locale       = locale
        _onResult     = onResult
        _onDone       = onDone
        _onError      = onError
        _onRmsChanged = onRmsChanged
        active        = true
        startSession()
    }

    /** User tapped Stop — end the loop and fire onDone once. */
    fun stopListening() {
        active = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        _onDone?.invoke()
    }

    fun destroy() {
        active = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        recognizer = null
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * Creates a fresh SpeechRecognizer and starts one recognition session.
     * If [active] is still true when the session ends, we schedule another.
     */
    private fun startSession() {
        if (!active) return

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _onError?.invoke("Speech recognition not available on this device.")
            active = false
            _onDone?.invoke()
            return
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onRmsChanged(rmsdB: Float) {
                _onRmsChanged?.invoke(rmsdB)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Keep the waveform alive during pauses — we don't use partials
                // for text, but receiving them proves audio is still flowing.
            }

            override fun onResults(results: Bundle?) {
                val best = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!best.isNullOrBlank()) {
                    _onResult?.invoke(best)
                }
                // Restart immediately so the user can keep talking
                restartIfActive()
            }

            override fun onEndOfSpeech() {
                // Android calls this before onResults; do nothing here —
                // the restart is handled in onResults / onError.
            }

            override fun onError(error: Int) {
                when (error) {
                    // Silence / no-match are expected during pauses → just restart
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        restartIfActive()
                    }
                    // Recogniser was busy (previous session not fully torn down) → retry after delay
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        handler.postDelayed({ restartIfActive() }, 300)
                    }
                    // Fatal errors → still try to continue
                    else -> {
                        _onError?.invoke(errorMessage(error))
                        restartIfActive()
                    }
                }
            }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,        _locale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)   // keep audio flowing
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,     1)
            // Ask for as much speech time as possible before the engine cuts off
            putExtra("android.speech.extra.DICTATION_MODE", true)
        }
        recognizer?.startListening(intent)
    }

    /**
     * Schedules a new session with a tiny gap (50 ms) to let the previous
     * recognizer release its audio focus cleanly.
     */
    private fun restartIfActive() {
        if (!active) return
        recognizer?.destroy()
        recognizer = null
        handler.postDelayed({ startSession() }, 50)
    }

    private fun errorMessage(error: Int) = when (error) {
        SpeechRecognizer.ERROR_AUDIO                   -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT                  -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Missing RECORD_AUDIO permission"
        SpeechRecognizer.ERROR_NETWORK                 -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT         -> "Network timeout"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY         -> "Recogniser busy"
        SpeechRecognizer.ERROR_SERVER                  -> "Server error"
        else                                           -> "Unknown error ($error)"
    }
}