package me.ash.reader.infrastructure.di

import coil3.ImageLoader
import coil3.PlatformContext
import me.ash.reader.infrastructure.image.ImageLoaderFactory
import java.io.File
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.db.createDesktopDatabase
import me.ash.reader.infrastructure.net.createOkHttpClient
import me.ash.reader.infrastructure.platform.AppPaths
import me.ash.reader.infrastructure.platform.createDesktopAppPaths
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPaths> { createDesktopAppPaths() }
    single<AndroidDatabase> {
        val appPaths: AppPaths = get()
        createDesktopDatabase(File(appPaths.dataDir, "ReadYou.db"))
    }
    single<OkHttpClient> {
        createOkHttpClient(get(), "ReadYou/0.16.2(47)")
    }
    single<ImageLoader> {
        ImageLoaderFactory.create(PlatformContext.INSTANCE, get(), get<AppPaths>().cacheDir)
    }
    single<me.ash.reader.infrastructure.platform.SyncScheduler> {
        val scope = this
        me.ash.reader.infrastructure.platform.DesktopSyncScheduler(
            coroutineScope = get(org.koin.core.qualifier.named(APPLICATION_SCOPE)),
            onSyncRequest = { targetAccountId, feedId, groupId ->
                val rssService: me.ash.reader.domain.service.RssService = scope.get()
                val accountService: me.ash.reader.domain.service.AccountService = scope.get()
                val readerCacheHelper: me.ash.reader.infrastructure.rss.ReaderCacheHelper = scope.get()
                val resolvedAccountId = targetAccountId ?: accountService.getCurrentAccountId()
                val svc = rssService.get(resolvedAccountId)
                svc.sync(accountId = resolvedAccountId, feedId = feedId, groupId = groupId)
                svc.clearKeepArchivedArticles().forEach {
                    readerCacheHelper.deleteCacheFor(articleId = it.id)
                }
            },
        )
    }
    single<me.ash.reader.infrastructure.platform.DesktopShortcutCoordinator> {
        me.ash.reader.infrastructure.platform.DesktopShortcutCoordinator()
    }
    single<me.ash.reader.infrastructure.platform.StringsHelper> {
        me.ash.reader.infrastructure.platform.CommonStringsHelper()
    }
    single<me.ash.reader.infrastructure.platform.NotificationManager> {
        me.ash.reader.infrastructure.platform.DesktopNotificationManager()
    }
    single<me.ash.reader.infrastructure.platform.ImageDownloader> {
        me.ash.reader.infrastructure.platform.DesktopImageDownloader(get(org.koin.core.qualifier.named(IO_DISPATCHER)), get(), get())
    }
    single<me.ash.reader.infrastructure.platform.TextToSpeechManager> {
        me.ash.reader.infrastructure.platform.DesktopTextToSpeechManager()
    }
}
