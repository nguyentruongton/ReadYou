package me.ash.reader.infrastructure.db

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountTypeConverters
import me.ash.reader.domain.model.account.KeepArchivedConverters
import me.ash.reader.domain.model.account.SyncBlockListConverters
import me.ash.reader.domain.model.account.SyncIntervalConverters
import me.ash.reader.domain.model.account.SyncOnStartConverters
import me.ash.reader.domain.model.account.SyncOnlyOnWiFiConverters
import me.ash.reader.domain.model.account.SyncOnlyWhenChargingConverters
import me.ash.reader.domain.model.account.security.DESUtils
import me.ash.reader.domain.model.article.ArchivedArticle
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.preference.KeepArchivedPreference
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.SyncOnStartPreference
import me.ash.reader.infrastructure.preference.SyncOnlyOnWiFiPreference
import me.ash.reader.infrastructure.preference.SyncOnlyWhenChargingPreference
import me.ash.reader.ui.ext.toInt
import java.util.Date

typealias RYDatabase = AndroidDatabase

@Database(
    entities = [Account::class, Feed::class, Article::class, Group::class, ArchivedArticle::class],
    version = 7,
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 5, to = 7),
        AutoMigration(from = 6, to = 7),
    ]
)
@TypeConverters(
    AndroidDatabase.DateConverters::class,
    AccountTypeConverters::class,
    SyncIntervalConverters::class,
    SyncOnStartConverters::class,
    SyncOnlyOnWiFiConverters::class,
    SyncOnlyWhenChargingConverters::class,
    KeepArchivedConverters::class,
    SyncBlockListConverters::class,
)
@ConstructedBy(AndroidDatabaseConstructor::class)
abstract class AndroidDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun groupDao(): GroupDao

    class DateConverters {

        @TypeConverter
        fun toDate(dateLong: Long?): Date? {
            return dateLong?.let { Date(it) }
        }

        @TypeConverter
        fun fromDate(date: Date?): Long? {
            return date?.time
        }
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AndroidDatabaseConstructor : RoomDatabaseConstructor<AndroidDatabase> {
    override fun initialize(): AndroidDatabase
}

val allMigrations: Array<Migration> = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
)

object MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE article ADD COLUMN img TEXT DEFAULT NULL")
    }
}

object MIGRATION_2_3 : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE article ADD COLUMN updateAt INTEGER DEFAULT ${System.currentTimeMillis()}"
        )
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN syncInterval INTEGER NOT NULL DEFAULT ${SyncIntervalPreference.default.value}"
        )
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN syncOnStart INTEGER NOT NULL DEFAULT ${SyncOnStartPreference.default.value.toInt()}"
        )
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN syncOnlyOnWiFi INTEGER NOT NULL DEFAULT ${SyncOnlyOnWiFiPreference.default.value.toInt()}"
        )
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN syncOnlyWhenCharging INTEGER NOT NULL DEFAULT ${SyncOnlyWhenChargingPreference.default.value.toInt()}"
        )
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN keepArchived INTEGER NOT NULL DEFAULT ${KeepArchivedPreference.default.value}"
        )
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN syncBlockList TEXT NOT NULL DEFAULT ''"
        )
    }
}

object MIGRATION_3_4 : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN securityKey TEXT DEFAULT '${DESUtils.empty}'"
        )
    }
}

object MIGRATION_4_5 : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE account ADD COLUMN lastArticleId TEXT DEFAULT NULL"
        )
    }
}
