package me.ash.reader.ui.component.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.ash.reader.ui.component.reader.Reader

@Composable
actual fun RYWebView(
    modifier: Modifier,
    content: String,
    refererDomain: String?,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)?,
) {
    // Desktop WebView fallback: render via Native Compose Reader until Phase 3 Wry integration
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            Reader(
                link = refererDomain ?: "",
                content = content,
                onImageClick = onImageClick,
                onLinkClick = {},
            )
        }
    }
}
