package com.example.ui.beirut

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
import com.example.sound.GameSoundEffects
import com.example.ui.components.SanaaTopBar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.*

// ----------------------------------------------------
// 3D GTA Beirut Models & Enums
// ----------------------------------------------------

enum class BeirutZone(val titleAr: String, val iconEmoji: String, val ambientColor: Color) {
  RAOUCHE_CORNICHE("كورنيش الروشة والمنارة 🌊", "🏖️", Color(0xFF1E88E5)),
  HAMRA_STREET("شارع الحمرا التجاري 🏬", "🛍️", Color(0xFFD81B60)),
  DOWNTOWN_SOLIDERE("وسط بيروت وساحة النجمة 🏛️", "🏰", Color(0xFFFFB300)),
  MAR_MIKHAEL("مار مخايل والجميزة 🎨", "🍷", Color(0xFF8E24AA)),
  BEIRUT_PORT("مرفأ بيروت وجسر شارل حلو 🚢", "🏗️", Color(0xFF546E7A))
}

enum class BeirutVehicleType(
  val titleAr: String,
  val nameShort: String,
  val iconEmoji: String,
  val maxSpeed: Float,
  val handling: Float,
  val armor: Float,
  val baseColor: Color,
  val isPolice: Boolean = false,
  val isSupercar: Boolean = false
) {
  MERCEDES_SERVICE("سرفيس مرسيدس 230E (أحمر)", "مرسيدس سرفيس", "🚕", 0.024f, 0.05f, 150f, Color(0xFFD32F2F)),
  VAN_FOUR("فان رقم 4 السريع (كولا - سلم)", "فان 4", "🚐", 0.028f, 0.045f, 180f, Color(0xFF00897B)),
  SUPERCAR_SOLIDERE("سوبركار فيراري وسط البلد", "سوبركار", "🏎️", 0.036f, 0.07f, 100f, Color(0xFFFF1744), isSupercar = true),
  LEBANESE_PICKUP("بيك آب شاص الدندشلي 4x4", "بيك آب 4x4", "🛻", 0.023f, 0.04f, 220f, Color(0xFF8D6E63)),
  SCOOTER_TMAX("موتوسيكل T-MAX الديليفري", "موتوسيكل T-MAX", "🛵", 0.032f, 0.08f, 70f, Color(0xFFFFB300)),
  ISF_POLICE("دورية قوى الأمن الداخلي ISF", "دورية ISF", "🚓", 0.027f, 0.06f, 190f, Color(0xFF0D47A1), isPolice = true)
}

enum class BeirutWeapon(
  val titleAr: String,
  val iconEmoji: String,
  val damage: Float,
  val fireRateMs: Long,
  val color: Color
) {
  FISTS("عراك بالأيدي واللكمات", "👊", 15f, 250, Color.White),
  DABKE_BATON("عصا خيزران / قضيب حديدي", "🪓", 30f, 350, Color(0xFFBCAAA4)),
  TASER("صاعق قوى الأمن الكهربائي", "⚡", 40f, 400, Color(0xFF00E5FF)),
  PISTOL_9MM("مسدس 9 ملم سريع", "🔫", 50f, 200, TaxiYellow),
  FIREWORKS_RPG("قاذف ألعاب نارية متفجر", "🎆", 110f, 800, GangShawlRed)
}

enum class BeirutCameraMode(val titleAr: String, val iconEmoji: String) {
  CHASE_3D("منظور ثلاثي الأبعاد (خلفي)", "🎥"),
  HOOD_3D("منظور الكبوت والأمام", "🚘"),
  HELI_3D("كاميرا جوية مائلة", "🚁")
}

enum class BeirutMission(
  val id: Int,
  val titleAr: String,
  val descAr: String,
  val targetDistance: Float,
  val rewardCoins: Int,
  val targetStars: Int,
  val zone: BeirutZone
) {
  FREE_ROAM(0, "هجولة وتجول حر في بيروت", "استكشف الكورنيش، اسرق أي مرسيدس أو فان 4، هجول وافلت من دوريات قوى الأمن!", 9999f, 80, 0, BeirutZone.RAOUCHE_CORNICHE),
  PORT_HEIST(1, "📦 عملية تهريب حاوية مرفأ بيروت", "اسرق شاحنة البضائع من المرفأ وافلت من حاجز قوى الأمن إلى مار مخايل.", 450f, 400, 2, BeirutZone.BEIRUT_PORT),
  RAOUCHE_ESCAPE(2, "🌊 الهروب الكبير على كورنيش الروشة", "طاردتك 3 دوريات قوى أمن.. فجر الحواجز وتفادَ السقوط في البحر!", 600f, 550, 3, BeirutZone.RAOUCHE_CORNICHE),
  SOLIDERE_GP(3, "🏎️ سباق سوبركارز وسط بيروت وسوليدير", "قد السوبركار بأقصى سرعة بين أبراج ساحة النجمة وكن الأول في خط النهاية!", 800f, 700, 1, BeirutZone.DOWNTOWN_SOLIDERE),
  HAMRA_BANK_JOB(4, "🏦 السطو الكبير على مصرف شارع الحمرا (5 نجوم)", "اقتحم البنك، خذ الحقيبة واهرب في حالة طوارئ قصوى مع إغلاق الطرق!", 1100f, 1200, 5, BeirutZone.HAMRA_STREET),
  VAN_FOUR_MADNESS(5, "🚐 تحدي فان 4 السريع عبر جسر الكولا", "قد الفان بأقصى جنون، قم بـ 10 تفحيطات واجمع 15 راكباً دون صدم الدوريات!", 700f, 600, 2, BeirutZone.HAMRA_STREET)
}

// 3D World Traffic Entity
data class Beirut3DCar(
  val id: Long,
  var worldX: Float, // -1.2 to 1.2 relative to road center
  var worldZ: Float, // Distance ahead (10m to 300m)
  val type: BeirutVehicleType,
  var speed: Float,
  var health: Float,
  var customColor: Color,
  var isDestroyed: Boolean = false,
  var isStolen: Boolean = false,
  var lane: Int = 0 // -1: Left, 0: Mid, 1: Right
)

// 3D Pedestrian Entity
data class Beirut3DPedestrian(
  val id: Long,
  var worldX: Float,
  var worldZ: Float,
  val nameAr: String,
  val emoji: String,
  var isFleeing: Boolean = false
)

// 3D Projectile / Bullet
data class Beirut3DProjectile(
  var worldX: Float,
  var worldY: Float,
  var worldZ: Float,
  val vx: Float,
  val vy: Float,
  val vz: Float,
  val weapon: BeirutWeapon
)

// 3D Roadblock / Police Barricade
data class Beirut3DRoadblock(
  var worldX: Float,
  var worldZ: Float,
  val title: String,
  var hasSpikes: Boolean = false
)

// 3D Coin & Cash Pickup
data class Beirut3DPickup(
  var worldX: Float,
  var worldZ: Float,
  val value: Int
)

// 3D Landmark / Building / Scenery
data class Beirut3DScenery(
  val id: Long,
  val side: Int, // -1 Left, 1 Right
  val distanceZ: Float,
  val typeName: String, // "PALM", "BUILDING", "RAOUCHE_ROCK", "HERITAGE_BALCONY", "STREET_LIGHT"
  val heightScale: Float,
  val color: Color
)

@Composable
fun GtaBeirut3DScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()

  // Mode Selection
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Free Roam, 1: Heist Missions
  var currentMission by remember { mutableStateOf(BeirutMission.FREE_ROAM) }
  var currentZone by remember { mutableStateOf(BeirutZone.RAOUCHE_CORNICHE) }

  // Game Engine State
  var isPlaying by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var isMissionSuccess by remember { mutableStateOf(false) }

  // 3D Player State
  var playerWorldX by remember { mutableFloatStateOf(0f) } // Road center is 0f (-1.0 to 1.0)
  var playerWorldY by remember { mutableFloatStateOf(0f) } // Jump height
  var playerHeadingAngle by remember { mutableFloatStateOf(0f) } // Steering tilt
  var isDriving by remember { mutableStateOf(true) }
  var currentVehicle by remember { mutableStateOf(BeirutVehicleType.MERCEDES_SERVICE) }
  var vehicleHealth by remember { mutableFloatStateOf(150f) }
  var playerOnFootHealth by remember { mutableFloatStateOf(100f) }
  var vehicleColor by remember { mutableStateOf(Color(0xFFD32F2F)) }

  // Camera Mode
  var cameraMode by remember { mutableStateOf(BeirutCameraMode.CHASE_3D) }

  // Wanted Stars (0 to 5) - ISF Police
  var wantedStars by remember { mutableIntStateOf(1) }
  var wantedCooldownTimer by remember { mutableIntStateOf(0) }

  // Nitro & Turbo / Drift
  var nitroLevel by remember { mutableFloatStateOf(100f) }
  var isNitroActive by remember { mutableStateOf(false) }
  var isDrifting by remember { mutableStateOf(false) }

  // Weapons & Combat
  var currentWeapon by remember { mutableStateOf(BeirutWeapon.PISTOL_9MM) }
  var lastShotTime by remember { mutableLongStateOf(0L) }

  // Beirut Radio Stations
  val radioStations = listOf(
    "راديو صوت بيروت 99.0 إف إم 📻",
    "إذاعة الدبكة والهجولة اللبنانية 🎶",
    "لاسلكي عمليات قوى الأمن ISF 🎙️",
    "إيقاف الراديو 🔇"
  )
  var currentRadioIndex by remember { mutableIntStateOf(0) }
  var radioToastMessage by remember { mutableStateOf("") }

  // Pay 'n' Spray Garage (كراج أبو علي للتجليس)
  var garageNotification by remember { mutableStateOf("") }

  // Progress & Stats
  var score by remember { mutableIntStateOf(0) }
  var coinsEarned by remember { mutableIntStateOf(0) }
  var distanceTraveled by remember { mutableFloatStateOf(0f) }
  var carsHijackedCount by remember { mutableIntStateOf(0) }
  var copsEvadedCount by remember { mutableIntStateOf(0) }
  var driftPoints by remember { mutableIntStateOf(0) }

  // 3D Entities
  val traffic = remember { mutableStateListOf<Beirut3DCar>() }
  val pedestrians = remember { mutableStateListOf<Beirut3DPedestrian>() }
  val projectiles = remember { mutableStateListOf<Beirut3DProjectile>() }
  val roadblocks = remember { mutableStateListOf<Beirut3DRoadblock>() }
  val pickups = remember { mutableStateListOf<Beirut3DPickup>() }
  val sceneries = remember { mutableStateListOf<Beirut3DScenery>() }
  val random = remember { Random() }

  // Nearby car for hijacking
  val nearbyCarToSteal by remember {
    derivedStateOf {
      if (isDriving) null
      else traffic.find { !it.isDestroyed && abs(it.worldX - playerWorldX) < 0.35f && it.worldZ < 25f && it.worldZ > -10f }
    }
  }

  // Switch Radio Station
  fun cycleRadio() {
    currentRadioIndex = (currentRadioIndex + 1) % radioStations.size
    radioToastMessage = radioStations[currentRadioIndex]
    GameSoundEffects.playRadioBeep()
  }

  // Switch Camera Mode
  fun cycleCamera() {
    val modes = BeirutCameraMode.values()
    val nextIdx = (modes.indexOf(cameraMode) + 1) % modes.size
    cameraMode = modes[nextIdx]
    GameSoundEffects.playJump()
  }

  // Main 3D Game Loop
  LaunchedEffect(isPlaying, isGameOver, isMissionSuccess) {
    if (isPlaying && !isGameOver && !isMissionSuccess) {
      var spawnTimer = 0
      var sceneryTimer = 0
      var idGen = 200L

      while (isActive && isPlaying && !isGameOver && !isMissionSuccess) {
        val playerSpeed = if (isDriving) {
          var spd = currentVehicle.maxSpeed
          if (isNitroActive && nitroLevel > 0f) spd *= 1.55f
          if (isDrifting) spd *= 0.85f
          spd
        } else {
          0.009f // On-foot running speed
        }

        distanceTraveled += playerSpeed * 120f
        score = (distanceTraveled * 12).toInt() + (coinsEarned * 35) + (carsHijackedCount * 200) + (driftPoints / 2)

        // Check Mission Completion
        if (currentMission != BeirutMission.FREE_ROAM && distanceTraveled >= currentMission.targetDistance) {
          isMissionSuccess = true
          isPlaying = false
          coinsEarned += currentMission.rewardCoins
          repository.recordVehicleScore(score, coinsEarned)
          GameSoundEffects.playCoin()
          break
        }

        // Nitro Drain & Recharge
        if (isNitroActive && nitroLevel > 0f) {
          nitroLevel = (nitroLevel - 1.0f).coerceAtLeast(0f)
          if (nitroLevel <= 0f) isNitroActive = false
        } else if (!isNitroActive && nitroLevel < 100f) {
          nitroLevel = (nitroLevel + 0.25f).coerceAtMost(100f)
        }

        // Drifting Physics & Points
        if (isDrifting && isDriving) {
          driftPoints += 5
          playerHeadingAngle = if (playerWorldX > 0) 18f else -18f
        } else {
          playerHeadingAngle *= 0.85f
        }

        // Police Wanted Decay Timer
        wantedCooldownTimer++
        if (wantedCooldownTimer >= 480 && wantedStars > 0) {
          wantedCooldownTimer = 0
          wantedStars--
          copsEvadedCount++
          GameSoundEffects.playPoliceWhistle()
        }

        // Spawn 3D Scenery (Palms, Raouche Rocks, Beirut Skyscrapers)
        sceneryTimer++
        if (sceneryTimer >= 18) {
          sceneryTimer = 0
          val sceneryTypes = listOf("PALM", "BUILDING", "HERITAGE_BALCONY", "STREET_LIGHT", "RAOUCHE_ROCK")
          val side = if (random.nextBoolean()) -1 else 1
          val sType = sceneryTypes[random.nextInt(sceneryTypes.size)]
          sceneries.add(
            Beirut3DScenery(
              id = idGen++,
              side = side,
              distanceZ = 280f,
              typeName = sType,
              heightScale = 0.8f + random.nextFloat() * 1.4f,
              color = when (sType) {
                "PALM" -> Color(0xFF2E7D32)
                "RAOUCHE_ROCK" -> Color(0xFF8D6E63)
                "HERITAGE_BALCONY" -> Color(0xFFFFB300)
                else -> Color(0xFF455A64)
              }
            )
          )
        }

        // Spawn 3D Traffic & ISF Cruisers
        spawnTimer++
        if (spawnTimer >= 38) {
          spawnTimer = 0
          val spawnPolice = wantedStars > 0 && random.nextInt(10) < (wantedStars * 2)
          val vType = if (spawnPolice) {
            BeirutVehicleType.ISF_POLICE
          } else {
            val roll = random.nextInt(100)
            when {
              roll < 32 -> BeirutVehicleType.MERCEDES_SERVICE
              roll < 55 -> BeirutVehicleType.VAN_FOUR
              roll < 72 -> BeirutVehicleType.SUPERCAR_SOLIDERE
              roll < 88 -> BeirutVehicleType.SCOOTER_TMAX
              else -> BeirutVehicleType.LEBANESE_PICKUP
            }
          }

          val laneChoice = random.nextInt(3) - 1 // -1, 0, 1
          traffic.add(
            Beirut3DCar(
              id = idGen++,
              worldX = laneChoice * 0.65f + (random.nextFloat() - 0.5f) * 0.15f,
              worldZ = 260f,
              type = vType,
              speed = if (vType.isPolice) 0.018f + (wantedStars * 0.003f) else 0.008f + random.nextFloat() * 0.006f,
              health = vType.armor,
              customColor = vType.baseColor,
              lane = laneChoice
            )
          )

          // Spawn 3D Pedestrian on Beirut Sidewalk
          if (random.nextInt(3) == 0) {
            val pNames = listOf("شاب بيروتي", "بائع قهوة إكسبرس", "سائق سرفيس", "عابر سبيل")
            val pEmojis = listOf("🚶‍♂️", "☕", "🚕", "🏃‍♂️")
            val idx = random.nextInt(pNames.size)
            pedestrians.add(
              Beirut3DPedestrian(
                id = idGen++,
                worldX = if (random.nextBoolean()) -1.3f else 1.3f,
                worldZ = 240f,
                nameAr = pNames[idx],
                emoji = pEmojis[idx]
              )
            )
          }

          // Spawn 3D Money / Loot Pickup
          if (random.nextInt(4) == 0) {
            pickups.add(
              Beirut3DPickup(
                worldX = (random.nextFloat() - 0.5f) * 1.2f,
                worldZ = 250f,
                value = 30 + random.nextInt(70)
              )
            )
          }

          // Spawn 3D ISF Barricade at 3+ Stars
          if (wantedStars >= 3 && roadblocks.size < 2 && random.nextInt(5) == 0) {
            roadblocks.add(
              Beirut3DRoadblock(
                worldX = 0f,
                worldZ = 270f,
                title = "حاجز قوى الأمن الداخلي 🚧",
                hasSpikes = wantedStars >= 4
              )
            )
          }
        }

        // Update 3D Scenery (Move towards camera along Z)
        val sIter = sceneries.iterator()
        while (sIter.hasNext()) {
          val scen = sIter.next()
          val moveZ = playerSpeed * 2200f
          val updatedZ = scen.distanceZ - moveZ
          if (updatedZ < -15f) {
            sIter.remove()
          } else {
            // Replace with updated Z
            val idx = sceneries.indexOf(scen)
            if (idx != -1) {
              sceneries[idx] = scen.copy(distanceZ = updatedZ)
            }
          }
        }

        // Update 3D Traffic
        val tIter = traffic.iterator()
        while (tIter.hasNext()) {
          val car = tIter.next()
          val relSpeed = (playerSpeed - car.speed) * 2200f
          car.worldZ -= relSpeed

          // ISF Police Cruiser AI Tracking Player
          if (car.type.isPolice && wantedStars > 0 && !car.isDestroyed) {
            if (car.worldX < playerWorldX - 0.05f) car.worldX += 0.008f
            else if (car.worldX > playerWorldX + 0.05f) car.worldX -= 0.008f
          }

          // 3D Collision with Player
          if (!car.isDestroyed && abs(car.worldX - playerWorldX) < 0.40f && car.worldZ in -8f..15f) {
            if (isDriving) {
              // Car-on-Car Smash & Ramming
              car.health -= 50f
              vehicleHealth -= 25f
              GameSoundEffects.playCarCrash()

              if (car.health <= 0f) {
                car.isDestroyed = true
                score += 150
                if (car.type.isPolice) {
                  wantedStars = (wantedStars + 1).coerceAtMost(5)
                  coinsEarned += 80
                  GameSoundEffects.playFirework()
                }
              }

              if (vehicleHealth <= 0f) {
                // Vehicle wrecked!
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
              // Pedestrian Player struck by car!
              playerOnFootHealth -= 30f
              GameSoundEffects.playPunch()
              car.health -= 20f
              if (playerOnFootHealth <= 0f) {
                isGameOver = true
                isPlaying = false
                repository.recordVehicleScore(score, coinsEarned)
              }
            }
          }

          // 3D Projectile Collision with Car
          val pIter = projectiles.iterator()
          while (pIter.hasNext()) {
            val proj = pIter.next()
            if (!car.isDestroyed && abs(proj.worldX - car.worldX) < 0.35f && abs(proj.worldZ - car.worldZ) < 18f) {
              car.health -= proj.weapon.damage
              pIter.remove()
              GameSoundEffects.playFirework()

              if (car.health <= 0f) {
                car.isDestroyed = true
                score += 180
                if (car.type.isPolice) {
                  wantedStars = (wantedStars + 1).coerceAtMost(5)
                  coinsEarned += 100
                }
              }
              break
            }
          }

          if (car.worldZ < -25f || car.worldZ > 320f) {
            tIter.remove()
          }
        }

        // Update 3D Pickups
        val pickIter = pickups.iterator()
        while (pickIter.hasNext()) {
          val p = pickIter.next()
          p.worldZ -= (playerSpeed * 2200f)
          if (abs(p.worldX - playerWorldX) < 0.45f && p.worldZ in -8f..15f) {
            coinsEarned += p.value
            GameSoundEffects.playCoin()
            pickIter.remove()
          } else if (p.worldZ < -20f) {
            pickIter.remove()
          }
        }

        // Update 3D Roadblocks
        val rbIter = roadblocks.iterator()
        while (rbIter.hasNext()) {
          val rb = rbIter.next()
          rb.worldZ -= (playerSpeed * 2200f)
          if (abs(rb.worldX - playerWorldX) < 0.70f && rb.worldZ in -6f..12f) {
            if (isDriving) {
              vehicleHealth -= if (rb.hasSpikes) 60f else 35f
              GameSoundEffects.playCarCrash()
              if (rb.hasSpikes) GameSoundEffects.playDriftScreech()
            } else {
              playerOnFootHealth -= 25f
              GameSoundEffects.playPunch()
            }
            rbIter.remove()
          } else if (rb.worldZ < -20f) {
            rbIter.remove()
          }
        }

        // Update 3D Pedestrians
        val pedIter = pedestrians.iterator()
        while (pedIter.hasNext()) {
          val ped = pedIter.next()
          ped.worldZ -= (playerSpeed * 2200f)
          if (ped.worldZ < -20f) {
            pedIter.remove()
          }
        }

        // Update 3D Projectiles
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
          val proj = projIter.next()
          proj.worldX += proj.vx
          proj.worldY += proj.vy
          proj.worldZ += proj.vz
          if (proj.worldZ > 280f || proj.worldZ < -10f || abs(proj.worldX) > 3f) {
            projIter.remove()
          }
        }

        delay(16)
      }
    }
  }

  // Jack / Steal 3D Car
  fun stealTargetVehicle(car: Beirut3DCar) {
    car.isStolen = true
    car.isDestroyed = true
    currentVehicle = car.type
    vehicleHealth = car.type.armor
    vehicleColor = car.customColor
    isDriving = true
    playerWorldX = car.worldX
    carsHijackedCount++
    wantedStars = (wantedStars + 1).coerceAtMost(5)
    GameSoundEffects.playCarEnter()
    GameSoundEffects.playCarHorn()
  }

  // Fire Weapon in 3D
  fun shoot3DWeapon() {
    val now = System.currentTimeMillis()
    if (now - lastShotTime < currentWeapon.fireRateMs) return
    lastShotTime = now

    projectiles.add(
      Beirut3DProjectile(
        worldX = playerWorldX,
        worldY = 0.5f,
        worldZ = 5f,
        vx = (playerHeadingAngle / 18f) * 0.03f,
        vy = 0f,
        vz = 8f,
        weapon = currentWeapon
      )
    )

    when (currentWeapon) {
      BeirutWeapon.FISTS -> GameSoundEffects.playPunch()
      BeirutWeapon.DABKE_BATON -> GameSoundEffects.playPunch()
      BeirutWeapon.TASER -> GameSoundEffects.playWalkieTalkie()
      BeirutWeapon.PISTOL_9MM -> GameSoundEffects.playWalkieTalkie()
      BeirutWeapon.FIREWORKS_RPG -> GameSoundEffects.playFirework()
    }
  }

  // Pay 'n' Spray Garage Visit (كراج أبو علي للتجليس والرش)
  fun visitBeirutModShop() {
    if (coinsEarned >= 60 || stats.totalCoins >= 60) {
      coinsEarned = (coinsEarned - 60).coerceAtLeast(0)
      wantedStars = 0
      vehicleHealth = currentVehicle.armor
      playerOnFootHealth = 100f
      // Cycle to a fresh neon / shiny paint job
      val coolColors = listOf(Color(0xFF00E5FF), Color(0xFFFFD600), Color(0xFFFF1744), Color(0xFF00E676), Color(0xFFD500F9))
      vehicleColor = coolColors[random.nextInt(coolColors.size)]
      garageNotification = "🎨 كراج أبو علي: تم تجليس ورش السيارة بلون جديد، إصلاح الهيكل 100% وإلغاء نجوم قوى الأمن!"
      GameSoundEffects.playGraffitiSpray()
      GameSoundEffects.playCoin()
    }
  }

  fun startNewBeirut3DGame(mission: BeirutMission) {
    currentMission = mission
    currentZone = mission.zone
    traffic.clear()
    pedestrians.clear()
    projectiles.clear()
    roadblocks.clear()
    pickups.clear()
    sceneries.clear()

    score = 0
    coinsEarned = 0
    distanceTraveled = 0f
    carsHijackedCount = 0
    copsEvadedCount = 0
    driftPoints = 0

    playerWorldX = 0f
    playerWorldY = 0f
    playerHeadingAngle = 0f
    isDriving = true
    currentVehicle = when (mission) {
      BeirutMission.SOLIDERE_GP -> BeirutVehicleType.SUPERCAR_SOLIDERE
      BeirutMission.VAN_FOUR_MADNESS -> BeirutVehicleType.VAN_FOUR
      else -> BeirutVehicleType.MERCEDES_SERVICE
    }
    vehicleHealth = currentVehicle.armor
    vehicleColor = currentVehicle.baseColor
    playerOnFootHealth = 100f
    wantedStars = mission.targetStars.coerceAtLeast(if (mission == BeirutMission.FREE_ROAM) 0 else 1)
    wantedCooldownTimer = 0
    nitroLevel = 100f
    isNitroActive = false
    isDrifting = false
    garageNotification = ""

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
    // Top Bar with Beirut 3D Branding
    SanaaTopBar(
      title = "حرامي سيارات بيروت 3D (GTA Beirut)",
      subtitle = "${currentZone.titleAr} • مطاردات قوى الأمن الداخلي",
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
      // 1. True 3D Perspective Projection Engine Canvas
      Beirut3DWorldCanvas(
        playerWorldX = playerWorldX,
        playerWorldY = playerWorldY,
        playerHeading = playerHeadingAngle,
        isDriving = isDriving,
        currentVehicle = currentVehicle,
        vehicleColor = vehicleColor,
        isNitro = isNitroActive,
        isDrifting = isDrifting,
        cameraMode = cameraMode,
        wantedStars = wantedStars,
        traffic = traffic,
        pedestrians = pedestrians,
        projectiles = projectiles,
        roadblocks = roadblocks,
        pickups = pickups,
        sceneries = sceneries,
        distance = distanceTraveled,
        zone = currentZone,
        onSteerDrag = { deltaX ->
          playerWorldX = (playerWorldX + deltaX).coerceIn(-1.1f, 1.1f)
          playerHeadingAngle = (deltaX * 120f).coerceIn(-25f, 25f)
        }
      )

      // 2. 3D GTA Beirut HUD Overlay
      if (isPlaying && !isGameOver && !isMissionSuccess) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
        ) {
          // Top Row: Wanted Stars ⭐⭐⭐⭐⭐ & Radio Station & Camera Angle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // ISF Wanted Stars
            Surface(
              color = DarkSurface.copy(alpha = 0.88f),
              shape = RoundedCornerShape(12.dp),
              border = androidx.compose.foundation.BorderStroke(1.5.dp, if (wantedStars > 0) PoliceRedLight else SanaaGold)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "ملاحقة قوى الأمن:",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                for (i in 1..5) {
                  Text(
                    text = "★",
                    color = if (i <= wantedStars) PoliceRedLight else Color.Gray.copy(alpha = 0.35f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Radio & Camera Switchers
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              // Camera Switcher
              Surface(
                color = DarkSurface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { cycleCamera() }
              ) {
                Text(
                  text = "${cameraMode.iconEmoji} ${cameraMode.titleAr.take(7)}",
                  color = SanaaGold,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }

              // Radio Switcher
              Surface(
                color = DarkSurface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { cycleRadio() }
              ) {
                Text(
                  text = radioStations[currentRadioIndex].take(14),
                  color = TaxiYellow,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Second Row: Mini-Map Radar & Active Vehicle HUD
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            // 3D MiniMap Compass Radar
            BeirutMiniMapRadar(
              playerX = playerWorldX,
              traffic = traffic,
              roadblocks = roadblocks,
              wantedStars = wantedStars
            )

            // Health & Vehicle Armor + Cash
            Column(
              horizontalAlignment = Alignment.End,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface.copy(alpha = 0.88f))
                .padding(8.dp)
            ) {
              // Money Counter
              Text(
                text = "$ ${stats.totalCoins + coinsEarned} 🪙",
                color = GangNeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )

              if (driftPoints > 0) {
                Text(
                  text = "نقاط الهجولة: $driftPoints 💨",
                  color = TaxiYellow,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              // Player or Vehicle Health
              if (isDriving) {
                Text(
                  text = "درع ${currentVehicle.nameShort}: ${vehicleHealth.toInt()}%",
                  color = if (vehicleHealth > 40) SanaaQamariyaEmerald else PoliceRedLight,
                  fontSize = 10.sp
                )
                LinearProgressIndicator(
                  progress = { (vehicleHealth / currentVehicle.armor).coerceIn(0f, 1f) },
                  modifier = Modifier
                    .width(115.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = if (vehicleHealth > 40) SanaaQamariyaEmerald else PoliceRedLight,
                  trackColor = DarkSurfaceVariant
                )
              } else {
                Text(
                  text = "صحة جاد (ابن البلد): ${playerOnFootHealth.toInt()}%",
                  color = SanaaGold,
                  fontSize = 10.sp
                )
                LinearProgressIndicator(
                  progress = { (playerOnFootHealth / 100f).coerceIn(0f, 1f) },
                  modifier = Modifier
                    .width(115.dp)
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
                  .width(115.dp)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = TaxiYellow,
                trackColor = DarkSurfaceVariant
              )
            }
          }

          // Mission Status Banner if active
          if (currentMission != BeirutMission.FREE_ROAM) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              color = DarkSurface.copy(alpha = 0.92f),
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

          // Garage Notification Flash
          AnimatedVisibility(visible = garageNotification.isNotEmpty()) {
            Surface(
              color = GangNeonGreen.copy(alpha = 0.92f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.padding(top = 6.dp)
            ) {
              Text(
                text = garageNotification,
                color = DarkBg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 3. Start Screen / Mode Selection / GTA Beirut Cover Art
      if (!isPlaying && !isGameOver && !isMissionSuccess) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.94f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp))
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              // 3D GTA Beirut Cover Art Banner
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(145.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.gta_beirut_cover),
                  contentDescription = "Grand Theft Auto Beirut 3D Cover Art",
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
                  color = Color(0xFF00E5FF),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.padding(10.dp)
                ) {
                  Text(
                    text = "🌟 GTA BEIRUT 3D • عالم ثلاثي الأبعاد",
                    color = DarkBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }

              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "🇱🇧 حرامي سيارات بيروت 3D (Grand Theft Auto Beirut)",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    fontSize = 16.sp
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "لعبة أكشن 3D بالمنظور الثالث في مدينة بيروت! تجول بحرية بين صخرة الروشة، شارع الحمرا، ومرفأ بيروت، اسرق سيارات المرسيدس وفان 4، وتفادَ مطاردات قوى الأمن الداخلي!",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                  )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs (Free Roam 3D vs Heist Missions)
                TabRow(
                  selectedTabIndex = selectedTab,
                  containerColor = DarkSurfaceVariant,
                  contentColor = Color(0xFF00E5FF)
                ) {
                  Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("عالم مفتوح ثلاثي الأبعاد 🌊", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                  )
                  Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("مهام السطو في بيروت 💼", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                  // Zone Selector for Free Roam
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    listOf(BeirutZone.RAOUCHE_CORNICHE, BeirutZone.HAMRA_STREET, BeirutZone.DOWNTOWN_SOLIDERE).forEach { z ->
                      Surface(
                        color = if (currentZone == z) z.ambientColor else DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                          .weight(1f)
                          .clickable { currentZone = z }
                      ) {
                        Text(
                          text = z.titleAr.take(12),
                          color = Color.White,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(6.dp),
                          maxLines = 1
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // Launch Free Roam
                  Button(
                    onClick = { startNewBeirut3DGame(BeirutMission.FREE_ROAM) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(48.dp)
                      .testTag("btn_gta_beirut_free_roam")
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("انطلق بالمنظور الثلاثي الأبعاد في بيروت 🏎️💨", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  }
                } else {
                  // Heist Missions List
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                      BeirutMission.PORT_HEIST,
                      BeirutMission.RAOUCHE_ESCAPE,
                      BeirutMission.SOLIDERE_GP,
                      BeirutMission.HAMRA_BANK_JOB,
                      BeirutMission.VAN_FOUR_MADNESS
                    ).forEach { mission ->
                      Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                          .fillMaxWidth()
                          .clickable { startNewBeirut3DGame(mission) }
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
              Text(text = "🏆 إنجاز العملية في بيروت بنجاح!", color = GangNeonGreen, fontWeight = FontWeight.Bold, fontSize = 19.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "«تم الهروب من كافة دوريات قوى الأمن والوصول إلى المخبأ الآمن!»",
                color = Color.White,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.height(14.dp))

              Text(text = "مكافأة المهمة: +${currentMission.rewardCoins} 🪙", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text(text = "السيارات المخطوفة في بيروت: $carsHijackedCount", color = Color.LightGray, fontSize = 12.sp)
              Text(text = "نقاط الهجولة والتفحيط: $driftPoints", color = TaxiYellow, fontSize = 12.sp)

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

      // 5. Game Over (Busted or Wrecked in Beirut)
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
              Text(text = "🚨 تم توقيفك من قوى الأمن (BUSTED)!", color = PoliceRedLight, fontWeight = FontWeight.Bold, fontSize = 19.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "تمت محاصرتك على كورنيش الروشة وحجز المركبة!",
                color = Color.White,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.height(14.dp))

              Text(text = "النقاط: $score  •  الغنائم: +$coinsEarned 🪙", color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)

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
                  onClick = { startNewBeirut3DGame(currentMission) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
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

    // 6. Interactive GTA Beirut 3D Action Controls
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
          // Row 1: Carjack / Exit / Horn / Beirut Mod Shop
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Carjack Button or Walk Status
            if (!isDriving) {
              val targetCar = nearbyCarToSteal
              if (targetCar != null) {
                Button(
                  onClick = { stealTargetVehicle(targetCar) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.testTag("btn_gta_beirut_carjack")
                ) {
                  Text("🚖 خطف ${targetCar.type.nameShort}!", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

            // Horn & Mod Garage Button
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

              // Beirut Garage (كراج أبو علي)
              Button(
                onClick = { visitBeirutModShop() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_gta_beirut_garage")
              ) {
                Text("🎨 كراج أبو علي (60🪙)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Row 2: Directional Steering, Weapon Selector, Attack, Drift / Nitro
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Steering Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  playerWorldX = (playerWorldX - 0.22f).coerceAtLeast(-1.1f)
                  playerHeadingAngle = -22f
                  GameSoundEffects.playJump()
                },
                modifier = Modifier.size(50.dp).testTag("btn_gta_beirut_left"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
              ) {
                Text("◀", fontSize = 18.sp, color = Color.White)
              }

              Button(
                onClick = {
                  playerWorldX = (playerWorldX + 0.22f).coerceAtMost(1.1f)
                  playerHeadingAngle = 22f
                  GameSoundEffects.playJump()
                },
                modifier = Modifier.size(50.dp).testTag("btn_gta_beirut_right"),
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
                val weapons = BeirutWeapon.values()
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
                Text(currentWeapon.titleAr.take(8), color = currentWeapon.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Action Buttons: Shoot & Nitro / Drift
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Attack / Shoot
              Button(
                onClick = { shoot3DWeapon() },
                colors = ButtonDefaults.buttonColors(containerColor = GangShawlRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(50.dp).testTag("btn_gta_beirut_attack")
              ) {
                Text(if (isDriving) "إطلاق 🎯" else "هجوم 👊", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              // Turbo / Drift Button
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
                modifier = Modifier.height(50.dp).testTag("btn_gta_beirut_nitro")
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

// ----------------------------------------------------
// 3D MiniMap Compass Radar
// ----------------------------------------------------
@Composable
fun BeirutMiniMapRadar(
  playerX: Float,
  traffic: List<Beirut3DCar>,
  roadblocks: List<Beirut3DRoadblock>,
  wantedStars: Int
) {
  Box(
    modifier = Modifier
      .size(74.dp)
      .clip(CircleShape)
      .background(DarkBg.copy(alpha = 0.88f))
      .border(1.5.dp, if (wantedStars > 0) PoliceRedLight else Color(0xFF00E5FF), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = w / 2f
      val cy = h / 2f

      // Radar Concentric Grid
      drawCircle(color = Color.DarkGray.copy(alpha = 0.5f), radius = w * 0.45f, center = Offset(cx, cy))
      drawCircle(color = Color.DarkGray.copy(alpha = 0.5f), radius = w * 0.25f, center = Offset(cx, cy))

      // Crosshairs
      drawLine(color = Color.DarkGray.copy(alpha = 0.4f), start = Offset(0f, cy), end = Offset(w, cy), strokeWidth = 1f)
      drawLine(color = Color.DarkGray.copy(alpha = 0.4f), start = Offset(cx, 0f), end = Offset(cx, h), strokeWidth = 1f)

      // Traffic Blips
      traffic.forEach { t ->
        if (!t.isDestroyed) {
          val bx = cx + (t.worldX - playerX) * (w * 0.35f)
          val by = cy - (t.worldZ / 260f) * (h * 0.40f)
          val blipColor = if (t.type.isPolice) PoliceRedLight else Color.LightGray
          drawCircle(color = blipColor, radius = 3.5f, center = Offset(bx, by))
        }
      }

      // Player Arrow at center
      drawCircle(color = Color(0xFF00E5FF), radius = 5f, center = Offset(cx, cy))
    }
  }
}

// ----------------------------------------------------
// True 3D Perspective Projection Engine
// ----------------------------------------------------
@Composable
fun Beirut3DWorldCanvas(
  playerWorldX: Float,
  playerWorldY: Float,
  playerHeading: Float,
  isDriving: Boolean,
  currentVehicle: BeirutVehicleType,
  vehicleColor: Color,
  isNitro: Boolean,
  isDrifting: Boolean,
  cameraMode: BeirutCameraMode,
  wantedStars: Int,
  traffic: List<Beirut3DCar>,
  pedestrians: List<Beirut3DPedestrian>,
  projectiles: List<Beirut3DProjectile>,
  roadblocks: List<Beirut3DRoadblock>,
  pickups: List<Beirut3DPickup>,
  sceneries: List<Beirut3DScenery>,
  distance: Float,
  zone: BeirutZone,
  onSteerDrag: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures { _, dragAmount ->
          onSteerDrag(dragAmount.x / 250f)
        }
      }
  ) {
    val width = size.width
    val height = size.height
    val horizonY = height * 0.42f // Horizon Line at 42% of screen
    val centerX = width / 2f - (playerWorldX * width * 0.18f)

    // 1. Beirut Sky & Mediterranean Sea Sunset Background
    // Sky Gradient
    val skyBrush = Brush.verticalGradient(
      colors = listOf(Color(0xFF0D1B2A), Color(0xFF415A77), Color(0xFFE07A5F), Color(0xFFF4A261)),
      startY = 0f,
      endY = horizonY
    )
    drawRect(brush = skyBrush, topLeft = Offset(0f, 0f), size = Size(width, horizonY))

    // Sun over the Mediterranean Sea
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD166), Color(0xFFF4A261).copy(alpha = 0.5f), Color.Transparent),
        center = Offset(width * 0.72f, horizonY * 0.65f),
        radius = 70.dp.toPx()
      ),
      center = Offset(width * 0.72f, horizonY * 0.65f),
      radius = 70.dp.toPx()
    )

    // 3D Raouche Pigeon Rocks Landmark on Sea Horizon (Left Side)
    val rockLeftX = width * 0.18f
    val rockPath = Path().apply {
      moveTo(rockLeftX - 35f, horizonY)
      lineTo(rockLeftX - 25f, horizonY - 45f)
      lineTo(rockLeftX - 10f, horizonY - 55f)
      lineTo(rockLeftX + 5f, horizonY - 40f)
      lineTo(rockLeftX + 20f, horizonY - 60f)
      lineTo(rockLeftX + 35f, horizonY)
      close()
    }
    drawPath(path = rockPath, color = Color(0xFF4E342E))

    // Sea Surface (Left & Right Coastal Waters)
    val seaBrush = Brush.verticalGradient(
      colors = listOf(Color(0xFF1E3D59), Color(0xFF17B890), Color(0xFF006494)),
      startY = horizonY,
      endY = height
    )
    drawRect(brush = seaBrush, topLeft = Offset(0f, horizonY), size = Size(width, height - horizonY))

    // 2. 3D Perspective Road Highway (Beirut Corniche & Charles Helou)
    val roadTopWidth = width * 0.12f
    val roadBottomWidth = width * 1.15f
    val roadLeftTop = centerX - (roadTopWidth / 2f)
    val roadRightTop = centerX + (roadTopWidth / 2f)
    val roadLeftBottom = width / 2f - (roadBottomWidth / 2f)
    val roadRightBottom = width / 2f + (roadBottomWidth / 2f)

    // Road Polygon Path
    val roadPath = Path().apply {
      moveTo(roadLeftTop, horizonY)
      lineTo(roadRightTop, horizonY)
      lineTo(roadRightBottom, height)
      lineTo(roadLeftBottom, height)
      close()
    }
    drawPath(path = roadPath, color = Color(0xFF1F242D))

    // 3D Road Curbs (Red & White Lebanese Striped Sidewalks)
    val curbLeftPath = Path().apply {
      moveTo(roadLeftTop - 12f, horizonY)
      lineTo(roadLeftTop, horizonY)
      lineTo(roadLeftBottom, height)
      lineTo(roadLeftBottom - 35f, height)
      close()
    }
    drawPath(path = curbLeftPath, color = Color(0xFFD32F2F))

    val curbRightPath = Path().apply {
      moveTo(roadRightTop, horizonY)
      lineTo(roadRightTop + 12f, horizonY)
      lineTo(roadRightBottom + 35f, height)
      lineTo(roadRightBottom, height)
      close()
    }
    drawPath(path = curbRightPath, color = Color(0xFFD32F2F))

    // 3D Perspective Dashed Lane Dividers
    val numSegments = 14
    for (i in 0 until numSegments) {
      val zNear = (i + (distance * 0.05f) % 1f) / numSegments.toFloat()
      val zFar = (i + 0.6f + (distance * 0.05f) % 1f) / numSegments.toFloat()

      if (zNear in 0f..1f && zFar in 0f..1f) {
        val y1 = horizonY + (height - horizonY) * (zNear * zNear)
        val y2 = horizonY + (height - horizonY) * (zFar * zFar)

        val w1 = roadTopWidth + (roadBottomWidth - roadTopWidth) * zNear
        val w2 = roadTopWidth + (roadBottomWidth - roadTopWidth) * zFar

        val cX1 = centerX + (width / 2f - centerX) * zNear
        val cX2 = centerX + (width / 2f - centerX) * zFar

        // Left Lane Dash
        val lx1 = cX1 - (w1 * 0.18f)
        val lx2 = cX2 - (w2 * 0.18f)
        drawLine(
          color = TaxiYellow.copy(alpha = 0.85f),
          start = Offset(lx1, y1),
          end = Offset(lx2, y2),
          strokeWidth = (2f + 5f * zNear)
        )

        // Right Lane Dash
        val rx1 = cX1 + (w1 * 0.18f)
        val rx2 = cX2 + (w2 * 0.18f)
        drawLine(
          color = TaxiYellow.copy(alpha = 0.85f),
          start = Offset(rx1, y1),
          end = Offset(rx2, y2),
          strokeWidth = (2f + 5f * zNear)
        )
      }
    }

    // 3. Render 3D Scenery (Palms & Buildings sorted by Z Depth)
    sceneries.sortedByDescending { it.distanceZ }.forEach { scen ->
      val depth = (scen.distanceZ / 260f).coerceIn(0.04f, 1f)
      val scale = 1f - depth
      if (scale > 0.02f) {
        val sy = horizonY + (height - horizonY) * (scale * scale)
        val curRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * scale
        val curCX = centerX + (width / 2f - centerX) * scale
        val sx = curCX + (scen.side * (curRoadW * 0.65f + 40f * scale))

        when (scen.typeName) {
          "PALM" -> {
            // 3D Palm Tree Trunk & Fronds
            val trunkH = 80f * scale * scen.heightScale
            drawLine(
              color = Color(0xFF5D4037),
              start = Offset(sx, sy),
              end = Offset(sx, sy - trunkH),
              strokeWidth = 5f * scale
            )
            // Fronds (Green Leaves)
            drawCircle(
              color = Color(0xFF2E7D32),
              radius = 22f * scale * scen.heightScale,
              center = Offset(sx, sy - trunkH)
            )
          }
          "BUILDING", "HERITAGE_BALCONY" -> {
            // 3D Beirut City Skyscraper / Heritage Building
            val bWidth = 60f * scale
            val bHeight = 110f * scale * scen.heightScale
            val bRect = Path().apply {
              addRect(androidx.compose.ui.geometry.Rect(sx - bWidth / 2f, sy - bHeight, sx + bWidth / 2f, sy))
            }
            drawPath(path = bRect, color = scen.color.copy(alpha = 0.9f))
            // Arched Lebanese Windows / Neon Sign
            drawCircle(
              color = TaxiYellow.copy(alpha = 0.8f),
              radius = 5f * scale,
              center = Offset(sx, sy - bHeight * 0.6f)
            )
          }
        }
      }
    }

    // 4. Render 3D Roadblocks (ISF Barricades)
    roadblocks.forEach { rb ->
      val depth = (rb.worldZ / 260f).coerceIn(0.02f, 1f)
      val scale = 1f - depth
      if (scale > 0.02f) {
        val rby = horizonY + (height - horizonY) * (scale * scale)
        val curRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * scale
        val curCX = centerX + (width / 2f - centerX) * scale
        val rbx = curCX + (rb.worldX * curRoadW * 0.40f)

        val rbW = 80f * scale
        val rbH = 25f * scale

        // Barricade Plank
        drawRect(
          color = Color(0xFFFF5722),
          topLeft = Offset(rbx - rbW / 2f, rby - rbH),
          size = Size(rbW, rbH)
        )
        // White Stripes on Barricade
        drawRect(
          color = Color.White,
          topLeft = Offset(rbx - rbW / 4f, rby - rbH),
          size = Size(rbW / 3f, rbH)
        )
      }
    }

    // 5. Render 3D Traffic & ISF Police Cruisers
    traffic.sortedByDescending { it.worldZ }.forEach { car ->
      val depth = (car.worldZ / 260f).coerceIn(0.02f, 1.1f)
      val scale = (1f - depth).coerceIn(0.04f, 1.2f)

      if (scale > 0.03f && !car.isDestroyed) {
        val cy = horizonY + (height - horizonY) * (scale * scale)
        val curRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * scale
        val curCX = centerX + (width / 2f - centerX) * scale
        val cx = curCX + (car.worldX * curRoadW * 0.42f)

        render3DCarModel(
          drawScope = this,
          x = cx,
          y = cy,
          scale = scale,
          type = car.type,
          color = car.customColor,
          isPolice = car.type.isPolice,
          isNitro = false,
          headingTilt = 0f
        )
      }
    }

    // 6. Render 3D Cash Pickups
    pickups.forEach { p ->
      val depth = (p.worldZ / 260f).coerceIn(0.02f, 1f)
      val scale = 1f - depth
      if (scale > 0.03f) {
        val py = horizonY + (height - horizonY) * (scale * scale)
        val curRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * scale
        val curCX = centerX + (width / 2f - centerX) * scale
        val px = curCX + (p.worldX * curRoadW * 0.40f)

        drawCircle(
          color = GangNeonGreen,
          radius = 10f * scale,
          center = Offset(px, py - 12f * scale)
        )
      }
    }

    // 7. Render 3D Projectiles
    projectiles.forEach { proj ->
      val depth = (proj.worldZ / 260f).coerceIn(0.02f, 1f)
      val scale = 1f - depth
      if (scale > 0.03f) {
        val pjy = horizonY + (height - horizonY) * (scale * scale) - (proj.worldY * 60f * scale)
        val curRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * scale
        val curCX = centerX + (width / 2f - centerX) * scale
        val pjx = curCX + (proj.worldX * curRoadW * 0.40f)

        drawCircle(
          color = proj.weapon.color,
          radius = 6f * scale,
          center = Offset(pjx, pjy)
        )
      }
    }

    // 8. Render PLAYER (3D Vehicle or 3D Character Model On Foot)
    val playerScale = 1.0f
    val playerScreenY = height * 0.80f - (playerWorldY * 50f)
    val playerScreenX = width / 2f

    if (isDriving) {
      // 3D Player Car Model
      render3DCarModel(
        drawScope = this,
        x = playerScreenX,
        y = playerScreenY,
        scale = playerScale,
        type = currentVehicle,
        color = vehicleColor,
        isPolice = currentVehicle.isPolice,
        isNitro = isNitro,
        headingTilt = playerHeading
      )

      // Drift Smoke / Nitro Flames
      if (isNitro) {
        // Dual Nitro Flames from Exhaust
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF0D47A1), Color.Transparent),
            center = Offset(playerScreenX - 25f, playerScreenY + 25f),
            radius = 25f
          ),
          center = Offset(playerScreenX - 25f, playerScreenY + 25f),
          radius = 25f
        )
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF0D47A1), Color.Transparent),
            center = Offset(playerScreenX + 25f, playerScreenY + 25f),
            radius = 25f
          ),
          center = Offset(playerScreenX + 25f, playerScreenY + 25f),
          radius = 25f
        )
      }
    } else {
      // 3D Third-Person Character (Jad - ابن البلد) on foot
      render3DCharacterModel(
        drawScope = this,
        x = playerScreenX,
        y = playerScreenY,
        scale = playerScale,
        headingTilt = playerHeading,
        isRunning = true
      )
    }

    // 9. Location & Zone HUD Tag
    try {
      drawText(
        textMeasurer = textMeasurer,
        text = "📍 ${zone.titleAr}",
        topLeft = Offset(width * 0.05f, height * 0.94f),
        style = TextStyle(color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Bold),
        size = Size((width * 0.9f).coerceAtLeast(50f), 40f)
      )
    } catch (_: Throwable) {}
  }
}

// ----------------------------------------------------
// 3D Vehicle Renderer Helper
// ----------------------------------------------------
fun render3DCarModel(
  drawScope: DrawScope,
  x: Float,
  y: Float,
  scale: Float,
  type: BeirutVehicleType,
  color: Color,
  isPolice: Boolean,
  isNitro: Boolean,
  headingTilt: Float
) {
  with(drawScope) {
    val carW = when (type) {
      BeirutVehicleType.SCOOTER_TMAX -> 35f * scale
      BeirutVehicleType.VAN_FOUR -> 78f * scale
      BeirutVehicleType.LEBANESE_PICKUP -> 85f * scale
      BeirutVehicleType.SUPERCAR_SOLIDERE -> 88f * scale
      else -> 75f * scale // Mercedes Taxi & ISF
    }

    val carH = when (type) {
      BeirutVehicleType.SCOOTER_TMAX -> 45f * scale
      BeirutVehicleType.VAN_FOUR -> 82f * scale
      BeirutVehicleType.LEBANESE_PICKUP -> 68f * scale
      BeirutVehicleType.SUPERCAR_SOLIDERE -> 48f * scale
      else -> 60f * scale
    }

    // Shadow on asphalt
    drawOval(
      color = Color.Black.copy(alpha = 0.55f),
      topLeft = Offset(x - carW * 0.6f + (headingTilt * 0.4f), y - carH * 0.2f),
      size = Size(carW * 1.2f, carH * 0.6f)
    )

    // Main 3D Chassis (Back & Roof Perspective)
    val chassisPath = Path().apply {
      moveTo(x - carW / 2f + (headingTilt * 0.3f), y)
      lineTo(x + carW / 2f + (headingTilt * 0.3f), y)
      lineTo(x + carW * 0.42f, y - carH)
      lineTo(x - carW * 0.42f, y - carH)
      close()
    }
    drawPath(path = chassisPath, color = color)

    // Rear Windshield (Glass)
    val windshieldPath = Path().apply {
      moveTo(x - carW * 0.35f, y - carH * 0.4f)
      lineTo(x + carW * 0.35f, y - carH * 0.4f)
      lineTo(x + carW * 0.28f, y - carH * 0.85f)
      lineTo(x - carW * 0.28f, y - carH * 0.85f)
      close()
    }
    drawPath(path = windshieldPath, color = Color(0xFF102027))

    // Red Tail Lights (Rear)
    drawRect(
      color = Color(0xFFFF1744),
      topLeft = Offset(x - carW * 0.46f, y - carH * 0.28f),
      size = Size(carW * 0.18f, carH * 0.16f)
    )
    drawRect(
      color = Color(0xFFFF1744),
      topLeft = Offset(x + carW * 0.28f, y - carH * 0.28f),
      size = Size(carW * 0.18f, carH * 0.16f)
    )

    // Lebanese License Plate (Red Lebanese Plate for Taxi / White for Private)
    val plateColor = if (type == BeirutVehicleType.MERCEDES_SERVICE || type == BeirutVehicleType.VAN_FOUR) Color(0xFFD32F2F) else Color.White
    drawRect(
      color = plateColor,
      topLeft = Offset(x - carW * 0.15f, y - carH * 0.22f),
      size = Size(carW * 0.30f, carH * 0.12f)
    )

    // ISF Police Lightbar (Flashing Red & Blue LEDs)
    if (isPolice) {
      val lightW = carW * 0.50f
      val lightH = carH * 0.14f
      // Blue LED (Left)
      drawRect(
        color = Color(0xFF2979FF),
        topLeft = Offset(x - lightW / 2f, y - carH - lightH),
        size = Size(lightW / 2f, lightH)
      )
      // Red LED (Right)
      drawRect(
        color = Color(0xFFFF1744),
        topLeft = Offset(x, y - carH - lightH),
        size = Size(lightW / 2f, lightH)
      )
    }

    // Taxi Roof Sign for Service
    if (type == BeirutVehicleType.MERCEDES_SERVICE) {
      drawRect(
        color = TaxiYellow,
        topLeft = Offset(x - carW * 0.18f, y - carH - carH * 0.12f),
        size = Size(carW * 0.36f, carH * 0.12f)
      )
    }

    // Wheels (Tires)
    drawRect(
      color = Color(0xFF111111),
      topLeft = Offset(x - carW * 0.52f, y - carH * 0.18f),
      size = Size(carW * 0.10f, carH * 0.24f)
    )
    drawRect(
      color = Color(0xFF111111),
      topLeft = Offset(x + carW * 0.42f, y - carH * 0.18f),
      size = Size(carW * 0.10f, carH * 0.24f)
    )
  }
}

// ----------------------------------------------------
// 3D Character Model Renderer Helper (On Foot)
// ----------------------------------------------------
fun render3DCharacterModel(
  drawScope: DrawScope,
  x: Float,
  y: Float,
  scale: Float,
  headingTilt: Float,
  isRunning: Boolean
) {
  with(drawScope) {
    val charH = 65f * scale
    val charW = 28f * scale

    // Shadow
    drawOval(
      color = Color.Black.copy(alpha = 0.5f),
      topLeft = Offset(x - charW * 0.6f, y - 8f),
      size = Size(charW * 1.2f, 16f)
    )

    // Torso / Jacket (Lebanese Hero Outfit)
    drawRoundRect(
      color = Color(0xFF263238),
      topLeft = Offset(x - charW / 2f, y - charH * 0.75f),
      size = Size(charW, charH * 0.45f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // Head
    drawCircle(
      color = Color(0xFFFFCC80),
      radius = charW * 0.38f,
      center = Offset(x, y - charH * 0.88f)
    )

    // Hair / Cap
    drawArc(
      color = Color(0xFF1A1A1A),
      startAngle = 180f,
      sweepAngle = 180f,
      useCenter = true,
      topLeft = Offset(x - charW * 0.42f, y - charH * 0.98f),
      size = Size(charW * 0.84f, charW * 0.65f)
    )

    // Legs in running stride
    val legOffset = if (isRunning) 10f else 0f
    drawLine(
      color = Color(0xFF1565C0), // Blue jeans
      start = Offset(x - charW * 0.25f, y - charH * 0.30f),
      end = Offset(x - charW * 0.35f + legOffset, y),
      strokeWidth = 6f * scale
    )
    drawLine(
      color = Color(0xFF1565C0),
      start = Offset(x + charW * 0.25f, y - charH * 0.30f),
      end = Offset(x + charW * 0.35f - legOffset, y),
      strokeWidth = 6f * scale
    )
  }
}
