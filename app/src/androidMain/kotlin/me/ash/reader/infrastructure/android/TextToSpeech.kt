package me.ash.reader.infrastructure.android

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Html
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextLanguage
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

import me.ash.reader.infrastructure.platform.TextToSpeechManager as ITextToSpeechManager
import me.ash.reader.infrastructure.platform.TextToSpeechManager.State.*

class TextToSpeechManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) : ITextToSpeechManager {

    private val _stateFlow = MutableStateFlow<ITextToSpeechManager.State>(Idle)
    override val stateFlow = _stateFlow.asStateFlow()

    override var state: ITextToSpeechManager.State
        get() = stateFlow.value
        set(value) {
            _stateFlow.value = value
        }

    private val tts: TextToSpeech = initTts()

    private fun initTts(): TextToSpeech {
        return TextToSpeech(context, TextToSpeech.OnInitListener {
            when (it) {
                TextToSpeech.SUCCESS -> {}
                else -> {
                    state = Error
                    Timber.e("TextToSpeech initialization failed $it")
                }
            }
        })
    }

    override fun readHtml(htmlContent: String) {
        coroutineScope.launch {
            val plainText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY).toString()
            readText(plainText)
        }
    }

    private fun readText(text: String) {
        stop()

        if (state != Idle) return

        state = Preparing

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tts.language =
                context.detectLocaleFromText(text.take(text.lastIndex.coerceAtMost(500)))
                    .firstOrNull()?.locale
        }

        val textSegments = text.split("\n").filterNot { it.isBlank() }
        val total = textSegments.size
        state = Reading(0, total)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                state = Reading(utteranceId?.toIntOrNull() ?: 0, total)
            }

            override fun onDone(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: 0
                val cur = state
                if (cur is Reading && index >= cur.total) {
                    state = Idle
                }
            }

            override fun onError(utteranceId: String?) {
                state = Error
            }
        })

        textSegments.forEachIndexed { index, text ->
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, (index + 1).toString())
        }
    }

    override fun stop() {
        tts.stop()
        state = Idle
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.detectLocaleFromText(
    text: CharSequence,
    minConfidence: Float = 80.0f,
): Sequence<LocaleWithConfidence> {
    val textClassificationManager =
        getSystemService<TextClassificationManager>() ?: return emptySequence()
    val textClassifier = textClassificationManager.textClassifier

    val textRequest = TextLanguage.Request.Builder(text).build()
    val detectedLanguage = textClassifier.detectLanguage(textRequest)

    return sequence {
        for (i in 0 until detectedLanguage.localeHypothesisCount) {
            val localeDetected = detectedLanguage.getLocale(i)
            val confidence = detectedLanguage.getConfidenceScore(localeDetected) * 100.0f
            if (confidence >= minConfidence) {
                yield(
                    LocaleWithConfidence(
                        locale = localeDetected.toLocale(),
                        confidence = confidence,
                    ),
                )
            }
        }
    }
}

data class LocaleWithConfidence(
    val locale: Locale,
    val confidence: Float,
)