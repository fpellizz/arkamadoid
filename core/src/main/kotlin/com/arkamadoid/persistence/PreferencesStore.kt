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

    /** Inserisce uno score nella top-N, ritorna il rank 1-based (0 = fuori top). */
    fun submitScore(initials: String, score: Int, level: Int, top: Int = 10): Int {
        val entry = SaveData.HighScoreEntry(initials, score, level)
        data.highScores.add(entry)
        data.highScores.sortByDescending { it.score }
        if (data.highScores.size > top) {
            data.highScores.subList(top, data.highScores.size).clear()
        }
        save()
        val idx = data.highScores.indexOf(entry)
        return if (idx >= 0) idx + 1 else 0
    }

    companion object {
        const val KEY = "save"
    }
}
