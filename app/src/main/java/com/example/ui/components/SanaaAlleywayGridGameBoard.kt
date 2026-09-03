package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import com.example.ui.sanaa7d.GameDifficulty
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Represents the type of cell in the Sana'a Alleyway grid game board.
 */
enum class AlleyCellType {
  COBBLESTONE_PATH,       // Traditional basalt and clay stone alley floor
  CLAY_TOWER_WALL,        // Historic multi-story mudbrick tower house wall
  QAMARIYA_ARCHWAY,       // Stained glass Qamariya arch passage
  WOODEN_HERITAGE_DOOR,   // Carved Yemeni wooden door (Hiding spot)
  SPICE_SOUQ_STALL,       // Market spices & pottery obstacle
  ROOFTOP_LADDER          // Parkour escape ladder
}

/**
 * Character State for State-Based Animation
 */
enum class CharacterMovementAnimState {
  IDLE,
  WALKING,
  RUNNING,
  JUMPING,
  SNEAKING_IN_SHADOWS,
  COLLIDED_STUNNED
}

/**
 * Temporary Power-up Items that spawn on the game board
 */
enum class GridPowerUpType(
  val titleAr: String,
  val iconEmoji: String,
  val durationSeconds: Int,
  val color: Color,
  val descAr: String
) {
  SPEED_BOOST("سرعة مضاعفة ⚡", "⚡", 6, TaxiYellow, "حذاء السرعة الصنعاني الخارق لمراوغة الدوريات"),
  INVISIBILITY("بردة التخفي 🥷", "🥷", 7, GangNeonGreen, "تخفٍ كامل وكسر خط نظر الشرطة في الأزقة"),
  POLICE_FREEZE("تجميد الدوريات ❄️", "❄️", 5, Color(0xFF00E5FF), "شل حركة سيارات ورجال الشرطة بالكامل"),
  GOLDEN_JANBIYA("الجنبية الذهبية 🗡️", "🗡️", 8, SanaaGold, "مضاعفة النقاط والغنائم x2")
}

data class GridPowerUpItem(
  val type: GridPowerUpType,
  val pos: GridPos,
  val spawnEpoch: Long = System.currentTimeMillis()
)

/**
 * 2D Grid Coordinate for the Alleyway Board
 */
data class GridPos(val row: Int, val col: Int)

/**
 * A Composable function that renders a grid-based representation of a traditional
 * Sana'a alleyway using basic shapes, serving as the main game board.
 * Includes state-based character movement animations, temporary power-ups, haptic feedback,
 * and collision feedback between pranksters and police.
 */
@Composable
fun SanaaAlleywayGridGameBoard(
  difficulty: GameDifficulty,
  onCollisionDetected: (String, Int) -> Unit,
  onScoreEarned: (Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val gridRows = 7
  val gridCols = 5

  // Game Board Grid Layout (Traditional Sana'a Alleyways Map)
  val alleywayGrid = remember {
    listOf(
      listOf(AlleyCellType.CLAY_TOWER_WALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.QAMARIYA_ARCHWAY, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.CLAY_TOWER_WALL),
      listOf(AlleyCellType.WOODEN_HERITAGE_DOOR, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.CLAY_TOWER_WALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.SPICE_SOUQ_STALL),
      listOf(AlleyCellType.CLAY_TOWER_WALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.CLAY_TOWER_WALL),
      listOf(AlleyCellType.SPICE_SOUQ_STALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.ROOFTOP_LADDER, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.WOODEN_HERITAGE_DOOR),
      listOf(AlleyCellType.CLAY_TOWER_WALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.CLAY_TOWER_WALL),
      listOf(AlleyCellType.WOODEN_HERITAGE_DOOR, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.CLAY_TOWER_WALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.SPICE_SOUQ_STALL),
      listOf(AlleyCellType.CLAY_TOWER_WALL, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.QAMARIYA_ARCHWAY, AlleyCellType.COBBLESTONE_PATH, AlleyCellType.CLAY_TOWER_WALL)
    )
  }

  // Player Prankster Position & Movement State
  var playerPos by remember { mutableStateOf(GridPos(row = 6, col = 2)) }
  var playerAnimState by remember { mutableStateOf(CharacterMovementAnimState.IDLE) }
  var isPlayerInHidingSpot by remember { mutableStateOf(false) }

  // Active Power-ups State
  var activePowerUps by remember { mutableStateOf(listOf<GridPowerUpItem>()) }
  var currentActivePowerUp by remember { mutableStateOf<GridPowerUpType?>(null) }
  var powerUpTimeRemainingSeconds by remember { mutableIntStateOf(0) }
  var powerUpBannerMessage by remember { mutableStateOf<String?>(null) }

  // Police AI Characters Position
  var policePos1 by remember { mutableStateOf(GridPos(row = 0, col = 1)) }
  var policePos2 by remember { mutableStateOf(GridPos(row = 2, col = 3)) }

  // Active Collectibles on Grid (Old Sana'a Coins & Qat Leaves)
  var collectibleCoins by remember {
    mutableStateOf(setOf(GridPos(1, 1), GridPos(3, 3), GridPos(5, 1), GridPos(4, 2)))
  }

  // Collision and Stun State
  var collisionFeedbackMessage by remember { mutableStateOf<String?>(null) }
  var collisionFlashActive by remember { mutableStateOf(false) }
  var totalCollisionsAvoided by remember { mutableIntStateOf(0) }

  // Police AI Speed Tick based on Difficulty & Active Freeze Power-up
  val baseStepInterval = (1200 / difficulty.policeSpeedMultiplier).toLong()
  val policeStepIntervalMs = if (currentActivePowerUp == GridPowerUpType.POLICE_FREEZE) 999999L else baseStepInterval

  // Power-Up Countdown Timer Loop
  LaunchedEffect(currentActivePowerUp) {
    if (currentActivePowerUp != null && powerUpTimeRemainingSeconds > 0) {
      while (powerUpTimeRemainingSeconds > 0) {
        delay(1000)
        powerUpTimeRemainingSeconds--
      }
      currentActivePowerUp = null
      powerUpBannerMessage = null
    }
  }

  // Periodic Power-Up Spawner on Grid
  LaunchedEffect(Unit) {
    val spawnSpots = listOf(GridPos(2, 1), GridPos(2, 3), GridPos(4, 1), GridPos(4, 3), GridPos(0, 3), GridPos(6, 1))
    while (true) {
      delay(8000)
      if (activePowerUps.size < 2) {
        val availableSpots = spawnSpots.filter { spot ->
          spot != playerPos && spot != policePos1 && spot != policePos2 && activePowerUps.none { it.pos == spot }
        }
        if (availableSpots.isNotEmpty()) {
          val randomType = GridPowerUpType.values().random()
          val randomSpot = availableSpots.random()
          activePowerUps = activePowerUps + GridPowerUpItem(randomType, randomSpot)
        }
      }
    }
  }

  // Police AI Game Loop
  LaunchedEffect(difficulty, playerPos, isPlayerInHidingSpot, currentActivePowerUp) {
    while (true) {
      if (currentActivePowerUp == GridPowerUpType.POLICE_FREEZE) {
        delay(1000)
        continue
      }
      delay(policeStepIntervalMs)

      val isEffectivelyHidden = isPlayerInHidingSpot || currentActivePowerUp == GridPowerUpType.INVISIBILITY

      // Move Police 1 towards Player if not hiding/invisible, else patrol
      if (!isEffectivelyHidden) {
        policePos1 = calculateNextPoliceStep(policePos1, playerPos, alleywayGrid, gridRows, gridCols)
        if (difficulty != GameDifficulty.EASY) {
          policePos2 = calculateNextPoliceStep(policePos2, playerPos, alleywayGrid, gridRows, gridCols)
        }
      } else {
        // Random patrol when player is hiding or invisible
        policePos1 = getAdjacentWalkable(policePos1, alleywayGrid, gridRows, gridCols)
        policePos2 = getAdjacentWalkable(policePos2, alleywayGrid, gridRows, gridCols)
      }

      // Check for Collisions between Prankster and Police
      if (!isEffectivelyHidden && (policePos1 == playerPos || policePos2 == playerPos)) {
        playerAnimState = CharacterMovementAnimState.COLLIDED_STUNNED
        collisionFlashActive = true
        HapticManager.vibrateCollision()
        GameSoundEffects.playPunch()

        val penalty = (60 * difficulty.policeSpeedMultiplier).toInt()
        collisionFeedbackMessage = "💥 تصادم مع دورية الشرطة الصنعانية! (-$penalty نقطة)"
        onCollisionDetected("POLICE_COLLISION", penalty)

        // Reset player to safe alley entrance after brief stun
        delay(600)
        collisionFlashActive = false
        delay(400)
        playerPos = GridPos(6, 2)
        playerAnimState = CharacterMovementAnimState.IDLE
        collisionFeedbackMessage = null
      }
    }
  }

  // State-Based Animations (Bobbing, Lean, Footstep strides, Stun flash)
  val infiniteTransition = rememberInfiniteTransition(label = "grid_chase_infinite_anim")
  val characterBobbing by infiniteTransition.animateFloat(
    initialValue = -3f,
    targetValue = 3f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bobbing_anim"
  )

  val pulseGlow by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_anim"
  )

  fun handlePlayerMoveTo(newPos: GridPos, isHiding: Boolean) {
    playerPos = newPos
    isPlayerInHidingSpot = isHiding
    playerAnimState = CharacterMovementAnimState.RUNNING
    HapticManager.vibrateMovement()
    GameSoundEffects.playFootstep()

    // Check Coin Pickup
    if (collectibleCoins.contains(newPos)) {
      collectibleCoins = collectibleCoins - newPos
      val basePts = 50
      val earnedPts = if (currentActivePowerUp == GridPowerUpType.GOLDEN_JANBIYA) basePts * 2 else basePts
      HapticManager.vibratePowerUp()
      GameSoundEffects.playCoin()
      onScoreEarned(earnedPts, "التقاط كنز في أزقة صنعاء 🪙 (x${if (currentActivePowerUp == GridPowerUpType.GOLDEN_JANBIYA) "2" else "1"})")
    }

    // Check Power-Up Pickup
    val collectedPowerUp = activePowerUps.find { it.pos == newPos }
    if (collectedPowerUp != null) {
      activePowerUps = activePowerUps.filter { it.pos != newPos }
      currentActivePowerUp = collectedPowerUp.type
      powerUpTimeRemainingSeconds = collectedPowerUp.type.durationSeconds
      powerUpBannerMessage = "✨ تفعيل: ${collectedPowerUp.type.titleAr} (${collectedPowerUp.type.descAr})"
      HapticManager.vibrateSuccess()
      GameSoundEffects.playNitroBoost()
      onScoreEarned(100, "الحصول على ميزة: ${collectedPowerUp.type.titleAr} ⚡")
    }

    // Avoid near-miss points
    if (isAdjacent(newPos, policePos1) || isAdjacent(newPos, policePos2)) {
      totalCollisionsAvoided++
      val pts = if (currentActivePowerUp == GridPowerUpType.GOLDEN_JANBIYA) 150 else 75
      onScoreEarned(pts, "مراوغة خاطفة لدورية صنعانية! 🏃‍♂️💨")
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("sanaa_alleyway_grid_board"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // ----------------------------------------------------
    // Difficulty & Active Power-Up Status Bar Header
    // ----------------------------------------------------
    Surface(
      color = DarkSurface.copy(alpha = 0.95f),
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.2.dp, SanaaGold.copy(alpha = 0.8f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 4.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Speed, contentDescription = null, tint = TaxiYellow, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "المستوى: ${difficulty.titleAr}",
            color = SanaaGold,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Active Power-Up Countdown Indicator
        if (currentActivePowerUp != null) {
          Surface(
            color = currentActivePowerUp!!.color.copy(alpha = 0.25f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentActivePowerUp!!.color)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(currentActivePowerUp!!.iconEmoji, fontSize = 11.sp)
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "${currentActivePowerUp!!.titleAr} (${powerUpTimeRemainingSeconds}ث)",
                color = currentActivePowerUp!!.color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
              )
            }
          }
        } else {
          Surface(
            color = if (isPlayerInHidingSpot) GangNeonGreen.copy(alpha = 0.2f) else Color(0xFFFF1744).copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isPlayerInHidingSpot) GangNeonGreen else Color(0xFFFF1744)
            )
          ) {
            Text(
              text = if (isPlayerInHidingSpot) "مختبئ في باب عتيق 🚪" else "مطارد في الأزقة 🏃‍♂️",
              color = if (isPlayerInHidingSpot) GangNeonGreen else Color(0xFFFF5252),
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }

    // Power-Up Activation Banner
    powerUpBannerMessage?.let { msg ->
      Surface(
        color = Color(0xFF0F2E1B),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GangNeonGreen),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp)
          .testTag("powerup_active_banner")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = msg, color = GangNeonGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // ----------------------------------------------------
    // Collision Feedback Alert Banner
    // ----------------------------------------------------
    collisionFeedbackMessage?.let { feedbackMsg ->
      Surface(
        color = Color(0xFF4A0A0A),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF1744)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 4.dp)
          .testTag("grid_collision_feedback_alert")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = feedbackMsg,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }

    // ----------------------------------------------------
    // Traditional Sana'a Alleyway Grid Canvas & Cells
    // ----------------------------------------------------
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1.05f)
        .clip(RoundedCornerShape(14.dp))
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF2A1C14), Color(0xFF140D09)),
            radius = 600f
          )
        )
        .border(
          width = if (collisionFlashActive) 3.dp else 1.5.dp,
          color = if (collisionFlashActive) Color(0xFFFF1744) else SanaaGold.copy(alpha = 0.85f),
          shape = RoundedCornerShape(14.dp)
        )
        .padding(6.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
      ) {
        for (r in 0 until gridRows) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            for (c in 0 until gridCols) {
              val cellType = alleywayGrid[r][c]
              val currentPos = GridPos(r, c)
              val isPlayerHere = (playerPos == currentPos)
              val isPolice1Here = (policePos1 == currentPos)
              val isPolice2Here = (policePos2 == currentPos && difficulty != GameDifficulty.EASY)
              val hasCoin = collectibleCoins.contains(currentPos)
              val powerUpHere = activePowerUps.find { it.pos == currentPos }

              SanaaAlleyCell(
                cellType = cellType,
                isPlayerHere = isPlayerHere,
                isPoliceHere = isPolice1Here || isPolice2Here,
                hasCoin = hasCoin,
                powerUpItem = powerUpHere,
                playerAnimState = playerAnimState,
                characterBobbing = characterBobbing,
                pulseGlow = pulseGlow,
                isInvisible = (currentActivePowerUp == GridPowerUpType.INVISIBILITY),
                onClick = {
                  // Interactive Move when adjacent
                  if (isAdjacent(playerPos, currentPos) && cellType != AlleyCellType.CLAY_TOWER_WALL) {
                    val isHiding = (cellType == AlleyCellType.WOODEN_HERITAGE_DOOR)
                    handlePlayerMoveTo(currentPos, isHiding)
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(1f)
                  .padding(2.dp)
              )
            }
          }
        }
      }
    }

    // ----------------------------------------------------
    // Directional D-Pad Controls for Grid Navigation
    // ----------------------------------------------------
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "⚡ اجمع المعززات (السرعة، التخفي، التجميد) لتفادي الشرطة!",
        color = Color.LightGray,
        fontSize = 8.5.sp,
        modifier = Modifier.weight(1f)
      )

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        DirectionButton("⬆️") {
          movePlayer(playerPos.row - 1, playerPos.col, alleywayGrid, gridRows, gridCols) { newPos, isHiding ->
            handlePlayerMoveTo(newPos, isHiding)
          }
        }
        DirectionButton("⬇️") {
          movePlayer(playerPos.row + 1, playerPos.col, alleywayGrid, gridRows, gridCols) { newPos, isHiding ->
            handlePlayerMoveTo(newPos, isHiding)
          }
        }
        DirectionButton("⬅️") {
          movePlayer(playerPos.row, playerPos.col - 1, alleywayGrid, gridRows, gridCols) { newPos, isHiding ->
            handlePlayerMoveTo(newPos, isHiding)
          }
        }
        DirectionButton("➡️") {
          movePlayer(playerPos.row, playerPos.col + 1, alleywayGrid, gridRows, gridCols) { newPos, isHiding ->
            handlePlayerMoveTo(newPos, isHiding)
          }
        }
      }
    }
  }
}

@Composable
private fun DirectionButton(
  symbol: String,
  onClick: () -> Unit
) {
  Surface(
    color = DarkSurface,
    shape = RoundedCornerShape(8.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SanaaGold),
    modifier = Modifier
      .size(32.dp)
      .clickable { onClick() }
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(text = symbol, fontSize = 14.sp)
    }
  }
}

/**
 * Individual Grid Cell representing a Sana'a Alleyway architecture element using basic shapes.
 */
@Composable
private fun SanaaAlleyCell(
  cellType: AlleyCellType,
  isPlayerHere: Boolean,
  isPoliceHere: Boolean,
  hasCoin: Boolean,
  powerUpItem: GridPowerUpItem?,
  playerAnimState: CharacterMovementAnimState,
  characterBobbing: Float,
  pulseGlow: Float,
  isInvisible: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    // Custom Canvas rendering basic geometric shapes representing Sana'a architecture
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      when (cellType) {
        AlleyCellType.COBBLESTONE_PATH -> {
          // Basalt & clay cobblestone paving stones
          drawRoundRect(
            color = Color(0xFF3B2A20),
            size = size,
            cornerRadius = CornerRadius(8f, 8f)
          )
          // Paver pattern lines
          drawRoundRect(
            color = Color(0xFF241810),
            size = Size(w * 0.85f, h * 0.85f),
            topLeft = Offset(w * 0.075f, h * 0.075f),
            style = Stroke(width = 1.5f),
            cornerRadius = CornerRadius(6f, 6f)
          )
        }

        AlleyCellType.CLAY_TOWER_WALL -> {
          // Sana'ani Mudbrick Tower Wall with traditional white gypsum geometric borders
          drawRoundRect(
            color = Color(0xFF6B3E26),
            size = size,
            cornerRadius = CornerRadius(6f, 6f)
          )
          // White gypsum trim (تخاريم جصية)
          drawRect(
            color = Color(0xFFFFF9E6),
            topLeft = Offset(0f, 0f),
            size = Size(w, h * 0.18f)
          )
          drawRect(
            color = Color(0xFFFFF9E6),
            topLeft = Offset(0f, h * 0.82f),
            size = Size(w, h * 0.18f)
          )
          // Geometric triangles
          val path = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.25f)
            lineTo(w * 0.8f, h * 0.5f)
            close()
          }
          drawPath(path, color = Color(0xFF8B4513))
        }

        AlleyCellType.QAMARIYA_ARCHWAY -> {
          // Vibrant Stained Glass Qamariya semi-circle arch
          drawRoundRect(
            color = Color(0xFF2E1A11),
            size = size,
            cornerRadius = CornerRadius(8f, 8f)
          )
          // Colored stained glass arcs (Blue, Red, Amber, Green)
          drawArc(
            color = Color(0xFF1E88E5),
            startAngle = 180f,
            sweepAngle = 45f,
            useCenter = true,
            size = Size(w * 0.8f, h * 0.8f),
            topLeft = Offset(w * 0.1f, h * 0.1f)
          )
          drawArc(
            color = Color(0xFFE53935),
            startAngle = 225f,
            sweepAngle = 45f,
            useCenter = true,
            size = Size(w * 0.8f, h * 0.8f),
            topLeft = Offset(w * 0.1f, h * 0.1f)
          )
          drawArc(
            color = Color(0xFFFFB300),
            startAngle = 270f,
            sweepAngle = 45f,
            useCenter = true,
            size = Size(w * 0.8f, h * 0.8f),
            topLeft = Offset(w * 0.1f, h * 0.1f)
          )
          drawArc(
            color = Color(0xFF43A047),
            startAngle = 315f,
            sweepAngle = 45f,
            useCenter = true,
            size = Size(w * 0.8f, h * 0.8f),
            topLeft = Offset(w * 0.1f, h * 0.1f)
          )
        }

        AlleyCellType.WOODEN_HERITAGE_DOOR -> {
          // Traditional Carved Brown Wooden Door with brass knockers
          drawRoundRect(
            color = Color(0xFF4E2A12),
            size = size,
            cornerRadius = CornerRadius(6f, 6f)
          )
          drawRoundRect(
            color = Color(0xFF331B0A),
            size = Size(w * 0.75f, h * 0.75f),
            topLeft = Offset(w * 0.125f, h * 0.125f),
            cornerRadius = CornerRadius(4f, 4f)
          )
          // Brass knocker dots
          drawCircle(color = SanaaGold, radius = 3.5f, center = Offset(w * 0.35f, h * 0.5f))
          drawCircle(color = SanaaGold, radius = 3.5f, center = Offset(w * 0.65f, h * 0.5f))
        }

        AlleyCellType.SPICE_SOUQ_STALL -> {
          // Souq spice bags & pots
          drawRoundRect(
            color = Color(0xFF4A3528),
            size = size,
            cornerRadius = CornerRadius(6f, 6f)
          )
          // Spice piles (Cumin yellow, Paprika red)
          drawCircle(color = Color(0xFFFFB300), radius = w * 0.2f, center = Offset(w * 0.35f, h * 0.45f))
          drawCircle(color = Color(0xFFD84315), radius = w * 0.2f, center = Offset(w * 0.65f, h * 0.55f))
        }

        AlleyCellType.ROOFTOP_LADDER -> {
          // Escape ladder to rooftops
          drawRoundRect(
            color = Color(0xFF261912),
            size = size,
            cornerRadius = CornerRadius(6f, 6f)
          )
          // Ladder rails
          drawLine(color = Color(0xFFBCAAA4), start = Offset(w * 0.3f, h * 0.1f), end = Offset(w * 0.3f, h * 0.9f), strokeWidth = 2.5f)
          drawLine(color = Color(0xFFBCAAA4), start = Offset(w * 0.7f, h * 0.1f), end = Offset(w * 0.7f, h * 0.9f), strokeWidth = 2.5f)
          // Rungs
          for (i in 1..4) {
            val yPos = h * (0.2f * i)
            drawLine(color = Color(0xFFBCAAA4), start = Offset(w * 0.3f, yPos), end = Offset(w * 0.7f, yPos), strokeWidth = 2f)
          }
        }
      }
    }

    // Power-Up Item Display on Grid
    if (powerUpItem != null && !isPlayerHere) {
      Surface(
        color = powerUpItem.type.color.copy(alpha = 0.3f),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, powerUpItem.type.color),
        modifier = Modifier.size(22.dp).scale(pulseGlow)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(text = powerUpItem.type.iconEmoji, fontSize = 11.sp)
        }
      }
    }

    // Collectible Coin Overlay
    if (hasCoin && !isPlayerHere && powerUpItem == null) {
      Text(
        text = "🪙",
        fontSize = 12.sp,
        modifier = Modifier.scale(pulseGlow)
      )
    }

    // ----------------------------------------------------
    // State-Based Character Movement Animations
    // ----------------------------------------------------
    if (isPlayerHere) {
      val isCollided = (playerAnimState == CharacterMovementAnimState.COLLIDED_STUNNED)
      Box(
        modifier = Modifier
          .offset(y = if (isCollided) 0.dp else characterBobbing.dp)
          .scale(if (isCollided) 1.25f else 1.0f)
          .testTag("prankster_grid_character"),
        contentAlignment = Alignment.Center
      ) {
        if (isCollided) {
          Text("💥😵", fontSize = 18.sp)
        } else {
          Surface(
            color = if (isInvisible) Color(0xFF4A148C).copy(alpha = 0.45f) else GangNeonGreen.copy(alpha = 0.35f),
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, if (isInvisible) Color(0xFFE040FB) else GangNeonGreen),
            modifier = Modifier.size(24.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(if (isInvisible) "🥷" else "🏃‍♂️", fontSize = 14.sp)
            }
          }
        }
      }
    }

    // Police AI Patrol Character
    if (isPoliceHere && !isPlayerHere) {
      Box(
        modifier = Modifier
          .offset(y = (-characterBobbing).dp)
          .testTag("police_grid_character"),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          color = Color(0xFFFF1744).copy(alpha = 0.35f),
          shape = CircleShape,
          border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFF1744)),
          modifier = Modifier.size(24.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("👮‍♂️", fontSize = 14.sp)
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// Helper Grid Calculations & Pathfinding
// ----------------------------------------------------
private fun isAdjacent(p1: GridPos, p2: GridPos): Boolean {
  val dRow = abs(p1.row - p2.row)
  val dCol = abs(p1.col - p2.col)
  return (dRow == 1 && dCol == 0) || (dRow == 0 && dCol == 1)
}

private fun movePlayer(
  targetRow: Int,
  targetCol: Int,
  grid: List<List<AlleyCellType>>,
  rows: Int,
  cols: Int,
  onMove: (GridPos, Boolean) -> Unit
) {
  if (targetRow in 0 until rows && targetCol in 0 until cols) {
    val cell = grid[targetRow][targetCol]
    if (cell != AlleyCellType.CLAY_TOWER_WALL) {
      val isHiding = (cell == AlleyCellType.WOODEN_HERITAGE_DOOR)
      onMove(GridPos(targetRow, targetCol), isHiding)
    }
  }
}

private fun getAdjacentWalkable(
  current: GridPos,
  grid: List<List<AlleyCellType>>,
  rows: Int,
  cols: Int
): GridPos {
  val neighbors = listOf(
    GridPos(current.row - 1, current.col),
    GridPos(current.row + 1, current.col),
    GridPos(current.row, current.col - 1),
    GridPos(current.row, current.col + 1)
  ).filter {
    it.row in 0 until rows && it.col in 0 until cols && grid[it.row][it.col] != AlleyCellType.CLAY_TOWER_WALL
  }
  return neighbors.randomOrNull() ?: current
}

private fun calculateNextPoliceStep(
  police: GridPos,
  target: GridPos,
  grid: List<List<AlleyCellType>>,
  rows: Int,
  cols: Int
): GridPos {
  val candidates = listOf(
    GridPos(police.row - 1, police.col),
    GridPos(police.row + 1, police.col),
    GridPos(police.row, police.col - 1),
    GridPos(police.row, police.col + 1)
  ).filter {
    it.row in 0 until rows && it.col in 0 until cols && grid[it.row][it.col] != AlleyCellType.CLAY_TOWER_WALL
  }

  // Pick step closest in Manhattan distance to target
  return candidates.minByOrNull {
    abs(it.row - target.row) + abs(it.col - target.col)
  } ?: police
}
