package me.ash.reader.infrastructure.platform

import java.io.File

fun createDesktopAppPaths(): AppPaths {
    val os = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")

    val dataDir: File
    val cacheDir: File

    when {
        os.contains("mac") -> {
            dataDir = File(userHome, "Library/Application Support/me.ash.reader")
            cacheDir = File(userHome, "Library/Caches/me.ash.reader")
        }
        os.contains("win") -> {
            val appData = System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming"
            val localAppData = System.getenv("LOCALAPPDATA") ?: "$userHome\\AppData\\Local"
            dataDir = File(appData, "ReadYou")
            cacheDir = File(localAppData, "ReadYou\\Cache")
        }
        else -> {
            dataDir = File(userHome, ".local/share/me.ash.reader")
            cacheDir = File(userHome, ".cache/me.ash.reader")
        }
    }

    dataDir.mkdirs()
    cacheDir.mkdirs()

    return AppPaths(
        dataDir = dataDir,
        cacheDir = cacheDir,
        dataStoreFile = File(dataDir, "datastore/settings.preferences_pb"),
    )
}
