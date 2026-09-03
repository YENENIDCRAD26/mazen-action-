package com.example.ui.vehicle

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.model.Faction
import com.example.sound.GameSoundEffects
import com.example.ui.components.SanaaTopBar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.*

enum class GtaVehicleType(
  val titleAr: String,
  val iconEmoji: String,
  val maxSpeed: Float,
  val armor: Float,
  val bodyColor: Color,
  val isPolice: Boolean = false
) {
  DABAB("الدباب الصنعاني الأصفر", "🚐", 0.022f, 120f, DabbabTeal),
  SHAS_TOYOTA("شاص تويوتا الجبلي 4x4", "🛻", 0.026f, 180f, Color(0xFFC29B38)),
  TAXI("تاكسي صنعاء الكلاسيكي", "🚕", 0.024f, 100f, TaxiYellow),
  MOTORBIKE("دراجة نارية صينية سريعة", "🏍️", 0.030f, 60f, GangShawlRed),
  POLICE_CRUISER("دورية نجدة شرطة العاصمة", "🚓", 0.025f, 150f, PoliceNavy, true),
  HEAVY_TRUCK("شاحنة توزيع البضائع", "🚚", 0.016f, 250f, Color(0xFF546E7A))
}

enum class GtaWeapon(
  val titleAr: String,
  val iconEmoji: String,
  val damage: Float,
  val fireRateMs: Long,
  val color: Color
) {
  PUNCH("عراك بالأيدي", "👊", 15f, 300, Color.White),
  SLINGSHOT("المقلاع الحجري", "🪨", 35f, 400, Color(0xFFD7CCC8)),
  BB_GUN("مسدس خرز سريع", "🔫", 25f, 180, TaxiYellow),
  FIREWORKS_RPG("قاذف ألعاب نارية متفجر", "🎆", 90f, 750, GangShawlRed),
  BANANA_TRAP("فخ قشور الموز والزيوت", "🍌", 40f, 600, Color(0xFFFFEB3B))
}

enum class GtaMission(
  val id: Int,
  val titleAr: String,
  val descAr: String,
  val targetDistance: Float,
  val rewardCoins: Int,
  val targetStars: Int
) {
  FREE_ROAM(0, "عالم مفتوح وهجولة حرة", "تجول بالأقدام، اسرق أي مركبة، افلت من دوريات الشرطة، وتفحص شوارع صنعاء بحرية كاملة!", 9999f, 50, 0),
  HEIST_SPICE(1, "📦 سطو على شحنة بهارات سوق الملح", "اسرق دباب البضائع وتفادَ كمين الدورية للوصول إلى المخبأ بأمان.", 400f, 350, 2),
  HEIST_RESCUE(2, "🚓 اعتراض شاحنة الترحيل وتخليص السجناء", "طارد دورية الشرطة، اطلق المفرقعات لتعطيلها، وحرر أطفال الحارة المعتقلين!", 650f, 500, 3),
  HEIST_RACE(3, "🏁 سباق شوارع صنعاء والسبعين", "سابق شاص الحارة وتاكسي الرويشان وكن الأول في خط النهاية!", 800f, 600, 1),
  HEIST_BIG_SCORE(4, "🏦 السطو الكبير على باب اليمن (5 نجوم)", "اقتحم النقطة الأمنية، احصل على الحقيبة الذهبية وافلت من حالة الطوارئ القصوى!", 1000f, 1000, 5)
}

data class GtaTrafficCar(
  val id: Long,
  var x: Float, // 0..1
  var y: Float, // 0..1
  val type: GtaVehicleType,
  var speed: Float,
  var health: Float,
  var isDestroyed: Boolean = false,
  var isStolen: Boolean = false
)

data class GtaPedestrian(
  val id: Long,
  var x: Float,
  var y: Float,
  val nameAr: String,
  val emoji: String,
  var isFleeing: Boolean = false
)

data class GtaProjectile(
  var x: Float,
  var y: Float,
  val vx: Float,
  val vy: Float,
  val weapon: GtaWeapon
)

data class GtaRoadblock(
  var x: Float,
  var y: Float,
  val title: String
)

data class GtaMoneyPickup(
  var x: Float,
  var y: Float,
  val value: Int
)

@Composable
fun VehicleHeistScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  val selectedFaction by repository.selectedFaction.collectAsState()

  // Mode Selection
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Free Roam, 1: Missions
  var currentMission by remember { mutableStateOf(GtaMission.FREE_ROAM) }

  // Game Engine State
  var isPlaying by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var isMissionSuccess by remember { mutableStateOf(false) }

  // Player State
  var playerX by remember { mutableFloatStateOf(0.5f) }
  var playerY by remember { mutableFloatStateOf(0.75f) }
  var isDriving by remember { mutableStateOf(true) }
  var currentVehicle by remember { mutableStateOf(GtaVehicleType.DABAB) }
  var vehicleHealth by remember { mutableFloatStateOf(100f) }
  var playerOnFootHealth by remember { mutableFloatStateOf(100f) }

  // Wanted Stars (0 to 5)
  var wantedStars by remember { mutableIntStateOf(1) }
  var wantedTimer by remember { mutableIntStateOf(0) }

  // Nitro & Turbo
  var nitroLevel by remember { mutableFloatStateOf(100f) }
  var isNitroActive by remember { mutableStateOf(false) }
  var isDrifting by remember { mutableStateOf(false) }

  // Weapons & Combat
  var currentWeapon by remember { mutableStateOf(GtaWeapon.BB_GUN) }
  var lastShotTime by remember { mutableLongStateOf(0L) }

  // Radio Station
  val radioStations = listOf("إذاعة صنعاء إف إم 📻", "هجولة وزوامل الحارة 🎶", "لاسلكي الشرطة الكوميدي 🎙️", "إيقاف الراديو 🔇")
  var currentRadioIndex by remember { mutableIntStateOf(0) }
  var radioToastMessage by remember { mutableStateOf("") }

  // Progress & Stats
  var score by remember { mutableIntStateOf(0) }
  var coinsEarned by remember { mutableIntStateOf(0) }
  var distanceTraveled by remember { mutableFloatStateOf(0f) }
  var carsHijackedCount by remember { mutableIntStateOf(0) }
  var copsEvadedCount by remember { mutableIntStateOf(0) }

  // Pay 'n' Spray Garage (in-game mechanic)
  var sprayShopNotification by remember { mutableStateOf("") }

  // Entities
  val traffic = remember { mutableStateListOf<GtaTrafficCar>() }
  val pedestrians = remember { mutableStateListOf<GtaPedestrian>() }
  val projectiles = remember { mutableStateListOf<GtaProjectile>() }
  val roadblocks = remember { mutableStateListOf<GtaRoadblock>() }
  val pickups = remember { mutableStateListOf<GtaMoneyPickup>() }
  val random = remember { Random() }

  // Nearby car for hijacking
  val nearbyCarToSteal by remember {
    derivedStateOf {
      if (isDriving) null
      else traffic.find { !it.isDestroyed && abs(it.x - playerX) < 0.16f && abs(it.y - playerY) < 0.16f }
    }
  }

  // Radio Switcher
  fun cycleRadio() {
    currentRadioIndex = (currentRadioIndex + 1) % radioStations.size
    radioToastMessage = radioStations[currentRadioIndex]
    GameSoundEffects.playRadioBeep()
  }

  // Main Game Loop
  LaunchedEffect(isPlaying, isGameOver, isMissionSuccess) {
    if (isPlaying && !isGameOver && !isMissionSuccess) {
      var spawnTick = 0
      var idCounter = 100L

      while (isActive && isPlaying && !isGameOver && !isMissionSuccess) {
        val baseSpeed = if (isDriving) {
          if (isNitroActive && nitroLevel > 0f) currentVehicle.maxSpeed * 1.5f else currentVehicle.maxSpeed
        } else {
          0.008f // On foot running speed
        }

        distanceTraveled += baseSpeed * 110f
        score = (distanceTraveled * 10).toInt() + (coinsEarned * 30) + (carsHijackedCount * 150)

        // Check Mission Completion
        if (currentMission != GtaMission.FREE_ROAM && distanceTraveled >= currentMission.targetDistance) {
          isMissionSuccess = true
          isPlaying = false
          coinsEarned += currentMission.rewardCoins
          repository.recordVehicleScore(score, coinsEarned)
          GameSoundEffects.playCoin()
          break
        }

        // Nitro Drain / Recharge
        if (isNitroActive && nitroLevel > 0f) {
          nitroLevel = (nitroLevel - 0.9f).coerceAtLeast(0f)
          if (nitroLevel <= 0f) isNitroActive = false
        } else if (!isNitroActive && nitroLevel < 100f) {
          nitroLevel = (nitroLevel + 0.2f).coerceAtMost(100f)
        }

        // Wanted Stars Cooldown (Natural decay when evading)
        wantedTimer++
        if (wantedTimer >= 500 && wantedStars > 0) {
          wantedTimer = 0
          wantedStars--
          copsEvadedCount++
          GameSoundEffects.playPoliceWhistle()
        }

        // Spawn Traffic & Police
        spawnTick++
        if (spawnTick >= 45) {
          spawnTick = 0
          val spawnPolice = wantedStars > 0 && random.nextInt(10) < (wantedStars * 2)
          val vType = if (spawnPolice) {
            GtaVehicleType.POLICE_CRUISER
          } else {
            val roll = random.nextInt(100)
            when {
              roll < 30 -> GtaVehicleType.TAXI
              roll < 55 -> GtaVehicleType.DABAB
              roll < 75 -> GtaVehicleType.SHAS_TOYOTA
              roll < 90 -> GtaVehicleType.MOTORBIKE
              else -> GtaVehicleType.HEAVY_TRUCK
            }
          }

          traffic.add(
            GtaTrafficCar(
              id = idCounter++,
              x = 0.20f + (random.nextFloat() * 0.60f),
              y = -0.15f,
              type = vType,
              speed = if (vType.isPolice) 0.015f + (wantedStars * 0.002f) else 0.006f + (random.nextFloat() * 0.004f),
              health = vType.armor
            )
          )

          // Spawn Pedestrian
          if (random.nextInt(3) == 0) {
            val pedNames = listOf("بائع قات", "طفل الحارة", "مواطن صنعاني", "عاقل الحارة")
            val pedEmojis = listOf("🚶‍♂️", "🏃‍♂️", "👳‍♂️", "🧒")
            val pIdx = random.nextInt(pedNames.size)
            pedestrians.add(
              GtaPedestrian(
                id = idCounter++,
                x = if (random.nextBoolean()) 0.08f else 0.92f,
                y = -0.1f,
                nameAr = pedNames[pIdx],
                emoji = pedEmojis[pIdx]
              )
            )
          }

          // Spawn Money Pickup
          if (random.nextInt(5) == 0) {
            pickups.add(
              GtaMoneyPickup(
                x = 0.25f + (random.nextFloat() * 0.5f),
                y = -0.1f,
                value = 25 + random.nextInt(50)
              )
            )
          }

          // Spawn Roadblock at 3+ Stars
          if (wantedStars >= 3 && roadblocks.size < 2 && random.nextInt(6) == 0) {
            roadblocks.add(
              GtaRoadblock(
                x = 0.5f,
                y = -0.2f,
                title = "نقطة تفتيش أمنية 🚧"
              )
            )
          }
        }

        // Update Traffic
        val tIter = traffic.iterator()
        while (tIter.hasNext()) {
          val car = tIter.next()
          car.y += (baseSpeed - car.speed)

          // Police Cruiser AI tracking player
          if (car.type.isPolice && wantedStars > 0 && !car.isDestroyed) {
            if (car.x < playerX - 0.02f) car.x += 0.003f
            else if (car.x > playerX + 0.02f) car.x -= 0.003f
          }

          // Collision with Player
          if (!car.isDestroyed && abs(car.x - playerX) < 0.12f && abs(car.y - playerY) < 0.12f) {
            if (isDriving) {
              // Car vs Car Ramming
              car.health -= 40f
              vehicleHealth -= 20f
              GameSoundEffects.playCarCrash()

              if (car.health <= 0f) {
                car.isDestroyed = true
                score += 100
                if (car.type.isPolice) {
                  wantedStars = (wantedStars + 1).coerceAtMost(5)
                  coinsEarned += 60
                  GameSoundEffects.playFirework()
                }
              }

              if (vehicleHealth <= 0f) {
                // Vehicle wrecked! Forced on foot or Game Over
                isDriving = false
                playerOnFootHealth -= 35f
                GameSoundEffects.playCarCrash()
                if (playerOnFootHealth <= 0f) {
                  isGameOver = true
                  isPlaying = false
                  repository.recordVehicleScore(score, coinsEarned)
                }
              }
            } else {
              // Pedestrian Player hit by car!
              playerOnFootHealth -= 30f
              GameSoundEffects.playPunch()
              car.health -= 15f
              if (playerOnFootHealth <= 0f) {
                isGameOver = true
                isPlaying = false
                repository.recordVehicleScore(score, coinsEarned)
              }
            }
          }

          // Projectile vs Car Collision
          val pIter = projectiles.iterator()
          while (pIter.hasNext()) {
            val proj = pIter.next()
            if (!car.isDestroyed && abs(proj.x - car.x) < 0.10f && abs(proj.y - car.y) < 0.09f) {
              car.health -= proj.weapon.damage
              pIter.remove()
              GameSoundEffects.playFirework()

              if (car.health <= 0f) {
                car.isDestroyed = true
                score += 120
                if (car.type.isPolice) {
                  wantedStars = (wantedStars + 1).coerceAtMost(5)
                  coinsEarned += 80
                }
              }
              break
            }
          }

          if (car.y > 1.25f || car.y < -0.4f) {
            tIter.remove()
          }
        }

        // Update Pickups
        val pickIter = pickups.iterator()
        while (pickIter.hasNext()) {
          val p = pickIter.next()
          p.y += baseSpeed
          if (abs(p.x - playerX) < 0.12f && abs(p.y - playerY) < 0.12f) {
            coinsEarned += p.value
            GameSoundEffects.playCoin()
            pickIter.remove()
          } else if (p.y > 1.2f) {
            pickIter.remove()
          }
        }

        // Update Roadblocks
        val rbIter = roadblocks.iterator()
        while (rbIter.hasNext()) {
          val rb = rbIter.next()
          rb.y += baseSpeed
          if (abs(rb.x - playerX) < 0.20f && abs(rb.y - playerY) < 0.08f) {
            if (isDriving) {
              vehicleHealth -= 35f
              GameSoundEffects.playCarCrash()
            } else {
              playerOnFootHealth -= 20f
              GameSoundEffects.playPunch()
            }
            rbIter.remove()
          } else if (rb.y > 1.2f) {
            rbIter.remove()
          }
        }

        // Update Pedestrians
        val pedIter = pedestrians.iterator()
        while (pedIter.hasNext()) {
          val ped = pedIter.next()
          ped.y += baseSpeed
          if (ped.y > 1.2f) {
            pedIter.remove()
          }
        }

        // Update Projectiles
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
          val proj = projIter.next()
          proj.x += proj.vx
          proj.y += proj.vy
          if (proj.y < -0.2f || proj.y > 1.2f || proj.x < 0f || proj.x > 1f) {
            projIter.remove()
          }
        }

        delay(16)
      }
    }
  }

  // Carjack / Hijack function
  fun hijackTargetCar(car: GtaTrafficCar) {
    car.isStolen = true
    car.isDestroyed = true
    currentVehicle = car.type
    vehicleHealth = car.type.armor
    isDriving = true
    playerX = car.x
    playerY = 0.75f
    carsHijackedCount++
    wantedStars = (wantedStars + 1).coerceAtMost(5)
    GameSoundEffects.playCarEnter()
    GameSoundEffects.playCarHorn()
  }

  // Fire current weapon / drive-by shoot
  fun fireWeapon() {
    val now = System.currentTimeMillis()
    if (now - lastShotTime < currentWeapon.fireRateMs) return
    lastShotTime = now

    projectiles.add(
      GtaProjectile(
        x = playerX,
        y = playerY - 0.06f,
        vx = if (isDrifting) (random.nextFloat() - 0.5f) * 0.02f else 0f,
        vy = -0.04f,
        weapon = currentWeapon
      )
    )

    when (currentWeapon) {
      GtaWeapon.PUNCH -> GameSoundEffects.playPunch()
      GtaWeapon.SLINGSHOT -> GameSoundEffects.playJump()
      GtaWeapon.BB_GUN -> GameSoundEffects.playWalkieTalkie()
      GtaWeapon.FIREWORKS_RPG -> GameSoundEffects.playFirework()
      GtaWeapon.BANANA_TRAP -> GameSoundEffects.playGraffitiSpray()
    }
  }

  // Pay 'n' Spray Garage Visit (Respray & Lose Stars)
  fun enterSprayShop() {
    if (coinsEarned >= 50 || stats.totalCoins >= 50) {
      coinsEarned = (coinsEarned - 50).coerceAtLeast(0)
      wantedStars = 0
      vehicleHealth = currentVehicle.armor
      playerOnFootHealth = 100f
      sprayShopNotification = "🎨 تم رش ودهان السيارة! تم شطب نجوم الشرطة وإصلاح المركبة 100%!"
      GameSoundEffects.playGraffitiSpray()
      GameSoundEffects.playCoin()
    }
  }

  fun startNewGtaGame(mission: GtaMission) {
    currentMission = mission
    traffic.clear()
    pedestrians.clear()
    projectiles.clear()
    roadblocks.clear()
    pickups.clear()

    score = 0
    coinsEarned = 0
    distanceTraveled = 0f
    carsHijackedCount = 0
    copsEvadedCount = 0

    playerX = 0.5f
    playerY = 0.75f
    isDriving = true
    currentVehicle = GtaVehicleType.DABAB
    vehicleHealth = currentVehicle.armor
    playerOnFootHealth = 100f
    wantedStars = mission.targetStars.coerceAtLeast(1)
    wantedTimer = 0
    nitroLevel = 100f
    isNitroActive = false
    isDrifting = false
    sprayShopNotification = ""

    isGameOver = false
    isMissionSuccess = false
    isPlaying = true

    GameSoundEffects.playCarEnter()
    GameSoundEffects.playCarHorn()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    // Top Bar with GTA Sana'a Branding
    SanaaTopBar(
      title = "حرامي سيارات صنعاء (GTA)",
      subtitle = "عالم مفتوح • سرقة وهجولة في شوارع العاصمة",
      coins = stats.totalCoins + coinsEarned,
      soundEnabled = stats.soundEnabled,
      onSoundToggle = {
        repository.toggleSound()
        GameSoundEffects.isMuted = !stats.soundEnabled
      },
      onBackClick = onNavigateBack
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      // 1. GTA Sana'a Top-Down Canvas
      GtaSanaaCanvas(
        playerX = playerX,
        playerY = playerY,
        isDriving = isDriving,
        currentVehicle = currentVehicle,
        isNitro = isNitroActive,
        isDrifting = isDrifting,
        wantedStars = wantedStars,
        traffic = traffic,
        pedestrians = pedestrians,
        projectiles = projectiles,
        roadblocks = roadblocks,
        pickups = pickups,
        distance = distanceTraveled,
        onSteerDrag = { deltaX ->
          playerX = (playerX + deltaX).coerceIn(0.12f, 0.88f)
        }
      )

      // 2. GTA Style HUD Overlay (Wanted Stars, MiniMap, Weapons, Health)
      if (isPlaying && !isGameOver && !isMissionSuccess) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
        ) {
          // Top Row: Wanted Stars ⭐⭐⭐⭐⭐ & Radio Station HUD
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Wanted Stars Bar
            Surface(
              color = DarkSurface.copy(alpha = 0.85f),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (wantedStars > 0) PoliceRedLight else SanaaGold)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "مستوى الطلب:",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                for (i in 1..5) {
                  Text(
                    text = "★",
                    color = if (i <= wantedStars) PoliceRedLight else Color.Gray.copy(alpha = 0.4f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Radio Station Pill
            Surface(
              color = DarkSurface.copy(alpha = 0.85f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.clickable { cycleRadio() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = radioStations[currentRadioIndex],
                  color = TaxiYellow,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Second Row: Mini-Map Radar & Active Vehicle / Player Armor
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            // Mini-Map Radar (GTA Compass)
            GtaMiniMapRadar(
              playerX = playerX,
              traffic = traffic,
              roadblocks = roadblocks,
              wantedStars = wantedStars
            )

            // Health & Vehicle Bars + Cash
            Column(
              horizontalAlignment = Alignment.End,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface.copy(alpha = 0.85f))
                .padding(8.dp)
            ) {
              // Money Counter
              Text(
                text = "$ ${stats.totalCoins + coinsEarned} 🪙",
                color = GangNeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )

              Spacer(modifier = Modifier.height(4.dp))

              // Player or Vehicle Health
              if (isDriving) {
                Text(
                  text = "درع ${currentVehicle.titleAr.take(12)}: ${vehicleHealth.toInt()}%",
                  color = if (vehicleHealth > 40) SanaaQamariyaEmerald else PoliceRedLight,
                  fontSize = 10.sp
                )
                LinearProgressIndicator(
                  progress = { (vehicleHealth / currentVehicle.armor).coerceIn(0f, 1f) },
                  modifier = Modifier
                    .width(110.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = if (vehicleHealth > 40) SanaaQamariyaEmerald else PoliceRedLight,
                  trackColor = DarkSurfaceVariant
                )
              } else {
                Text(
                  text = "صحة مازن بالأقدام: ${playerOnFootHealth.toInt()}%",
                  color = SanaaGold,
                  fontSize = 10.sp
                )
                LinearProgressIndicator(
                  progress = { (playerOnFootHealth / 100f).coerceIn(0f, 1f) },
                  modifier = Modifier
                    .width(110.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = SanaaGold,
                  trackColor = DarkSurfaceVariant
                )
              }

              // Nitro / Sprint Bar
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isDriving) "نيترو التوربو: ${nitroLevel.toInt()}%" else "طاقة الركض: ${nitroLevel.toInt()}%",
                color = TaxiYellow,
                fontSize = 10.sp
              )
              LinearProgressIndicator(
                progress = { (nitroLevel / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .width(110.dp)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = TaxiYellow,
                trackColor = DarkSurfaceVariant
              )
            }
          }

          // Mission Status Banner if active
          if (currentMission != GtaMission.FREE_ROAM) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              color = DarkSurface.copy(alpha = 0.9f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, TaxiYellow)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "المهمة: ${currentMission.titleAr} (${(distanceTraveled).toInt()} / ${currentMission.targetDistance.toInt()}م)",
                  color = TaxiYellow,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Spray Shop Flash Notification
          AnimatedVisibility(visible = sprayShopNotification.isNotEmpty()) {
            Surface(
              color = GangNeonGreen.copy(alpha = 0.9f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.padding(top = 6.dp)
            ) {
              Text(
                text = sprayShopNotification,
                color = DarkBg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 3. Start Screen / Mode Selection / GTA Cover Art
      if (!isPlaying && !isGameOver && !isMissionSuccess) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.92f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.5.dp, TaxiYellow, RoundedCornerShape(20.dp))
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              // GTA Cover Art Banner
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(135.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.gta_sanaa_action),
                  contentDescription = "Grand Theft Auto Sana'a Cover Art",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(
                      Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkSurface.copy(alpha = 0.95f))
                      )
                    )
                )
                Surface(
                  color = TaxiYellow,
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.padding(10.dp)
                ) {
                  Text(
                    text = "🌟 GTA SANAA CITY",
                    color = DarkBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "🚗 حرامي سيارات صنعاء (Grand Theft Auto)",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TaxiYellow,
                    fontSize = 17.sp
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "تجول بحرية على الأقدام أو اسرق الدبابات والسيارات والشاص، قُد بسرعة وهجولة، وتفادَ نجوم الشرطة الـ 5 بورشة الدهان السريعة!",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                  )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs (Free Roam vs Missions)
                TabRow(
                  selectedTabIndex = selectedTab,
                  containerColor = DarkSurfaceVariant,
                  contentColor = TaxiYellow
                ) {
                  Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("عالم مفتوح وهجولة 🌆", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                  )
                  Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("مهام السطو الكبرى 💼", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                  // Free Roam Launch
                  Button(
                    onClick = { startNewGtaGame(GtaMission.FREE_ROAM) },
                    colors = ButtonDefaults.buttonColors(containerColor = TaxiYellow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(48.dp)
                      .testTag("btn_gta_free_roam")
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("انطلق بالعالم المفتوح وهجولة الشوارع 🏎️💨", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                } else {
                  // Missions List Selector
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                      GtaMission.HEIST_SPICE,
                      GtaMission.HEIST_RESCUE,
                      GtaMission.HEIST_RACE,
                      GtaMission.HEIST_BIG_SCORE
                    ).forEach { mission ->
                      Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable { startNewGtaGame(mission) }
                      ) {
                        Row(
                          modifier = Modifier.padding(10.dp),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Column(modifier = Modifier.weight(1f)) {
                            Text(mission.titleAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(mission.descAr, color = Color.LightGray, fontSize = 10.sp, maxLines = 1)
                          }
                          Surface(
                            color = SanaaGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                          ) {
                            Text("+${mission.rewardCoins} 🪙", color = SanaaGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 4. Mission Success / Victory Screen
      if (isMissionSuccess) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.94f))
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(2.dp, GangNeonGreen, RoundedCornerShape(20.dp))
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "🏆 إنجاز المهمة بنجاح!", color = GangNeonGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "«تمت العملية بنجاح والهروب من كل دوريات شرطة صنعاء!»",
                color = Color.White,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.height(14.dp))

              Text(text = "مكافأة المهمة: +${currentMission.rewardCoins} 🪙", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text(text = "السيارات المخفية والمخطوفة: $carsHijackedCount", color = Color.LightGray, fontSize = 12.sp)
              Text(text = "دوريات تم الإفلات منها: $copsEvadedCount", color = Color.LightGray, fontSize = 12.sp)

              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { isMissionSuccess = false; isPlaying = false },
                colors = ButtonDefaults.buttonColors(containerColor = GangNeonGreen),
                modifier = Modifier.fillMaxWidth().height(46.dp)
              ) {
                Text("متابعة اللعب 🎮", color = DarkBg, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // 5. Game Over (Busted or Wrecked)
      if (isGameOver) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.94f))
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(2.dp, PoliceRedLight, RoundedCornerShape(20.dp))
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "🚨 تم القبض عليك (BUSTED)!", color = PoliceRedLight, fontWeight = FontWeight.Bold, fontSize = 20.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "طوقتك دوريات الشرطة في جولة كنتاكي وتم حجز المركبة!",
                color = Color.White,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.height(14.dp))

              Text(text = "النقاط المحققة: $score  •  الغنائم: +$coinsEarned 🪙", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

              Spacer(modifier = Modifier.height(16.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = onNavigateBack,
                  modifier = Modifier.weight(1f).height(46.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("الرئيسية", color = Color.White)
                }
                Button(
                  onClick = { startNewGtaGame(currentMission) },
                  colors = ButtonDefaults.buttonColors(containerColor = TaxiYellow),
                  modifier = Modifier.weight(1f).height(46.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("إعادة المحاولة 🔄", color = DarkBg, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // 6. Interactive GTA Bottom Action Controls (Drive, Walk, Steal, Shoot, Horn, Spray)
    if (isPlaying && !isGameOver && !isMissionSuccess) {
      Surface(
        color = DarkSurface,
        tonalElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
        ) {
          // Row 1: Hijack Prompt / Exit Car / Pay 'n' Spray / Horn
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Carjack or Exit Button
            if (!isDriving) {
              val targetCar = nearbyCarToSteal
              if (targetCar != null) {
                Button(
                  onClick = { hijackTargetCar(targetCar) },
                  colors = ButtonDefaults.buttonColors(containerColor = TaxiYellow),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.testTag("btn_gta_carjack")
                ) {
                  Text("🚖 خطف ${targetCar.type.titleAr.take(10)}!", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              } else {
                Surface(
                  color = DarkSurfaceVariant,
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text(
                    text = "اقترب من أي سيارة لخطفها 🚶‍♂️",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                  )
                }
              }
            } else {
              // Exit Car
              OutlinedButton(
                onClick = {
                  isDriving = false
                  GameSoundEffects.playCarEnter()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
              ) {
                Text("🚪 ترجل من السيارة", fontSize = 11.sp)
              }
            }

            // Pay 'n' Spray Quick Button & Horn
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Horn Button
              if (isDriving) {
                Button(
                  onClick = { GameSoundEffects.playCarHorn() },
                  colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                  shape = CircleShape,
                  modifier = Modifier.size(42.dp)
                ) {
                  Text("📢", fontSize = 14.sp)
                }
              }

              // Pay 'n' Spray Button
              Button(
                onClick = { enterSprayShop() },
                colors = ButtonDefaults.buttonColors(containerColor = SanaaClay),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_gta_spray_shop")
              ) {
                Text("🎨 ورشة الدهان (50🪙)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Row 2: Steering Directional Buttons, Attack & Nitro
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Directional Steering
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  playerX = (playerX - 0.12f).coerceAtLeast(0.12f)
                  GameSoundEffects.playJump()
                },
                modifier = Modifier.size(50.dp).testTag("btn_gta_steer_left"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
              ) {
                Text("◀", fontSize = 18.sp, color = Color.White)
              }

              Button(
                onClick = {
                  playerX = (playerX + 0.12f).coerceAtMost(0.88f)
                  GameSoundEffects.playJump()
                },
                modifier = Modifier.size(50.dp).testTag("btn_gta_steer_right"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
              ) {
                Text("▶", fontSize = 18.sp, color = Color.White)
              }
            }

            // Weapon Cycle Selector Pill
            Surface(
              color = DarkSurfaceVariant,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.clickable {
                val weapons = GtaWeapon.values()
                val nextIdx = (weapons.indexOf(currentWeapon) + 1) % weapons.size
                currentWeapon = weapons[nextIdx]
                GameSoundEffects.playJump()
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(currentWeapon.iconEmoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(currentWeapon.titleAr, color = currentWeapon.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Action Buttons: Shoot / Punch & Nitro / Drift
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Shoot Button
              Button(
                onClick = { fireWeapon() },
                colors = ButtonDefaults.buttonColors(containerColor = GangShawlRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(50.dp).testTag("btn_gta_fire_weapon")
              ) {
                Text(if (isDriving) "إطلاق 🎯" else "هجوم 👊", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              // Turbo / Drift / Sprint Button
              Button(
                onClick = {
                  if (nitroLevel > 10f) {
                    isNitroActive = !isNitroActive
                    if (isNitroActive) {
                      GameSoundEffects.playNitroBoost()
                      if (isDriving) GameSoundEffects.playDriftScreech()
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isNitroActive) TaxiYellow else DarkSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(50.dp).testTag("btn_gta_nitro")
              ) {
                Text(
                  text = if (isDriving) "توربو ⚡" else "ركض 🏃‍♂️",
                  color = if (isNitroActive) DarkBg else Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun GtaMiniMapRadar(
  playerX: Float,
  traffic: List<GtaTrafficCar>,
  roadblocks: List<GtaRoadblock>,
  wantedStars: Int
) {
  Box(
    modifier = Modifier
      .size(72.dp)
      .clip(CircleShape)
      .background(DarkBg.copy(alpha = 0.85f))
      .border(1.5.dp, if (wantedStars > 0) PoliceRedLight else SanaaGold, CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f

      // Radar Grid concentric rings
      drawCircle(color = Color.DarkGray.copy(alpha = 0.5f), radius = w * 0.45f, center = Offset(cx, cy))
      drawCircle(color = Color.DarkGray.copy(alpha = 0.5f), radius = w * 0.25f, center = Offset(cx, cy))

      // Crosshairs
      drawLine(color = Color.DarkGray.copy(alpha = 0.4f), start = Offset(0f, cy), end = Offset(w, cy), strokeWidth = 1f)
      drawLine(color = Color.DarkGray.copy(alpha = 0.4f), start = Offset(cx, 0f), end = Offset(cx, h), strokeWidth = 1f)

      // Traffic blips
      traffic.forEach { t ->
        if (!t.isDestroyed) {
          val bx = cx + (t.x - playerX) * w * 0.8f
          val by = cy + (t.y - 0.75f) * h * 0.8f
          val blipColor = if (t.type.isPolice) PoliceRedLight else Color.LightGray
          drawCircle(color = blipColor, radius = 3f, center = Offset(bx, by))
        }
      }

      // Player Blip (Center Arrow / Star)
      drawCircle(color = TaxiYellow, radius = 4.5f, center = Offset(cx, cy))
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
  allocatedWidth: Float = 250f,
  allocatedHeight: Float = 60f
) {
  val canvasW = size.width
  val canvasH = size.height
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
    // Gracefully handle constraints
  }
}

@Composable
fun GtaSanaaCanvas(
  playerX: Float,
  playerY: Float,
  isDriving: Boolean,
  currentVehicle: GtaVehicleType,
  isNitro: Boolean,
  isDrifting: Boolean,
  wantedStars: Int,
  traffic: List<GtaTrafficCar>,
  pedestrians: List<GtaPedestrian>,
  projectiles: List<GtaProjectile>,
  roadblocks: List<GtaRoadblock>,
  pickups: List<GtaMoneyPickup>,
  distance: Float,
  onSteerDrag: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures { _, dragAmount ->
          onSteerDrag(dragAmount.x / 350f)
        }
      }
  ) {
    val width = size.width
    val height = size.height

    // 1. Asphalt Sana'a Grand Highway & Intersections
    drawRect(color = Color(0xFF1B2028), size = Size(width, height))

    val roadMargin = width * 0.12f

    // Sidewalks on Left & Right
    drawRect(color = SanaaMudWarm, topLeft = Offset(0f, 0f), size = Size(roadMargin, height))
    drawRect(color = SanaaMudWarm, topLeft = Offset(width - roadMargin, 0f), size = Size(roadMargin, height))

    // Road Dashes
    val dashOffset = (distance * 1.6f) % 60f
    for (lane in 1..2) {
      val lx = roadMargin + (width - 2 * roadMargin) * (lane / 3f)
      drawLine(
        color = TaxiYellow.copy(alpha = 0.75f),
        start = Offset(lx, 0f),
        end = Offset(lx, height),
        strokeWidth = 3f,
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(30f, 25f), dashOffset)
      )
    }

    // Street Landmarks Text
    safeDrawText(
      textMeasurer = textMeasurer,
      text = "جولة كنتاكي 🚩",
      topLeft = Offset(4f, (distance * 0.6f) % height),
      style = TextStyle(color = TaxiYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    )
    safeDrawText(
      textMeasurer = textMeasurer,
      text = "ميدان السبعين 🇾🇪",
      topLeft = Offset(width - roadMargin + 2f, (distance * 0.8f + 250f) % height),
      style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    )
    safeDrawText(
      textMeasurer = textMeasurer,
      text = "🎨 ورشة الدهان Pay'n'Spray",
      topLeft = Offset(4f, (distance * 0.5f + 400f) % height),
      style = TextStyle(color = GangNeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    )

    // 2. Draw Pickups (Cash & Repair)
    pickups.forEach { p ->
      val px = p.x * width
      val py = p.y * height
      drawCircle(color = TaxiYellow, radius = 12f, center = Offset(px, py))
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "💰",
        topLeft = Offset(px - 9f, py - 10f),
        style = TextStyle(fontSize = 12.sp),
        allocatedWidth = 40f,
        allocatedHeight = 40f
      )
    }

    // 3. Draw Roadblocks
    roadblocks.forEach { rb ->
      val rbx = rb.x * width
      val rby = rb.y * height
      drawRect(color = PoliceRedLight, topLeft = Offset(rbx - 45f, rby - 12f), size = Size(90f, 24f))
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "حاجز أمني 🚧",
        topLeft = Offset(rbx - 36f, rby - 10f),
        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold),
        allocatedWidth = 100f,
        allocatedHeight = 40f
      )
    }

    // 4. Draw Pedestrians
    pedestrians.forEach { ped ->
      val px = ped.x * width
      val py = ped.y * height
      safeDrawText(
        textMeasurer = textMeasurer,
        text = ped.emoji,
        topLeft = Offset(px - 8f, py - 8f),
        style = TextStyle(fontSize = 16.sp),
        allocatedWidth = 40f,
        allocatedHeight = 40f
      )
    }

    // 5. Draw Projectiles
    projectiles.forEach { proj ->
      drawCircle(
        color = proj.weapon.color,
        radius = if (proj.weapon == GtaWeapon.FIREWORKS_RPG) 8f else 5f,
        center = Offset(proj.x * width, proj.y * height)
      )
    }

    // 6. Draw Traffic Cars
    traffic.forEach { car ->
      if (!car.isDestroyed) {
        val tx = car.x * width
        val ty = car.y * height

        // Car Shadow
        drawOval(color = Color.Black.copy(alpha = 0.4f), topLeft = Offset(tx - 24f, ty - 32f), size = Size(48f, 74f))

        // Car Body
        drawRoundRect(
          color = car.type.bodyColor,
          topLeft = Offset(tx - 22f, ty - 36f),
          size = Size(44f, 72f),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Police Siren Lights
        if (car.type.isPolice) {
          drawCircle(color = PoliceRedLight, radius = 5f, center = Offset(tx - 9f, ty - 8f))
          drawCircle(color = PoliceAccent, radius = 5f, center = Offset(tx + 9f, ty - 8f))
        }

        safeDrawText(
          textMeasurer = textMeasurer,
          text = car.type.iconEmoji,
          topLeft = Offset(tx - 10f, ty - 14f),
          style = TextStyle(fontSize = 16.sp),
          allocatedWidth = 40f,
          allocatedHeight = 40f
        )
      }
    }

    // 7. Draw Player (Driving Vehicle or On Foot)
    val px = playerX * width
    val py = playerY * height

    if (isDriving) {
      // Nitro Exhaust
      if (isNitro) {
        drawOval(
          brush = Brush.verticalGradient(listOf(TaxiYellow, GangShawlRed, Color.Transparent)),
          topLeft = Offset(px - 14f, py + 38f),
          size = Size(28f, 45f)
        )
      }

      // Player Vehicle Shadow
      drawOval(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(px - 26f, py - 32f),
        size = Size(52f, 80f)
      )

      // Player Vehicle Body
      drawRoundRect(
        color = currentVehicle.bodyColor,
        topLeft = Offset(px - 24f, py - 40f),
        size = Size(48f, 80f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
      )

      // Windshield
      drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(px - 18f, py - 34f),
        size = Size(36f, 20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
      )

      // Vehicle Icon
      safeDrawText(
        textMeasurer = textMeasurer,
        text = currentVehicle.iconEmoji,
        topLeft = Offset(px - 11f, py - 12f),
        style = TextStyle(fontSize = 18.sp),
        allocatedWidth = 40f,
        allocatedHeight = 40f
      )

      // Mazen driving badge
      drawCircle(color = GangShawlRed, radius = 8f, center = Offset(px + 12f, py + 16f))
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "👑",
        topLeft = Offset(px + 6f, py + 8f),
        style = TextStyle(fontSize = 9.sp),
        allocatedWidth = 25f,
        allocatedHeight = 25f
      )
    } else {
      // Player On Foot (Mazen Walking / Running)
      drawOval(color = Color.Black.copy(alpha = 0.4f), topLeft = Offset(px - 14f, py - 6f), size = Size(28f, 16f))
      drawCircle(color = GangShawlRed, radius = 14f, center = Offset(px, py))
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "👑",
        topLeft = Offset(px - 10f, py - 14f),
        style = TextStyle(fontSize = 14.sp),
        allocatedWidth = 30f,
        allocatedHeight = 30f
      )
    }
  }
}
