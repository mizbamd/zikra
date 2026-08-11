package com.mizbamd.zikra.util

/**
 * Known dhikr/dua pairs so Add/Edit can suggest Arabic from transliteration and the reverse.
 * Matching is on a stripped latin key (lowercase, letters only) or on Arabic without tashkeel.
 */
data class DhikrPair(
    val arabic: String,
    val latin: String,
)

object DhikrLexicon {
    private val tashkeel = setOf(
        '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650',
        '\u0651', '\u0652', '\u0653', '\u0654', '\u0655', '\u0670',
    )

    private val pairs = listOf(
        DhikrPair("سبحان الله", "SubhanAllah"),
        DhikrPair("الحمد لله", "Alhamdulillah"),
        DhikrPair("الله أكبر", "Allahu Akbar"),
        DhikrPair("أستغفر الله", "Astaghfirullah"),
        DhikrPair("لا إله إلا الله", "La ilaha illallah"),
        DhikrPair("لا إله إلا الله محمد رسول الله", "La ilaha illallah Muhammadur Rasulullah"),
        DhikrPair("سبحان الله وبحمده", "SubhanAllahi wa bihamdihi"),
        DhikrPair("سبحان الله العظيم", "SubhanAllahil Azeem"),
        DhikrPair("سبحان الله وبحمده سبحان الله العظيم", "SubhanAllahi wa bihamdihi SubhanAllahil Azeem"),
        DhikrPair("لا حول ولا قوة إلا بالله", "La hawla wa la quwwata illa billah"),
        DhikrPair("اللهم صل على محمد", "Allahumma salli ala Muhammad"),
        DhikrPair("اللهم صل على سيدنا محمد", "Allahumma salli ala Sayyidina Muhammad"),
        DhikrPair("صلى الله عليه وسلم", "Sallallahu alayhi wa sallam"),
        DhikrPair("حسبي الله ونعم الوكيل", "Hasbunallahu wa ni'mal wakeel"),
        DhikrPair("رب اغفر لي", "Rabbighfir li"),
        DhikrPair("يا لطيف", "Ya Latif"),
        DhikrPair("لا إله إلا أنت سبحانك إني كنت من الظالمين", "La ilaha illa anta subhanaka inni kuntu minaz-zalimin"),
        DhikrPair("ربنا آتنا في الدنيا حسنة وفي الآخرة حسنة وقنا عذاب النار", "Rabbana atina fid-dunya hasanah wa fil-akhirati hasanah wa qina adhaban-nar"),
        DhikrPair("بسم الله الرحمن الرحيم", "Bismillahir Rahmanir Rahim"),
        DhikrPair("أعوذ بالله من الشيطان الرجيم", "A'udhu billahi min ash-shaytanir rajim"),
    )

    private val latinAliases = mapOf(
        "subhanallah" to 0,
        "subhanalla" to 0,
        "subhanallahi" to 0,
        "alhamdulillah" to 1,
        "alhumdulillah" to 1,
        "alhamdulilah" to 1,
        "alhumdulilah" to 1,
        "allahuakbar" to 2,
        "allahuakber" to 2,
        "allahhuakbar" to 2,
        "astaghfirullah" to 3,
        "astagfirullah" to 3,
        "astaghfirulla" to 3,
        "astagfirulla" to 3,
        "lailahailla" to 4,
        "lailahaillallah" to 4,
        "lailahaillaallah" to 4,
        "lailahaillalah" to 4,
        "lailahaillallahu" to 4,
        "lailahaillallahmuhammadurrasulullah" to 5,
        "subhanallahiwaibihamdihi" to 6,
        "subhanallahiwabihamdihi" to 6,
        "subhanallahilazeem" to 7,
        "subhanallahilazim" to 7,
        "subhanallahilazeeem" to 7,
        "lahawlawalaquwwataillabillah" to 9,
        "lahawlawalaquwatillabillah" to 9,
        "allahummasallialamuhammad" to 10,
        "salallahuawayhiwasallam" to 12,
        "sallallahualayhiwasallam" to 12,
        "hasbunallahunimalwakeel" to 13,
        "hasbiallah" to 13,
        "rabbighfirli" to 14,
        "yalatif" to 15,
        "lailahaillaanta" to 16,
        "lailahaillaantasubhanakainnikuntuminazzalimin" to 16,
        "lailahaillaanthasubhanakainnikuntumminazzalimeen" to 16,
        "lailahaillaantasubhanaka" to 16,
        "rabbanaatinafiddunyahasanah" to 17,
        "bismillahirrahmanirrahim" to 18,
        "bismillah" to 18,
        "audhubillahiminashshaytanirrajim" to 19,
        "audhubillah" to 19,
    )

    private val byLatin: Map<String, DhikrPair> = buildMap {
        pairs.forEach { put(normalizeLatin(it.latin), it) }
        latinAliases.forEach { (alias, index) -> put(alias, pairs[index]) }
        Defaults.signedIn.forEach { def ->
            put(normalizeLatin(def.transliteration), DhikrPair(def.arabic, def.transliteration))
        }
    }

    private val byArabic: Map<String, DhikrPair> = buildMap {
        pairs.forEach { put(normalizeArabic(it.arabic), it) }
        Defaults.signedIn.forEach { def ->
            put(normalizeArabic(def.arabic), DhikrPair(def.arabic, def.transliteration))
        }
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
