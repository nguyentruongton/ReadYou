package me.ash.reader.infrastructure.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.platform.AppPaths
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class DesktopKoinSmokeTest {
    @Test
    fun desktopKoinGraphResolvesDatabaseDaosAndDataStore() {
        val app = startKoin {
            modules(commonModule, platformModule)
        }
        try {
            val koin = app.koin
            assertNotNull(koin.get<AppPaths>())
            assertNotNull(koin.get<AndroidDatabase>())
            assertNotNull(koin.get<AccountDao>())
            assertNotNull(koin.get<DataStore<Preferences>>())
        } finally {
            stopKoin()
        }
    }
}
