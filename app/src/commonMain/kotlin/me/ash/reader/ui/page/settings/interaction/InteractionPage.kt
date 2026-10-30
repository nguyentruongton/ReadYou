package me.ash.reader.ui.page.settings.interaction

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*
import me.ash.reader.infrastructure.preference.InitialFilterPreference
import me.ash.reader.infrastructure.preference.InitialPagePreference
import me.ash.reader.infrastructure.preference.LocalArticleListSwipeEndAction
import me.ash.reader.infrastructure.preference.LocalArticleListSwipeStartAction
import me.ash.reader.infrastructure.preference.LocalHideEmptyGroups
import me.ash.reader.infrastructure.preference.LocalInitialFilter
import me.ash.reader.infrastructure.preference.LocalInitialPage
import me.ash.reader.infrastructure.preference.LocalMarkAsReadOnScroll
import me.ash.reader.infrastructure.preference.LocalOpenLink
import me.ash.reader.infrastructure.preference.LocalOpenLinkSpecificBrowser
import me.ash.reader.infrastructure.preference.LocalPullToSwitchArticle
import me.ash.reader.infrastructure.preference.LocalSettings
import me.ash.reader.infrastructure.preference.LocalSharedContent
import me.ash.reader.infrastructure.preference.LocalSortUnreadArticles
import me.ash.reader.infrastructure.preference.OpenLinkPreference
import me.ash.reader.infrastructure.preference.PullToLoadNextFeedPreference
import me.ash.reader.infrastructure.preference.SharedContentPreference
import me.ash.reader.infrastructure.preference.SortUnreadArticlesPreference
import me.ash.reader.infrastructure.preference.SwipeEndActionPreference
import me.ash.reader.infrastructure.preference.SwipeStartActionPreference
import me.ash.reader.infrastructure.preference.toDesc
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.getPlatformBrowserList
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun InteractionPage(
    onBack: () -> Unit,
) {
    val initialPage = LocalInitialPage.current
    val initialFilter = LocalInitialFilter.current
    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current
    val markAsReadOnScroll = LocalMarkAsReadOnScroll.current
    val hideEmptyGroups = LocalHideEmptyGroups.current
    val sortUnreadArticles = LocalSortUnreadArticles.current
    val pullToSwitchArticle = LocalPullToSwitchArticle.current
    val openLink = LocalOpenLink.current
    val openLinkSpecificBrowser = LocalOpenLinkSpecificBrowser.current
    val sharedContent = LocalSharedContent.current
    val settings = LocalSettings.current
    val pullToSwitchFeed = settings.pullToSwitchFeed

    val scope = rememberCoroutineScope()
    val isOpenLinkSpecificBrowserItemEnabled = remember(openLink) {
        openLink == OpenLinkPreference.SpecificBrowser
    }
    var initialPageDialogVisible by remember { mutableStateOf(false) }
    var initialFilterDialogVisible by remember { mutableStateOf(false) }
    var swipeStartDialogVisible by remember { mutableStateOf(false) }
    var swipeEndDialogVisible by remember { mutableStateOf(false) }
    var openLinkDialogVisible by remember { mutableStateOf(false) }
    var openLinkSpecificBrowserDialogVisible by remember { mutableStateOf(false) }
    var sharedContentDialogVisible by remember { mutableStateOf(false) }
    var showSortUnreadArticlesDialog by remember { mutableStateOf(false) }
    var showPullToLoadDialog by remember { mutableStateOf(false) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(Res.string.interaction), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.on_start),
                    )
                    SettingItem(
                        title = stringResource(Res.string.initial_page),
                        desc = initialPage.toDesc(),
                        onClick = {
                            initialPageDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.initial_filter),
                        desc = initialFilter.toDesc(),
                        onClick = {
                            initialFilterDialogVisible = true
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))

                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.feeds_page),
                    )
                    SettingItem(
                        title = stringResource(Res.string.hide_empty_groups),
                        onClick = {
                            hideEmptyGroups.toggle(scope)
                        },
                    ) {
                        RYSwitch(activated = hideEmptyGroups.value) {
                            hideEmptyGroups.toggle(scope)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.article_list),
                    )
                    SettingItem(
                        title = stringResource(Res.string.swipe_to_start),
                        desc = swipeToStartAction.desc,
                        onClick = {
                            swipeStartDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.swipe_to_end),
                        desc = swipeToEndAction.desc,
                        onClick = {
                            swipeEndDialogVisible = true
                        },
                    ) {}

                    SettingItem(
                        title = stringResource(Res.string.sort_unread_articles),
                        onClick = {
                            showSortUnreadArticlesDialog = true
                        },
                        desc = sortUnreadArticles.description()
                    ) {}

                    SettingItem(
                        title = stringResource(Res.string.mark_as_read_on_scroll),
                        onClick = {
                            markAsReadOnScroll.toggle(scope)
                        },
                    ) {
                        RYSwitch(activated = markAsReadOnScroll.value) {
                            markAsReadOnScroll.toggle(scope)
                        }
                    }

                    SettingItem(
                        title = stringResource(Res.string.pull_from_bottom),
                        desc = pullToSwitchFeed.description(),
                        onClick = {
                            showPullToLoadDialog = true
                        },
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.reading_page),
                    )
                    SettingItem(
                        title = stringResource(Res.string.pull_to_switch_article),
                        onClick = { pullToSwitchArticle.toggle(scope) }) {
                        RYSwitch(activated = pullToSwitchArticle.value) {
                            pullToSwitchArticle.toggle(scope)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.external_links),
                    )
                    SettingItem(
                        title = stringResource(Res.string.initial_open_app),
                        desc = openLink.toDesc(),
                        onClick = {
                            openLinkDialogVisible = true
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.open_link_specific_browser),
                        desc = openLinkSpecificBrowser.packageName ?: stringResource(Res.string.open_link_specific_browser_not_selected),
                        enabled = isOpenLinkSpecificBrowserItemEnabled,
                        onClick = {
                            if (isOpenLinkSpecificBrowserItemEnabled) {
                                openLinkSpecificBrowserDialogVisible = true
                            }
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))

                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.share),
                    )
                    SettingItem(
                        title = stringResource(Res.string.shared_content),
                        desc = sharedContent.toDesc(),
                        onClick = {
                            sharedContentDialogVisible = true
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    RadioDialog(
        visible = initialPageDialogVisible,
        title = stringResource(Res.string.initial_page),
        options = InitialPagePreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == initialPage,
            ) {
                it.put(scope)
            }
        },
    ) {
        initialPageDialogVisible = false
    }

    RadioDialog(
        visible = initialFilterDialogVisible,
        title = stringResource(Res.string.initial_filter),
        options = InitialFilterPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == initialFilter,
            ) {
                it.put(scope)
            }
        },
    ) {
        initialFilterDialogVisible = false
    }

    RadioDialog(
        visible = swipeStartDialogVisible,
        title = stringResource(Res.string.swipe_to_start),
        options = SwipeStartActionPreference.values.map {
            RadioDialogOption(
                text = it.desc,
                selected = it == swipeToStartAction,
            ) {
                it.put(scope)
            }
        },
    ) {
        swipeStartDialogVisible = false
    }

    RadioDialog(
        visible = swipeEndDialogVisible,
        title = stringResource(Res.string.swipe_to_end),
        options = SwipeEndActionPreference.values.map {
            RadioDialogOption(
                text = it.desc,
                selected = it == swipeToEndAction,
            ) {
                it.put(scope)
            }
        },
    ) {
        swipeEndDialogVisible = false
    }

    RadioDialog(
        visible = openLinkDialogVisible,
        title = stringResource(Res.string.initial_open_app),
        options = OpenLinkPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == openLink,
            ) {
                it.put(scope)
            }
        },
    ) {
        openLinkDialogVisible = false
    }

    val browserList = remember { getPlatformBrowserList() }

    RadioDialog(
        visible = openLinkSpecificBrowserDialogVisible,
        title = stringResource(Res.string.open_link_specific_browser),
        options = browserList.map {
            RadioDialogOption(
                text = it.name,
                selected = it.packageName == openLinkSpecificBrowser.packageName,
            ) {
                openLinkSpecificBrowser.copy(packageName = it.packageName).put(scope)
            }
        },
        onDismissRequest = {
            openLinkSpecificBrowserDialogVisible = false
        }
    )

    RadioDialog(
        visible = sharedContentDialogVisible,
        title = stringResource(Res.string.shared_content),
        options = SharedContentPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == sharedContent,
            ) {
                it.put(scope)
            }
        },
    ) {
        sharedContentDialogVisible = false
    }

    RadioDialog(
        visible = showSortUnreadArticlesDialog,
        title = stringResource(Res.string.sort_unread_articles),
        options = SortUnreadArticlesPreference.values.map {
            RadioDialogOption(
                text = it.description(),
                selected = it == sortUnreadArticles,
            ) {
                it.put(scope)
            }
        },
        onDismissRequest = {
            showSortUnreadArticlesDialog = false
        }
    )

    RadioDialog(
        visible = showPullToLoadDialog,
        title = stringResource(Res.string.pull_from_bottom),
        options = PullToLoadNextFeedPreference.values.map {
            RadioDialogOption(
                text = it.description(),
                selected = it == pullToSwitchFeed,
            ) {
                it.put(scope)
            }
        },
        onDismissRequest = {
            showPullToLoadDialog = false
        }
    )
}
