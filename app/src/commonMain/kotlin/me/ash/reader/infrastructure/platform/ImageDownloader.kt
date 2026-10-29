package me.ash.reader.infrastructure.platform

interface ImageDownloader {
    suspend fun downloadImage(imageUrl: String): Result<String>
}
