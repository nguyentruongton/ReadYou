package me.ash.reader.domain.service

import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import me.ash.reader.domain.model.general.toVersion
import me.ash.reader.infrastructure.net.Download
import me.ash.reader.infrastructure.net.NetworkDataSource
import me.ash.reader.infrastructure.net.downloadToFileWithProgress
import me.ash.reader.infrastructure.platform.AppPaths
import me.ash.reader.infrastructure.platform.Log
import me.ash.reader.infrastructure.preference.NewVersionDownloadUrlPreference
import me.ash.reader.infrastructure.preference.NewVersionLogPreference
import me.ash.reader.infrastructure.preference.NewVersionNumberPreference
import me.ash.reader.infrastructure.preference.NewVersionPublishDatePreference
import me.ash.reader.infrastructure.preference.NewVersionSizePreference
import me.ash.reader.infrastructure.preference.NewVersionSizePreference.formatSize
import me.ash.reader.infrastructure.preference.SettingsProvider
import org.koin.core.context.GlobalContext

class AppService(
    private val appPaths: AppPaths,
    private val networkDataSource: NetworkDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
    private val currentVersion: String = "0.16.2",
) {

    constructor(
        context: Any?,
        networkDataSource: NetworkDataSource,
        ioDispatcher: CoroutineDispatcher,
        mainDispatcher: CoroutineDispatcher,
    ) : this(
        GlobalContext.get().get(),
        networkDataSource,
        ioDispatcher,
        mainDispatcher,
        GlobalContext.get().get(),
    )

    suspend fun checkUpdate(showToast: Boolean = true): Boolean? = withContext(ioDispatcher) {
        try {
            val response = networkDataSource.getReleaseLatest("https://api.github.com/repos/Ashinch/ReadYou/releases/latest")
            when {
                response.code() == 403 -> {
                    return@withContext null
                }

                response.body() == null -> {
                    return@withContext null
                }
            }
            val skipVersion = settingsProvider.settings.skipVersionNumber
            val current = currentVersion.toVersion()
            val latest = response.body()!!
            val latestVersion = latest.tag_name.toVersion()
            val latestLog = latest.body ?: ""
            val latestPublishDate = latest.published_at ?: latest.created_at ?: ""
            val latestSize = latest.assets?.first()?.size ?: 0
            val latestDownloadUrl = latest.assets?.first()?.browser_download_url ?: ""

            Log.i("RLog", "current version $current")
            if (latestVersion.whetherNeedUpdate(current, skipVersion)) {
                Log.i("RLog", "new version $latestVersion")
                NewVersionNumberPreference.put(this, latestVersion.toString())
                NewVersionLogPreference.put(this, latestLog)
                NewVersionPublishDatePreference.put(this, latestPublishDate)
                NewVersionSizePreference.put(this, latestSize.formatSize())
                NewVersionDownloadUrlPreference.put(this, latestDownloadUrl)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "checkUpdate: ${e.message}")
            null
        }
    }

    suspend fun downloadFile(url: String): Flow<Download> =
        withContext(ioDispatcher) {
            Log.i("RLog", "downloadFile start: $url")
            try {
                val apkFile = File(appPaths.cacheDir, "update.apk")
                return@withContext networkDataSource.downloadFile(url)
                    .downloadToFileWithProgress(apkFile)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("RLog", "downloadFile: ${e.message}")
            }
            emptyFlow()
        }
}
