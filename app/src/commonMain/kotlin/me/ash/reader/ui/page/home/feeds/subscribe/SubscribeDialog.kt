package me.ash.reader.ui.page.home.feeds.subscribe

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.koin.compose.viewmodel.koinViewModel
import me.ash.reader.*
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.RenameDialog
import me.ash.reader.ui.component.base.ClipboardTextField
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.rememberFilePicker
import me.ash.reader.ui.ext.roundClick
import me.ash.reader.ui.page.home.feeds.FeedOptionView

@OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SubscribeDialog(
    subscribeViewModel: SubscribeViewModel = koinViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val subscribeUiState = subscribeViewModel.subscribeUiState.collectAsStateValue()
    val subscribeState = subscribeViewModel.subscribeState.collectAsStateValue()

    val filePicker = rememberFilePicker { bytes ->
        subscribeViewModel.importFromInputStream(bytes.inputStream())
    }

    if (subscribeState is SubscribeState.Visible) {

        DisposableEffect(Unit) {
            onDispose {
                subscribeViewModel.cancelSearch()
            }
        }

        AlertDialog(
            modifier = Modifier.padding(horizontal = 44.dp),
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                focusManager.clearFocus()
                subscribeViewModel.hideDrawer()
            },
            icon = {
                val iconUrl = when (subscribeState) {
                    is SubscribeState.Configure -> subscribeState.searchedFeed.icon?.url
                    else -> null
                }
                FeedIcon(
                    feedName = null,
                    iconUrl = iconUrl,
                    placeholderIcon = Icons.Rounded.RssFeed,
                )
            },
            title = {
                Text(
                    modifier = Modifier.roundClick {
                        if (subscribeState is SubscribeState.Configure) {
                            subscribeViewModel.showRenameDialog()
                        }
                    },
                    text = when (subscribeState) {
                        is SubscribeState.Configure -> subscribeState.searchedFeed.title
                        is SubscribeState.Fetching -> stringResource(Res.string.searching)
                        is SubscribeState.Idle -> stringResource(Res.string.subscribe)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            text = {
                AnimatedContent(
                    targetState = subscribeState,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()) using null
                    },
                    contentKey = { it is SubscribeState.Configure }
                ) { state ->
                    when (state) {
                        is SubscribeState.Input -> {
                            val errorText = when (state) {
                                is SubscribeState.Fetching -> ""
                                is SubscribeState.Idle -> state.errorMessage ?: ""
                            }

                            ClipboardTextField(
                                state = state.linkState,
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = state is SubscribeState.Fetching,
                                placeholder = stringResource(Res.string.feed_or_site_url),
                                errorText = errorText,
                                imeAction = ImeAction.Search,
                                onConfirm = {
                                    subscribeViewModel.searchFeed()
                                },
                            )
                        }

                        is SubscribeState.Configure -> {
                            FeedOptionView(
                                link = state.feedLink,
                                groups = state.groups,
                                selectedAllowNotificationPreset = state.notification,
                                selectedParseFullContentPreset = state.fullContent,
                                selectedOpenInBrowserPreset = state.browser,
                                selectedGroupId = state.selectedGroupId,
                                allowNotificationPresetOnClick = {
                                    subscribeViewModel.toggleAllowNotificationPreset()
                                },
                                parseFullContentPresetOnClick = {
                                    subscribeViewModel.toggleParseFullContentPreset()
                                },
                                openInBrowserPresetOnClick = {
                                    subscribeViewModel.toggleOpenInBrowserPreset()
                                },
                                onGroupClick = {
                                    subscribeViewModel.selectedGroup(it)
                                },
                                onAddNewGroup = {
                                    subscribeViewModel.showNewGroupDialog()
                                },
                            )
                        }

                        SubscribeState.Hidden -> {}
                    }
                }
            },
            confirmButton = {
                when (subscribeState) {
                    is SubscribeState.Configure -> {
                        TextButton(
                            onClick = {
                                focusManager.clearFocus()
                                subscribeViewModel.subscribe()
                            }
                        ) {
                            Text(stringResource(Res.string.subscribe))
                        }
                    }

                    is SubscribeState.Input -> {
                        val enabled =
                            subscribeState is SubscribeState.Idle && subscribeState.linkState.text.isNotBlank()
                        TextButton(
                            enabled = enabled,
                            onClick = {
                                focusManager.clearFocus()
                                subscribeViewModel.searchFeed()
                            }
                        ) {
                            Text(
                                text = stringResource(Res.string.search),
                            )
                        }
                    }
                }
            },
            dismissButton = {
                if (subscribeState is SubscribeState.Idle && subscribeState.importFromOpmlEnabled) {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            filePicker()
                            subscribeViewModel.hideDrawer()
                        }
                    ) {
                        Text(text = stringResource(Res.string.import_from_opml))
                    }
                } else {
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            subscribeViewModel.hideDrawer()
                        }
                    ) {
                        Text(text = stringResource(Res.string.cancel))
                    }
                }
            },
        )

        RenameDialog(
            visible = subscribeUiState.renameDialogVisible,
            value = subscribeUiState.newName,
            onValueChange = {
                subscribeViewModel.inputNewName(it)
            },
            onDismissRequest = {
                subscribeViewModel.hideRenameDialog()
            },
            onConfirm = {
                subscribeViewModel.renameFeed()
                subscribeViewModel.hideRenameDialog()
            }
        )

        TextFieldDialog(
            visible = subscribeUiState.newGroupDialogVisible,
            title = stringResource(Res.string.create_new_group),
            icon = Icons.Outlined.CreateNewFolder,
            value = subscribeUiState.newGroupContent,
            placeholder = stringResource(Res.string.name),
            onValueChange = {
                subscribeViewModel.inputNewGroup(it)
            },
            onDismissRequest = {
                subscribeViewModel.hideNewGroupDialog()
            },
            onConfirm = {
                subscribeViewModel.addNewGroup()
            }
        )
    }
}
