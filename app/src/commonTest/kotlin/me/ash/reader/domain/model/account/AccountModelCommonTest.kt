package me.ash.reader.domain.model.account

import me.ash.reader.domain.model.account.security.DESUtils
import me.ash.reader.infrastructure.preference.SyncBlockListPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountModelCommonTest {
    @Test
    fun desUtilsDecryptsLegacyEmptyAndTrailingNewlineCiphertexts() {
        assertEquals("{}", DESUtils.decrypt(DESUtils.empty))
        assertEquals("{}", DESUtils.decrypt("CvJ1PKM8EW8=\n"))
        val encrypted = DESUtils.encrypt("secret-token-123")
        assertEquals("secret-token-123", DESUtils.decrypt(encrypted))
    }

    @Test
    fun syncBlockListConvertersRoundTrip() {
        val converters = SyncBlockListConverters()
        val raw = converters.fromBlockList(listOf("ads", "  sponsor  ", ""))
        val parsed = converters.toBlockList(raw).map { it.trim().trimEnd(',') }.filter { it.isNotEmpty() }
        assertEquals(listOf("ads", "sponsor"), parsed)
    }
}
