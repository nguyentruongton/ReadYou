package me.ash.reader.infrastructure.net

import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun OkHttpClient.Builder.configurePlatformSsl(
    clientCertificateAlias: String?,
    trustAllCerts: Boolean
): OkHttpClient.Builder {
    if (!trustAllCerts) return this
    try {
        hostnameVerifier { _, _ -> true }
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        sslSocketFactory(sslContext.socketFactory, trustManager)
    } catch (_: Exception) {}
    return this
}
