package me.ash.reader.infrastructure.net

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun OkHttpClient.Builder.configurePlatformSsl(
    clientCertificateAlias: String?,
    trustAllCerts: Boolean
): OkHttpClient.Builder {
    if (clientCertificateAlias.isNullOrBlank() && !trustAllCerts) {
        return this
    }
    try {
        val trustManager = if (trustAllCerts) {
            hostnameVerifier { _, _ -> true }

            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        } else {
            Platform.get().platformTrustManager()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        sslSocketFactory(sslContext.socketFactory, trustManager)
    } catch (_: Exception) {}
    return this
}
