package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.ash.reader.*
import me.ash.reader.domain.model.general.MarkAsReadConditions
import me.ash.reader.ui.theme.palette.alwaysLight

@Composable
fun MarkAsReadBar(
    onItemClick: (MarkAsReadConditions) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MarkAsReadBarItem(
            modifier = Modifier.width(56.dp),
            text = stringResource(Res.string.seven_days),
        ) {
            onItemClick(MarkAsReadConditions.SevenDays)
        }
        MarkAsReadBarItem(
            modifier = Modifier.width(56.dp),
            text = stringResource(Res.string.three_days),
        ) {
            onItemClick(MarkAsReadConditions.ThreeDays)
        }
        MarkAsReadBarItem(
            modifier = Modifier.width(56.dp),
            text = stringResource(Res.string.one_day),
        ) {
            onItemClick(MarkAsReadConditions.OneDay)
        }
        MarkAsReadBarItem(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.mark_all_as_read),
            isPrimary = true,
        ) {
            onItemClick(MarkAsReadConditions.All)
        }
    }
}

@Composable
fun MarkAsReadBarItem(
    modifier: Modifier = Modifier,
    text: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) {
            MaterialTheme.colorScheme.primaryContainer alwaysLight true
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    textAlign = TextAlign.Center,
                ),
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.onSurface alwaysLight true
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }
    }
    if (!isPrimary) {
        Spacer(modifier = Modifier.width(8.dp))
    }
}
