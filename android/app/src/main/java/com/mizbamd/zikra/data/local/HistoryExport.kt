package com.mizbamd.zikra.data.local

object HistoryExport {
    fun toCsv(history: List<DailyCountEntity>, names: Map<String, String>): String {
        val sb = StringBuilder("date,dhikr,count\n")
        history.filter { it.count > 0 }
            .sortedWith(compareByDescending<DailyCountEntity> { it.date }.thenBy { names[it.frameId] ?: it.frameId })
            .forEach { row ->
                val name = names[row.frameId] ?: row.frameId
                sb.append(row.date).append(',')
                    .append(csvEscape(name)).append(',')
                    .append(row.count).append('\n')
            }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
