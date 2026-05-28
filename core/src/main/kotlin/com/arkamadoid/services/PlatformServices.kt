package com.arkamadoid.services

interface PlatformServices {
    val gpgs: GpgsService
    fun vibrate(milliseconds: Int)
    fun exitApp()

    /** Versione applicativa corrente. Su Android = PackageInfo.versionName; altrove "dev". */
    val appVersion: String get() = "dev"

    /**
     * true se l'app è stata installata da uno store (Play Store, ecc.).
     * Quando true il notifier custom di update va disabilitato: ci pensa lo store.
     */
    val isInstalledFromStore: Boolean get() = false

    /** Apre l'URL nel browser di sistema. No-op se non supportato. */
    fun openUrl(url: String) = Unit

    /**
     * Registra callback per audio focus dell'OS. `onLoss` parte su perdita
     * (telefonata, altra app, cuffie staccate). `onGain` quando il focus
     * viene riacquisito. No-op sulle piattaforme che non lo supportano.
     */
    fun registerAudioFocusCallbacks(onLoss: () -> Unit, onGain: () -> Unit) = Unit
}
