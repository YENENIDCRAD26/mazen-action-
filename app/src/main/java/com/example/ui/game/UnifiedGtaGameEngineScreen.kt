package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.HighScoreEntity
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Unified GTA San Andreas Sana'a Game Engine Screen.
 * Merges the 4 heroes into a unified, high-performance open-world game experience:
 * - Direct 360 player movement via Analog Virtual Joystick on bottom-left
 * - Action buttons on bottom-right (Shoot, Vehicle, Jump/Sprint, Hero Switch, Weapon Switch)
 * - Complete absence of intrusive dialog boxes and notifications
 * - Authentic GTA Mobile HUD (Radar Mini-Map, Clock, Green Cash Counter, Health/Armor, Location)
 * - Game Over overlay with Room DB score persistence and immediate restart without closing app
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

  // Player position & movement physics
  var playerX by remember { mutableFloatStateOf(0f) } // -1f (left) to 1f (right)
  var playerZ by remember { mutableFloatStateOf(0f) } // distance progression
  var playerAngleDeg by remember { mutableFloatStateOf(0f) }
  var isWalking by remember { mutableStateOf(false) }
  var isRunning by remember { mutableStateOf(false) }
  var jumpHeight by remember { mutableFloatStateOf(0f) }
  var isJumping by remember { mutableStateOf(false) }
  var isInsideVehicle by remember { mutableStateOf(false) }

  // Game Stats
  var playerHealth by remember { mutableFloatStateOf(1.0f) }
  var playerArmor by remember { mutableFloatStateOf(1.0f) }
  var cashAmount by remember { mutableIntStateOf(80872) }
  var gameTimeMinutes by remember { mutableIntStateOf(21 * 60 + 13) } // 21:13 starting time
  var isPaused by remember { mutableStateOf(false) }
  var chaseRemainingSeconds by remember { mutableIntStateOf(300) } // 5 minutes chase timer
  var timerAccumulator by remember { mutableFloatStateOf(0f) }

  // Game Over & Room DB persistence state
  var isGameOver by remember { mutableStateOf(false) }
  var gameOverReason by remember { mutableStateOf("WASTED - سقط البطل في الاشتباك 💀") }
  var savedScoreRecord by remember { mutableStateOf<HighScoreEntity?>(null) }
  var globalBestScore by remember { mutableIntStateOf(0) }
  var isSavingToDb by remember { mutableStateOf(false) }

  // Projectiles
  val projectiles = remember { mutableStateListOf<GameProjectile>() }

  // Traffic vehicles
  val trafficVehicles = remember {
    mutableStateListOf(
      TrafficCar(1, 25f, -1, "SEDAN", Color(0xFFC2185B)),
      TrafficCar(2, 55f, 1, "DABAB", Color(0xFFFBC02D)),
      TrafficCar(3, 85f, -1, "POLICE", Color(0xFF1976D2)),
      TrafficCar(4, 115f, 1, "SEDAN", Color(0xFF388E3C))
    )
  }

  // Joystick Input Vector
  var inputVectorX by remember { mutableFloatStateOf(0f) }
  var inputVectorY by remember { mutableFloatStateOf(0f) }

  // Function to restart the game session immediately without closing the app
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
        TrafficCar(1, 25f, -1, "SEDAN", Color(0xFFC2185B)),
        TrafficCar(2, 55f, 1, "DABAB", Color(0xFFFBC02D)),
        TrafficCar(3, 85f, -1, "POLICE", Color(0xFF1976D2)),
        TrafficCar(4, 115f, 1, "SEDAN", Color(0xFF388E3C))
      )
    )
    savedScoreRecord = null
    isPaused = false
    isGameOver = false
  }

  // Function to trigger Game Over, calculate score, and persist into Room Database
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
        mode = "GTA_SANAA_UNIFIED",
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
        stageName = "باب اليمن - أزقة صنعاء",
        isPersonalBest = true
      )
      val newId = highScoreDao.insertHighScore(entity)
      val fromDb = highScoreDao.getScoreById(newId) ?: entity.copy(id = newId)
      savedScoreRecord = fromDb
      globalBestScore = highScoreDao.getGlobalHighScore() ?: calculatedScore
      isSavingToDb = false
    }
  }

  // Game Loop Coroutine (60 FPS smooth physics & distance progression)
  LaunchedEffect(isPaused, isGameOver, currentHero, isInsideVehicle) {
    var lastTimeNanos = System.nanoTime()

    while (isActive && !isGameOver) {
      withFrameNanos { now ->
        if (!isPaused && !isGameOver) {
          val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
          lastTimeNanos = now

          val speedFactor = currentHero.baseSpeed * (if (isInsideVehicle) 2.4f else 1.0f)

          // Process movement input
          val mag = sqrt(inputVectorX * inputVectorX + inputVectorY * inputVectorY)
          if (mag > 0.08f) {
            isWalking = true
            isRunning = mag > 0.65f

            // Move player laterally
            val speedX = inputVectorX * 0.9f * speedFactor * dt
            playerX = (playerX + speedX).coerceIn(-0.95f, 0.95f)

            // Forward movement
            val forwardSpeed = (if (inputVectorY < 0) -inputVectorY else 0.4f) * 14f * speedFactor * dt
            playerZ += forwardSpeed

            // Calculate rotation angle
            playerAngleDeg = (atan2(inputVectorX, -inputVectorY) * 180f / PI.toFloat())
          } else {
            isWalking = false
            isRunning = false
            // Auto idle scroll forward slightly if in vehicle
            if (isInsideVehicle) {
              playerZ += 6f * dt
            }
          }

          // Jump Physics
          if (isJumping) {
            jumpHeight += 3.5f * dt * currentHero.jumpPower
            if (jumpHeight >= 1f) {
              isJumping = false
            }
          } else if (jumpHeight > 0f) {
            jumpHeight = (jumpHeight - 4.5f * dt).coerceAtLeast(0f)
          }

          // Advance in-game clock slowly & chase timer
          timerAccumulator += dt
          if (timerAccumulator >= 1f) {
            timerAccumulator -= 1f
            gameTimeMinutes = (gameTimeMinutes + 1) % (24 * 60)
            if (chaseRemainingSeconds > 0) {
              chaseRemainingSeconds -= 1
            }
          }

          // Update Projectiles
          val iterator = projectiles.iterator()
          while (iterator.hasNext()) {
            val p = iterator.next()
            p.screenY -= p.velocityY * dt
            p.lifeTime -= dt * 1.5f
            if (p.lifeTime <= 0f || p.screenY < 0f) {
              iterator.remove()
            }
          }

          // Advance Traffic Vehicles & Check Collision
          for (car in trafficVehicles) {
            if (car.laneIndex < 0) {
              car.worldZ -= 8f * dt // oncoming traffic
              if (car.worldZ < playerZ - 30f) {
                car.worldZ = playerZ + 100f + (car.id * 20f)
              }
            } else {
              car.worldZ += 9f * dt // forward
              if (car.worldZ < playerZ - 20f) {
                car.worldZ = playerZ + 120f + (car.id * 20f)
              }
            }

            // Check collision with player when on foot
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
    // 1. 3D Third-Person Perspective Canvas
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
      onCameraRotate = { deltaDeg ->
        playerAngleDeg = (playerAngleDeg + deltaDeg) % 360f
      }
    )

    // 2. Authentic GTA Mobile HUD (Radar Mini-Map, Clock, Green Cash, Health, Location, Circular Chase Timer)
    GtaAuthenticHud(
      hero = currentHero,
      weapon = currentWeapon,
      healthPercent = playerHealth,
      armorPercent = playerArmor,
      cashAmount = cashAmount,
      gameTimeMinutes = gameTimeMinutes,
      locationNameAr = "حي صنعاء - باب اليمن",
      playerWorldAngle = playerAngleDeg,
      policeDistance = 45f,
      chaseRemainingSeconds = chaseRemainingSeconds,
      modifier = Modifier.fillMaxSize()
    )

    // 3. Top-Center Mini Bar (Pause & Back button)
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color(0xCC111827))
          .border(1.dp, Color(0x66FFFFFF), CircleShape)
          .testTag("btn_engine_back")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "عودة",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = { isPaused = !isPaused },
        modifier = Modifier
          .size(36.dp)
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

    // 4. Virtual Analog Joystick on Bottom-Left
    Box(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(bottom = 20.dp, start = 18.dp)
    ) {
      GtaAnalogJoystick(
        baseRadiusDp = 65f,
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

    // 5. Action Buttons Cluster on Bottom-Right (Shoot, Vehicle, Jump, Hero Switch, Weapon Switch)
    GtaActionButtonsCluster(
      currentHero = currentHero,
      currentWeapon = currentWeapon,
      isInsideVehicle = isInsideVehicle,
      onShoot = {
        // Spawn Projectile from player center
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
      },
      onJumpOrSprint = {
        if (!isJumping && jumpHeight <= 0f) {
          isJumping = true
        }
      },
      onSwitchHero = {
        // Seamlessly cycle through all 4 heroes!
        currentHeroIndex = (currentHeroIndex + 1) % UnifiedHeroId.values().size
      },
      onSwitchWeapon = {
        // Cycle weapons
        currentWeaponIndex = (currentWeaponIndex + 1) % UNIFIED_WEAPONS.size
      },
      modifier = Modifier.align(Alignment.BottomEnd)
    )

    // 6. Pause Dialog
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

    // 7. Game Over Overlay with Room DB Persistence & Immediate Session Restart
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
        // 1. Header Banner
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

        // Hero Info Tag
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

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Room Database Saved Score Section
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

              // Grid of Breakdown Stats
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

        // 3. Action Buttons
        // Primary: Restart session immediately without closing the app
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

        // Secondary: Return to Main Menu
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

