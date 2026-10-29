package me.ash.reader.infrastructure.platform

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

interface StringsHelper {
    suspend fun get(res: StringResource, vararg formatArgs: Any): String
}

class CommonStringsHelper : StringsHelper {
    override suspend fun get(res: StringResource, vararg formatArgs: Any): String {
        return getString(res, *formatArgs)
    }
}
