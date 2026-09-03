package com.example.sound

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

enum class YemeniMusicCategory(val titleAr: String, val iconEmoji: String) {
  ZAMIL_TRIBAL("زوامل قبلية وحماسية", "🥁"),
  SANAANI_CLASSIC_SONG("أغاني وتواشيح صنعانية", "🪕"),
  ANCIENT_POETRY_MAWWAL("قصائد شعرية ومواويل قديمة", "📜"),
  FOLK_MIRWAS_DANCE("طرب شعبي وإيقاع المرواس", "🎶")
}

data class YemeniHeritageTrack(
  val id: String,
  val titleAr: String,
  val category: YemeniMusicCategory,
  val artistOrPoetAr: String,
  val descriptionAr: String,
  val verses: List<String>,
  val melodyNotes: List<Pair<Double, Int>>, // Frequency (Hz) to duration (ms)
  val hasDrums: Boolean = true,
  val tempoBpm: Int = 110,
  val iconEmoji: String = "🇾🇪"
)

object YemeniHeritageRadio {
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private var playbackJob: Job? = null

  var isPlaying by mutableStateOf(false)
    private set

  var currentTrackIndex by mutableIntStateOf(0)
    private set

  var currentVerseIndex by mutableIntStateOf(0)
    private set

  var currentVerseText by mutableStateOf("")
    private set

  val playlist: List<YemeniHeritageTrack> = listOf(
    // 1. Zamil of Sana'a Glory & Chivalry
    YemeniHeritageTrack(
      id = "zamil_sanaa_glory",
      titleAr = "زامل العز والشموخ الصنعاني",
      category = YemeniMusicCategory.ZAMIL_TRIBAL,
      artistOrPoetAr = "تراث زوامل قبائل طوق صنعاء القديمة",
      descriptionAr = "زامل شعبي حماسي يحكي شجاعة أهل اليمن وهيبة أزقة صنعاء وباب اليمن وحصن نقم.",
      iconEmoji = "🥁",
      tempoBpm = 105,
      verses = listOf(
        "يا سلامي على صنعاء وحصن نقم ... ومن سكن في شوارعها وسامع نغم",
        "عزنا في الكرامة والوفاء والشيم ... ما نخاف المنايا لو يثور الحمم",
        "يا جبال اليمن يا صرح عالي أشم ... دمت يا موطن الشجعان درع وقسم",
        "نحمي الدار والمهجة وعهد الكرم ... سيرة المجد تتوارث برفع العلم"
      ),
      melodyNotes = listOf(
        Pair(293.66, 250), // D4
        Pair(329.63, 250), // E4
        Pair(349.23, 300), // F4
        Pair(392.00, 350), // G4
        Pair(349.23, 200), // F4
        Pair(329.63, 250), // E4
        Pair(293.66, 450), // D4
        Pair(261.63, 250), // C4
        Pair(293.66, 500)  // D4
      )
    ),

    // 2. Sana'a Hawat Kulla Fan (Classical Sanaani Folk Song)
    YemeniHeritageTrack(
      id = "sanaa_hawat_kull_fan",
      titleAr = "أغنية: صنعاء حوت كل فن",
      category = YemeniMusicCategory.SANAANI_CLASSIC_SONG,
      artistOrPoetAr = "روائع التراث الغنائي الصنعاني الأصيل",
      descriptionAr = "توشيحة وغناء حميني تراثي عريق يتغزل بجمال صنعاء وبيوت الطين ونوافذ القمريات.",
      iconEmoji = "🪕",
      tempoBpm = 95,
      verses = listOf(
        "صنعاء حوت كل فن ... نزهة المشتاق والمفتون",
        "يا من لقى في رباها جنّة المأوى ... غصن القنا مال والطيب انتشر مكنون",
        "بين القمريات والأسطح تفيض شجون ... يا بدر تم على وادي بنا ميمون",
        "روحي فدا من سكن في دارها المزيون ... والورد في وجنتيه والطرف كحيل العيون"
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

    // 3. Zamil of Bab Al-Yemen & Ayban Peaks
    YemeniHeritageTrack(
      id = "zamil_ayban_peaks",
      titleAr = "زامل باب اليمن وقمم عيبان",
      category = YemeniMusicCategory.ZAMIL_TRIBAL,
      artistOrPoetAr = "زوامل أصيلة من تراث شعراء اليمن",
      descriptionAr = "زامل قبلي مهيب عن شموخ قمة عيبان وباب اليمن القديم وشهامة الفرسان.",
      iconEmoji = "⛰️",
      tempoBpm = 115,
      verses = listOf(
        "يا بارق العز لاحت فوق قمة عيبان ... صنعاء الأبية حماها الواحد الرحمن",
        "من عهد حمير وأهل العزم والشجعان ... تاريخنا بالذهب مكتوب في الأزمان",
        "نخوض موج المعارك بالوفا والإيمان ... والحر ما ينثني لو زادت الأحزان",
        "صنعاء عصية على الغازي مدى الأزمان ... وراية المجد تزهو في سماء الأوطان"
      ),
      melodyNotes = listOf(
        Pair(261.63, 220), // C4
        Pair(293.66, 220), // D4
        Pair(329.63, 250), // E4
        Pair(349.23, 350), // F4
        Pair(392.00, 220), // G4
        Pair(349.23, 220), // F4
        Pair(329.63, 220), // E4
        Pair(293.66, 450)  // D4
      )
    ),

    // 4. Ancient Humayni Poetry: Ya Reem Wadi Theqaf
    YemeniHeritageTrack(
      id = "poem_reem_wadi_theqaf",
      titleAr = "قصيدة حمينية: يا ريم وادي ثقف",
      category = YemeniMusicCategory.ANCIENT_POETRY_MAWWAL,
      artistOrPoetAr = "من عيون الشعر الحميني اليمني القديم",
      descriptionAr = "قصيدة غزلية بلاغية فصيحة من أقدم ما كُتب في ديوان الشعر الصنعاني والحميني.",
      iconEmoji = "📜",
      tempoBpm = 85,
      verses = listOf(
        "يا ريم وادي ثقف يا غصن موز الندى ... من علمك يا غزال التيه هذا الردا",
        "والورد فوق الوجن والطرف ساهي كحيل ... والقد مياس يحكي نرجس الشهداء",
        "إن مر في حينا فاح العبير شذا ... يا ليت شعري متى يحلو لنا اللقياء",
        "تزهو بك الروح يا أنس الفؤاد إذا ... لاحت عيونك بدراً في سماء الفضا"
      ),
      melodyNotes = listOf(
        Pair(349.23, 350), // F4
        Pair(392.00, 300), // G4
        Pair(440.00, 350), // A4
        Pair(466.16, 400), // Bb4
        Pair(440.00, 300), // A4
        Pair(392.00, 300), // G4
        Pair(349.23, 500)  // F4
      )
    ),

    // 5. Sana'ani Wisdom Ode (موال وحكمة صنعانية)
    YemeniHeritageTrack(
      id = "poem_sanaani_wisdom",
      titleAr = "قصيدة وموال الحكمة: صبر الفتى في النوائب",
      category = YemeniMusicCategory.ANCIENT_POETRY_MAWWAL,
      artistOrPoetAr = "حكم وأمثال تراثية من حكماء صنعاء",
      descriptionAr = "أبيات حكمة عميقة تحث على الصبر والشهامة ورباطة الجأش عند الشدائد والمطاردات.",
      iconEmoji = "🏛️",
      tempoBpm = 88,
      verses = listOf(
        "الصبر مفتاح للفرجات والظفر ... ومن سعى في دروب العز ما خسر",
        "صنعاء مدينة بها الأجداد قد شادوا ... مجداً تليداً على الأيام يفتخر",
        "لا تيأسن إذا ما ضاقت الحيل ... فبعد كل عسير ينجلي الكدر",
        "كن كالنخيل عن الأحقاد مرتفعاً ... يُرمى بصخرٍ فيلقي أطيب الثمر"
      ),
      melodyNotes = listOf(
        Pair(293.66, 300), // D4
        Pair(349.23, 300), // F4
        Pair(392.00, 350), // G4
        Pair(440.00, 450), // A4
        Pair(392.00, 250), // G4
        Pair(349.23, 300), // F4
        Pair(293.66, 500)  // D4
      )
    ),

    // 6. Folk Mirwas Song: Khatar Ghusn Al-Qana
    YemeniHeritageTrack(
      id = "folk_khatar_ghusn_qana",
      titleAr = "طرب شعبي: خطر غصن القنا",
      category = YemeniMusicCategory.FOLK_MIRWAS_DANCE,
      artistOrPoetAr = "إيقاع المرواس والدندنة الشعبية اليمنية",
      descriptionAr = "أشهر دندنة وإيقاع مرواس شعبي يمني راقص يجوب مجالس وأعراس وحارات صنعاء.",
      iconEmoji = "🎶",
      tempoBpm = 120,
      verses = listOf(
        "خطر غصن القنا وارد على الما ... نزل وادي بنا حيا وسلما",
        "سلب عقلي وفكري واختطفني ... بقدٍ كالقضيب إذا ترنما",
        "رعى الله الصبا وأيام صنعاء ... وعهداً كان بالأنوار مفعما",
        "ودام الفرح في كل الحواري ... وصوت العود باللحن تكلما"
      ),
      melodyNotes = listOf(
        Pair(392.00, 200), // G4
        Pair(440.00, 200), // A4
        Pair(523.25, 300), // C5
        Pair(440.00, 200), // A4
        Pair(392.00, 200), // G4
        Pair(349.23, 200), // F4
        Pair(392.00, 400)  // G4
      )
    )
  )

  val currentTrack: YemeniHeritageTrack
    get() = playlist[currentTrackIndex.coerceIn(0, playlist.size - 1)]

  fun togglePlay() {
    if (isPlaying) {
      pause()
    } else {
      playCurrentTrack()
    }
  }

  fun playTrack(index: Int) {
    if (index in playlist.indices) {
      currentTrackIndex = index
      currentVerseIndex = 0
      playCurrentTrack()
    }
  }

  fun nextTrack() {
    currentTrackIndex = (currentTrackIndex + 1) % playlist.size
    currentVerseIndex = 0
    if (isPlaying) {
      playCurrentTrack()
    } else {
      currentVerseText = playlist[currentTrackIndex].verses.firstOrNull() ?: ""
    }
  }

  fun previousTrack() {
    currentTrackIndex = if (currentTrackIndex - 1 < 0) playlist.size - 1 else currentTrackIndex - 1
    currentVerseIndex = 0
    if (isPlaying) {
      playCurrentTrack()
    } else {
      currentVerseText = playlist[currentTrackIndex].verses.firstOrNull() ?: ""
    }
  }

  fun pause() {
    isPlaying = false
    playbackJob?.cancel()
    playbackJob = null
  }

  private fun playCurrentTrack() {
    playbackJob?.cancel()
    isPlaying = true
    val track = currentTrack
    currentVerseText = track.verses[currentVerseIndex.coerceIn(0, track.verses.size - 1)]

    playbackJob = scope.launch {
      var noteIndex = 0
      var beatCounter = 0
      val beatIntervalMs = (60000 / track.tempoBpm) / 2

      while (isActive && isPlaying) {
        if (!GameSoundEffects.isMuted) {
          // 1. Play Oud Pluck Melody note
          if (noteIndex < track.melodyNotes.size) {
            val (freq, duration) = track.melodyNotes[noteIndex]
            GameSoundEffects.playOudNote(freq, duration)
            noteIndex = (noteIndex + 1) % track.melodyNotes.size
          }

          // 2. Play Zamil / Mirwas Drum Beat rhythm
          if (track.hasDrums && beatCounter % 2 == 0) {
            GameSoundEffects.playZamilDrumBeat()
          }
        }

        beatCounter++

        // Advance verse lyrics every 4 beats
        if (beatCounter % 8 == 0) {
          currentVerseIndex = (currentVerseIndex + 1) % track.verses.size
          currentVerseText = track.verses[currentVerseIndex]
        }

        delay(beatIntervalMs.toLong())
      }
    }
  }
}
