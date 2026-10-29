package me.ash.reader.infrastructure.platform

import java.io.File

data class AppPaths(
    val dataDir: File,
    val cacheDir: File,
    val dataStoreFile: File,
)
