package me.ash.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.infrastructure.di.commonModule
import me.ash.reader.infrastructure.di.platformModule
import me.ash.reader.infrastructure.platform.AppPaths
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() = application {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(commonModule, platformModule)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ReadYou Desktop",
    ) {
        MaterialTheme {
            val appPaths = GlobalContext.get().get<AppPaths>()
            val accountDao = GlobalContext.get().get<AccountDao>()
            val accounts by accountDao.queryAllAsFlow().collectAsState(initial = emptyList())

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ReadYou Compose Multiplatform",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Data directory: ${appPaths.dataDir.absolutePath}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Accounts connected: ${accounts.size}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
