package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class DesktopTextToSpeechManager(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val osNameProvider: () -> String = { System.getProperty("os.name") ?: "" },
    private val processRunner: (suspend (List<String>) -> Unit)? = null,
) : TextToSpeechManager {

    private val _stateFlow = MutableStateFlow<TextToSpeechManager.State>(TextToSpeechManager.State.Idle)
    override val stateFlow: StateFlow<TextToSpeechManager.State> = _stateFlow.asStateFlow()

    override var state: TextToSpeechManager.State
        get() = _stateFlow.value
        set(value) {
            _stateFlow.value = value
        }

    private var readingJob: Job? = null
    @Volatile private var currentProcess: Process? = null

    override fun readHtml(htmlContent: String) {
        stop()

        val sentences = extractSentencesFromHtml(htmlContent)
        if (sentences.isEmpty()) {
            state = TextToSpeechManager.State.Idle
            return
        }

        val osName = osNameProvider()

        readingJob = scope.launch {
            try {
                for ((index, sentence) in sentences.withIndex()) {
                    state = TextToSpeechManager.State.Reading(index + 1, sentences.size)
                    val cmd = buildTtsCommand(osName, sentence)
                    if (processRunner != null) {
                        processRunner.invoke(cmd)
                    } else {
                        runProcess(cmd)
                    }
                }
            } finally {
                state = TextToSpeechManager.State.Idle
                currentProcess = null
            }
        }
    }

    override fun stop() {
        readingJob?.cancel()
        readingJob = null
        currentProcess?.destroyForcibly()
        currentProcess = null
        state = TextToSpeechManager.State.Idle
    }

    private suspend fun runProcess(cmd: List<String>) = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(cmd)
            .redirectErrorStream(true)
            .start()
        currentProcess = process
        try {
            process.waitFor()
        } finally {
            if (currentProcess == process) {
                currentProcess = null
            }
        }
    }

    companion object {
        fun extractSentencesFromHtml(htmlContent: String): List<String> {
            val plainText = Jsoup.parse(htmlContent).text().trim()
            if (plainText.isBlank()) return emptyList()
            return plainText
                .split(Regex("(?<=[.!?。！？\\n])\\s+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }

        fun buildTtsCommand(osName: String, text: String): List<String> {
            val lower = osName.lowercase()
            return when {
                lower.contains("mac") || lower.contains("darwin") -> {
                    listOf("/usr/bin/say", text)
                }
                lower.contains("win") -> {
                    val escaped = text.replace("'", "''")
                    listOf(
                        "powershell",
                        "-NoProfile",
                        "-NonInteractive",
                        "-Command",
                        "Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('$escaped')"
                    )
                }
                else -> {
                    listOf("spd-say", "-w", text)
                }
            }
        }
    }
}
