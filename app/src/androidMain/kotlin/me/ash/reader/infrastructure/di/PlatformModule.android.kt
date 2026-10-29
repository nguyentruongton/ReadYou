package me.ash.reader.infrastructure.di

import androidx.work.WorkManager
import coil3.ImageLoader
import me.ash.reader.infrastructure.image.ImageLoaderFactory
import me.ash.reader.BuildConfig
import me.ash.reader.domain.service.ReaderWorker
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.domain.service.WidgetUpdateWorker
import me.ash.reader.infrastructure.android.AndroidImageDownloader
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.android.TextToSpeechManager
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.db.createAndroidDatabase
import me.ash.reader.infrastructure.net.createOkHttpClient
import me.ash.reader.infrastructure.platform.AppPaths
import me.ash.reader.infrastructure.platform.createAndroidAppPaths
import me.ash.reader.ui.widget.WidgetRepository
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPaths> { createAndroidAppPaths(androidContext()) }
    single<AndroidDatabase> { createAndroidDatabase(androidContext()) }
    single<OkHttpClient> {
        createOkHttpClient(
            get<AppPaths>(),
            BuildConfig.USER_AGENT_STRING
        )
    }
    single<ImageLoader> {
        ImageLoaderFactory.create(
            context = androidContext(),
            okHttpClient = get(),
            cacheDir = get<AppPaths>().cacheDir,
            extraComponents = {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(coil3.gif.AnimatedImageDecoder.Factory())
                } else {
                    add(coil3.gif.GifDecoder.Factory())
                }
            }
        )
    }
    single<WorkManager> { WorkManager.getInstance(androidContext()) }

    singleOf(::AndroidStringsHelper)
    single<me.ash.reader.infrastructure.platform.StringsHelper> { get<AndroidStringsHelper>() }
    single { NotificationHelper(androidContext(), get(named(IO_DISPATCHER)), get(named(APPLICATION_SCOPE))) }
    single<me.ash.reader.infrastructure.platform.NotificationManager> { get<NotificationHelper>() }
    single { TextToSpeechManager(androidContext(), get(named(APPLICATION_SCOPE))) }
    single<me.ash.reader.infrastructure.platform.TextToSpeechManager> { get<TextToSpeechManager>() }
    single { AndroidImageDownloader(androidContext(), get(named(IO_DISPATCHER)), get()) }
    single<me.ash.reader.infrastructure.platform.ImageDownloader> { get<AndroidImageDownloader>() }
    single<me.ash.reader.infrastructure.platform.SyncScheduler> {
        me.ash.reader.infrastructure.platform.AndroidSyncScheduler(get(), get(named(APPLICATION_SCOPE)))
    }
    single { WidgetRepository(androidContext(), get(named(APPLICATION_SCOPE)), get(), get(), get(), get()) }

    worker { SyncWorker(get(), get(), get(), get(), get()) }
    worker { ReaderWorker(get(), get(), get(), get()) }
    worker { WidgetUpdateWorker(get(), get(), get()) }
}
