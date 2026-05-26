package com.arkamadoid.persistence

import com.badlogic.gdx.Gdx

class PreferencesStore {
    private val prefs = Gdx.app.getPreferences("arkamadoid")

    var data: SaveData = load()

    private fun load(): SaveData {
        // TODO: deserialize from prefs (JSON blob) into SaveData
        return SaveData()
    }

    fun save() {
        // TODO: serialize SaveData and flush
        prefs.flush()
    }
}
