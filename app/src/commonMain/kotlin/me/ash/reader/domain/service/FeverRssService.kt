package me.ash.reader.domain.service

import androidx.compose.ui.util.fastFilter
import java.util.Date
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.account.security.FeverSecurityKey
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.article.ArticleMeta
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithArticle
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.exception.FeverAPIException
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.infrastructure.platform.Log
import me.ash.reader.infrastructure.platform.NotificationManager
import me.ash.reader.infrastructure.platform.SyncScheduler
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.infrastructure.rss.provider.fever.FeverAPI
import me.ash.reader.infrastructure.rss.provider.fever.FeverDTO
import me.ash.reader.ui.ext.decodeHTML
import me.ash.reader.ui.ext.dollarLast
import me.ash.reader.ui.ext.isFuture
import me.ash.reader.ui.ext.spacerDollar

class FeverRssService(
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val notificationManager: NotificationManager,
    private val groupDao: GroupDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher,
    private val syncScheduler: SyncScheduler,
    private val accountService: AccountService,
) :
    AbstractRssRepository(
        articleDao,
        groupDao,
        feedDao,
        syncScheduler,
        rssHelper,
        ioDispatcher,
        defaultDispatcher,
        accountService,
    ) {

    constructor(
        context: Any?,
        articleDao: ArticleDao,
        feedDao: FeedDao,
        rssHelper: RssHelper,
        notificationManager: NotificationManager,
        groupDao: GroupDao,
        ioDispatcher: CoroutineDispatcher,
        mainDispatcher: CoroutineDispatcher,
        defaultDispatcher: CoroutineDispatcher,
        syncScheduler: SyncScheduler,
        accountService: AccountService,
    ) : this(
        articleDao,
        feedDao,
        rssHelper,
        notificationManager,
        groupDao,
        ioDispatcher,
        mainDispatcher,
        defaultDispatcher,
        syncScheduler,
        accountService,
    )

    override val importSubscription: Boolean = false
    override val addSubscription: Boolean = false
    override val moveSubscription: Boolean = false
    override val deleteSubscription: Boolean = false
    override val updateSubscription: Boolean = false

    private suspend fun getFeverAPI() =
        FeverSecurityKey(accountService.getCurrentAccount().securityKey).run {
            FeverAPI.getInstance(
                serverUrl = serverUrl!!,
                username = username!!,
                password = password!!,
                httpUsername = null,
                httpPassword = null,
            )
        }

    override suspend fun validCredentials(account: Account): Boolean =
        FeverSecurityKey(account.securityKey).run {
            try {
                FeverAPI.getInstance(
                    serverUrl = serverUrl!!,
                    username = username!!,
                    password = password!!,
                    httpUsername = null,
                    httpPassword = null,
                ).validCredentials() > 0
            } catch (e: FeverAPIException) {
                false
            }
        }

    override suspend fun clearAuthorization() {
        FeverAPI.clearInstance()
    }

    /**
     * 1. Fetch the Fever groups
     * 2. Fetch the Fever feeds (including favicons, may need to remove orphaned feeds)
     * 3. Fetch the Fever articles
     * 4. Synchronize read/unread and starred/un-starred items
     */
    override suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?,
    ): SyncResult = coroutineScope {
        try {
            val preTime = System.currentTimeMillis()
            val preDate = Date(preTime)
            val account = accountService.getAccountById(accountId)!!
            check(account.type.id == AccountType.Fever.id) { "account type is invalid" }

            val feverAPI = getFeverAPI()

            // 1. Fetch the Fever groups
            val groupsBody = feverAPI.getGroups()

            val groups =
                groupsBody.groups?.map {
                    Group(
                        id = accountId.spacerDollar(it.id!!),
                        name = it.title ?: "Untitled",
                        accountId = accountId,
                    )
                } ?: emptyList()
            groupDao.insertOrUpdate(groups)

            // 2. Fetch the Fever feeds
            val feedsBody = feverAPI.getFeeds()

            val feedsGroupsMap =
                buildMap<String, String> {
                    groupsBody.feeds_groups?.forEach { feedsGroups ->
                        feedsGroups.group_id?.toString()?.let { groupId ->
                            feedsGroups.feed_ids?.split(",")?.forEach { feedId ->
                                this[feedId] = groupId
                            }
                        }
                    }

                    feedsBody.feeds_groups?.forEach { feedsGroups ->
                        feedsGroups.group_id?.toString()?.let { groupId ->
                            feedsGroups.feed_ids?.split(",")?.forEach { feedId ->
                                this[feedId] = groupId
                            }
                        }
                    }
                }

            // Fetch the Fever favicons
            val faviconsById = feverAPI.getFavicons().favicons?.associateBy { it.id } ?: emptyMap()
            feedDao.insertOrUpdate(
                feedsBody.feeds?.map {
                    Feed(
                        id = accountId.spacerDollar(it.id!!),
                        name = it.title.decodeHTML() ?: "Untitled",
                        url = it.url!!,
                        groupId = accountId.spacerDollar(feedsGroupsMap[it.id.toString()]!!),
                        accountId = accountId,
                        icon = faviconsById[it.favicon_id]?.data,
                    )
                } ?: emptyList()
            )

            // Handle empty icon for feeds
            val noIconFeeds = feedDao.queryNoIcon(accountId)
            feedDao.update(
                *noIconFeeds
                    .map { it.copy(icon = rssHelper.queryRssIconLink(it.url)) }
                    .toTypedArray()
            )

            // 3. Fetch the Fever articles (up to unlimited counts)
            val allArticles = mutableListOf<Article>()

            var lastSeenId = account.lastArticleId?.dollarLast() ?: ""

            while (true) {
                val itemsBody = feverAPI.getItemsSince(lastSeenId)
                val fetchedItems = itemsBody.items

                if (fetchedItems.isNullOrEmpty()) {
                    break
                }

                val articlesFromBatch =
                    fetchedItems.map { item ->
                        Article(
                            id = accountId.spacerDollar(item.id!!),
                            date =
                                item.created_on_time
                                    ?.run { Date(this * 1000) }
                                    ?.takeIf { !it.isFuture(preDate) } ?: preDate,
                            title = item.title.decodeHTML() ?: "Untitled",
                            author = item.author,
                            rawDescription = item.html ?: "",
                            shortDescription =
                                Readability.parseToText(item.html, item.url).take(280),
                            img = rssHelper.findThumbnail(item.html),
                            link = item.url ?: "",
                            feedId = accountId.spacerDollar(item.feed_id!!),
                            accountId = accountId,
                            isUnread = (item.is_read ?: 0) <= 0,
                            isStarred = (item.is_saved ?: 0) > 0,
                            updateAt = preDate,
                        )
                    }

                allArticles.addAll(articlesFromBatch)

                lastSeenId = fetchedItems.lastOrNull()?.id ?: break

                if (fetchedItems.size < 50) {
                    break
                }
            }

            if (allArticles.isNotEmpty()) {
                articleDao.insert(*allArticles.toTypedArray())
                val notificationFeeds =
                    feedDao.queryNotificationEnabled(accountId).associateBy { it.id }
                val notificationFeedIds = notificationFeeds.keys
                allArticles
                    .fastFilter { it.isUnread && it.feedId in notificationFeedIds }
                    .groupBy { it.feedId }
                    .mapKeys { (feedId, _) -> notificationFeeds[feedId]!! }
                    .forEach { (feed, articles) -> notificationManager.notify(FeedWithArticle(feed, articles)) }
            }

            // 4. Synchronize read/unread and starred/un-starred
            val unreadArticleIds = feverAPI.getUnreadItems().unread_item_ids?.split(",")
            val starredArticleIds = feverAPI.getSavedItems().saved_item_ids?.split(",")
            val articleMeta = articleDao.queryMetadataAll(accountId)
            for (meta: ArticleMeta in articleMeta) {
                val articleId = meta.id.dollarLast()
                val shouldBeUnread = unreadArticleIds?.contains(articleId)
                val shouldBeStarred = starredArticleIds?.contains(articleId)
                if (meta.isUnread != shouldBeUnread) {
                    articleDao.markAsReadByArticleId(accountId, meta.id, shouldBeUnread ?: true)
                }
                if (meta.isStarred != shouldBeStarred) {
                    articleDao.markAsStarredByArticleId(
                        accountId,
                        meta.id,
                        shouldBeStarred ?: false,
                    )
                }
            }

            // Remove orphaned groups and feeds, after synchronizing the starred/un-starred
            val groupIds = groups.map { it.id }
            groupDao.queryAll(accountId).forEach {
                if (!groupIds.contains(it.id)) {
                    super.deleteGroup(it, true)
                }
            }

            feedDao.queryAll(accountId).forEach {
                if (!feedsGroupsMap.contains(it.id.dollarLast())) {
                    super.deleteFeed(it, true)
                }
            }

            Log.i("RLog", "onCompletion: ${System.currentTimeMillis() - preTime}")
            accountService.update(
                account.copy(
                    updateAt = Date(),
                    lastArticleId =
                        if (lastSeenId.isNotEmpty()) {
                            accountId.spacerDollar(lastSeenId)
                        } else account.lastArticleId,
                )
            )
            SyncResult.Success
        } catch (e: Exception) {
            Log.e("RLog", "On sync exception: ${e.message}", e)
            SyncResult.Failure
        }
    }

    override suspend fun markAsRead(
        groupId: String?,
        feedId: String?,
        articleId: String?,
        before: Date?,
        isUnread: Boolean,
    ) {
        super.markAsRead(groupId, feedId, articleId, before, isUnread)
        val feverAPI = getFeverAPI()
        val beforeUnixTimestamp = (before?.time ?: Date(Long.MAX_VALUE).time) / 1000
        when {
            groupId != null -> {
                feverAPI.markGroup(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = groupId.dollarLast().toLong(),
                    before = beforeUnixTimestamp,
                )
            }

            feedId != null -> {
                feverAPI.markFeed(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = feedId.dollarLast().toLong(),
                    before = beforeUnixTimestamp,
                )
            }

            articleId != null -> {
                feverAPI.markItem(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = articleId.dollarLast(),
                )
            }

            else -> {
                feedDao.queryAll(accountService.getCurrentAccountId()).forEach {
                    feverAPI.markFeed(
                        status =
                            if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                        id = it.id.dollarLast().toLong(),
                        before = beforeUnixTimestamp,
                    )
                }
            }
        }
    }

    override suspend fun syncReadStatus(articleIds: Set<String>, isUnread: Boolean): Set<String> {
        val feverAPI = getFeverAPI()
        val syncedEntries = mutableSetOf<String>()
        articleIds
            .takeIf { it.isNotEmpty() }
            ?.forEachIndexed { index, it ->
                Log.d("RLog", "sync markAsRead: ${index}/${articleIds.size} num")
                feverAPI.markItem(
                    status = if (isUnread) FeverDTO.StatusEnum.Unread else FeverDTO.StatusEnum.Read,
                    id = it.dollarLast(),
                )
                syncedEntries += it
            }
        return syncedEntries
    }

    override suspend fun markAsStarred(articleId: String, isStarred: Boolean) {
        super.markAsStarred(articleId, isStarred)
        val feverAPI = getFeverAPI()
        feverAPI.markItem(
            status = if (isStarred) FeverDTO.StatusEnum.Saved else FeverDTO.StatusEnum.Unsaved,
            id = articleId.dollarLast(),
        )
    }
}
