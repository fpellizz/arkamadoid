package com.arkamadoid.persistence

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

class PreferencesStore {
    private val prefs = Gdx.app.getPreferences("arkamadoid")
    private val json = Json().apply {
        ignoreUnknownFields = true
        setOutputType(JsonWriter.OutputType.json)
    }

    var data: SaveData = load()
        private set

    private fun load(): SaveData {
        val raw = prefs.getString(KEY, "")
        if (raw.isEmpty()) return SaveData()
        return try {
            json.fromJson(SaveData::class.java, raw)
        } catch (e: Throwable) {
            Gdx.app.error("PreferencesStore", "deserialize failed: ${e.message}")
            SaveData()
        }
    }

    fun save() {
        prefs.putString(KEY, json.toJson(data, SaveData::class.java))
        prefs.flush()
    }

    /**
     * Registra il punteggio del DAILY del giorno. Se la data è cambiata, resetta il best
     * a questo score; altrimenti tiene il max. Ritorna il best del giorno post-update.
     */
    fun recordDailyScore(dateKey: String, score: Int): Int {
        if (data.dailyDate != dateKey) {
            data.dailyDate = dateKey
            data.dailyBestScore = score
        } else if (score > data.dailyBestScore) {
            data.dailyBestScore = score
        }
        save()
        return data.dailyBestScore
    }

    /** Best score del giorno se la data combacia, altrimenti 0. */
    fun dailyBestFor(dateKey: String): Int =
        if (data.dailyDate == dateKey) data.dailyBestScore else 0

    /**
     * Inserisce uno score nella top-N della modalità, ritorna il rank 1-based
     * relativo a quella tabella (0 = fuori top).
     */
    fun submitScore(initials: String, score: Int, level: Int, mode: String, top: Int = 10): Int {
        val entry = SaveData.HighScoreEntry(initials, score, level, mode)
        data.highScores.add(entry)
        // sort globale (mode + score desc), poi tronca per ogni mode separatamente
        data.highScores.sortByDescending { it.score }
        val byMode = data.highScores.groupBy { it.mode }
        val keep = mutableListOf<SaveData.HighScoreEntry>()
        for ((_, list) in byMode) keep += list.take(top)
        keep.sortByDescending { it.score }
        data.highScores.clear()
        data.highScores.addAll(keep)
        save()
        // rank nel sotto-insieme della mode
        val rank = data.highScores.filter { it.mode == mode }.indexOf(entry)
        return if (rank >= 0) rank + 1 else 0
    }

    fun highScoresFor(mode: String): List<SaveData.HighScoreEntry> =
        data.highScores.filter { it.mode == mode }.sortedByDescending { it.score }

    companion object {
        const val KEY = "save"
    }
}
