package me.ash.reader.ui.component.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun RYWebView(
    modifier: Modifier = Modifier,
    content: String,
    refererDomain: String? = null,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
)
