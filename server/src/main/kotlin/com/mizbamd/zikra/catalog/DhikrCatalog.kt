package com.mizbamd.zikra.catalog

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

/**
 * Server allowlist for frame arabic+latin. Keep in sync with catalog/dhikr.json
 * (copied onto the classpath from the shared repo file).
 */
object DhikrCatalog {
    private val json = Json { ignoreUnknownKeys = true }
    private val tashkeel = setOf(
        '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650',
        '\u0651', '\u0652', '\u0653', '\u0654', '\u0655', '\u0670',
    )

    private var entries: List<Entry> = emptyList()

    fun load(stream: InputStream) {
        val raw = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        entries = json.decodeFromString<List<Entry>>(raw)
    }

    fun contains(arabic: String, latin: String): Boolean {
        val a = normalizeArabic(arabic)
        val l = normalizeLatin(latin)
        if (a.isBlank() || l.isBlank()) return false
        return entries.any { entry ->
            normalizeArabic(entry.arabic) == a && latinMatches(entry, l)
        }
    }

    fun message(): String = "Choose a dhikr from the list"

    private fun latinMatches(entry: Entry, normalizedLatin: String): Boolean {
        if (normalizeLatin(entry.latin) == normalizedLatin) return true
        return entry.aliases.any { normalizeLatin(it) == normalizedLatin }
    }

    private fun normalizeLatin(raw: String): String =
        raw.lowercase()
            .replace('’', '\'')
            .replace("aa", "a")
            .replace("ee", "i")
            .replace("oo", "u")
            .filter { it in 'a'..'z' }

    private fun normalizeArabic(raw: String): String =
        buildString(raw.length) {
            raw.forEach { ch ->
                when {
                    ch in tashkeel || ch.isWhitespace() || ch == '\u0640' -> Unit
                    ch == 'أ' || ch == 'إ' || ch == 'آ' || ch == 'ٱ' -> append('ا')
                    else -> append(ch)
                }
            }
        }

    @Serializable
    private data class Entry(
        val arabic: String,
        val latin: String,
        val aliases: List<String> = emptyList(),
    )
}
