package me.ash.reader.domain.model.account

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*

@Composable
fun AccountType.toDesc(context: Any? = null): String =
    when (this.id) {
        1 -> stringResource(Res.string.local)
        2 -> stringResource(Res.string.fever)
        3 -> stringResource(Res.string.google_reader)
        4 -> stringResource(Res.string.fresh_rss)
        5 -> stringResource(Res.string.feedly)
        6 -> stringResource(Res.string.inoreader)
        else -> stringResource(Res.string.unknown)
    }

@Stable
@Composable
fun AccountType.toIcon(): Any =
    when (this.id) {
        1 -> Icons.Rounded.RssFeed
        2 -> painterResource(Res.drawable.ic_fever)
        3 -> Icons.Rounded.RssFeed
        4 -> painterResource(Res.drawable.ic_freshrss)
        5 -> painterResource(Res.drawable.ic_feedly)
        6 -> painterResource(Res.drawable.ic_inoreader)
        else -> Icons.Rounded.RssFeed
    }
