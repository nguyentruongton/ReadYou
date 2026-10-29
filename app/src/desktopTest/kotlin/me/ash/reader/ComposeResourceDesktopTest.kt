package me.ash.reader

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeResourceDesktopTest {

    @Test
    fun testReederHtmlResourceExists() = runBlocking {
        val bytes = Res.readBytes("files/reeder.html")
        assertNotNull(bytes)
        assertTrue(bytes.isNotEmpty())
        val content = bytes.decodeToString()
        assertTrue(content.contains("reeder") || content.contains("html"))
    }
}
