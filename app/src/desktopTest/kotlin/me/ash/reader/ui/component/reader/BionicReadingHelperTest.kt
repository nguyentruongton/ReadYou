package me.ash.reader.ui.component.reader

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BionicReadingHelperTest {

    private val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)

    @Test
    fun `computeStemLength bolds full word for 1 to 3 chars and 70 percent rounded for longer words`() {
        assertEquals(1, BionicReadingHelper.computeStemLength(1))
        assertEquals(2, BionicReadingHelper.computeStemLength(2))
        assertEquals(3, BionicReadingHelper.computeStemLength(3))
        assertEquals(3, BionicReadingHelper.computeStemLength(4)) // round(4 * 0.7) = 3
        assertEquals(4, BionicReadingHelper.computeStemLength(5)) // round(5 * 0.7) = 4
        assertEquals(7, BionicReadingHelper.computeStemLength(10)) // round(10 * 0.7) = 7
    }

    @Test
    fun `applyBionicReading preserves text and adds bold spans to ASCII and Vietnamese Unicode words`() {
        val input = AnnotatedString("ReadYou đọc Tiếng Việt, 123!")
        val result = BionicReadingHelper.applyBionicReading(input, boldStyle)

        assertEquals(input.text, result.text)
        // "ReadYou" (0..7, len 7 -> stem 5: 0..5)
        // "đọc" (8..11, len 3 -> stem 3: 8..11)
        // "Tiếng" (12..17, len 5 -> stem 4: 12..16)
        // "Việt" (18..22, len 4 -> stem 3: 18..21)
        val ranges = result.spanStyles.map { it.start to it.end }
        assertTrue(ranges.contains(0 to 5))
        assertTrue(ranges.contains(8 to 11))
        assertTrue(ranges.contains(12 to 16))
        assertTrue(ranges.contains(18 to 21))
    }
}
