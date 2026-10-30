package me.ash.reader.ui.page.settings.troubleshooting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReportGmailerrorred
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import java.util.Date
import me.ash.reader.*
import me.ash.reader.domain.data.Log
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.DateFormat
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.getAppVersion
import me.ash.reader.ui.ext.openPlatformUrl
import me.ash.reader.ui.ext.rememberFilePicker
import me.ash.reader.ui.ext.rememberFileSaver
import me.ash.reader.ui.ext.toString
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TroubleshootingPage(onBack: () -> Unit, viewModel: TroubleshootingViewModel = koinViewModel()) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val uiState = viewModel.troubleshootingUiState.collectAsStateValue()
    var byteArray by remember { mutableStateOf(ByteArray(0)) }

    val syncLogList = remember { mutableStateListOf<Log>() }

    LaunchedEffect(viewModel) { viewModel.getSyncLogs().let { syncLogList.addAll(it) } }

    var exportBytesToSave by remember { mutableStateOf<ByteArray?>(null) }
    val exportLauncher = rememberFileSaver(
        defaultName = "Read-You-${getAppVersion()}-settings-${Date().toString(DateFormat.YYYY_MM_DD_DASH_HH_MM_SS_DASH)}.json",
        onFileSave = { exportBytesToSave }
    )

    val importLauncher = rememberFilePicker { bytes ->
        byteArray = bytes
        viewModel.tryImport(bytes)
    }

    RYScaffold(
        containerColor =
            MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack,
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(Res.string.troubleshooting), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                    Banner(
                        title = stringResource(Res.string.bug_report),
                        icon = Icons.Outlined.Info,
                        action = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = stringResource(Res.string.go_to),
                            )
                        },
                    ) {
                        openPlatformUrl("https://github.com/Ashinch/ReadYou/issues")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.app_preferences),
                    )
                    SettingItem(
                        title = stringResource(Res.string.import_from_json),
                        onClick = { importLauncher() },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.export_as_json),
                        onClick = {
                            viewModel.exportPreferencesAsJSON { bytes ->
                                exportBytesToSave = bytes
                                exportLauncher()
                            }
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                if (syncLogList.isNotEmpty()) {
                    item {
                        Subtitle(
                            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
                            text = "Sync errors",
                        )
                    }
                    items(syncLogList) { SyncLogItem(log = it) }
                    item {
                        Button(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                            onClick = {
                                viewModel.clearSyncLogs()
                                syncLogList.clear()
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            },
                            shapes = ButtonDefaults.shapes(),
                        ) {
                            Text(stringResource(Res.string.clear))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(
                        modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)
                    )
                }
            }
        },
    )

    RYDialog(
        visible = uiState.warningDialogVisible,
        onDismissRequest = { viewModel.hideWarningDialog() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.ReportGmailerrorred,
                contentDescription = stringResource(Res.string.import_from_json),
            )
        },
        title = { Text(text = stringResource(Res.string.import_from_json)) },
        text = { Text(text = stringResource(Res.string.invalid_json_file_warning)) },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.hideWarningDialog()
                    viewModel.importPreferencesFromJSON(byteArray)
                }
            ) {
                Text(text = stringResource(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.hideWarningDialog() }) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SyncLogItem(log: Log) {
    val clipboard = LocalClipboard.current
    var showFull by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clickable { showFull = !showFull }
    ) {
        Text(
            text = log.fileName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = log.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (showFull) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
