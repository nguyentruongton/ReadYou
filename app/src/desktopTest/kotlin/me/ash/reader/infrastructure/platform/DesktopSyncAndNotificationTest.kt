package me.ash.reader.infrastructure.platform

import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithArticle
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.SyncOnStartPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopSyncAndNotificationTest {

    @Test
    fun initSyncTriggersImmediateSyncWhenSyncOnStartEnabledAndSchedulesPeriodicTimer() = runTest {
        val syncedAccounts = mutableListOf<Int?>()
        val schedulerScope = TestScope(StandardTestDispatcher(testScheduler))
        val scheduler = DesktopSyncScheduler(
            coroutineScope = schedulerScope,
            onSyncRequest = { accountId, _, _ -> syncedAccounts.add(accountId) },
        )

        val account = Account(
            id = 7,
            name = "Local",
            type = AccountType.Local,
            syncOnStart = SyncOnStartPreference.On,
            syncInterval = SyncIntervalPreference.Every15Minutes,
        )

        scheduler.initSync(account)
        runCurrent()
        assertEquals(listOf<Int?>(7), syncedAccounts)

        advanceTimeBy(15 * 60_000L)
        runCurrent()
        assertEquals(listOf<Int?>(7, 7), syncedAccounts)

        advanceTimeBy(15 * 60_000L)
        runCurrent()
        assertEquals(listOf<Int?>(7, 7, 7), syncedAccounts)

        scheduler.cancelSync()
    }

    @Test
    fun initSyncReplacesPreviousPeriodicTimerAndDoesNotLoopWhenIntervalIsManual() = runTest {
        val syncedAccounts = mutableListOf<Int?>()
        val schedulerScope = TestScope(StandardTestDispatcher(testScheduler))
        val scheduler = DesktopSyncScheduler(
            coroutineScope = schedulerScope,
            onSyncRequest = { accountId, _, _ -> syncedAccounts.add(accountId) },
        )

        val firstAccount = Account(
            id = 1,
            name = "First",
            type = AccountType.Local,
            syncOnStart = SyncOnStartPreference.Off,
            syncInterval = SyncIntervalPreference.Every15Minutes,
        )
        val manualAccount = Account(
            id = 2,
            name = "Manual",
            type = AccountType.Local,
            syncOnStart = SyncOnStartPreference.Off,
            syncInterval = SyncIntervalPreference.Manually,
        )

        scheduler.initSync(firstAccount)
        scheduler.initSync(manualAccount)

        advanceTimeBy(60 * 60_000L)
        runCurrent()
        assertTrue(syncedAccounts.isEmpty())
    }

    @Test
    fun notificationManagerOnlyNotifiesWhenFeedHasNotificationEnabledAndArticlesNonEmpty() {
        val delivered = mutableListOf<Pair<String, String>>()
        val manager = DesktopNotificationManager(
            onSendNotification = { title, message -> delivered.add(title to message) }
        )

        val disabledFeed = Feed(
            id = "f1",
            name = "Disabled Feed",
            url = "https://example.com/rss",
            groupId = "g1",
            accountId = 1,
            isNotification = false,
        )
        val enabledFeed = disabledFeed.copy(id = "f2", name = "Kotlin Blog", isNotification = true)
        val article1 = Article(
            id = "a1",
            date = Date(),
            title = "Kotlin \"2.2\" Released",
            rawDescription = "Desc",
            shortDescription = "Short",
            link = "https://kotlinlang.org",
            feedId = "f2",
            accountId = 1,
        )
        val article2 = article1.copy(id = "a2", title = "Compose Desktop Update")

        manager.notify(FeedWithArticle(disabledFeed, listOf(article1)))
        manager.notify(FeedWithArticle(enabledFeed, emptyList()))
        assertTrue(delivered.isEmpty())

        manager.notify(FeedWithArticle(enabledFeed, listOf(article1)))
        assertEquals(1, delivered.size)
        assertEquals("Kotlin Blog" to "Kotlin \"2.2\" Released", delivered[0])

        manager.notify(FeedWithArticle(enabledFeed, listOf(article1, article2)))
        assertEquals(2, delivered.size)
        assertEquals("Kotlin Blog" to "2 new articles: Kotlin \"2.2\" Released", delivered[1])

        val cmd = DesktopNotificationManager.buildAppleScriptNotificationCommand(
            title = "Kotlin \"Blog\"",
            message = "Line\\1 \"Quote\"",
        )
        assertEquals(
            listOf(
                "/usr/bin/osascript",
                "-e",
                "display notification \"Line\\\\1 \\\"Quote\\\"\" with title \"Kotlin \\\"Blog\\\"\"",
            ),
            cmd,
        )
    }
}
