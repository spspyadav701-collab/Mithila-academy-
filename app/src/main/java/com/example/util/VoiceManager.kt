package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

class VoiceManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var hasSpokenInitialGreeting = false
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var amplitudeJob: Job? = null
    private var listeningVisualizerJob: Job? = null
    private var lastRecognizedHypothesis: String = ""

    private val _currentLanguage = MutableStateFlow(AppLanguage.HINDI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _waveformBands = MutableStateFlow(List(16) { 0.1f })
    val waveformBands: StateFlow<List<Float>> = _waveformBands.asStateFlow()

    private val prefs = context.getSharedPreferences("ai_teacher_voice_prefs", Context.MODE_PRIVATE)

    private val _speechSpeed = MutableStateFlow(prefs.getFloat("key_speech_speed", 0.95f))
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()

    private val _speechPitch = MutableStateFlow(prefs.getFloat("key_speech_pitch", 1.0f))
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    init {
        mainHandler.post {
            initTts()
        }
    }

    private val defaultIdleWaveform = List(16) { 0.05f }

    private fun startAudioSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch(Dispatchers.Default) {
            var step = 0f
            while (isActive && _isSpeaking.value) {
                step += 0.25f
                val base = 0.45f + 0.35f * sin(step.toDouble()).toFloat()
                val jitter = Random.nextFloat() * 0.2f
                val amp = (base + jitter).coerceIn(0.15f, 1.0f)
                _audioAmplitude.value = amp

                _waveformBands.value = List(16) { i ->
                    val bandFreq = sin((step * 1.5f + i * 0.45f).toDouble()).toFloat()
                    ((bandFreq + 1f) * 0.5f * amp).coerceIn(0.08f, 1.0f)
                }
                delay(60)
            }
            _audioAmplitude.value = 0f
            _waveformBands.value = defaultIdleWaveform
        }
    }

    private fun stopAudioSimulation() {
        amplitudeJob?.cancel()
        _audioAmplitude.value = 0f
        _waveformBands.value = defaultIdleWaveform
    }

    private fun startListeningVisualizer() {
        listeningVisualizerJob?.cancel()
        listeningVisualizerJob = scope.launch(Dispatchers.Default) {
            var step = 0f
            while (isActive && _isListening.value) {
                step += 0.3f
                val base = 0.35f + 0.25f * sin(step.toDouble()).toFloat()
                val amp = (_audioAmplitude.value.coerceAtLeast(base)).coerceIn(0.2f, 1.0f)
                _waveformBands.value = List(16) { i ->
                    val bandFreq = sin((step * 1.8f + i * 0.5f).toDouble()).toFloat()
                    ((bandFreq + 1f) * 0.5f * amp).coerceIn(0.1f, 1.0f)
                }
                delay(60)
            }
        }
    }

    private fun stopListeningVisualizer() {
        listeningVisualizerJob?.cancel()
        if (!_isSpeaking.value) {
            _audioAmplitude.value = 0f
            _waveformBands.value = defaultIdleWaveform
        }
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true

                    val hindiLocale = Locale("hi", "IN")
                    val result = textToSpeech?.setLanguage(hindiLocale)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.setLanguage(Locale("hi"))
                    }

                    // Attempt to select a natural Hindi female voice if available
                    try {
                        val availableVoices: Set<Voice>? = textToSpeech?.voices
                        if (!availableVoices.isNullOrEmpty()) {
                            val hindiVoices = availableVoices.filter {
                                it.locale.language == "hi" || it.locale.toLanguageTag().startsWith("hi", ignoreCase = true)
                            }
                            val bestVoice = hindiVoices.firstOrNull { v ->
                                v.name.contains("female", ignoreCase = true) ||
                                        v.name.contains("f0", ignoreCase = true) ||
                                        v.name.contains("hi-in-x-hie", ignoreCase = true) ||
                                        v.name.contains("network", ignoreCase = true)
                            } ?: hindiVoices.firstOrNull()

                            if (bestVoice != null) {
                                textToSpeech?.voice = bestVoice
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("VoiceManager", "Voice auto-selection note: ${e.message}")
                    }

                    textToSpeech?.setSpeechRate(_speechSpeed.value)
                    textToSpeech?.setPitch(_speechPitch.value)
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            startAudioSimulation()
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            stopAudioSimulation()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            stopAudioSimulation()
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                            stopAudioSimulation()
                        }
                    })

                    // Automatically speak standard opening Hindi greeting when initialized
                    if (!hasSpokenInitialGreeting && _ttsEnabled.value) {
                        hasSpokenInitialGreeting = true
                        speak("नमस्ते! मैं मिथिला अकादमी द्वारा निर्मित किया गया एक AI टीचर हूँ और मुझे SP ने बनाया है। आपका क्या सवाल है? आप मुझे बताएं, मैं उसका उत्तर अभी देता हूँ।")
                    }
                } else {
                    Log.w("VoiceManager", "TTS initialization failed code: $status")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "TTS Exception: ${e.message}")
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("key_selected_language", language.code).apply()
        try {
            val result = textToSpeech?.setLanguage(language.ttsLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.getDefault())
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error updating TTS language: ${e.message}")
        }
    }

    fun setSpeechSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        _speechSpeed.value = clamped
        prefs.edit().putFloat("key_speech_speed", clamped).apply()
        try {
            textToSpeech?.setSpeechRate(clamped)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error setting speech rate: ${e.message}")
        }
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        _speechPitch.value = clamped
        prefs.edit().putFloat("key_speech_pitch", clamped).apply()
        try {
            textToSpeech?.setPitch(clamped)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error setting pitch: ${e.message}")
        }
    }

    fun resetVoiceSettings() {
        setSpeechSpeed(0.95f)
        setSpeechPitch(1.0f)
    }

    fun toggleTts() {
        _ttsEnabled.value = !_ttsEnabled.value
        if (!_ttsEnabled.value) {
            stopSpeaking()
        }
    }

    /**
     * Cleans text thoroughly before sending to TTS engine:
     * - Strips markdown (asterisks, hashtags, underscores, code tags)
     * - Removes emojis, strange symbols, and math notation markers that cause stutter
     * - Normalizes phonetics (SP -> एस पी, AI -> ए आई, etc.)
     */
    fun cleanTextForTts(text: String): String {
        return text
            // Remove code blocks and inline code
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("`[^`]*`"), " ")
            // Remove markdown links [Title](url) -> Title
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1")
            // Remove standalone URLs
            .replace(Regex("https?://\\S+"), " ")
            // Common math replacements for natural Hindi speech
            .replace("\\frac{", " ")
            .replace("\\sqrt{", " रूट ")
            .replace("}", " ")
            .replace("CO₂", " कार्बन डाइऑक्साइड ")
            .replace("H₂O", " जल ")
            .replace("O₂", " ऑक्सीजन ")
            .replace("SO₄", " सल्फेट ")
            .replace("FeSO₄", " फेरस सल्फेट ")
            .replace("CuSO₄", " कॉपर सल्फेट ")
            .replace("pH", " पीएच ")
            // Identity phonetic sanitization
            .replace(Regex("(?i)ESPA\\s*AI"), "AI टीचर")
            .replace(Regex("(?i)S-P-A-I"), "AI टीचर")
            .replace(Regex("(?i)S-P-A"), "SP")
            .replace(Regex("(?i)SPA\\s*AI"), "AI टीचर")
            .replace(Regex("(?i)\\bSPA\\b"), "SP")
            // Strip markdown formatting symbols and bullet points
            .replace(Regex("[*#_`~>•▪▫◆★▶➤•]"), " ")
            // Strip emojis & unwanted special unicode characters
            .replace(Regex("[\\p{So}\\p{Cn}\\p{Cs}]"), " ")
            .replace(Regex("[📚🔬📐🧪🧬🎯🏛️📖✍️💡❓✅❌🔊🎤⚡🎬📊📄📢🤖🎨]"), " ")
            // Normalize spaces
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun speak(text: String, force: Boolean = false) {
        if (!_ttsEnabled.value && !force) return
        if (!isTtsInitialized || textToSpeech == null) return

        try {
            val cleanText = cleanTextForTts(text)

            if (cleanText.isNotBlank()) {
                stopSpeaking()
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "teacher_speech_${System.currentTimeMillis()}")
                }
                textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "teacher_speech_id")
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Speak error: ${e.message}")
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
            stopAudioSimulation()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Stop speak error: ${e.message}")
        }
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        mainHandler.post {
            stopSpeaking()
            stopListeningInternal()

            try {
                lastRecognizedHypothesis = ""

                val recognizer = try {
                    SpeechRecognizer.createSpeechRecognizer(context)
                } catch (e: Exception) {
                    try {
                        SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                    } catch (e2: Exception) {
                        null
                    }
                }

                if (recognizer == null) {
                    Log.e("VoiceManager", "Speech recognition service could not be initialized")
                    _isListening.value = false
                    onError("Microphone recognition service not ready on this device.")
                    return@post
                }

                speechRecognizer = recognizer
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        startListeningVisualizer()
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val norm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioAmplitude.value = norm
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        stopListeningVisualizer()
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        stopListeningVisualizer()

                        // If we captured any partial hypothesis before timeout/error, deliver it!
                        if (lastRecognizedHypothesis.isNotBlank()) {
                            onResult(lastRecognizedHypothesis)
                            lastRecognizedHypothesis = ""
                            return
                        }

                        when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                Log.d("VoiceManager", "Speech ended with timeout or no match (code $error)")
                            }
                            SpeechRecognizer.ERROR_CLIENT -> {
                                Log.d("VoiceManager", "Speech client cancellation")
                            }
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                onError("Microphone permission required")
                            }
                            SpeechRecognizer.ERROR_AUDIO -> {
                                onError("Audio input error. Please check microphone.")
                            }
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                Log.d("VoiceManager", "Speech recognizer busy, resetting")
                            }
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                                onError("Network connection required for speech recognition")
                            }
                            else -> {
                                Log.w("VoiceManager", "SpeechRecognizer error code: $error")
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        stopListeningVisualizer()
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                            onResult(matches[0].trim())
                        } else if (lastRecognizedHypothesis.isNotBlank()) {
                            onResult(lastRecognizedHypothesis.trim())
                        }
                        lastRecognizedHypothesis = ""
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            lastRecognizedHypothesis = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val activeLanguage = _currentLanguage.value
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, activeLanguage.speechLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, activeLanguage.speechLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                _isListening.value = true
                startListeningVisualizer()
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                stopListeningVisualizer()
                Log.e("VoiceManager", "startListening error: ${e.message}")
                onError("Microphone error: ${e.localizedMessage}")
            }
        }
    }

    private fun stopListeningInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
            stopListeningVisualizer()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Stop listening internal error: ${e.message}")
        }
    }

    fun stopListening() {
        mainHandler.post {
            stopListeningInternal()
        }
    }

    fun destroy() {
        mainHandler.post {
            stopListeningInternal()
            stopSpeaking()
            textToSpeech?.shutdown()
            textToSpeech = null
        }
    }
}

