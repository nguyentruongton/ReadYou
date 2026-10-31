package me.ash.reader.ui.component.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.webview.web.WebViewFactoryParam
import io.github.kdroidfilter.webview.web.defaultWebViewFactory
import io.github.kdroidfilter.webview.web.rememberWebViewStateWithHTMLData
import io.github.kdroidfilter.webview.wry.WryWebViewPanel
import java.awt.GraphicsEnvironment
import javax.swing.JPanel
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.reader.Reader
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.alwaysLight

actual val isEmbeddedNativeWebView: Boolean = true

fun buildDesktopWebViewHtml(
    content: String,
    refererDomain: String?,
    fontSize: Int,
    fontPath: String? = null,
    lineHeight: Float,
    letterSpacing: Float,
    textMargin: Int,
    textColor: Int,
    textBold: Boolean,
    textAlign: String,
    boldTextColor: Int,
    subheadBold: Boolean,
    subheadUpperCase: Boolean,
    imgMargin: Int,
    imgBorderRadius: Int,
    linkTextColor: Int,
    codeTextColor: Int,
    codeBgColor: Int,
    selectionTextColor: Int,
    selectionBgColor: Int,
    boldCharacters: Boolean,
): String =
    WebViewHtml.HTML.format(
        WebViewStyle.get(
            fontSize = fontSize,
            fontPath = fontPath,
            lineHeight = lineHeight,
            letterSpacing = letterSpacing,
            textMargin = textMargin,
            textColor = textColor,
            textBold = textBold,
            textAlign = textAlign,
            boldTextColor = boldTextColor,
            subheadBold = subheadBold,
            subheadUpperCase = subheadUpperCase,
            imgMargin = imgMargin,
            imgBorderRadius = imgBorderRadius,
            linkTextColor = linkTextColor,
            codeTextColor = codeTextColor,
            codeBgColor = codeBgColor,
            tableMargin = textMargin,
            selectionTextColor = selectionTextColor,
            selectionBgColor = selectionBgColor,
        ),
        refererDomain ?: "",
        content,
        WebViewScript.get(boldCharacters),
    )

@Composable
actual fun RYWebView(
    modifier: Modifier,
    content: String,
    refererDomain: String?,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)?,
) {
    val uriHandler = LocalUriHandler.current
    val tonalElevation = LocalReadingPageTonalElevation.current
    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation.value.dp)
    val selectionTextColor = Color.Black.toArgb()
    val selectionBgColor = (MaterialTheme.colorScheme.tertiaryContainer alwaysLight true).toArgb()
    val textColor: Int = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val textBold: Boolean = LocalReadingTextBold.current.value
    val textAlign: String = LocalReadingTextAlign.current.toTextAlignCSS()
    val textMargin: Int = LocalReadingTextHorizontalPadding.current
    val boldTextColor: Int = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkTextColor: Int = MaterialTheme.colorScheme.primary.toArgb()
    val subheadBold: Boolean = LocalReadingSubheadBold.current.value
    val subheadUpperCase: Boolean = LocalReadingSubheadUpperCase.current.value
    val fontSize: Int = LocalReadingTextFontSize.current
    val letterSpacing: Float = LocalReadingTextLetterSpacing.current
    val lineHeight: Float = LocalReadingTextLineHeight.current
    val imgMargin: Int = LocalReadingImageHorizontalPadding.current
    val imgBorderRadius: Int = LocalReadingImageRoundedCorners.current
    val codeTextColor: Int = MaterialTheme.colorScheme.tertiary.toArgb()
    val codeBgColor: Int =
        MaterialTheme.colorScheme.surfaceColorAtElevation((tonalElevation.value + 6).dp).toArgb()
    val boldCharacters = LocalReadingBoldCharacters.current
    val readingFonts = LocalReadingFonts.current
    val fontPath = remember(readingFonts) {
        me.ash.reader.ui.theme.PlatformFontLoader.resolveWebViewFontPath(readingFonts)
    }

    val html =
        remember(
            content,
            refererDomain,
            fontSize,
            fontPath,
            lineHeight,
            letterSpacing,
            textMargin,
            textColor,
            textBold,
            textAlign,
            boldTextColor,
            subheadBold,
            subheadUpperCase,
            imgMargin,
            imgBorderRadius,
            linkTextColor,
            codeTextColor,
            codeBgColor,
            selectionTextColor,
            selectionBgColor,
            boldCharacters.value,
        ) {
            buildDesktopWebViewHtml(
                content = content,
                refererDomain = refererDomain,
                fontSize = fontSize,
                fontPath = fontPath,
                lineHeight = lineHeight,
                letterSpacing = letterSpacing,
                textMargin = textMargin,
                textColor = textColor,
                textBold = textBold,
                textAlign = textAlign,
                boldTextColor = boldTextColor,
                subheadBold = subheadBold,
                subheadUpperCase = subheadUpperCase,
                imgMargin = imgMargin,
                imgBorderRadius = imgBorderRadius,
                linkTextColor = linkTextColor,
                codeTextColor = codeTextColor,
                codeBgColor = codeBgColor,
                selectionTextColor = selectionTextColor,
                selectionBgColor = selectionBgColor,
                boldCharacters = boldCharacters.value,
            )
        }

    val isHeadless = remember { GraphicsEnvironment.isHeadless() }
    var webViewError by remember { mutableStateOf(false) }

    if (isHeadless || webViewError) {
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                Reader(
                    link = refererDomain ?: "",
                    content = content,
                    onImageClick = onImageClick,
                    onLinkClick = { uriHandler.openUri(it) },
                )
            }
        }
    } else {
        val state = rememberWebViewStateWithHTMLData(data = html, baseUrl = refererDomain)
        state.webSettings.backgroundColor = surfaceColor
        state.webSettings.desktopWebSettings.transparent = false

        SwingPanel(
            modifier = modifier.fillMaxSize(),
            factory = {
                try {
                    val panel = defaultWebViewFactory(WebViewFactoryParam(state))
                    panel.background = java.awt.Color(surfaceColor.toArgb(), true)
                    panel.addNavigateListener { url ->
                        if (
                            (url.startsWith("http://") || url.startsWith("https://")) &&
                                url != refererDomain
                        ) {
                            uriHandler.openUri(url)
                            false
                        } else {
                            true
                        }
                    }
                    panel.loadHtml(html)
                    panel
                } catch (e: Throwable) {
                    println("Failed to initialize Desktop WebView, falling back to Native Reader: ${e.message}")
                    webViewError = true
                    JPanel()
                }
            },
            update = { panel ->
                try {
                    if (panel is WryWebViewPanel) {
                        panel.loadHtml(html)
                    }
                } catch (e: Throwable) {
                    println("Failed to update Desktop WebView HTML: ${e.message}")
                }
            },
        )
    }
}
