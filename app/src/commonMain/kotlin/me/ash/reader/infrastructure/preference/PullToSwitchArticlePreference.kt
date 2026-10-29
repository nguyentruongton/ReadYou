package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.pullToSwitchArticle
import me.ash.reader.ui.ext.put

val LocalPullToSwitchArticle = compositionLocalOf { PullToSwitchArticlePreference.default }

class PullToSwitchArticlePreference(val value: Boolean) : Preference() {
    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.pullToSwitchArticle, value)
        }
    }

    fun toggle(scope: CoroutineScope) =
        PullToSwitchArticlePreference(!value).put(scope)

    fun toggle(context: Any?, scope: CoroutineScope) = toggle(scope)

    companion object {
        val default = PullToSwitchArticlePreference(true)
        fun fromPreference(preference: Preferences): PullToSwitchArticlePreference {
            return PullToSwitchArticlePreference(
                preference[DataStoreKey.keys[pullToSwitchArticle]?.key as Preferences.Key<Boolean>] ?: return default
            )
        }
    }
}
