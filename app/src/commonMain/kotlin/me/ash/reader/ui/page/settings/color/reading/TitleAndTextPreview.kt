package me.ash.reader.ui.page.settings.color.reading

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import me.ash.reader.*
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.reader.bodyStyle
import me.ash.reader.ui.component.reader.h3Style
import me.ash.reader.ui.component.reader.textHorizontalPadding

@Composable
fun TitleAndTextPreview() {
    val titleBold = LocalReadingTitleBold.current
    val subtitleBold = LocalReadingSubheadBold.current
    val titleUpperCase = LocalReadingTitleUpperCase.current
    val subheadUpperCase = LocalReadingSubheadUpperCase.current
    val titleAlign = LocalReadingTitleAlign.current
    val subtitleAlign = LocalReadingSubheadAlign.current

    val titleString = stringResource(Res.string.title)
    val subheadString = stringResource(Res.string.subhead)
    val titleUpperCaseString = titleString.uppercase()
    val subheadUpperCaseString = subheadString.uppercase()

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = textHorizontalPadding().dp),
            text = if (titleUpperCase.value) titleUpperCaseString else titleString,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = LocalReadingFonts.current.asFontFamily(),
                fontWeight = if (titleBold.value) FontWeight.Bold else FontWeight.Medium,
            ),
            textAlign = titleAlign.toTextAlign(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (subheadUpperCase.value) subheadUpperCaseString else subheadString,
            style = h3Style().copy(textAlign = bodyStyle().textAlign),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = textHorizontalPadding().dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.run {
                merge(lineHeight = if (lineHeight.isSpecified) (lineHeight.value * LocalReadingTextLineHeight.current).sp else TextUnit.Unspecified)
            }) {
            Text(
                text = stringResource(Res.string.preview_article_desc),
                style = bodyStyle(),
                modifier = Modifier.padding(horizontal = textHorizontalPadding().dp)
            )
        }
    }
}
