package me.ash.reader.infrastructure.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class DesktopDatabaseTest {
    @Test
    fun roomInMemoryDatabaseInsertsAndQueriesAccount() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder<AndroidDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(*allMigrations)
            .build()
        try {
            val id = db.accountDao().insert(Account(name = "Desktop Local", type = AccountType.Local)).toInt()
            val loaded = db.accountDao().queryById(id)
            assertEquals("Desktop Local", loaded?.name)
            assertEquals(AccountType.Local.id, loaded?.type?.id)
        } finally {
            db.close()
        }
    }
}
