package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.openLinkAppSpecificBrowser
import me.ash.reader.ui.ext.put

@Deprecated("Use LocalUriHandler instead")
val LocalOpenLinkSpecificBrowser = compositionLocalOf { OpenLinkSpecificBrowserPreference.default }

data class OpenLinkSpecificBrowserPreference(
    val packageName: String?
    ) : Preference() {

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.openLinkAppSpecificBrowser,
                packageName.orEmpty()
            )
        }
    }

    companion object {
        val default = OpenLinkSpecificBrowserPreference(null)
        fun fromPreferences(preferences: Preferences): OpenLinkSpecificBrowserPreference {
            val packageName = preferences[DataStoreKey.keys[openLinkAppSpecificBrowser]?.key as Preferences.Key<String>]
            return OpenLinkSpecificBrowserPreference(packageName)
        }
    }
}
