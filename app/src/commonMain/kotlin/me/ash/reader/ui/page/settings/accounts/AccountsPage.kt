package me.ash.reader.ui.page.settings.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import me.ash.reader.*
import me.ash.reader.domain.model.account.toDesc
import me.ash.reader.domain.model.account.toIcon
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun AccountsPage(
    viewModel: AccountViewModel = koinViewModel(),
    onBack: () -> Unit,
    navigateToAddAccount: () -> Unit,
    navigateToAccountDetails: (Int) -> Unit,
) {
    val uiState = viewModel.accountUiState.collectAsStateValue()
    val accounts = viewModel.accounts.collectAsStateValue(initial = emptyList())

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
                    DisplayText(text = stringResource(Res.string.accounts), desc = "")
                    Spacer(modifier = Modifier.height(16.dp))
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.list),
                    )
                }
                accounts.forEach {
                    item {
                        SettingItem(
                            title = it.name,
                            desc = it.type.toDesc(),
                            icon = it.type.toIcon().takeIf { it is ImageVector }?.let { it as ImageVector },
                            iconPainter = it.type.toIcon().takeIf { it is Painter }?.let { it as Painter },
                            onClick = { navigateToAccountDetails(it.id!!) },
                        ) {}
                    }
                }
                item {
                    Tips(text = stringResource(Res.string.accounts_tips))
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.more),
                    )
                    SettingItem(
                        title = stringResource(Res.string.add_accounts),
                        desc = stringResource(Res.string.add_accounts_desc),
                        icon = Icons.Outlined.PersonAdd,
                        onClick = navigateToAddAccount,
                    ) {}
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )
}
