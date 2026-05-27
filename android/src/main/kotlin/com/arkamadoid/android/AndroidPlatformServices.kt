package com.arkamadoid.android

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.arkamadoid.services.GpgsService
import com.arkamadoid.services.NoopGpgsService
import com.arkamadoid.services.PlatformServices

class AndroidPlatformServices(private val activity: Activity) : PlatformServices {

    override val gpgs: GpgsService =
        if (isGpgsConfigured()) AndroidGpgsService(activity) else NoopGpgsService

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun vibrate(milliseconds: Int) {
        val v = vibrator ?: return
        v.vibrate(VibrationEffect.createOneShot(milliseconds.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun exitApp() {
        activity.finishAndRemoveTask()
    }

    private fun isGpgsConfigured(): Boolean {
        val appId = activity.getString(R.string.gpgs_app_id).trim()
        return appId.isNotEmpty() && appId.any { it != '0' }
    }
}
