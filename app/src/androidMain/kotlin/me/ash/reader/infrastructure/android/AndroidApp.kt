package me.ash.reader.infrastructure.android

import android.app.Application
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.BuildConfig
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.AppService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.di.APPLICATION_SCOPE
import me.ash.reader.infrastructure.di.IO_DISPATCHER
import me.ash.reader.infrastructure.di.commonModule
import me.ash.reader.infrastructure.di.platformModule
import me.ash.reader.ui.ext.del
import me.ash.reader.ui.ext.getLatestApk
import me.ash.reader.ui.ext.isGitHub
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import timber.log.Timber

/** The Application class, initializing Koin and app singletons. */
class AndroidApp : Application(), Configuration.Provider, KoinComponent {

    val accountService: AccountService by inject()
    val appService: AppService by inject()
    val rssService: RssService by inject()
    val applicationScope: CoroutineScope by inject(named(APPLICATION_SCOPE))
    val ioDispatcher: CoroutineDispatcher by inject(named(IO_DISPATCHER))
    val imageLoader: ImageLoader by inject()

    /**
     * When the application startup.
     * 1. Initialize Koin
     * 2. Set the uncaught exception handler
     * 3. Initialize the default account if there is none
     * 4. Synchronize once
     * 5. Check for new version
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@AndroidApp)
            workManagerFactory()
            modules(commonModule, platformModule)
        }

        CrashHandler(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        applicationScope.launch {
            accountInit()
            workerInit()
            checkUpdate()
        }
        Coil.setImageLoader(imageLoader)
    }

    /** Override the [Configuration.Builder] to provide the [KoinWorkerFactory]. */
    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(KoinWorkerFactory())
                .setWorkerCoroutineContext(Dispatchers.IO)
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()

    private suspend fun accountInit() {
        withContext(ioDispatcher) {
            if (accountService.isNoAccount()) {
                launch { accountService.initWithDefaultAccount() }
                    .invokeOnCompletion {
                        rssService.get().doSyncOneTime(accountService.getCurrentAccountId())
                    }
            }
        }
    }

    private suspend fun workerInit() {
        rssService.get().initSync()
    }

    private suspend fun checkUpdate() {
        if (!isGitHub) return
        withContext(ioDispatcher) {
            applicationContext.getLatestApk().let { if (it.exists()) it.del() }
        }
        appService.checkUpdate(showToast = false)
    }
}
