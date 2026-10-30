package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.feedsFilterBarStyle
import me.ash.reader.ui.ext.put

val LocalFeedsFilterBarStyle =
    compositionLocalOf<FeedsFilterBarStylePreference> { FeedsFilterBarStylePreference.default }

sealed class FeedsFilterBarStylePreference(val value: Int) : Preference() {
    object Icon : FeedsFilterBarStylePreference(0)
    object IconLabel : FeedsFilterBarStylePreference(1)
    object IconLabelOnlySelected : FeedsFilterBarStylePreference(2)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.feedsFilterBarStyle,
                value
            )
        }
    }

    

    companion object {

        val default = Icon
        val values = listOf(Icon, IconLabel, IconLabelOnlySelected)

        fun fromPreferences(preferences: Preferences): FeedsFilterBarStylePreference =
            when (preferences[DataStoreKey.keys[feedsFilterBarStyle]?.key as Preferences.Key<Int>]) {
                0 -> Icon
                1 -> IconLabel
                2 -> IconLabelOnlySelected
                else -> default
            }
    }
}
