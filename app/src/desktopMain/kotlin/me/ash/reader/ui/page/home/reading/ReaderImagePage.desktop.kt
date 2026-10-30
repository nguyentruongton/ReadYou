package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import me.ash.reader.ui.ext.extractDomain

@Composable
actual fun ReaderImageViewer(
    imageData: ImageData,
    onDownloadImage: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
        ) {
            val painter =
                rememberAsyncImagePainter(
                    ImageRequest.Builder(coil3.PlatformContext.INSTANCE)
                        .apply {
                            val domain = imageData.imageUrl.extractDomain()
                            if (domain != null) {
                                httpHeaders(NetworkHeaders.Builder().set("Referer", domain).build())
                            }
                        }
                        .data(data = imageData.imageUrl)
                        .build()
                )

            Image(
                painter = painter,
                contentDescription = imageData.altText,
                modifier =
                    Modifier.fillMaxSize()
                        .clickable { onDismissRequest() },
                alignment = Alignment.Center,
                contentScale = ContentScale.Inside,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                    )
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = { onDownloadImage(imageData.imageUrl) },
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download",
                    )
                }
            }
        }
    }
}
