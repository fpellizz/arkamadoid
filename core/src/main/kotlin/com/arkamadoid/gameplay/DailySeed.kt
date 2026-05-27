package com.arkamadoid.gameplay

import java.time.LocalDate

/**
 * Genera in modo deterministico il livello del giorno per la modalità DAILY.
 * Tutti i giocatori che lanciano la stessa data ottengono lo stesso livello.
 */
object DailySeed {

    fun today(): LocalDate = LocalDate.now()

    fun dateKey(date: LocalDate = today()): String = date.toString()  // YYYY-MM-DD

    /**
     * Indice del livello 1..maxLevels per la data data, prodotto da un hash mix
     * (SplitMix64) per evitare che date consecutive diano livelli consecutivi.
     */
    fun levelIndex(maxLevels: Int, date: LocalDate = today()): Int {
        if (maxLevels <= 0) return 1
        var h = (date.year * 10000L + date.monthValue * 100L + date.dayOfMonth)
        h = h xor (h ushr 33)
        h *= -0x00aaeaba34f2c2cdL  // 0xff51afd7ed558ccd
        h = h xor (h ushr 33)
        h *= -0x3b314601e57a13adL  // 0xc4ceb9fe1a85ec53
        h = h xor (h ushr 33)
        val mod = (h % maxLevels)
        val idx = if (mod < 0) mod + maxLevels else mod
        return idx.toInt() + 1
    }
}
