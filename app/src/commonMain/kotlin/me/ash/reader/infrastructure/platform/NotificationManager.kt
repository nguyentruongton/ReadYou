package me.ash.reader.infrastructure.platform

import me.ash.reader.domain.model.feed.FeedWithArticle

interface NotificationManager {
    fun notify(feedWithArticle: FeedWithArticle)
}
