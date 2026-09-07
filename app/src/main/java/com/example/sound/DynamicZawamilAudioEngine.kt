package com.example.sound

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

/**
 * بيئة وأجواء اللعب التي تحدد طابع الزامل الصوتي ومستواه الحماسي
 */
enum class GameplayEnvironment(
  val titleAr: String,
  val descriptionAr: String,
  val targetBpm: Int,
  val intensity: Float,
  val iconEmoji: String
) {
  CALM_ROAMING(
    titleAr = "تجول هادئ في صنعاء القديمة",
    descriptionAr = "ألحان عود يماني عذبة ودندنة تراثية أصيلة ترافق تجوالك بين باب اليمن وسوق الملح",
    targetBpm = 84,
    intensity = 0.35f,
    iconEmoji = "🕌"
  ),
  HIGH_SPEED_CHASE(
    titleAr = "مطاردة بوليسية واشتباك حماسي",
    descriptionAr = "طبول حرب وقرع طاسة سريع مع زوامل بطولية تشعل الحماس أثناء مطاردات الشرطة والتفحيط",
    targetBpm = 136,
    intensity = 1.0f,
    iconEmoji = "🚨"
  ),
  STEALTH_INFILTRATION(
    titleAr = "تسلل وتخفي في أزقة الحارات",
    descriptionAr = "إيقاع حذر ودقات مرواس مكتومة ترافق تسللك عبر الأسطح والمسارب الضيقة",
    targetBpm = 76,
    intensity = 0.5f,
    iconEmoji = "🤫"
  ),
  GOVERNMENT_HQ_ALERT(
    titleAr = "طوارئ واقتحام مقرات سيادية",
    descriptionAr = "إيقاع مارش عسكري حازم يعبر عن هيبة مجمع العرضي ووزارة الدفاع ورئاسة الوزراء",
    targetBpm = 108,
    intensity = 0.85f,
    iconEmoji = "🏛️"
  ),
  MISSION_VICTORY(
    titleAr = "احتفال النصر والبطولة اليمانية",
    descriptionAr = "برعة يمنية كبرى وزوامل فخر وزغاريد ابتهاجاً بنجاح المهمة وكسر الحصار",
    targetBpm = 120,
    intensity = 0.9f,
    iconEmoji = "🏆"
  )
}

/**
 * المؤثرات والصرخات الصوتية الحماسية المرافقة للزوامل
 */
enum class ZawamilStinger {
  WAR_DRUM_SLAM,   // ضربة مرد حربي ساحقة
  METALLIC_TASA,   // قرعة طاسة نحاسية مدوية
  CELEBRATION_SHOT // رصاصة فخر واحتفال نصر
}

/**
 * حالة التشغيل التفاعلي
 */
data class ZawamilState(
  val isPlaying: Boolean = false,
  val environment: GameplayEnvironment = GameplayEnvironment.CALM_ROAMING,
  val currentBpm: Int = 84,
  val currentTrackTitle: String = "",
  val activeLyricLine: String = "",
  val intensity: Float = 0.35f
)

/**
 * الواجهة البرمجية لتشغيل الزوامل اليمنية كخلفية صوتية متغيرة بناءً على بيئة اللعب.
 * تتيح التبديل التلقائي أو اليدوي للموسيقى والإيقاع حسب مجريات اللعبة (مطاردة، تجول، تسلل، مقرات حكومية).
 */
interface DynamicZawamilPlayer {
  val currentEnvironment: GameplayEnvironment
  val activeTrack: ZamilTrack
  val isPlaying: Boolean
  val currentBpm: Int
  val intensityLevel: Float
  val beatPulse: Float
  val stateFlow: StateFlow<ZawamilState>

  /** تحديث بيئة اللعبة لتغيير الزامل والإيقاع ديناميكياً */
  fun setEnvironment(newEnvironment: GameplayEnvironment)

  /** تشغيل أو استئناف الصوت */
  fun play()

  /** إيقاف مؤقت */
  fun pause()

  /** تبديل حالة التشغيل */
  fun togglePlayback()

  /** ضبط مستوى الصوت العام */
  fun setVolume(volume: Float)

  /** إطلاق صرخة أو ضربة إيقاعية مميزة */
  fun triggerStinger(stinger: ZawamilStinger)

  /** الانتقال للزامل التالي / السابق */
  fun nextTrack()
  fun previousTrack()
}

/**
 * المحرك الصوتي الديناميكي المتكيف مع بيئة اللعبة
 */
object AdaptiveZawamilEngine : DynamicZawamilPlayer {

  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var playbackJob: Job? = null

  private val _stateFlow = MutableStateFlow(ZawamilState())
  override val stateFlow: StateFlow<ZawamilState> = _stateFlow.asStateFlow()

  override var currentEnvironment by mutableStateOf(GameplayEnvironment.CALM_ROAMING)
    private set

  override var isPlaying by mutableStateOf(false)
    private set

  override var currentBpm by mutableIntStateOf(84)
    private set

  override var intensityLevel by mutableFloatStateOf(0.35f)
    private set

  override var beatPulse by mutableFloatStateOf(0f)
    private set

  private var masterVolume by mutableFloatStateOf(0.9f)
  var currentLyricIndex by mutableIntStateOf(0)
    private set

  // قاموس الزوامل المخصصة لكل بيئة لعب
  private val environmentTracks = mapOf(
    GameplayEnvironment.CALM_ROAMING to ZamilTrack(
      id = "zamil_roaming_sanaa",
      titleAr = "زامل نسيم باب اليمن وعبق التراث 🕌",
      poetOrOriginAr = "تراث صنعاء القديمة وأهازيج حارات الطين",
      categoryAr = "تجوال وتراث أصيل",
      bpm = 84,
      descriptionAr = "أنغام هادئة وألحان عود يماني أصيل تأخذك في رحلة بين أزقة باب اليمن ودكاكين الفضة والعقيق.",
      lyrics = listOf(
        "من باب اليمن تشرق شموس الأصالة .. وحارات صنعاء تعبق بطيب الجلالة",
        "يا سوق الملح فيك الوفا والجمالة .. وعقد القمريات يحكي لجيله رسالة",
        "نسيم الصباح العذب يسري دلالة .. على دار صنعاء حوت كل هالة",
        "يماني أصيل العهد ماله مثالة .. عزيز الكرامة وثابت مجالة"
      )
    ),
    GameplayEnvironment.HIGH_SPEED_CHASE to ZamilTrack(
      id = "zamil_chase_action",
      titleAr = "زامل البرعة الحماسية وطبول الحرب 🥁🚨",
      poetOrOriginAr = "زوامل النزال والمواجهة الصنعانية",
      categoryAr = "حماس ومطاردات نارية",
      bpm = 136,
      descriptionAr = "إيقاع حربي سريع بطبول المرد وقرع الطاسة يشعل الحماس أثناء الهروب والمطاردة والتفحيط.",
      lyrics = listOf(
        "دق يا طبال واشعلها حماسة .. واقرع الطاسة على راس العوادي",
        "برعة الفرسان في وقت الشراسة .. حيد صامد ما يلين لأي عادي",
        "بين كنتاكي وميدان السياسة .. الشاص ينقض في وجه المنادي",
        "لا تراجع لا توانى في القيادة .. عهدنا بالنصر دوم الاعتمادِ"
      )
    ),
    GameplayEnvironment.STEALTH_INFILTRATION to ZamilTrack(
      id = "zamil_stealth_shadows",
      titleAr = "زامل أسرار الحارة ومسارب السائلة 🤫",
      poetOrOriginAr = "أهازيج المسرى الليلي وحراسة القلاع",
      categoryAr = "تسلل وتخفي تكتيكي",
      bpm = 76,
      descriptionAr = "نبضات دقات مرواس خافتة ونغمات عود عميقة تبني الترقب والإثارة أثناء التسلل الحذر.",
      lyrics = listOf(
        "في جنح الظلام والخطى في سكون .. نقطع مسارب الحارة بلا أي ظنون",
        "أعين تراقب والأماني تصون .. عهد الشجاعة في ضمير العيون",
        "بين السقوف العالية والحصون .. نمضي بثقة ما يهاب المنون",
        "حكمة يمانية ونبض الحنون .. تفتح دروب النصر سر مصون"
      )
    ),
    GameplayEnvironment.GOVERNMENT_HQ_ALERT to ZamilTrack(
      id = "zamil_government_sovereignty",
      titleAr = "زامل هيبة السيادة وقلاع العرضي 🏛️",
      poetOrOriginAr = "زوامل الحرس والبطولة الرسمية",
      categoryAr = "مارش سيادي وعسكري",
      bpm = 108,
      descriptionAr = "مارش عسكري مهيب وقرعات طبول متتابعة ترافق وصولك إلى مجمع العرضي والوزارات السيادية.",
      lyrics = listOf(
        "في مجمع العرضي ترف السيادة .. راية بلادي في سماء الريادة",
        "وزارة الدفاع وحصن الشهادة .. عزم اليماني زاد فيه اعتياده",
        "رئاسة الوزراء وفخر القيادة .. سيف العدالة ما تغير مداده",
        "صنعاء أبية في ثبات الإرادة .. نبع الكرامة والمكارم ولادة"
      )
    ),
    GameplayEnvironment.MISSION_VICTORY to ZamilTrack(
      id = "zamil_victory_celebration",
      titleAr = "زامل النصر وفخر العزة اليمانية 🏆🇾🇪",
      poetOrOriginAr = "زوامل الأفراح الشعبية والانتصارات",
      categoryAr = "احتفال وانتصار كبير",
      bpm = 120,
      descriptionAr = "برعة يمنية كبرى وإيقاع فرح راقص احتفالاً بنجاح المهمة وتحقيق أعلى الدرجات.",
      lyrics = listOf(
        "مبروك يا وافي كسبت النزالا .. واثبت للعالم كبار الفعالا",
        "صنعاء تبارك لك وتزهو دلالا .. بالفوز والرفعة وهيبة رجالا",
        "راية بلادي فوق هام الجبالا .. تخفق بعزة في شموس الليالا",
        "هذا اليماني ما يعرف المحالا .. عالي الهمم دوم نحو المعالا"
      )
    )
  )

  override val activeTrack: ZamilTrack
    get() = environmentTracks[currentEnvironment] ?: environmentTracks[GameplayEnvironment.CALM_ROAMING]!!

  override fun setEnvironment(newEnvironment: GameplayEnvironment) {
    if (currentEnvironment == newEnvironment) return
    currentEnvironment = newEnvironment
    val track = activeTrack
    currentBpm = track.bpm
    intensityLevel = newEnvironment.intensity
    currentLyricIndex = 0

    updateInternalState()

    // إذا كان المشغل يعمل، نعيد توجيه الحلقة فوراً للإيقاع والسرعة الجديدة بانسيابية
    if (isPlaying) {
      launchLoop()
    }
  }

  override fun play() {
    if (isPlaying) return
    isPlaying = true
    updateInternalState()
    launchLoop()
  }

  override fun pause() {
    isPlaying = false
    playbackJob?.cancel()
    playbackJob = null
    beatPulse = 0f
    updateInternalState()
  }

  override fun togglePlayback() {
    if (isPlaying) pause() else play()
  }

  override fun setVolume(volume: Float) {
    masterVolume = volume.coerceIn(0f, 1f)
  }

  override fun triggerStinger(stinger: ZawamilStinger) {
    scope.launch {
      when (stinger) {
        ZawamilStinger.WAR_DRUM_SLAM -> {
          GameSoundEffects.playZamilDrumBeat()
          beatPulse = 1.0f
        }
        ZawamilStinger.METALLIC_TASA -> {
          GameSoundEffects.playTone(920.0, 60)
          delay(40)
          GameSoundEffects.playTone(1100.0, 90)
          beatPulse = 0.9f
        }
        ZawamilStinger.CELEBRATION_SHOT -> {
          GameSoundEffects.playFirework()
          beatPulse = 1.0f
        }
      }
    }
  }

  override fun nextTrack() {
    val environments = GameplayEnvironment.values()
    val nextIndex = (currentEnvironment.ordinal + 1) % environments.size
    setEnvironment(environments[nextIndex])
  }

  override fun previousTrack() {
    val environments = GameplayEnvironment.values()
    val prevIndex = if (currentEnvironment.ordinal > 0) currentEnvironment.ordinal - 1 else environments.size - 1
    setEnvironment(environments[prevIndex])
  }

  private fun updateInternalState() {
    val track = activeTrack
    val lyric = if (track.lyrics.isNotEmpty()) track.lyrics[currentLyricIndex % track.lyrics.size] else ""
    _stateFlow.value = ZawamilState(
      isPlaying = isPlaying,
      environment = currentEnvironment,
      currentBpm = currentBpm,
      currentTrackTitle = track.titleAr,
      activeLyricLine = lyric,
      intensity = intensityLevel
    )
  }

  private fun launchLoop() {
    playbackJob?.cancel()
    playbackJob = scope.launch {
      var step = 0
      while (isActive && isPlaying) {
        val env = currentEnvironment
        val bpm = currentBpm
        val stepMs = ((60_000L / bpm) / 2).coerceIn(180L, 700L)

        // إيقاعات زوامل متكيفة بناءً على البيئة
        when (env) {
          GameplayEnvironment.HIGH_SPEED_CHASE -> {
            // إيقاع حربي متسارع (طبول المرد + طاسة ثنائية حادة)
            when (step % 4) {
              0 -> {
                GameSoundEffects.playZamilDrumBeat()
                beatPulse = 1.0f
              }
              1 -> {
                GameSoundEffects.playTone(940.0, 35) // نقرة طاسة
                beatPulse = 0.5f
              }
              2 -> {
                GameSoundEffects.playTone(360.0, 60) // مرواس
                beatPulse = 0.7f
              }
              3 -> {
                GameSoundEffects.playTone(1050.0, 50) // طاسة قوية
                GameSoundEffects.playOudNote(349.2, 110)
                beatPulse = 0.95f
              }
            }
          }
          GameplayEnvironment.CALM_ROAMING -> {
            // إيقاع هادئ مع ألحان عود يمانية رخيمة
            when (step % 8) {
              0 -> {
                GameSoundEffects.playTone(180.0, 90) // دف هادئ
                beatPulse = 0.7f
              }
              2 -> {
                GameSoundEffects.playTone(440.0, 50)
                beatPulse = 0.35f
              }
              4 -> {
                // سلم بياتي حجازي عذب
                val bayatiScale = listOf(220.0, 246.9, 261.6, 293.6, 329.6)
                val note = bayatiScale[(step / 8) % bayatiScale.size]
                GameSoundEffects.playOudNote(note, 220)
                beatPulse = 0.6f
              }
              6 -> {
                GameSoundEffects.playTone(320.0, 60)
                beatPulse = 0.3f
              }
            }
          }
          GameplayEnvironment.GOVERNMENT_HQ_ALERT -> {
            // مارش عسكري منضبط وثابت
            when (step % 4) {
              0 -> {
                GameSoundEffects.playZamilDrumBeat()
                beatPulse = 0.95f
              }
              2 -> {
                GameSoundEffects.playTone(280.0, 75)
                beatPulse = 0.65f
              }
            }
          }
          GameplayEnvironment.STEALTH_INFILTRATION -> {
            // نقرات مرواس خافتة ومكتومة
            if (step % 4 == 0) {
              GameSoundEffects.playTone(130.0, 60)
              beatPulse = 0.45f
            } else if (step % 4 == 2) {
              GameSoundEffects.playTone(210.0, 40)
              beatPulse = 0.25f
            }
          }
          GameplayEnvironment.MISSION_VICTORY -> {
            // برعة وفرح احتفالي سريع
            when (step % 4) {
              0 -> {
                GameSoundEffects.playZamilDrumBeat()
                beatPulse = 1.0f
              }
              1, 3 -> {
                GameSoundEffects.playTone(880.0, 40)
                beatPulse = 0.6f
              }
              2 -> {
                GameSoundEffects.playOudNote(392.0, 150)
                beatPulse = 0.8f
              }
            }
          }
        }

        // تحديث أسطر الكلمات كل 8 حركات إيقاعية
        if (step % 16 == 0 && activeTrack.lyrics.isNotEmpty()) {
          currentLyricIndex = (currentLyricIndex + 1) % activeTrack.lyrics.size
          updateInternalState()
        }

        step++
        delay(stepMs)
        beatPulse = (beatPulse * 0.72f).coerceAtLeast(0f)
      }
    }
  }
}
