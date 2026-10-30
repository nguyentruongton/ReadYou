package me.ash.reader.infrastructure.preference

sealed class KeepArchivedPreference(
    val value: Long,
) {
    object Always : KeepArchivedPreference(0L)
    object For1Day : KeepArchivedPreference(86400000L)
    object For2Days : KeepArchivedPreference(172800000L)
    object For3Days : KeepArchivedPreference(259200000L)
    object For1Week : KeepArchivedPreference(604800000L)
    object For2Weeks : KeepArchivedPreference(1209600000L)
    object For1Month : KeepArchivedPreference(2592000000L)

    companion object {
        val default = For1Month
        val values = listOf(
            Always,
            For1Day,
            For2Days,
            For3Days,
            For1Week,
            For2Weeks,
            For1Month,
        )
    }
}
