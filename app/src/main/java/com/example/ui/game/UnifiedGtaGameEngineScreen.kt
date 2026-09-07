package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AppDatabase
import com.example.data.local.HighScoreEntity
import com.example.model.*
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import com.example.sound.YemeniZawamilEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Unified GTA San Andreas Sana'a 10D Game Engine Screen (المحرك الشامل الموحد لعالم صنعاء 10D).
 * Features:
 * - 10D Realistic human & vehicle scaling for characters and cars
 * - Walkable and passable Sana'a streets, alleys, roundabouts, mosques, government buildings, souqs & stadiums
 * - Dynamic pedestrian simulation (citizens, elders, street vendors, women in Sitara)
 * - Commercial shops with Arabic storefronts and striped awnings
 * - Authentic Yemeni Zawamil radio engine with tribal drum beats, Tasa clanks, and lyrics
 * - Real-time Landmark & District Navigator (Bab Al-Yemen, Al-Saleh Mosque, Kentucky Roundabout, Al-Thawra Stadium, etc.)
 * - Room DB score persistence and instant restart
 */
@Composable
fun UnifiedGtaGameEngineScreen(
  initialHeroId: UnifiedHeroId = UnifiedHeroId.MAZEN,
  onNavigateBack: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val db = remember { AppDatabase.getDatabase(context) }
  val highScoreDao = remember { db.highScoreDao() }

  // Current Selected Hero (1 of 4 heroes, switchable anytime!)
  var currentHeroIndex by remember { mutableIntStateOf(UnifiedHeroId.values().indexOf(initialHeroId).coerceAtLeast(0)) }
  val currentHero = UnifiedHeroId.values()[currentHeroIndex]

  // Weapons
  var currentWeaponIndex by remember { mutableIntStateOf(0) }
  val currentWeapon = UNIFIED_WEAPONS[currentWeaponIndex]

  // 10D Real-Life Proportions Scaling Toggle (Default: TRUE as requested!)
  var is10DRealScale by remember { mutableStateOf(true) }

  // Current Sana'a District & Landmark
  var currentDistrict by remember { mutableStateOf(SanaaDistrict.AL_SALEH_MOSQUE) }
  var showDistrictSelectorDialog by remember { mutableStateOf(false) }
  var showZawamilDialog by remember { mutableStateOf(false) }

  // Road Curvature & Turn Branches (الانعطاف والتحرك التفاعلي بين الأحياء والأزقة)
  var roadCurvature by remember { mutableFloatStateOf(0f) }
  var activeTurnBranch by remember { mutableStateOf<SanaaTurnBranch?>(null) }
  var turnAlertMessage by remember { mutableStateOf<String?>(null) }
  var turnAlertTimer by remember { mutableFloatStateOf(0f) }

  val availableTurnBranches = remember {
    listOf(
      SanaaTurnBranch("b1", 60f, "شارع باب اليمن وسوق الملح", "أزقة صنعاء القديمة", SanaaDistrict.BAB_AL_YEMEN, -1),
      SanaaTurnBranch("b2", 130f, "تقاطع وجولة كنتاكي", "شارع الزبيري التجاري", SanaaDistrict.AL_ZUBAYRI_KENTUCKY, 1),
      SanaaTurnBranch("b3", 200f, "طريق مطار صنعاء الدولي", "المطار والمدرج وبرج المراقبة", SanaaDistrict.SANAA_AIRPORT, 1),
      SanaaTurnBranch("b4", 270f, "شارع المدارس الكبرى", "مدرسة جمال عبد الناصر والكويت", SanaaDistrict.SANAA_SCHOOLS, -1),
      SanaaTurnBranch("b5", 340f, "جسر عصر المعلق والتقاطعات", "الجسور العلوية العابرة", SanaaDistrict.SANAA_BRIDGES, 1),
      SanaaTurnBranch("b6", 410f, "المقرات الحكومية ومجمع الدفاع", "رئاسة الوزراء والوزارات السيادية", SanaaDistrict.DEFENCE_MINISTRY_ORDI, -1),
      SanaaTurnBranch("b7", 480f, "جولة المصباحي وشارع حدة", "المراكز التجارية والبوليفارد", SanaaDistrict.HADDAH_MESBAHI, 1),
      SanaaTurnBranch("b8", 550f, "سائلة صنعاء التراثية", "المجرى التاريخي المرصوف", SanaaDistrict.SANAA_SAILAH, -1)
    )
  }

  // Player position & movement physics
  var playerX by remember { mutableFloatStateOf(0f) }
  var playerZ by remember { mutableFloatStateOf(0f) }
  var playerAngleDeg by remember { mutableFloatStateOf(0f) }
  var isWalking by remember { mutableStateOf(false) }
  var isRunning by remember { mutableStateOf(false) }
  var jumpHeight by remember { mutableFloatStateOf(0f) }
  var isJumping by remember { mutableStateOf(false) }
  var isInsideVehicle by remember { mutableStateOf(false) }

  fun executeTurn(branch: SanaaTurnBranch) {
    currentDistrict = branch.targetDistrict
    playerX = 0f
    playerAngleDeg = (playerAngleDeg + (if (branch.turnDirection < 0) -90f else 90f)) % 360f
    turnAlertMessage = "⮌ تم الانعطاف بنجاح نحو: ${branch.titleAr}"
    turnAlertTimer = 3.5f
    GameSoundEffects.playNitroBoost()
    HapticManager.vibrateHeavyImpact()
  }

  // Game stats
  var playerHealth by remember { mutableFloatStateOf(1.0f) }
  var playerArmor by remember { mutableFloatStateOf(1.0f) }
  var cashAmount by remember { mutableIntStateOf(80872) }
  var gameTimeMinutes by remember { mutableIntStateOf(21 * 60 + 13) }
  var chaseRemainingSeconds by remember { mutableIntStateOf(300) }
  var timerAccumulator by remember { mutableFloatStateOf(0f) }

  // Session state
  var isPaused by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var gameOverReason by remember { mutableStateOf("") }
  var savedScoreRecord by remember { mutableStateOf<HighScoreEntity?>(null) }
  var globalBestScore by remember { mutableIntStateOf(0) }
  var isSavingToDb by remember { mutableStateOf(false) }

  // Projectiles
  val projectiles = remember { mutableStateListOf<GameProjectile>() }

  // Traffic Vehicles
  val trafficVehicles = remember {
    mutableStateListOf(
      TrafficCar(1, 25f, -1, "SHAS", Color(0xFFD7CCC8)),
      TrafficCar(2, 55f, 1, "DABAB", Color(0xFFFBC02D)),
      TrafficCar(3, 85f, -1, "POLICE", Color(0xFF0D47A1)),
      TrafficCar(4, 115f, 1, "TAXI", Color(0xFFECEFF1))
    )
  }

  // Sana'ani Pedestrians on Sidewalks
  val pedestrians = remember {
    mutableStateListOf(
      SanaaPedestrian(1, 15f, -1.05f, SanaaPedestrianType.SANAANI_MAN, 0.7f),
      SanaaPedestrian(2, 35f, 1.08f, SanaaPedestrianType.STREET_POTATO_VENDOR, 0.45f),
      SanaaPedestrian(3, 58f, -1.06f, SanaaPedestrianType.ELDER_WITH_CANE, 0.35f),
      SanaaPedestrian(4, 78f, 1.05f, SanaaPedestrianType.SANAANI_SITARA_WOMAN, 0.6f),
      SanaaPedestrian(5, 98f, -1.08f, SanaaPedestrianType.SANAANI_YOUTH, 0.85f)
    )
  }

  // Commercial Shops & Souqs
  val commercialShops = remember {
    mutableStateListOf(
      SanaaCommercialShop(1, "سوق الملح والتوابل 🧂", "بهارات وتوابل", Color(0xFF1B5E20), Color.White, Color(0xFFFFD54F), -1, 30f),
      SanaaCommercialShop(2, "دكان العقيق اليماني 💍", "مجوهرات وفضة", Color(0xFF0D47A1), Color.White, Color(0xFF00E5FF), 1, 30f),
      SanaaCommercialShop(3, "مطعم الشيباني للسلتة 🍲", "مأكولات شعبية", Color(0xFFB71C1C), Color.White, Color(0xFFFF9800), -1, 65f),
      SanaaCommercialShop(4, "مخبز الرغيف والملوج 🥖", "أفران تقليدية", Color(0xFFE65100), Color.White, Color(0xFFFFEB3B), 1, 65f),
      SanaaCommercialShop(5, "بوفية شاي عدني وكرك ☕", "مشروبات ومقاهي", Color(0xFF4A148C), Color.White, Color(0xFFEEFF41), -1, 95f),
      SanaaCommercialShop(6, "محلات القات والبن 🌿", "تراث يماني", Color(0xFF2E7D32), Color.White, Color(0xFF76FF03), 1, 95f)
    )
  }

  // Controller Input Mode (Joypad vs. Joystick)
  var controllerMode by remember { mutableStateOf(ControllerInputMode.JOYPAD) }
  var inputVectorX by remember { mutableFloatStateOf(0f) }
  var inputVectorY by remember { mutableFloatStateOf(0f) }

  // Restart session
  fun restartGameSession() {
    GameSoundEffects.playNitroBoost()
    HapticManager.vibrateHeavyImpact()
    playerHealth = 1.0f
    playerArmor = 1.0f
    cashAmount = 80872
    gameTimeMinutes = 21 * 60 + 13
    chaseRemainingSeconds = 300
    timerAccumulator = 0f
    playerX = 0f
    playerZ = 0f
    playerAngleDeg = 0f
    isWalking = false
    isRunning = false
    isJumping = false
    jumpHeight = 0f
    isInsideVehicle = false
    inputVectorX = 0f
    inputVectorY = 0f
    projectiles.clear()
    trafficVehicles.clear()
    trafficVehicles.addAll(
      listOf(
        TrafficCar(1, 25f, -1, "SHAS", Color(0xFFD7CCC8)),
        TrafficCar(2, 55f, 1, "DABAB", Color(0xFFFBC02D)),
        TrafficCar(3, 85f, -1, "POLICE", Color(0xFF0D47A1)),
        TrafficCar(4, 115f, 1, "TAXI", Color(0xFFECEFF1))
      )
    )
    savedScoreRecord = null
    roadCurvature = 0f
    activeTurnBranch = null
    turnAlertMessage = null
    turnAlertTimer = 0f
    isPaused = false
    isGameOver = false
  }

  // Game Over trigger
  fun triggerGameOver(reason: String) {
    if (isGameOver) return
    isGameOver = true
    gameOverReason = reason
    HapticManager.vibrateExplosion()

    val calculatedScore = ((playerZ * 15).toInt() + (cashAmount / 10) + (300 - chaseRemainingSeconds) * 8).coerceAtLeast(150)
    val elapsedSeconds = (300 - chaseRemainingSeconds).toFloat().coerceAtLeast(1f)

    coroutineScope.launch {
      isSavingToDb = true
      val entity = HighScoreEntity(
        playerName = currentHero.heroNameAr,
        score = calculatedScore,
        mode = "GTA_SANAA_10D",
        difficulty = "NORMAL",
        dateEpoch = System.currentTimeMillis(),
        titleAr = currentHero.heroTitleAr,
        rankBadgeEmoji = when (currentHero) {
          UnifiedHeroId.MAZEN -> "👑"
          UnifiedHeroId.FARIS -> "🧗‍♂️"
          UnifiedHeroId.AMMAR -> "🚐"
          UnifiedHeroId.SALEM -> "🎒"
        },
        coinsEarned = (cashAmount / 50).coerceAtLeast(25),
        chaseTimeSeconds = elapsedSeconds,
        stageName = currentDistrict.titleAr,
        isPersonalBest = true
      )
      val newId = highScoreDao.insertHighScore(entity)
      val fromDb = highScoreDao.getScoreById(newId) ?: entity.copy(id = newId)
      savedScoreRecord = fromDb
      globalBestScore = highScoreDao.getGlobalHighScore() ?: calculatedScore
      isSavingToDb = false
    }
  }

  // Main Physics & Simulation Loop
  LaunchedEffect(isPaused, isGameOver, currentHero, isInsideVehicle) {
    var lastTimeNanos = System.nanoTime()

    while (isActive && !isGameOver) {
      withFrameNanos { now ->
        if (!isPaused && !isGameOver) {
          val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
          lastTimeNanos = now

          val speedFactor = currentHero.baseSpeed * (if (isInsideVehicle) 2.5f else 1.0f)
          val mag = sqrt(inputVectorX * inputVectorX + inputVectorY * inputVectorY)

          if (mag > 0.15f) {
            isWalking = true
            isRunning = mag > 0.85f

            val moveSpeed = (if (isRunning) 14f else 8f) * speedFactor * dt
            playerZ += moveSpeed
            playerX = (playerX + (inputVectorX * 1.5f * dt)).coerceIn(-1.15f, 1.15f)
          } else {
            isWalking = false
            isRunning = false
            if (isInsideVehicle) {
              playerZ += 6f * dt // idle engine cruise
            }
          }

          // Smooth Dynamic Road Curvature based on road geometry and steering input
          val naturalCurve = sin(playerZ * 0.012f) * 0.38f
          val steeringCurve = (inputVectorX * 0.32f)
          roadCurvature = (naturalCurve + steeringCurve).coerceIn(-0.85f, 0.85f)
          playerAngleDeg = (playerAngleDeg + inputVectorX * 35f * dt) % 360f

          // Detect nearest branch intersection
          val currentCycleZ = (playerZ % 600f)
          val candidate = availableTurnBranches.firstOrNull { abs(it.triggerZ - currentCycleZ) < 32f }
          activeTurnBranch = candidate

          // Automatic steering turn if player moves into the turn branch alleyway
          if (candidate != null) {
            val isTurningLeft = candidate.turnDirection < 0 && (playerX < -0.85f || inputVectorX < -0.7f)
            val isTurningRight = candidate.turnDirection > 0 && (playerX > 0.85f || inputVectorX > 0.7f)
            if (isTurningLeft || isTurningRight) {
              executeTurn(candidate)
            }
          }

          if (turnAlertTimer > 0f) {
            turnAlertTimer -= dt
            if (turnAlertTimer <= 0f) {
              turnAlertMessage = null
            }
          }

          // Jump physics
          if (isJumping) {
            jumpHeight += 3.8f * dt
            if (jumpHeight >= 1.0f) {
              jumpHeight = 1.0f
              isJumping = false
            }
          } else if (jumpHeight > 0f) {
            jumpHeight -= 4.2f * dt
            if (jumpHeight < 0f) jumpHeight = 0f
          }

          // Timer and Game clock
          timerAccumulator += dt
          if (timerAccumulator >= 1.0f) {
            timerAccumulator -= 1.0f
            gameTimeMinutes += 1
            if (chaseRemainingSeconds > 0) {
              chaseRemainingSeconds -= 1
            }
          }

          // Advance Pedestrians
          for (ped in pedestrians) {
            ped.worldZ += (if (ped.isWalkingForward) 1f else -1f) * ped.walkSpeed * dt * 4f
            if (ped.worldZ < playerZ - 20f) {
              ped.worldZ = playerZ + 90f + (ped.id * 15f)
            } else if (ped.worldZ > playerZ + 120f) {
              ped.worldZ = playerZ + 10f
            }
          }

          // Advance Projectiles
          val projIterator = projectiles.iterator()
          while (projIterator.hasNext()) {
            val p = projIterator.next()
            p.screenY -= p.velocityY * dt
            p.lifeTime -= dt
            if (p.lifeTime <= 0f || p.screenY < 0f) {
              projIterator.remove()
            }
          }

          // Advance Traffic Vehicles
          for (car in trafficVehicles) {
            if (car.laneIndex < 0) {
              car.worldZ -= 8.5f * dt
              if (car.worldZ < playerZ - 30f) {
                car.worldZ = playerZ + 100f + (car.id * 20f)
              }
            } else {
              car.worldZ += 9.5f * dt
              if (car.worldZ < playerZ - 20f) {
                car.worldZ = playerZ + 120f + (car.id * 20f)
              }
            }

            // Vehicle collision
            if (!isInsideVehicle && abs(car.worldZ - playerZ) < 3.0f) {
              val carX = if (car.laneIndex < 0) -0.5f else 0.5f
              if (abs(carX - playerX) < 0.35f) {
                if (playerArmor > 0f) {
                  playerArmor = (playerArmor - 0.6f * dt).coerceAtLeast(0f)
                } else {
                  playerHealth = (playerHealth - 0.5f * dt).coerceAtLeast(0f)
                }
                HapticManager.vibrateClick()
              }
            }
          }

          // Check Game Over conditions
          if (playerHealth <= 0f) {
            triggerGameOver("WASTED - سقط البطل في أزقة صنعاء! 💀")
          } else if (chaseRemainingSeconds <= 0) {
            triggerGameOver("BUSTED - انتهى الوقت وحاصرتك دوريات الشرطة! 🚓")
          }
        } else {
          lastTimeNanos = now
        }
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // 1. 10D Realistic High-Fidelity Game Canvas
    UnifiedGtaGameCanvas(
      hero = currentHero,
      weapon = currentWeapon,
      playerX = playerX,
      playerZ = playerZ,
      playerAngleDeg = playerAngleDeg,
      isWalking = isWalking,
      isRunning = isRunning,
      jumpHeight = jumpHeight,
      isInsideVehicle = isInsideVehicle,
      projectiles = projectiles,
      trafficVehicles = trafficVehicles,
      modifier = Modifier.fillMaxSize(),
      is10DRealScale = is10DRealScale,
      currentDistrict = currentDistrict,
      pedestrians = pedestrians,
      commercialShops = commercialShops,
      roadCurvature = roadCurvature,
      activeTurnBranch = activeTurnBranch,
      onCameraRotate = { deltaDeg ->
        playerAngleDeg = (playerAngleDeg + deltaDeg) % 360f
      }
    )

    // 2. Authentic GTA Mobile HUD
    GtaAuthenticHud(
      hero = currentHero,
      weapon = currentWeapon,
      healthPercent = playerHealth,
      armorPercent = playerArmor,
      cashAmount = cashAmount,
      gameTimeMinutes = gameTimeMinutes,
      locationNameAr = currentDistrict.titleAr,
      playerWorldAngle = playerAngleDeg,
      policeDistance = 45f,
      chaseRemainingSeconds = chaseRemainingSeconds,
      modifier = Modifier.fillMaxSize()
    )

    // 3. Top Action Controls Bar: Back, District Badge, 10D Scale Switcher, Pause
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Back to Menu Button
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(Color(0xCC111827))
          .border(1.dp, Color(0x66FFFFFF), CircleShape)
          .testTag("btn_engine_back")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = Color.White, modifier = Modifier.size(18.dp))
      }

      // Sana'a Landmark & District Navigator Button (Clickable!)
      Surface(
        color = Color(0xEE111827),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, currentDistrict.themeColor),
        modifier = Modifier
          .height(38.dp)
          .clickable { showDistrictSelectorDialog = true }
          .testTag("btn_district_navigator")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(currentDistrict.iconEmoji, fontSize = 16.sp)
          Text(
            text = currentDistrict.titleAr.take(18) + "...",
            color = Color(0xFFF5C518),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
          )
          Icon(Icons.Default.Map, contentDescription = "خارطة صنعاء", tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
        }
      }

      // 10D Real-Life Scale Toggle Button
      Surface(
        color = if (is10DRealScale) Color(0xEE1B5E20) else Color(0xCC111827),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, if (is10DRealScale) Color(0xFF00E676) else Color(0x66FFFFFF)),
        modifier = Modifier
          .height(38.dp)
          .clickable {
            is10DRealScale = !is10DRealScale
            GameSoundEffects.playCoin()
            HapticManager.vibrateClick()
          }
          .testTag("btn_toggle_10d_scale")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text("🎥", fontSize = 13.sp)
          Text(
            text = if (is10DRealScale) "10D واقعي" else "10D تكتيكي",
            color = if (is10DRealScale) Color.White else Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Pause Button
      IconButton(
        onClick = { isPaused = !isPaused },
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(Color(0xCC111827))
          .border(1.dp, Color(0x66FFFFFF), CircleShape)
          .testTag("btn_engine_pause")
      ) {
        Icon(
          imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
          contentDescription = "إيقاف مؤقت",
          tint = Color(0xFFF5C518),
          modifier = Modifier.size(18.dp)
        )
      }
    }

    // 4. Yemeni Zawamil Music HUD Player Bar (راديو الزوامل اليمنية الحماسية 🇾🇪)
    Surface(
      color = Color(0xEE0D1624),
      shape = RoundedCornerShape(22.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66F5C518)),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 54.dp)
        .testTag("zamil_hud_player_bar")
    ) {
      Row(
        modifier = Modifier
          .padding(horizontal = 12.dp, vertical = 5.dp)
          .clickable { showZawamilDialog = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Play / Pause Button
        IconButton(
          onClick = {
            YemeniZawamilEngine.toggleZamilPlayback()
            HapticManager.vibrateClick()
          },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (YemeniZawamilEngine.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
            contentDescription = "تشغيل الزامل",
            tint = Color(0xFFFFD54F),
            modifier = Modifier.size(26.dp)
          )
        }

        // Jumping Equalizer Audio Bars (Animated with Zamil Rhythm)
        Row(
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          verticalAlignment = Alignment.Bottom,
          modifier = Modifier.height(18.dp)
        ) {
          val pulse = YemeniZawamilEngine.beatPulse
          val barHeights = listOf(
            (6 + pulse * 12).coerceIn(4f, 18f),
            (10 + pulse * 8).coerceIn(4f, 18f),
            (4 + pulse * 14).coerceIn(4f, 18f),
            (8 + pulse * 10).coerceIn(4f, 18f)
          )
          for (h in barHeights) {
            Box(
              modifier = Modifier
                .width(3.dp)
                .height(h.dp)
                .background(Color(0xFF00E676), RoundedCornerShape(1.dp))
            )
          }
        }

        // Zamil Track Title
        Column {
          Text(
            text = YemeniZawamilEngine.currentTrack.titleAr,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "اضغط لتغيير الزامل وعرض الكلمات 📜",
            color = Color(0xFFAAAAAA),
            fontSize = 9.sp
          )
        }

        // Next Zamil Track
        IconButton(
          onClick = {
            YemeniZawamilEngine.nextTrack()
            HapticManager.vibrateClick()
          },
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "الزامل التالي",
            tint = Color(0xFFFFD54F),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // 4.5. Interactive Street Turn & Alleyway Navigator Banner (الأنعطاف والتنقل بين الأحياء والأزقة)
    AnimatedVisibility(
      visible = activeTurnBranch != null,
      enter = fadeIn() + slideInVertically(),
      exit = fadeOut() + slideOutVertically(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 96.dp)
    ) {
      activeTurnBranch?.let { branch ->
        val isLeft = branch.turnDirection < 0
        Surface(
          color = Color(0xF0111827),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
          modifier = Modifier
            .clickable { executeTurn(branch) }
            .testTag("btn_street_turn_branch")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = if (isLeft) "⮌" else "⮎",
              color = Color(0xFFFFD54F),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
            Column(horizontalAlignment = if (isLeft) Alignment.Start else Alignment.End) {
              Text(
                text = if (isLeft) "««« انعطف يساراً: ${branch.titleAr}" else "${branch.titleAr} :انعطف يميناً »»»",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "المس هنا أو حرّك عصا التحكم نحو ${branch.subtitleAr}",
                color = Color(0xFF00E676),
                fontSize = 10.sp
              )
            }
            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // Turn Confirmation Alert
    AnimatedVisibility(
      visible = turnAlertMessage != null,
      enter = fadeIn() + scaleIn(),
      exit = fadeOut() + scaleOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 152.dp)
    ) {
      turnAlertMessage?.let { msg ->
        Surface(
          color = Color(0xEE1B5E20),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E676))
        ) {
          Text(
            text = msg,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          )
        }
      }
    }

    // 5. Controller Cluster on Bottom-Left: Joypad or Analog Joystick
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(bottom = 14.dp, start = 14.dp),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      JoypadModeToggle(
        currentMode = controllerMode,
        onModeChanged = { controllerMode = it }
      )

      if (controllerMode == ControllerInputMode.JOYPAD) {
        GtaJoypadController(
          sizeDp = 142f,
          onMove = { normX, normY ->
            inputVectorX = normX
            inputVectorY = normY
          },
          onRelease = {
            inputVectorX = 0f
            inputVectorY = 0f
          }
        )
      } else {
        GtaAnalogJoystick(
          baseRadiusDp = 64f,
          knobRadiusDp = 26f,
          onMove = { normX, normY ->
            inputVectorX = normX
            inputVectorY = normY
          },
          onRelease = {
            inputVectorX = 0f
            inputVectorY = 0f
          }
        )
      }
    }

    // 6. Action Buttons Cluster on Bottom-Right
    GtaActionButtonsCluster(
      currentHero = currentHero,
      currentWeapon = currentWeapon,
      isInsideVehicle = isInsideVehicle,
      onShoot = {
        projectiles.add(
          GameProjectile(
            id = System.currentTimeMillis(),
            screenX = 500f + playerX * 250f,
            screenY = 700f,
            velocityY = 850f,
            type = currentWeapon.soundEffectKey
          )
        )
        cashAmount += 10
      },
      onVehicleToggle = {
        isInsideVehicle = !isInsideVehicle
        GameSoundEffects.playNitroBoost()
      },
      onJumpOrSprint = {
        if (!isJumping && jumpHeight <= 0f) {
          isJumping = true
        }
      },
      onSwitchHero = {
        currentHeroIndex = (currentHeroIndex + 1) % UnifiedHeroId.values().size
        GameSoundEffects.playCoin()
      },
      onSwitchWeapon = {
        currentWeaponIndex = (currentWeaponIndex + 1) % UNIFIED_WEAPONS.size
        GameSoundEffects.playReload()
      },
      modifier = Modifier.align(Alignment.BottomEnd)
    )

    // 7. Sana'a Districts & Landmarks Navigator Dialog (خارطة شوارع ومعالم صنعاء)
    if (showDistrictSelectorDialog) {
      Dialog(onDismissRequest = { showDistrictSelectorDialog = false }) {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF5C518)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🗺️ شوارع ومعالم صنعاء 10D",
                color = Color(0xFFF5C518),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              IconButton(onClick = { showDistrictSelectorDialog = false }) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
              }
            }

            Text(
              text = "اختر الشارع أو المعلم الذي ترغب بالتجوال والمرور فيه:",
              color = Color.LightGray,
              fontSize = 11.5.sp,
              modifier = Modifier.fillMaxWidth(),
              textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(SanaaDistrict.values()) { dist ->
                val isSelected = dist == currentDistrict
                Surface(
                  color = if (isSelected) Color(0xFF1F2937) else Color(0xFF0D1117),
                  shape = RoundedCornerShape(12.dp),
                  border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFFFFD54F) else Color(0x44FFFFFF)
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      currentDistrict = dist
                      showDistrictSelectorDialog = false
                      GameSoundEffects.playCoin()
                      HapticManager.vibrateClick()
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Text(dist.iconEmoji, fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                      Text(dist.titleAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text(dist.descriptionAr, color = Color.Gray, fontSize = 10.5.sp)
                    }
                    if (isSelected) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 8. Yemeni Zawamil Selection & Lyrics Dialog (مشغل الزوامل والكلمات)
    if (showZawamilDialog) {
      Dialog(onDismissRequest = { showZawamilDialog = false }) {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E676)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🇾🇪 راديو الزوامل اليمنية الأصيلة",
                color = Color(0xFF00E676),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              IconButton(onClick = { showZawamilDialog = false }) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current Lyrics Card
            Surface(
              color = Color(0xFF1E293B),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFD54F)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("📜 أبيات الزامل الحماسي:", color = Color(0xFFFFD54F), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                val track = YemeniZawamilEngine.currentTrack
                for ((idx, verse) in track.lyrics.withIndex()) {
                  val isCurrentLine = idx == YemeniZawamilEngine.currentLyricLineIndex
                  Text(
                    text = verse,
                    color = if (isCurrentLine) Color(0xFF00E676) else Color.White,
                    fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playlist of Tracks
            Text("قائمة الزوامل المتوفرة:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(YemeniZawamilEngine.playlist.indices.toList()) { idx ->
                val trk = YemeniZawamilEngine.playlist[idx]
                val isSelected = idx == YemeniZawamilEngine.currentTrackIndex
                Surface(
                  color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B1220),
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF00E676) else Color(0x33FFFFFF)
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      YemeniZawamilEngine.selectTrack(idx)
                      HapticManager.vibrateClick()
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text(trk.titleAr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                      Text("${trk.categoryAr} • ${trk.bpm} BPM", color = Color.Gray, fontSize = 10.sp)
                    }
                    if (isSelected && YemeniZawamilEngine.isPlaying) {
                      Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF00E676))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 9. Pause Dialog
    AnimatedVisibility(
      visible = isPaused && !isGameOver,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Surface(
        color = Color(0xDD111827),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF5C518)),
        modifier = Modifier.padding(24.dp)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("⏸️ اللعبة متوقفة مؤقتاً", color = Color(0xFFF5C518), fontSize = 18.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = { isPaused = false },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C518)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("استئناف اللعب ▶", color = Color.Black, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = {
              isPaused = false
              triggerGameOver("نهاية الجولة بناءً على طلب اللاعب 🛑")
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("btn_end_game_session")
          ) {
            Text("إنهاء الجولة وعرض النتيجة 📊", color = Color(0xFFFF5252), fontSize = 12.sp)
          }
        }
      }
    }

    // 10. Game Over Overlay with Room DB Persistence & Immediate Session Restart
    AnimatedVisibility(
      visible = isGameOver,
      enter = fadeIn() + scaleIn(initialScale = 0.92f),
      exit = fadeOut() + scaleOut(targetScale = 0.92f),
      modifier = Modifier.fillMaxSize()
    ) {
      GameOverOverlay(
        reason = gameOverReason,
        savedRecord = savedScoreRecord,
        globalBestScore = globalBestScore,
        isSaving = isSavingToDb,
        currentHero = currentHero,
        onRestart = { restartGameSession() },
        onExitToMenu = onNavigateBack
      )
    }
  }
}

/**
 * Game Over Overlay displaying the final score saved from the database
 * and providing an immediate restart button to resume play without exiting the app.
 */
@Composable
fun GameOverOverlay(
  reason: String,
  savedRecord: HighScoreEntity?,
  globalBestScore: Int,
  isSaving: Boolean,
  currentHero: UnifiedHeroId,
  onRestart: () -> Unit,
  onExitToMenu: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xF2090D14))
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    val isWasted = reason.contains("WASTED")
    val borderColor = if (isWasted) Color(0xFFFF1744) else Color(0xFFF5C518)

    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
      shape = RoundedCornerShape(24.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 500.dp)
        .border(2.dp, borderColor, RoundedCornerShape(24.dp))
        .testTag("game_over_overlay_card")
    ) {
      Column(
        modifier = Modifier
          .padding(22.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (isWasted) "WASTED 💀" else "BUSTED 🚨",
          color = if (isWasted) Color(0xFFFF5252) else Color(0xFFF5C518),
          fontSize = 28.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = reason,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
          color = Color(0xFF21262D),
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "البطل: ${currentHero.heroNameAr} (${currentHero.heroTitleAr})",
              color = Color(0xFFF5C518),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp))
        ) {
          androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_sanaa_street),
            contentDescription = "شوارع ومنازل صنعاء القديمة",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color(0xDD000000))
                )
              )
          )
          Text(
            text = "📍 صنعاء القديمة - أزقة باب اليمن التراثية",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 6.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          color = Color(0xFF0D1117),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF30363D)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "النتيجة النهائية المحفوظة في قاعدة البيانات (Room DB)",
              color = Color.LightGray,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isSaving) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(22.dp),
                  color = Color(0xFFF5C518),
                  strokeWidth = 2.5.dp
                )
                Text("جاري الحفظ في Room Database...", color = Color.White, fontSize = 12.sp)
              }
            } else if (savedRecord != null) {
              Text(
                text = "${savedRecord.score}",
                modifier = Modifier.testTag("final_saved_score_text"),
                color = Color(0xFF4CAF50),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
              )

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF4CAF50),
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "تم التوثيق محلياً في قاعدة البيانات بنجاح",
                  color = Color(0xFF4CAF50),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("الغنائم المحصلة", color = Color.Gray, fontSize = 11.sp)
                  Text("+${savedRecord.coinsEarned} 🪙", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("مدة المطاردة", color = Color.Gray, fontSize = 11.sp)
                  Text(savedRecord.getFormattedChaseTime(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("أعلى رقم قياسي", color = Color.Gray, fontSize = 11.sp)
                  Text("${maxOf(globalBestScore, savedRecord.score)} 🏆", color = Color(0xFF81D4FA), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
              }
            } else {
              Text("0", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onRestart,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C518)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("btn_restart_game")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "إعادة المحاولة فوراً 🔄 (Restart)",
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onExitToMenu,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_game_over_back_menu")
        ) {
          Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text("العودة للقائمة الرئيسية 🏠", color = Color.White, fontSize = 13.sp)
        }
      }
    }
  }
}
