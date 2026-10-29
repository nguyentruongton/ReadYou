package me.ash.reader.ui.component.reader

import androidx.compose.foundation.lazy.LazyListScope

@Suppress("FunctionName")
fun LazyListScope.Reader(
    context: Any? = null,
    subheadUpperCase: Boolean = false,
    link: String,
    content: String,
    onImageClick: ((imgUrl: String, altText: String) -> Unit)? = null,
    onLinkClick: (String) -> Unit
) {
    htmlFormattedText(
        inputStream = content.byteInputStream(),
        subheadUpperCase = subheadUpperCase,
        baseUrl = link,
        onImageClick = onImageClick,
        imagePlaceholder = null,
        onLinkClick = onLinkClick
    )
}
