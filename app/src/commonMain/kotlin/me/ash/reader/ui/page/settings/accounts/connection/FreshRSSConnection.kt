package me.ash.reader.ui.page.settings.accounts.connection

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import me.ash.reader.*
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.security.FreshRSSSecurityKey
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.mask
import me.ash.reader.ui.ext.rememberCertificateChooser
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@Composable
fun LazyItemScope.FreshRSSConnection(
    account: Account,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val securityKey by remember { derivedStateOf { FreshRSSSecurityKey(account.securityKey) } }

    var passwordMask by remember { mutableStateOf(securityKey.password?.mask()) }

    var serverUrlValue by remember { mutableStateOf(securityKey.serverUrl) }
    var usernameValue by remember { mutableStateOf(securityKey.username) }
    var passwordValue by remember { mutableStateOf(securityKey.password) }

    var serverUrlDialogVisible by remember { mutableStateOf(false) }
    var usernameDialogVisible by remember { mutableStateOf(false) }
    var passwordDialogVisible by remember { mutableStateOf(false) }

    val certificateChooser = rememberCertificateChooser { alias ->
        securityKey.clientCertificateAlias = alias
        save(account, viewModel, securityKey)
    }

    LaunchedEffect(securityKey.password) { passwordMask = securityKey.password?.mask() }

    SettingItem(
        title = stringResource(Res.string.server_url),
        desc = securityKey.serverUrl ?: "",
        onClick = { serverUrlDialogVisible = true },
    ) {}
    SettingItem(
        title = stringResource(Res.string.username),
        desc = securityKey.username ?: "",
        onClick = { usernameDialogVisible = true },
    ) {}
    SettingItem(
        title = stringResource(Res.string.password),
        desc = passwordMask,
        onClick = { passwordDialogVisible = true },
    ) {}
    SettingItem(
        title = stringResource(Res.string.client_certificate),
        desc = securityKey.clientCertificateAlias ?: "",
        onClick = certificateChooser,
    ) {}

    TextFieldDialog(
        visible = serverUrlDialogVisible,
        title = stringResource(Res.string.server_url),
        value = serverUrlValue ?: "",
        placeholder = "https://demo.freshrss.org/api/greader.php",
        onValueChange = { serverUrlValue = it },
        onDismissRequest = { serverUrlDialogVisible = false },
        onConfirm = {
            if (securityKey.serverUrl?.isNotBlank() == true) {
                securityKey.serverUrl = serverUrlValue
                save(account, viewModel, securityKey)
                serverUrlDialogVisible = false
            }
        },
    )

    TextFieldDialog(
        visible = usernameDialogVisible,
        title = stringResource(Res.string.username),
        value = usernameValue ?: "",
        onValueChange = { usernameValue = it },
        onDismissRequest = { usernameDialogVisible = false },
        onConfirm = {
            if (securityKey.username?.isNotEmpty() == true) {
                securityKey.username = usernameValue
                save(account, viewModel, securityKey)
                usernameDialogVisible = false
            }
        },
    )

    TextFieldDialog(
        visible = passwordDialogVisible,
        title = stringResource(Res.string.password),
        value = passwordValue ?: "",
        isPassword = true,
        onValueChange = { passwordValue = it },
        onDismissRequest = { passwordDialogVisible = false },
        onConfirm = {
            if (securityKey.password?.isNotEmpty() == true) {
                securityKey.password = passwordValue
                save(account, viewModel, securityKey)
                passwordDialogVisible = false
            }
        },
    )
}

private fun save(account: Account, viewModel: AccountViewModel, securityKey: FreshRSSSecurityKey) {
    account.id?.let { viewModel.update(it) { copy(securityKey = securityKey.toString()) } }
}
