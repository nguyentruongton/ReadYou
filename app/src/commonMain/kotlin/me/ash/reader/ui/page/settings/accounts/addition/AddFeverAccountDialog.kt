package me.ash.reader.ui.page.settings.accounts.addition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.koin.compose.viewmodel.koinViewModel
import me.ash.reader.*
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.account.security.FeverSecurityKey
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.component.base.RYOutlineTextField
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AddFeverAccountDialog(
    onBack: () -> Unit,
    onNavigateToAccountDetails: (Int) -> Unit,
    viewModel: AdditionViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.additionUiState.collectAsStateValue()
    val accountUiState = accountViewModel.accountUiState.collectAsStateValue()

    var feverServerUrl by rememberSaveable { mutableStateOf("") }
    var feverUsername by rememberSaveable { mutableStateOf("") }
    var feverPassword by rememberSaveable { mutableStateOf("") }
    var feverClientCertificateAlias by rememberSaveable { mutableStateOf("") }
    val defaultAccountName = stringResource(Res.string.fever)

    RYDialog(
        modifier = Modifier.padding(horizontal = 44.dp),
        visible = uiState.addFeverAccountDialogVisible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            accountViewModel.cancelAdd()
            viewModel.hideAddFeverAccountDialog()
        },
        icon = {
            if (accountUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.ic_fever),
                    contentDescription = stringResource(Res.string.fever),
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.fever),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlineTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = accountUiState.isLoading,
                    value = feverServerUrl,
                    onValueChange = { feverServerUrl = it },
                    label = stringResource(Res.string.server_url),
                    placeholder = "https://demo.freshrss.org/api/fever.php",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlineTextField(
                    modifier = Modifier.fillMaxWidth(),
                    requestFocus = false,
                    readOnly = accountUiState.isLoading,
                    value = feverUsername,
                    onValueChange = { feverUsername = it },
                    label = stringResource(Res.string.username),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(modifier = Modifier.height(10.dp))
                RYOutlineTextField(
                    modifier = Modifier.fillMaxWidth(),
                    requestFocus = false,
                    readOnly = accountUiState.isLoading,
                    value = feverPassword,
                    onValueChange = { feverPassword = it },
                    isPassword = true,
                    label = stringResource(Res.string.password),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(modifier = Modifier.height(10.dp))
                CertificateSelector(
                    value = feverClientCertificateAlias,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    feverClientCertificateAlias = it
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
        confirmButton = {
            TextButton(
                enabled =
                    !accountUiState.isLoading &&
                        feverServerUrl.isNotBlank() &&
                        feverUsername.isNotEmpty() &&
                        feverPassword.isNotEmpty(),
                onClick = {
                    focusManager.clearFocus()
                    accountViewModel.addAccount(
                        Account(
                            type = AccountType.Fever,
                            name = defaultAccountName,
                            securityKey =
                                FeverSecurityKey(
                                        serverUrl = feverServerUrl,
                                        username = feverUsername,
                                        password = feverPassword,
                                        clientCertificateAlias = feverClientCertificateAlias,
                                    )
                                    .toString(),
                        )
                    ) { account, exception ->
                        if (account == null) {
                            showToast(exception?.message ?: "Not valid credentials")
                        } else {
                            viewModel.hideAddFeverAccountDialog()
                            onBack()
                            onNavigateToAccountDetails(account.id!!)
                        }
                    }
                },
            ) {
                Text(stringResource(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    accountViewModel.cancelAdd()
                    viewModel.hideAddFeverAccountDialog()
                }
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
