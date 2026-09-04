package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*

/**
 * Unified GTA San Andreas Sana'a Game Engine Screen.
 * Merges the 4 heroes into a unified, high-performance open-world game experience:
 * - Direct 360 player movement via Analog Virtual Joystick on bottom-left
 * - Action buttons on bottom-right (Shoot, Vehicle, Jump/Sprint, Hero Switch, Weapon Switch)
 * - Complete absence of intrusive dialog boxes and notifications
 * - Authentic GTA Mobile HUD (Radar Mini-Map, Clock, Green Cash Counter, Health/Armor, Location)
 */
@Composable
fun UnifiedGtaGameEngineScreen(
  initialHeroId: UnifiedHeroId = UnifiedHeroId.MAZEN,
  onNavigateBack: () -> Unit = {},
  modifier: Modifier = Modifier
) {
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

  // Game Loop Coroutine (60 FPS smooth physics & distance progression)
  LaunchedEffect(isPaused, currentHero, isInsideVehicle) {
    var lastTimeNanos = System.nanoTime()

    while (isActive) {
      withFrameNanos { now ->
        if (!isPaused) {
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

          // Advance in-game clock slowly
          gameTimeMinutes = (gameTimeMinutes + 1) % (24 * 60)

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

          // Advance Traffic Vehicles
          for (car in trafficVehicles) {
            if (car.laneIndex < 0) {
              car.worldZ -= 7f * dt // oncoming
            } else {
              car.worldZ += 9f * dt // forward
            }
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
    // 1. 3D Third-Person Perspective Canvas (Matching Screenshots 1, 2, 3)
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

    // 2. Authentic GTA Mobile HUD (Radar Mini-Map, Clock, Green Cash, Health, Location)
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
      modifier = Modifier.fillMaxSize()
    )

    // 3. Top-Center Mini Bar (Pause & Back button without any intrusive dialogs!)
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

    // 4. Virtual Analog Joystick on Bottom-Left (Matching Screenshot 3)
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

    // 6. Subtly Fading Hero Switch Announcement (Pure visual, no blocking dialogs!)
    AnimatedVisibility(
      visible = isPaused,
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
        }
      }
    }
  }
}
