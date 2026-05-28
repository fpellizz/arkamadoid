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
        override val appVersion: String = "dev"
        override val isInstalledFromStore: Boolean = false
        override fun openUrl(url: String) {
            runCatching {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(java.net.URI(url))
                }
            }
        }
    }
    Lwjgl3Application(ArkamadoidGame(platform), config)
}
