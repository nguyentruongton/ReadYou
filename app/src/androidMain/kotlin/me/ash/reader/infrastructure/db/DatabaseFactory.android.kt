package me.ash.reader.infrastructure.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun createAndroidDatabase(context: Context): AndroidDatabase {
    val dbFile = context.getDatabasePath("Reader")
    return Room.databaseBuilder<AndroidDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(*allMigrations)
        .build()
}
