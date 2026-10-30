package me.ash.reader.ui.page.settings.accounts.addition

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.ash.reader.*
import me.ash.reader.ui.ext.rememberCertificateChooser
import me.ash.reader.ui.ext.showToastSuspend
import org.jetbrains.compose.resources.stringResource

@Composable
fun CertificateSelector(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val chooser = rememberCertificateChooser { alias ->
        if (alias == null) {
            scope.launch {
                showToastSuspend("No client certificate found")
            }
        } else {
            onValueChange(alias)
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = chooser) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                )
            }
        },
        placeholder = {
            Text(stringResource(Res.string.client_certificate))
        },
        modifier = modifier.fillMaxWidth()
    )
}
