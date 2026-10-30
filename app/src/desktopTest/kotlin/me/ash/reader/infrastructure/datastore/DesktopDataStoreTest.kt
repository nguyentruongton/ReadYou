package me.ash.reader.infrastructure.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.ash.reader.infrastructure.platform.AppPaths
import org.junit.Assert.assertEquals
import org.junit.Test

class DesktopDataStoreTest {
    @Test
    fun preferenceDataStoreWritesAndReadsKeysOnDesktop() = runBlocking {
        val tempDir = Files.createTempDirectory("readyou-test").toFile()
        val appPaths = AppPaths(
            dataDir = tempDir,
            cacheDir = tempDir.resolve("cache"),
            dataStoreFile = tempDir.resolve("datastore/settings.preferences_pb"),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val dataStore = createPreferenceDataStore(appPaths, scope)
            val key = intPreferencesKey(PreferencesKey.currentAccountId)
            dataStore.edit { it[key] = 42 }
            assertEquals(42, dataStore.data.first()[key])
        } finally {
            scope.cancel()
            tempDir.deleteRecursively()
        }
    }
}
