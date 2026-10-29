package me.ash.reader.infrastructure.android

import android.content.Context
import java.util.Date
import me.ash.reader.infrastructure.platform.StringsHelper
import me.ash.reader.ui.ext.formatAsString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class AndroidStringsHelper(
    private val context: Context,
) : StringsHelper {

    override suspend fun get(res: StringResource, vararg formatArgs: Any): String =
        getString(res, *formatArgs)

    fun getString(resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)

    fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any): String =
        context.resources.getQuantityString(resId, quantity, *formatArgs)

    override fun formatAsString(
        date: Date?,
        onlyHourMinute: Boolean?,
        atHourMinute: Boolean?,
    ): String = date?.formatAsString(context, onlyHourMinute, atHourMinute) ?: ""
}
