package me.ash.reader.ui.component.webview

import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopWebViewHtmlTest {

    @Test
    fun `isEmbeddedNativeWebView is true on Desktop`() {
        assertTrue(isEmbeddedNativeWebView)
    }

    @Test
    fun `buildDesktopWebViewHtml injects CSS variables, base href, article body, and Bionic Reading script`() {
        val html = buildDesktopWebViewHtml(
            content = "<p>Hello Desktop WebView</p>",
            refererDomain = "https://example.com",
            fontSize = 18,
            lineHeight = 1.4f,
            letterSpacing = 0.5f,
            textMargin = 24,
            textColor = 0xFF222222.toInt(),
            textBold = false,
            textAlign = "start",
            boldTextColor = 0xFF000000.toInt(),
            subheadBold = true,
            subheadUpperCase = false,
            imgMargin = 12,
            imgBorderRadius = 8,
            linkTextColor = 0xFF0066CC.toInt(),
            codeTextColor = 0xFF990055.toInt(),
            codeBgColor = 0xFFF5F5F5.toInt(),
            selectionTextColor = 0xFF000000.toInt(),
            selectionBgColor = 0xFFCCE5FF.toInt(),
            boldCharacters = true,
        )

        assertTrue(html.contains("--font-size: 18px;"))
        assertTrue(html.contains("<base href=\"https://example.com\" />"))
        assertTrue(html.contains("<p>Hello Desktop WebView</p>"))
        assertTrue(html.contains("setBold(true);"))
    }
}
