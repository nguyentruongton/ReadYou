package me.ash.reader.infrastructure.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers

fun createDesktopDatabase(dbFile: File): AndroidDatabase {
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AndroidDatabase>(
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(*allMigrations)
        .build()
}
