package me.ash.reader.ui.ext

import android.content.Context
import me.ash.reader.R
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.formatAsString(
    context: Context,
    onlyHourMinute: Boolean? = false,
    atHourMinute: Boolean? = false,
): String {
    val locale = Locale.getDefault()
    val df = DateFormat.getDateInstance(DateFormat.FULL, locale)
    return when {
        onlyHourMinute == true -> {
            this.toTimeString(context = context)
        }

        atHourMinute == true -> {
            context.getString(
                R.string.date_at_time,
                df.format(this),
                this.toTimeString(context = context),
            )
        }

        else -> {
            df.format(this).run {
                when (this) {
                    df.format(Date()) -> context.getString(R.string.today)
                    df.format(
                        Calendar.getInstance().apply {
                            time = Date()
                            add(Calendar.DAY_OF_MONTH, -1)
                        }.time
                    ),
                    -> context.getString(R.string.yesterday)

                    else -> this
                }
            }
        }
    }
}

private fun Date.toTimeString(context: Context): String =
    android.text.format.DateFormat.getTimeFormat(context).format(this)
