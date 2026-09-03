package com.example.sound

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.sin

/**
 * Traditional Yemeni Heritage Music Tracks suitable for ambient background and dynamic chase scoring.
 */
data class YemeniAmbientTrack(
  val id: String,
  val titleAr: String,
  val categoryAr: String,
  val poetOrOriginAr: String,
  val iconEmoji: String,
  val baseBpm: Int,
  val melodyNotes: List<Pair<Double, Int>>, // Frequency Hz to base duration ms
  val versesAr: List<String>,
  val descriptionAr: String
)

/**
 * Dynamic Chase Proximity Intensity levels that shift tempo, drum layers, and musical urgency
 * depending on how close police patrol units are to the player.
 */
enum class ChaseProximityIntensity(
  val titleAr: String,
  val badgeEmoji: String,
  val tempoBpm: Int,
  val urgencyLabelAr: String,
  val colorHex: Long,
  val descriptionAr: String
) {
  CALM(
    titleAr = "أمان وهدوء نسبي",
    badgeEmoji = "🟢",
    tempoBpm = 92,
    urgencyLabelAr = "مسافة أمان بعيدة (> 75م)",
    colorHex = 0xFF00E676,
    descriptionAr = "الدوريات بعيدة، أنغام عود صنعاني هادئة وشجية تستحضر عراقة أزقة صنعاء القديمة."
  ),
  APPROACHING(
    titleAr = "اشتباه واقتراب الدوريات",
    badgeEmoji = "🟡",
    tempoBpm = 120,
    urgencyLabelAr = "اقتراب الدوريات (40م - 75م)",
    colorHex = 0xFFFFD700,
    descriptionAr = "الدوريات في الجوار، تسارع الإيقاع ودخول ضربات المرواس التراثية لتحذير اللاعب."
  ),
  HOT_PURSUIT(
    titleAr = "مطاردة ساخنة وشيكة",
    badgeEmoji = "🔥",
    tempoBpm = 148,
    urgencyLabelAr = "مطاردة لصيقة (18م - 40م)",
    colorHex = 0xFFFF9100,
    descriptionAr = "صافرات الإنذار والدوريات في الخلف مباشرة! طبول زامل حماسية نارية ونبض أدرينالين عالي."
  ),
  CRITICAL_PROXIMITY(
    titleAr = "التحام وخطر إطباق أمني",
    badgeEmoji = "🚨",
    tempoBpm = 170,
    urgencyLabelAr = "خطر الالتحام الشديد (< 18م)",
    colorHex = 0xFFFF1744,
    descriptionAr = "المسافة معدومة والدورية على وشك الصدم أو الإمساك! إيقاع برع حربي يماني متواصل وسريع."
  ),
  STEALTH_HIDING(
    titleAr = "سكون وتخفي في الأزقة",
    badgeEmoji = "🤫",
    tempoBpm = 75,
    urgencyLabelAr = "متخفٍ خلف الأبواب العتيقة",
    colorHex = 0xFF78909C,
    descriptionAr = "صمت مشوب بالترقب، نبضات قلب بطيئة مكتومة تترقب ابتعاد الدوريات عن المكان."
  )
}

/**
 * Ambient Sound Manager that streams traditional Yemeni heritage music tracks
 * and dynamically shifts tempo, rhythm, and intensity based on police chase proximity.
 */
object SanaaAmbientSoundManager {
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var playbackJob: Job? = null

  // 1. Curated Traditional Yemeni Music Tracks
  val tracks: List<YemeniAmbientTrack> = listOf(
    YemeniAmbientTrack(
      id = "zamil_sanaa_glory",
      titleAr = "زامل العز والشموخ الصنعاني",
      categoryAr = "زوامل قبلية حماسية",
      poetOrOriginAr = "تراث زوامل قبائل طوق صنعاء وجبل نقم",
      iconEmoji = "🥁",
      baseBpm = 100,
      descriptionAr = "ألحان قبلية أصيلة بطبول الزامل ترتفع وتتسارع مع اشتداد المطاردة في أزقة صنعاء.",
      versesAr = listOf(
        "يا سلامي على صنعاء وحصن نقم ... ومن سكن في شوارعها وسامع نغم",
        "عزنا في الكرامة والوفاء والشيم ... ما نخاف المنايا لو يثور الحمم",
        "يا جبال اليمن يا صرح عالي أشم ... دمت يا موطن الشجعان درع وقسم"
      ),
      melodyNotes = listOf(
        Pair(293.66, 260), // D4
        Pair(329.63, 260), // E4
        Pair(349.23, 320), // F4
        Pair(392.00, 360), // G4
        Pair(349.23, 220), // F4
        Pair(329.63, 260), // E4
        Pair(293.66, 440), // D4
        Pair(261.63, 260), // C4
        Pair(293.66, 480)  // D4
      )
    ),
    YemeniAmbientTrack(
      id = "sanaa_hawat_kull_fan",
      titleAr = "توشيحة: صنعاء حوت كل فن",
      categoryAr = "طرب وتواشيح صنعانية",
      poetOrOriginAr = "روائع الغناء الحميني التراثي الأصيل",
      iconEmoji = "🪕",
      baseBpm = 94,
      descriptionAr = "توشيحة وغناء عود يماني أصيل يتناغم مع جمال القمريات والبيوت الطينية العتيقة.",
      versesAr = listOf(
        "صنعاء حوت كل فن ... نزهة المشتاق والمفتون",
        "يا من لقى في رباها جنّة المأوى ... غصن القنا مال والطيب انتشر مكنون",
        "بين القمريات والأسطح تفيض شجون ... يا بدر تم على وادي بنا ميمون"
      ),
      melodyNotes = listOf(
        Pair(329.63, 280), // E4
        Pair(349.23, 280), // F4
        Pair(392.00, 400), // G4
        Pair(440.00, 300), // A4
        Pair(392.00, 250), // G4
        Pair(349.23, 250), // F4
        Pair(329.63, 300), // E4
        Pair(293.66, 350), // D4
        Pair(329.63, 500)  // E4
      )
    ),
    YemeniAmbientTrack(
      id = "yemeni_baraa_mirwas",
      titleAr = "إيقاع البرع اليمني والمرواس",
      categoryAr = "إيقاعات وطنية وفلكلور",
      poetOrOriginAr = "فنون الإيقاع والبرع في الأعراس والكرنفالات",
      iconEmoji = "🎶",
      baseBpm = 110,
      descriptionAr = "نبضات المرواس والبرع الصنعاني المحفزة التي تلهب الحماس أثناء مراوغة الدوريات.",
      versesAr = listOf(
        "دق المرواس وعزف الأوتار يطرب ... حيا الله الشجعان في كل مقلب",
        "خطوة يمانية وعز ما ينغلب ... في أزقة التاريخ والمجد يكتب"
      ),
      melodyNotes = listOf(
        Pair(392.00, 220), // G4
        Pair(466.16, 240), // Bb4
        Pair(523.25, 300), // C5
        Pair(466.16, 220), // Bb4
        Pair(392.00, 250), // G4
        Pair(349.23, 220), // F4
        Pair(392.00, 450)  // G4
      )
    ),
    YemeniAmbientTrack(
      id = "mawwal_bab_yemen",
      titleAr = "موال باب اليمن وحصن نقم",
      categoryAr = "مواويل وقصائد قديمة",
      poetOrOriginAr = "قصائد شعراء التراث اليمني العتيق",
      iconEmoji = "📜",
      baseBpm = 90,
      descriptionAr = "موال شجي يمتزج بصوت الرياح فوق قمم صنعاء، يبعث الهدوء قبل اشتعال الملاحقة.",
      versesAr = listOf(
        "واقف على باب اليمن والقلب مشتاق ... يا ديرة الأمجاد يا تاج الأعناق",
        "تاريخنا منقوش في الصخر برّاق ... وصنعاء تظل العز في كل الآفاق"
      ),
      melodyNotes = listOf(
        Pair(293.66, 320), // D4
        Pair(349.23, 340), // F4
        Pair(440.00, 400), // A4
        Pair(392.00, 320), // G4
        Pair(329.63, 320), // E4
        Pair(293.66, 500)  // D4
      )
    )
  )

  // 2. Reactive StateFlows for Compose UI Observation
  private val _currentTrackIndex = MutableStateFlow(0)
  val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

  val currentTrack: StateFlow<YemeniAmbientTrack> = MutableStateFlow(tracks[0])

  private val _intensityState = MutableStateFlow(ChaseProximityIntensity.CALM)
  val intensityState: StateFlow<ChaseProximityIntensity> = _intensityState.asStateFlow()

  private val _currentBpm = MutableStateFlow(ChaseProximityIntensity.CALM.tempoBpm)
  val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

  private val _intensityRatio = MutableStateFlow(0.0f) // 0.0 (calm) to 1.0 (critical danger)
  val intensityRatio: StateFlow<Float> = _intensityRatio.asStateFlow()

  private val _proximityDistanceMeters = MutableStateFlow(100f)
  val proximityDistanceMeters: StateFlow<Float> = _proximityDistanceMeters.asStateFlow()

  private val _isMusicPlaying = MutableStateFlow(false)
  val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

  private val _beatPulseTick = MutableStateFlow(0)
  val beatPulseTick: StateFlow<Int> = _beatPulseTick.asStateFlow()

  private val _currentVerseIndex = MutableStateFlow(0)
  val currentVerseIndex: StateFlow<Int> = _currentVerseIndex.asStateFlow()

  // Internal runtime state
  private var isMuted = false
  private var currentProximityZ: Float = 120f
  private var isPlayerHiddenState: Boolean = false
  private var isPausedState: Boolean = false
  private var activeWantedStars: Int = 1

  /**
   * Initializes and starts ambient Yemeni heritage music.
   */
  fun startAmbientMusic() {
    if (_isMusicPlaying.value && playbackJob?.isActive == true) return
    _isMusicPlaying.value = true
    launchPlaybackLoop()
  }

  /**
   * Stops or pauses ambient music when game is paused or exiting screen.
   */
  fun pauseAmbientMusic() {
    _isMusicPlaying.value = false
    playbackJob?.cancel()
    playbackJob = null
  }

  fun resumeAmbientMusic() {
    if (!_isMusicPlaying.value) {
      startAmbientMusic()
    }
  }

  fun stopAmbientMusic() {
    pauseAmbientMusic()
  }

  /**
   * Called on stage victory or mission complete.
   */
  fun onChaseEnded(isVictory: Boolean) {
    pauseAmbientMusic()
    if (isVictory) {
      GameSoundEffects.playVictoryFanfare()
    }
  }

  /**
   * Switches to the next traditional Yemeni music track.
   */
  fun nextTrack() {
    val nextIdx = (_currentTrackIndex.value + 1) % tracks.size
    selectTrack(nextIdx)
  }

  /**
   * Switches to the previous track.
   */
  fun previousTrack() {
    val prevIdx = if (_currentTrackIndex.value - 1 < 0) tracks.size - 1 else _currentTrackIndex.value - 1
    selectTrack(prevIdx)
  }

  fun selectTrack(index: Int) {
    if (index in tracks.indices) {
      _currentTrackIndex.value = index
      (currentTrack as MutableStateFlow).value = tracks[index]
      _currentVerseIndex.value = 0
    }
  }

  fun toggleMute(): Boolean {
    isMuted = !isMuted
    return isMuted
  }

  fun setMuted(muted: Boolean) {
    isMuted = muted
  }

  /**
   * Pure calculation helper for determining proximity intensity level.
   */
  fun calculateIntensity(
    proximityDistanceZ: Float,
    isHiding: Boolean,
    isPaused: Boolean = false,
    wantedStars: Int = 1
  ): ChaseProximityIntensity {
    return when {
      isHiding -> ChaseProximityIntensity.STEALTH_HIDING
      proximityDistanceZ < 18f -> ChaseProximityIntensity.CRITICAL_PROXIMITY
      proximityDistanceZ < 42f -> ChaseProximityIntensity.HOT_PURSUIT
      proximityDistanceZ < 75f -> ChaseProximityIntensity.APPROACHING
      else -> ChaseProximityIntensity.CALM
    }
  }

  /**
   * Dynamically updates the ambient sound manager with real-time chase proximity from the closest
   * police officer or patrol car. Shifts intensity, tempo, and drum layers smoothly.
   *
   * @param proximityDistanceZ distance along the road (in world units/meters) to nearest pursuer.
   * @param isPlayerHiding whether player is currently taking cover in an alley hiding spot.
   * @param isPaused whether the chase session is currently paused.
   * @param wantedStars current wanted level (1..5 stars).
   */
  fun updateChaseProximity(
    proximityDistanceZ: Float,
    isPlayerHiding: Boolean,
    isPaused: Boolean,
    wantedStars: Int = 1
  ) {
    currentProximityZ = proximityDistanceZ
    isPlayerHiddenState = isPlayerHiding
    isPausedState = isPaused
    activeWantedStars = wantedStars

    _proximityDistanceMeters.value = proximityDistanceZ.coerceAtLeast(0f)

    if (isPaused) return

    val newIntensity = calculateIntensity(proximityDistanceZ, isPlayerHiding, isPaused, wantedStars)

    if (_intensityState.value != newIntensity) {
      _intensityState.value = newIntensity
      _currentBpm.value = newIntensity.tempoBpm
    }

    // Normalized intensity ratio (0.0 to 1.0)
    val ratio = when (newIntensity) {
      ChaseProximityIntensity.STEALTH_HIDING -> 0.15f
      ChaseProximityIntensity.CALM -> 0.10f
      ChaseProximityIntensity.APPROACHING -> 0.45f
      ChaseProximityIntensity.HOT_PURSUIT -> 0.78f
      ChaseProximityIntensity.CRITICAL_PROXIMITY -> 1.0f
    }
    _intensityRatio.value = ratio
  }

  /**
   * Main coroutine loop synthesizing music notes and traditional drums in real-time,
   * dynamically scaling tempo and percussion intensity based on proximity.
   */
  private fun launchPlaybackLoop() {
    playbackJob?.cancel()
    playbackJob = scope.launch {
      var noteIdx = 0
      var beatCounter = 0

      while (isActive && _isMusicPlaying.value) {
        if (isPausedState) {
          delay(100)
          continue
        }

        val track = currentTrack.value
        val intensity = _intensityState.value
        val bpm = intensity.tempoBpm
        val beatIntervalMs = (60000L / bpm) / 2L // Eighth note resolution

        if (!isMuted && !GameSoundEffects.isMuted) {
          // 1. Synthesize Traditional Oud Melody Note
          if (noteIdx < track.melodyNotes.size && intensity != ChaseProximityIntensity.STEALTH_HIDING) {
            val (baseFreq, baseDur) = track.melodyNotes[noteIdx]
            // At higher chase intensity, shorten duration and raise attack urgency
            val scaledDuration = when (intensity) {
              ChaseProximityIntensity.CALM -> baseDur
              ChaseProximityIntensity.APPROACHING -> (baseDur * 0.85f).toInt()
              ChaseProximityIntensity.HOT_PURSUIT -> (baseDur * 0.70f).toInt()
              ChaseProximityIntensity.CRITICAL_PROXIMITY -> (baseDur * 0.58f).toInt()
              ChaseProximityIntensity.STEALTH_HIDING -> baseDur
            }
            GameSoundEffects.playOudNote(baseFreq, scaledDuration.coerceAtLeast(100))
            noteIdx = (noteIdx + 1) % track.melodyNotes.size
          }

          // 2. Layered Traditional Yemeni Drums based on Chase Proximity
          when (intensity) {
            ChaseProximityIntensity.CALM -> {
              // Gentle Mirwas tap every 4 half-beats
              if (beatCounter % 4 == 0) {
                GameSoundEffects.playOudNote(130.81, 140) // C3 acoustic bass thud
              }
            }
            ChaseProximityIntensity.APPROACHING -> {
              // Steady Mirwas drum beat every 2 half-beats
              if (beatCounter % 2 == 0) {
                GameSoundEffects.playZamilDrumBeat()
              }
            }
            ChaseProximityIntensity.HOT_PURSUIT -> {
              // Heavy tribal Zamil war drums on every beat + syncopated rim accents
              GameSoundEffects.playZamilDrumBeat()
            }
            ChaseProximityIntensity.CRITICAL_PROXIMITY -> {
              // Rapid double-time tribal Bara'a war percussion
              GameSoundEffects.playZamilDrumBeat()
              GameSoundEffects.playOudNote(110.0, 90)
            }
            ChaseProximityIntensity.STEALTH_HIDING -> {
              // Low suspenseful heartbeat pulse
              if (beatCounter % 3 == 0) {
                GameSoundEffects.playHeartbeatStealth()
              }
            }
          }
        }

        beatCounter++
        _beatPulseTick.update { (it + 1) % 1000 }

        // Advance verse text periodically every 8 beats
        if (beatCounter % 8 == 0 && track.versesAr.isNotEmpty()) {
          _currentVerseIndex.update { (it + 1) % track.versesAr.size }
        }

        delay(beatIntervalMs.coerceAtLeast(80L))
      }
    }
  }
}
