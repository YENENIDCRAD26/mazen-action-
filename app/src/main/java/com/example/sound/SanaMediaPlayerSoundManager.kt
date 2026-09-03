package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

/**
 * Sound Manager utilizing Android MediaPlayer to trigger authentic audio sound effects
 * for chase events, collisions, and score increases in the streets of Sana'a.
 */
class SanaMediaPlayerSoundManager(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.IO)
  var isMuted: Boolean = false
    private set
  private val activePlayers = mutableListOf<MediaPlayer>()
  private var ambientMediaPlayer: MediaPlayer? = null
  private var chaseMusicPlayer: MediaPlayer? = null
  private var isChaseMusicPlaying = false
  private var isAmbientPlaying = false

  fun setMuted(muted: Boolean) {
    isMuted = muted
    if (muted) {
      pauseChaseMusic()
      stopAmbientStreetSounds()
    }
  }

  /**
   * Ambient Sana'a Old City street atmosphere loop using MediaPlayer
   */
  fun playAmbientStreetSounds() {
    if (isMuted || isAmbientPlaying) return
    scope.launch {
      try {
        val sampleRate = 22050
        val durationMs = 2800
        val numSamples = (durationMs * sampleRate) / 1000
        val pcmBuffer = ShortArray(numSamples)

        // Generate warm street bazaar ambiance with subtle breeze and acoustic resonance
        for (i in 0 until numSamples) {
          val t = i.toDouble() / sampleRate
          val wind = sin(2 * Math.PI * 55.0 * t) * 0.18 + sin(2 * Math.PI * 110.0 * t) * 0.12
          val distantBazaar = (sin(2 * Math.PI * 220.0 * t) + sin(2 * Math.PI * 330.0 * t) * 0.7) * 0.15
          val randomJitter = (Math.random() - 0.5) * 0.08
          pcmBuffer[i] = ((wind + distantBazaar + randomJitter) * 32767 * 0.4).toInt().coerceIn(-32768, 32767).toShort()
        }

        val audioFile = File(context.cacheDir, "sanaa_street_ambience.wav")
        writeWavFile(audioFile, pcmBuffer, sampleRate)

        ambientMediaPlayer?.release()
        ambientMediaPlayer = MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          setDataSource(context, Uri.fromFile(audioFile))
          isLooping = true
          setVolume(0.35f, 0.35f)
          prepare()
          start()
        }
        isAmbientPlaying = true
      } catch (_: Exception) {}
    }
  }

  fun stopAmbientStreetSounds() {
    try {
      ambientMediaPlayer?.stop()
      ambientMediaPlayer?.release()
      ambientMediaPlayer = null
      isAmbientPlaying = false
    } catch (_: Exception) {}
  }

  /**
   * Dynamic high-octane Sana'a chase music loop using MediaPlayer
   */
  fun playChaseMusic(isUrgent: Boolean = false) {
    if (isMuted || isChaseMusicPlaying) return
    scope.launch {
      try {
        val sampleRate = 22050
        val durationMs = 3200
        val numSamples = (durationMs * sampleRate) / 1000
        val pcmBuffer = ShortArray(numSamples)

        // Rhythmic adrenaline chase beat with pulsing bassline & tension synth
        val tempo = if (isUrgent) 4.5 else 3.2
        for (i in 0 until numSamples) {
          val t = i.toDouble() / sampleRate
          val beatPhase = (t * tempo) % 1.0
          val kickEnv = Math.exp(-beatPhase * 12.0)
          val kick = sin(2 * Math.PI * (70.0 - beatPhase * 30.0) * t) * kickEnv * 0.55

          // Arpeggiated tension bass notes (D Minor / Hijaz mode vibe: D, Eb, F#, G, A)
          val noteIndex = ((t * tempo * 2).toInt()) % 4
          val baseFreq = when (noteIndex) {
            0 -> 146.83 // D3
            1 -> 155.56 // Eb3
            2 -> 185.00 // F#3
            else -> 146.83
          }
          val bass = sin(2 * Math.PI * baseFreq * t) * 0.35

          // Hi-hat / shaker pulse
          val hatEnv = Math.exp(-((t * tempo * 4) % 1.0) * 18.0)
          val hat = (Math.random() - 0.5) * hatEnv * 0.18

          pcmBuffer[i] = ((kick + bass + hat) * 32767 * 0.5).toInt().coerceIn(-32768, 32767).toShort()
        }

        val audioFile = File(context.cacheDir, "sanaa_chase_music.wav")
        writeWavFile(audioFile, pcmBuffer, sampleRate)

        chaseMusicPlayer?.release()
        chaseMusicPlayer = MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          setDataSource(context, Uri.fromFile(audioFile))
          isLooping = true
          setVolume(0.55f, 0.55f)
          prepare()
          start()
        }
        isChaseMusicPlaying = true
      } catch (_: Exception) {}
    }
  }

  fun pauseChaseMusic() {
    try {
      if (chaseMusicPlayer?.isPlaying == true) {
        chaseMusicPlayer?.pause()
      }
      if (ambientMediaPlayer?.isPlaying == true) {
        ambientMediaPlayer?.pause()
      }
    } catch (_: Exception) {}
  }

  fun resumeChaseMusic() {
    if (isMuted) return
    try {
      if (chaseMusicPlayer != null && !chaseMusicPlayer!!.isPlaying) {
        chaseMusicPlayer?.start()
      } else if (chaseMusicPlayer == null) {
        playChaseMusic()
      }
      if (ambientMediaPlayer != null && !ambientMediaPlayer!!.isPlaying) {
        ambientMediaPlayer?.start()
      }
    } catch (_: Exception) {}
  }

  fun stopChaseMusic() {
    try {
      chaseMusicPlayer?.stop()
      chaseMusicPlayer?.release()
      chaseMusicPlayer = null
      isChaseMusicPlaying = false
    } catch (_: Exception) {}
  }

  /**
   * Sound when caught by police
   */
  fun playCaughtByPoliceSound() {
    if (isMuted) return
    scope.launch {
      playSynthesizedWav("caught_police.wav") { buffer, rate ->
        generateToneSequence(
          buffer,
          rate,
          doubleArrayOf(350.0, 290.0, 220.0, 140.0),
          intArrayOf(100, 100, 120, 220)
        )
      }
    }
  }

  /**
   * Sound effect for Chase Events (Police sirens, alert whistles, turbo whoosh, stealth enter)
   */
  fun playChaseEventSound(eventType: ChaseEventType) {
    if (isMuted) return
    scope.launch {
      when (eventType) {
        ChaseEventType.POLICE_SIREN -> playSynthesizedWav("siren.wav") { buffer, rate ->
          generateSiren(buffer, rate)
        }
        ChaseEventType.POLICE_WHISTLE -> playSynthesizedWav("whistle.wav") { buffer, rate ->
          generateToneSequence(buffer, rate, doubleArrayOf(2400.0, 2800.0), intArrayOf(100, 150))
        }
        ChaseEventType.TURBO_BOOST -> playSynthesizedWav("turbo.wav") { buffer, rate ->
          generateSweep(buffer, rate, 150.0, 750.0)
        }
        ChaseEventType.STEALTH_ENTER -> playSynthesizedWav("stealth.wav") { buffer, rate ->
          generateToneSequence(buffer, rate, doubleArrayOf(220.0, 160.0), intArrayOf(90, 120))
        }
        ChaseEventType.POWER_UP_SPAWN -> playSynthesizedWav("powerup_spawn.wav") { buffer, rate ->
          generateToneSequence(buffer, rate, doubleArrayOf(587.33, 880.0), intArrayOf(80, 120))
        }
      }
    }
  }

  /**
   * Sound effect for Collisions (Car crash, police stun, wall bump)
   */
  fun playCollisionSound(collisionType: CollisionType) {
    if (isMuted) return
    scope.launch {
      when (collisionType) {
        CollisionType.POLICE_BUMP -> playSynthesizedWav("bump.wav") { buffer, rate ->
          generateCrashNoise(buffer, rate, 180)
        }
        CollisionType.HEAVY_CRASH -> playSynthesizedWav("crash.wav") { buffer, rate ->
          generateCrashNoise(buffer, rate, 320)
        }
        CollisionType.STUN_DIZZY -> playSynthesizedWav("stun.wav") { buffer, rate ->
          generateSweep(buffer, rate, 800.0, 200.0)
        }
      }
    }
  }

  /**
   * Sound effect for Score Increases (Coin pickup, Combo pop, Level Up fanfare)
   */
  fun playScoreIncreaseSound(scoreType: ScoreSoundType) {
    if (isMuted) return
    scope.launch {
      when (scoreType) {
        ScoreSoundType.COIN_PICKUP -> playSynthesizedWav("coin.wav") { buffer, rate ->
          generateToneSequence(buffer, rate, doubleArrayOf(987.77, 1318.51), intArrayOf(70, 120))
        }
        ScoreSoundType.COMBO_STREAK -> playSynthesizedWav("combo.wav") { buffer, rate ->
          generateToneSequence(buffer, rate, doubleArrayOf(523.25, 659.25, 783.99), intArrayOf(60, 60, 100))
        }
        ScoreSoundType.HIGH_SCORE_FANFARE -> playSynthesizedWav("fanfare.wav") { buffer, rate ->
          generateToneSequence(buffer, rate, doubleArrayOf(523.25, 659.25, 783.99, 1046.50), intArrayOf(90, 90, 90, 220))
        }
        ScoreSoundType.POWER_UP_COLLECT -> playSynthesizedWav("powerup_collect.wav") { buffer, rate ->
          generateSweep(buffer, rate, 350.0, 1100.0)
        }
      }
    }
  }

  private fun playSynthesizedWav(filename: String, generator: (ShortArray, Int) -> Unit) {
    try {
      val sampleRate = 22050
      val durationMs = 400
      val numSamples = (durationMs * sampleRate) / 1000
      val pcmBuffer = ShortArray(numSamples)
      generator(pcmBuffer, sampleRate)

      val audioFile = File(context.cacheDir, filename)
      writeWavFile(audioFile, pcmBuffer, sampleRate)

      val mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        setDataSource(context, Uri.fromFile(audioFile))
        prepare()
        setOnCompletionListener { mp ->
          synchronized(activePlayers) {
            activePlayers.remove(mp)
          }
          mp.release()
        }
        setOnErrorListener { mp, _, _ ->
          synchronized(activePlayers) {
            activePlayers.remove(mp)
          }
          mp.release()
          true
        }
      }

      synchronized(activePlayers) {
        activePlayers.add(mediaPlayer)
      }
      mediaPlayer.start()
    } catch (_: Exception) {}
  }

  private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
    val byteRate = sampleRate * 2
    val totalAudioLen = pcmData.size * 2
    val totalDataLen = totalAudioLen + 36

    val header = ByteArray(44)
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    header[4] = (totalDataLen and 0xff).toByte()
    header[5] = ((totalDataLen shr 8) and 0xff).toByte()
    header[6] = ((totalDataLen shr 16) and 0xff).toByte()
    header[7] = ((totalDataLen shr 24) and 0xff).toByte()
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    header[16] = 16
    header[17] = 0
    header[18] = 0
    header[19] = 0
    header[20] = 1 // PCM
    header[21] = 0
    header[22] = 1 // Mono
    header[23] = 0
    header[24] = (sampleRate and 0xff).toByte()
    header[25] = ((sampleRate shr 8) and 0xff).toByte()
    header[26] = ((sampleRate shr 16) and 0xff).toByte()
    header[27] = ((sampleRate shr 24) and 0xff).toByte()
    header[28] = (byteRate and 0xff).toByte()
    header[29] = ((byteRate shr 8) and 0xff).toByte()
    header[30] = ((byteRate shr 16) and 0xff).toByte()
    header[31] = ((byteRate shr 24) and 0xff).toByte()
    header[32] = 2
    header[33] = 0
    header[34] = 16 // 16-bit
    header[35] = 0
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    header[40] = (totalAudioLen and 0xff).toByte()
    header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
    header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
    header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

    FileOutputStream(file).use { fos ->
      fos.write(header)
      val byteBuffer = ByteArray(pcmData.size * 2)
      for (i in pcmData.indices) {
        byteBuffer[i * 2] = (pcmData[i].toInt() and 0xff).toByte()
        byteBuffer[i * 2 + 1] = ((pcmData[i].toInt() shr 8) and 0xff).toByte()
      }
      fos.write(byteBuffer)
    }
  }

  private fun generateSiren(buffer: ShortArray, sampleRate: Int) {
    for (i in buffer.indices) {
      val t = i.toDouble() / sampleRate
      val freq = 700.0 + 350.0 * sin(2 * Math.PI * 4.0 * t)
      val env = (1.0 - (i.toDouble() / buffer.size))
      buffer[i] = (sin(2 * Math.PI * freq * t) * 32767 * env * 0.45).toInt().toShort()
    }
  }

  private fun generateToneSequence(buffer: ShortArray, sampleRate: Int, freqs: DoubleArray, durationsMs: IntArray) {
    var offset = 0
    for (idx in freqs.indices) {
      val samples = (durationsMs[idx] * sampleRate) / 1000
      val freq = freqs[idx]
      for (i in 0 until samples) {
        if (offset + i >= buffer.size) break
        val t = i.toDouble() / sampleRate
        val env = 1.0 - (i.toDouble() / samples)
        buffer[offset + i] = (sin(2 * Math.PI * freq * t) * 32767 * env * 0.45).toInt().toShort()
      }
      offset += samples
    }
  }

  private fun generateSweep(buffer: ShortArray, sampleRate: Int, startFreq: Double, endFreq: Double) {
    for (i in buffer.indices) {
      val progress = i.toDouble() / buffer.size
      val currentFreq = startFreq + (endFreq - startFreq) * progress
      val t = i.toDouble() / sampleRate
      val env = 1.0 - progress
      buffer[i] = (sin(2 * Math.PI * currentFreq * t) * 32767 * env * 0.45).toInt().toShort()
    }
  }

  private fun generateCrashNoise(buffer: ShortArray, sampleRate: Int, durationMs: Int) {
    val totalSamples = minOf(buffer.size, (durationMs * sampleRate) / 1000)
    val random = java.util.Random()
    for (i in 0 until totalSamples) {
      val progress = i.toDouble() / totalSamples
      val env = 1.0 - progress
      val noise = random.nextDouble() * 2.0 - 1.0
      val lowThud = sin(2 * Math.PI * 80.0 * (i.toDouble() / sampleRate)) * 0.5
      buffer[i] = ((noise * 0.6 + lowThud) * 32767 * env * 0.5).toInt().toShort()
    }
  }

  fun releaseAll() {
    synchronized(activePlayers) {
      activePlayers.forEach { it.release() }
      activePlayers.clear()
    }
  }
}

enum class ChaseEventType {
  POLICE_SIREN,
  POLICE_WHISTLE,
  TURBO_BOOST,
  STEALTH_ENTER,
  POWER_UP_SPAWN
}

enum class CollisionType {
  POLICE_BUMP,
  HEAVY_CRASH,
  STUN_DIZZY
}

enum class ScoreSoundType {
  COIN_PICKUP,
  COMBO_STREAK,
  HIGH_SCORE_FANFARE,
  POWER_UP_COLLECT
}
