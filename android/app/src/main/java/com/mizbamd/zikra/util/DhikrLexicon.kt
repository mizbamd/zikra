package com.mizbamd.zikra.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Known dhikr/dua pairs so Add/Edit can search the catalog and persist only a pick.
 * Matching is on a stripped latin key (lowercase, letters only) or on Arabic without tashkeel.
 */
data class DhikrPair(
    val arabic: String,
    val latin: String,
)

@Serializable
private data class DhikrCatalogEntry(
    val arabic: String,
    val latin: String,
    val aliases: List<String> = emptyList(),
)

object DhikrLexicon {
    private val json = Json { ignoreUnknownKeys = true }
    private val tashkeel = setOf(
        '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650',
        '\u0651', '\u0652', '\u0653', '\u0654', '\u0655', '\u0670',
    )

    private var catalog: List<DhikrCatalogEntry> = emptyList()
    private var byLatin: Map<String, DhikrPair> = emptyMap()
    private var byArabic: Map<String, DhikrPair> = emptyMap()

    fun loadFromJson(raw: String) {
        catalog = json.decodeFromString<List<DhikrCatalogEntry>>(raw)
        byLatin = buildMap {
            catalog.forEach { item ->
                val pair = DhikrPair(item.arabic, item.latin)
                put(normalizeLatin(item.latin), pair)
                item.aliases.forEach { alias -> put(normalizeLatin(alias), pair) }
            }
        }
        byArabic = buildMap {
            catalog.forEach { item ->
                put(normalizeArabic(item.arabic), DhikrPair(item.arabic, item.latin))
            }
        }
    }

    fun matchPair(arabic: String, latin: String): DhikrPair? {
        val a = normalizeArabic(arabic)
        val l = normalizeLatin(latin)
        if (a.isBlank() || l.isBlank()) return null
        val item = catalog.find { e ->
            normalizeArabic(e.arabic) == a && latinMatches(e, l)
        } ?: return null
        return DhikrPair(item.arabic, item.latin)
    }

    fun searchLatin(raw: String, limit: Int = 8): List<DhikrPair> {
        val key = normalizeLatin(raw)
        if (key.length < 2) return emptyList()
        return rankMatches(key, byLatin, limit)
    }

    fun searchArabic(raw: String, limit: Int = 8): List<DhikrPair> {
        val key = normalizeArabic(raw)
        if (key.length < 2) return emptyList()
        return rankMatches(key, byArabic, limit)
    }

    fun fromLatin(raw: String): DhikrPair? = uniqueStrongMatch(searchLatin(raw))

    fun fromArabic(raw: String): DhikrPair? {
        val key = normalizeArabic(raw)
        if (key.isBlank()) return null
        byArabic[key]?.let { return it }
        return uniqueStrongMatch(searchArabic(raw))
    }

    fun normalizeLatin(raw: String): String =
        raw.lowercase()
            .replace('’', '\'')
            .replace("aa", "a")
            .replace("ee", "i")
            .replace("oo", "u")
            .filter { it in 'a'..'z' }

    fun normalizeArabic(raw: String): String =
        buildString(raw.length) {
            raw.forEach { ch ->
                when {
                    ch in tashkeel || ch.isWhitespace() || ch == '\u0640' -> Unit
                    ch == 'أ' || ch == 'إ' || ch == 'آ' || ch == 'ٱ' -> append('ا')
                    else -> append(ch)
                }
            }
        }

    private fun latinMatches(entry: DhikrCatalogEntry, normalizedLatin: String): Boolean {
        if (normalizeLatin(entry.latin) == normalizedLatin) return true
        return entry.aliases.any { normalizeLatin(it) == normalizedLatin }
    }

    private fun uniqueStrongMatch(matches: List<DhikrPair>): DhikrPair? =
        matches.singleOrNull()

    private fun rankMatches(
        key: String,
        index: Map<String, DhikrPair>,
        limit: Int,
    ): List<DhikrPair> {
        data class Hit(val pair: DhikrPair, val score: Int, val matchedKeyLen: Int)

        val best = LinkedHashMap<String, Hit>()
        val allowContains = key.length >= 4
        index.forEach { (mapKey, pair) ->
            val score = when {
                mapKey == key -> 0
                mapKey.startsWith(key) -> 1
                key.startsWith(mapKey) && mapKey.length >= 4 -> 2
                allowContains && mapKey.contains(key) -> 3
                else -> return@forEach
            }
            val id = normalizeArabic(pair.arabic)
            val hit = Hit(pair, score, mapKey.length)
            val prev = best[id]
            if (prev == null ||
                hit.score < prev.score ||
                (hit.score == prev.score && hit.matchedKeyLen < prev.matchedKeyLen)
            ) {
                best[id] = hit
            }
        }
        return best.values
            .sortedWith(
                compareBy<Hit> { it.score }
                    .thenBy { normalizeLatin(it.pair.latin).length }
                    .thenBy { it.pair.latin },
            )
            .take(limit)
            .map { it.pair }
    }
}
