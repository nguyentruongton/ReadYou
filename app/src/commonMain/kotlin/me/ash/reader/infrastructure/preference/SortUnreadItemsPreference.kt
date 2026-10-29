package me.ash.reader.infrastructure.preference


import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowSortUnreadArticles
import me.ash.reader.ui.ext.put

val LocalSortUnreadArticles =
    compositionLocalOf<SortUnreadArticlesPreference> { SortUnreadArticlesPreference.default }

sealed class SortUnreadArticlesPreference(val value: Boolean) : Preference() {
    data object Latest : SortUnreadArticlesPreference(false)
    data object Earliest : SortUnreadArticlesPreference(true)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                flowSortUnreadArticles,
                value
            )
        }
    }

    @Composable
    fun description(): String {
        return when (this) {
            Earliest -> stringResource(Res.string.earliest)
            Latest -> stringResource(Res.string.latest)
        }
    }

    companion object {

        val default = Latest
        val values = listOf(Latest, Earliest)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowSortUnreadArticles]?.key as Preferences.Key<Boolean>]) {
                true -> Earliest
                false -> Latest
                else -> default
            }
    }
}