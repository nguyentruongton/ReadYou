package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class DesktopImageDownloader(
    private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient,
    private val appPaths: AppPaths,
) : ImageDownloader {

    override suspend fun downloadImage(imageUrl: String): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val request = Request.Builder().url(imageUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download image: HTTP ${response.code}")
            }
            val fileName = imageUrl.substringAfterLast('/').substringBefore('?').ifBlank { "download_${System.currentTimeMillis()}.jpg" }
            val picturesDir = File(System.getProperty("user.home"), "Pictures/ReadYou").apply { mkdirs() }
            val file = File(picturesDir, fileName)
            response.body.byteStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        }
    }
}
