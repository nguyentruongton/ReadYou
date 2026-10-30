package me.ash.reader.ui.page.home.reading

import androidx.compose.runtime.Composable

data class ImageData(val imageUrl: String = "", val altText: String = "")

@Composable
expect fun ReaderImageViewer(
    imageData: ImageData,
    onDownloadImage: (String) -> Unit,
    onDismissRequest: () -> Unit = {},
)
