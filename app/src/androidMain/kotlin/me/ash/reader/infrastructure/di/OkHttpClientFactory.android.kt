package me.ash.reader.infrastructure.di

import android.annotation.SuppressLint
import android.content.Context
import android.security.KeyChain
import me.ash.reader.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.platform.Platform
import java.io.File
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager
import javax.net.ssl.X509TrustManager

const val USER_AGENT_STRING = BuildConfig.USER_AGENT_STRING

object UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request()
                .newBuilder()
                .header("User-Agent", USER_AGENT_STRING)
                .build()
        )
    }
}

fun cachingHttpClient(
    context: Context,
    cacheDirectory: File? = null,
    cacheSize: Long = 10L * 1024L * 1024L,
    trustAllCerts: Boolean = true,
    connectTimeoutSecs: Long = 30L,
    readTimeoutSecs: Long = 30L,
    clientCertificateAlias: String? = null,
): OkHttpClient {
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()

    if (cacheDirectory != null) {
        builder.cache(Cache(cacheDirectory, cacheSize))
    }

    builder
        .connectTimeout(connectTimeoutSecs, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
        .followRedirects(true)

    if (!clientCertificateAlias.isNullOrBlank() || trustAllCerts) {
        builder.setupSsl(context, clientCertificateAlias, trustAllCerts)
    }

    return builder.build()
}

fun OkHttpClient.Builder.setupSsl(
    context: Context,
    clientCertificateAlias: String?,
    trustAllCerts: Boolean
) {
    try {
        val clientKeyManager = clientCertificateAlias?.let { clientAlias ->
            object : X509KeyManager {
                override fun getClientAliases(keyType: String?, issuers: Array<Principal>?) =
                    throw UnsupportedOperationException("getClientAliases")

                override fun chooseClientAlias(
                    keyType: Array<String>?,
                    issuers: Array<Principal>?,
                    socket: Socket?
                ) = clientCertificateAlias

                override fun getServerAliases(keyType: String?, issuers: Array<Principal>?) =
                    throw UnsupportedOperationException("getServerAliases")

                override fun chooseServerAlias(
                    keyType: String?,
                    issuers: Array<Principal>?,
                    socket: Socket?
                ) = throw UnsupportedOperationException("chooseServerAlias")

                override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
                    return if (alias == clientAlias) KeyChain.getCertificateChain(context, clientAlias) else null
                }

                override fun getPrivateKey(alias: String?): PrivateKey? {
                    return if (alias == clientAlias) KeyChain.getPrivateKey(context, clientAlias) else null
                }
            }
        }

        val trustManager = if (trustAllCerts) {
            hostnameVerifier { _, _ -> true }

            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        } else {
            Platform.get().platformTrustManager()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf(clientKeyManager), arrayOf(trustManager), null)
        val sslSocketFactory = sslContext.socketFactory

        sslSocketFactory(sslSocketFactory, trustManager)
    } catch (e: NoSuchAlgorithmException) {
        // ignore
    } catch (e: KeyManagementException) {
        // ignore
    }
}
