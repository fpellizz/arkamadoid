package com.arkamadoid.desktop

import com.arkamadoid.ArkamadoidGame
import com.arkamadoid.services.GpgsService
import com.arkamadoid.services.NoopGpgsService
import com.arkamadoid.services.PlatformServices
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Arkamadoid (dev)")
        setWindowedMode(360, 640)
        setForegroundFPS(60)
        useVsync(true)
    }
    val platform = object : PlatformServices {
        override val gpgs: GpgsService = NoopGpgsService
        override fun vibrate(milliseconds: Int) {}
        override fun exitApp() { System.exit(0) }
    }
    Lwjgl3Application(ArkamadoidGame(platform), config)
}
