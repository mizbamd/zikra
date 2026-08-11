package com.mizbamd.zikra.data.local

import java.time.LocalDate

/**
 * Local [DailyCountEntity] rows older than [RETENTION_MONTHS] are pruned on
 * app launch and after a successful sync so phone storage stays small.
 * Server history is unchanged; only the on-device copy is trimmed.
 */
object HistoryRetention {
    const val RETENTION_MONTHS = 24

    fun cutoffDate(today: LocalDate = LocalDate.now()): String =
        today.minusMonths(RETENTION_MONTHS.toLong()).toString()
}
