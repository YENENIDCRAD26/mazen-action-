package com.example.ui.sanaa7d

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SanaGameRepository
import com.example.model.*
import com.example.sound.ChaseEventType
import com.example.sound.CollisionType
import com.example.sound.GameSoundEffects
import com.example.model.SanaaHeroProgression
import com.example.sound.HapticManager
import com.example.sound.SanaaAmbientSoundManager
import com.example.sound.ScoreSoundType
import com.example.sound.YemeniHeritageRadio
import com.example.ui.components.BlurPauseMenuOverlay
import com.example.ui.components.ChaseDifficultyAndScoreHud
import com.example.ui.components.ChaseHudCountdownAndStealthIndicator
import com.example.ui.components.MilestoneToastHudOverlay
import com.example.ui.components.SanaaAlleywayGridGameBoard
import com.example.ui.components.SanaaAmbientSoundModal
import com.example.ui.components.SanaaChaseMiniMap
import com.example.ui.components.SanaaHeroProgressionModal
import com.example.ui.components.SanaaTopBar
import com.example.ui.components.YemeniHeritageRadioModal
import com.example.ui.leaderboard.Top10LeaderboardScreen
import com.example.ui.theme.*
import com.example.util.MilestoneToastManager
import com.example.util.MilestoneType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.*

// ----------------------------------------------------
// 7D Sana'a Action Stages & Storyline
// ----------------------------------------------------

enum class Sanaa7DStage(
  val id: Int,
  val titleAr: String,
  val subtitleAr: String,
  val locationName: String,
  val targetDistance: Float,
  val rewardCoins: Int,
  val policeIntensity: Int,
  val timeLimitSec: Int
) {
  STAGE_1_BAB_YEMEN(
    1,
    "المرحلة 1: زقازيق باب اليمن وسوق الملح 🏰",
    "the city chase. (Shuwa'i Sana'a) - الهروب والباركور هرباً من دوريات الشرطة",
    "سوق الملح وباب اليمن",
    2500f,
    600,
    1,
    300 // 5 دقائق كاملة
  ),
  STAGE_2_QAMARIYA_ROOFTOPS(
    2,
    "المرحلة 2: قفز أسطح المنازل ونوافذ القمريات 🪟",
    "mountain scramble. Jibal al-Yaman - تسلق الأبراج الطينية وتفادي كشافات النجدة",
    "حارة القاسمي وبيوت الطين",
    2800f,
    850,
    2,
    320 // 5 دقائق و20 ثانية
  ),
  STAGE_3_DABAB_DRIFT_KENTUCKY(
    3,
    "المرحلة 3: هجولة الدباب الصنعاني في جولة كنتاكي 🚐",
    "dabab drift. Kentucky Heist - التفحيط بالدباب والشاص بين الأزقة والميادين",
    "جولة كنتاكي وشارع الزبيري",
    3200f,
    1200,
    3,
    350 // 5 دقائق و50 ثانية
  ),
  STAGE_4_SABEEN_BARRICADES(
    4,
    "المرحلة 4: اقتحام حواجز قوى الأمن عند ميدان السبعين 🚧",
    "AL KAMEEN. Sabeen Square - اختراق الحواجز الشوكية وتشتيت الدوريات بالألعاب النارية",
    "ميدان السبعين والتقاطعات الرئيسية",
    3600f,
    1600,
    4,
    380 // 6 دقائق و20 ثانية
  ),
  STAGE_5_WADI_DHAR_HEIST(
    5,
    "المرحلة 5: السطو الكبير والمخبأ السري في دار الحجر (5 نجوم) 👑",
    "THE HANDOVER! Wadi Dhar Heist - تفادي مروحيات الشرطة وسيارات العقيد ناصر والوصول لدار الحجر",
    "وادي ظهر وقصر دار الحجر التاريخي",
    4000f,
    2500,
    5,
    420 // 7 دقائق
  )
}

enum class SanaaPlayerActionState {
  RUNNING,
  SLIDING,
  ROOFTOP_JUMPING,
  AIMING_TOY_RIFLE,
  DRIVING_DABAB,
  DRIVING_SHAS,
  HIDING_IN_SPOT
}

enum class SanaaHidingSpotType(
  val titleAr: String,
  val descAr: String,
  val iconEmoji: String,
  val doorColor: Color,
  val soundType: String
) {
  ANTIQUE_WOODEN_DOOR(
    "باب صنعاني خشبي عتيق 🚪",
    "باب خشب ساج عتيق مطرز بمسامير وقفل حديدي يخفي المشاغب تماماً",
    "🚪",
    Color(0xFF5D4037),
    "DOOR"
  ),
  UNDER_STONE_STAIRS(
    "تحت درج وسلالم الطين 🪜",
    "مساحة مظلمة تحت الدرج الحجري لبيوت صنعاء لكسر خط نظر الدوريات",
    "🪜",
    Color(0xFF4E342E),
    "STAIRS"
  ),
  SPICE_POTTERY_VAULT(
    "خلف جرار وزكائب سوق الملح 🏺",
    "أزيار فخارية ضخمة وأكياس هيل وزعتر تحجب الرؤية عن الشرطة",
    "🏺",
    Color(0xFF8D6E63),
    "JARS"
  ),
  VAULTED_SAQIFAH_ARCH(
    "سقيفة ودهليز البيت الصنعاني 🏛️",
    "قبو ودهليز تاريخي مظلم يضيع فيه رجال الأمن ويفقدون الأثر",
    "🏛️",
    Color(0xFF3E2723),
    "ARCH"
  )
}

data class Sanaa7DHidingSpot(
  val id: Long,
  val side: Int, // -1 Left alley wall, 1 Right alley wall
  var worldZ: Float, // distance ahead
  val type: SanaaHidingSpotType
)

enum class SanaaWeatherType(
  val id: Int,
  val titleAr: String,
  val iconEmoji: String,
  val descriptionAr: String,
  val skyTopColor: Color,
  val skyMidColor: Color,
  val horizonColor: Color,
  val sunGlowColor: Color,
  val atmosphereFogColor: Color,
  val cobblestoneGleamColor: Color,
  val hasSunRays: Boolean,
  val dustDensity: Float,
  val sunPositionX: Float,
  val sunIntensity: Float,
  val fogColor: Color
) {
  GOLDEN_SUNSET_RAYS(
    1,
    "أصيل صنعاء الذهبي 🌅",
    "🌅",
    "أشعة الشمس الذهبية المنكسرة على الحجر والبيوت الطينية، وانعكاسات زجاج القمريات الملونة",
    Color(0xFF1A237E),
    Color(0xFF880E4F),
    Color(0xFFFF6F00),
    Color(0xFFFFD54F),
    Color(0xFFFFB74D).copy(alpha = 0.18f),
    Color(0xFFFFCA28).copy(alpha = 0.35f),
    true,
    0.45f,
    0.72f,
    0.85f,
    Color(0xFFFFB74D).copy(alpha = 0.18f)
  ),
  SWIRLING_DUST_STORM(
    2,
    "عجاج وغبار الأزقة 🌪️",
    "🌪️",
    "ذرات غبار رملية متطايرة في الهواء وهبات رياح وزوابع حركية تعزز واقعية شوارع صنعاء القديمة",
    Color(0xFF4E342E),
    Color(0xFF795548),
    Color(0xFFD7CCC8),
    Color(0xFFFFCC80),
    Color(0xFFBCAAA4).copy(alpha = 0.32f),
    Color(0xFF8D6E63).copy(alpha = 0.25f),
    false,
    1.2f,
    0.35f,
    0.25f,
    Color(0xFFBCAAA4).copy(alpha = 0.35f)
  ),
  HIGHLAND_RAIN_GLEAM(
    3,
    "رذاذ مطر صنعاء ولمعان الحجر 🌧️",
    "🌧️",
    "رذاذ مطر ناعم مع لمعان حجارة البازلت الرطبة وانعكاسات برك الماء بين الأزقة",
    Color(0xFF102027),
    Color(0xFF37474F),
    Color(0xFF78909C),
    Color(0xFF80DEEA),
    Color(0xFF546E7A).copy(alpha = 0.22f),
    Color(0xFF80D8FF).copy(alpha = 0.45f),
    false,
    0.2f,
    0.50f,
    0.05f,
    Color(0xFF546E7A).copy(alpha = 0.25f)
  ),
  MIDDAY_SOLAR_HEAT(
    4,
    "شمس الظهيرة الحارقة ☀️",
    "☀️",
    "إشراق شمس ساطع، سراب حراري متوهج، ولمعان حاد على واجهات وقباب صنعاء القديمة",
    Color(0xFF0288D1),
    Color(0xFF29B6F6),
    Color(0xFFFFF9C4),
    Color(0xFFFFF176),
    Color(0xFFFFF59D).copy(alpha = 0.15f),
    Color(0xFFFFF59D).copy(alpha = 0.40f),
    true,
    0.6f,
    0.50f,
    1.0f,
    Color(0xFFFFF59D).copy(alpha = 0.15f)
  ),
  DAWN_MIST_SERENITY(
    5,
    "فجر صنعاء الرطب والضباب 🌫️",
    "🌫️",
    "نسمات الفجر الباردة وضباب خفيف يعلو قمم نقم وعيبان وانعكاس هادئ لبيوت الطين",
    Color(0xFF1A1A2E),
    Color(0xFF30336B),
    Color(0xFF95A5A6),
    Color(0xFFE056FD),
    Color(0xFFBDC3C7).copy(alpha = 0.25f),
    Color(0xFF74B9FF).copy(alpha = 0.30f),
    true,
    0.35f,
    0.18f,
    0.40f,
    Color(0xFFBDC3C7).copy(alpha = 0.28f)
  ),
  MOONLIT_QAMARIYA_NIGHT(
    6,
    "ليل صنعاء وقمر القمريات 🌙",
    "🌙",
    "سماء ليلية مرصعة بالنجوم وهلال ساطع يضيء نوافذ القمريات الملونة في بيوت صنعاء القديمة",
    Color(0xFF050510),
    Color(0xFF0F172A),
    Color(0xFF1E293B),
    Color(0xFFF1F5F9),
    Color(0xFF334155).copy(alpha = 0.15f),
    Color(0xFF94A3B8).copy(alpha = 0.20f),
    true,
    0.20f,
    0.80f,
    0.65f,
    Color(0xFF1E293B).copy(alpha = 0.20f)
  );

  val skyColors: List<Color>
    get() = listOf(skyTopColor, skyMidColor, horizonColor)
}

// ----------------------------------------------------
// Yemeni Heritage Achievements & Badges Model (أوسمة وألقاب صنعاء التراثية)
// ----------------------------------------------------
data class YemeniHeritageAchievement(
  val id: String,
  val titleAr: String,
  val badgeEmoji: String,
  val descriptionAr: String,
  val conditionAr: String,
  val rewardCoins: Int
)

object YemeniAchievementsCatalog {
  val allAchievements = listOf(
    YemeniHeritageAchievement(
      id = "sultan_al_harah",
      titleAr = "سلطان الحارة",
      badgeEmoji = "👑",
      descriptionAr = "حاكم أزقة صنعاء القديمة وبطل المهمات الذي لا يُقهر",
      conditionAr = "تحقيق أكثر من 1500 نقطة أو إتمام مرحلة بنجاح",
      rewardCoins = 300
    ),
    YemeniHeritageAchievement(
      id = "theeb_sanaa",
      titleAr = "ذيب صنعاء",
      badgeEmoji = "🐺",
      descriptionAr = "مراوغ داهية يفلت من كماشة دوريات الشرطة بخفة وذكاء",
      conditionAr = "تشتيت ومراوغة 4 دوريات شرطة على الأقل",
      rewardCoins = 250
    ),
    YemeniHeritageAchievement(
      id = "saqr_qamariyat",
      titleAr = "صقر القمريات",
      badgeEmoji = "🦅",
      descriptionAr = "سيد الباركور والقفز الرشيق بين أسطح الطين ونوافذ القمريات",
      conditionAr = "القفز فوق 5 عوائق أو أسطح في الزقاق",
      rewardCoins = 200
    ),
    YemeniHeritageAchievement(
      id = "sheikh_hajwalah",
      titleAr = "شيخ الهجولة",
      badgeEmoji = "🚐",
      descriptionAr = "سائق الدباب والشاص الأصفر المحترف في شوارع وميادين صنعاء",
      conditionAr = "ركوب وقيادة الدباب أو الشاص في المطاردة",
      rewardCoins = 200
    ),
    YemeniHeritageAchievement(
      id = "fares_bab_yemen",
      titleAr = "فارس باب اليمن",
      badgeEmoji = "🛡️",
      descriptionAr = "مدافع شجاع يقبض على اللصوص ويسلمهم لرجال الأمن",
      conditionAr = "تسليم أفراد العصابة المقبوض عليهم للشرطة",
      rewardCoins = 350
    ),
    YemeniHeritageAchievement(
      id = "shabah_al_aziqah",
      titleAr = "شبح الأزقة",
      badgeEmoji = "🥷",
      descriptionAr = "أستاذ التخفي الذي يكسر خط نظر الشرطة خلف الأبواب والسلالم",
      conditionAr = "التخفي بنجاح في المخابئ التراثية",
      rewardCoins = 180
    ),
    YemeniHeritageAchievement(
      id = "hami_al_athar",
      titleAr = "حامي الآثار السبئية",
      badgeEmoji = "🏺",
      descriptionAr = "مستعيد التحف والكنوز المسروقة من عصابات التهريب",
      conditionAr = "استعادة وجمع أكثر من 80 عملة وغنيمة",
      rewardCoins = 220
    )
  )
}

// ----------------------------------------------------
// Daily Renewable Sana'a Challenges Model (تحديات صنعاء اليومية المتجددة)
// ----------------------------------------------------
data class DailySanaaChallenge(
  val id: String,
  val titleAr: String,
  val descriptionAr: String,
  val iconEmoji: String,
  val targetGoal: Int,
  var currentProgress: Int = 0,
  val rewardCoins: Int,
  val categoryAr: String,
  var isCompleted: Boolean = false,
  var isClaimed: Boolean = false
)

object DailySanaaChallengeGenerator {
  fun generateDailyChallenges(): List<DailySanaaChallenge> {
    return listOf(
      DailySanaaChallenge(
        id = "evade_3_cops_1min",
        titleAr = "الهروب من 3 دوريات في دقيقة ⚡",
        descriptionAr = "تشتيت ومراوغة 3 دوريات شرطة في أقل من دقيقة خلال المطاردة",
        iconEmoji = "🚓",
        targetGoal = 3,
        currentProgress = 0,
        rewardCoins = 350,
        categoryAr = "مراوغة وسرعة"
      ),
      DailySanaaChallenge(
        id = "score_qasimi_alley",
        titleAr = "سيد زقاق حي القاسمي 🏰",
        descriptionAr = "جمع 500 نقطة أو أكثر في أزقة حارة القاسمي العتيقة",
        iconEmoji = "🏺",
        targetGoal = 500,
        currentProgress = 0,
        rewardCoins = 300,
        categoryAr = "استكشاف ونقاط"
      ),
      DailySanaaChallenge(
        id = "stealth_master_hiding",
        titleAr = "شبح المخابئ التراثية 🥷",
        descriptionAr = "التخفي بنجاح خلف الأبواب العتيقة والسقائف لكسر خط النظر مرتين",
        iconEmoji = "🚪",
        targetGoal = 2,
        currentProgress = 0,
        rewardCoins = 250,
        categoryAr = "تسلل وتخفي"
      ),
      DailySanaaChallenge(
        id = "tackle_gang_thugs",
        titleAr = "صياد غنائم العصابات 💰",
        descriptionAr = "الانقضاض على 2 من لصوص العصابات واستعادة الغنائم المنهوبة",
        iconEmoji = "🪢",
        targetGoal = 2,
        currentProgress = 0,
        rewardCoins = 400,
        categoryAr = "مهمات أمنية"
      ),
      DailySanaaChallenge(
        id = "rooftop_parkour_jump",
        titleAr = "صقر قفز الأسطح 🦅",
        descriptionAr = "تنفيذ 4 قفزات باركور ناجحة فوق أسطح وبيوت صنعاء القديمة",
        iconEmoji = "🦘",
        targetGoal = 4,
        currentProgress = 0,
        rewardCoins = 200,
        categoryAr = "باركور وحركة"
      )
    )
  }
}

enum class Sanaa7DCameraAngle(val titleAr: String, val iconEmoji: String) {
  CINEMATIC_CHASE("كاميرا المطاردة 7D السينمائية", "🎥"),
  SHOULDER_AIM("منظور الكتف والتصويب الدقيق", "🎯"),
  DRONE_ROOFTOP("كاميرا الدرون الجوية فوق الأسطح", "🚁"),
  DABAB_COCKPIT("منظور كابينة الدباب والتفحيط", "🚐")
}

// 7D Entities
data class SanaaGangThug(
  val id: Long,
  var worldX: Float,
  var worldZ: Float,
  val nameAr: String,
  val gangRole: String, // "لص المتاجر", "مهرب التحف والآثار", "قاطع طرق الأزقة", "زعيم عصابة السطو"
  val lootName: String, // "صرة ذهب وفضة 💰", "تحفة سبئية أثرية 🏺", "خزينة مجوهرات 💎", "حزمة ريالات منهوبة 💵"
  val lootValue: Int,
  var health: Float = 100f,
  var isTackled: Boolean = false,
  var isBoundInRopes: Boolean = false,
  var isLooted: Boolean = false
)

data class Sanaa7DObstacle(
  val id: Long,
  var worldX: Float, // -1.0 to 1.0 relative to alley
  var worldZ: Float, // distance ahead
  val typeName: String, // "SPICE_CART", "WATER_BARREL", "POLICE_BARRICADE", "CLOTHESLINE", "CHAIR_CAFE"
  val nameAr: String,
  val iconEmoji: String,
  val isHigh: Boolean = false // Needs slide instead of jump
)

data class Sanaa7DPoliceOfficer(
  val id: Long,
  var worldX: Float,
  var worldZ: Float,
  val nameAr: String,
  val rankBadge: String,
  val isVehicle: Boolean = false,
  var health: Float = 100f,
  var isStunned: Boolean = false
)

data class Sanaa7DProjectile(
  var worldX: Float,
  var worldY: Float,
  var worldZ: Float,
  val vx: Float,
  val vy: Float,
  val vz: Float,
  val isFirework: Boolean
)

data class Sanaa7DBuildingScenery(
  val id: Long,
  val side: Int, // -1 Left, 1 Right
  val distanceZ: Float,
  val buildingHeight: Float,
  val hasQamariya: Boolean,
  val wallColor: Color
)

enum class ParticleKind {
  FIREWORK_SPARK,
  AIR_DUST_MOTE,
  GROUND_SAND_SWIRL,
  RAIN_DROP,
  SUN_BEAM_GLOW
}

data class Sanaa7DParticle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var size: Float,
  var color: Color,
  var alpha: Float,
  var lifeMs: Long,
  val kind: ParticleKind = ParticleKind.FIREWORK_SPARK
)

@Composable
fun GtaSanaa7DChaseScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ChaseGameViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
    factory = ChaseGameViewModel.provideFactory(
      application = androidx.compose.ui.platform.LocalContext.current.applicationContext as Application,
      repository = repository
    )
  )
) {
  val stats by repository.stats.collectAsState()
  val isDevActive by repository.isDeveloperModeActive.collectAsState()
  val completedStages by repository.completedStageIndexes.collectAsState()
  val isOfflineVsComputer by repository.isOfflineVsComputerMode.collectAsState()
  val stealthMode by viewModel.stealthState.collectAsState()
  val countdownState by viewModel.countdownState.collectAsState()
  val levelProgressionUiState by viewModel.levelProgressionState.collectAsState()
  val scoreState by viewModel.scoreState.collectAsState()
  val difficultyState by viewModel.difficultyState.collectAsState()
  val adaptivePursuitState by viewModel.adaptivePursuitState.collectAsState()
  val collisionFeedback by viewModel.collisionFeedback.collectAsState()
  val ambientTrack by SanaaAmbientSoundManager.currentTrack.collectAsState()
  val ambientIntensity by SanaaAmbientSoundManager.intensityState.collectAsState()
  val heroTier = SanaaHeroProgression.getTier(stats.successfulMissionsCount, isDevActive)

  // Active View Mode (7D Cinematic Chase vs 2D Tactical Grid Alleyway)
  var isTacticalGridMode by remember { mutableStateOf(false) }

  // Active Stage & Mode Selection
  val context = androidx.compose.ui.platform.LocalContext.current
  val particleSystem = remember { SanaaParticleSystem(140) }
  var chaseTimeElapsedSeconds by remember { mutableFloatStateOf(0f) }
  var showLeaderboardModal by remember { mutableStateOf(false) }

  var currentStage by remember { mutableStateOf(Sanaa7DStage.STAGE_1_BAB_YEMEN) }
  var showStageSelector by remember { mutableStateOf(false) }
  var showTacticalSettings by remember { mutableStateOf(false) }
  var showCharacterDossierDialog by remember { mutableStateOf(false) }

  // Game Engine & State (including Blur Pause state)
  var isPlaying by remember { mutableStateOf(true) }
  var isPaused by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var isStageVictory by remember { mutableStateOf(false) }

  // Back Button handles In-Game Pause Menu
  BackHandler(enabled = isPlaying && !isGameOver && !isStageVictory && !isPaused) {
    isPaused = true
    HapticManager.vibrateMovement()
  }

  // Lifecycle Sound Cleanup
  DisposableEffect(Unit) {
    onDispose {
      repository.soundManager.stopChaseMusic()
      repository.soundManager.stopAmbientStreetSounds()
      SanaaAmbientSoundManager.stopAmbientMusic()
    }
  }

  // 7D Camera & Visual Mode
  var cameraAngle by remember { mutableStateOf(Sanaa7DCameraAngle.CINEMATIC_CHASE) }
  var currentWeather by remember { mutableStateOf(SanaaWeatherType.GOLDEN_SUNSET_RAYS) }
  var is7DHyperSensoryActive by remember { mutableStateOf(true) }

  // Player Physics in 7D Alley
  var playerX by remember { mutableFloatStateOf(0f) } // -1.0 to 1.0
  var playerY by remember { mutableFloatStateOf(0f) } // Jump / Elevation
  var playerAction by remember { mutableStateOf(SanaaPlayerActionState.RUNNING) }
  var playerHealth by remember { mutableFloatStateOf(100f) }
  var playerAdrenaline by remember { mutableFloatStateOf(100f) }
  var isAdrenalineSlowMo by remember { mutableStateOf(false) }

  // Vehicle (Dabab / Shas) Hijack State
  var isVehicleHijacked by remember { mutableStateOf(false) }
  var vehicleHealth by remember { mutableFloatStateOf(160f) }
  var vehicleType by remember { mutableStateOf("دباب صنعاني أصفر") }

  // Wanted Stars (1 to 5) & Police Radio
  var wantedStars by remember { mutableIntStateOf(1) }
  var policeRadioChatter by remember { mutableStateOf("دوريات باب اليمن: المشاغب الصغير رُصد في زقاق القاسمي!") }
  var radioTimer by remember { mutableIntStateOf(0) }

  // Game Metrics
  var score by remember { mutableIntStateOf(0) }
  var coinsCollected by remember { mutableIntStateOf(0) }
  var distanceCovered by remember { mutableFloatStateOf(0f) }
  var timeLeft by remember { mutableIntStateOf(300) }
  var copsEvaded by remember { mutableIntStateOf(0) }
  var rooftopsCleared by remember { mutableIntStateOf(0) }
  var fireworksFired by remember { mutableIntStateOf(0) }

  // 7D Entities
  val gangThugs = remember { mutableStateListOf<SanaaGangThug>() }
  val obstacles = remember { mutableStateListOf<Sanaa7DObstacle>() }
  val policePursuers = remember { mutableStateListOf<Sanaa7DPoliceOfficer>() }
  val hidingSpots = remember { mutableStateListOf<Sanaa7DHidingSpot>() }
  val projectiles = remember { mutableStateListOf<Sanaa7DProjectile>() }
  val buildings = remember { mutableStateListOf<Sanaa7DBuildingScenery>() }
  val particles = remember { mutableStateListOf<Sanaa7DParticle>() }
  val random = remember { Random() }

  // Gang Hunting & Police Handover State (مطاردة العصابات ونهبهم وتسليمهم للشرطة)
  var capturedGangCount by remember { mutableIntStateOf(0) }
  var totalDeliveredToPolice by remember { mutableIntStateOf(0) }
  var totalLootStolenValue by remember { mutableIntStateOf(0) }
  var gangBountyNotice by remember { mutableStateOf<String?>(null) }
  var gangNoticeTimer by remember { mutableIntStateOf(0) }

  // Stealth & Hiding Spots State (نظام المخابئ وتكسير خط النظر)
  var isPlayerHiding by remember { mutableStateOf(false) }
  var currentHidingSpot by remember { mutableStateOf<Sanaa7DHidingSpot?>(null) }
  var hidingTimerSeconds by remember { mutableFloatStateOf(0f) }
  var stealthBonusNotice by remember { mutableStateOf<String?>(null) }
  var stealthNoticeTimer by remember { mutableIntStateOf(0) }

  // Yemeni Heritage Achievements & Badges (أوسمة وألقاب صنعاء التراثية)
  val unlockedAchievements = remember { mutableStateListOf<YemeniHeritageAchievement>() }
  var recentUnlockedAchievementNotice by remember { mutableStateOf<YemeniHeritageAchievement?>(null) }
  var achievementNoticeTimer by remember { mutableIntStateOf(0) }

  // Player Level & XP Progression State (نظام تقدم المستوى ونقاط الخبرة)
  val playerLevelInfo = remember(stats.playerXp) { PlayerLevelSystem.getLevelInfo(stats.playerXp) }
  var showLevelProgressionDialog by remember { mutableStateOf(false) }
  var recentXpEarnedNotice by remember { mutableStateOf<String?>(null) }
  var xpNoticeTimer by remember { mutableIntStateOf(0) }
  var recentLeveledUpRank by remember { mutableStateOf<LevelRank?>(null) }
  var leveledUpDialogTimer by remember { mutableIntStateOf(0) }
  var lastEarnedXpBreakdown by remember { mutableStateOf<XpBreakdown?>(null) }

  fun grantInGameXp(amount: Int, reasonAr: String) {
    viewModel.grantInGameXp(amount, reasonAr)
    val previousLevel = PlayerLevelSystem.getLevelInfo(stats.playerXp).currentLevel
    val isLeveledUp = repository.addXp(amount)
    val updatedInfo = PlayerLevelSystem.getLevelInfo(stats.playerXp)
    recentXpEarnedNotice = "+$amount XP  $reasonAr"
    xpNoticeTimer = 160
    if (isLeveledUp || updatedInfo.currentLevel > previousLevel) {
      val rank = PlayerLevelSystem.ranks.find { it.level == updatedInfo.currentLevel }
      recentLeveledUpRank = rank
      leveledUpDialogTimer = 260
      GameSoundEffects.playVictoryFanfare()
    }
  }

  // Daily Renewable Challenges State (قائمة التحديات اليومية المتجددة)
  val dailyChallenges = remember { mutableStateListOf<DailySanaaChallenge>().apply { addAll(DailySanaaChallengeGenerator.generateDailyChallenges()) } }
  var showDailyChallengesDialog by remember { mutableStateOf(false) }
  var recentCompletedChallengeNotice by remember { mutableStateOf<DailySanaaChallenge?>(null) }
  var challengeNoticeTimer by remember { mutableIntStateOf(0) }
  var sessionTimeElapsedSeconds by remember { mutableIntStateOf(0) }

  fun updateChallengeProgress(challengeId: String, increment: Int = 1, absoluteValue: Int? = null) {
    val index = dailyChallenges.indexOfFirst { it.id == challengeId }
    if (index != -1) {
      val challenge = dailyChallenges[index]
      if (!challenge.isCompleted) {
        val newProgress = if (absoluteValue != null) {
          maxOf(challenge.currentProgress, absoluteValue)
        } else {
          challenge.currentProgress + increment
        }
        val cappedProgress = newProgress.coerceAtMost(challenge.targetGoal)
        val isNowCompleted = cappedProgress >= challenge.targetGoal
        val updated = challenge.copy(
          currentProgress = cappedProgress,
          isCompleted = isNowCompleted,
          isClaimed = if (isNowCompleted) true else challenge.isClaimed
        )
        dailyChallenges[index] = updated

        if (isNowCompleted && !challenge.isCompleted) {
          coinsCollected += challenge.rewardCoins
          recentCompletedChallengeNotice = updated
          challengeNoticeTimer = 240
          GameSoundEffects.playVictoryFanfare()
        }
      }
    }
  }

  fun refreshDailyChallenges() {
    dailyChallenges.clear()
    dailyChallenges.addAll(DailySanaaChallengeGenerator.generateDailyChallenges())
    GameSoundEffects.playRadioBeep()
  }

  // Chase Start Countdown Timer (عداد بدء المطاردة 3..2..1..انطلق)
  var chaseStartCountdown by remember { mutableIntStateOf(3) }

  fun checkAndUnlockAchievement(achievementId: String) {
    val ach = YemeniAchievementsCatalog.allAchievements.find { it.id == achievementId } ?: return
    if (unlockedAchievements.none { it.id == achievementId }) {
      unlockedAchievements.add(ach)
      coinsCollected += ach.rewardCoins
      recentUnlockedAchievementNotice = ach
      achievementNoticeTimer = 220
      GameSoundEffects.playVictoryFanfare()
    }
  }

  // Yemeni Heritage Radio Dialog State
  var showHeritageRadioDialog by remember { mutableStateOf(false) }

  // Sana'a Hero Progression Modal State
  var showHeroProgressionDialog by remember { mutableStateOf(false) }

  // Sana'a Dynamic Ambient Sound & Proximity Intensity Modal State
  var showAmbientSoundDialog by remember { mutableStateOf(false) }

  // Enter Sana'a Alley Hiding Spot (Old wooden door, under stone stairs, pottery vault)
  fun enterHidingSpot(spot: Sanaa7DHidingSpot) {
    if (isVehicleHijacked) return
    isPlayerHiding = true
    viewModel.setStealth(true)
    currentHidingSpot = spot
    hidingTimerSeconds = 5.0f
    playerAction = SanaaPlayerActionState.HIDING_IN_SPOT
    playerX = if (spot.side < 0) -0.85f else 0.85f
    checkAndUnlockAchievement("shabah_al_aziqah")

    if (spot.type.soundType == "DOOR") {
      GameSoundEffects.playDoorCreak()
    } else {
      GameSoundEffects.playUnderStairsHide()
    }
  }

  // Exit Sana'a Hiding Spot
  fun exitHidingSpot() {
    if (!isPlayerHiding) return
    isPlayerHiding = false
    viewModel.setStealth(false)
    currentHidingSpot = null
    playerAction = SanaaPlayerActionState.RUNNING
    playerX = 0f
    score += 150
    coinsCollected += 35
    copsEvaded++
    wantedStars = (wantedStars - 2).coerceAtLeast(0)
    stealthBonusNotice = "🥷 نجاح التخفي! تم كسر خط نظر الشرطة وتبريد المطاردة (+150 نقطة)"
    stealthNoticeTimer = 180
    grantInGameXp(45, "تسلل وتبريد مطاردة")
    if (sessionTimeElapsedSeconds <= 60) {
      updateChallengeProgress("evade_3_cops_1min", 1)
    }
    updateChallengeProgress("stealth_master_hiding", 1)
    if (copsEvaded >= 4) {
      checkAndUnlockAchievement("theeb_sanaa")
    }
    GameSoundEffects.playStealthEvaded()
  }

  // Switch 7D Camera
  fun cycleCameraAngle() {
    val angles = Sanaa7DCameraAngle.values()
    val nextIdx = (angles.indexOf(cameraAngle) + 1) % angles.size
    cameraAngle = angles[nextIdx]
    GameSoundEffects.playRadioBeep()
  }

  // Switch Weather & Atmospheric Simulation
  fun cycleWeather() {
    val weathers = SanaaWeatherType.values()
    val nextIdx = (weathers.indexOf(currentWeather) + 1) % weathers.size
    currentWeather = weathers[nextIdx]
    GameSoundEffects.playRadioBeep()
  }

  // Fire Toy Rifle / Fireworks in 7D Alley
  fun shootSanaaWeapon(isFireworks: Boolean) {
    if (isFireworks && playerAdrenaline < 25f) return
    if (isFireworks) {
      playerAdrenaline = (playerAdrenaline - 25f).coerceAtLeast(0f)
      fireworksFired++
      GameSoundEffects.playFirework()
    } else {
      GameSoundEffects.playWalkieTalkie()
    }

    projectiles.add(
      Sanaa7DProjectile(
        worldX = playerX,
        worldY = playerY + 0.6f,
        worldZ = 4f,
        vx = (random.nextFloat() - 0.5f) * 0.02f,
        vy = 0.01f,
        vz = 10f,
        isFirework = isFireworks
      )
    )

    // Add 7D Spark & Firework Particles
    for (i in 0..12) {
      particles.add(
        Sanaa7DParticle(
          x = playerX,
          y = playerY + 0.5f,
          vx = (random.nextFloat() - 0.5f) * 0.08f,
          vy = (random.nextFloat() - 0.5f) * 0.08f,
          size = 4f + random.nextFloat() * 6f,
          color = if (isFireworks) Color(0xFFFFD600) else Color(0xFF00E5FF),
          alpha = 1f,
          lifeMs = System.currentTimeMillis() + 450
        )
      )
    }
  }

  // Toggle 7D Adrenaline Slow-Motion (Bullet Time)
  fun triggerAdrenalineSlowMo() {
    if (playerAdrenaline >= 30f) {
      isAdrenalineSlowMo = !isAdrenalineSlowMo
      GameSoundEffects.playNitroBoost()
    }
  }

  // Gang Pursuit, Looting & Police Handover Mechanics (مطاردة العصابات ونهبهم وتسليمهم للشرطة)
  fun tackleAndLootGang(thug: SanaaGangThug) {
    if (thug.isBoundInRopes) return
    thug.isTackled = true
    thug.isBoundInRopes = true
    thug.isLooted = true
    capturedGangCount++
    coinsCollected += thug.lootValue
    totalLootStolenValue += thug.lootValue
    score += (thug.lootValue * 3) + 200
    gangBountyNotice = "🥷 تم الانقضاض على ${thug.nameAr} ونهب ${thug.lootName} (+${thug.lootValue}🪙) والكلبشة بالحبال!"
    gangNoticeTimer = 180
    policeRadioChatter = "العمليات: المغامر الصغير طارد ${thug.nameAr} وشل حركته واستعاد المسروقات! 💰🪢"
    updateChallengeProgress("tackle_gang_thugs", 1)
    grantInGameXp(60, "نهب لصوص العصابة")
    if (coinsCollected >= 80 || totalLootStolenValue >= 200) {
      checkAndUnlockAchievement("hami_al_athar")
    }
    GameSoundEffects.playCoin()
    GameSoundEffects.playPunch()
  }

  fun handoverGangToPolice(cop: Sanaa7DPoliceOfficer? = null) {
    if (capturedGangCount <= 0) {
      gangBountyNotice = "⚠️ لا يوجد لصوص محتجزون حالياً! طارد العصابات في الأزقة وانهبهم أولاً!"
      gangNoticeTimer = 120
      return
    }
    val count = capturedGangCount
    val bountyAward = count * 350
    totalDeliveredToPolice += count
    coinsCollected += bountyAward
    score += bountyAward * 4
    capturedGangCount = 0
    wantedStars = 0 // Clear wanted stars completely!
    cop?.isStunned = false
    policeRadioChatter = "النقيب عادل: كفو يا بطل صنعاء الصغير! تم استلام $count من أفراد العصابة وتصفير الطوارئ! 👮‍♂️🤝🎖️"
    gangBountyNotice = "👮‍♂️ تم تسليم $count لصوص لرجال الشرطة! مكافأة شرفية +$bountyAward🪙 ووسام صقر صنعاء!"
    gangNoticeTimer = 220
    grantInGameXp(85 * count, "تسليم لصوص للشرطة")
    checkAndUnlockAchievement("fares_bab_yemen")
    GameSoundEffects.playVictoryFanfare()
    GameSoundEffects.playPoliceWhistle()
  }

  // Perform 7D Rooftop Parkour Jump or Slide
  fun performParkourJump() {
    if (playerY <= 0.05f) {
      playerY = 1.25f
      playerAction = SanaaPlayerActionState.ROOFTOP_JUMPING
      rooftopsCleared++
      updateChallengeProgress("rooftop_parkour_jump", 1)
      grantInGameXp(25, "قفزة باركور فوق الأسطح")
      if (rooftopsCleared >= 5) {
        checkAndUnlockAchievement("saqr_qamariyat")
      }
      GameSoundEffects.playJump()
    }
  }

  fun performAlleySlide() {
    playerAction = SanaaPlayerActionState.SLIDING
    GameSoundEffects.playDriftScreech()
  }

  // Hijack Nearby Dabab or Toyota Hilux / Shas Pickup Truck (GTA San Andreas Yemen Style)
  fun hijackNearbyVehicle() {
    isVehicleHijacked = true
    vehicleHealth = 200f
    vehicleType = if (vehicleType.contains("دباب")) "شاص تويوتا أصفر 🛻 (GTA Yemen)" else "دباب صنعاني أصفر 🚐"
    playerAction = SanaaPlayerActionState.DRIVING_SHAS
    checkAndUnlockAchievement("sheikh_hajwalah")
    GameSoundEffects.playCarEnter()
    GameSoundEffects.playCarHorn()
  }

  // Start a new 7D Game Session for a stage
  fun startSanaa7DGame(stage: Sanaa7DStage) {
    currentStage = stage
    obstacles.clear()
    policePursuers.clear()
    hidingSpots.clear()
    projectiles.clear()
    buildings.clear()
    particles.clear()

    // Reset stealth & hiding state
    isPlayerHiding = false
    viewModel.setStealth(false)
    viewModel.updateCountdown(stage.timeLimitSec, stage.timeLimitSec)
    currentHidingSpot = null
    hidingTimerSeconds = 0f
    stealthBonusNotice = null
    stealthNoticeTimer = 0

    // Assign default atmospheric weather according to stage
    currentWeather = when (stage) {
      Sanaa7DStage.STAGE_1_BAB_YEMEN -> SanaaWeatherType.GOLDEN_SUNSET_RAYS
      Sanaa7DStage.STAGE_2_QAMARIYA_ROOFTOPS -> SanaaWeatherType.GOLDEN_SUNSET_RAYS
      Sanaa7DStage.STAGE_3_DABAB_DRIFT_KENTUCKY -> SanaaWeatherType.SWIRLING_DUST_STORM
      Sanaa7DStage.STAGE_4_SABEEN_BARRICADES -> SanaaWeatherType.MIDDAY_SOLAR_HEAT
      Sanaa7DStage.STAGE_5_WADI_DHAR_HEIST -> SanaaWeatherType.HIGHLAND_RAIN_GLEAM
    }

    // Reset Gang chasing & handover stats
    gangThugs.clear()
    capturedGangCount = 0
    totalDeliveredToPolice = 0
    totalLootStolenValue = 0
    gangBountyNotice = null
    gangNoticeTimer = 0

    score = 0
    coinsCollected = 0
    distanceCovered = 0f
    timeLeft = stage.timeLimitSec
    sessionTimeElapsedSeconds = 0
    chaseTimeElapsedSeconds = 0f
    copsEvaded = 0
    rooftopsCleared = 0
    fireworksFired = 0

    playerX = 0f
    playerY = 0f
    playerHealth = 100f
    playerAdrenaline = 100f
    playerAction = SanaaPlayerActionState.RUNNING
    isVehicleHijacked = false
    wantedStars = stage.policeIntensity

    isGameOver = false
    isStageVictory = false
    isPlaying = true
    showStageSelector = false
    showTacticalSettings = false
    showHeroProgressionDialog = false
    showAmbientSoundDialog = false
    showHeritageRadioDialog = false
    showCharacterDossierDialog = false
    showDailyChallengesDialog = false
    showLevelProgressionDialog = false
    showLeaderboardModal = false

    GameSoundEffects.playPoliceWhistle()
    GameSoundEffects.playJump()
  }

  fun finishChaseSession(isVictory: Boolean) {
    isPlaying = false
    if (isVictory) {
      isStageVictory = true
      coinsCollected += currentStage.rewardCoins
      repository.markStageCompleted(currentStage.id, currentStage.rewardCoins)
    } else {
      isGameOver = true
    }

    val breakdown = PlayerLevelSystem.calculateChaseXp(
      isVictory = isVictory,
      score = score,
      copsEvaded = copsEvaded,
      rooftopsCleared = rooftopsCleared,
      thugsCaptured = totalDeliveredToPolice + capturedGangCount,
      distanceCovered = distanceCovered
    )
    lastEarnedXpBreakdown = breakdown
    repository.recordChaseScore(score, coinsCollected, breakdown.totalXpEarned)
    viewModel.completeMission(breakdown.totalXpEarned, isVictory)

    // Milestone Check: Personal Best High Score
    val isNewPersonalBest = (score > stats.highChaseScore && score > 0)
    if (isNewPersonalBest) {
      MilestoneToastManager.notifyNewPersonalBest(context, score, currentStage.titleAr)
    }

    // Milestone Check: Level Up
    val currentTotalXp = stats.playerXp + breakdown.totalXpEarned
    val info = PlayerLevelSystem.getLevelInfo(currentTotalXp)
    if (info.currentLevel > playerLevelInfo.currentLevel) {
      val rank = PlayerLevelSystem.ranks.find { it.level == info.currentLevel }
      recentLeveledUpRank = rank
      leveledUpDialogTimer = 300
      val rankTitle = rank?.titleAr ?: "بطل أزقة صنعاء"
      MilestoneToastManager.notifyLevelUp(context, info.currentLevel, rankTitle)
    }

    // Milestone Check: Fastest Escape Time
    if (isVictory && chaseTimeElapsedSeconds > 0) {
      val mins = (chaseTimeElapsedSeconds / 60).toInt()
      val secs = chaseTimeElapsedSeconds % 60
      val formattedTime = "%02d:%05.2f".format(mins, secs)
      MilestoneToastManager.notifyFastestEscape(context, formattedTime, currentStage.titleAr)
    }

    // Save High Score & Speedrun record in Room Database
    val heroTitle = if (repository.selectedFaction.value == Faction.GANG) "مازن (الزعيم)" else "مفتش أمن صنعاء"
    repository.saveHighScoreToRoom(
      playerName = heroTitle,
      score = score,
      mode = "GTA_SANAA_7D",
      difficulty = difficultyState.name,
      titleAr = currentStage.titleAr,
      coinsEarned = coinsCollected,
      chaseTimeSeconds = chaseTimeElapsedSeconds,
      stageName = currentStage.titleAr,
      isPersonalBest = isNewPersonalBest
    )

    if (isVictory) {
      val (newHeroTier, isNewHeroTier) = repository.recordSuccessfulMission()
      if (isNewHeroTier) {
        if (newHeroTier.isSanaaHeroStatus) {
          MilestoneToastManager.notifySanaaHeroUnlocked(context, newHeroTier.speedBoostPercent)
        } else {
          MilestoneToastManager.notifySpeedBoostUnlocked(context, newHeroTier.statusTitleAr, newHeroTier.speedBoostPercent)
        }
      }
      SanaaAmbientSoundManager.onChaseEnded(isVictory = true)
      HapticManager.vibrateSuccess()
      repository.soundManager.playScoreIncreaseSound(ScoreSoundType.HIGH_SCORE_FANFARE)
      GameSoundEffects.playVictoryFanfare()
    } else {
      SanaaAmbientSoundManager.onChaseEnded(isVictory = false)
      HapticManager.vibrateCollision()
      repository.soundManager.playCollisionSound(CollisionType.HEAVY_CRASH)
      GameSoundEffects.playGameOver()
    }
  }

  // Main 7D Hyper-Sensory Simulation Game Loop with Sound & Haptics
  LaunchedEffect(isPlaying, isPaused, isGameOver, isStageVictory) {
    if (isPlaying && !isPaused && !isGameOver && !isStageVictory) {
      repository.soundManager.playAmbientStreetSounds()
      repository.soundManager.playChaseMusic(isUrgent = wantedStars >= 3)
      SanaaAmbientSoundManager.startAmbientMusic()
      var spawnObstacleTimer = 0
      var spawnPoliceTimer = 0
      var spawnGangTimer = 0
      var spawnHidingSpotTimer = 0
      var buildingTimer = 0
      var secondClockTimer = 0
      var entityIdGen = 500L

      while (isActive && isPlaying && !isPaused && !isGameOver && !isStageVictory) {
        val gameSpeedMultiplier = if (isAdrenalineSlowMo) 0.5f else 1.0f
        val heroSpeedMultiplier = SanaaHeroProgression.getSpeedMultiplier(stats.successfulMissionsCount, isDevActive)

        val baseSpeed = when {
          isPlayerHiding -> 0.003f // Slow alley creep when hidden behind doors
          isVehicleHijacked -> 0.024f
          playerAction == SanaaPlayerActionState.ROOFTOP_JUMPING -> 0.018f
          else -> 0.013f
        } * gameSpeedMultiplier * heroSpeedMultiplier

        distanceCovered += baseSpeed * 11f
        score = (distanceCovered * 15).toInt() + (coinsCollected * 40) + (copsEvaded * 150) + (rooftopsCleared * 80)

        if (currentStage == Sanaa7DStage.STAGE_2_QAMARIYA_ROOFTOPS || currentStage == Sanaa7DStage.STAGE_1_BAB_YEMEN) {
          updateChallengeProgress("score_qasimi_alley", absoluteValue = score)
        }

        // Stealth Hiding Spot Logic (كسر خط نظر رجال الشرطة وإلغاء حالة المطاردة)
        if (isPlayerHiding) {
          hidingTimerSeconds -= (0.016f * gameSpeedMultiplier)

          // Heartbeat sound pulse every ~1.2 seconds
          if ((hidingTimerSeconds * 60).toInt() % 75 == 0 && hidingTimerSeconds > 0.5f) {
            GameSoundEffects.playHeartbeatStealth()
          }

          // Cool down wanted stars and confuse police radio
          if ((hidingTimerSeconds * 60).toInt() % 90 == 0 && wantedStars > 0) {
            wantedStars = (wantedStars - 1).coerceAtLeast(0)
            val lostSightCalls = listOf(
              "غرفة العمليات: فُقد الاتصال البصري بالمشاغب الصغير خلف أبواب وسلالم الزقاق! ❓",
              "النقيب عادل: خط النظر مكسور تماماً! المشاغب اختفى داخل السقيفة! 🚪",
              "دورية النجدة: المنطقة فارغة.. إلغاء حالة الاستنفار تدريجياً! 📻"
            )
            policeRadioChatter = lostSightCalls[random.nextInt(lostSightCalls.size)]
          }

          // Confuse all active police pursuers
          for (cop in policePursuers) {
            cop.isStunned = true
            cop.worldX += if (cop.worldX >= 0) 0.008f else -0.008f
          }

          // Auto-exit when timer runs out with stealth bonus
          if (hidingTimerSeconds <= 0f) {
            exitHidingSpot()
          }
        }

        // Stealth notice banner decay
        if (stealthNoticeTimer > 0) {
          stealthNoticeTimer--
          if (stealthNoticeTimer <= 0) {
            stealthBonusNotice = null
          }
        }

        // Gang Bounty Notice Decay
        if (gangNoticeTimer > 0) {
          gangNoticeTimer--
          if (gangNoticeTimer <= 0) {
            gangBountyNotice = null
          }
        }

        // Daily Challenge Completion Banner Decay
        if (challengeNoticeTimer > 0) {
          challengeNoticeTimer--
          if (challengeNoticeTimer <= 0) {
            recentCompletedChallengeNotice = null
          }
        }

        // XP Notification Banner Decay
        if (xpNoticeTimer > 0) {
          xpNoticeTimer--
          if (xpNoticeTimer <= 0) {
            recentXpEarnedNotice = null
          }
        }

        // Level Up Banner / Modal Decay
        if (leveledUpDialogTimer > 0) {
          leveledUpDialogTimer--
          if (leveledUpDialogTimer <= 0) {
            recentLeveledUpRank = null
          }
        }

        // Player Gravity / Jump Recovery
        if (playerY > 0f) {
          playerY = (playerY - 0.065f * gameSpeedMultiplier).coerceAtLeast(0f)
          if (playerY <= 0f && playerAction == SanaaPlayerActionState.ROOFTOP_JUMPING) {
            playerAction = if (isVehicleHijacked) SanaaPlayerActionState.DRIVING_DABAB else SanaaPlayerActionState.RUNNING
          }
        }

        // Slide Recovery
        if (playerAction == SanaaPlayerActionState.SLIDING) {
          // Slide timer
        }

        // Adrenaline Drain & Regen
        if (isAdrenalineSlowMo) {
          playerAdrenaline = (playerAdrenaline - 0.6f).coerceAtLeast(0f)
          if (playerAdrenaline <= 0f) isAdrenalineSlowMo = false
        } else if (playerAdrenaline < 100f) {
          playerAdrenaline = (playerAdrenaline + 0.15f).coerceAtMost(100f)
        }

        // Countdown Timer
        secondClockTimer++
        if (secondClockTimer >= 60) {
          secondClockTimer = 0
          timeLeft--
          sessionTimeElapsedSeconds++
          viewModel.updateCountdown(timeLeft, currentStage.timeLimitSec)
          if (timeLeft <= 0) {
            finishChaseSession(false)
            break
          }
        }

        // Check Stage Victory
        if (distanceCovered >= currentStage.targetDistance) {
          finishChaseSession(true)
          break
        }

        // Police Radio Dispatch Chatter Update
        radioTimer++
        if (radioTimer >= 180) {
          radioTimer = 0
          val dispatches = listOf(
            "غرفة العمليات: العقيد ناصر يأمر بتطويق زقاق سوق الملح فوراً! 🚨",
            "دورية النجدة 14: المشاغب الصغير يقفز فوق أسطح حارة القاسمي! 🏃‍♂️",
            "النقيب عادل: أقيموا حاجزاً شوكياً قرب ميدان السبعين وباب اليمن! 🚧",
            "عمليات العاصمة: انتبهوا.. المشاغب يقود دباباً أصفر ويفحط بين الأزقة! 🚐💨",
            "برقية عاجلة: الزعيم المشاغب يستخدم قاذف ألعاب نارية تشتيتي! 🎆"
          )
          policeRadioChatter = dispatches[random.nextInt(dispatches.size)]
          GameSoundEffects.playWalkieTalkie()
        }

        // Spawn 7D Sana'a Clay Mud Buildings & Qamariya Windows
        buildingTimer++
        if (buildingTimer >= 14) {
          buildingTimer = 0
          val side = if (random.nextBoolean()) -1 else 1
          buildings.add(
            Sanaa7DBuildingScenery(
              id = entityIdGen++,
              side = side,
              distanceZ = 240f,
              buildingHeight = 1.0f + random.nextFloat() * 1.5f,
              hasQamariya = random.nextBoolean(),
              wallColor = if (random.nextBoolean()) Color(0xFF8D6E63) else Color(0xFF6D4C41)
            )
          )
        }

        // Spawn 7D Alley Obstacles (Spice carts, water barrels, clotheslines)
        spawnObstacleTimer++
        if (spawnObstacleTimer >= 32) {
          spawnObstacleTimer = 0
          val obsTypes = listOf(
            Triple("عربة بهارات سوق الملح", "🛒", false),
            Triple("براميل ماء فخارية", "🏺", false),
            Triple("حبال غسيل معلقة بين المباني", "👕", true),
            Triple("حاجز تفتيش أمني", "🚧", false),
            Triple("طاولات مقهى الشاي الصنعاني", "☕", false)
          )
          val chosen = obsTypes[random.nextInt(obsTypes.size)]
          val laneX = (random.nextFloat() - 0.5f) * 1.4f

          obstacles.add(
            Sanaa7DObstacle(
              id = entityIdGen++,
              worldX = laneX,
              worldZ = 220f,
              typeName = chosen.first,
              nameAr = chosen.first,
              iconEmoji = chosen.second,
              isHigh = chosen.third
            )
          )
        }

        // Spawn 7D Police Pursuers & Cruisers
        spawnPoliceTimer++
        if (spawnPoliceTimer >= 48) {
          spawnPoliceTimer = 0
          val isCar = wantedStars >= 2 && random.nextInt(3) == 0
          policePursuers.add(
            Sanaa7DPoliceOfficer(
              id = entityIdGen++,
              worldX = (random.nextFloat() - 0.5f) * 1.2f,
              worldZ = 240f,
              nameAr = if (isCar) "دورية نجدة صنعاء 🚓" else if (wantedStars >= 4) "ضابط تحريات المباحث 👮‍♂️" else "شرطي راجل 👮",
              rankBadge = if (isCar) "دورية مسلحة" else "قوات الأمن",
              isVehicle = isCar,
              health = if (isCar) 160f else 80f
            )
          )
        }

        // Spawn 7D Gang Thugs & Loot Carriers (عصابات شوارع صنعاء للنهب والتسليم)
        spawnGangTimer++
        if (spawnGangTimer >= 36) {
          spawnGangTimer = 0
          val gangRoster = listOf(
            Triple("لص دكاكين سوق الملح 🦹‍♂️", "صرة ريالات وذهب مسروق 💰", 150),
            Triple("مهرب التحف والآثار 🏺", "تمثال سبئي برونزي ثمين 💎", 260),
            Triple("قاطع طرق أزقة القاسمي 🗡️", "خزينة مجوهرات مرصعة 👑", 320),
            Triple("زعيم عصابة السطو المسلح 💼", "حقيبة نقود البنك المنهوبة 💵", 400),
            Triple("نشال جولة كنتاكي 🏃‍♂️", "صرة فضة صنعانية عتيقة 🪙", 180)
          )
          val chosenGang = gangRoster[random.nextInt(gangRoster.size)]
          gangThugs.add(
            SanaaGangThug(
              id = entityIdGen++,
              worldX = (random.nextFloat() - 0.5f) * 1.3f,
              worldZ = 230f,
              nameAr = chosenGang.first,
              gangRole = chosenGang.first,
              lootName = chosenGang.second,
              lootValue = chosenGang.third
            )
          )
        }

        // Spawn 7D Sana'a Alley Hiding Spots (الأبواب الخشبية العتيقة والسلالم وسقائف صنعاء)
        spawnHidingSpotTimer++
        if (spawnHidingSpotTimer >= 36) {
          spawnHidingSpotTimer = 0
          val side = if (random.nextBoolean()) -1 else 1
          val spotTypes = SanaaHidingSpotType.values()
          val chosenType = spotTypes[random.nextInt(spotTypes.size)]
          hidingSpots.add(
            Sanaa7DHidingSpot(
              id = entityIdGen++,
              side = side,
              worldZ = 230f,
              type = chosenType
            )
          )
        }

        // Update 7D Buildings
        val bIter = buildings.iterator()
        while (bIter.hasNext()) {
          val b = bIter.next()
          val moveZ = baseSpeed * 2200f
          val updatedZ = b.distanceZ - moveZ
          if (updatedZ < -15f) {
            bIter.remove()
          } else {
            val idx = buildings.indexOf(b)
            if (idx != -1) {
              buildings[idx] = b.copy(distanceZ = updatedZ)
            }
          }
        }

        // Update 7D Hiding Spots
        val hIter = hidingSpots.iterator()
        while (hIter.hasNext()) {
          val hSpot = hIter.next()
          if (!isPlayerHiding) {
            hSpot.worldZ -= (baseSpeed * 2200f)
          }
          if (hSpot.worldZ < -18f) {
            hIter.remove()
          }
        }

        // Update 7D Obstacles & Collisions
        val oIter = obstacles.iterator()
        while (oIter.hasNext()) {
          val obs = oIter.next()
          obs.worldZ -= (baseSpeed * 2200f)

          // Collision with Player
          if (abs(obs.worldX - playerX) < 0.45f && obs.worldZ in -4f..10f) {
            val isEvaded = if (obs.isHigh) playerAction == SanaaPlayerActionState.SLIDING else playerY > 0.4f
            if (!isEvaded) {
              // Hit obstacle!
              HapticManager.vibrateCollision()
              if (isVehicleHijacked) {
                vehicleHealth -= 30f
                GameSoundEffects.playCarCrash()
                if (vehicleHealth <= 0f) {
                  isVehicleHijacked = false
                  playerAction = SanaaPlayerActionState.RUNNING
                }
              } else {
                playerHealth -= 20f
                GameSoundEffects.playPunch()
                if (playerHealth <= 0f) {
                  finishChaseSession(false)
                  break
                }
              }
            } else {
              // Successfully jumped or slid!
              score += 60
              coinsCollected += 15
              grantInGameXp(15, "مراوغة عائق")
              HapticManager.vibrateMovement()
              GameSoundEffects.playCoin()
            }
            oIter.remove()
          } else if (obs.worldZ < -15f) {
            oIter.remove()
          }
        }

        // Update 7D Police Pursuers (scaled dynamically with adaptive pursuit system based on completed stages)
        val pIter = policePursuers.iterator()
        val adaptiveSpeedFactor = adaptivePursuitState.effectivePolicePursuitSpeed
        while (pIter.hasNext()) {
          val cop = pIter.next()
          val copRelSpeed = (baseSpeed - (if (cop.isVehicle) 0.016f else 0.009f) * (1f / adaptiveSpeedFactor)) * 2200f
          cop.worldZ -= copRelSpeed

          // Tracking AI (breaks line-of-sight if player is hiding in an alley spot)
          if (!cop.isStunned && !isPlayerHiding) {
            val trackingStep = 0.006f * adaptiveSpeedFactor
            if (cop.worldX < playerX - 0.04f) cop.worldX += trackingStep
            else if (cop.worldX > playerX + 0.04f) cop.worldX -= trackingStep
          }

          // Close encounter collision (immune if hiding behind door or under stairs)
          if (!cop.isStunned && !isPlayerHiding && abs(cop.worldX - playerX) < 0.40f && cop.worldZ in -4f..8f) {
            viewModel.triggerCollisionFeedback("POLICE_CHASE", "💥 اصطدام بدورية الشرطة الصنعانية!", 35)
            HapticManager.vibrateCollision()
            repository.soundManager.playCollisionSound(CollisionType.POLICE_BUMP)
            if (isVehicleHijacked) {
              cop.health -= 80f
              vehicleHealth -= 20f
              GameSoundEffects.playCarCrash()
              if (cop.health <= 0f) {
                cop.isStunned = true
                copsEvaded++
                coinsCollected += 40
                viewModel.recordStunt("صدم دورية بالدباب/الشاص", 200)
                grantInGameXp(40, "صدم دورية بالشاص")
              }
            } else {
              playerHealth -= 25f
              GameSoundEffects.playPunch()
              if (playerHealth <= 0f) {
                HapticManager.vibrateCaughtByPolice()
                repository.soundManager.playCaughtByPoliceSound()
                finishChaseSession(false)
                break
              }
            }
          }

          if (cop.worldZ < -20f || cop.worldZ > 280f) {
            pIter.remove()
          }
        }

        // Track nearest active cop proximity for dynamic ambient music intensity shifting
        val nearestActiveCop = policePursuers.filter { !it.isStunned }.minByOrNull { abs(it.worldZ) }
        val nearestCopDistance = nearestActiveCop?.let { abs(it.worldZ) } ?: 130f

        SanaaAmbientSoundManager.updateChaseProximity(
          proximityDistanceZ = nearestCopDistance,
          isPlayerHiding = isPlayerHiding,
          isPaused = isPaused || isGameOver || isStageVictory,
          wantedStars = wantedStars
        )

        // Update 7D Gang Thugs (حركة ومطاردة ونهب أفراد العصابات)
        val gIter = gangThugs.iterator()
        while (gIter.hasNext()) {
          val thug = gIter.next()
          val thugRelSpeed = if (thug.isBoundInRopes) baseSpeed * 2200f else (baseSpeed - 0.007f) * 2200f
          thug.worldZ -= thugRelSpeed

          // Gang Evasion AI
          if (!thug.isBoundInRopes) {
            if (thug.worldX < playerX) thug.worldX -= 0.003f
            else thug.worldX += 0.003f
            thug.worldX = thug.worldX.coerceIn(-1.0f, 1.0f)
          }

          // Close encounter collision: Tackle and loot stash!
          if (!thug.isBoundInRopes && abs(thug.worldX - playerX) < 0.45f && thug.worldZ in -4f..8f) {
            tackleAndLootGang(thug)
          }

          if (thug.worldZ < -20f || thug.worldZ > 280f) {
            gIter.remove()
          }
        }

        // Update 7D Projectiles & Hits
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
          val proj = projIter.next()
          proj.worldX += proj.vx
          proj.worldY += proj.vy
          proj.worldZ += proj.vz

          // Check hit against gang thugs
          var hitSomething = false
          for (thug in gangThugs) {
            if (!thug.isBoundInRopes && abs(thug.worldX - proj.worldX) < 0.40f && abs(thug.worldZ - proj.worldZ) < 14f) {
              tackleAndLootGang(thug)
              projIter.remove()
              hitSomething = true
              break
            }
          }
          if (hitSomething) continue

          // Check hit against police pursuers
          for (cop in policePursuers) {
            if (!cop.isStunned && abs(cop.worldX - proj.worldX) < 0.35f && abs(cop.worldZ - proj.worldZ) < 12f) {
              cop.health -= if (proj.isFirework) 90f else 40f
              cop.isStunned = true
              copsEvaded++
              score += 120
              coinsCollected += 30
              GameSoundEffects.playFirework()
              projIter.remove()
              break
            }
          }

          if (proj.worldZ > 260f || proj.worldZ < -10f) {
            projIter.remove()
          }
        }

        // Update 7D Particles
        val nowMs = System.currentTimeMillis()

        // Continuous Ambient Weather Particles Generation
        if (particles.size < 40 && random.nextFloat() < currentWeather.dustDensity) {
          when (currentWeather) {
            SanaaWeatherType.GOLDEN_SUNSET_RAYS -> {
              particles.add(
                Sanaa7DParticle(
                  x = (random.nextFloat() - 0.5f) * 2.2f,
                  y = random.nextFloat() * 1.8f,
                  vx = (random.nextFloat() - 0.5f) * 0.006f + 0.002f,
                  vy = -0.003f - random.nextFloat() * 0.004f,
                  size = 3f + random.nextFloat() * 4f,
                  color = if (random.nextBoolean()) Color(0xFFFFD54F) else Color(0xFFFF8A65),
                  alpha = 0.85f,
                  lifeMs = nowMs + 1600 + random.nextInt(1000).toLong(),
                  kind = ParticleKind.AIR_DUST_MOTE
                )
              )
            }
            SanaaWeatherType.SWIRLING_DUST_STORM -> {
              particles.add(
                Sanaa7DParticle(
                  x = 1.1f,
                  y = random.nextFloat() * 1.4f,
                  vx = -0.022f - random.nextFloat() * 0.028f,
                  vy = (random.nextFloat() - 0.5f) * 0.008f,
                  size = 4f + random.nextFloat() * 6f,
                  color = if (random.nextBoolean()) Color(0xFFD7CCC8) else Color(0xFFBCAAA4),
                  alpha = 0.75f,
                  lifeMs = nowMs + 1200 + random.nextInt(600).toLong(),
                  kind = if (random.nextFloat() < 0.5f) ParticleKind.GROUND_SAND_SWIRL else ParticleKind.AIR_DUST_MOTE
                )
              )
            }
            SanaaWeatherType.HIGHLAND_RAIN_GLEAM -> {
              particles.add(
                Sanaa7DParticle(
                  x = (random.nextFloat() - 0.5f) * 2.2f,
                  y = 2.0f,
                  vx = -0.005f,
                  vy = -0.07f - random.nextFloat() * 0.03f,
                  size = 2.5f + random.nextFloat() * 2.5f,
                  color = Color(0xFF80DEEA),
                  alpha = 0.9f,
                  lifeMs = nowMs + 850,
                  kind = ParticleKind.RAIN_DROP
                )
              )
            }
            SanaaWeatherType.MIDDAY_SOLAR_HEAT -> {
              particles.add(
                Sanaa7DParticle(
                  x = (random.nextFloat() - 0.5f) * 2.0f,
                  y = random.nextFloat() * 1.6f,
                  vx = (random.nextFloat() - 0.5f) * 0.004f,
                  vy = 0.005f + random.nextFloat() * 0.005f,
                  size = 3f + random.nextFloat() * 3.5f,
                  color = Color(0xFFFFF9C4),
                  alpha = 0.6f,
                  lifeMs = nowMs + 1400,
                  kind = ParticleKind.SUN_BEAM_GLOW
                )
              )
            }
            SanaaWeatherType.DAWN_MIST_SERENITY -> {
              particles.add(
                Sanaa7DParticle(
                  x = (random.nextFloat() - 0.5f) * 2.2f,
                  y = random.nextFloat() * 1.2f,
                  vx = 0.004f + (random.nextFloat() - 0.5f) * 0.003f,
                  vy = -0.002f,
                  size = 5f + random.nextFloat() * 6f,
                  color = Color(0xFFBDC3C7).copy(alpha = 0.45f),
                  alpha = 0.65f,
                  lifeMs = nowMs + 1800,
                  kind = ParticleKind.AIR_DUST_MOTE
                )
              )
            }
            SanaaWeatherType.MOONLIT_QAMARIYA_NIGHT -> {
              particles.add(
                Sanaa7DParticle(
                  x = (random.nextFloat() - 0.5f) * 2.4f,
                  y = 0.5f + random.nextFloat() * 1.5f,
                  vx = (random.nextFloat() - 0.5f) * 0.002f,
                  vy = -0.001f,
                  size = 2f + random.nextFloat() * 3f,
                  color = if (random.nextBoolean()) Color(0xFFF1F5F9) else Color(0xFFFFD54F),
                  alpha = 0.8f,
                  lifeMs = nowMs + 2000,
                  kind = ParticleKind.AIR_DUST_MOTE
                )
              )
            }
          }
        }

        // Vehicle tire dust & sprint dust trail
        if (particles.size < 48 && (isVehicleHijacked || playerAction == SanaaPlayerActionState.RUNNING) && random.nextFloat() < 0.30f) {
          particles.add(
            Sanaa7DParticle(
              x = playerX + (random.nextFloat() - 0.5f) * 0.14f,
              y = 0.05f + random.nextFloat() * 0.1f,
              vx = (random.nextFloat() - 0.5f) * 0.01f,
              vy = 0.004f + random.nextFloat() * 0.006f,
              size = 4f + random.nextFloat() * 5f,
              color = Color(0xFF8D6E63).copy(alpha = 0.6f),
              alpha = 0.7f,
              lifeMs = nowMs + 600,
              kind = ParticleKind.GROUND_SAND_SWIRL
            )
          )
        }

        val partIter = particles.iterator()
        while (partIter.hasNext()) {
          val pt = partIter.next()
          pt.x += pt.vx
          pt.y += pt.vy
          pt.alpha = ((pt.lifeMs - nowMs) / 450f).coerceIn(0f, 1f)
          if (nowMs >= pt.lifeMs) {
            partIter.remove()
          }
        }

        // ----------------------------------------------------
        // Sana'a Canvas Particle System (Tire Smoke, Sand Dust Clouds, Nitro Sparks)
        // ----------------------------------------------------
        chaseTimeElapsedSeconds += (0.016f * gameSpeedMultiplier)
        val playerScreenX = 400f + playerX * 280f
        val playerScreenY = 850f - playerY * 160f

        if (isVehicleHijacked) {
          particleSystem.emitTireSmoke(
            centerX = playerScreenX,
            centerY = playerScreenY + 25f,
            count = if (isAdrenalineSlowMo) 3 else 2,
            intensity = if (isAdrenalineSlowMo) 1.5f else 1.0f,
            isDrifting = (kotlin.math.abs(playerX) > 0.40f)
          )
          particleSystem.emitSanaaDustCloud(
            centerX = playerScreenX,
            centerY = playerScreenY + 20f,
            count = 2,
            isClayAlley = (currentStage != Sanaa7DStage.STAGE_4_SABEEN_BARRICADES)
          )
        } else if (playerAction == SanaaPlayerActionState.RUNNING || playerAction == SanaaPlayerActionState.ROOFTOP_JUMPING) {
          particleSystem.emitSanaaDustCloud(
            centerX = playerScreenX,
            centerY = playerScreenY + 20f,
            count = 1,
            isClayAlley = true
          )
        }

        if (isAdrenalineSlowMo) {
          particleSystem.emitNitroSparks(playerScreenX, playerScreenY, count = 2)
        }

        particleSystem.update(1.0f)

        delay(16)
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    // 1. Top Bar (shown only when paused or not in active gameplay to maximize 3D screen)
    if (!isPlaying || isPaused || isGameOver || isStageVictory) {
      SanaaTopBar(
        title = "حرامي صنعاء 7D (GTA Sana'a 7D)",
        subtitle = "${currentStage.titleAr} • مطاردات أزقة وبيوت اليمن",
        coins = stats.totalCoins + coinsCollected,
        soundEnabled = stats.soundEnabled,
        onSoundToggle = {
          repository.toggleSound()
          GameSoundEffects.isMuted = !stats.soundEnabled
        },
        onBackClick = onNavigateBack
      )
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // 2. 7D Real-Time Alley & Mud Architecture Canvas
      Sanaa7DAlleyCanvas(
        playerX = playerX,
        playerY = playerY,
        playerAction = playerAction,
        isVehicle = isVehicleHijacked,
        vehicleType = vehicleType,
        cameraAngle = cameraAngle,
        weather = currentWeather,
        wantedStars = wantedStars,
        distance = distanceCovered,
        buildings = buildings,
        obstacles = obstacles,
        policeList = policePursuers,
        gangList = gangThugs,
        hidingSpots = hidingSpots,
        isPlayerHiding = isPlayerHiding,
        currentHidingSpot = currentHidingSpot,
        projectiles = projectiles,
        particles = particles,
        isSlowMo = isAdrenalineSlowMo,
        onDragSteer = { deltaX ->
          playerX = (playerX + deltaX).coerceIn(-1.1f, 1.1f)
        }
      )

      // 2.1 Rich Canvas Particle System Overlay (Tire Smoke, Ancient Street Dust Clouds, Sparks)
      SanaaParticleCanvas(
        particleSystem = particleSystem,
        modifier = Modifier.fillMaxSize()
      )

      // 3. Traditional Yemeni Geometric Corner Framing & Ornamental Motifs
      YemeniGeometricCornerFrame(modifier = Modifier.fillMaxSize())

      // 4. Clean, Minimalist GTA San Andreas Yemen In-Game HUD Overlay
      if (isPlaying && !isGameOver && !isStageVictory) {
        val currentTrack = YemeniHeritageRadio.currentTrack
        val isRadioPlaying = YemeniHeritageRadio.isPlaying
        val nearbyHidingSpot = hidingSpots.firstOrNull { it.worldZ in -2f..16f }
        val nearbyThug = gangThugs.firstOrNull { !it.isBoundInRopes && it.worldZ in -2f..18f }
        val nearbyCop = policePursuers.firstOrNull { it.worldZ in -2f..20f }

        val currentMissionCode = when (currentStage) {
          Sanaa7DStage.STAGE_1_BAB_YEMEN -> "the city chase. (Shuwa'i Sana'a)"
          Sanaa7DStage.STAGE_2_QAMARIYA_ROOFTOPS -> "mountain scramble. Jibal al-Yaman"
          Sanaa7DStage.STAGE_3_DABAB_DRIFT_KENTUCKY -> "dabab drift. Kentucky Heist"
          Sanaa7DStage.STAGE_4_SABEEN_BARRICADES -> "AL KAMEEN. Sabeen Square"
          Sanaa7DStage.STAGE_5_WADI_DHAR_HEIST -> "THE HANDOVER! Wadi Dhar Heist"
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
          // Top HUD Row: [Minimalist Health/Armor ❤️] --- [Objective Banner] --- [MiniMap & Pause & Wanted Stars ★]
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            // Top-Left: Minimalist GTA Health ❤️ & Armor / Adrenaline Bars & Pause Button
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              IconButton(
                onClick = {
                  isPaused = true
                  HapticManager.vibrateMovement()
                },
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(DarkSurface.copy(alpha = 0.90f))
                  .border(1.2.dp, SanaaGold.copy(alpha = 0.8f), CircleShape)
                  .testTag("btn_pause_game")
              ) {
                Icon(
                  imageVector = Icons.Default.Pause,
                  contentDescription = "Pause Game",
                  tint = SanaaGold,
                  modifier = Modifier.size(18.dp)
                )
              }

              GtaHealthArmorMinimalBar(
                health = playerHealth,
                adrenaline = playerAdrenaline,
                isVehicle = isVehicleHijacked,
                vehicleHealth = vehicleHealth,
                modifier = Modifier.testTag("hud_health_armor")
              )
            }

            // Top-Center: Clean Objective Badge with Traditional Yemeni Ornamental Borders
            Surface(
              color = DarkSurface.copy(alpha = 0.88f),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.2.dp, SanaaGold.copy(alpha = 0.85f)),
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .clickable { showStageSelector = true }
                .testTag("hud_objective_badge")
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = "✦ $currentMissionCode ✦",
                    color = SanaaGold,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                  )
                  Surface(
                    color = PoliceRedLight.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "${adaptivePursuitState.tierBadgeEmoji} x${"%.2f".format(adaptivePursuitState.effectivePolicePursuitSpeed)}",
                      color = SanaaGold,
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Text(
                  text = when {
                    isPlayerHiding -> "🥷 متخفٍ (خط النظر مكسور 👁️❌)"
                    capturedGangCount > 0 -> "👮‍♂️ سلّم اللصوص المقبوض عليهم ($capturedGangCount) للشرطة"
                    nearbyThug != null -> "🎯 انقضاض ونهب ${nearbyThug.nameAr}!"
                    isVehicleHijacked -> "🛻 هجولة الشاص الأصفر في شوارع صنعاء"
                    else -> currentStage.titleAr
                  },
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }
            }

            // Top-Right: GTA Wanted Level 5 Stars + Countdown Timer & Weather
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              GtaWantedLevelMinimalHud(
                wantedStars = wantedStars,
                timeLeft = timeLeft,
                distance = distanceCovered.toInt(),
                targetDistance = currentStage.targetDistance.toInt(),
                weatherEmoji = currentWeather.iconEmoji,
                cameraEmoji = cameraAngle.iconEmoji,
                onWeatherClick = { cycleWeather() },
                onCameraClick = { cycleCameraAngle() },
                modifier = Modifier.testTag("hud_wanted_level")
              )

              IconButton(
                onClick = { showTacticalSettings = !showTacticalSettings },
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(if (showTacticalSettings) SanaaGold else DarkSurface.copy(alpha = 0.90f))
                  .border(1.2.dp, SanaaGold.copy(alpha = 0.8f), CircleShape)
                  .testTag("btn_toggle_tactical_settings")
              ) {
                Text(
                  text = if (showTacticalSettings) "✕" else "⚙️",
                  fontSize = 13.sp,
                  color = if (showTacticalSettings) DarkBg else SanaaGold
                )
              }
            }
          }

          // Expandable Secondary Cards (Only shown when user toggles ⚙️, keeping the 7D Canvas vast and unobstructed)
          if (showTacticalSettings) {
            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic Ambient Sound, Sana'a Hero Status & Police Dispatch Ticker
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Interactive Ambient Sound & Proximity Intensity Pill
              Surface(
                color = DarkBg.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(ambientIntensity.colorHex)),
                modifier = Modifier
                  .weight(1.1f)
                  .clickable { showAmbientSoundDialog = true }
                  .testTag("hud_ambient_sound_pill")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(ambientIntensity.badgeEmoji, fontSize = 11.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "🎵 ${ambientTrack.titleAr}",
                      color = SanaaGold,
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1
                    )
                    Text(
                      text = "${ambientIntensity.titleAr} • ${ambientIntensity.tempoBpm} BPM",
                      color = Color(ambientIntensity.colorHex),
                      fontSize = 7.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(4.dp))

              // Sana'a Hero Status & Faster Movement Speed Mini Pill
              Surface(
                color = if (heroTier.isSanaaHeroStatus) Color(0xFF332002) else DarkBg.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (heroTier.isSanaaHeroStatus) SanaaGold else Color(0xFFFF9100).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                  .clickable { showHeroProgressionDialog = true }
                  .testTag("hud_hero_status_pill")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(heroTier.badgeEmoji, fontSize = 11.sp)
                  Spacer(modifier = Modifier.width(3.dp))
                  Column {
                    Text(
                      text = if (heroTier.isSanaaHeroStatus) "👑 بطل صنعاء" else heroTier.statusTitleAr,
                      color = if (heroTier.isSanaaHeroStatus) SanaaGold else Color.White,
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Black
                    )
                    Text(
                      text = "⚡ +${heroTier.speedBoostPercent}% سرعة",
                      color = Color(0xFF00E676),
                      fontSize = 7.5.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(4.dp))

              Surface(
                color = DarkBg.copy(alpha = 0.82f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, TaxiYellow.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1.0f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Notifications, contentDescription = null, tint = TaxiYellow, modifier = Modifier.size(11.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = policeRadioChatter,
                    color = TaxiYellow,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ----------------------------------------------------
            // Score Management & Difficulty Settings HUD
            // ----------------------------------------------------
            ChaseDifficultyAndScoreHud(
              scoreState = scoreState,
              currentDifficulty = difficultyState,
              onDifficultySelected = { newDiff -> viewModel.setDifficulty(newDiff) },
              onResetScore = { viewModel.resetSessionScore() },
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ----------------------------------------------------
            // View Mode Switcher: 7D 3D Chase vs 2D Tactical Alleyway Grid Board
            // ----------------------------------------------------
            Surface(
              color = DarkSurface.copy(alpha = 0.90f),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, SanaaGold.copy(alpha = 0.6f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                Button(
                  onClick = { isTacticalGridMode = false },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isTacticalGridMode) SanaaGold else Color.Transparent
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                ) {
                  Text(
                    text = "🎬 منظور 7D المجسم",
                    color = if (!isTacticalGridMode) DarkBg else Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                  onClick = { isTacticalGridMode = true },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTacticalGridMode) TaxiYellow else Color.Transparent
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .testTag("toggle_tactical_grid_board_btn")
                ) {
                  Text(
                    text = "🗺️ خريطة أزقة صنعاء (Grid)",
                    color = if (isTacticalGridMode) DarkBg else Color.LightGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            if (isTacticalGridMode) {
              Spacer(modifier = Modifier.height(4.dp))
              SanaaAlleywayGridGameBoard(
                difficulty = difficultyState,
                onCollisionDetected = { colType, penalty ->
                  viewModel.triggerCollisionFeedback(colType, "💥 تصادم مع دوريات أزقة صنعاء!", penalty)
                },
                onScoreEarned = { pts, reason ->
                  viewModel.addScorePoints(pts, reason)
                  score += pts
                  coinsCollected += (pts / 5).coerceAtLeast(1)
                },
                modifier = Modifier.fillMaxWidth()
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ----------------------------------------------------
            // Integrated Countdown Timer, Pulsing Stealth Indicator & Level Bar
            // ----------------------------------------------------
            ChaseHudCountdownAndStealthIndicator(
              stealthMode = stealthMode,
              countdownState = countdownState,
              levelState = levelProgressionUiState,
              onLevelClick = { showLevelProgressionDialog = true },
              modifier = Modifier.fillMaxWidth()
            )
          }

          // In-Game Floating XP Reward Notice
          recentXpEarnedNotice?.let { xpNotice ->
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
              color = Color(0xFF00381B).copy(alpha = 0.95f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GangNeonGreen),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("in_game_xp_reward_banner")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("⚡", fontSize = 11.sp)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = xpNotice,
                    color = GangNeonGreen,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black
                  )
                }
                Text(
                  text = "نقاط خبرة",
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 8.5.sp
                )
              }
            }
          }

          // Level Up Celebratory Modal Banner
          recentLeveledUpRank?.let { newRank ->
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
              color = Color(0xFF2C1E03).copy(alpha = 0.98f),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(2.dp, SanaaGold),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showLevelProgressionDialog = true }
                .testTag("level_up_celebration_banner")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(newRank.badgeEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "🎉 ارتقاء مستوى جديد: المستوى ${newRank.level}!",
                    color = SanaaGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black
                  )
                  Text(
                    text = "«${newRank.titleAr}» • ${newRank.perkDescAr}",
                    color = Color.White,
                    fontSize = 9.sp
                  )
                }
                Surface(
                  color = SanaaGold,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "+${newRank.rewardCoinsOnReach}🪙",
                    color = DarkBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }

          // Unlocked Achievement Celebratory Banner
          recentUnlockedAchievementNotice?.let { ach ->
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = Color(0xFF1B1304),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(2.dp, SanaaGold),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("achievement_unlock_banner")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(ach.badgeEmoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "🎖️ وسام صنعائي جديد: ${ach.titleAr}",
                    color = SanaaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                  )
                  Text(
                    text = ach.descriptionAr,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp
                  )
                }
                Surface(
                  color = SanaaGold,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "+${ach.rewardCoins} 🪙",
                    color = DarkBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }

          // Completed Daily Challenge Celebratory Banner
          recentCompletedChallengeNotice?.let { ch ->
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = Color(0xFF071F11),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(2.dp, GangNeonGreen),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_challenge_complete_banner")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(ch.iconEmoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "🎯 تم إنجاز التحدي اليومي: ${ch.titleAr}",
                    color = GangNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                  )
                  Text(
                    text = "${ch.descriptionAr} (${ch.currentProgress}/${ch.targetGoal})",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp
                  )
                }
                Surface(
                  color = GangNeonGreen,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "+${ch.rewardCoins} 🪙",
                    color = DarkBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }

          // In-Game Dynamic Alerts (Loot Gang, Stealth, Handover)
          gangBountyNotice?.let { gNotice ->
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = SanaaGold.copy(alpha = 0.95f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = gNotice,
                color = DarkBg,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1
              )
            }
          }

          stealthBonusNotice?.let { sNotice ->
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = GangNeonGreen.copy(alpha = 0.95f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = sNotice,
                color = DarkBg,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1
              )
            }
          }

          // Prompt if nearby Thug / Cop / Hiding Spot
          if (nearbyThug != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = Color(0xFFFF6D00).copy(alpha = 0.92f),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD600)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { tackleAndLootGang(nearbyThug) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "🎯 انقضاض ونهب ${nearbyThug.nameAr} [${nearbyThug.lootName} +${nearbyThug.lootValue}🪙]",
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Surface(color = DarkBg, shape = RoundedCornerShape(6.dp)) {
                  Text("نهب 🪢", color = Color(0xFFFFD600), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
            }
          }

          if (capturedGangCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = if (nearbyCop != null) PoliceAccent.copy(alpha = 0.95f) else DarkSurface.copy(alpha = 0.90f),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (nearbyCop != null) SanaaGold else PoliceAccent),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { handoverGangToPolice(nearbyCop) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (nearbyCop != null) "👮‍♂️ سلّم لصوص العصابة ($capturedGangCount) للشرطة فوراً!" else "🎒 محتجز معك: $capturedGangCount أفراد عصابة مسلوبين",
                  color = if (nearbyCop != null) Color.White else SanaaGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Surface(color = if (nearbyCop != null) SanaaGold else DarkSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                  Text("تسليم 🎖️", color = if (nearbyCop != null) DarkBg else SanaaGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
            }
          }
        }
      }

      // Initial Chase Start Countdown Animation Overlay (3... 2... 1... انطلق!)
      if (chaseStartCountdown > 0) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.70f)),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            color = DarkSurface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, SanaaGold),
            modifier = Modifier
              .padding(24.dp)
              .testTag("chase_start_countdown_dialog")
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 32.dp, vertical = 22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "🚨 بَدْءُ المُطَارَدَةِ فِي صَنْعَاء 🚨",
                color = SanaaGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "$chaseStartCountdown",
                color = if (chaseStartCountdown == 1) GangNeonGreen else Color(0xFFFF5252),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "استعد للمراوغة والقفز بين الأسطح والتخفي!",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // 5. Tactical Mini-Map Radar Overlay (Positioned in Bottom-End of 7D Chase Canvas)
      if (isPlaying && !isGameOver && !isStageVictory) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 12.dp, end = 12.dp),
          contentAlignment = Alignment.BottomEnd
        ) {
          SanaaChaseMiniMap(
            playerWorldX = playerX,
            isPlayerHiding = isPlayerHiding,
            isVehicleHijacked = isVehicleHijacked,
            policePursuers = policePursuers,
            gangThugs = gangThugs,
            hidingSpots = hidingSpots,
            distanceCovered = distanceCovered,
            targetDistance = currentStage.targetDistance,
            modifier = Modifier.testTag("sanaa_chase_minimap")
          )
        }
      }

      // 6. Clean & Minimalist Game Victory Screen (MISSION PASSED!)
      if (isStageVictory) {
        val earnedCoins = currentStage.rewardCoins.coerceAtLeast(450)
        val earnedXp = lastEarnedXpBreakdown?.totalXpEarned ?: 725

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(horizontal = 24.dp, vertical = 32.dp),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            color = Color(0xDD121824),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, GangNeonGreen),
            shadowElevation = 16.dp,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("clean_victory_screen")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // 1. Huge Glowing Green Cartoon Title
              Text(
                text = "MISSION PASSED!",
                color = GangNeonGreen,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = 2.sp,
                style = TextStyle(
                  shadow = androidx.compose.ui.graphics.Shadow(
                    color = GangNeonGreen.copy(alpha = 0.7f),
                    offset = Offset(0f, 0f),
                    blurRadius = 16f
                  )
                ),
                modifier = Modifier.testTag("mission_passed_title")
              )

              Spacer(modifier = Modifier.height(24.dp))

              // 2. Simple Reward Badges: (🪙 + 450 ريال) and (⭐ + 725 XP)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Gold Reward
                Surface(
                  color = Color(0xFF1E2638),
                  shape = RoundedCornerShape(16.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, TaxiYellow.copy(alpha = 0.5f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("🪙", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "+$earnedCoins ريال",
                      color = TaxiYellow,
                      fontWeight = FontWeight.Black,
                      fontSize = 17.sp
                    )
                  }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // XP Reward
                Surface(
                  color = Color(0xFF1E2638),
                  shape = RoundedCornerShape(16.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, SanaaQamariyaCyan.copy(alpha = 0.5f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("⭐", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "+$earnedXp XP",
                      color = SanaaQamariyaCyan,
                      fontWeight = FontWeight.Black,
                      fontSize = 17.sp
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              // 3. Earned Title Badge
              Surface(
                color = SanaaGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, SanaaGold)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("👑", fontSize = 18.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "بطل صنعاء الأسطوري",
                    color = SanaaGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(36.dp))

              // 4. Two Clean Action Buttons at Bottom
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Secondary Button: Main Menu
                OutlinedButton(
                  onClick = {
                    repository.soundManager.stopChaseMusic()
                    repository.soundManager.stopAmbientStreetSounds()
                    SanaaAmbientSoundManager.stopAmbientMusic()
                    onNavigateBack()
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("btn_victory_main_menu"),
                  shape = RoundedCornerShape(16.dp),
                  border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                  )
                ) {
                  Text(
                    text = "القائمة الرئيسية",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                // Primary Button: Next Stage (Broad Glowing Green)
                Button(
                  onClick = {
                    val nextStageIdx = (currentStage.id) % Sanaa7DStage.values().size
                    startSanaa7DGame(Sanaa7DStage.values()[nextStageIdx])
                  },
                  modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp)
                    .testTag("btn_victory_next_stage"),
                  shape = RoundedCornerShape(16.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = GangNeonGreen
                  ),
                  elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                  Text(
                    text = "التالي ❯",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                  )
                }
              }
            }
          }
        }
      }

      // 6. Game Over Screen (BUSTED / WASTED in Sana'a)
      if (isGameOver) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.92f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(2.dp, PoliceRedLight, RoundedCornerShape(18.dp))
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "BUSTED! تم القبض عليك 🚨",
                color = PoliceRedLight,
                fontWeight = FontWeight.Black,
                fontSize = 19.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "حاصرتك دوريات شرطة صنعاء في أزقة القاسمي!",
                color = Color.White,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.height(8.dp))

              Text(text = "النقاط: $score  •  الغنائم المحصلة: +$coinsCollected 🪙", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

              // Level & XP Gain Progression Card (نقاط الخبرة في نهاية الجولة)
              lastEarnedXpBreakdown?.let { xpBreakdown ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  color = Color(0xFF1F1200).copy(alpha = 0.95f),
                  shape = RoundedCornerShape(12.dp),
                  border = androidx.compose.foundation.BorderStroke(1.2.dp, SanaaGold),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLevelProgressionDialog = true }
                    .testTag("game_over_xp_progression_card")
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(playerLevelInfo.badgeEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                          Text(
                            text = "المستوى ${playerLevelInfo.currentLevel}: ${playerLevelInfo.currentTitleAr}",
                            color = SanaaGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                          )
                          Text(
                            text = if (playerLevelInfo.isMaxLevel) "الرتبة الأسطورية القصوى 👑" else "${playerLevelInfo.currentLevelXp} / ${playerLevelInfo.xpRequiredForCurrentLevelSpan} XP",
                            color = Color.LightGray,
                            fontSize = 8.5.sp
                          )
                        }
                      }
                      Surface(
                        color = SanaaGold,
                        shape = RoundedCornerShape(6.dp)
                      ) {
                        Text(
                          text = "+${xpBreakdown.totalXpEarned} XP ⚡",
                          color = DarkBg,
                          fontWeight = FontWeight.Black,
                          fontSize = 10.5.sp,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                      progress = { playerLevelInfo.progressRatio },
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                      color = GangNeonGreen,
                      trackColor = Color(0xFF332000)
                    )
                  }
                }
              }

              // Yemeni Badges Earned Section in Game Over
              if (unlockedAchievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "🎖️ الأوسمة التي حققتها قبل القبض عليك:",
                  color = SanaaGold,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  for (badge in unlockedAchievements.take(3)) {
                    Surface(
                      color = DarkBg.copy(alpha = 0.8f),
                      shape = RoundedCornerShape(6.dp),
                      border = androidx.compose.foundation.BorderStroke(0.8.dp, SanaaGold.copy(alpha = 0.5f)),
                      modifier = Modifier.weight(1f)
                    ) {
                      Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(badge.badgeEmoji, fontSize = 14.sp)
                        Text(badge.titleAr, color = SanaaGold, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                  onClick = { showLeaderboardModal = true },
                  modifier = Modifier.weight(1f).height(42.dp),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("لوحة الشرف 🏆", color = SanaaGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                  onClick = onNavigateBack,
                  modifier = Modifier.weight(0.85f).height(42.dp),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("القائمة", color = Color.White, fontSize = 10.5.sp)
                }
                Button(
                  onClick = { startSanaa7DGame(currentStage) },
                  colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
                  modifier = Modifier.weight(1.15f).height(42.dp),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("إعادة 🔄", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }

    // 7. Interactive Modern Minimalist 7D Action Controls Panel
    if (isPlaying && !isGameOver && !isStageVictory) {
      Surface(
        color = DarkSurface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SanaaGold.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .navigationBarsPadding()
        ) {
          // Primary Action Row: Jump, Slide, Loot Gang, Handover
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = { performParkourJump() },
              colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(42.dp).testTag("btn_7d_jump")
            ) {
              Text("🦘 قفز الأسطح", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            Button(
              onClick = { performAlleySlide() },
              colors = ButtonDefaults.buttonColors(containerColor = TaxiYellow),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(42.dp).testTag("btn_7d_slide")
            ) {
              Text("⛷️ انزلاق", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            val nearestThug = gangThugs.firstOrNull { !it.isBoundInRopes && it.worldZ in -4f..26f }
            Button(
              onClick = {
                if (nearestThug != null) {
                  tackleAndLootGang(nearestThug)
                } else {
                  gangBountyNotice = "🔍 ابحث عن أفراد العصابات في أزقة صنعاء للانقضاض ونهبهم!"
                  gangNoticeTimer = 90
                  GameSoundEffects.playWalkieTalkie()
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (nearestThug != null) Color(0xFFFF6D00) else DarkSurfaceVariant
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1.1f).height(42.dp).testTag("btn_tackle_gang")
            ) {
              Text("🎯 نهب 💰", color = if (nearestThug != null) Color.White else SanaaGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            val nearestCop = policePursuers.firstOrNull { it.worldZ in -4f..26f }
            Button(
              onClick = { handoverGangToPolice(nearestCop) },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (capturedGangCount > 0) PoliceAccent else DarkSurfaceVariant
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1.1f).height(42.dp).testTag("btn_handover_police")
            ) {
              Text(
                text = if (capturedGangCount > 0) "👮‍♂️ تسليم ($capturedGangCount) 🎖️" else "👮‍♂️ تسليم",
                color = if (capturedGangCount > 0) Color.White else Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Secondary Quick Row: Hijack Shas/Dabab, Stealth Hiding, SlowMo, Fireworks, Radio, Stage Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            Button(
              onClick = { hijackNearbyVehicle() },
              colors = ButtonDefaults.buttonColors(containerColor = if (isVehicleHijacked) GangShawlRed else DarkSurfaceVariant),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp).testTag("btn_7d_hijack")
            ) {
              Text(if (isVehicleHijacked) "🛻 شاص/دباب" else "🚐 ركوب", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            val nearestSpot = hidingSpots.firstOrNull { it.worldZ in -4f..20f }
            Button(
              onClick = {
                if (isPlayerHiding) {
                  exitHidingSpot()
                } else if (nearestSpot != null) {
                  enterHidingSpot(nearestSpot)
                } else {
                  stealthBonusNotice = "🚪 ابحث عن باب خشبي عتيق أو سلم للاختباء!"
                  stealthNoticeTimer = 90
                  GameSoundEffects.playDoorCreak()
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlayerHiding) GangNeonGreen else if (nearestSpot != null) SanaaGold else DarkSurfaceVariant
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp).testTag("btn_7d_hide")
            ) {
              Text(if (isPlayerHiding) "🏃‍♂️ خروج" else "🥷 اختباء", color = if (isPlayerHiding || nearestSpot != null) DarkBg else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { triggerAdrenalineSlowMo() },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isAdrenalineSlowMo) Color(0xFF00E5FF) else DarkSurfaceVariant
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp).testTag("btn_7d_slowmo")
            ) {
              Text("⚡ تبطيء", color = if (isAdrenalineSlowMo) DarkBg else Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { shootSanaaWeapon(isFireworks = true) },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp).testTag("btn_7d_firework")
            ) {
              Text("🎆 نارية", color = Color(0xFFFFD600), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                YemeniHeritageRadio.nextTrack()
                stealthBonusNotice = "📻 ${YemeniHeritageRadio.currentTrack.titleAr}"
                stealthNoticeTimer = 70
                GameSoundEffects.playWalkieTalkie()
              },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(0.85f).height(36.dp).testTag("btn_7d_radio")
            ) {
              Text("📻", color = SanaaGold, fontSize = 11.sp)
            }

            Button(
              onClick = {
                val completed = dailyChallenges.count { it.isCompleted }
                stealthBonusNotice = "🎯 التحديات: $completed من ${dailyChallenges.size} منجزة"
                stealthNoticeTimer = 70
                GameSoundEffects.playRadioBeep()
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (dailyChallenges.any { it.isCompleted }) Color(0xFF1B5E20) else DarkSurfaceVariant
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(0.85f).height(36.dp).testTag("btn_7d_challenges")
            ) {
              Text("🎯", color = SanaaGold, fontSize = 11.sp)
            }

            Button(
              onClick = {
                GameSoundEffects.playCarHorn()
                policePursuers.filter { it.worldZ in 0f..45f }.forEach { it.isStunned = true }
                stealthBonusNotice = "📢 إطلاق بوري وصافرة صنعاء لتشتيت الدوريات!"
                stealthNoticeTimer = 60
              },
              colors = ButtonDefaults.buttonColors(containerColor = SanaaGold.copy(alpha = 0.85f)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(0.85f).height(36.dp).testTag("btn_7d_horn")
            ) {
              Text("📢", color = DarkBg, fontSize = 11.sp)
            }
          }
        }
      }
    }

    // 6. Blur Effect Pause Menu Overlay (triggered by Back button or Pause icon)
    if (isPaused) {
      BlurPauseMenuOverlay(
        currentStage = currentStage,
        adaptivePursuitState = adaptivePursuitState,
        currentScore = score,
        coinsCollected = coinsCollected,
        distanceCovered = distanceCovered,
        copsEvaded = copsEvaded,
        onResumeGame = {
          isPaused = false
          repository.soundManager.resumeChaseMusic()
          SanaaAmbientSoundManager.resumeAmbientMusic()
        },
        onReturnToCharacterSelection = {
          repository.soundManager.stopChaseMusic()
          repository.soundManager.stopAmbientStreetSounds()
          SanaaAmbientSoundManager.stopAmbientMusic()
          onNavigateBack()
        },
        onRestartStage = {
          isPaused = false
          startSanaa7DGame(currentStage)
        },
        onOpenLeaderboard = {
          showLeaderboardModal = true
        },
        successfulMissionsCount = stats.successfulMissionsCount,
        isDevMode = isDevActive,
        onOpenHeroProgression = {
          showHeroProgressionDialog = true
        },
        onOpenAmbientSound = {
          showAmbientSoundDialog = true
        }
      )
    }

    // 7. Milestone Celebration Toast HUD Overlay (New Personal Best, Level Up, Fastest Escapes)
    MilestoneToastHudOverlay()
  }

  // Sana'a Hero Progression Roadmap & Speed Boost Unlocks Modal
  if (showHeroProgressionDialog && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    SanaaHeroProgressionModal(
      stats = stats,
      isDevMode = isDevActive,
      onDismiss = { showHeroProgressionDialog = false }
    )
  }

  // Sana'a Traditional Ambient Sound & Proximity Intensity Live Mixer Modal
  if (showAmbientSoundDialog && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    SanaaAmbientSoundModal(
      onDismiss = { showAmbientSoundDialog = false }
    )
  }

  // Stage Selector Dialog (Direct 1-Tap stage jump in Sana'a)
  if (showStageSelector && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    AlertDialog(
      onDismissRequest = { showStageSelector = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("🗺️", fontSize = 20.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text("اختيار مرحلة مطاردة صنعاء", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (isDevActive) {
            Surface(
              color = GangNeonGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, GangNeonGreen),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "👑 وضع الأدمن (mazengalab): كافة المراحل مفتوحة بالكامل بدون حدود!",
                color = GangNeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          } else {
            Surface(
              color = Color(0xFF00E5FF).copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "🆓 المراحل (1، 2، 3) مجانية بالكامل ومفتوحة للدخول البسيط والمباشر!",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          }

          Sanaa7DStage.values().forEach { stage ->
            val isSelected = currentStage == stage
            val isFreeStage = stage.id <= 3
            val isUnlocked = isFreeStage || isDevActive || completedStages.contains(stage.id)

            Surface(
              color = if (isSelected) SanaaGold.copy(alpha = 0.25f) else DarkSurfaceVariant,
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) SanaaGold else if (isFreeStage) Color(0xFF00E5FF).copy(alpha = 0.7f) else DarkCardBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  startSanaa7DGame(stage)
                  showStageSelector = false
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stage.titleAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    if (isFreeStage) {
                      Spacer(modifier = Modifier.width(4.dp))
                      Surface(
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                      ) {
                        Text("مجانية 🆓", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                      }
                    } else if (isDevActive) {
                      Spacer(modifier = Modifier.width(4.dp))
                      Surface(
                        color = GangNeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                      ) {
                        Text("أدمن 👑", color = GangNeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                      }
                    }
                  }
                  Text(stage.subtitleAr, color = Color.LightGray, fontSize = 9.sp, maxLines = 1)
                }
                Text("+${stage.rewardCoins} 🪙", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showStageSelector = false },
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold)
        ) {
          Text("إغلاق", color = DarkBg, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface
    )
  }

  // Yemeni Heritage Music & Poetry Radio Modal
  if (showHeritageRadioDialog && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    YemeniHeritageRadioModal(
      onDismiss = { showHeritageRadioDialog = false }
    )
  }

  // Character Dossier Dialog (Detailed view of real photos and backstories)
  if (showCharacterDossierDialog && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    AlertDialog(
      onDismissRequest = { showCharacterDossierDialog = false },
      title = {
        Text("ملف أبطال مطاردة صنعاء 7D", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.sanaa_kid_leader),
              contentDescription = null,
              modifier = Modifier.size(50.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("زعيم المشاغبين (مازن)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("الطفل الذكي والشجاع، خبير أزقة صنعاء وأسطحها ومزاريبها ومطارد العصابات.", color = Color.LightGray, fontSize = 10.sp)
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.sanaa_police_commander),
              contentDescription = null,
              modifier = Modifier.size(50.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("العقيد ناصر (مدير المباحث)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("قائد عمليات المطاردة وغرفة الاتصالات والتحريات في العاصمة صنعاء.", color = Color.LightGray, fontSize = 10.sp)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showCharacterDossierDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold)
        ) {
          Text("إغلاق", color = DarkBg, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface
    )
  }

  // Daily Renewable Challenges Dialog
  if (showDailyChallengesDialog && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    DailySanaaChallengesDialog(
      challenges = dailyChallenges,
      onDismiss = { showDailyChallengesDialog = false },
      onRefresh = { refreshDailyChallenges() }
    )
  }

  // Player Level & XP Progression Roadmap Dialog
  if (showLevelProgressionDialog && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    PlayerLevelProgressionDialog(
      stats = stats,
      onDismiss = { showLevelProgressionDialog = false }
    )
  }

  // Top 10 High Scores & Best Chase Times Leaderboard Modal
  if (showLeaderboardModal && (!isPlaying || isPaused || isGameOver || isStageVictory)) {
    androidx.compose.ui.window.Dialog(
      onDismissRequest = { showLeaderboardModal = false },
      properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(DarkBg)
      ) {
        Top10LeaderboardScreen(
          repository = repository,
          onNavigateBack = { showLeaderboardModal = false },
          onStartChallenge = {
            showLeaderboardModal = false
            if (!isPlaying || isGameOver || isStageVictory) {
              startSanaa7DGame(currentStage)
            }
          }
        )
      }
    }
  }
}

// ----------------------------------------------------
// Player Level & XP Progression Dialog Composable (خارطة مستويات ورتب صنعاء)
// ----------------------------------------------------
@Composable
fun PlayerLevelProgressionDialog(
  stats: GameStats,
  onDismiss: () -> Unit
) {
  val levelInfo = PlayerLevelSystem.getLevelInfo(stats.playerXp)
  val allRanks = PlayerLevelSystem.ranks

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "👑 رتب ومستويات بطل صنعاء (XP System)",
          color = SanaaGold,
          fontWeight = FontWeight.Black,
          fontSize = 15.sp
        )
        Text(
          text = "ارتقِ بمستواك عبر المطاردات والباركور ونهب العصابات لفتح امتيازات حصرية",
          color = Color.LightGray,
          fontSize = 9.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Current Level Hero Status Card
        Surface(
          color = DarkBg.copy(alpha = 0.95f),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, SanaaGold)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(levelInfo.badgeEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "المستوى الحالي: ${levelInfo.currentLevel}",
                    color = GangNeonGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.5.sp
                  )
                  Text(
                    text = levelInfo.currentTitleAr,
                    color = SanaaGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }
              }
              Surface(
                color = SanaaGold,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "إجمالي: ${stats.playerXp} XP",
                  color = DarkBg,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress to next level
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (levelInfo.isMaxLevel) "👑 الرتبة القصوى" else "التقدم نحو المستوى التالي (${(levelInfo.progressRatio * 100).toInt()}%)",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (levelInfo.isMaxLevel) "مكتمل 100%" else "${levelInfo.currentLevelXp} / ${levelInfo.xpRequiredForCurrentLevelSpan} XP",
                color = GangNeonGreen,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { levelInfo.progressRatio },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = GangNeonGreen,
              trackColor = Color(0xFF2B2B2B)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "🏆 خارطة الرتب والمكافآت (10 رتب صنعائية):",
          color = SanaaGold,
          fontWeight = FontWeight.Black,
          fontSize = 11.5.sp
        )

        // Ranks Roadmap List
        allRanks.forEach { rank ->
          val isCurrent = rank.level == levelInfo.currentLevel
          val isUnlocked = levelInfo.currentLevel >= rank.level

          Surface(
            color = when {
              isCurrent -> Color(0xFF1F1803).copy(alpha = 0.95f)
              isUnlocked -> DarkBg.copy(alpha = 0.85f)
              else -> Color(0xFF141414).copy(alpha = 0.65f)
            },
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
              if (isCurrent) 1.5.dp else 0.8.dp,
              when {
                isCurrent -> SanaaGold
                isUnlocked -> GangNeonGreen.copy(alpha = 0.6f)
                else -> Color(0xFF333333)
              }
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = rank.badgeEmoji,
                fontSize = 20.sp,
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.45f)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Lv.${rank.level} - ${rank.titleAr}",
                    color = if (isCurrent) SanaaGold else if (isUnlocked) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                  )
                  if (isCurrent) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(color = SanaaGold, shape = RoundedCornerShape(4.dp)) {
                      Text("رتبتك الحالية", color = DarkBg, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                  }
                }
                Text(
                  text = "المتطلب: ${rank.requiredTotalXp} XP • ${rank.perkDescAr}",
                  color = if (isUnlocked) Color.LightGray else Color(0xFF777777),
                  fontSize = 8.5.sp
                )
              }
              Surface(
                color = if (isUnlocked) GangNeonGreen.copy(alpha = 0.2f) else DarkSurfaceVariant,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = if (isUnlocked) "مكتسب 🪙+${rank.rewardCoinsOnReach}" else "🔒 +${rank.rewardCoinsOnReach}🪙",
                  color = if (isUnlocked) GangNeonGreen else Color.Gray,
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = SanaaGold)
      ) {
        Text("إغلاق", color = DarkBg, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = DarkSurface
  )
}

// ----------------------------------------------------
// Daily Renewable Challenges Dialog Composable (قائمة التحديات اليومية المتجددة)
// ----------------------------------------------------
@Composable
fun DailySanaaChallengesDialog(
  challenges: List<DailySanaaChallenge>,
  onDismiss: () -> Unit,
  onRefresh: () -> Unit
) {
  val completedCount = challenges.count { it.isCompleted }
  val totalBonus = challenges.filter { it.isCompleted }.sumOf { it.rewardCoins }
  val allDone = completedCount == challenges.size && challenges.isNotEmpty()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "🎯 قائمة التحديات اليومية المتجددة",
          color = SanaaGold,
          fontWeight = FontWeight.Black,
          fontSize = 15.sp
        )
        Text(
          text = "مهام يومية حصرية لمضاعفة وتيرة اللعب وكسب العملات والمكافآت",
          color = Color.LightGray,
          fontSize = 9.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Daily Progress Summary Header
        Surface(
          color = DarkBg.copy(alpha = 0.9f),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, if (allDone) GangNeonGreen else SanaaGold)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "الإنجاز اليومي: $completedCount من ${challenges.size}",
                color = if (allDone) GangNeonGreen else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
              )
              Surface(
                color = if (allDone) GangNeonGreen else SanaaGold,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "+$totalBonus 🪙 محصلة",
                  color = DarkBg,
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { if (challenges.isEmpty()) 0f else completedCount.toFloat() / challenges.size },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = if (allDone) GangNeonGreen else SanaaGold,
              trackColor = Color(0xFF2B2B2B)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⏱️ تتجدد التحديات تلقائياً كل 24 ساعة",
                color = TaxiYellow,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              if (allDone) {
                Text(
                  text = "👑 بطل اليوم مكتمل!",
                  color = GangNeonGreen,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }

        // List of Daily Challenges
        challenges.forEach { challenge ->
          val progressRatio = (challenge.currentProgress.toFloat() / challenge.targetGoal).coerceIn(0f, 1f)
          Surface(
            color = if (challenge.isCompleted) Color(0xFF0D2818) else DarkSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (challenge.isCompleted) GangNeonGreen else DarkCardBorder
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Text(text = challenge.iconEmoji, fontSize = 18.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = challenge.titleAr,
                      color = if (challenge.isCompleted) GangNeonGreen else SanaaGold,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.5.sp
                    )
                    Text(
                      text = challenge.categoryAr,
                      color = Color.LightGray.copy(alpha = 0.8f),
                      fontSize = 8.5.sp
                    )
                  }
                }

                Surface(
                  color = if (challenge.isCompleted) GangNeonGreen else SanaaGold.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(6.dp),
                  border = androidx.compose.foundation.BorderStroke(0.8.dp, if (challenge.isCompleted) GangNeonGreen else SanaaGold)
                ) {
                  Text(
                    text = if (challenge.isCompleted) "مكتمل ✓ (+${challenge.rewardCoins}🪙)" else "+${challenge.rewardCoins} 🪙",
                    color = if (challenge.isCompleted) DarkBg else SanaaGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = challenge.descriptionAr,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 9.5.sp,
                lineHeight = 13.sp
              )

              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                LinearProgressIndicator(
                  progress = { progressRatio },
                  modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = if (challenge.isCompleted) GangNeonGreen else SanaaGold,
                  trackColor = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${challenge.currentProgress} / ${challenge.targetGoal}",
                  color = if (challenge.isCompleted) GangNeonGreen else Color.LightGray,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onRefresh,
          modifier = Modifier.weight(1f).height(38.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("تجديد المهام 🔄", color = TaxiYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
          modifier = Modifier.weight(1f).height(38.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("إغلاق", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    },
    containerColor = DarkSurface
  )
}

// ----------------------------------------------------
// Traditional Yemeni Geometric Pattern Frame Canvas
// ----------------------------------------------------
@Composable
fun YemeniGeometricCornerFrame(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val gold = Color(0xFFD4AF37)
    val cornerSize = 28.dp.toPx()
    val strokeWidth = 2.dp.toPx()
    val step = 8.dp.toPx()

    // 1. Top-Left Corner
    drawLine(color = gold, start = Offset(12f, 12f), end = Offset(12f + cornerSize, 12f), strokeWidth = strokeWidth)
    drawLine(color = gold, start = Offset(12f, 12f), end = Offset(12f, 12f + cornerSize), strokeWidth = strokeWidth)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(12f + step, 12f + step), end = Offset(12f + cornerSize - step, 12f + step), strokeWidth = 1.2f)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(12f + step, 12f + step), end = Offset(12f + step, 12f + cornerSize - step), strokeWidth = 1.2f)
    drawCircle(color = gold, radius = 2.5.dp.toPx(), center = Offset(12f + step * 0.5f, 12f + step * 0.5f))

    // 2. Top-Right Corner
    val right = size.width - 12f
    drawLine(color = gold, start = Offset(right, 12f), end = Offset(right - cornerSize, 12f), strokeWidth = strokeWidth)
    drawLine(color = gold, start = Offset(right, 12f), end = Offset(right, 12f + cornerSize), strokeWidth = strokeWidth)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(right - step, 12f + step), end = Offset(right - cornerSize + step, 12f + step), strokeWidth = 1.2f)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(right - step, 12f + step), end = Offset(right - step, 12f + cornerSize - step), strokeWidth = 1.2f)
    drawCircle(color = gold, radius = 2.5.dp.toPx(), center = Offset(right - step * 0.5f, 12f + step * 0.5f))

    // 3. Bottom-Left Corner
    val bottom = size.height - 12f
    drawLine(color = gold, start = Offset(12f, bottom), end = Offset(12f + cornerSize, bottom), strokeWidth = strokeWidth)
    drawLine(color = gold, start = Offset(12f, bottom), end = Offset(12f, bottom - cornerSize), strokeWidth = strokeWidth)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(12f + step, bottom - step), end = Offset(12f + cornerSize - step, bottom - step), strokeWidth = 1.2f)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(12f + step, bottom - step), end = Offset(12f + step, bottom - cornerSize + step), strokeWidth = 1.2f)
    drawCircle(color = gold, radius = 2.5.dp.toPx(), center = Offset(12f + step * 0.5f, bottom - step * 0.5f))

    // 4. Bottom-Right Corner
    drawLine(color = gold, start = Offset(right, bottom), end = Offset(right - cornerSize, bottom), strokeWidth = strokeWidth)
    drawLine(color = gold, start = Offset(right, bottom), end = Offset(right, bottom - cornerSize), strokeWidth = strokeWidth)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(right - step, bottom - step), end = Offset(right - cornerSize + step, bottom - step), strokeWidth = 1.2f)
    drawLine(color = gold.copy(alpha = 0.6f), start = Offset(right - step, bottom - step), end = Offset(right - step, bottom - cornerSize + step), strokeWidth = 1.2f)
    drawCircle(color = gold, radius = 2.5.dp.toPx(), center = Offset(right - step * 0.5f, bottom - step * 0.5f))
  }
}

// ----------------------------------------------------
// Minimalist GTA Health & Armor Display (Top-Left)
// ----------------------------------------------------
@Composable
fun GtaHealthArmorMinimalBar(
  health: Float,
  adrenaline: Float,
  isVehicle: Boolean,
  vehicleHealth: Float,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Red Heart Icon
    Surface(
      color = Color(0xFFB71C1C),
      shape = CircleShape,
      border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD54F)),
      modifier = Modifier.size(24.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text("❤️", fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.width(5.dp))

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      // 1. Red Health Bar
      Box(
        modifier = Modifier
          .width(88.dp)
          .height(8.dp)
          .background(Color(0xFF370000), RoundedCornerShape(2.dp))
          .border(1.dp, Color(0xFF212121), RoundedCornerShape(2.dp))
      ) {
        val healthFraction = if (isVehicle) (vehicleHealth / 180f).coerceIn(0f, 1f) else (health / 100f).coerceIn(0f, 1f)
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(healthFraction)
            .background(if (health > 30) Color(0xFFFF1744) else Color(0xFFD50000), RoundedCornerShape(2.dp))
        )
      }

      // 2. Green Armor / Adrenaline Bar
      Box(
        modifier = Modifier
          .width(88.dp)
          .height(7.dp)
          .background(Color(0xFF002910), RoundedCornerShape(2.dp))
          .border(1.dp, Color(0xFF212121), RoundedCornerShape(2.dp))
      ) {
        val armorFraction = (adrenaline / 100f).coerceIn(0f, 1f)
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(armorFraction)
            .background(Color(0xFF00E676), RoundedCornerShape(2.dp))
        )
      }
    }
  }
}

// ----------------------------------------------------
// Minimalist GTA Wanted Level & Timer (Top-Right)
// ----------------------------------------------------
@Composable
fun GtaWantedLevelMinimalHud(
  wantedStars: Int,
  timeLeft: Int,
  distance: Int,
  targetDistance: Int,
  weatherEmoji: String,
  cameraEmoji: String,
  onWeatherClick: () -> Unit,
  onCameraClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.End
  ) {
    // 5 Wanted Stars
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      for (i in 1..5) {
        Text(
          text = "★",
          color = if (i <= wantedStars) Color(0xFFFFD600) else Color(0xFF424242),
          fontSize = 15.sp,
          fontWeight = FontWeight.Black
        )
      }
    }

    Text(
      text = "WANTED LEVEL",
      color = if (wantedStars > 0) Color(0xFFFF5252) else Color(0xFFE0E0E0),
      fontSize = 8.5.sp,
      fontWeight = FontWeight.Black,
      letterSpacing = 0.8.sp
    )

    Spacer(modifier = Modifier.height(2.dp))

    // Time Left & Weather/Camera quick buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Surface(
        color = DarkSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable { onWeatherClick() }
      ) {
        Text(weatherEmoji, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
      }

      Surface(
        color = DarkSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable { onCameraClick() }
      ) {
        Text(cameraEmoji, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
      }

      Surface(
        color = if (timeLeft <= 15) Color(0xFFD50000) else DarkSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(6.dp),
        border = if (timeLeft <= 15) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)) else null
      ) {
        Text(
          text = "⏱️ ${timeLeft}ث  📍 $distance/${targetDistance}م",
          color = Color.White,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
      }
    }
  }
}

// ----------------------------------------------------
// Safe DrawText Extension to Prevent Constraint Exceptions
// ----------------------------------------------------
private fun androidx.compose.ui.graphics.drawscope.DrawScope.safeDrawText(
  textMeasurer: androidx.compose.ui.text.TextMeasurer,
  text: String,
  topLeft: Offset,
  style: TextStyle = TextStyle.Default,
  allocatedWidth: Float = 260f,
  allocatedHeight: Float = 100f
) {
  val canvasW = size.width
  val canvasH = size.height
  // Avoid drawing if offscreen or invalid coordinates
  if (topLeft.x >= canvasW || topLeft.y >= canvasH || topLeft.x <= -allocatedWidth || topLeft.y <= -allocatedHeight) {
    return
  }
  val safeW = allocatedWidth.coerceAtLeast(20f)
  val safeH = allocatedHeight.coerceAtLeast(20f)
  try {
    drawText(
      textMeasurer = textMeasurer,
      text = text,
      topLeft = topLeft,
      style = style,
      size = Size(safeW, safeH)
    )
  } catch (_: Throwable) {
    // Gracefully handle any internal text constraint exceptions
  }
}

// ----------------------------------------------------
// 7D Sana'a Alleyway Custom Render Canvas
// ----------------------------------------------------
@Composable
fun Sanaa7DAlleyCanvas(
  playerX: Float,
  playerY: Float,
  playerAction: SanaaPlayerActionState,
  isVehicle: Boolean,
  vehicleType: String,
  cameraAngle: Sanaa7DCameraAngle,
  weather: SanaaWeatherType,
  wantedStars: Int,
  distance: Float,
  buildings: List<Sanaa7DBuildingScenery>,
  obstacles: List<Sanaa7DObstacle>,
  policeList: List<Sanaa7DPoliceOfficer>,
  gangList: List<SanaaGangThug>,
  hidingSpots: List<Sanaa7DHidingSpot>,
  isPlayerHiding: Boolean,
  currentHidingSpot: Sanaa7DHidingSpot?,
  projectiles: List<Sanaa7DProjectile>,
  particles: List<Sanaa7DParticle>,
  isSlowMo: Boolean,
  onDragSteer: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          onDragSteer(dragAmount.x * 0.0055f)
        }
      }
  ) {
    val canvasW = size.width
    val canvasH = size.height

    val horizonY = when (cameraAngle) {
      Sanaa7DCameraAngle.DRONE_ROOFTOP -> canvasH * 0.28f
      Sanaa7DCameraAngle.SHOULDER_AIM -> canvasH * 0.45f
      else -> canvasH * 0.38f
    }
    val vanishX = canvasW * 0.5f

    // 1. Dynamic Weather Sky & Atmospheric Horizon
    drawRect(
      brush = Brush.verticalGradient(
        colors = weather.skyColors,
        startY = 0f,
        endY = horizonY
      ),
      size = Size(canvasW, horizonY)
    )

    // Dynamic Sun / Solar Flare & Sun Rays (ضوء الشمس المنعكس على صنعاء)
    val sunCenter = Offset(canvasW * weather.sunPositionX, horizonY * 0.42f)
    if (weather.sunIntensity > 0.1f) {
      // Radiant Solar Corona
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            weather.sunGlowColor.copy(alpha = weather.sunIntensity * 0.85f),
            weather.sunGlowColor.copy(alpha = weather.sunIntensity * 0.35f),
            Color.Transparent
          ),
          center = sunCenter,
          radius = 120f * weather.sunIntensity
        ),
        center = sunCenter,
        radius = 120f * weather.sunIntensity
      )

      // Solid Sun Core
      drawCircle(
        color = Color(0xFFFFFFEE).copy(alpha = weather.sunIntensity),
        center = sunCenter,
        radius = 22f * weather.sunIntensity
      )

      // Diagonal Volumetric Light Rays (God Rays) across Sana'a Valley
      for (rayAngle in listOf(-0.35f, -0.15f, 0.10f, 0.30f, 0.55f)) {
        val rayPath = Path().apply {
          moveTo(sunCenter.x, sunCenter.y)
          lineTo(sunCenter.x + (canvasW * 0.35f * rayAngle) - 40f, canvasH * 0.85f)
          lineTo(sunCenter.x + (canvasW * 0.35f * rayAngle) + 40f, canvasH * 0.85f)
          close()
        }
        drawPath(
          path = rayPath,
          color = weather.sunGlowColor.copy(alpha = weather.sunIntensity * 0.12f)
        )
      }
    }

    // 2. Far Skyline: Sana'a Mountain Silhouette (Jabal Nuqum / Aiban)
    val mountainPath = Path().apply {
      moveTo(0f, horizonY)
      lineTo(canvasW * 0.18f, horizonY - 45f)
      lineTo(canvasW * 0.35f, horizonY - 25f)
      lineTo(canvasW * 0.55f, horizonY - 55f)
      lineTo(canvasW * 0.80f, horizonY - 30f)
      lineTo(canvasW, horizonY - 40f)
      lineTo(canvasW, horizonY)
      close()
    }
    drawPath(mountainPath, color = Color(0xFF263238).copy(alpha = 0.85f))

    // Atmospheric Weather Fog Haze over Mountains & Distant Horizon
    if (weather.fogColor != Color.Transparent) {
      drawRect(
        color = weather.fogColor,
        topLeft = Offset(0f, horizonY - 60f),
        size = Size(canvasW, 70f)
      )
    }

    // 2.5. Ancient Sana'a Mud & Earth Ground Terrain Base (Rich Earthy Bedrock)
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(
          Color(0xFF3E2723),
          Color(0xFF2D1B17),
          Color(0xFF1B110E)
        ),
        startY = horizonY,
        endY = canvasH
      ),
      topLeft = Offset(0f, horizonY),
      size = Size(canvasW, canvasH - horizonY)
    )

    // 3. 7D Perspective Mud Brick Buildings & Stained Glass Qamariya Windows
    for (b in buildings) {
      val z = b.distanceZ.coerceAtLeast(1f)
      val scale = (180f / (z + 40f)).coerceIn(0.08f, 1.8f)

      val screenX = vanishX + (b.side * (canvasW * 0.45f)) * scale
      val bWidth = 85f * scale
      val bHeight = (180f * b.buildingHeight) * scale
      val bTop = horizonY - bHeight + (40f * scale)

      // Base Wall Color adjusted for Weather Lighting
      val buildingWallColor = if (weather == SanaaWeatherType.HIGHLAND_RAIN_GLEAM) {
        b.wallColor.copy(alpha = 0.95f)
      } else {
        b.wallColor
      }

      // Draw Yemeni Mud Tower
      drawRect(
        color = buildingWallColor,
        topLeft = Offset(if (b.side < 0) screenX - bWidth else screenX, bTop),
        size = Size(bWidth, bHeight + (canvasH - horizonY))
      )

      // Sunbeam Wall Highlight / Golden Stone Reflection
      if (weather.sunIntensity > 0.3f) {
        val isFacingSun = (weather.sunPositionX > 0.5f && b.side > 0) || (weather.sunPositionX <= 0.5f && b.side < 0)
        if (isFacingSun) {
          drawRect(
            brush = Brush.horizontalGradient(
              colors = listOf(
                weather.sunGlowColor.copy(alpha = weather.sunIntensity * 0.30f),
                Color.Transparent
              ),
              startX = if (b.side < 0) screenX else screenX - bWidth,
              endX = if (b.side < 0) screenX - bWidth else screenX
            ),
            topLeft = Offset(if (b.side < 0) screenX - bWidth else screenX, bTop),
            size = Size(bWidth, bHeight + (canvasH - horizonY))
          )
        }
      }

      // White Gypsum Ornaments (Taqwees)
      drawRect(
        color = Color.White.copy(alpha = if (weather == SanaaWeatherType.SWIRLING_DUST_STORM) 0.65f else 0.88f),
        topLeft = Offset(if (b.side < 0) screenX - bWidth else screenX, bTop),
        size = Size(bWidth, 6f * scale)
      )

      // Stained Glass Qamariya Window with Sunlight Illumination
      if (b.hasQamariya && scale > 0.25f) {
        val qRadius = 14f * scale
        val qCenter = Offset(
          if (b.side < 0) screenX - (bWidth * 0.5f) else screenX + (bWidth * 0.5f),
          bTop + (35f * scale)
        )
        // Radiant stained glass glow influenced by weather
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFEB3B).copy(alpha = 0.95f),
              Color(0xFFE91E63).copy(alpha = 0.85f),
              Color(0xFF00E5FF).copy(alpha = 0.75f)
            ),
            center = qCenter,
            radius = qRadius
          ),
          center = qCenter,
          radius = qRadius
        )

        // Qamariya sun reflection halo
        if (weather.sunIntensity > 0.4f) {
          drawCircle(
            color = weather.sunGlowColor.copy(alpha = 0.25f),
            center = qCenter,
            radius = qRadius * 1.5f
          )
        }
      }
    }

    // 4. Ground: Ancient Sana'a Stone Cobblestone Alley (حجارة الزقاق الصنعاني)
    val alleyLeftAtBottom = canvasW * 0.08f
    val alleyRightAtBottom = canvasW * 0.92f
    val alleyLeftAtHorizon = vanishX - 25f
    val alleyRightAtHorizon = vanishX + 25f

    val groundPath = Path().apply {
      moveTo(alleyLeftAtHorizon, horizonY)
      lineTo(alleyRightAtHorizon, horizonY)
      lineTo(alleyRightAtBottom, canvasH)
      lineTo(alleyLeftAtBottom, canvasH)
      close()
    }

    val groundColors = when (weather) {
      SanaaWeatherType.HIGHLAND_RAIN_GLEAM -> listOf(Color(0xFF1C2833), Color(0xFF0D1B2A))
      SanaaWeatherType.SWIRLING_DUST_STORM -> listOf(Color(0xFF4E342E), Color(0xFF3E2723))
      SanaaWeatherType.MIDDAY_SOLAR_HEAT -> listOf(Color(0xFF455A64), Color(0xFF263238))
      else -> listOf(Color(0xFF37474F), Color(0xFF212121))
    }

    drawPath(
      brush = Brush.verticalGradient(
        colors = groundColors,
        startY = horizonY,
        endY = canvasH
      ),
      path = groundPath
    )

    // Rain puddle reflections on alley floor
    if (weather == SanaaWeatherType.HIGHLAND_RAIN_GLEAM) {
      for (puddleIdx in 0..2) {
        val pY = horizonY + (canvasH - horizonY) * (0.45f + puddleIdx * 0.22f)
        val pW = (canvasW * 0.28f) * (0.6f + puddleIdx * 0.35f)
        drawOval(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF80DEEA).copy(alpha = 0.30f), Color.Transparent),
            center = Offset(vanishX + (puddleIdx - 1) * 60f, pY),
            radius = pW * 0.5f
          ),
          topLeft = Offset(vanishX + (puddleIdx - 1) * 60f - pW * 0.5f, pY - 12f),
          size = Size(pW, 24f)
        )
      }
    }

    // Cobblestone Alley perspective grid lines
    for (i in 1..4) {
      val t = i / 5f
      val startX = alleyLeftAtHorizon + (alleyRightAtHorizon - alleyLeftAtHorizon) * t
      val endX = alleyLeftAtBottom + (alleyRightAtBottom - alleyLeftAtBottom) * t
      drawLine(
        color = if (weather == SanaaWeatherType.HIGHLAND_RAIN_GLEAM) Color(0xFF80DEEA).copy(alpha = 0.4f) else Color(0xFF546E7A).copy(alpha = 0.5f),
        start = Offset(startX, horizonY),
        end = Offset(endX, canvasH),
        strokeWidth = 2f
      )
    }

    // 4.5. 7D Sana'a Alley Hiding Spots (الأبواب الخشبية العتيقة والسلالم وسقائف صنعاء)
    for (spot in hidingSpots) {
      val z = spot.worldZ.coerceAtLeast(1f)
      val scale = (200f / (z + 40f)).coerceIn(0.08f, 2.2f)
      val roadW = (alleyRightAtBottom - alleyLeftAtBottom) * scale
      val screenX = vanishX + (spot.side * roadW * 0.52f)
      val screenY = horizonY + (canvasH - horizonY) * scale

      if (screenY in horizonY..canvasH && scale > 0.12f) {
        val spotW = 52f * scale
        val spotH = 85f * scale

        // Glow indicator around hiding spot
        drawCircle(
          color = GangNeonGreen.copy(alpha = 0.25f),
          center = Offset(screenX, screenY - spotH * 0.5f),
          radius = 45f * scale
        )

        when (spot.type) {
          SanaaHidingSpotType.ANTIQUE_WOODEN_DOOR -> {
            // Antique Sana'ani Carved Wooden Door
            val doorLeft = if (spot.side < 0) screenX - spotW else screenX
            val doorTop = screenY - spotH

            // Door Frame (Stone Arched Lintel)
            drawRoundRect(
              color = Color(0xFF5D4037),
              topLeft = Offset(doorLeft - 4f * scale, doorTop - 10f * scale),
              size = Size(spotW + 8f * scale, spotH + 12f * scale),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f * scale, 14f * scale)
            )

            // Carved Mahogany Wood Door Panel
            drawRoundRect(
              color = Color(0xFF3E2723),
              topLeft = Offset(doorLeft, doorTop),
              size = Size(spotW, spotH),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale, 8f * scale)
            )

            // Iron Rivets / Studs (مسامير الباب القديمة)
            for (rx in 1..3) {
              for (ry in 1..5) {
                drawCircle(
                  color = SanaaGold,
                  center = Offset(doorLeft + rx * (spotW / 4f), doorTop + ry * (spotH / 6f)),
                  radius = 2.5f * scale
                )
              }
            }

            // Heavy Iron Knocker Ring (مطرقة الباب الفولاذية)
            drawCircle(
              color = Color(0xFFFFD54F),
              center = Offset(doorLeft + (spotW * 0.5f), doorTop + (spotH * 0.45f)),
              radius = 5f * scale,
              style = Stroke(width = 2.5f * scale)
            )
          }
          SanaaHidingSpotType.UNDER_STONE_STAIRS -> {
            // Stone Stairs leading upwards with dark alcove
            val stairLeft = if (spot.side < 0) screenX - spotW else screenX
            val stairTop = screenY - spotH

            // Dark shadowy hiding alcove under stairs
            val alcovePath = Path().apply {
              moveTo(stairLeft, screenY)
              lineTo(stairLeft + spotW, screenY)
              lineTo(if (spot.side < 0) stairLeft else stairLeft + spotW, stairTop + (spotH * 0.3f))
              close()
            }
            drawPath(alcovePath, color = Color(0xFF1B1B1B).copy(alpha = 0.92f))

            // Stepped stone staircases
            for (step in 0..4) {
              val stepY = screenY - (step * (spotH / 5f))
              val stepW = spotW * (1f - (step * 0.15f))
              drawRect(
                color = Color(0xFF78909C),
                topLeft = Offset(if (spot.side < 0) stairLeft else stairLeft + (spotW - stepW), stepY - (10f * scale)),
                size = Size(stepW, 10f * scale)
              )
            }
          }
          SanaaHidingSpotType.SPICE_POTTERY_VAULT -> {
            // Cluster of Sana'ani Clay Water/Spice Jars
            val jarLeft = if (spot.side < 0) screenX - spotW else screenX
            for (j in 0..2) {
              val jX = jarLeft + (j * 16f * scale)
              val jY = screenY - (35f * scale)
              drawOval(
                color = Color(0xFFD84315),
                topLeft = Offset(jX, jY),
                size = Size(20f * scale, 35f * scale)
              )
              drawRect(
                color = Color(0xFFBF360C),
                topLeft = Offset(jX + 4f * scale, jY - 6f * scale),
                size = Size(12f * scale, 8f * scale)
              )
            }
          }
          SanaaHidingSpotType.VAULTED_SAQIFAH_ARCH -> {
            // Saqifah Stone Archway
            val archLeft = if (spot.side < 0) screenX - spotW else screenX
            val archTop = screenY - spotH
            drawRoundRect(
              color = Color(0xFF455A64),
              topLeft = Offset(archLeft, archTop),
              size = Size(spotW, spotH),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f * scale, 20f * scale)
            )
            // Hanging traditional lantern
            drawCircle(
              color = Color(0xFFFFD54F).copy(alpha = 0.85f),
              center = Offset(archLeft + spotW * 0.5f, archTop + 25f * scale),
              radius = 8f * scale
            )
          }
        }

        // Title and Stealth Indicator Badge
        safeDrawText(
          textMeasurer = textMeasurer,
          text = "${spot.type.iconEmoji} ${spot.type.titleAr}",
          topLeft = Offset(screenX - 35f * scale, screenY - spotH - 18f * scale),
          style = TextStyle(
            color = GangNeonGreen,
            fontSize = (10f * scale).coerceAtLeast(8f).sp,
            fontWeight = FontWeight.Bold
          ),
          allocatedWidth = 140f * scale,
          allocatedHeight = 40f * scale
        )
      }
    }

    // 5. 7D Obstacles (Spice carts, Mountain Goats, Barrels, Clotheslines, Barricades)
    for (obs in obstacles) {
      val z = obs.worldZ.coerceAtLeast(1f)
      val scale = (200f / (z + 40f)).coerceIn(0.12f, 2.5f)
      val roadW = (alleyRightAtBottom - alleyLeftAtBottom) * scale
      val screenX = vanishX + (obs.worldX * roadW * 0.5f)
      val screenY = horizonY + (canvasH - horizonY) * scale

      if (screenY in horizonY..canvasH && scale > 0.12f) {
        val obsW = 55f * scale
        val obsH = 48f * scale

        when {
          obs.typeName.contains("ماعز") -> {
            // 3D Yemeni Mountain Goat (ماعز صنعاني جبلي - الصورة 3)
            // Body
            drawRoundRect(
              color = Color(0xFF8D6E63),
              topLeft = Offset(screenX - obsW * 0.45f, screenY - obsH * 0.7f),
              size = Size(obsW * 0.9f, obsH * 0.48f),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
            )
            // White fur patch
            drawOval(
              color = Color(0xFFECEFF1),
              topLeft = Offset(screenX - obsW * 0.2f, screenY - obsH * 0.65f),
              size = Size(obsW * 0.45f, obsH * 0.35f)
            )
            // Head & Curved Horns
            drawCircle(
              color = Color(0xFF6D4C41),
              center = Offset(screenX - obsW * 0.4f, screenY - obsH * 0.8f),
              radius = 12f * scale
            )
            // Horns
            drawLine(
              color = Color(0xFFD7CCC8),
              start = Offset(screenX - obsW * 0.4f, screenY - obsH * 0.85f),
              end = Offset(screenX - obsW * 0.55f, screenY - obsH * 1.15f),
              strokeWidth = 3.5f * scale
            )
            drawLine(
              color = Color(0xFFD7CCC8),
              start = Offset(screenX - obsW * 0.35f, screenY - obsH * 0.85f),
              end = Offset(screenX - obsW * 0.3f, screenY - obsH * 1.18f),
              strokeWidth = 3.5f * scale
            )
            // Trotting legs
            drawLine(color = Color(0xFF4E342E), start = Offset(screenX - obsW * 0.35f, screenY - obsH * 0.25f), end = Offset(screenX - obsW * 0.35f, screenY), strokeWidth = 3.2f * scale)
            drawLine(color = Color(0xFF4E342E), start = Offset(screenX + obsW * 0.25f, screenY - obsH * 0.25f), end = Offset(screenX + obsW * 0.25f, screenY), strokeWidth = 3.2f * scale)
          }
          obs.typeName.contains("بهارات") || obs.typeName.contains("توابل") -> {
            // 3D Spice Market Burlap Sacks & Wooden Cart (سوق الملح والتوابل - الصور 1 و2 و3)
            // Wooden Cart Frame
            drawRect(
              color = Color(0xFF5D4037),
              topLeft = Offset(screenX - obsW * 0.5f, screenY - obsH * 0.4f),
              size = Size(obsW, obsH * 0.35f)
            )
            // Burlap Sacks: Golden Turmeric (Yellow), Red Sumac (Deep Red), Zaatar (Green)
            drawCircle(color = Color(0xFFFFB300), center = Offset(screenX - obsW * 0.25f, screenY - obsH * 0.55f), radius = 14f * scale)
            drawCircle(color = Color(0xFFC62828), center = Offset(screenX, screenY - obsH * 0.65f), radius = 15f * scale)
            drawCircle(color = Color(0xFF2E7D32), center = Offset(screenX + obsW * 0.25f, screenY - obsH * 0.55f), radius = 14f * scale)
            // Spoke Wheels
            drawCircle(color = Color(0xFF212121), center = Offset(screenX - obsW * 0.35f, screenY - 5f * scale), radius = 8f * scale)
            drawCircle(color = Color(0xFF212121), center = Offset(screenX + obsW * 0.35f, screenY - 5f * scale), radius = 8f * scale)
          }
          obs.typeName.contains("فخار") || obs.typeName.contains("ماء") -> {
            // Terracotta Zir / Water Amphora
            drawRoundRect(
              color = Color(0xFFD84315),
              topLeft = Offset(screenX - obsW * 0.35f, screenY - obsH * 0.8f),
              size = Size(obsW * 0.7f, obsH * 0.8f),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * scale, 10f * scale)
            )
            drawRect(
              color = Color(0xFFBF360C),
              topLeft = Offset(screenX - obsW * 0.18f, screenY - obsH * 0.95f),
              size = Size(obsW * 0.36f, obsH * 0.18f)
            )
          }
          else -> {
            if (obs.isHigh) {
              // High Clothesline
              drawLine(
                color = Color(0xFFB0BEC5),
                start = Offset(screenX - obsW * 0.8f, screenY - obsH * 1.3f),
                end = Offset(screenX + obsW * 0.8f, screenY - obsH * 1.3f),
                strokeWidth = 3f * scale
              )
              drawRect(color = Color(0xFFE53935), topLeft = Offset(screenX - obsW * 0.4f, screenY - obsH * 1.25f), size = Size(obsW * 0.32f, obsH * 0.55f))
              drawRect(color = Color(0xFF1E88E5), topLeft = Offset(screenX + obsW * 0.1f, screenY - obsH * 1.25f), size = Size(obsW * 0.32f, obsH * 0.55f))
            } else {
              // Police Checkpoint Barricade
              drawRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(screenX - obsW * 0.6f, screenY - obsH * 0.55f),
                size = Size(obsW * 1.2f, obsH * 0.45f)
              )
              drawRect(
                color = Color.White,
                topLeft = Offset(screenX - obsW * 0.2f, screenY - obsH * 0.55f),
                size = Size(obsW * 0.4f, obsH * 0.45f)
              )
            }
          }
        }

        safeDrawText(
          textMeasurer = textMeasurer,
          text = obs.nameAr,
          topLeft = Offset(screenX - (45f * scale), if (obs.isHigh) screenY - (obsH * 1.5f) else screenY - (obsH * 1.15f)),
          style = TextStyle(color = TaxiYellow, fontSize = (10f * scale).coerceAtLeast(7.5f).sp, fontWeight = FontWeight.Bold),
          allocatedWidth = 140f * scale,
          allocatedHeight = 40f * scale
        )
      }
    }

    // 6. 7D Police Pursuers & Cruisers (دوريات شرطة نجدة صنعاء - الصور 1 و2)
    for (cop in policeList) {
      val z = cop.worldZ.coerceAtLeast(1f)
      val scale = (200f / (z + 40f)).coerceIn(0.12f, 2.6f)
      val roadW = (alleyRightAtBottom - alleyLeftAtBottom) * scale
      val screenX = vanishX + (cop.worldX * roadW * 0.5f)
      val screenY = horizonY + (canvasH - horizonY) * scale

      if (screenY in horizonY..canvasH && scale > 0.14f) {
        if (cop.isVehicle) {
          // Toyota Land Cruiser 70s Police Patrol Cruiser (Image 1 & 2)
          val carW = 95f * scale
          val carH = 58f * scale

          // Ground shadow
          drawOval(
            color = Color.Black.copy(alpha = 0.55f),
            topLeft = Offset(screenX - carW * 0.55f, screenY - 6f * scale),
            size = Size(carW * 1.1f, 15f * scale)
          )

          // White Cruiser Body
          drawRoundRect(
            color = Color(0xFFFAFAFA),
            topLeft = Offset(screenX - carW * 0.5f, screenY - carH),
            size = Size(carW, carH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale, 8f * scale)
          )

          // Dark Navy Police Stripe across side
          drawRect(
            color = Color(0xFF0D47A1),
            topLeft = Offset(screenX - carW * 0.5f, screenY - carH * 0.58f),
            size = Size(carW, carH * 0.28f)
          )

          // Front Steel Bull-Bar Bumper
          drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(screenX - carW * 0.45f, screenY - carH * 0.32f),
            size = Size(carW * 0.9f, 7f * scale)
          )

          // Dual Headlights
          drawCircle(color = Color(0xFFFFF9C4), center = Offset(screenX - carW * 0.35f, screenY - carH * 0.26f), radius = 5.5f * scale)
          drawCircle(color = Color(0xFFFFF9C4), center = Offset(screenX + carW * 0.35f, screenY - carH * 0.26f), radius = 5.5f * scale)

          // Roof Emergency Siren Lightbar: GREEN ON LEFT, RED ON RIGHT! (Authentic Yemeni Police)
          val isBlinkLeft = (System.currentTimeMillis() % 350) < 175
          drawRect(
            color = if (isBlinkLeft) Color(0xFF00E676) else Color(0xFF1B5E20),
            topLeft = Offset(screenX - carW * 0.35f, screenY - carH - 10f * scale),
            size = Size(carW * 0.32f, 9f * scale)
          )
          drawRect(
            color = if (!isBlinkLeft) Color(0xFFFF1744) else Color(0xFFB71C1C),
            topLeft = Offset(screenX + carW * 0.03f, screenY - carH - 10f * scale),
            size = Size(carW * 0.32f, 9f * scale)
          )

          // Siren Radiant Halo on cobblestones
          drawCircle(
            color = (if (isBlinkLeft) Color(0xFF00E676) else Color(0xFFFF1744)).copy(alpha = 0.40f),
            center = Offset(screenX, screenY - carH - 5f * scale),
            radius = 36f * scale
          )

          safeDrawText(
            textMeasurer = textMeasurer,
            text = "الشرطة POLICE 🚓",
            topLeft = Offset(screenX - carW * 0.45f, screenY - carH * 0.55f),
            style = TextStyle(color = Color.White, fontSize = (8.5f * scale).coerceAtLeast(6.5f).sp, fontWeight = FontWeight.Black),
            allocatedWidth = carW * 0.9f,
            allocatedHeight = carH * 0.3f
          )
        } else {
          // Foot Police Officer (Image 1 & 2)
          val copW = 38f * scale
          val copH = 72f * scale

          // Trousers
          drawRect(
            color = Color(0xFF0D47A1),
            topLeft = Offset(screenX - copW * 0.45f, screenY - copH * 0.45f),
            size = Size(copW * 0.9f, copH * 0.45f)
          )

          // Sky-Blue Police Uniform Shirt
          drawRoundRect(
            color = Color(0xFF81D4FA),
            topLeft = Offset(screenX - copW * 0.5f, screenY - copH * 0.85f),
            size = Size(copW, copH * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
          )

          // Navy Necktie & Gold Shoulder Epaulets
          drawRect(color = Color(0xFF0D47A1), topLeft = Offset(screenX - 2.5f * scale, screenY - copH * 0.82f), size = Size(5f * scale, copH * 0.22f))
          drawCircle(color = Color(0xFFFFD54F), center = Offset(screenX - copW * 0.42f, screenY - copH * 0.82f), radius = 3.5f * scale)
          drawCircle(color = Color(0xFFFFD54F), center = Offset(screenX + copW * 0.42f, screenY - copH * 0.82f), radius = 3.5f * scale)

          // Head & Tanned Skin
          drawCircle(color = Color(0xFFF0BD88), center = Offset(screenX, screenY - copH * 0.95f), radius = 10f * scale)

          // Peaked Navy Service Cap with Gold Crest
          drawRoundRect(
            color = Color(0xFF0D47A1),
            topLeft = Offset(screenX - copW * 0.45f, screenY - copH * 1.14f),
            size = Size(copW * 0.9f, 10f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale, 3f * scale)
          )
          drawCircle(color = Color(0xFFFFD54F), center = Offset(screenX, screenY - copH * 1.08f), radius = 3f * scale)

          // Classic Mustache
          drawRect(color = Color(0xFF212121), topLeft = Offset(screenX - 4.5f * scale, screenY - copH * 0.92f), size = Size(9f * scale, 2.8f * scale))

          safeDrawText(
            textMeasurer = textMeasurer,
            text = if (cop.isStunned) "👮‍♂️ فاقد الأثر ❓" else "👮‍♂️ ضابط شرطة",
            topLeft = Offset(screenX - 35f * scale, screenY - copH - 22f * scale),
            style = TextStyle(color = if (cop.isStunned) Color.Yellow else Color.White, fontSize = (9.5f * scale).coerceAtLeast(7.5f).sp, fontWeight = FontWeight.Bold),
            allocatedWidth = 140f * scale,
            allocatedHeight = 40f * scale
          )
        }
      }
    }

    // 6.5. 7D Gang Thugs & Loot Carriers (عصابات شوارع صنعاء - الصورة 3)
    for (thug in gangList) {
      val z = thug.worldZ.coerceAtLeast(1f)
      val scale = (200f / (z + 40f)).coerceIn(0.12f, 2.6f)
      val roadW = (alleyRightAtBottom - alleyLeftAtBottom) * scale
      val screenX = vanishX + (thug.worldX * roadW * 0.5f)
      val screenY = horizonY + (canvasH - horizonY) * scale

      if (screenY in horizonY..canvasH && scale > 0.12f) {
        val thugW = 44f * scale
        val thugH = 75f * scale

        if (thug.isBoundInRopes) {
          // Bound & Looted Gang Member in Ropes (الصورة 3 البانل 3)
          drawOval(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(screenX - thugW * 0.6f, screenY - 8f * scale),
            size = Size(thugW * 1.2f, 14f * scale)
          )

          drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(screenX - thugW * 0.5f, screenY - thugH * 0.75f),
            size = Size(thugW, thugH * 0.75f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
          )

          // Hands raised up in surrender
          drawLine(color = Color(0xFFE0A96D), start = Offset(screenX - thugW * 0.45f, screenY - thugH * 0.7f), end = Offset(screenX - thugW * 0.65f, screenY - thugH * 0.95f), strokeWidth = 4f * scale)
          drawLine(color = Color(0xFFE0A96D), start = Offset(screenX + thugW * 0.45f, screenY - thugH * 0.7f), end = Offset(screenX + thugW * 0.65f, screenY - thugH * 0.95f), strokeWidth = 4f * scale)

          // Thick Yellow Manila Ropes wrapped securely
          for (rIdx in 1..5) {
            drawLine(
              color = Color(0xFFFFD54F),
              start = Offset(screenX - thugW * 0.5f, screenY - thugH * (0.12f + rIdx * 0.11f)),
              end = Offset(screenX + thugW * 0.5f, screenY - thugH * (0.12f + rIdx * 0.11f)),
              strokeWidth = 3.5f * scale
            )
          }

          safeDrawText(
            textMeasurer = textMeasurer,
            text = "🪢 مقبوض عليه بالحبال!\n(جاهز للتسليم للشرطة 👮‍♂️)",
            topLeft = Offset(screenX - 55f * scale, screenY - thugH - 28f * scale),
            style = TextStyle(color = GangNeonGreen, fontSize = (9.5f * scale).coerceAtLeast(8f).sp, fontWeight = FontWeight.Bold),
            allocatedWidth = 160f * scale,
            allocatedHeight = 50f * scale
          )
        } else {
          // Active Running Robber from Image 3
          drawOval(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = Offset(screenX - thugW * 0.6f, screenY - 6f * scale),
            size = Size(thugW * 1.2f, 13f * scale)
          )

          // Dark Charcoal / Brown Zip Jacket
          drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(screenX - thugW * 0.5f, screenY - thugH * 0.8f),
            size = Size(thugW, thugH * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
          )

          // Blue Denim Jeans
          drawRect(
            color = Color(0xFF1565C0),
            topLeft = Offset(screenX - thugW * 0.42f, screenY - thugH * 0.35f),
            size = Size(thugW * 0.84f, thugH * 0.35f)
          )

          // Head with Panicked Expression from Image 3
          drawCircle(color = Color(0xFFE0A96D), center = Offset(screenX, screenY - thugH * 0.92f), radius = 10f * scale)

          // Black Knit Beanie / Skull Cap
          drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(screenX - thugW * 0.35f, screenY - thugH * 1.15f),
            size = Size(thugW * 0.7f, 11f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
          )

          // Wide panicked eyes looking back in terror
          drawCircle(color = Color.White, center = Offset(screenX - 4f * scale, screenY - thugH * 0.95f), radius = 2.8f * scale)
          drawCircle(color = Color.Black, center = Offset(screenX - 4f * scale, screenY - thugH * 0.95f), radius = 1.3f * scale)
          drawCircle(color = Color.White, center = Offset(screenX + 4f * scale, screenY - thugH * 0.95f), radius = 2.8f * scale)
          drawCircle(color = Color.Black, center = Offset(screenX + 4f * scale, screenY - thugH * 0.95f), radius = 1.3f * scale)

          // Open screaming mouth
          drawOval(color = Color(0xFF3E2723), topLeft = Offset(screenX - 3.5f * scale, screenY - thugH * 0.88f), size = Size(7f * scale, 5.5f * scale))

          // Stolen Gold Loot Burlap Sack
          drawCircle(
            color = Color(0xFFFFD600).copy(alpha = 0.95f),
            center = Offset(screenX + thugW * 0.45f, screenY - thugH * 0.45f),
            radius = 12f * scale
          )

          safeDrawText(
            textMeasurer = textMeasurer,
            text = "🦹‍♂️ ${thug.nameAr}\n[${thug.lootName}]",
            topLeft = Offset(screenX - 45f * scale, screenY - thugH - 32f * scale),
            style = TextStyle(color = Color(0xFFFFD54F), fontSize = (9.5f * scale).coerceAtLeast(7.5f).sp, fontWeight = FontWeight.Bold),
            allocatedWidth = 140f * scale,
            allocatedHeight = 50f * scale
          )
        }
      }
    }

    // 7. 7D Projectiles (Toy Rifle BBs & Fireworks)
    for (proj in projectiles) {
      val z = proj.worldZ.coerceAtLeast(1f)
      val scale = (200f / (z + 40f)).coerceIn(0.1f, 2.0f)
      val roadW = (alleyRightAtBottom - alleyLeftAtBottom) * scale
      val screenX = vanishX + (proj.worldX * roadW * 0.5f)
      val screenY = horizonY + (canvasH - horizonY) * scale - (proj.worldY * 60f * scale)

      drawCircle(
        color = if (proj.isFirework) Color(0xFFFFD600) else Color(0xFF00E5FF),
        center = Offset(screenX, screenY),
        radius = (if (proj.isFirework) 8f else 4f) * scale
      )
    }

    // 8. 7D Dynamic Weather Particles & Effects (غبار متطاير، قطرات مطر، بريق شمس)
    for (pt in particles) {
      val pScreenX = vanishX + (pt.x * canvasW * 0.38f)
      val pScreenY = (canvasH * 0.75f) - (pt.y * 110f)

      when (pt.kind) {
        ParticleKind.RAIN_DROP -> {
          drawLine(
            color = pt.color.copy(alpha = pt.alpha),
            start = Offset(pScreenX, pScreenY),
            end = Offset(pScreenX - 4f, pScreenY + 16f),
            strokeWidth = pt.size
          )
        }
        ParticleKind.SUN_BEAM_GLOW -> {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(pt.color.copy(alpha = pt.alpha), Color.Transparent),
              center = Offset(pScreenX, pScreenY),
              radius = pt.size * 2f
            ),
            center = Offset(pScreenX, pScreenY),
            radius = pt.size * 2f
          )
        }
        ParticleKind.GROUND_SAND_SWIRL -> {
          drawOval(
            color = pt.color.copy(alpha = pt.alpha * 0.8f),
            topLeft = Offset(pScreenX - pt.size * 2f, pScreenY - pt.size),
            size = Size(pt.size * 4f, pt.size * 2f)
          )
        }
        else -> {
          drawCircle(
            color = pt.color.copy(alpha = pt.alpha),
            center = Offset(pScreenX, pScreenY),
            radius = pt.size
          )
        }
      }
    }

    // 8.5. Stealth Vignette Darkening (عندما يختبئ اللاعب خلف الباب أو الدرج)
    if (isPlayerHiding) {
      drawRect(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.Transparent,
            Color(0x99000000),
            Color(0xDD002200)
          ),
          center = Offset(canvasW * 0.5f, canvasH * 0.6f),
          radius = canvasW * 0.65f
        ),
        size = Size(canvasW, canvasH)
      )
    }

    // 9. Main Hero: Mazen (زعيم المشاغبين - مستنسخ بدقة 3D ضخمة من الصور المرفقة) or Hijacked Yellow Toyota Truck
    val playerScreenX = vanishX + (playerX * (canvasW * 0.38f))
    val playerScreenY = (canvasH * 0.82f) - (playerY * 95f)

    if (isVehicle) {
      // 3D Classic Yellow Toyota Hilux / Pickup Truck (شاص تويوتا هايلوكس أصفر كلاسيكي - الصورة 1 "تهريب السيارات في صنعاء")
      val carW = 230.dp.toPx()
      val carH = 135.dp.toPx()

      // Deep ground shadow under the truck
      drawOval(
        color = Color.Black.copy(alpha = 0.65f),
        topLeft = Offset(playerScreenX - (carW * 0.52f), canvasH * 0.85f),
        size = Size(carW * 1.04f, 32.dp.toPx())
      )

      // Spinning rim dust clouds bursting behind wheels
      drawOval(
        color = Color(0xFFBCAAA4).copy(alpha = 0.60f),
        topLeft = Offset(playerScreenX - (carW * 0.55f), canvasH * 0.83f),
        size = Size(45.dp.toPx(), 24.dp.toPx())
      )
      drawOval(
        color = Color(0xFFBCAAA4).copy(alpha = 0.60f),
        topLeft = Offset(playerScreenX + (carW * 0.32f), canvasH * 0.83f),
        size = Size(45.dp.toPx(), 24.dp.toPx())
      )

      // 1. Yellow Hilux Truck Cab & Body (Image 1)
      drawRoundRect(
        color = Color(0xFFFBC02D), // Vibrant Mustard/Taxi Yellow
        topLeft = Offset(playerScreenX - (carW * 0.45f), playerScreenY - carH),
        size = Size(carW * 0.90f, carH * 0.85f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
      )

      // 2. Front Chrome Grille & "TOYOTA" Badge
      drawRoundRect(
        color = Color(0xFF263238),
        topLeft = Offset(playerScreenX - (carW * 0.32f), playerScreenY - (carH * 0.42f)),
        size = Size(carW * 0.64f, carH * 0.28f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
      )
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "TOYOTA",
        topLeft = Offset(playerScreenX - 25.dp.toPx(), playerScreenY - (carH * 0.35f)),
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
        allocatedWidth = 60.dp.toPx(),
        allocatedHeight = 25.dp.toPx()
      )

      // 3. Dual Round Chrome Headlights with Halogen Beam & Amber Blinkers (Image 1)
      drawCircle(color = Color(0xFFFFF9C4), center = Offset(playerScreenX - (carW * 0.36f), playerScreenY - (carH * 0.28f)), radius = 12.dp.toPx())
      drawCircle(color = Color(0xFFFF9800), center = Offset(playerScreenX - (carW * 0.42f), playerScreenY - (carH * 0.28f)), radius = 6.dp.toPx())
      drawCircle(color = Color(0xFFFFF9C4), center = Offset(playerScreenX + (carW * 0.36f), playerScreenY - (carH * 0.28f)), radius = 12.dp.toPx())
      drawCircle(color = Color(0xFFFF9800), center = Offset(playerScreenX + (carW * 0.42f), playerScreenY - (carH * 0.28f)), radius = 6.dp.toPx())

      // 4. Polished Steel Front Bumper & Yemeni License Plate: "صنعاء 5"
      drawRoundRect(
        color = Color(0xFFECEFF1),
        topLeft = Offset(playerScreenX - (carW * 0.46f), playerScreenY - (carH * 0.16f)),
        size = Size(carW * 0.92f, carH * 0.16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
      )
      drawRect(
        color = Color(0xFFFFFFFF),
        topLeft = Offset(playerScreenX - 22.dp.toPx(), playerScreenY - (carH * 0.14f)),
        size = Size(44.dp.toPx(), 14.dp.toPx())
      )
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "صنعاء 5",
        topLeft = Offset(playerScreenX - 18.dp.toPx(), playerScreenY - (carH * 0.15f)),
        style = TextStyle(color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold),
        allocatedWidth = 40.dp.toPx(),
        allocatedHeight = 16.dp.toPx()
      )

      // 5. Curved Windshield with Sky Reflection
      drawRoundRect(
        color = Color(0xFF4FC3F7).copy(alpha = 0.85f),
        topLeft = Offset(playerScreenX - (carW * 0.38f), playerScreenY - (carH * 0.92f)),
        size = Size(carW * 0.76f, carH * 0.42f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
      )

      // 6. Mazen Leaning Out Driver Window Smiling & Steering (Image 1 replica!)
      // Head
      drawCircle(
        color = Color(0xFFF0BD88),
        center = Offset(playerScreenX - (carW * 0.28f), playerScreenY - (carH * 0.72f)),
        radius = 14.dp.toPx()
      )
      // Styled Dark Hair
      drawRoundRect(
        color = Color(0xFF1A1A1A),
        topLeft = Offset(playerScreenX - (carW * 0.33f), playerScreenY - (carH * 0.82f)),
        size = Size(20.dp.toPx(), 12.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
      )
      // Arm leaning out on door sill
      drawLine(
        color = Color(0xFFF0BD88),
        start = Offset(playerScreenX - (carW * 0.28f), playerScreenY - (carH * 0.60f)),
        end = Offset(playerScreenX - (carW * 0.45f), playerScreenY - (carH * 0.50f)),
        strokeWidth = 6.dp.toPx()
      )

      // 7. Heavy-duty All-Terrain Wheels
      drawCircle(color = Color(0xFF212121), center = Offset(playerScreenX - (carW * 0.38f), playerScreenY - (carH * 0.02f)), radius = 18.dp.toPx())
      drawCircle(color = Color(0xFF78909C), center = Offset(playerScreenX - (carW * 0.38f), playerScreenY - (carH * 0.02f)), radius = 9.dp.toPx())
      drawCircle(color = Color(0xFF212121), center = Offset(playerScreenX + (carW * 0.38f), playerScreenY - (carH * 0.02f)), radius = 18.dp.toPx())
      drawCircle(color = Color(0xFF78909C), center = Offset(playerScreenX + (carW * 0.38f), playerScreenY - (carH * 0.02f)), radius = 9.dp.toPx())

      safeDrawText(
        textMeasurer = textMeasurer,
        text = "🛻 هايلوكس تويوتا 1985 (تهريب صنعاء 7D)",
        topLeft = Offset(playerScreenX - 70.dp.toPx(), playerScreenY - carH - 24.dp.toPx()),
        style = TextStyle(color = SanaaGold, fontSize = 11.5.sp, fontWeight = FontWeight.Black),
        allocatedWidth = 240.dp.toPx(),
        allocatedHeight = 30.dp.toPx()
      )
    } else {
      // 3D Hero Mazen (زعيم المشاغبين - مازن: مستنسخ بدقة كاملة بحجم 3D ضخم من الصور 1 و3 و4)
      val heroW = 95.dp.toPx()
      val heroH = if (isPlayerHiding) 90.dp.toPx() else 145.dp.toPx()

      // Ground Shadow
      drawOval(
        color = Color.Black.copy(alpha = if (isPlayerHiding) 0.85f else 0.55f),
        topLeft = Offset(playerScreenX - 40.dp.toPx(), canvasH * 0.85f),
        size = Size(80.dp.toPx(), 22.dp.toPx())
      )

      // 1. Dynamic 3D Running Athletic Legs & Sneakers
      val legCycle = (distance * 18f).toInt() % 360
      val leftLegAngle = kotlin.math.sin(Math.toRadians(legCycle.toDouble())).toFloat() * 18.dp.toPx()
      val rightLegAngle = -leftLegAngle

      // Left Leg (Navy blue track pants with white side stripe)
      drawLine(
        color = Color(0xFF0D47A1),
        start = Offset(playerScreenX - 12.dp.toPx(), playerScreenY - (heroH * 0.38f)),
        end = Offset(playerScreenX - 12.dp.toPx() + leftLegAngle, playerScreenY - (heroH * 0.05f)),
        strokeWidth = 10.dp.toPx()
      )
      // Left Sneaker with white rubber sole & red accent
      drawRoundRect(
        color = Color(0xFF212121),
        topLeft = Offset(playerScreenX - 18.dp.toPx() + leftLegAngle, playerScreenY - (heroH * 0.06f)),
        size = Size(18.dp.toPx(), 9.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
      )
      drawRect(
        color = Color.White,
        topLeft = Offset(playerScreenX - 18.dp.toPx() + leftLegAngle, playerScreenY - (heroH * 0.02f)),
        size = Size(18.dp.toPx(), 3.dp.toPx())
      )

      // Right Leg
      drawLine(
        color = Color(0xFF1565C0),
        start = Offset(playerScreenX + 12.dp.toPx(), playerScreenY - (heroH * 0.38f)),
        end = Offset(playerScreenX + 12.dp.toPx() + rightLegAngle, playerScreenY - (heroH * 0.05f)),
        strokeWidth = 10.dp.toPx()
      )
      // Right Sneaker
      drawRoundRect(
        color = Color(0xFF212121),
        topLeft = Offset(playerScreenX + 6.dp.toPx() + rightLegAngle, playerScreenY - (heroH * 0.06f)),
        size = Size(18.dp.toPx(), 9.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
      )
      drawRect(
        color = Color.White,
        topLeft = Offset(playerScreenX + 6.dp.toPx() + rightLegAngle, playerScreenY - (heroH * 0.02f)),
        size = Size(18.dp.toPx(), 3.dp.toPx())
      )

      // 2. Torso - Mazen's Signature T-Shirt (مستنسخ تماماً من الصور 1 و3 و4)
      // Sky Blue lower body & short sleeves
      drawRoundRect(
        color = Color(0xFF42A5F5), // Vibrant Sky Blue
        topLeft = Offset(playerScreenX - (heroW * 0.35f), playerScreenY - (heroH * 0.72f)),
        size = Size(heroW * 0.70f, heroH * 0.36f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
      )

      // Crisp White horizontal chest bar across shoulders
      drawRect(
        color = Color(0xFFFFFFFF),
        topLeft = Offset(playerScreenX - (heroW * 0.35f), playerScreenY - (heroH * 0.74f)),
        size = Size(heroW * 0.70f, heroH * 0.12f)
      )

      // Red Shield Chest Emblem: "SANAA 7D"
      drawCircle(
        color = Color(0xFFE53935),
        center = Offset(playerScreenX, playerScreenY - (heroH * 0.68f)),
        radius = 7.dp.toPx()
      )

      // Deep Navy Blue upper chest, shoulders & collar
      drawRoundRect(
        color = Color(0xFF0D1B2A), // Deep Navy
        topLeft = Offset(playerScreenX - (heroW * 0.35f), playerScreenY - (heroH * 0.85f)),
        size = Size(heroW * 0.70f, heroH * 0.14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
      )

      // 3. Traditional Yemeni Embroidered Leather Sash & Golden Jambiya Dagger
      // Green & Gold Embroidered Sash
      drawRect(
        color = Color(0xFF2E7D32),
        topLeft = Offset(playerScreenX - (heroW * 0.36f), playerScreenY - (heroH * 0.44f)),
        size = Size(heroW * 0.72f, 12.dp.toPx())
      )
      drawRect(
        color = SanaaGold,
        topLeft = Offset(playerScreenX - (heroW * 0.36f), playerScreenY - (heroH * 0.41f)),
        size = Size(heroW * 0.72f, 3.dp.toPx())
      )

      // Sculpted Golden Jambiya Curved Dagger (خنجر الجنبية الصنعانية المذهبة)
      drawLine(
        color = Color(0xFFFFD700),
        start = Offset(playerScreenX - 5.dp.toPx(), playerScreenY - (heroH * 0.44f)),
        end = Offset(playerScreenX + 16.dp.toPx(), playerScreenY - (heroH * 0.32f)),
        strokeWidth = 6.dp.toPx()
      )
      drawCircle(
        color = Color(0xFFFFB300),
        center = Offset(playerScreenX - 6.dp.toPx(), playerScreenY - (heroH * 0.45f)),
        radius = 5.dp.toPx()
      )

      // 4. Arms & Lasso / Slingshot
      // Left arm swinging with run
      drawLine(
        color = Color(0xFFF0BD88),
        start = Offset(playerScreenX - (heroW * 0.32f), playerScreenY - (heroH * 0.75f)),
        end = Offset(playerScreenX - (heroW * 0.45f) - leftLegAngle * 0.5f, playerScreenY - (heroH * 0.55f)),
        strokeWidth = 8.dp.toPx()
      )
      // Right arm holding golden lasso rope coil (Image 3 Panel 3)
      drawLine(
        color = Color(0xFFF0BD88),
        start = Offset(playerScreenX + (heroW * 0.32f), playerScreenY - (heroH * 0.75f)),
        end = Offset(playerScreenX + (heroW * 0.48f) - rightLegAngle * 0.5f, playerScreenY - (heroH * 0.58f)),
        strokeWidth = 8.dp.toPx()
      )
      // Coiled Yellow Lasso Rope
      drawCircle(
        color = Color(0xFFFFCA28),
        center = Offset(playerScreenX + (heroW * 0.50f), playerScreenY - (heroH * 0.58f)),
        radius = 10.dp.toPx(),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
      )

      // 5. Head & Facial Features - Cloned from Images 1, 3, and 4
      // Warm Tanned Yemeni Skin Complexion
      drawCircle(
        color = if (isPlayerHiding) Color(0xFFD7CCC8) else Color(0xFFF0BD88),
        center = Offset(playerScreenX, playerScreenY - (heroH * 0.95f)),
        radius = 20.dp.toPx()
      )

      // Styled Jet-Black Hair with Forward Swept Locks (Images 1, 3, 4)
      drawOval(
        color = Color(0xFF1A1A1A),
        topLeft = Offset(playerScreenX - 22.dp.toPx(), playerScreenY - (heroH * 1.08f)),
        size = Size(44.dp.toPx(), 22.dp.toPx())
      )
      // Hair locks on forehead
      drawCircle(color = Color(0xFF263238), center = Offset(playerScreenX - 8.dp.toPx(), playerScreenY - (heroH * 0.99f)), radius = 7.dp.toPx())
      drawCircle(color = Color(0xFF263238), center = Offset(playerScreenX + 8.dp.toPx(), playerScreenY - (heroH * 0.99f)), radius = 7.dp.toPx())

      // Expressive Dark Eyes with White Specular Highlights
      drawCircle(color = Color.White, center = Offset(playerScreenX - 7.dp.toPx(), playerScreenY - (heroH * 0.95f)), radius = 4.5.dp.toPx())
      drawCircle(color = Color(0xFF212121), center = Offset(playerScreenX - 7.dp.toPx(), playerScreenY - (heroH * 0.95f)), radius = 2.8.dp.toPx())
      drawCircle(color = Color.White, center = Offset(playerScreenX - 6.dp.toPx(), playerScreenY - (heroH * 0.96f)), radius = 1.dp.toPx())

      drawCircle(color = Color.White, center = Offset(playerScreenX + 7.dp.toPx(), playerScreenY - (heroH * 0.95f)), radius = 4.5.dp.toPx())
      drawCircle(color = Color(0xFF212121), center = Offset(playerScreenX + 7.dp.toPx(), playerScreenY - (heroH * 0.95f)), radius = 2.8.dp.toPx())
      drawCircle(color = Color.White, center = Offset(playerScreenX + 8.dp.toPx(), playerScreenY - (heroH * 0.96f)), radius = 1.dp.toPx())

      // Energetic Confident Smile with White Teeth
      drawOval(
        color = Color(0xFF3E2723),
        topLeft = Offset(playerScreenX - 6.dp.toPx(), playerScreenY - (heroH * 0.88f)),
        size = Size(12.dp.toPx(), 6.dp.toPx())
      )
      drawRect(
        color = Color.White,
        topLeft = Offset(playerScreenX - 4.dp.toPx(), playerScreenY - (heroH * 0.88f)),
        size = Size(8.dp.toPx(), 2.5.dp.toPx())
      )

      // 6. Action Tag
      safeDrawText(
        textMeasurer = textMeasurer,
        text = when {
          isPlayerHiding -> "🥷 الزعيم مازن (متخفٍ في أزقة صنعاء)"
          playerAction == SanaaPlayerActionState.ROOFTOP_JUMPING -> "🦘 قفز الأسطح الطينية!"
          playerAction == SanaaPlayerActionState.SLIDING -> "⛷️ انزلاق سريع في الزقاق!"
          else -> "🏃‍♂️ البطل مازن (زعيم المشاغبين 7D)"
        },
        topLeft = Offset(playerScreenX - 65.dp.toPx(), playerScreenY - heroH - 30.dp.toPx()),
        style = TextStyle(
          color = if (isPlayerHiding) GangNeonGreen else SanaaGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Black
        ),
        allocatedWidth = 240.dp.toPx(),
        allocatedHeight = 35.dp.toPx()
      )
    }
  }
}
