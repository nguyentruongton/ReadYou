package me.ash.reader.infrastructure.platform

import java.util.Date
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

interface StringsHelper {
    suspend fun get(res: StringResource, vararg formatArgs: Any): String
    suspend fun getQuantity(res: PluralStringResource, quantity: Int, vararg formatArgs: Any): String
    fun formatAsString(
        date: Date?,
        onlyHourMinute: Boolean? = false,
        atHourMinute: Boolean? = false,
    ): String
}

class CommonStringsHelper : StringsHelper {
    override suspend fun get(res: StringResource, vararg formatArgs: Any): String {
        return getString(res, *formatArgs)
    }

    override suspend fun getQuantity(
        res: PluralStringResource,
        quantity: Int,
        vararg formatArgs: Any,
    ): String {
        return getPluralString(res, quantity, *formatArgs)
    }

    override fun formatAsString(
        date: Date?,
        onlyHourMinute: Boolean?,
        atHourMinute: Boolean?,
    ): String {
        if (date == null) return ""
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        if (onlyHourMinute == true) {
            return timeFormat.format(date)
        }
        val dateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL, java.util.Locale.getDefault())
        if (atHourMinute == true) {
            return "${dateFormat.format(date)} ${timeFormat.format(date)}"
        }
        val todayStr = dateFormat.format(Date())
        val cal = java.util.Calendar.getInstance().apply {
            time = Date()
            add(java.util.Calendar.DAY_OF_MONTH, -1)
        }
        val yesterdayStr = dateFormat.format(cal.time)
        val dateStr = dateFormat.format(date)
        return when (dateStr) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> dateStr
        }
    }
}
