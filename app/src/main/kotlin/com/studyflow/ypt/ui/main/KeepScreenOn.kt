package com.studyflow.ypt.ui.main

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
