package me.ash.reader.ui.component.reader

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

object BionicReadingHelper {
    private val WORD_REGEX = Regex("\\p{L}+")

    fun computeStemLength(wordLength: Int): Int =
        if (wordLength <= 3) wordLength else kotlin.math.round(wordLength * 0.7f).toInt()

    fun applyBionicReading(text: AnnotatedString, boldSpanStyle: SpanStyle): AnnotatedString {
        if (text.isEmpty()) return text

        val builder = AnnotatedString.Builder(text)

        WORD_REGEX.findAll(text.text).forEach { matchResult ->
            val range = matchResult.range
            val wordLength = range.last - range.first + 1
            val stemLength = computeStemLength(wordLength)
            if (stemLength > 0) {
                builder.addStyle(
                    style = boldSpanStyle,
                    start = range.first,
                    end = range.first + stemLength,
                )
            }
        }

        return builder.toAnnotatedString()
    }
}
