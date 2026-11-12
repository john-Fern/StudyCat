package com.studyflow.ypt.ui.main

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Mantiene la pantalla del dispositivo encendida mientras [enabled] sea `true`.
 * Combina `View.keepScreenOn` con `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`
 * en la ventana de la Activity para garantizar que el celular no apague la pantalla
 * mientras el cronómetro de estudio está corriendo.
 */
@Composable
fun KeepScreenOn(enabled: Boolean) {
  val currentView = LocalView.current
  DisposableEffect(enabled, currentView) {
    currentView.keepScreenOn = enabled
    val window = currentView.context.findActivity()?.window
    if (enabled) {
      window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
      window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    onDispose {
      currentView.keepScreenOn = false
      window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }
}

/**
 * Reproduce sonido de alarma y vibración cuando finaliza un Pomodoro o un descanso,
 * y los detiene inmediatamente cuando el usuario descarta o responde la alarma.
 */
@Composable
fun PomodoroAlarmEffect(alarm: PomodoroAlarmState?) {
  val context = LocalContext.current
  DisposableEffect(alarm?.id) {
    var ringtone: Ringtone? = null
    var toneGenerator: ToneGenerator? = null
    var vibrator: Vibrator? = null

    if (alarm != null) {
      try {
        val alarmUri =
          RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone =
          RingtoneManager.getRingtone(context, alarmUri)?.apply {
            audioAttributes =
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
              isLooping = true
            }
            play()
          }
      } catch (_: Exception) {
        // Fallback a ToneGenerator si RingtoneManager falla en algún dispositivo
      }

      try {
        toneGenerator =
          ToneGenerator(AudioManager.STREAM_ALARM, 100).apply {
            startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
          }
      } catch (_: Exception) {}

      try {
        vibrator =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
              context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
          } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
          }
        val pattern = longArrayOf(0L, 500L, 300L, 500L, 300L, 700L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(pattern, 0)
        }
      } catch (_: Exception) {}
    }

    onDispose {
      try {
        ringtone?.stop()
      } catch (_: Exception) {}
      try {
        toneGenerator?.release()
      } catch (_: Exception) {}
      try {
        vibrator?.cancel()
      } catch (_: Exception) {}
    }
  }
}

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
