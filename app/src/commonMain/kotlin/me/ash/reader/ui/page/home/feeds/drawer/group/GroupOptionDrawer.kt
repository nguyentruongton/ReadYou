package me.ash.reader.ui.page.home.feeds.drawer.group


import me.ash.reader.ui.ext.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import me.ash.reader.*
import me.ash.reader.ui.ext.getDefaultGroupId
import me.ash.reader.domain.model.group.Group
import me.ash.reader.ui.component.RenameDialog
import me.ash.reader.ui.component.base.BottomDrawer
import me.ash.reader.ui.component.base.RYSelectionChip
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.*
import me.ash.reader.ui.interaction.alphaIndicationClickable

@Composable
fun GroupOptionDrawer(
    drawerState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
    viewModel: GroupOptionViewModel = koinViewModel(),
    content: @Composable () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val groupOptionUiState = viewModel.groupOptionUiState.collectAsStateValue()
    val currentRssService = viewModel.rssService.flow().collectAsStateValue()
    val group = groupOptionUiState.group
    val toastString = stringResource(Res.string.rename_toast, groupOptionUiState.newName)

    BackHandler(drawerState.isVisible) {
        scope.launch {
            drawerState.hide()
        }
    }

    BottomDrawer(
        drawerState = drawerState,
        sheetContent = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = group?.name ?: stringResource(Res.string.unknown),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.alphaIndicationClickable() {
                            if (currentRssService.updateSubscription) {
                                viewModel.showRenameDialog()
                            }
                        },
                        text = group?.name ?: stringResource(Res.string.unknown),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.group_option_tips),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))
                    Subtitle(text = stringResource(Res.string.preset))

                    Spacer(modifier = Modifier.height(10.dp))
                    Preset(viewModel, group, currentRssService.deleteSubscription)

                    if (currentRssService.moveSubscription && groupOptionUiState.groups.size != 1) {
                        Spacer(modifier = Modifier.height(26.dp))
                        Subtitle(text = stringResource(Res.string.move_to_group))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (groupOptionUiState.groups.size > 6) {
                            LazyRowGroups(groupOptionUiState, group, viewModel)
                        } else {
                            FlowRowGroups(groupOptionUiState, group, viewModel)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    ) {
        content()
    }

    ClearGroupDialog(
        groupName = group?.name ?: "",
        onConfirm = { scope.launch { drawerState.hide() } })
    DeleteGroupDialog(
        groupName = group?.name ?: "",
        onConfirm = { scope.launch { drawerState.hide() } })
    AllAllowNotificationDialog(
        groupName = group?.name ?: "",
        onConfirm = { scope.launch { drawerState.hide() } })
    AllParseFullContentDialog(
        groupName = group?.name ?: "",
        onConfirm = { scope.launch { drawerState.hide() } })
    AllOpenInBrowserDialog(
        groupName = group?.name ?: "",
        onConfirm = { scope.launch { drawerState.hide() } })
    AllMoveToGroupDialog(
        groupName = group?.name ?: "",
        onConfirm = { scope.launch { drawerState.hide() } })
    RenameDialog(
        visible = groupOptionUiState.renameDialogVisible,
        value = groupOptionUiState.newName,
        onValueChange = {
            viewModel.inputNewName(it)
        },
        onDismissRequest = {
            viewModel.hideRenameDialog()
        },
        onConfirm = {
            viewModel.rename()
            scope.launch { drawerState.hide() }
            showToast(toastString)
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Preset(
    viewModel: GroupOptionViewModel,
    group: Group?,
    canDeleteSubscription: Boolean,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        RYSelectionChip(
            modifier = Modifier,
            content = stringResource(Res.string.allow_notification),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(Res.string.allow_notification),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            viewModel.showAllAllowNotificationDialog()
        }
        RYSelectionChip(
            modifier = Modifier,
            content = stringResource(Res.string.parse_full_content),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Article,
                    contentDescription = stringResource(Res.string.parse_full_content),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            viewModel.showAllParseFullContentDialog()
        }
        RYSelectionChip(
            modifier = Modifier,
            content = stringResource(Res.string.open_in_browser),
            selected = false,
            selectedIcon = {
                Icon(
                    imageVector = Icons.Outlined.OpenInBrowser,
                    contentDescription = stringResource(Res.string.open_in_browser),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp),
                )
            },
        ) {
            viewModel.showAllOpenInBrowserDialog()
        }
        RYSelectionChip(
            modifier = Modifier,
            content = stringResource(Res.string.clear_articles),
            selected = false,
        ) {
            viewModel.showClearDialog()
        }
        if (canDeleteSubscription && group?.id != group?.accountId?.getDefaultGroupId()) {
            RYSelectionChip(
                modifier = Modifier,
                content = stringResource(Res.string.delete_group),
                selected = false,
            ) {
                viewModel.showDeleteDialog()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowGroups(
    groupOptionUiState: GroupOptionUiState,
    group: Group?,
    groupOptionViewModel: GroupOptionViewModel,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        groupOptionUiState.groups.forEach {
            if (it.id != group?.id) {
                RYSelectionChip(
                    modifier = Modifier,
                    content = it.name,
                    selected = false,
                ) {
                    groupOptionViewModel.showAllMoveToGroupDialog(it)
                }
            }
        }
    }
}

@Composable
private fun LazyRowGroups(
    groupOptionUiState: GroupOptionUiState,
    group: Group?,
    groupOptionViewModel: GroupOptionViewModel,
) {
    LazyRow {
        items(groupOptionUiState.groups) {
            if (it.id != group?.id) {
                RYSelectionChip(
                    modifier = Modifier,
                    content = it.name,
                    selected = false,
                ) {
                    groupOptionViewModel.showAllMoveToGroupDialog(it)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}
