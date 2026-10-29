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
        me.ash.reader.infrastructure.platform.DesktopSyncScheduler(get(org.koin.core.qualifier.named(APPLICATION_SCOPE)))
    }
    single<me.ash.reader.infrastructure.platform.StringsHelper> {
        me.ash.reader.infrastructure.platform.CommonStringsHelper()
    }
    single<me.ash.reader.infrastructure.platform.NotificationManager> {
        object : me.ash.reader.infrastructure.platform.NotificationManager {
            override fun notify(feedWithArticle: me.ash.reader.domain.model.feed.FeedWithArticle) {}
        }
    }
}
