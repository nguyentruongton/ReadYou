package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowFilterBarPadding
import me.ash.reader.ui.ext.put

val LocalFlowFilterBarPadding =
    compositionLocalOf { FlowFilterBarPaddingPreference.default }

object FlowFilterBarPaddingPreference {

    const val default = 60

    fun put(scope: CoroutineScope, value: Int) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.flowFilterBarPadding, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Int) = put(scope, value)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[flowFilterBarPadding]?.key as Preferences.Key<Int>] ?: default
}
