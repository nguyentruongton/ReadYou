package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.flowArticleListDesc
import me.ash.reader.ui.ext.put

val LocalFlowArticleListDesc =
    compositionLocalOf<FlowArticleListDescPreference> { FlowArticleListDescPreference.default }

sealed class FlowArticleListDescPreference(val value: Int) : Preference() {
    object NONE : FlowArticleListDescPreference(0)
    object SHORT : FlowArticleListDescPreference(1)
    object LONG : FlowArticleListDescPreference(2)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.flowArticleListDesc,
                value
            )
        }
    }

    @Composable
    fun description(): String {
        return when (this) {
            LONG -> stringResource(Res.string.long_desc)
            NONE -> stringResource(Res.string.none_desc)
            SHORT -> stringResource(Res.string.short_desc)
        }
    }

    companion object {

        val default = SHORT
        val values = listOf(NONE, SHORT, LONG)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[flowArticleListDesc]?.key as Preferences.Key<*>]) {
                0 -> NONE
                1 -> SHORT
                2 -> LONG
                else -> default
            }
    }
}
