package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowFilterBarStyle
import me.ash.reader.ui.ext.put

val LocalFlowFilterBarStyle =
    compositionLocalOf<FlowFilterBarStylePreference> { FlowFilterBarStylePreference.default }

sealed class FlowFilterBarStylePreference(val value: Int) : Preference() {
    object Icon : FlowFilterBarStylePreference(0)
    object IconLabel : FlowFilterBarStylePreference(1)
    object IconLabelOnlySelected : FlowFilterBarStylePreference(2)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.flowFilterBarStyle,
                value
            )
        }
    }

    

    companion object {

        val default = Icon
        val values = listOf(Icon, IconLabel, IconLabelOnlySelected)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowFilterBarStyle]?.key as Preferences.Key<Int>]) {
                0 -> Icon
                1 -> IconLabel
                2 -> IconLabelOnlySelected
                else -> default
            }
    }
}
