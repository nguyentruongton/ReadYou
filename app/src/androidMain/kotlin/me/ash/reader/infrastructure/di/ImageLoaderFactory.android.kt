package me.ash.reader.infrastructure.di

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient

fun provideImageLoader(
    context: Context,
    okHttpClient: OkHttpClient,
): ImageLoader {
    return ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .dispatcher(Dispatchers.Default)
        .components {
            add(SvgDecoder.Factory())
            add(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                }
            )
        }
        .diskCache(
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("images"))
                .maxSizePercent(0.02)
                .build()
        )
        .memoryCache(
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        )
        .build()
}
