package me.ash.reader.ui.component.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import me.ash.reader.ui.ext.extractDomain

val SIZE_1000 = Size(1000, 1000)

@Composable
fun RYAsyncImage(
    modifier: Modifier = Modifier,
    data: Any? = null,
    size: Size = Size.ORIGINAL,
    scale: Scale = Scale.FIT,
    precision: Precision = Precision.EXACT,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
    placeholder: Any? = null,
    error: Any? = null,
) {
    val painter =
        rememberAsyncImagePainter(
            model =
                ImageRequest.Builder(LocalPlatformContext.current)
                    .apply {
                        val domain = data?.toString()?.extractDomain()
                        if (domain != null) {
                            httpHeaders(NetworkHeaders.Builder().set("Referer", domain).build())
                        }
                    }
                    .data(data = data)
                    .apply {
                        crossfade(true)
                        scale(scale)
                        precision(precision)
                        size(size)
                    }
                    .build()
        )
    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
    )
}

/**
 * Create and return a new [Painter] that wraps [painter] with its [alpha], [colorFilter], or
 * [onDraw] overwritten.
 */
fun forwardingPainter(
    painter: Painter,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    onDraw: DrawScope.(ForwardingDrawInfo) -> Unit = DefaultOnDraw,
): Painter = ForwardingPainter(painter, alpha, colorFilter, onDraw)

@Immutable
data class ForwardingDrawInfo(
    val painter: Painter,
    val alpha: Float,
    val colorFilter: ColorFilter?,
)

@Immutable
private class ForwardingPainter(
    private val painter: Painter,
    private var alpha: Float,
    private var colorFilter: ColorFilter?,
    private val onDraw: DrawScope.(ForwardingDrawInfo) -> Unit,
) : Painter() {

    private var info = newInfo()

    override val intrinsicSize
        get() = painter.intrinsicSize

    override fun applyAlpha(alpha: Float): Boolean {
        if (alpha == DefaultAlpha) {
            this.alpha = alpha
            this.info = newInfo()
        }
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        if (colorFilter == null) {
            this.colorFilter = colorFilter
            this.info = newInfo()
        }
        return true
    }

    override fun DrawScope.onDraw() = onDraw(info)

    private fun newInfo() = ForwardingDrawInfo(painter, alpha, colorFilter)
}

private val DefaultOnDraw: DrawScope.(ForwardingDrawInfo) -> Unit = { info ->
    with(info.painter) {
        draw(
            androidx.compose.ui.geometry.Size(size.width, size.height),
            info.alpha,
            info.colorFilter,
        )
    }
}
