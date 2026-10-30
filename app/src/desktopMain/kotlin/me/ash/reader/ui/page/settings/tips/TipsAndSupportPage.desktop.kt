package me.ash.reader.ui.page.settings.tips

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.ash.reader.Res
import me.ash.reader.ic_launcher_pure
import me.ash.reader.read_you
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.getAppVersion
import me.ash.reader.ui.ext.openPlatformUrl
import me.ash.reader.ui.theme.palette.onLight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun TipsAndSupportPage(
    onBack: () -> Unit,
    navigateToLicenseList: () -> Unit,
) {
    val currentVersion = getAppVersion().toString()

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        actions = {
            FeedbackIconButton(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Rounded.Balance,
                contentDescription = "Open Source Licenses",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = navigateToLicenseList
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(36.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic_launcher_pure),
                            contentDescription = stringResource(Res.string.read_you),
                            modifier = Modifier.size(80.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.tertiary,
                            ) {
                                Text(text = currentVersion)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.read_you),
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(onClick = { openPlatformUrl("https://ash7.io/sponsor/") }) {
                            Text("Sponsor")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        OutlinedButton(onClick = { openPlatformUrl("https://github.com/Ashinch/ReadYou") }) {
                            Text("GitHub")
                        }
                    }
                }
            }
        }
    )
}
