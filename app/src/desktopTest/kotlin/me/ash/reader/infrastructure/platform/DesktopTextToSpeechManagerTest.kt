package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopTextToSpeechManagerTest {

    @Test
    fun `extractSentencesFromHtml strips HTML tags and splits into non-empty sentences`() {
        val html = "<h1>Title!</h1><p>First sentence. Second sentence? Third sentence。</p>"
        val sentences = DesktopTextToSpeechManager.extractSentencesFromHtml(html)
        assertEquals(listOf("Title!", "First sentence.", "Second sentence?", "Third sentence。"), sentences)
    }

    @Test
    fun `extractSentencesFromHtml returns empty list for blank HTML`() {
        assertEquals(emptyList<String>(), DesktopTextToSpeechManager.extractSentencesFromHtml("<p>   </p>"))
    }

    @Test
    fun `buildTtsCommand builds OS specific commands and escapes Windows single quotes`() {
        val macCmd = DesktopTextToSpeechManager.buildTtsCommand("Mac OS X", "Hello world")
        assertEquals(listOf("/usr/bin/say", "Hello world"), macCmd)

        val winCmd = DesktopTextToSpeechManager.buildTtsCommand("Windows 11", "It's ReadYou")
        assertEquals("powershell", winCmd[0])
        assertTrue(winCmd.last().contains("It''s ReadYou"))

        val linuxCmd = DesktopTextToSpeechManager.buildTtsCommand("Linux", "Hello Linux")
        assertEquals(listOf("spd-say", "-w", "Hello Linux"), linuxCmd)
    }

    @Test
    fun `readHtml updates stateFlow through Reading chunks and returns to Idle or stops immediately`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val spokenCommands = mutableListOf<List<String>>()
        val gate = CompletableDeferred<Unit>()

        val manager = DesktopTextToSpeechManager(
            scope = scope,
            osNameProvider = { "Mac OS X" },
            processRunner = { cmd ->
                spokenCommands.add(cmd)
                gate.await()
            }
        )

        manager.readHtml("<p>Sentence one. Sentence two.</p>")
        testScheduler.runCurrent()

        assertEquals(TextToSpeechManager.State.Reading(1, 2), manager.state)
        assertEquals(1, spokenCommands.size)

        // Stop mid-speech
        manager.stop()
        assertEquals(TextToSpeechManager.State.Idle, manager.state)

        // Complete full run without stopping
        val completedCommands = mutableListOf<List<String>>()
        val manager2 = DesktopTextToSpeechManager(
            scope = scope,
            osNameProvider = { "Mac OS X" },
            processRunner = { cmd -> completedCommands.add(cmd) }
        )
        manager2.readHtml("<p>One. Two.</p>")
        advanceUntilIdle()
        assertEquals(2, completedCommands.size)
        assertEquals(TextToSpeechManager.State.Idle, manager2.state)
    }
}
