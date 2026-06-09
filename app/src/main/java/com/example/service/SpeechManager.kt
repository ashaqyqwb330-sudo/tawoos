package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale
import java.util.UUID

class SpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    var isEnabled: Boolean = true

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { textToSpeech ->
                    val result = textToSpeech.setLanguage(Locale("ar"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech.language = Locale.US
                    }
                }
            }
        }
    }

    fun speak(text: String) {
        if (!isEnabled || tts == null || text.isBlank()) return
        tts?.setSpeechRate(0.85f)   // Slow, clear military/academic tone
        tts?.setPitch(1.05f)        // Solid and clear acoustics
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

val LocalSpeechManager = staticCompositionLocalOf<SpeechManager> {
    error("No SpeechManager provided")
}
