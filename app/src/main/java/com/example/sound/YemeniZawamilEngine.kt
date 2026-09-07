package com.example.sound

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlin.math.sin

/**
 * Authentic Yemeni Zawamil Music & Audio Engine (مشغل ومؤلف الزوامل اليمنية الأصيلة).
 * Provides rhythmic tribal war drum beats (Merdas), Tasa metallic clanks,
 * syncopated claps, and iconic melodic chanting themes.
 */
data class ZamilTrack(
  val id: String,
  val titleAr: String,
  val poetOrOriginAr: String,
  val categoryAr: String,
  val bpm: Int,
  val descriptionAr: String,
  val lyrics: List<String>
)

object YemeniZawamilEngine {

  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var playbackJob: Job? = null

  val playlist = listOf(
    ZamilTrack(
      id = "zamil_tahaddi_sanaa",
      titleAr = "زامل التحدي وهيبة صنعاء 🇾🇪",
      poetOrOriginAr = "تراث زوامل قبائل طوق صنعاء وجبال همدان",
      categoryAr = "زوامل حماسية وبطولية",
      bpm = 104,
      descriptionAr = "إيقاع طاسة ومرد حماسي يعبر عن صمود صنعاء وجبال نقم وعيبان الأبية.",
      lyrics = listOf(
        "يا عيبان ونقم بلغ التحية .. لصنعاء العز ورموز الحمية",
        "يماني ما يلين الدهر عوده .. كريم الجود في ساعة ردية",
        "على نهج الكرامة سرنا وثبتنا .. وصوت الحق والهمة قوية",
        "تاريخ المجد سطر في بلادي .. حروف الفخر في أرض البرية"
      )
    ),
    ZamilTrack(
      id = "zamil_barah_sanaani",
      titleAr = "زامل البرعة وطبول المرد الصنعاني 🥁",
      poetOrOriginAr = "أهازيج البرعة الصنعانية التاريخية",
      categoryAr = "برعة وقبائل",
      bpm = 118,
      descriptionAr = "إيقاع برعة سريعة مع دقات الطاسة والطبول الحربية لرفع المعنويات في المطاردة.",
      lyrics = listOf(
        "دق يا طبال واشعلها حماسة .. واقرع الطاسة على رأس العوادي",
        "برعة الفرسان في وادي العروبة .. يوم نادى المنادي للجهادِ",
        "فزعة الشهم اليماني والمروءة .. عهدنا بالوفا دوم السيادِ",
        "صنعاء الأحرار ما تركع لغازي .. مجدها عالي على كل الوهادِ"
      )
    ),
    ZamilTrack(
      id = "zamil_shas_hajwala",
      titleAr = "زامل هجولة الشاص والدباب 🚐",
      poetOrOriginAr = "ألحان وتراث الطرق السريعة وأزقة صنعاء",
      categoryAr = "هجولة وسرعة ومطاردات",
      bpm = 126,
      descriptionAr = "لحن سريع مخصص لتفحيط الشاص والدباب في جولة كنتاكي وميدان السبعين.",
      lyrics = listOf(
        "شاص تويوتا على دربه تسابق .. في دروب العز واصل بالمطارد",
        "دباب صنعاني يعانق كل حارة .. والمهارة فوق كل حد باعد",
        "بين كنتاكي وحدّة والزبيري .. صوت محركنا على الصدمات صامد",
        "لا تهاب الخوف واقطع كل حاجز .. عزنا بالله ماهو بيد جاحد"
      )
    ),
    ZamilTrack(
      id = "zamil_bab_al_yemen",
      titleAr = "زامل باب اليمن وقلاع الشرف 🏰",
      poetOrOriginAr = "زوامل التراث والشعر الشعبي الصنعاني",
      categoryAr = "تراث وتاريخ عريق",
      bpm = 96,
      descriptionAr = "قصيدة زامل أصيلة من أزقة باب اليمن وسوق الملح تعبق برائحة البهارات والفضة.",
      lyrics = listOf(
        "من باب اليمن تشرق شموس العز .. وقصور الطين تحكي كل ماضي",
        "يا سوق الملح عطر في نسيمك .. والقمريات تزهو بالتراضي",
        "صنعاء حوت كل فن وكل حكمة .. ومائها العذب في الوديان فاضي",
        "يا دار الحجر في صخرة منيفة .. شامخ الرايات للأيام ماضي"
      )
    )
  )

  var currentTrackIndex by mutableIntStateOf(0)
  var isPlaying by mutableStateOf(false)
  var currentLyricLineIndex by mutableIntStateOf(0)
  var beatPulse by mutableFloatStateOf(0f)
  var volume by mutableFloatStateOf(0.9f)

  val currentTrack: ZamilTrack
    get() = playlist[currentTrackIndex % playlist.size]

  fun startZamilPlayback() {
    if (isPlaying) return
    isPlaying = true
    launchAudioLoop()
  }

  fun pauseZamilPlayback() {
    isPlaying = false
    playbackJob?.cancel()
    playbackJob = null
  }

  fun toggleZamilPlayback() {
    if (isPlaying) pauseZamilPlayback() else startZamilPlayback()
  }

  fun nextTrack() {
    currentTrackIndex = (currentTrackIndex + 1) % playlist.size
    currentLyricLineIndex = 0
    GameSoundEffects.playCoin()
  }

  fun previousTrack() {
    currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else playlist.size - 1
    currentLyricLineIndex = 0
    GameSoundEffects.playCoin()
  }

  fun selectTrack(index: Int) {
    if (index in playlist.indices) {
      currentTrackIndex = index
      currentLyricLineIndex = 0
      if (!isPlaying) startZamilPlayback()
    }
  }

  private fun launchAudioLoop() {
    playbackJob?.cancel()
    playbackJob = scope.launch {
      var beatStep = 0
      var lyricTick = 0

      while (isActive && isPlaying) {
        val track = currentTrack
        val beatIntervalMs = (60_000L / track.bpm).coerceIn(300L, 800L)
        val stepIntervalMs = beatIntervalMs / 2 // 8th note resolution

        // Tribal Zamil Rhythm Pattern:
        // Step 0: Heavy Merdas War Bass Drum (Boom)
        // Step 1: Subtle Tasa metallic clink
        // Step 2: Mirwas rim tap / slap
        // Step 3: Syncopated double Tasa clank + Oud harmony note
        when (beatStep % 4) {
          0 -> {
            GameSoundEffects.playZamilDrumBeat()
            beatPulse = 1.0f
          }
          1 -> {
            // Metallic high Tasa clank
            GameSoundEffects.playTone(880.0, 45)
            beatPulse = 0.4f
          }
          2 -> {
            // Mirwas Rim Tap
            GameSoundEffects.playTone(320.0, 70)
            beatPulse = 0.7f
          }
          3 -> {
            // Oud harmonic melodic note matching authentic Yemeni scales (Bayati / Hijaz)
            val melodicNotes = listOf(220.0, 247.0, 261.6, 293.6, 329.6, 349.2, 392.0)
            val noteFreq = melodicNotes[(beatStep / 4) % melodicNotes.size]
            GameSoundEffects.playOudNote(noteFreq, 140)
            beatPulse = 0.85f
          }
        }

        // Animate lyrics every 8 measures
        lyricTick++
        if (lyricTick % 16 == 0 && track.lyrics.isNotEmpty()) {
          currentLyricLineIndex = (currentLyricLineIndex + 1) % track.lyrics.size
        }

        beatStep++
        delay(stepIntervalMs)
        beatPulse = (beatPulse * 0.7f).coerceAtLeast(0f)
      }
    }
  }
}
