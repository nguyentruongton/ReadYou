package me.ash.reader.ui.component.base

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil3.compose.rememberAsyncImagePainter
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.ui.svg.parseDynamicColor
import me.ash.reader.ui.theme.palette.LocalTonalPalettes

@Composable
fun DynamicSVGImage(
    modifier: Modifier = Modifier,
    svgImageString: String,
    contentDescription: String,
) {
    val useDarkTheme = LocalDarkTheme.current.isDarkTheme()
    val tonalPalettes = LocalTonalPalettes.current
    val svgBytes = remember(svgImageString, tonalPalettes, useDarkTheme) {
        svgImageString.parseDynamicColor(tonalPalettes, useDarkTheme).encodeToByteArray()
    }

    Row(
        modifier = modifier.aspectRatio(1.38f)
    ) {
        Crossfade(targetState = svgBytes) { bytes ->
            Image(
                contentDescription = contentDescription,
                painter = rememberAsyncImagePainter(bytes)
            )
        }
    }
}
