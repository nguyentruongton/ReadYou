package me.ash.reader.ui.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.context.GlobalContext

val Context.dataStore: DataStore<Preferences>
    get() = GlobalContext.get().get()

val Context.skipVersionNumber: String
    get() = this.dataStore.get(DataStoreKey.skipVersionNumber) ?: ""
val Context.isFirstLaunch: Boolean
    get() = this.dataStore.get(DataStoreKey.isFirstLaunch) ?: true

@Deprecated("Use AccountService to retrieve the current account")
val Context.currentAccountId: Int
    get() = this.dataStore.get(DataStoreKey.currentAccountId) ?: 1
@Deprecated("Use AccountService to retrieve the current account")
val Context.currentAccountType: Int
    get() = this.dataStore.get(DataStoreKey.currentAccountType) ?: 1

val Context.initialPage: Int
    get() = this.dataStore.get(DataStoreKey.initialPage) ?: 0
val Context.initialFilter: Int
    get() = this.dataStore.get(DataStoreKey.initialFilter) ?: 2

val Context.languages: Int
    get() = this.dataStore.get(DataStoreKey.languages) ?: 0

suspend fun Context.fromDataStoreToJSONString(): String = dataStore.fromDataStoreToJSONString()

suspend fun String.fromJSONStringToDataStore(context: Context) = fromJSONStringToDataStore(context.dataStore)
