package me.ash.reader.infrastructure.image

import coil3.ComponentRegistry
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.io.File

object ImageLoaderFactory {
    fun create(
        context: PlatformContext,
        okHttpClient: OkHttpClient,
        cacheDir: File? = null,
        maxMemoryCacheBytes: Long? = null,
        extraComponents: (ComponentRegistry.Builder.() -> Unit)? = null,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .coroutineContext(Dispatchers.Default)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
                add(SvgDecoder.Factory())
                extraComponents?.invoke(this)
            }
            .memoryCache {
                MemoryCache.Builder()
                    .apply {
                        if (maxMemoryCacheBytes != null) {
                            maxSizeBytes(maxMemoryCacheBytes)
                        } else {
                            maxSizePercent(context, 0.25)
                        }
                    }
                    .build()
            }
            .apply {
                if (cacheDir != null) {
                    diskCache {
                        DiskCache.Builder()
                            .directory(cacheDir.resolve("images").toOkioPath())
                            .maxSizePercent(0.02)
                            .build()
                    }
                }
            }
            .build()
    }
}
