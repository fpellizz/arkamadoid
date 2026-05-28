package com.arkamadoid.android

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager as AndroidAudioManager
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

    private var focusRequest: AudioFocusRequest? = null

    override fun registerAudioFocusCallbacks(onLoss: () -> Unit, onGain: () -> Unit) {
        val sysAudio = activity.getSystemService(Context.AUDIO_SERVICE) as? AndroidAudioManager ?: return
        // se già registrato, abbandona la precedente
        focusRequest?.let { sysAudio.abandonAudioFocusRequest(it) }
        val listener = AndroidAudioManager.OnAudioFocusChangeListener { change ->
            when (change) {
                AndroidAudioManager.AUDIOFOCUS_LOSS,
                AndroidAudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AndroidAudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onLoss()
                AndroidAudioManager.AUDIOFOCUS_GAIN -> onGain()
            }
        }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val req = AudioFocusRequest.Builder(AndroidAudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener(listener)
            .setWillPauseWhenDucked(true)
            .setAcceptsDelayedFocusGain(true)
            .build()
        focusRequest = req
        sysAudio.requestAudioFocus(req)
    }

    private fun isGpgsConfigured(): Boolean {
        val appId = activity.getString(R.string.gpgs_app_id).trim()
        return appId.isNotEmpty() && appId.any { it != '0' }
    }
}
