package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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

enum class ObstacleType {
  SPICE_CART,
  MUD_BARRICADE,
  POLICE_SPIKES,
  LOW_ARCH,
  COIN_PACK,
  SCOUT_HELPER
}

data class StreetObstacle(
  val id: Long,
  var yPos: Float, // 0f (top) to 1f (bottom)
  val lane: Int,   // 0: Left, 1: Center, 2: Right
  val type: ObstacleType,
  var isCollected: Boolean = false
)

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var alpha: Float,
  val color: Color,
  val size: Float
)

@Composable
fun ChaseGameScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  val selectedFaction by repository.selectedFaction.collectAsState()

  var isPlaying by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var isPaused by remember { mutableStateOf(false) }

  var playerLane by remember { mutableStateOf(1) } // 0, 1, 2
  var isJumping by remember { mutableStateOf(false) }
  var isSliding by remember { mutableStateOf(false) }

  var score by remember { mutableIntStateOf(0) }
  var coinsThisRun by remember { mutableIntStateOf(0) }
  var distanceCovered by remember { mutableFloatStateOf(0f) }
  var gameSpeed by remember { mutableFloatStateOf(0.012f) }

  // Special Gadgets Count
  var bananaAmmo by remember { mutableIntStateOf(3) }
  var graffitiAmmo by remember { mutableIntStateOf(2) }
  var fireworkAmmo by remember { mutableIntStateOf(2) }
  var netAmmo by remember { mutableIntStateOf(3) }
  var smokeAmmo by remember { mutableIntStateOf(2) }

  var activeGraffitiEffect by remember { mutableStateOf(false) }
  var activeSmokeEffect by remember { mutableStateOf(false) }
  var adrenalineBoost by remember { mutableFloatStateOf(0f) }

  val obstacles = remember { mutableStateListOf<StreetObstacle>() }
  val particles = remember { mutableStateListOf<Particle>() }
  val random = remember { Random() }

  // Game Loop
  LaunchedEffect(isPlaying, isPaused, isGameOver) {
    if (isPlaying && !isPaused && !isGameOver) {
      var nextObstacleTick = 0
      var obstacleIdCounter = 0L

      while (isActive && isPlaying && !isGameOver) {
        if (!isPaused) {
          distanceCovered += (gameSpeed * 100)
          score = (distanceCovered * 10).toInt() + (coinsThisRun * 50)
          gameSpeed = (0.012f + (distanceCovered / 10000f)).coerceAtMost(0.028f)

          if (adrenalineBoost > 0f) {
            adrenalineBoost = (adrenalineBoost - 0.01f).coerceAtLeast(0f)
          }

          // Spawn obstacles
          nextObstacleTick++
          if (nextObstacleTick >= 35 - (gameSpeed * 600).toInt().coerceAtMost(20)) {
            nextObstacleTick = 0
            val lane = random.nextInt(3)
            val typeRoll = random.nextInt(100)
            val type = when {
              typeRoll < 30 -> ObstacleType.COIN_PACK
              typeRoll < 50 -> ObstacleType.SPICE_CART
              typeRoll < 70 -> ObstacleType.MUD_BARRICADE
              typeRoll < 85 -> ObstacleType.LOW_ARCH
              typeRoll < 92 -> ObstacleType.POLICE_SPIKES
              else -> ObstacleType.SCOUT_HELPER
            }
            obstacles.add(
              StreetObstacle(
                id = obstacleIdCounter++,
                yPos = -0.1f,
                lane = lane,
                type = type
              )
            )
          }

          // Update obstacles position
          val iterator = obstacles.iterator()
          while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.yPos += gameSpeed * if (adrenalineBoost > 0f) 1.5f else 1.0f

            // Collision check with player (player is at yPos ~ 0.78f)
            if (!obs.isCollected && obs.yPos in 0.72f..0.85f && obs.lane == playerLane) {
              when (obs.type) {
                ObstacleType.COIN_PACK -> {
                  obs.isCollected = true
                  coinsThisRun += 10
                  GameSoundEffects.playCoin()
                  // Add shiny particles
                  repeat(6) {
                    particles.add(
                      Particle(
                        x = 0.2f + playerLane * 0.3f,
                        y = 0.78f,
                        vx = (random.nextFloat() - 0.5f) * 0.02f,
                        vy = (random.nextFloat() - 0.5f) * 0.02f,
                        alpha = 1f,
                        color = SanaaGold,
                        size = 8f
                      )
                    )
                  }
                }
                ObstacleType.SCOUT_HELPER -> {
                  obs.isCollected = true
                  coinsThisRun += 25
                  if (selectedFaction == Faction.GANG) {
                    bananaAmmo = (bananaAmmo + 1).coerceAtMost(5)
                    graffitiAmmo = (graffitiAmmo + 1).coerceAtMost(4)
                  } else {
                    netAmmo = (netAmmo + 1).coerceAtMost(5)
                    smokeAmmo = (smokeAmmo + 1).coerceAtMost(4)
                  }
                  GameSoundEffects.playWalkieTalkie()
                }
                ObstacleType.LOW_ARCH -> {
                  // Must slide under
                  if (!isSliding && adrenalineBoost <= 0f) {
                    isGameOver = true
                    isPlaying = false
                    GameSoundEffects.playCarCrash()
                    repository.recordChaseScore(score, coinsThisRun)
                  }
                }
                ObstacleType.SPICE_CART, ObstacleType.MUD_BARRICADE -> {
                  // Must jump over
                  if (!isJumping && adrenalineBoost <= 0f) {
                    isGameOver = true
                    isPlaying = false
                    GameSoundEffects.playCarCrash()
                    repository.recordChaseScore(score, coinsThisRun)
                  }
                }
                ObstacleType.POLICE_SPIKES -> {
                  if (adrenalineBoost <= 0f) {
                    isGameOver = true
                    isPlaying = false
                    GameSoundEffects.playCarCrash()
                    repository.recordChaseScore(score, coinsThisRun)
                  }
                }
              }
            }

            if (obs.yPos > 1.15f) {
              iterator.remove()
            }
          }

          // Update particles
          val pIter = particles.iterator()
          while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= 0.05f
            if (p.alpha <= 0f) {
              pIter.remove()
            }
          }
        }
        delay(16) // ~60fps
      }
    }
  }

  // Jump animation reset
  LaunchedEffect(isJumping) {
    if (isJumping) {
      GameSoundEffects.playJump()
      delay(450)
      isJumping = false
    }
  }

  // Slide animation reset
  LaunchedEffect(isSliding) {
    if (isSliding) {
      delay(400)
      isSliding = false
    }
  }

  // Graffiti temporary effect
  LaunchedEffect(activeGraffitiEffect) {
    if (activeGraffitiEffect) {
      delay(3000)
      activeGraffitiEffect = false
    }
  }

  // Smoke temporary effect
  LaunchedEffect(activeSmokeEffect) {
    if (activeSmokeEffect) {
      delay(3000)
      activeSmokeEffect = false
    }
  }

  fun startNewGame() {
    obstacles.clear()
    particles.clear()
    score = 0
    coinsThisRun = 0
    distanceCovered = 0f
    gameSpeed = 0.012f
    playerLane = 1
    isJumping = false
    isSliding = false
    isGameOver = false
    isPaused = false
    bananaAmmo = 3
    graffitiAmmo = 2
    fireworkAmmo = 2
    netAmmo = 3
    smokeAmmo = 2
    isPlaying = true
    if (selectedFaction == Faction.POLICE) {
      GameSoundEffects.playPoliceWhistle()
    } else {
      GameSoundEffects.playSiren()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    // Header Bar
    SanaaTopBar(
      title = if (selectedFaction == Faction.GANG) "مطاردة المشاغبين الصغار" else "مطاردة شرطة صنعاء",
      subtitle = "صنعاء القديمة • أزقة سوق الملح والأسطح",
      coins = stats.totalCoins + coinsThisRun,
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
      // The Main Interactive Game Canvas
      GameChaseCanvas(
        faction = selectedFaction,
        playerLane = playerLane,
        isJumping = isJumping,
        isSliding = isSliding,
        obstacles = obstacles,
        particles = particles,
        distance = distanceCovered,
        activeGraffiti = activeGraffitiEffect,
        activeSmoke = activeSmokeEffect,
        adrenalineBoost = adrenalineBoost > 0f,
        onSwipeLeft = {
          if (playerLane > 0) {
            playerLane--
            GameSoundEffects.playJump()
          }
        },
        onSwipeRight = {
          if (playerLane < 2) {
            playerLane++
            GameSoundEffects.playJump()
          }
        }
      )

      // HUD Overlay: Score & Stats
      if (isPlaying && !isGameOver) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Score & Distance
          Column(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(DarkSurface.copy(alpha = 0.85f))
              .border(1.dp, SanaaGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = "النقاط: $score",
              style = MaterialTheme.typography.titleMedium.copy(
                color = SanaaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            )
            Text(
              text = "المسافة: ${(distanceCovered).toInt()} متر",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.LightGray,
                fontSize = 11.sp
              )
            )
          }

          // In-run coins
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(DarkSurface.copy(alpha = 0.85f))
              .border(1.dp, TaxiYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("🪙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+$coinsThisRun",
              style = MaterialTheme.typography.labelLarge.copy(
                color = TaxiYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            )
          }

          // Pause Button
          IconButton(
            onClick = { isPaused = !isPaused },
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(DarkSurface.copy(alpha = 0.85f))
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = "إيقاف مؤقت",
              tint = Color.White
            )
          }
        }
      }

      // Start Screen Overlay
      if (!isPlaying && !isGameOver) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.90f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.5.dp, SanaaGold, RoundedCornerShape(20.dp))
          ) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Action Banner Image
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_sanaa_chase_action),
                  contentDescription = "مطاردة أكشن في شوارع صنعاء",
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
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    color = GangShawlRed,
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = "⚡ طور الأكشن الحركي",
                      color = Color.White,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }

              Column(modifier = Modifier.padding(16.dp)) {
                // Cloned Real Hero Header Row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Image(
                    painter = painterResource(id = R.drawable.img_hero_avatar),
                    contentDescription = "مازن - البطل الحقيقي",
                    modifier = Modifier
                      .size(48.dp)
                      .clip(CircleShape)
                      .border(2.dp, SanaaGold, CircleShape),
                    contentScale = ContentScale.Crop
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = if (selectedFaction == Faction.GANG) "مازن (الزعيم الصغير الحقيقي) 👑" else "ملازم أول أحمد الصنعاني 👮‍♂️",
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SanaaGold,
                        fontSize = 15.sp
                      )
                    )
                    Text(
                      text = if (selectedFaction == Faction.GANG) "باركور الأسطح • رمي المقلاع • حركات بهلوانية" else "مطاردة بالدباب • شباك الإمساك • إشارات لاسلكية",
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.LightGray,
                        fontSize = 11.sp
                      )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = if (selectedFaction == Faction.GANG)
                    "اركض في أزقة صنعاء القديمة، اقفز فوق عربات التوابل وأسطح الطين، استعن بأطفال الحارة وأطلق المفرقعات وقشور الموز للإفلات والسيطرة على الحارة!"
                  else
                    "نسق مع الدورية، سد المنافذ، وانشر شباك الإمساك والقنابل الدخانية للقبض على المشاغبين الصغار بسلام!",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                  )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // High score banner
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "أعلى رقم قياسي في الشوارع:",
                    color = Color.LightGray,
                    fontSize = 12.sp
                  )
                  Text(
                    text = "${stats.highChaseScore} نقطة 🏆",
                    color = SanaaGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                  onClick = { startNewGame() },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFaction == Faction.GANG) GangShawlRed else PoliceAccent
                  ),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_chase_game_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "ابدأ مطاردة وأكشن صنعاء الآن 🏃‍♂️💨",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      // Game Over Overlay
      if (isGameOver) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.92f))
            .padding(24.dp),
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
              modifier = Modifier.padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = if (selectedFaction == Faction.GANG) "🚨 تم إيقاف المشاغب!" else "🏃‍♂️ أفلت المشاغب الصغير!",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (selectedFaction == Faction.GANG) PoliceRedLight else SanaaGold,
                  fontSize = 20.sp
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("النقاط المحققة", color = Color.Gray, fontSize = 12.sp)
                  Text(
                    text = "$score",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                  )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("الريالات المجمعة", color = Color.Gray, fontSize = 12.sp)
                  Text(
                    text = "+$coinsThisRun 🪙",
                    color = TaxiYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = onNavigateBack,
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("القائمة الرئيسية", color = Color.White, fontSize = 13.sp)
                }
                Button(
                  onClick = { startNewGame() },
                  colors = ButtonDefaults.buttonColors(containerColor = SanaaClay),
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("retry_chase_btn"),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("إعادة المحاولة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              }
            }
          }
        }
      }
    }

    // Interactive Action Controls at Bottom
    if (isPlaying && !isGameOver) {
      Surface(
        color = DarkSurface,
        tonalElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
        ) {
          // Tactical Gadgets Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            if (selectedFaction == Faction.GANG) {
              // Banana Peel
              TacticalGadgetButton(
                icon = "🍌",
                label = "قشر موز",
                count = bananaAmmo,
                onClick = {
                  if (bananaAmmo > 0) {
                    bananaAmmo--
                    GameSoundEffects.playFirework()
                    // Clear nearby obstacle
                    obstacles.removeAll { it.yPos > 0.5f && it.type != ObstacleType.COIN_PACK }
                  }
                }
              )
              // Graffiti Spray
              TacticalGadgetButton(
                icon = "🎨",
                label = "غرافيتي",
                count = graffitiAmmo,
                onClick = {
                  if (graffitiAmmo > 0) {
                    graffitiAmmo--
                    activeGraffitiEffect = true
                    GameSoundEffects.playGraffitiSpray()
                  }
                }
              )
              // Fireworks
              TacticalGadgetButton(
                icon = "🎆",
                label = "مفرقعات",
                count = fireworkAmmo,
                onClick = {
                  if (fireworkAmmo > 0) {
                    fireworkAmmo--
                    adrenalineBoost = 1.0f
                    GameSoundEffects.playFirework()
                  }
                }
              )
            } else {
              // Police Nets
              TacticalGadgetButton(
                icon = "🕸️",
                label = "شبكة إمساك",
                count = netAmmo,
                onClick = {
                  if (netAmmo > 0) {
                    netAmmo--
                    GameSoundEffects.playPoliceWhistle()
                    obstacles.removeAll { it.yPos > 0.4f && it.type != ObstacleType.COIN_PACK }
                  }
                }
              )
              // Smoke Screen
              TacticalGadgetButton(
                icon = "💨",
                label = "قنبلة دخان",
                count = smokeAmmo,
                onClick = {
                  if (smokeAmmo > 0) {
                    smokeAmmo--
                    activeSmokeEffect = true
                    GameSoundEffects.playNoise(200)
                  }
                }
              )
              // Siren Rush
              TacticalGadgetButton(
                icon = "🚨",
                label = "صفارة إنذار",
                count = 3,
                onClick = {
                  adrenalineBoost = 1.0f
                  GameSoundEffects.playSiren()
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // D-Pad and Action Buttons Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left & Right Steer Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  if (playerLane > 0) {
                    playerLane--
                    GameSoundEffects.playJump()
                  }
                },
                modifier = Modifier
                  .size(54.dp)
                  .testTag("chase_left_btn"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
              ) {
                Text("◀", fontSize = 18.sp, color = Color.White)
              }

              Button(
                onClick = {
                  if (playerLane < 2) {
                    playerLane++
                    GameSoundEffects.playJump()
                  }
                },
                modifier = Modifier
                  .size(54.dp)
                  .testTag("chase_right_btn"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
              ) {
                Text("▶", fontSize = 18.sp, color = Color.White)
              }
            }

            // Jump & Slide Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              // Slide Button
              Button(
                onClick = { isSliding = true },
                modifier = Modifier
                  .height(54.dp)
                  .testTag("chase_slide_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SanaaMudWarm)
              ) {
                Text("زحلقة 🔻", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              // Jump Button
              Button(
                onClick = { isJumping = true },
                modifier = Modifier
                  .height(54.dp)
                  .testTag("chase_jump_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (selectedFaction == Faction.GANG) GangShawlRed else PoliceAccent
                )
              ) {
                Text("قفزة باركور 🔺", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TacticalGadgetButton(
  icon: String,
  label: String,
  count: Int,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(
      onClick = onClick,
      enabled = count > 0,
      modifier = Modifier.size(50.dp),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = DarkSurfaceVariant,
        disabledContainerColor = DarkBg
      ),
      contentPadding = PaddingValues(0.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(text = icon, fontSize = 20.sp)
        if (count > 0) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 6.dp, y = (-6).dp)
              .clip(CircleShape)
              .background(SanaaGold)
              .size(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$count",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = DarkBg
            )
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(
        fontSize = 10.sp,
        color = Color.LightGray
      )
    )
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
fun GameChaseCanvas(
  faction: Faction,
  playerLane: Int,
  isJumping: Boolean,
  isSliding: Boolean,
  obstacles: List<StreetObstacle>,
  particles: List<Particle>,
  distance: Float,
  activeGraffiti: Boolean,
  activeSmoke: Boolean,
  adrenalineBoost: Boolean,
  onSwipeLeft: () -> Unit,
  onSwipeRight: () -> Unit,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectHorizontalDragGestures { _, dragAmount ->
          if (dragAmount > 25) {
            onSwipeRight()
          } else if (dragAmount < -25) {
            onSwipeLeft()
          }
        }
      }
  ) {
    val width = size.width
    val height = size.height

    // 1. Draw Sana'a Skyline & Clay Tower Buildings in Background
    drawSanaaArchitectureBackground(width, height, distance)

    // 2. Draw 3 Street Lanes
    val laneWidth = width / 3f
    val laneCenterX = listOf(laneWidth * 0.5f, laneWidth * 1.5f, laneWidth * 2.5f)

    // Cobblestone road texture lines
    for (i in 1..2) {
      val xLine = laneWidth * i
      drawLine(
        color = Color.White.copy(alpha = 0.18f),
        start = Offset(xLine, 0f),
        end = Offset(xLine, height),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), distance % 50f)
      )
    }

    // 3. Draw Obstacles
    obstacles.forEach { obs ->
      if (!obs.isCollected) {
        val obsX = laneCenterX[obs.lane]
        val obsY = obs.yPos * height

        when (obs.type) {
          ObstacleType.COIN_PACK -> {
            drawCircle(
              color = TaxiYellow,
              radius = 16f,
              center = Offset(obsX, obsY)
            )
            drawCircle(
              color = SanaaGold,
              radius = 11f,
              center = Offset(obsX, obsY)
            )
            safeDrawText(
              textMeasurer = textMeasurer,
              text = "🪙",
              topLeft = Offset(obsX - 14f, obsY - 18f),
              style = TextStyle(fontSize = 18.sp),
              allocatedWidth = 40f,
              allocatedHeight = 40f
            )
          }
          ObstacleType.SPICE_CART -> {
            // Traditional Yemeni Spice Cart
            drawRoundRect(
              color = SanaaMudWarm,
              topLeft = Offset(obsX - 35f, obsY - 20f),
              size = Size(70f, 40f),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
            )
            drawCircle(color = SanaaOcher, radius = 10f, center = Offset(obsX - 15f, obsY))
            drawCircle(color = TaxiYellow, radius = 10f, center = Offset(obsX + 15f, obsY))
            safeDrawText(
              textMeasurer = textMeasurer,
              text = "توابل 🏺",
              topLeft = Offset(obsX - 25f, obsY - 14f),
              style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold),
              allocatedWidth = 60f,
              allocatedHeight = 30f
            )
          }
          ObstacleType.MUD_BARRICADE -> {
            drawRoundRect(
              color = SanaaClay,
              topLeft = Offset(obsX - 38f, obsY - 16f),
              size = Size(76f, 32f),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            // Decorative Mud Brick Lines
            drawLine(
              color = Color.White.copy(alpha = 0.5f),
              start = Offset(obsX - 38f, obsY),
              end = Offset(obsX + 38f, obsY),
              strokeWidth = 2f
            )
            safeDrawText(
              textMeasurer = textMeasurer,
              text = "جدار طيني 🧱",
              topLeft = Offset(obsX - 30f, obsY - 12f),
              style = TextStyle(fontSize = 10.sp, color = Color.White),
              allocatedWidth = 70f,
              allocatedHeight = 25f
            )
          }
          ObstacleType.LOW_ARCH -> {
            // Archway requiring slide
            drawRoundRect(
              color = DarkSurfaceVariant,
              topLeft = Offset(obsX - 45f, obsY - 25f),
              size = Size(90f, 50f),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
            )
            safeDrawText(
              textMeasurer = textMeasurer,
              text = "قوس قمري ⚠️",
              topLeft = Offset(obsX - 35f, obsY - 14f),
              style = TextStyle(fontSize = 11.sp, color = SanaaQamariyaCyan, fontWeight = FontWeight.Bold),
              allocatedWidth = 80f,
              allocatedHeight = 30f
            )
          }
          ObstacleType.POLICE_SPIKES -> {
            drawRect(
              color = PoliceRedLight,
              topLeft = Offset(obsX - 40f, obsY - 8f),
              size = Size(80f, 16f)
            )
            safeDrawText(
              textMeasurer = textMeasurer,
              text = "حاجز أمني 🚨",
              topLeft = Offset(obsX - 32f, obsY - 12f),
              style = TextStyle(fontSize = 10.sp, color = Color.White),
              allocatedWidth = 75f,
              allocatedHeight = 25f
            )
          }
          ObstacleType.SCOUT_HELPER -> {
            drawCircle(
              color = GangNeonGreen,
              radius = 18f,
              center = Offset(obsX, obsY)
            )
            safeDrawText(
              textMeasurer = textMeasurer,
              text = "🎒",
              topLeft = Offset(obsX - 14f, obsY - 16f),
              style = TextStyle(fontSize = 18.sp),
              allocatedWidth = 40f,
              allocatedHeight = 40f
            )
          }
        }
      }
    }

    // 4. Draw Player Character
    val playerX = laneCenterX[playerLane]
    var playerY = height * 0.78f
    val playerScale = if (isJumping) 1.35f else if (isSliding) 0.65f else 1.0f

    if (isJumping) {
      playerY -= 50f
    }

    // Player Shadow
    drawOval(
      color = Color.Black.copy(alpha = 0.4f),
      topLeft = Offset(playerX - 25f * playerScale, height * 0.81f),
      size = Size(50f * playerScale, 18f * playerScale)
    )

    // Adrenaline Glow
    if (adrenalineBoost) {
      drawCircle(
        color = SanaaGold.copy(alpha = 0.35f),
        radius = 45f * playerScale,
        center = Offset(playerX, playerY)
      )
    }

    // Character Avatar
    val charEmoji = if (faction == Faction.GANG) "👑" else "👮‍♂️"
    val charBgColor = if (faction == Faction.GANG) GangShawlRed else PoliceAccent

    drawCircle(
      color = charBgColor,
      radius = 28f * playerScale,
      center = Offset(playerX, playerY)
    )
    drawCircle(
      color = Color.White,
      radius = 24f * playerScale,
      center = Offset(playerX, playerY)
    )

    safeDrawText(
      textMeasurer = textMeasurer,
      text = charEmoji,
      topLeft = Offset(playerX - 18f * playerScale, playerY - 22f * playerScale),
      style = TextStyle(fontSize = (28 * playerScale).sp),
      allocatedWidth = 45f * playerScale,
      allocatedHeight = 45f * playerScale
    )

    // 5. Draw Particles
    particles.forEach { p ->
      drawCircle(
        color = p.color.copy(alpha = p.alpha),
        radius = p.size,
        center = Offset(p.x * width, p.y * height)
      )
    }

    // 6. Visual Fullscreen Overlays for Graffiti & Smoke
    if (activeGraffiti) {
      drawRect(
        brush = Brush.radialGradient(
          colors = listOf(
            GangGraffitiPink.copy(alpha = 0.75f),
            SanaaQamariyaCyan.copy(alpha = 0.6f),
            Color.Transparent
          ),
          center = Offset(width * 0.5f, height * 0.5f),
          radius = width * 0.7f
        ),
        size = Size(width, height)
      )
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "⚡ غرافيتي المشاغبين ⚡",
        topLeft = Offset(width * 0.25f, height * 0.35f),
        style = TextStyle(
          color = Color.White,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        ),
        allocatedWidth = width * 0.6f,
        allocatedHeight = 60f
      )
    }

    if (activeSmoke) {
      drawRect(
        color = Color.White.copy(alpha = 0.65f),
        size = Size(width, height)
      )
      safeDrawText(
        textMeasurer = textMeasurer,
        text = "💨 ستار دخاني أمني 💨",
        topLeft = Offset(width * 0.28f, height * 0.35f),
        style = TextStyle(
          color = PoliceNavy,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        ),
        allocatedWidth = width * 0.6f,
        allocatedHeight = 60f
      )
    }
  }
}

fun DrawScope.drawSanaaArchitectureBackground(width: Float, height: Float, distance: Float) {
  // Deep Night Sana'a Sky
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(DarkBg, Color(0xFF16192E), SanaaMudWarm.copy(alpha = 0.4f))
    ),
    size = Size(width, height)
  )

  // Mud Tower Silhouettes on Left and Right Sides of the Alley
  val buildingWidth = width * 0.22f
  val buildingHeight = height * 0.95f

  // Left Mud Tower
  drawRect(
    color = SanaaMudWarm,
    topLeft = Offset(0f, 0f),
    size = Size(buildingWidth, buildingHeight)
  )
  // Right Mud Tower
  drawRect(
    color = SanaaMudWarm,
    topLeft = Offset(width - buildingWidth, 0f),
    size = Size(buildingWidth, buildingHeight)
  )

  // Qamariya Arch Windows (Traditional Yemeni Stained Glass Arch)
  val scrollOffset = (distance * 0.5f) % 120f
  for (row in 0..8) {
    val winY = (row * 120f - scrollOffset)
    if (winY in -50f..height) {
      // Left Qamariya
      drawQamariyaWindow(
        center = Offset(buildingWidth * 0.5f, winY),
        radius = 16f
      )
      // Right Qamariya
      drawQamariyaWindow(
        center = Offset(width - buildingWidth * 0.5f, winY),
        radius = 16f
      )
    }
  }
}

fun DrawScope.drawQamariyaWindow(center: Offset, radius: Float) {
  // White plaster arch framing
  drawCircle(
    color = Color.White,
    radius = radius + 3f,
    center = center
  )
  // Colored Stained Glass Segments
  drawCircle(
    color = SanaaQamariyaCyan,
    radius = radius,
    center = center
  )
  drawCircle(
    color = SanaaQamariyaRuby,
    radius = radius * 0.5f,
    center = center
  )
}
