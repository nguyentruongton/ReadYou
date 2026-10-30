package me.ash.reader.infrastructure.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.context.GlobalContext

internal fun preferenceDataStore(): DataStore<Preferences> = GlobalContext.get().get()
