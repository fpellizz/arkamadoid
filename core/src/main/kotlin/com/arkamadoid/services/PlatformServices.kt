package com.arkamadoid.services

interface PlatformServices {
    val gpgs: GpgsService
    fun vibrate(milliseconds: Int)
    fun exitApp()

    /**
     * Registra callback per audio focus dell'OS. `onLoss` parte su perdita
     * (telefonata, altra app, cuffie staccate). `onGain` quando il focus
     * viene riacquisito. No-op sulle piattaforme che non lo supportano.
     */
    fun registerAudioFocusCallbacks(onLoss: () -> Unit, onGain: () -> Unit) = Unit
}
