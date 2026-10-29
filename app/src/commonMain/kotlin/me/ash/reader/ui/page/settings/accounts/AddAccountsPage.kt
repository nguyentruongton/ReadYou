package me.ash.reader.ui.page.settings.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import me.ash.reader.*
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.addition.*
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun AddAccountsPage(
    viewModel: AccountViewModel = koinViewModel(),
    additionViewModel: AdditionViewModel = koinViewModel(),
    onBack: () -> Unit,
    navigateToAccountDetails: (Int) -> Unit,
) {
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
                    DisplayText(text = stringResource(Res.string.add_accounts), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.local),
                    )
                    SettingItem(
                        title = stringResource(Res.string.local),
                        desc = stringResource(Res.string.local_desc),
                        icon = Icons.Rounded.RssFeed,
                        onClick = {
                            additionViewModel.showAddLocalAccountDialog()
                        },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.self_hosted),
                    )
                    SettingItem(
                        title = stringResource(Res.string.fresh_rss),
                        desc = stringResource(Res.string.fresh_rss_desc),
                        iconPainter = painterResource(Res.drawable.ic_freshrss),
                        onClick = {
                            additionViewModel.showAddFreshRSSAccountDialog()
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.google_reader),
                        desc = stringResource(Res.string.google_reader_desc),
                        icon = Icons.Rounded.RssFeed,
                        onClick = {
                            additionViewModel.showAddGoogleReaderAccountDialog()
                        },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.fever),
                        desc = stringResource(Res.string.fever_desc),
                        iconPainter = painterResource(Res.drawable.ic_fever),
                        onClick = {
                            additionViewModel.showAddFeverAccountDialog()
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

    AddLocalAccountDialog(onBack, navigateToAccountDetails)
    AddFeverAccountDialog(onBack, navigateToAccountDetails)
    AddGoogleReaderAccountDialog(onBack, navigateToAccountDetails)
    AddFreshRSSAccountDialog(onBack, navigateToAccountDetails)
}
