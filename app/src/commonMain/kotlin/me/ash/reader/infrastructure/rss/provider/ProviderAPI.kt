package me.ash.reader.infrastructure.rss.provider

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.ash.reader.infrastructure.net.createOkHttpClient
import me.ash.reader.infrastructure.platform.AppPaths
import okhttp3.OkHttpClient
import org.koin.core.context.GlobalContext

abstract class ProviderAPI(clientCertificateAlias: String? = null) {

    constructor(context: Any?, clientCertificateAlias: String?) : this(clientCertificateAlias)

    protected val client: OkHttpClient = runCatching {
        val appPaths: AppPaths = GlobalContext.get().get()
        createOkHttpClient(
            appPaths = appPaths,
            userAgent = "ReadYou",
            clientCertificateAlias = clientCertificateAlias,
        )
    }.getOrElse {
        OkHttpClient.Builder().build()
    }

    protected val gson: Gson = GsonBuilder().create()

    protected inline fun <reified T> toDTO(jsonStr: String): T =
        gson.fromJson(jsonStr, T::class.java)!!
}
