package me.ash.reader.infrastructure.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

sealed interface PreferencesKey {
    val name: String
    val key: Preferences.Key<*>

    data class IntKey(
        override val name: String,
        override val key: Preferences.Key<Int> = intPreferencesKey(name),
    ) : PreferencesKey

    data class LongKey(
        override val name: String,
        override val key: Preferences.Key<Long> = longPreferencesKey(name),
    ) : PreferencesKey

    data class StringKey(
        override val name: String,
        override val key: Preferences.Key<String> = stringPreferencesKey(name),
    ) : PreferencesKey

    data class BooleanKey(
        override val name: String,
        override val key: Preferences.Key<Boolean> = booleanPreferencesKey(name),
    ) : PreferencesKey

    data class FloatKey(
        override val name: String,
        override val key: Preferences.Key<Float> = floatPreferencesKey(name),
    ) : PreferencesKey

    companion object {
        const val isFirstLaunch = "isFirstLaunch"
        const val newVersionPublishDate = "newVersionPublishDate"
        const val newVersionLog = "newVersionLog"
        const val newVersionSizeString = "newVersionSizeString"
        const val newVersionDownloadUrl = "newVersionDownloadUrl"
        const val newVersionNumber = "newVersionNumber"
        const val skipVersionNumber = "skipVersionNumber"
        const val currentAccountId = "currentAccountId"
        const val currentAccountType = "currentAccountType"
        const val themeIndex = "themeIndex"
        const val customPrimaryColor = "customPrimaryColor"
        const val darkTheme = "darkTheme"
        const val amoledDarkTheme = "amoledDarkTheme"
        const val basicFonts = "basicFonts"

        // Feeds page
        const val feedsFilterBarStyle = "feedsFilterBarStyle"
        const val feedsFilterBarPadding = "feedsFilterBarPadding"
        const val feedsFilterBarTonalElevation = "feedsFilterBarTonalElevation"
        const val feedsTopBarTonalElevation = "feedsTopBarTonalElevation"
        const val feedsGroupListExpand = "feedsGroupListExpand"
        const val feedsGroupListTonalElevation = "feedsGroupListTonalElevation"

        // Flow page
        const val flowFilterBarStyle = "flowFilterBarStyle"
        const val flowFilterBarPadding = "flowFilterBarPadding"
        const val flowFilterBarTonalElevation = "flowFilterBarTonalElevation"
        const val flowTopBarTonalElevation = "flowTopBarTonalElevation"
        const val flowArticleListFeedIcon = "flowArticleListFeedIcon"
        const val flowArticleListFeedName = "flowArticleListFeedName"
        const val flowArticleListImage = "flowArticleListImage"
        const val flowArticleListDesc = "flowArticleListDescription"
        const val flowArticleListTime = "flowArticleListTime"
        const val flowArticleListDateStickyHeader = "flowArticleListDateStickyHeader"
        const val flowArticleListTonalElevation = "flowArticleListTonalElevation"
        const val flowArticleListReadIndicator = "flowArticleListReadStatusIndicator"
        const val flowSortUnreadArticles = "flowArticleListSortUnreadArticles"

        // Reading page
        const val readingRenderer = "readingRender"
        const val readingBoldCharacters = "readingBoldCharacters"
        const val readingPageTonalElevation = "readingPageTonalElevation"
        const val readingTextFontSize = "readingTextFontSize"
        const val readingTextLineHeight = "readingTextLineHeight"
        const val readingTextLetterSpacing = "readingTextLetterSpacing"
        const val readingTextHorizontalPadding = "readingTextHorizontalPadding"
        const val readingTextBold = "readingTextBold"
        const val readingTextAlign = "readingTextAlign"
        const val readingTitleAlign = "readingTitleAlign"
        const val readingSubheadAlign = "readingSubheadAlign"
        const val readingTheme = "readingTheme"
        const val readingFonts = "readingFonts"
        const val readingAutoHideToolbar = "readingAutoHideToolbar"
        const val readingTitleBold = "readingTitleBold"
        const val readingSubheadBold = "readingSubheadBold"
        const val readingTitleUpperCase = "readingTitleUpperCase"
        const val readingSubheadUpperCase = "readingSubheadUpperCase"
        const val readingImageMaximize = "readingImageMaximize"
        const val readingImageHorizontalPadding = "readingImageHorizontalPadding"
        const val readingImageRoundedCorners = "readingImageRoundedCorners"

        // Interaction
        const val initialPage = "initialPage"
        const val initialFilter = "initialFilter"
        const val swipeStartAction = "swipeStartAction"
        const val swipeEndAction = "swipeEndAction"
        const val markAsReadOnScroll = "markAsReadOnScroll"
        const val hideEmptyGroups = "hideEmptyGroups"
        const val pullToLoadNextFeed = "pullToLoadNextFeed"
        const val pullToSwitchArticle = "pullToSwitchArticle"
        const val openLink = "openLink"
        const val openLinkAppSpecificBrowser = "openLinkAppSpecificBrowser"
        const val sharedContent = "sharedContent"

        // Languages
        const val languages = "languages"

        private val keyList =
            listOf(
                BooleanKey(isFirstLaunch),
                StringKey(newVersionPublishDate),
                StringKey(newVersionLog),
                StringKey(newVersionSizeString),
                StringKey(newVersionDownloadUrl),
                StringKey(newVersionNumber),
                StringKey(skipVersionNumber),
                IntKey(currentAccountId),
                IntKey(currentAccountType),
                IntKey(themeIndex),
                StringKey(customPrimaryColor),
                IntKey(darkTheme),
                BooleanKey(amoledDarkTheme),
                IntKey(basicFonts),
                IntKey(feedsFilterBarStyle),
                IntKey(feedsFilterBarPadding),
                IntKey(feedsFilterBarTonalElevation),
                IntKey(feedsTopBarTonalElevation),
                BooleanKey(feedsGroupListExpand),
                IntKey(feedsGroupListTonalElevation),
                IntKey(flowFilterBarStyle),
                IntKey(flowFilterBarPadding),
                IntKey(flowFilterBarTonalElevation),
                IntKey(flowTopBarTonalElevation),
                BooleanKey(flowArticleListFeedIcon),
                BooleanKey(flowArticleListFeedName),
                BooleanKey(flowArticleListImage),
                BooleanKey(flowArticleListDesc),
                BooleanKey(flowArticleListTime),
                BooleanKey(flowArticleListDateStickyHeader),
                IntKey(flowArticleListTonalElevation),
                IntKey(flowArticleListReadIndicator),
                BooleanKey(flowSortUnreadArticles),
                IntKey(readingRenderer),
                BooleanKey(readingBoldCharacters),
                IntKey(readingPageTonalElevation),
                IntKey(readingTextFontSize),
                FloatKey(readingTextLineHeight),
                FloatKey(readingTextLetterSpacing),
                IntKey(readingTextHorizontalPadding),
                BooleanKey(readingTextBold),
                IntKey(readingTextAlign),
                IntKey(readingTitleAlign),
                IntKey(readingSubheadAlign),
                IntKey(readingTheme),
                IntKey(readingFonts),
                BooleanKey(readingAutoHideToolbar),
                BooleanKey(readingTitleBold),
                BooleanKey(readingSubheadBold),
                BooleanKey(readingTitleUpperCase),
                BooleanKey(readingSubheadUpperCase),
                BooleanKey(readingImageMaximize),
                IntKey(readingImageHorizontalPadding),
                IntKey(readingImageRoundedCorners),
                IntKey(initialPage),
                IntKey(initialFilter),
                IntKey(swipeStartAction),
                IntKey(swipeEndAction),
                BooleanKey(markAsReadOnScroll),
                BooleanKey(hideEmptyGroups),
                BooleanKey(pullToLoadNextFeed),
                BooleanKey(pullToSwitchArticle),
                IntKey(openLink),
                StringKey(openLinkAppSpecificBrowser),
                IntKey(sharedContent),
                IntKey(languages),
            )

        val keys = keyList.associateBy { it.name }
    }
}
