package com.example.sound

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptic feedback (Vibration) manager for Sana'a Chase game events.
 */
object HapticManager {
  private var vibrator: Vibrator? = null
  var isHapticsEnabled: Boolean = true

  fun initialize(context: Context) {
    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  /**
   * Subtle tick for character movement / footsteps / jump
   */
  fun vibrateMovement() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(18)
      }
    } catch (_: Exception) {}
  }

  fun vibrateClick() = vibrateMovement()

  /**
   * Heavy rumble on collision with police or obstacle
   */
  fun vibrateCollision() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 80, 50, 120)
        val amplitudes = intArrayOf(0, 200, 0, 255)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(180)
      }
    } catch (_: Exception) {}
  }

  /**
   * Distinct intense vibration pattern when the player is caught by the police (Busted / Arrested)
   */
  fun vibrateCaughtByPolice() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 120, 60, 180, 60, 300)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 120, 60, 180, 60, 300), -1)
      }
    } catch (_: Exception) {}
  }

  /**
   * Obstacle bump vibration
   */
  fun vibrateObstacleHit() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 60, 40, 90)
        val amplitudes = intArrayOf(0, 180, 0, 220)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 60, 40, 90), -1)
      }
    } catch (_: Exception) {}
  }

  /**
   * Crisp double-tap for collecting power-ups or coins
   */
  fun vibratePowerUpPickup() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 35, 40, 60)
        val amplitudes = intArrayOf(0, 180, 0, 220)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 35, 40, 60), -1)
      }
    } catch (_: Exception) {}
  }

  fun vibratePowerUp() = vibratePowerUpPickup()

  /**
   * Success / Victory celebratory vibration pattern
   */
  fun vibrateSuccess() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 40, 50, 60, 50, 100)
        val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 40, 50, 60, 50, 100), -1)
      }
    } catch (_: Exception) {}
  }

  /**
   * Nitro / Turbo acceleration pulse
   */
  fun vibrateNitroBoost() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 25, 25, 35, 25, 45)
        val amplitudes = intArrayOf(0, 120, 0, 180, 0, 240)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(longArrayOf(0, 25, 25, 35, 25, 45), -1)
      }
    } catch (_: Exception) {}
  }

  /**
   * Soft pulse when entering stealth hiding spot
   */
  fun vibrateStealth() {
    if (!isHapticsEnabled) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(45, 100))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(45)
      }
    } catch (_: Exception) {}
  }
}
