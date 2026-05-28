package com.arkamadoid.audio

import com.arkamadoid.persistence.PreferencesStore
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.JsonReader

class AudioManager(private val prefs: PreferencesStore) : Disposable {

    enum class Sfx { BOUNCE, BRICK, POWERUP, LIFE_LOST, GAME_OVER, COIN }

    private val trackFiles: Map<MusicTrack, String>
    private val trackLoops: Map<MusicTrack, Boolean>
    private val musicCache = mutableMapOf<MusicTrack, Music>()
    private val sfxCache = mutableMapOf<Sfx, Sound>()

    private var current: PlayingTrack? = null
    private var pending: PlayingTrack? = null
    private var fadeRemaining: Float = 0f

    private data class PlayingTrack(val track: MusicTrack, val music: Music, var targetVolume: Float)

    init {
        val (files, loops) = loadManifest()
        trackFiles = files
        trackLoops = loops
        loadSfx()
    }

    private fun loadSfx() {
        for (sfx in Sfx.values()) {
            val handle = Gdx.files.internal("audio/sfx/${sfx.name.lowercase()}.ogg")
            if (handle.exists()) sfxCache[sfx] = Gdx.audio.newSound(handle)
        }
    }

    private fun loadManifest(): Pair<Map<MusicTrack, String>, Map<MusicTrack, Boolean>> {
        val handle = Gdx.files.internal("audio/music/manifest.json")
        if (!handle.exists()) return emptyMap<MusicTrack, String>() to emptyMap()
        val root = JsonReader().parse(handle)
        val tracksNode = root["tracks"]
        val loopNode = root["loop"]
        val files = MusicTrack.values().associateWith { tracksNode?.getString(it.name, "") ?: "" }
            .filterValues { it.isNotEmpty() }
        val loops = MusicTrack.values().associateWith { loopNode?.getBoolean(it.name, true) ?: true }
        return files to loops
    }

    fun playSfx(sfx: Sfx, pitch: Float = 1f) {
        val vol = prefs.data.sfxVolume
        if (vol <= 0f) return
        val direct = sfxCache[sfx]
        if (direct != null) {
            direct.play(vol, pitch, 0f)
            return
        }
        // fallback per Sfx senza file .ogg dedicato: riusa una traccia esistente con pitch
        val (fallback, fallbackPitch) = when (sfx) {
            Sfx.GAME_OVER -> Sfx.LIFE_LOST to 0.55f
            Sfx.COIN -> Sfx.BRICK to 1.8f
            else -> return
        }
        sfxCache[fallback]?.play(vol, fallbackPitch * pitch, 0f)
    }

    fun playMusic(track: MusicTrack, fadeSeconds: Float = 0.8f) {
        if (current?.track == track && current?.music?.isPlaying == true) return
        val music = loadOrGet(track) ?: return
        music.isLooping = trackLoops[track] ?: true
        val target = prefs.data.musicVolume
        if (current == null || fadeSeconds <= 0f) {
            current?.music?.stop()
            music.volume = target
            music.play()
            current = PlayingTrack(track, music, target)
            pending = null
            fadeRemaining = 0f
        } else {
            music.volume = 0f
            music.play()
            pending = PlayingTrack(track, music, target)
            fadeRemaining = fadeSeconds
        }
    }

    fun stopMusic(fadeSeconds: Float = 0.4f) {
        val now = current ?: return
        if (fadeSeconds <= 0f) {
            now.music.stop()
            current = null
        } else {
            pending = null
            now.targetVolume = 0f
            fadeRemaining = fadeSeconds
        }
    }

    fun update(delta: Float) {
        if (fadeRemaining <= 0f) return
        fadeRemaining = (fadeRemaining - delta).coerceAtLeast(0f)
        val t = if (fadeRemaining == 0f) 1f else 1f - (fadeRemaining / (fadeRemaining + delta))
        current?.let { it.music.volume = (it.music.volume + (it.targetVolume - it.music.volume) * t).coerceIn(0f, 1f) }
        pending?.let { it.music.volume = (it.music.volume + (it.targetVolume - it.music.volume) * t).coerceIn(0f, 1f) }
        if (fadeRemaining == 0f) {
            current?.takeIf { it.targetVolume == 0f }?.music?.stop()
            pending?.let {
                current?.music?.stop()
                current = it
                pending = null
            }
        }
    }

    /** Applica un nuovo volume a tutte le tracce attive (chiamato quando l'utente cambia volume in Settings). */
    fun applyVolume() {
        val v = prefs.data.musicVolume
        current?.let { it.targetVolume = v; it.music.volume = v }
        pending?.let { it.targetVolume = v }
    }

    private fun loadOrGet(track: MusicTrack): Music? {
        musicCache[track]?.let { return it }
        val file = trackFiles[track] ?: return null
        val handle = Gdx.files.internal("audio/music/$file")
        if (!handle.exists()) {
            Gdx.app.error("AudioManager", "Music file not found: $file")
            return null
        }
        val music = Gdx.audio.newMusic(handle)
        musicCache[track] = music
        return music
    }

    override fun dispose() {
        current?.music?.stop()
        pending?.music?.stop()
        musicCache.values.forEach { it.dispose() }
        musicCache.clear()
        sfxCache.values.forEach { it.dispose() }
        sfxCache.clear()
    }
}
