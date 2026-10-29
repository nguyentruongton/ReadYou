package me.ash.reader.ui.component.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import java.util.Base64

@Composable
fun Base64Image(
    modifier: Modifier = Modifier,
    base64Uri: String,
    onEmpty: @Composable () -> Unit = {},
) {
    val bytes = remember(base64Uri) {
        val base64Data = base64Uri.substringAfter("base64,")
        try {
            Base64.getMimeDecoder().decode(base64Data)
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    if (bytes.isEmpty()) {
        onEmpty()
    } else {
        AsyncImage(
            model = bytes,
            contentDescription = null,
            modifier = modifier,
        )
    }
}
