package me.ash.reader.infrastructure.net

import java.io.File
import java.util.concurrent.TimeUnit
import me.ash.reader.infrastructure.platform.AppPaths
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

expect fun OkHttpClient.Builder.configurePlatformSsl(clientCertificateAlias: String?, trustAllCerts: Boolean): OkHttpClient.Builder

fun createOkHttpClient(
    appPaths: AppPaths,
    userAgent: String,
    trustAllCerts: Boolean = true,
    connectTimeoutSecs: Long = 30L,
    readTimeoutSecs: Long = 30L,
    clientCertificateAlias: String? = null,
): OkHttpClient {
    val cacheDirectory = File(appPaths.cacheDir, "http")
    val builder = OkHttpClient.Builder()
        .cache(Cache(cacheDirectory, 10L * 1024L * 1024L))
        .connectTimeout(connectTimeoutSecs, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
        .followRedirects(true)
        .addNetworkInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                )
            }
        })

    builder.configurePlatformSsl(clientCertificateAlias, trustAllCerts)
    return builder.build()
}
