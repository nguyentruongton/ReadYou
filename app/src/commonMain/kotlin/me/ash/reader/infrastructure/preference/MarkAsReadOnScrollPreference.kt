package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.markAsReadOnScroll
import me.ash.reader.ui.ext.put

val LocalMarkAsReadOnScroll =
    compositionLocalOf<MarkAsReadOnScrollPreference> { MarkAsReadOnScrollPreference.default }

sealed class MarkAsReadOnScrollPreference(val value: Boolean) : Preference() {
    data object ON : MarkAsReadOnScrollPreference(true)
    data object OFF : MarkAsReadOnScrollPreference(false)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                markAsReadOnScroll,
                value
            )
        }
    }

    fun toggle(scope: CoroutineScope) = scope.launch {
        preferenceDataStore().put(
            markAsReadOnScroll,
            !value
        )
    }

    fun toggle(context: Any?, scope: CoroutineScope) = toggle(scope)

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[markAsReadOnScroll]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}