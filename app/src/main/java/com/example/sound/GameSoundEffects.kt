package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object GameSoundEffects {
  private val scope = CoroutineScope(Dispatchers.Default)
  var isMuted: Boolean = false

  fun playJump() {
    if (isMuted) return
    scope.launch {
      playSweep(400.0, 900.0, 120)
    }
  }

  fun playFootstep() {
    if (isMuted) return
    scope.launch {
      playSweep(180.0, 80.0, 50)
      playNoise(40)
    }
  }

  fun playCoin() {
    if (isMuted) return
    scope.launch {
      playTone(987.77, 80) // B5
      playTone(1318.51, 140) // E6
    }
  }

  fun playSiren() {
    if (isMuted) return
    scope.launch {
      playSweep(600.0, 1100.0, 200)
      playSweep(1100.0, 600.0, 200)
    }
  }

  fun playGraffitiSpray() {
    if (isMuted) return
    scope.launch {
      playNoise(220)
    }
  }

  fun playFirework() {
    if (isMuted) return
    scope.launch {
      playSweep(800.0, 200.0, 150)
      playNoise(250)
    }
  }

  fun playNitroBoost() {
    if (isMuted) return
    scope.launch {
      playSweep(150.0, 600.0, 350)
    }
  }

  fun playPoliceWhistle() {
    if (isMuted) return
    scope.launch {
      playTone(2400.0, 90)
      playTone(2800.0, 140)
    }
  }

  fun playCarCrash() {
    if (isMuted) return
    scope.launch {
      playNoise(350)
      playSweep(300.0, 80.0, 200)
    }
  }

  fun playWalkieTalkie() {
    if (isMuted) return
    scope.launch {
      playNoise(60)
      playTone(1760.0, 70)
      playTone(1320.0, 90)
    }
  }

  fun playCarHorn() {
    if (isMuted) return
    scope.launch {
      // Yemeni Dabab Dual-Tone Horn
      playDualTone(440.0, 554.37, 180)
      kotlinx.coroutines.delay(40)
      playDualTone(440.0, 554.37, 280)
    }
  }

  fun playDriftScreech() {
    if (isMuted) return
    scope.launch {
      playSweep(1400.0, 1100.0, 180)
      playNoise(160)
    }
  }

  fun playCarEnter() {
    if (isMuted) return
    scope.launch {
      playTone(350.0, 60)
      kotlinx.coroutines.delay(30)
      playSweep(100.0, 450.0, 150)
    }
  }

  fun playPunch() {
    if (isMuted) return
    scope.launch {
      playSweep(250.0, 70.0, 100)
      playNoise(90)
    }
  }

  fun playRadioBeep() {
    if (isMuted) return
    scope.launch {
      playTone(1200.0, 50)
      playTone(1600.0, 70)
    }
  }

  fun playGameOver() {
    if (isMuted) return
    scope.launch {
      playSweep(450.0, 120.0, 300)
      playNoise(150)
    }
  }

  private fun playDualTone(freq1: Double, freq2: Double, durationMs: Int) {
    try {
      val sampleRate = 22050
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val envelope = (1.0 - (i.toDouble() / numSamples))
        val sample = (sin(2 * Math.PI * freq1 * t) + sin(2 * Math.PI * freq2 * t)) * 0.5
        buffer[i] = (sample * 32767 * envelope * 0.45).toInt().toShort()
      }
      playRawBuffer(buffer, sampleRate)
    } catch (_: Exception) {}
  }

  private fun playTone(freq: Double, durationMs: Int) {
    try {
      val sampleRate = 22050
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val envelope = (1.0 - (i.toDouble() / numSamples)) // fade out
        buffer[i] = (sin(2 * Math.PI * freq * t) * 32767 * envelope * 0.4).toInt().toShort()
      }
      playRawBuffer(buffer, sampleRate)
    } catch (_: Exception) {}
  }

  private fun playSweep(startFreq: Double, endFreq: Double, durationMs: Int) {
    try {
      val sampleRate = 22050
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val currentFreq = startFreq + (endFreq - startFreq) * progress
        val t = i.toDouble() / sampleRate
        val envelope = (1.0 - progress)
        buffer[i] = (sin(2 * Math.PI * currentFreq * t) * 32767 * envelope * 0.4).toInt().toShort()
      }
      playRawBuffer(buffer, sampleRate)
    } catch (_: Exception) {}
  }

  fun playGunshot() {
    if (isMuted) return
    scope.launch {
      playNoise(90)
      playTone(180.0, 60)
    }
  }

  fun playSlingSnap() {
    if (isMuted) return
    scope.launch {
      playSweep(800.0, 300.0, 70)
      playNoise(50)
    }
  }

  fun playSlipBanana() {
    if (isMuted) return
    scope.launch {
      playSweep(300.0, 950.0, 180)
      playSweep(950.0, 200.0, 180)
    }
  }

  fun playVictoryFanfare() {
    if (isMuted) return
    scope.launch {
      playTone(523.25, 120) // C5
      playTone(659.25, 120) // E5
      playTone(783.99, 120) // G5
      playTone(1046.50, 300) // C6
    }
  }

  fun playNoise(durationMs: Int = 200) {
    if (isMuted) return
    scope.launch {
      playNoiseInternal(durationMs)
    }
  }

  fun playDoorCreak() {
    if (isMuted) return
    scope.launch {
      // Antique wooden door creak sound
      playSweep(180.0, 480.0, 140)
      kotlinx.coroutines.delay(20)
      playSweep(420.0, 210.0, 180)
      playNoise(60)
    }
  }

  fun playUnderStairsHide() {
    if (isMuted) return
    scope.launch {
      // Rustle under stone stairs and stealth crouch
      playNoise(90)
      playSweep(260.0, 120.0, 120)
    }
  }

  fun playHeartbeatStealth() {
    if (isMuted) return
    scope.launch {
      // Low heartbeat pulse
      playSweep(95.0, 45.0, 110)
      kotlinx.coroutines.delay(100)
      playSweep(85.0, 40.0, 130)
    }
  }

  fun playStealthEvaded() {
    if (isMuted) return
    scope.launch {
      // Police lost sight - relief chime and stealth bonus
      playTone(523.25, 70) // C5
      playTone(659.25, 70) // E5
      playTone(783.99, 90) // G5
      playTone(1046.50, 180) // C6
    }
  }

  fun playLevelUp() {
    if (isMuted) return
    scope.launch {
      playTone(523.25, 80) // C5
      kotlinx.coroutines.delay(60)
      playTone(659.25, 80) // E5
      kotlinx.coroutines.delay(60)
      playTone(783.99, 100) // G5
      kotlinx.coroutines.delay(80)
      playTone(1046.50, 220) // C6
    }
  }

  fun playEngineStart() {
    if (isMuted) return
    scope.launch {
      playSweep(120.0, 380.0, 180)
      playNoise(100)
    }
  }

  fun playZamilDrumBeat() {
    if (isMuted) return
    scope.launch {
      // Deep resonant Zamil war drum (Boom) + Mirwas rim tap
      playSweep(160.0, 45.0, 140)
      kotlinx.coroutines.delay(70)
      playNoise(40)
    }
  }

  fun playOudNote(freq: Double, durationMs: Int = 220) {
    if (isMuted) return
    scope.launch {
      try {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
          val t = i.toDouble() / sampleRate
          val envelope = Math.exp(-4.5 * (i.toDouble() / numSamples)) // sharp acoustic pluck decay
          // Fundamental + 2nd and 3rd harmonics for authentic wooden body resonance
          val sample = (sin(2 * Math.PI * freq * t) * 0.6 +
            sin(2 * Math.PI * (freq * 2) * t) * 0.25 +
            sin(2 * Math.PI * (freq * 3) * t) * 0.15)
          buffer[i] = (sample * 32767 * envelope * 0.45).toInt().toShort()
        }
        playRawBuffer(buffer, sampleRate)
      } catch (_: Exception) {}
    }
  }

  private fun playNoiseInternal(durationMs: Int) {
    try {
      val sampleRate = 22050
      val numSamples = (durationMs * sampleRate) / 1000
      val buffer = ShortArray(numSamples)
      val random = java.util.Random()
      for (i in 0 until numSamples) {
        val progress = i.toDouble() / numSamples
        val envelope = (1.0 - progress)
        val noise = (random.nextDouble() * 2.0 - 1.0)
        buffer[i] = (noise * 32767 * envelope * 0.3).toInt().toShort()
      }
      playRawBuffer(buffer, sampleRate)
    } catch (_: Exception) {}
  }

  private fun playRawBuffer(buffer: ShortArray, sampleRate: Int) {
    try {
      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(buffer, 0, buffer.size)
      audioTrack.play()
      // Release after playing
      audioTrack.setNotificationMarkerPosition(buffer.size)
      audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(track: AudioTrack?) {
          track?.release()
        }
        override fun onPeriodicNotification(track: AudioTrack?) {}
      })
    } catch (_: Exception) {}
  }
}
