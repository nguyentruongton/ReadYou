package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.sharedContent
import me.ash.reader.ui.ext.orNotEmpty
import me.ash.reader.ui.ext.put

val LocalSharedContent = compositionLocalOf<SharedContentPreference> { SharedContentPreference.default }

sealed class SharedContentPreference(val value: Int) : Preference() {
    object OnlyLink : SharedContentPreference(0)
    object TitleAndLink : SharedContentPreference(1)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.sharedContent,
                value
            )
        }
    }

    companion object {

        val default = OnlyLink
        val values = listOf(OnlyLink, TitleAndLink)

        fun fromPreferences(preferences: Preferences): SharedContentPreference =
            when (preferences[DataStoreKey.keys[sharedContent]?.key as Preferences.Key<Int>]) {
                0 -> OnlyLink
                1 -> TitleAndLink
                else -> default
            }
    }
}
