package me.ash.reader.infrastructure.android

import android.content.Context
import me.ash.reader.ui.ext.formatAsString
import java.util.Date

class AndroidStringsHelper(
    private val context: Context,
) {

    fun getString(resId: Int, vararg formatArgs: Any) = context.getString(resId, *formatArgs)

    fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any) =
        context.resources.getQuantityString(resId, quantity, *formatArgs)

    fun formatAsString(
        date: Date?,
        onlyHourMinute: Boolean? = false,
        atHourMinute: Boolean? = false,
    ) = date?.formatAsString(context, onlyHourMinute, atHourMinute)
}
