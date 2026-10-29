package me.ash.reader.infrastructure.di

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
}
