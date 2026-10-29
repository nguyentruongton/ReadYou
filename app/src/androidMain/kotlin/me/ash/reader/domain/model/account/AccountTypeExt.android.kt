package me.ash.reader.domain.model.account

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.painterResource
import me.ash.reader.R

fun AccountType.toDesc(context: Context): String =
    when (this.id) {
        1 -> context.getString(R.string.local)
        2 -> context.getString(R.string.fever)
        3 -> context.getString(R.string.google_reader)
        4 -> context.getString(R.string.fresh_rss)
        5 -> context.getString(R.string.feedly)
        6 -> context.getString(R.string.inoreader)
        else -> context.getString(R.string.unknown)
    }

@Stable
@Composable
fun AccountType.toIcon(): Any =
    when (this.id) {
        1 -> Icons.Rounded.RssFeed
        2 -> painterResource(id = R.drawable.ic_fever)
        3 -> Icons.Rounded.RssFeed
        4 -> painterResource(id = R.drawable.ic_freshrss)
        5 -> painterResource(id = R.drawable.ic_feedly)
        6 -> painterResource(id = R.drawable.ic_inoreader)
        else -> Icons.Rounded.RssFeed
    }
