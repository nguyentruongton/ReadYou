package me.ash.reader.ui.page.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*
import me.ash.reader.infrastructure.preference.LocalNewVersionNumber
import me.ash.reader.infrastructure.preference.LocalSkipVersionNumber
import me.ash.reader.infrastructure.preference.toDisplayName
import me.ash.reader.ui.component.base.Banner
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.getAppVersion
import me.ash.reader.ui.page.settings.tips.UpdateDialog
import me.ash.reader.ui.page.settings.tips.UpdateViewModel
import me.ash.reader.ui.theme.palette.onLight
import java.util.Locale

@Composable
fun SettingsPage(
    updateViewModel: UpdateViewModel = koinViewModel(),
    onBack: () -> Unit,
    navigateToAccounts: () -> Unit,
    navigateToColorAndStyle: () -> Unit,
    navigateToInteraction: () -> Unit,
    navigateToLanguages: () -> Unit,
    navigateToTroubleshooting: () -> Unit,
    navigateToTipsAndSupport: () -> Unit,
) {
    val newVersion = LocalNewVersionNumber.current
    val skipVersion = LocalSkipVersionNumber.current
    val currentVersion by remember { mutableStateOf(getAppVersion()) }

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
                    DisplayText(text = stringResource(Res.string.settings), desc = "")
                }
                item {
                    Box {
                        if (newVersion.whetherNeedUpdate(currentVersion, skipVersion)) {
                            Banner(
                                modifier = Modifier.zIndex(1f),
                                title = stringResource(Res.string.get_new_updates),
                                desc = stringResource(
                                    Res.string.get_new_updates_desc,
                                    newVersion.toString(),
                                ),
                                icon = Icons.Outlined.Lightbulb,
                                action = {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(Res.string.close),
                                    )
                                },
                            ) {
                                updateViewModel.showDialog()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(Res.string.accounts),
                        desc = stringResource(Res.string.accounts_desc),
                        icon = Icons.Outlined.AccountCircle,
                        onClick = navigateToAccounts
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(Res.string.color_and_style),
                        desc = stringResource(Res.string.color_and_style_desc),
                        icon = Icons.Outlined.Palette,
                        onClick = navigateToColorAndStyle
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(Res.string.interaction),
                        desc = stringResource(Res.string.interaction_desc),
                        icon = Icons.Outlined.TouchApp,
                        onClick = navigateToInteraction
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(Res.string.languages),
                        desc = Locale.getDefault().toDisplayName(),
                        icon = Icons.Outlined.Language,
                        onClick = navigateToLanguages
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(Res.string.troubleshooting),
                        desc = stringResource(Res.string.troubleshooting_desc),
                        icon = Icons.Outlined.BugReport,
                        onClick = navigateToTroubleshooting
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(Res.string.tips_and_support),
                        desc = stringResource(Res.string.tips_and_support_desc),
                        icon = Icons.Outlined.TipsAndUpdates,
                        onClick = navigateToTipsAndSupport
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    UpdateDialog()
}
