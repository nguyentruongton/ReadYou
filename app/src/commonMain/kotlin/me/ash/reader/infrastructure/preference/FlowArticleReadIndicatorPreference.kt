package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListReadIndicator
import me.ash.reader.ui.ext.put

val LocalFlowArticleListReadIndicator =
    compositionLocalOf<FlowArticleReadIndicatorPreference> { FlowArticleReadIndicatorPreference.default }

sealed class FlowArticleReadIndicatorPreference(val value: Int) : Preference() {
    data object ExcludingStarred : FlowArticleReadIndicatorPreference(0)
    data object AllRead : FlowArticleReadIndicatorPreference(1)
    data object None : FlowArticleReadIndicatorPreference(2)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                flowArticleListReadIndicator,
                value
            )
        }
    }

    val description: String
        @Composable get() {
            return when (this) {
                AllRead -> stringResource(Res.string.all_read)
                ExcludingStarred -> stringResource(Res.string.read_excluding_starred)
                None -> stringResource(Res.string.none)
            }
        }

    companion object {

        val default = ExcludingStarred
        val values = listOf(ExcludingStarred, AllRead, None)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListReadIndicator]?.key as Preferences.Key<Int>]) {
                0 -> ExcludingStarred
                1 -> AllRead
                2 -> None
                else -> default
            }

    }
}