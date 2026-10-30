package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.hideEmptyGroups
import me.ash.reader.ui.ext.put

val LocalHideEmptyGroups =
    compositionLocalOf<HideEmptyGroupsPreference> { HideEmptyGroupsPreference.default }

sealed class HideEmptyGroupsPreference(val value: Boolean) : Preference() {
    data object ON : HideEmptyGroupsPreference(true)
    data object OFF : HideEmptyGroupsPreference(false)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                hideEmptyGroups,
                value
            )
        }
    }

    fun toggle(scope: CoroutineScope) = scope.launch {
        preferenceDataStore().put(
            hideEmptyGroups,
            !value
        )
    }

    fun toggle(context: Any?, scope: CoroutineScope) = toggle(scope)

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[hideEmptyGroups]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}
