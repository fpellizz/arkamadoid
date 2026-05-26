package com.arkamadoid.services

interface PlatformServices {
    val gpgs: GpgsService
    fun vibrate(milliseconds: Int)
    fun exitApp()
}
