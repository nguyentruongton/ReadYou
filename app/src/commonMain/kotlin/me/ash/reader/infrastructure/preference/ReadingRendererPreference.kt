package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingRenderer
import me.ash.reader.ui.ext.put

val LocalReadingRenderer =
    compositionLocalOf<ReadingRendererPreference> { ReadingRendererPreference.default }

sealed class ReadingRendererPreference(val value: Int) : Preference() {
    object WebView : ReadingRendererPreference(0)
    object NativeComponent : ReadingRendererPreference(1)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingRenderer, value)
        }
    }

    

    companion object {

        val default = WebView
        val values = listOf(WebView, NativeComponent)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingRenderer]?.key as Preferences.Key<Int>]) {
                0 -> WebView
                1 -> NativeComponent
                else -> default
            }
    }
}
