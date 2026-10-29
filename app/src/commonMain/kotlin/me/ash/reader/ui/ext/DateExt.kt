package me.ash.reader.ui.ext

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date

object DateFormat {
    val YYYY_MM_DD_HH_MM_SS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val YYYY_MM_DD_DASH_HH_MM_SS = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
    val YYYY_MM_DD_DASH_HH_MM_SS_DASH = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
}

fun Date.toString(format: SimpleDateFormat): String {
    return format.format(this)
}

fun String.parseToDate(
    patterns: Array<String> = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss",
        "yyyyMMdd",
        "yyyy/MM/dd",
        "yyyy年MM月dd日",
        "yyyy MM dd",
    ),
): Date? {
    val df = SimpleDateFormat()
    for (pattern in patterns) {
        df.applyPattern(pattern)
        df.isLenient = false
        val date = df.parse(this, ParsePosition(0))
        if (date != null) {
            return date
        }
    }
    return null
}

fun Date.isFuture(staticDate: Date = Date()): Boolean = this.time > staticDate.time

fun Date.formatAsString(
    onlyHourMinute: Boolean? = false,
    atHourMinute: Boolean? = false,
): String {
    val locale = java.util.Locale.getDefault()
    val df = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL, locale)
    val tf = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, locale)
    return when {
        onlyHourMinute == true -> tf.format(this)
        atHourMinute == true -> "${df.format(this)}, ${tf.format(this)}"
        else -> {
            val now = df.format(Date())
            val cal = java.util.Calendar.getInstance().apply {
                time = Date()
                add(java.util.Calendar.DAY_OF_MONTH, -1)
            }
            val yesterday = df.format(cal.time)
            when (df.format(this)) {
                now -> "Today"
                yesterday -> "Yesterday"
                else -> df.format(this)
            }
        }
    }
}

fun Date.formatAsString(
    context: Any?,
    onlyHourMinute: Boolean? = false,
    atHourMinute: Boolean? = false,
): String = formatAsString(onlyHourMinute, atHourMinute)

