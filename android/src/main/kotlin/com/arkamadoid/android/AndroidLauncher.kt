package com.arkamadoid.android

import android.os.Bundle
import android.view.WindowManager
import com.arkamadoid.ArkamadoidGame
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
        }
        val platform = AndroidPlatformServices(this)
        initialize(ArkamadoidGame(platform), config)
    }
}
