package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListImage
import me.ash.reader.ui.ext.put

val LocalFlowArticleListImage =
    compositionLocalOf<FlowArticleListImagePreference> { FlowArticleListImagePreference.default }

sealed class FlowArticleListImagePreference(val value: Boolean) : Preference() {
    object ON : FlowArticleListImagePreference(true)
    object OFF : FlowArticleListImagePreference(false)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.flowArticleListImage,
                value
            )
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListImage]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowArticleListImagePreference.not(): FlowArticleListImagePreference =
    when (value) {
        true -> FlowArticleListImagePreference.OFF
        false -> FlowArticleListImagePreference.ON
    }
