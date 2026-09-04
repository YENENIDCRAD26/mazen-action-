package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.example.sound.HapticManager
import kotlin.math.*

/**
 * 3D Third-Person Perspective Canvas for Unified GTA San Andreas Sana'a.
 * Faithfully matches the user's provided screenshots:
 * - Asphalt road with double solid yellow center lines
 * - Sidewalk with red-and-white curb blocks
 * - Lush 3D shaded leafy street trees
 * - Tall lampposts with warm volumetric light cones
 * - 3D player rendered from behind with cowboy hat, bandana, green gangster shirt with back emblem
 * - Moving civilian traffic & police patrols
 * - In-vehicle mode, projectile effects, jump physics
 */
@Composable
fun UnifiedGtaGameCanvas(
  hero: UnifiedHeroId,
  weapon: WeaponItem,
  playerX: Float, // -1f (left sidewalk) to 1f (right sidewalk)
  playerZ: Float, // forward distance progression
  playerAngleDeg: Float,
  isWalking: Boolean,
  isRunning: Boolean,
  jumpHeight: Float, // 0f (on ground) to 1f (peak jump)
  isInsideVehicle: Boolean,
  projectiles: List<GameProjectile>,
  trafficVehicles: List<TrafficCar>,
  modifier: Modifier = Modifier,
  onCameraRotate: (deltaDeg: Float) -> Unit = {}
) {
  // Animation frame counter for walking stride and tire rotation
  var walkCycleTick by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(isWalking, isRunning, isInsideVehicle) {
    while (true) {
      withFrameNanos {
        val speedMultiplier = when {
          isInsideVehicle -> 3.0f
          isRunning -> 2.2f
          isWalking -> 1.0f
          else -> 0f
        }
        if (speedMultiplier > 0f) {
          walkCycleTick = (walkCycleTick + 0.18f * speedMultiplier) % (2f * PI.toFloat())
        }
      }
    }
  }

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .pointerInput(Unit) {
        // Touch drag across screen allows camera orbit/panning
        detectDragGestures { change, dragAmount ->
          change.consume()
          onCameraRotate(dragAmount.x * 0.4f)
        }
      }
  ) {
    val canvasW = size.width
    val canvasH = size.height
    val horizonY = canvasH * 0.38f // 3D horizon line

    // 1. Evening Sky Gradient (Matching Screenshots 1 & 2 - around 21:13 twilight)
    val skyGradient = Brush.verticalGradient(
      colors = listOf(
        Color(0xFF0B101E), // Deep night blue
        Color(0xFF1B273D), // Twilight navy
        Color(0xFF334155), // Horizon dusk glow
        Color(0xFF475569)
      ),
      startY = 0f,
      endY = horizonY
    )
    drawRect(brush = skyGradient, size = Size(canvasW, horizonY))

    // Distant mountain silhouette (Jabal Nuqum & Ayban)
    drawDistantMountains(canvasW, horizonY)

    // 2. 3D Perspective Roadway Ground
    drawPerspectiveRoadAndSidewalks(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ
    )

    // 3. Buildings & Architecture on both sides of street
    drawBuildingsAlongStreet(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ
    )

    // 4. Street Trees & Volumetric Streetlights (Matching Screenshot 1)
    drawTreesAndStreetlights(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ
    )

    // 5. Traffic Vehicles on Road (Civilian Cars, Taxi Dababs, Police)
    drawTrafficVehicles(
      vehicles = trafficVehicles,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ
    )

    // 6. Projectiles (Bullets, Slingshot Rocks, Firecrackers)
    drawProjectiles(
      projectiles = projectiles,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY
    )

    // 7. Player Character / Vehicle (Centered in Third-Person Perspective)
    drawPlayerCharacter(
      hero = hero,
      playerX = playerX,
      jumpHeight = jumpHeight,
      walkCycle = walkCycleTick,
      isInsideVehicle = isInsideVehicle,
      playerAngleDeg = playerAngleDeg,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY
    )
  }
}

// ----------------------------------------------------
// Projectile Model
// ----------------------------------------------------
data class GameProjectile(
  val id: Long,
  var screenX: Float,
  var screenY: Float,
  val velocityY: Float,
  val type: String,
  var lifeTime: Float = 1.0f
)

// ----------------------------------------------------
// Traffic Vehicle Model
// ----------------------------------------------------
data class TrafficCar(
  val id: Int,
  var worldZ: Float,
  val laneIndex: Int, // -1: oncoming left lane, 1: forward right lane
  val carType: String, // "SEDAN", "DABAB", "POLICE"
  val color: Color
)

// ----------------------------------------------------
// Canvas Drawing Helper Functions
// ----------------------------------------------------

private fun DrawScope.drawDistantMountains(canvasW: Float, horizonY: Float) {
  val mountainPath = Path().apply {
    moveTo(0f, horizonY)
    lineTo(canvasW * 0.15f, horizonY - 45f)
    lineTo(canvasW * 0.35f, horizonY - 25f)
    lineTo(canvasW * 0.55f, horizonY - 60f)
    lineTo(canvasW * 0.78f, horizonY - 30f)
    lineTo(canvasW, horizonY - 50f)
    lineTo(canvasW, horizonY)
    close()
  }
  drawPath(mountainPath, Color(0xFF1E293B).copy(alpha = 0.75f))
}

private fun DrawScope.drawPerspectiveRoadAndSidewalks(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float
) {
  val groundH = canvasH - horizonY
  val vanishingX = canvasW * 0.5f

  // Ground base fill (dark verge)
  drawRect(
    color = Color(0xFF141A1E),
    topLeft = Offset(0f, horizonY),
    size = Size(canvasW, groundH)
  )

  // Road Perspective Quadrilateral
  val roadTopW = canvasW * 0.14f
  val roadBottomW = canvasW * 0.88f

  val roadPath = Path().apply {
    moveTo(vanishingX - roadTopW / 2f, horizonY)
    lineTo(vanishingX + roadTopW / 2f, horizonY)
    lineTo(vanishingX + roadBottomW / 2f, canvasH)
    lineTo(vanishingX - roadBottomW / 2f, canvasH)
    close()
  }
  // Rich asphalt dark grey
  drawPath(roadPath, Color(0xFF1E2024))

  // Sidewalks on Left and Right
  val sidewalkTopW = canvasW * 0.08f
  val sidewalkBottomW = canvasW * 0.32f

  // Left Sidewalk
  val leftSidewalk = Path().apply {
    moveTo(vanishingX - roadTopW / 2f - sidewalkTopW, horizonY)
    lineTo(vanishingX - roadTopW / 2f, horizonY)
    lineTo(vanishingX - roadBottomW / 2f, canvasH)
    lineTo(vanishingX - roadBottomW / 2f - sidewalkBottomW, canvasH)
    close()
  }
  drawPath(leftSidewalk, Color(0xFF33383F))

  // Right Sidewalk
  val rightSidewalk = Path().apply {
    moveTo(vanishingX + roadTopW / 2f, horizonY)
    lineTo(vanishingX + roadTopW / 2f + sidewalkTopW, horizonY)
    lineTo(vanishingX + roadBottomW / 2f + sidewalkBottomW, canvasH)
    lineTo(vanishingX + roadBottomW / 2f, canvasH)
    close()
  }
  drawPath(rightSidewalk, Color(0xFF33383F))

  // Red & White Painted Curb Blocks (Matching Screenshots 1 & 2)
  val curbSegments = 16
  for (i in 0 until curbSegments) {
    val t0 = (i / curbSegments.toFloat() + (cameraScrollZ * 0.03f) % (1f / curbSegments)) % 1f
    val t1 = (t0 + 1f / curbSegments).coerceAtMost(1f)
    val curbColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFFEEEEEE)

    val y0 = horizonY + t0 * t0 * groundH
    val y1 = horizonY + t1 * t1 * groundH

    val rw0 = roadTopW + (roadBottomW - roadTopW) * (t0 * t0)
    val rw1 = roadTopW + (roadBottomW - roadTopW) * (t1 * t1)

    // Left curb segment
    drawLine(curbColor, Offset(vanishingX - rw0 / 2f, y0), Offset(vanishingX - rw1 / 2f, y1), strokeWidth = (2f + t0 * 6f))
    // Right curb segment
    drawLine(curbColor, Offset(vanishingX + rw0 / 2f, y0), Offset(vanishingX + rw1 / 2f, y1), strokeWidth = (2f + t0 * 6f))
  }

  // Double Solid Yellow Center Line (Directly matching Screenshots 1 & 2!)
  val yellowColor = Color(0xFFF5C518)
  val centerDashCount = 14
  for (i in 0 until centerDashCount) {
    val t = (i / centerDashCount.toFloat() + (cameraScrollZ * 0.05f) % (1f / centerDashCount)) % 1f
    val y = horizonY + t * t * groundH
    val nextY = horizonY + ((t + 0.05f).coerceAtMost(1f)).let { it * it * groundH }
    val thickness = (1.5f + t * 4.5f)
    val lineSpacing = (2.5f + t * 6f)

    // Left yellow stripe
    drawLine(yellowColor, Offset(vanishingX - lineSpacing / 2f, y), Offset(vanishingX - lineSpacing / 2f, nextY), strokeWidth = thickness)
    // Right yellow stripe
    drawLine(yellowColor, Offset(vanishingX + lineSpacing / 2f, y), Offset(vanishingX + lineSpacing / 2f, nextY), strokeWidth = thickness)
  }
}

private fun DrawScope.drawBuildingsAlongStreet(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY

  // Traditional Sana'a clay & stone buildings along left and right background
  val buildingTiers = listOf(0.15f, 0.35f, 0.65f, 0.95f)

  for ((idx, baseT) in buildingTiers.withIndex()) {
    val t = (baseT + (cameraScrollZ * 0.02f) % 1f) % 1f
    if (t < 0.05f) continue

    val yBottom = horizonY + t * groundH
    val scale = t
    val bWidth = 70f * scale + 30f
    val bHeight = 110f * scale + 45f

    // Left Building
    val leftX = (vanishingX - (canvasW * 0.44f * scale) - bWidth).coerceAtLeast(-10f)
    drawRect(
      color = if (idx % 2 == 0) Color(0xFF4E342E) else Color(0xFF5D4037),
      topLeft = Offset(leftX, yBottom - bHeight),
      size = Size(bWidth, bHeight)
    )
    // Sana'a Qamariya stained glass windows
    if (scale > 0.4f) {
      val windowR = 7f * scale
      drawCircle(Color(0xFFFFD54F), radius = windowR, center = Offset(leftX + bWidth * 0.5f, yBottom - bHeight * 0.7f))
      drawCircle(Color(0xFF29B6F6), radius = windowR * 0.6f, center = Offset(leftX + bWidth * 0.5f, yBottom - bHeight * 0.7f))
    }

    // Right Building
    val rightX = (vanishingX + (canvasW * 0.44f * scale)).coerceAtMost(canvasW)
    drawRect(
      color = if (idx % 2 == 0) Color(0xFF3E2723) else Color(0xFF4E342E),
      topLeft = Offset(rightX, yBottom - bHeight),
      size = Size(bWidth, bHeight)
    )
    if (scale > 0.4f) {
      val windowR = 7f * scale
      drawCircle(Color(0xFFFFD54F), radius = windowR, center = Offset(rightX + bWidth * 0.5f, yBottom - bHeight * 0.7f))
      drawCircle(Color(0xFF66BB6A), radius = windowR * 0.6f, center = Offset(rightX + bWidth * 0.5f, yBottom - bHeight * 0.7f))
    }
  }
}

private fun DrawScope.drawTreesAndStreetlights(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY
  val lampTiers = listOf(0.2f, 0.5f, 0.85f)

  for (baseT in lampTiers) {
    val t = (baseT + (cameraScrollZ * 0.04f) % 1f) % 1f
    if (t < 0.08f) continue

    val y = horizonY + t * t * groundH
    val scale = t
    val roadW = (canvasW * 0.14f) + (canvasW * 0.74f) * (t * t)

    // Left Street Tree (Lush green foliage matching Screenshot 1 & 2)
    val treeX = vanishingX - roadW / 2f - (28f * scale)
    val trunkH = 65f * scale + 15f
    val foliageR = 32f * scale + 12f

    // Tree drop shadow
    drawOval(Color(0x55000000), Offset(treeX - foliageR * 0.8f, y - 4f), Size(foliageR * 1.6f, foliageR * 0.5f))
    // Trunk
    drawLine(Color(0xFF42281A), Offset(treeX, y), Offset(treeX, y - trunkH), strokeWidth = 5f * scale + 2f)
    // Leafy green crowns (multi-layered)
    drawCircle(Color(0xFF1B5E20), radius = foliageR, center = Offset(treeX, y - trunkH - foliageR * 0.5f))
    drawCircle(Color(0xFF2E7D32), radius = foliageR * 0.82f, center = Offset(treeX - 4f * scale, y - trunkH - foliageR * 0.6f))
    drawCircle(Color(0xFF43A047), radius = foliageR * 0.55f, center = Offset(treeX + 3f * scale, y - trunkH - foliageR * 0.8f))

    // Right Streetlight (Tall pole with curved head & glowing light cone matching Screenshot 1)
    val lampX = vanishingX + roadW / 2f + (14f * scale)
    val poleH = 85f * scale + 25f

    // Volumetric warm light cone on the ground
    val coneR = 55f * scale + 18f
    drawOval(
      brush = Brush.radialGradient(
        colors = listOf(Color(0x66FFE082), Color(0x33FFD54F), Color.Transparent),
        center = Offset(lampX - 10f * scale, y),
        radius = coneR
      ),
      topLeft = Offset(lampX - coneR, y - coneR * 0.35f),
      size = Size(coneR * 2f, coneR * 0.7f)
    )

    // Pole (metal grey curved)
    val polePath = Path().apply {
      moveTo(lampX, y)
      lineTo(lampX, y - poleH)
      quadraticBezierTo(lampX, y - poleH - 12f * scale, lampX - 14f * scale, y - poleH - 10f * scale)
    }
    drawPath(polePath, Color(0xFF9E9E9E), style = Stroke(width = 3f * scale + 1.5f))

    // Glowing Lantern Bulb
    val bulbPos = Offset(lampX - 14f * scale, y - poleH - 10f * scale)
    drawCircle(Color(0xFFFFF9C4), radius = 4.5f * scale + 2f, center = bulbPos)
    drawCircle(Color(0x88FFD54F), radius = 10f * scale + 4f, center = bulbPos)
  }
}

private fun DrawScope.drawTrafficVehicles(
  vehicles: List<TrafficCar>,
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY

  for (car in vehicles) {
    val t = ((car.worldZ - cameraScrollZ) % 100f + 100f) % 100f / 100f
    if (t !in 0.1f..0.92f) continue

    val y = horizonY + t * t * groundH
    val scale = t
    val roadW = (canvasW * 0.14f) + (canvasW * 0.74f) * (t * t)

    val laneOffset = if (car.laneIndex < 0) -roadW * 0.26f else roadW * 0.26f
    val carX = vanishingX + laneOffset
    val carW = 54f * scale + 18f
    val carH = 34f * scale + 12f

    // Car Shadow
    drawOval(Color(0x66000000), Offset(carX - carW * 0.5f, y - 4f), Size(carW, carH * 0.4f))

    // Car Body
    drawRoundRect(
      color = car.color,
      topLeft = Offset(carX - carW * 0.5f, y - carH),
      size = Size(carW, carH),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
    )

    // Windshield & Rear Window
    drawRect(
      color = Color(0xFF1E293B),
      topLeft = Offset(carX - carW * 0.38f, y - carH * 0.85f),
      size = Size(carW * 0.76f, carH * 0.45f)
    )

    // Tail-lights (Red glow) or Headlights
    val lightY = y - carH * 0.25f
    if (car.laneIndex > 0) {
      // Facing away -> Tail lights
      drawCircle(Color(0xFFFF1744), radius = 3.5f * scale, center = Offset(carX - carW * 0.4f, lightY))
      drawCircle(Color(0xFFFF1744), radius = 3.5f * scale, center = Offset(carX + carW * 0.4f, lightY))
    } else {
      // Facing toward player -> Headlights beam
      drawCircle(Color(0xFFFFF9C4), radius = 4f * scale, center = Offset(carX - carW * 0.4f, lightY))
      drawCircle(Color(0xFFFFF9C4), radius = 4f * scale, center = Offset(carX + carW * 0.4f, lightY))
    }
  }
}

private fun DrawScope.drawProjectiles(
  projectiles: List<GameProjectile>,
  canvasW: Float,
  canvasH: Float,
  horizonY: Float
) {
  for (p in projectiles) {
    if (p.lifeTime <= 0f) continue
    val px = p.screenX
    val py = p.screenY

    when (p.type) {
      "GUN_FIRE" -> {
        // Glowing tracer bullet line
        drawLine(
          color = Color(0xFFFFEB3B),
          start = Offset(px, py),
          end = Offset(px, py - 20f),
          strokeWidth = 3.5f
        )
        drawCircle(Color(0xFFFF5722), radius = 4f, center = Offset(px, py))
      }
      "SLING_SHOT" -> {
        // Rock projectile
        drawCircle(Color(0xFFB0BEC5), radius = 5f, center = Offset(px, py))
      }
      "SPRAY" -> {
        // Graffiti paint cloud
        drawCircle(Color(0xFF00E5FF).copy(alpha = 0.6f * p.lifeTime), radius = 14f, center = Offset(px, py))
      }
      else -> {
        // Firecracker / explosion spark
        drawCircle(Color(0xFFFF3D00), radius = 7f, center = Offset(px, py))
      }
    }
  }
}

/**
 * 3D Player Character Rendering (Third-Person Behind View).
 * Recreates the exact character from Screenshots 1, 2, 3, 6:
 * - Cowboy Hat with curved brim & crown
 * - Shemagh / Bandana wrapped around the neck
 * - Green Short-Sleeve Shirt with White Circular Emblem on back!
 * - Moving arms and swinging legs
 * - Dark Baggy Pants and White Sneakers
 * - Jump animation and Ground Shadow
 */
private fun DrawScope.drawPlayerCharacter(
  hero: UnifiedHeroId,
  playerX: Float,
  jumpHeight: Float,
  walkCycle: Float,
  isInsideVehicle: Boolean,
  playerAngleDeg: Float,
  canvasW: Float,
  canvasH: Float,
  horizonY: Float
) {
  val centerX = canvasW * 0.5f + (playerX * canvasW * 0.34f)
  val baseGroundY = canvasH * 0.82f
  val jumpOffsetY = jumpHeight * 90f
  val playerY = baseGroundY - jumpOffsetY

  if (isInsideVehicle) {
    // ----------------------------------------------------
    // In-Vehicle Mode: Sana'a Dabab or Sport Pickup Hilux
    // ----------------------------------------------------
    val vehicleW = 120f
    val vehicleH = 75f

    // Ground Shadow
    drawOval(Color(0x77000000), Offset(centerX - vehicleW * 0.55f, baseGroundY - 8f), Size(vehicleW * 1.1f, 32f))

    // Vehicle Body (Yemeni Dabab Yellow or Shas White)
    val bodyColor = if (hero == UnifiedHeroId.AMMAR) Color(0xFFFBC02D) else Color(0xFFECEFF1)
    drawRoundRect(
      color = bodyColor,
      topLeft = Offset(centerX - vehicleW * 0.5f, playerY - vehicleH),
      size = Size(vehicleW, vehicleH),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )

    // Roof rack & luggage
    drawRect(Color(0xFF37474F), Offset(centerX - vehicleW * 0.4f, playerY - vehicleH - 12f), Size(vehicleW * 0.8f, 12f))
    // Rear windshield
    drawRect(Color(0xFF1E293B), Offset(centerX - vehicleW * 0.38f, playerY - vehicleH + 10f), Size(vehicleW * 0.76f, 26f))
    // Tail-lights
    drawCircle(Color(0xFFFF1744), radius = 7f, center = Offset(centerX - vehicleW * 0.42f, playerY - 18f))
    drawCircle(Color(0xFFFF1744), radius = 7f, center = Offset(centerX + vehicleW * 0.42f, playerY - 18f))

    // License Plate
    drawRoundRect(Color(0xFFEEEEEE), Offset(centerX - 18f, playerY - 22f), Size(36f, 14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
    return
  }

  // ----------------------------------------------------
  // On-Foot Mode: 3D Character from Behind (Matching Screenshots!)
  // ----------------------------------------------------

  // 1. Soft Dynamic Ground Shadow
  val shadowScale = (1f - jumpHeight * 0.4f).coerceAtLeast(0.5f)
  drawOval(
    color = Color(0x77000000),
    topLeft = Offset(centerX - 24f * shadowScale, baseGroundY - 6f),
    size = Size(48f * shadowScale, 16f * shadowScale)
  )

  // Leg walking/running stride offsets
  val legStride = sin(walkCycle) * 14f
  val armStride = -sin(walkCycle) * 12f

  // 2. Legs & Pants (Dark Navy / Black Loose Trousers)
  val hipY = playerY - 48f
  val footY = playerY

  // Left Leg
  val leftFootX = centerX - 10f + legStride * 0.6f
  val leftFootY = footY - abs(cos(walkCycle)) * 4f
  drawLine(
    color = Color(0xFF1E293B), // Dark pants
    start = Offset(centerX - 8f, hipY),
    end = Offset(leftFootX, leftFootY),
    strokeWidth = 12f,
    cap = StrokeCap.Round
  )
  // Left Sneaker (White/Grey with sole)
  drawRoundRect(
    color = Color(0xFFEEEEEE),
    topLeft = Offset(leftFootX - 7f, leftFootY - 6f),
    size = Size(14f, 8f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
  )

  // Right Leg
  val rightFootX = centerX + 10f - legStride * 0.6f
  val rightFootY = footY - abs(sin(walkCycle)) * 4f
  drawLine(
    color = Color(0xFF1E293B),
    start = Offset(centerX + 8f, hipY),
    end = Offset(rightFootX, rightFootY),
    strokeWidth = 12f,
    cap = StrokeCap.Round
  )
  // Right Sneaker
  drawRoundRect(
    color = Color(0xFFEEEEEE),
    topLeft = Offset(rightFootX - 7f, rightFootY - 6f),
    size = Size(14f, 8f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
  )

  // 3. Torso: Gangster Green Shirt (Iconic Grove Style matching Screenshots 1, 2, 3, 6!)
  val torsoTopY = playerY - 82f
  val torsoH = 34f
  val torsoW = 32f

  // Shirt body
  drawRoundRect(
    color = hero.shirtColor,
    topLeft = Offset(centerX - torsoW * 0.5f, torsoTopY),
    size = Size(torsoW, torsoH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
  )

  // White Emblem on the Back (Matching Screenshots 1, 2, 3!)
  drawCircle(
    color = Color.White.copy(alpha = 0.92f),
    radius = 7.5f,
    center = Offset(centerX, torsoTopY + torsoH * 0.45f)
  )
  drawCircle(
    color = hero.shirtColor,
    radius = 4f,
    center = Offset(centerX, torsoTopY + torsoH * 0.45f)
  )

  // 4. Arms (Short sleeves + skin forearms swinging)
  // Left Arm
  val leftHandY = torsoTopY + 24f + armStride
  drawLine(
    color = hero.shirtColor,
    start = Offset(centerX - torsoW * 0.5f, torsoTopY + 4f),
    end = Offset(centerX - torsoW * 0.5f - 8f, torsoTopY + 16f),
    strokeWidth = 9f,
    cap = StrokeCap.Round
  )
  drawLine(
    color = Color(0xFFD7A177), // Skin forearm
    start = Offset(centerX - torsoW * 0.5f - 8f, torsoTopY + 16f),
    end = Offset(centerX - torsoW * 0.5f - 10f, leftHandY),
    strokeWidth = 7f,
    cap = StrokeCap.Round
  )

  // Right Arm (Holding weapon or swinging)
  val rightHandY = torsoTopY + 24f - armStride
  drawLine(
    color = hero.shirtColor,
    start = Offset(centerX + torsoW * 0.5f, torsoTopY + 4f),
    end = Offset(centerX + torsoW * 0.5f + 8f, torsoTopY + 16f),
    strokeWidth = 9f,
    cap = StrokeCap.Round
  )
  drawLine(
    color = Color(0xFFD7A177),
    start = Offset(centerX + torsoW * 0.5f + 8f, torsoTopY + 16f),
    end = Offset(centerX + torsoW * 0.5f + 10f, rightHandY),
    strokeWidth = 7f,
    cap = StrokeCap.Round
  )

  // 5. Bandana / Shemagh Wrapped Around Neck (Matching Screenshots 1, 2, 6!)
  if (hero.hasBandana) {
    val bandanaPath = Path().apply {
      moveTo(centerX - 12f, torsoTopY + 2f)
      lineTo(centerX + 12f, torsoTopY + 2f)
      lineTo(centerX, torsoTopY + 14f)
      close()
    }
    drawPath(bandanaPath, Color(0xFFB71C1C)) // Crimson Red Shemagh pattern
    drawCircle(Color(0xFFEEEEEE), radius = 2f, center = Offset(centerX, torsoTopY + 7f))
  }

  // 6. Head & Hair
  val headCenterY = torsoTopY - 14f
  drawCircle(
    color = Color(0xFFD7A177), // Skin
    radius = 12f,
    center = Offset(centerX, headCenterY)
  )

  // 7. Cowboy Hat / Headwear (Matching Screenshots 1, 2, 6!)
  when (hero.hatType) {
    HatType.COWBOY_HAT -> {
      // Curved Cowboy Hat Brim (Wide brim seen from behind)
      val brimPath = Path().apply {
        moveTo(centerX - 24f, headCenterY - 4f)
        quadraticBezierTo(centerX, headCenterY + 2f, centerX + 24f, headCenterY - 4f)
        lineTo(centerX + 24f, headCenterY - 9f)
        quadraticBezierTo(centerX, headCenterY - 3f, centerX - 24f, headCenterY - 9f)
        close()
      }
      drawPath(brimPath, Color(0xFF3E2723)) // Dark leather brown

      // Crown of the Hat (Indented center)
      val crownPath = Path().apply {
        moveTo(centerX - 13f, headCenterY - 8f)
        lineTo(centerX - 10f, headCenterY - 26f)
        quadraticBezierTo(centerX, headCenterY - 22f, centerX + 10f, headCenterY - 26f)
        lineTo(centerX + 13f, headCenterY - 8f)
        close()
      }
      drawPath(crownPath, Color(0xFF4E342E))
      // Hat Band
      drawLine(Color(0xFFB71C1C), Offset(centerX - 12f, headCenterY - 9f), Offset(centerX + 12f, headCenterY - 9f), strokeWidth = 3f)
    }

    HatType.BACKWARDS_CAP -> {
      // Backwards Cap (Cap crown + visor facing backwards)
      drawCircle(Color(0xFFD84315), radius = 13f, center = Offset(centerX, headCenterY - 4f))
      drawRoundRect(Color(0xFFBF360C), Offset(centerX - 14f, headCenterY - 2f), Size(28f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
    }

    HatType.BERET -> {
      // Tactical Beret
      drawOval(Color(0xFF263238), Offset(centerX - 15f, headCenterY - 18f), Size(30f, 15f))
      drawCircle(Color(0xFFFFD54F), radius = 2.5f, center = Offset(centerX - 7f, headCenterY - 11f))
    }

    HatType.SPORTS_BAND -> {
      // Sports Headband
      drawRoundRect(Color(0xFF00E676), Offset(centerX - 13f, headCenterY - 10f), Size(26f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
    }

    HatType.NONE -> {
      // Dark short hair
      drawCircle(Color(0xFF1A1A1A), radius = 12.5f, center = Offset(centerX, headCenterY - 2f))
    }
  }
}
