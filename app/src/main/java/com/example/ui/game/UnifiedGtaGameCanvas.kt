package com.example.ui.game

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import com.example.model.*
import kotlin.math.*

/**
 * 10D Realistic High-Fidelity Sana'a 3D Third-Person Game Canvas (محرك الرسم 10D لصنعاء الواقعية).
 * Features real-scale human and vehicle proportions, authentic Sana'a architecture,
 * iconic roundabouts (Kentucky, Al-Mesbahi, Asr), historic landmarks (Al-Saleh Mosque, Bab Al-Yemen,
 * Grand Mosque, Al-Thawra Stadium, Government Compound), bustling pedestrians, and vibrant shops.
 */
@Composable
fun UnifiedGtaGameCanvas(
  hero: UnifiedHeroId,
  weapon: WeaponItem,
  playerX: Float,
  playerZ: Float,
  playerAngleDeg: Float,
  isWalking: Boolean,
  isRunning: Boolean,
  jumpHeight: Float,
  isInsideVehicle: Boolean,
  projectiles: List<GameProjectile>,
  trafficVehicles: List<TrafficCar>,
  modifier: Modifier = Modifier,
  is10DRealScale: Boolean = true,
  currentDistrict: SanaaDistrict = SanaaDistrict.BAB_AL_YEMEN,
  roadCurvature: Float = 0f,
  activeTurnBranch: SanaaTurnBranch? = null,
  pedestrians: List<SanaaPedestrian> = emptyList(),
  commercialShops: List<SanaaCommercialShop> = emptyList(),
  onCameraRotate: (deltaDeg: Float) -> Unit = {}
) {
  val context = LocalContext.current

  // Load photo textures of Yemeni nature and Old Sana'a architecture
  val naturePhoto = remember {
    try {
      val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_sanaa_nature)
      bmp?.asImageBitmap()
    } catch (_: Exception) {
      null
    }
  }

  val housesPhoto = remember {
    try {
      val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.img_sanaa_houses)
      bmp?.asImageBitmap()
    } catch (_: Exception) {
      null
    }
  }

  // Animation ticks for walking, blinking lights, water and heat waves
  val infiniteTransition = rememberInfiniteTransition(label = "unified_gta_anim")
  val animTimeSeconds by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28318f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "anim_time"
  )

  val walkCycleTick by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28318f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (isRunning) 350 else 600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "walk_cycle"
  )

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          onCameraRotate(dragAmount.x * 0.35f)
        }
      }
  ) {
    val canvasW = size.width
    val canvasH = size.height
    val horizonY = canvasH * 0.38f

    // 1. Sana'a Night Sky & Mountain Basin Horizon (Jabal Nuqum & Jabal Ayban)
    drawSanaaSkyAndMountains(canvasW, horizonY, animTimeSeconds, naturePhoto)

    // 2. Colossal Historic Landmarks on the Horizon & Roadside (Al-Saleh, Bab Al-Yemen, Stadium, Compound, Airport, Schools, Bridges)
    drawSanaaLandmarks(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      district = currentDistrict,
      animTime = animTimeSeconds
    )

    // 3. 3D Perspective Roadway, Curves, Basalt Curbs, Sidewalks, Roundabouts, Bridges & Intersections
    drawPerspectiveRoadSidewalksAndRoundabouts(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      district = currentDistrict,
      roadCurvature = roadCurvature,
      activeTurnBranch = activeTurnBranch,
      animTime = animTimeSeconds
    )

    // 4. Buildings on BOTH Sides (Tower Houses, Government HQ, Schools, Souqs, Airport)
    drawOldSanaaArchitectureAndShops(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      roadCurvature = roadCurvature,
      animTime = animTimeSeconds,
      housesPhoto = housesPhoto,
      shops = commercialShops,
      is10DScale = is10DRealScale
    )

    // 5. Overhead Sana'a Green Highway Signs & Electric Cables (أسلاك الكهرباء ولوحات الشوارع)
    drawSanaaOverheadSignsAndWires(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      district = currentDistrict
    )

    // 6. Street Trees (Tall Date Palms, Lush Acacia & Neem) & Traditional Streetlights
    drawTreesAndStreetlights(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      is10DScale = is10DRealScale
    )

    // 7. Sana'ani Pedestrians Walking on Sidewalks & Crossing (الناس تمر في الشوارع)
    drawSanaaPedestrians(
      pedestrians = pedestrians,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      walkCycleTick = walkCycleTick,
      is10DScale = is10DRealScale
    )

    // 8. Authentic Yemeni Traffic Vehicles in 10D Real Scale (شاص يمني 4x4، دباب أصفر، تاكسي، شرطة)
    drawTrafficVehicles(
      vehicles = trafficVehicles,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      cameraScrollZ = playerZ,
      animTime = animTimeSeconds,
      is10DScale = is10DRealScale
    )

    // 9. Projectiles (Tracers, Slingshot Rocks, Graffiti Spray, Firecrackers)
    drawProjectiles(
      projectiles = projectiles,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY
    )

    // 10. Player Character in 10D Real-Life Proportions (بالمعوز الصنعاني والجنبية والشال)
    drawPlayerCharacter(
      hero = hero,
      playerX = playerX,
      jumpHeight = jumpHeight,
      walkCycle = walkCycleTick,
      isWalking = isWalking,
      isRunning = isRunning,
      isInsideVehicle = isInsideVehicle,
      playerAngleDeg = playerAngleDeg,
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      animTime = animTimeSeconds,
      is10DScale = is10DRealScale
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
  val carType: String, // "SHAS", "DABAB", "TAXI", "POLICE"
  val color: Color
)

// ----------------------------------------------------
// 1. Sana'a Night Sky & Mountains (جبل نقم وجبل عيبان)
// ----------------------------------------------------
private fun DrawScope.drawSanaaSkyAndMountains(
  canvasW: Float,
  horizonY: Float,
  animTime: Float,
  naturePhoto: ImageBitmap? = null
) {
  if (naturePhoto != null) {
    drawImage(
      image = naturePhoto,
      dstOffset = IntOffset(0, 0),
      dstSize = IntSize(canvasW.toInt(), horizonY.toInt()),
      alpha = 0.55f
    )
  }

  val skyGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xEE060911),
      Color(0xCC0D1624),
      Color(0x881E293B),
      Color(0x44334155)
    ),
    startY = 0f,
    endY = horizonY
  )
  drawRect(brush = skyGradient, size = Size(canvasW, horizonY))

  // Golden Crescent Moon of Sana'a (هلال صنعاء الذهبي)
  val moonX = canvasW * 0.82f
  val moonY = horizonY * 0.28f
  val moonRadius = 16f
  drawCircle(Color(0xFFFFE082), radius = moonRadius, center = Offset(moonX, moonY))
  drawCircle(Color(0xFF0D1624), radius = moonRadius * 0.85f, center = Offset(moonX - 5.5f, moonY - 3.5f))

  // Stars in high-altitude Sana'a sky
  val starPositions = listOf(
    Offset(canvasW * 0.10f, horizonY * 0.16f),
    Offset(canvasW * 0.22f, horizonY * 0.10f),
    Offset(canvasW * 0.38f, horizonY * 0.20f),
    Offset(canvasW * 0.54f, horizonY * 0.13f),
    Offset(canvasW * 0.70f, horizonY * 0.22f),
    Offset(canvasW * 0.92f, horizonY * 0.12f)
  )
  for ((idx, pos) in starPositions.withIndex()) {
    val twinkle = (sin(animTime * 2.5f + idx * 1.5f) + 1f) * 0.5f
    val starAlpha = 0.4f + 0.6f * twinkle
    drawCircle(Color.White.copy(alpha = starAlpha), radius = 1.8f, center = pos)
  }

  // Mountain Ridges (Jabal Nuqum & Jabal Ayban)
  val farMountainPath = Path().apply {
    moveTo(0f, horizonY)
    lineTo(0f, horizonY - 45f)
    lineTo(canvasW * 0.18f, horizonY - 80f)
    lineTo(canvasW * 0.35f, horizonY - 55f)
    lineTo(canvasW * 0.52f, horizonY - 95f)
    lineTo(canvasW * 0.70f, horizonY - 65f)
    lineTo(canvasW * 0.88f, horizonY - 105f)
    lineTo(canvasW, horizonY - 50f)
    lineTo(canvasW, horizonY)
    close()
  }
  drawPath(farMountainPath, Color(0xFF141C2B).copy(alpha = 0.90f))

  val nearMountainPath = Path().apply {
    moveTo(0f, horizonY)
    lineTo(0f, horizonY - 28f)
    lineTo(canvasW * 0.15f, horizonY - 50f)
    lineTo(canvasW * 0.32f, horizonY - 34f)
    lineTo(canvasW * 0.55f, horizonY - 58f)
    lineTo(canvasW * 0.75f, horizonY - 38f)
    lineTo(canvasW, horizonY - 42f)
    lineTo(canvasW, horizonY)
    close()
  }
  drawPath(nearMountainPath, Color(0xFF1B2436))

  // Pinprick lights of traditional mountain terraced villages
  val villageLights = listOf(
    Offset(canvasW * 0.16f, horizonY - 58f),
    Offset(canvasW * 0.19f, horizonY - 54f),
    Offset(canvasW * 0.50f, horizonY - 72f),
    Offset(canvasW * 0.54f, horizonY - 66f),
    Offset(canvasW * 0.86f, horizonY - 76f)
  )
  for (light in villageLights) {
    drawCircle(Color(0xFFFFD54F).copy(alpha = 0.85f), radius = 2.2f, center = light)
  }
}

// ----------------------------------------------------
// 2. Colossal Sana'a Landmarks (معالم صنعاء الكبرى)
// ----------------------------------------------------
private fun DrawScope.drawSanaaLandmarks(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  district: SanaaDistrict,
  animTime: Float
) {
  val vanishingX = canvasW * 0.5f

  when (district.landmarkType) {
    // A. AL-SALEH GRAND MOSQUE (جامع الصالح وميدان السبعين)
    LandmarkVisualType.AL_SALEH_MOSQUE -> {
      val mosqueW = canvasW * 0.58f
      val mosqueX = vanishingX - mosqueW * 0.5f
      val mosqueBaseY = horizonY - 8f

      // Golden ambient aura behind mosque
      drawOval(
        brush = Brush.radialGradient(
          colors = listOf(Color(0x66FFD700), Color(0x22FFA000), Color.Transparent),
          center = Offset(vanishingX, mosqueBaseY - 60f),
          radius = mosqueW * 0.6f
        ),
        topLeft = Offset(vanishingX - mosqueW * 0.6f, mosqueBaseY - 120f),
        size = Size(mosqueW * 1.2f, 130f)
      )

      // Main Mosque Body (Ochre Stone)
      drawRect(
        color = Color(0xFFD7CCC8),
        topLeft = Offset(mosqueX, mosqueBaseY - 45f),
        size = Size(mosqueW, 45f)
      )
      // Arched facade bays
      val bayCount = 7
      val bayW = mosqueW / bayCount
      for (b in 0 until bayCount) {
        val bx = mosqueX + b * bayW + bayW * 0.15f
        drawRoundRect(
          color = Color(0xFF3E2723),
          topLeft = Offset(bx, mosqueBaseY - 32f),
          size = Size(bayW * 0.7f, 32f),
          cornerRadius = CornerRadius(bayW * 0.35f, bayW * 0.35f)
        )
      }

      // Massive Central Golden Dome (القبة الذهبية الكبرى)
      val domeRadius = mosqueW * 0.16f
      val domeCenterX = vanishingX
      val domeCenterY = mosqueBaseY - 45f
      drawCircle(Color(0xFFFFD54F), radius = domeRadius, center = Offset(domeCenterX, domeCenterY))
      // Dome crescent finial
      drawLine(Color(0xFFFFD54F), Offset(domeCenterX, domeCenterY - domeRadius), Offset(domeCenterX, domeCenterY - domeRadius - 14f), strokeWidth = 2.5f)
      drawCircle(Color(0xFFFFE082), radius = 3.5f, center = Offset(domeCenterX, domeCenterY - domeRadius - 14f))

      // 4 Secondary White & Gold Domes
      val secDomes = listOf(vanishingX - mosqueW * 0.28f, vanishingX + mosqueW * 0.28f)
      for (sdX in secDomes) {
        drawCircle(Color(0xFFFFE082), radius = domeRadius * 0.65f, center = Offset(sdX, mosqueBaseY - 42f))
      }

      // 6 Soaring White & Gold Minarets (مآذن جامع الصالح الشاهقة)
      val minaretPositions = listOf(
        mosqueX - 10f,
        mosqueX + mosqueW * 0.16f,
        mosqueX + mosqueW * 0.84f,
        mosqueX + mosqueW + 10f
      )
      for (mX in minaretPositions) {
        val minH = 110f
        // Minaret tower
        drawRect(Color(0xFFEEEEEE), Offset(mX - 5f, mosqueBaseY - minH), Size(10f, minH))
        // Minaret Balconies (Muqarnas)
        drawRect(Color(0xFFFFD54F), Offset(mX - 8f, mosqueBaseY - minH * 0.7f), Size(16f, 4f))
        drawRect(Color(0xFFFFD54F), Offset(mX - 7f, mosqueBaseY - minH * 0.9f), Size(14f, 3.5f))
        // Golden spire & crescent
        val spirePath = Path().apply {
          moveTo(mX - 5f, mosqueBaseY - minH)
          lineTo(mX + 5f, mosqueBaseY - minH)
          lineTo(mX, mosqueBaseY - minH - 18f)
          close()
        }
        drawPath(spirePath, Color(0xFFFFD54F))
        // Minaret beacon light blinking
        drawCircle(Color(0xCCFFF9C4), radius = 3f, center = Offset(mX, mosqueBaseY - minH - 18f))
      }
    }

    // B. BAB AL-YEMEN HISTORIC ARCHWAY (بوابة باب اليمن التاريخية)
    LandmarkVisualType.BAB_AL_YEMEN_GATE -> {
      val gateW = canvasW * 0.62f
      val gateX = vanishingX - gateW * 0.5f
      val gateBaseY = horizonY - 2f
      val gateH = 80f

      // Ancient Stone Gatehouse Wall
      drawRect(Color(0xFF8D6E63), Offset(gateX, gateBaseY - gateH), Size(gateW, gateH))
      // Basalt stone layer at bottom
      drawRect(Color(0xFF3E2723), Offset(gateX, gateBaseY - 20f), Size(gateW, 20f))

      // Twin Crenellated Watchtowers on Left & Right
      val towerW = gateW * 0.22f
      drawRect(Color(0xFF6D4C41), Offset(gateX - 8f, gateBaseY - gateH - 18f), Size(towerW, gateH + 18f))
      drawRect(Color(0xFF6D4C41), Offset(gateX + gateW - towerW + 8f, gateBaseY - gateH - 18f), Size(towerW, gateH + 18f))

      // Tower Crenellations (الشرفات الحربية التراثية)
      val merlonCount = 4
      val mStep = towerW / merlonCount
      for (m in 0 until merlonCount) {
        drawRect(Color(0xFFF5F5F5), Offset(gateX - 8f + m * mStep, gateBaseY - gateH - 25f), Size(mStep * 0.6f, 7f))
        drawRect(Color(0xFFF5F5F5), Offset(gateX + gateW - towerW + 8f + m * mStep, gateBaseY - gateH - 25f), Size(mStep * 0.6f, 7f))
      }

      // Grand Horseshoe Archway Portal spanning the road (القوس التاريخي العظيم)
      val archW = gateW * 0.44f
      val archH = gateH * 0.72f
      val archX = vanishingX - archW * 0.5f
      drawRoundRect(
        color = Color(0xFF1E140F),
        topLeft = Offset(archX, gateBaseY - archH),
        size = Size(archW, archH),
        cornerRadius = CornerRadius(archW * 0.5f, archW * 0.5f)
      )
      // White gypsum rosette decorative frame over the arch
      drawRoundRect(
        color = Color(0xFFEEEEEE),
        topLeft = Offset(archX - 3f, gateBaseY - archH - 3f),
        size = Size(archW + 6f, archH + 6f),
        cornerRadius = CornerRadius(archW * 0.5f + 3f, archW * 0.5f + 3f),
        style = Stroke(width = 3.5f)
      )

      // Arched wooden historic doors with iron rivets
      drawLine(Color(0xFF4E342E), Offset(vanishingX, gateBaseY - archH), Offset(vanishingX, gateBaseY), strokeWidth = 3f)

      // Inscription Board above the Arch ("باب اليمن - صنعاء القديمة")
      val signW = gateW * 0.38f
      val signH = 16f
      val signY = gateBaseY - archH - signH - 5f
      drawRoundRect(Color(0xFF3E2723), Offset(vanishingX - signW * 0.5f, signY), Size(signW, signH), CornerRadius(3f, 3f))
      drawRoundRect(Color(0xFFFFD54F), Offset(vanishingX - signW * 0.5f, signY), Size(signW, signH), CornerRadius(3f, 3f), style = Stroke(width = 1.2f))

      drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 11f
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText("🏰 باب اليمن - ٩٠٠ هـ 🏰", vanishingX, signY + 12f, paint)
      }
    }

    // C. AL-THAWRA SPORTS STADIUM (ستاد الثورة والمدينة الرياضية)
    LandmarkVisualType.AL_THAWRA_STADIUM -> {
      val stadiumW = canvasW * 0.65f
      val stadiumX = vanishingX - stadiumW * 0.5f
      val sBaseY = horizonY - 4f

      // Curved tiered grandstands
      drawRoundRect(Color(0xFF37474F), Offset(stadiumX, sBaseY - 48f), Size(stadiumW, 48f), CornerRadius(16f, 16f))
      // Green pitch glow inside
      drawOval(Color(0xFF2E7D32), Offset(vanishingX - stadiumW * 0.35f, sBaseY - 32f), Size(stadiumW * 0.7f, 22f))

      // 4 Giant Floodlight Steel Lattice Towers (أبراج كشافات ستاد الثورة العملاقة)
      val towerXs = listOf(stadiumX - 15f, stadiumX + stadiumW * 0.25f, stadiumX + stadiumW * 0.75f, stadiumX + stadiumW + 15f)
      for (tX in towerXs) {
        val twH = 95f
        // Steel lattice posts
        drawLine(Color(0xFF78909C), Offset(tX - 4f, sBaseY), Offset(tX - 1f, sBaseY - twH), strokeWidth = 2.5f)
        drawLine(Color(0xFF78909C), Offset(tX + 4f, sBaseY), Offset(tX + 1f, sBaseY - twH), strokeWidth = 2.5f)
        // Cross bracing
        var yB = sBaseY
        while (yB > sBaseY - twH) {
          drawLine(Color(0xFF546E7A), Offset(tX - 4f, yB), Offset(tX + 4f, yB - 10f), strokeWidth = 1.2f)
          yB -= 10f
        }
        // Floodlight Bank (Brilliant white rectangular cluster)
        drawRoundRect(Color(0xFFECEFF1), Offset(tX - 14f, sBaseY - twH - 12f), Size(28f, 14f), CornerRadius(2f, 2f))
        // Radial beam down to the stadium
        drawCircle(Color(0x88FFFFFF), radius = 10f, center = Offset(tX, sBaseY - twH - 5f))
      }
    }

    // D. GOVERNMENT DEFENCE COMPOUND (مجمع الدفاع العرضي والمباني الحكومية)
    LandmarkVisualType.GOVERNMENT_COMPOUND -> {
      val compW = canvasW * 0.60f
      val compX = vanishingX - compW * 0.5f
      val cBaseY = horizonY - 5f

      // Monumental government stone facade
      drawRect(Color(0xFF455A64), Offset(compX, cBaseY - 55f), Size(compW, 55f))
      // Central classical pediment
      val pedPath = Path().apply {
        moveTo(vanishingX - compW * 0.22f, cBaseY - 55f)
        lineTo(vanishingX + compW * 0.22f, cBaseY - 55f)
        lineTo(vanishingX, cBaseY - 78f)
        close()
      }
      drawPath(pedPath, Color(0xFF37474F))

      // Flagpole with Yemeni National Flag flying high 🇾🇪
      val poleX = vanishingX
      val poleTopY = cBaseY - 108f
      drawLine(Color(0xFFB0BEC5), Offset(poleX, cBaseY - 78f), Offset(poleX, poleTopY), strokeWidth = 2.5f)

      // National Flag (Red, White, Black horizontal stripes waving)
      val flagW = 28f
      val flagH = 18f
      val wave = sin(animTime * 4f) * 2f
      drawRect(Color(0xFFD32F2F), Offset(poleX, poleTopY), Size(flagW, flagH / 3f))
      drawRect(Color.White, Offset(poleX, poleTopY + flagH / 3f), Size(flagW, flagH / 3f))
      drawRect(Color.Black, Offset(poleX, poleTopY + (flagH * 2f / 3f)), Size(flagW, flagH / 3f))
    }

    // E. SANAA INTERNATIONAL AIRPORT (مطار صنعاء الدولي وبرج المراقبة ومدرج الطائرات)
    LandmarkVisualType.SANAA_AIRPORT -> {
      val airW = canvasW * 0.65f
      val airX = vanishingX - airW * 0.5f
      val aBaseY = horizonY - 4f

      // Airport Terminal Building with tinted curved glass facade
      drawRoundRect(Color(0xFF263238), Offset(airX, aBaseY - 45f), Size(airW, 45f), CornerRadius(8f, 8f))
      drawRoundRect(Color(0xFF0288D1).copy(alpha = 0.65f), Offset(airX + 10f, aBaseY - 40f), Size(airW - 20f, 32f), CornerRadius(6f, 6f))

      // Control Tower (برج المراقبة الجوية الأيقوني)
      val towerX = airX + airW * 0.82f
      val towerH = 100f
      drawRect(Color(0xFFECEFF1), Offset(towerX - 10f, aBaseY - towerH), Size(20f, towerH))
      drawRoundRect(Color(0xFF01579B), Offset(towerX - 18f, aBaseY - towerH - 18f), Size(36f, 20f), CornerRadius(4f, 4f))
      val radarAngle = animTime * 4f
      val radarEndX = towerX + cos(radarAngle) * 14f
      val radarEndY = aBaseY - towerH - 24f + sin(radarAngle) * 4f
      drawLine(Color(0xFFFFD54F), Offset(towerX, aBaseY - towerH - 20f), Offset(radarEndX, radarEndY), strokeWidth = 3f)
      drawCircle(Color(0xFFFF1744), radius = 3f, center = Offset(towerX, aBaseY - towerH - 26f))

      // Airport Signboard
      drawRoundRect(Color(0xFF0D47A1), Offset(airX + airW * 0.15f, aBaseY - 58f), Size(airW * 0.52f, 18f), CornerRadius(4f, 4f))
      drawContext.canvas.nativeCanvas.apply {
        val p = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 11f
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText("✈️ مطار صنعاء الدولي • SANA'A INTL AIRPORT", airX + airW * 0.41f, aBaseY - 45f, p)
      }

      val rLightCount = 8
      val rStep = airW / rLightCount
      for (r in 0 until rLightCount) {
        val lX = airX + r * rStep
        val lColor = if (r % 2 == 0) Color(0xFF00E676) else Color(0xFF00E5FF)
        drawCircle(lColor, radius = 3f, center = Offset(lX, aBaseY - 4f))
      }
    }

    // F. HISTORIC SANAA SCHOOLS (المدارس التعليمية الكبرى: جمال عبدالناصر والكويت)
    LandmarkVisualType.SANAA_SCHOOLS -> {
      val schW = canvasW * 0.62f
      val schX = vanishingX - schW * 0.5f
      val sBaseY = horizonY - 4f

      drawRect(Color(0xFFE0E0E0), Offset(schX, sBaseY - 55f), Size(schW, 55f))
      val winCount = 8
      val wStep = schW / winCount
      for (w in 0 until winCount) {
        drawRoundRect(Color(0xFF37474F), Offset(schX + w * wStep + 4f, sBaseY - 42f), Size(wStep * 0.65f, 22f), CornerRadius(4f, 4f))
      }

      // School Clock Tower
      val clockX = vanishingX
      val clockH = 88f
      drawRect(Color(0xFF5D4037), Offset(clockX - 16f, sBaseY - clockH), Size(32f, clockH))
      drawCircle(Color.White, radius = 10f, center = Offset(clockX, sBaseY - clockH + 18f))
      drawCircle(Color.Black, radius = 8f, center = Offset(clockX, sBaseY - clockH + 18f), style = Stroke(width = 1.5f))
      drawLine(Color.Black, Offset(clockX, sBaseY - clockH + 18f), Offset(clockX + 4f, sBaseY - clockH + 14f), strokeWidth = 2f)

      val sFlagY = sBaseY - clockH - 18f
      drawLine(Color(0xFF78909C), Offset(clockX, sBaseY - clockH), Offset(clockX, sFlagY), strokeWidth = 2.5f)
      drawRect(Color(0xFFD32F2F), Offset(clockX, sFlagY), Size(24f, 5f))
      drawRect(Color.White, Offset(clockX, sFlagY + 5f), Size(24f, 5f))
      drawRect(Color.Black, Offset(clockX, sFlagY + 10f), Size(24f, 5f))

      drawRoundRect(Color(0xFF1B5E20), Offset(vanishingX - schW * 0.35f, sBaseY - 68f), Size(schW * 0.70f, 18f), CornerRadius(4f, 4f))
      drawContext.canvas.nativeCanvas.apply {
        val p = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 10.5f
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText("🏫 مدرسة جمال عبد الناصر الثانوية للمتفوقين", vanishingX, sBaseY - 55f, p)
      }
    }

    // G. SANAA FLYOVER BRIDGES (جسور وتقاطعات صنعاء العلوية المعلقة)
    LandmarkVisualType.SANAA_BRIDGES -> {
      val brW = canvasW * 0.95f
      val brX = vanishingX - brW * 0.5f
      val brY = horizonY - 15f
      val brH = 26f

      drawRoundRect(Color(0xFF455A64), Offset(brX, brY - brH), Size(brW, brH), CornerRadius(4f, 4f))
      drawLine(Color(0xFF90A4AE), Offset(brX, brY - brH), Offset(brX + brW, brY - brH), strokeWidth = 3f)
      drawLine(Color(0xFFFFD54F), Offset(brX, brY - 4f), Offset(brX + brW, brY - 4f), strokeWidth = 2.5f)

      val pierPositions = listOf(vanishingX - canvasW * 0.32f, vanishingX + canvasW * 0.32f)
      for (pX in pierPositions) {
        drawRect(Color(0xFF37474F), Offset(pX - 14f, brY), Size(28f, 25f))
      }

      val topCarX = brX + ((animTime * 120f) % (brW - 40f))
      drawRoundRect(Color(0xFFD32F2F), Offset(topCarX, brY - brH - 12f), Size(24f, 12f), CornerRadius(2f, 2f))
      drawCircle(Color(0xFFFFEE58), radius = 2.5f, center = Offset(topCarX + 22f, brY - brH - 5f))

      val topCar2X = brX + brW - ((animTime * 150f) % (brW - 40f))
      drawRoundRect(Color(0xFF0288D1), Offset(topCar2X, brY - brH - 12f), Size(26f, 12f), CornerRadius(2f, 2f))
      drawCircle(Color(0xFFFF1744), radius = 2.5f, center = Offset(topCar2X, brY - brH - 5f))

      drawRoundRect(Color(0xFF1B5E20), Offset(vanishingX - 80f, brY - brH - 16f), Size(160f, 15f), CornerRadius(3f, 3f))
      drawContext.canvas.nativeCanvas.apply {
        val p = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = 9.5f
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText("🌉 جسر عصر المعلق وجسر كنتاكي", vanishingX, brY - brH - 4f, p)
      }
    }

    else -> {
      // General Sana'a Historic Old City skyline with stone minarets and tower roofs
      val minaretX = vanishingX + canvasW * 0.28f
      val mH = 75f
      drawRect(Color(0xFFD7CCC8), Offset(minaretX - 4f, horizonY - mH), Size(8f, mH))
      drawCircle(Color(0xFFFFD54F), radius = 5f, center = Offset(minaretX, horizonY - mH))
    }
  }
}

// ----------------------------------------------------
// 3. 3D Perspective Roadway, Curves, Basalt Curbs, Sidewalks, Roundabouts, Bridges & Intersections
// ----------------------------------------------------
private fun DrawScope.drawPerspectiveRoadSidewalksAndRoundabouts(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  district: SanaaDistrict,
  roadCurvature: Float,
  activeTurnBranch: SanaaTurnBranch?,
  animTime: Float
) {
  val groundH = canvasH - horizonY
  val vanishingX = canvasW * 0.5f

  fun getRoadCenterX(t: Float): Float {
    val curveFactor = (1f - t) * (1f - t)
    return vanishingX + (roadCurvature * canvasW * 0.30f * curveFactor)
  }

  // Ground base fill
  drawRect(
    color = Color(0xFF13171B),
    topLeft = Offset(0f, horizonY),
    size = Size(canvasW, groundH)
  )

  // 3D Curved Roadway rendered via vertical perspective strips
  val roadTopW = canvasW * 0.16f
  val roadBottomW = canvasW * 0.92f
  val sliceCount = 18

  val leftRoadPoints = ArrayList<Offset>()
  val rightRoadPoints = ArrayList<Offset>()
  val leftSidewalkPoints = ArrayList<Offset>()
  val rightSidewalkPoints = ArrayList<Offset>()

  for (i in 0..sliceCount) {
    val t = i / sliceCount.toFloat()
    val y = horizonY + t * t * groundH
    val rCenterX = getRoadCenterX(t)
    val rw = roadTopW + (roadBottomW - roadTopW) * (t * t)
    val sw = (canvasW * 0.10f) + (canvasW * 0.30f) * (t * t)

    leftRoadPoints.add(Offset(rCenterX - rw / 2f, y))
    rightRoadPoints.add(Offset(rCenterX + rw / 2f, y))
    leftSidewalkPoints.add(Offset(rCenterX - rw / 2f - sw, y))
    rightSidewalkPoints.add(Offset(rCenterX + rw / 2f + sw, y))
  }

  // Draw Left Sidewalk
  val leftSidewalkPath = Path().apply {
    moveTo(leftSidewalkPoints[0].x, leftSidewalkPoints[0].y)
    for (pt in leftSidewalkPoints) lineTo(pt.x, pt.y)
    for (idx in leftRoadPoints.indices.reversed()) lineTo(leftRoadPoints[idx].x, leftRoadPoints[idx].y)
    close()
  }
  drawPath(leftSidewalkPath, Color(0xFF2D323A))

  // Draw Right Sidewalk
  val rightSidewalkPath = Path().apply {
    moveTo(rightRoadPoints[0].x, rightRoadPoints[0].y)
    for (pt in rightRoadPoints) lineTo(pt.x, pt.y)
    for (idx in rightSidewalkPoints.indices.reversed()) lineTo(rightSidewalkPoints[idx].x, rightSidewalkPoints[idx].y)
    close()
  }
  drawPath(rightSidewalkPath, Color(0xFF2D323A))

  // Draw Asphalt Road Surface
  val roadPath = Path().apply {
    moveTo(leftRoadPoints[0].x, leftRoadPoints[0].y)
    for (pt in leftRoadPoints) lineTo(pt.x, pt.y)
    for (idx in rightRoadPoints.indices.reversed()) lineTo(rightRoadPoints[idx].x, rightRoadPoints[idx].y)
    close()
  }
  drawPath(roadPath, Color(0xFF1E2228))

  // Red & White Painted Curbs along the curve
  val curbSegments = 20
  for (i in 0 until curbSegments) {
    val t0 = (i / curbSegments.toFloat() + (cameraScrollZ * 0.03f) % (1f / curbSegments)) % 1f
    val t1 = (t0 + 1f / curbSegments).coerceAtMost(1f)
    val curbColor = if (i % 2 == 0) Color(0xFFD32F2F) else Color(0xFFF5F5F5)

    val y0 = horizonY + t0 * t0 * groundH
    val y1 = horizonY + t1 * t1 * groundH

    val rw0 = roadTopW + (roadBottomW - roadTopW) * (t0 * t0)
    val rw1 = roadTopW + (roadBottomW - roadTopW) * (t1 * t1)

    val cx0 = getRoadCenterX(t0)
    val cx1 = getRoadCenterX(t1)

    val strokeW = (2.5f + t0 * 8f)
    drawLine(curbColor, Offset(cx0 - rw0 / 2f, y0), Offset(cx1 - rw1 / 2f, y1), strokeWidth = strokeW)
    drawLine(curbColor, Offset(cx0 + rw0 / 2f, y0), Offset(cx1 + rw1 / 2f, y1), strokeWidth = strokeW)
  }

  // Double Solid Yellow Highway Centerline along the road curve
  val yellowColor = Color(0xFFFBC02D)
  val dashCount = 18
  for (i in 0 until dashCount) {
    val t = (i / dashCount.toFloat() + (cameraScrollZ * 0.05f) % (1f / dashCount)) % 1f
    val nextT = (t + 0.035f).coerceAtMost(1f)

    val y = horizonY + t * t * groundH
    val nextY = horizonY + nextT * nextT * groundH

    val cx = getRoadCenterX(t)
    val nextCx = getRoadCenterX(nextT)

    val gap = 3f + t * 5f
    val lineW = 2.2f + t * 5f

    drawLine(yellowColor, Offset(cx - gap, y), Offset(nextCx - gap, nextY), strokeWidth = lineW)
    drawLine(yellowColor, Offset(cx + gap, y), Offset(nextCx + gap, nextY), strokeWidth = lineW)
  }

  // ----------------------------------------------------
  // INTERSECTIONS & TURNING BRANCHES (الأنعطاف والتنقل بين الأحياء والأزقة)
  // ----------------------------------------------------
  if (activeTurnBranch != null) {
    val branchT = 0.58f
    val branchY = horizonY + branchT * branchT * groundH
    val branchCx = getRoadCenterX(branchT)
    val branchRw = roadTopW + (roadBottomW - roadTopW) * (branchT * branchT)
    val isLeft = activeTurnBranch.turnDirection < 0

    // Branching street cutout opening on the sidewalk
    val branchCutout = Path().apply {
      if (isLeft) {
        moveTo(branchCx - branchRw / 2f, branchY - 24f)
        lineTo(branchCx - branchRw / 2f - canvasW * 0.28f, branchY - 10f)
        lineTo(branchCx - branchRw / 2f - canvasW * 0.28f, branchY + 45f)
        lineTo(branchCx - branchRw / 2f, branchY + 30f)
      } else {
        moveTo(branchCx + branchRw / 2f, branchY - 24f)
        lineTo(branchCx + branchRw / 2f + canvasW * 0.28f, branchY - 10f)
        lineTo(branchCx + branchRw / 2f + canvasW * 0.28f, branchY + 45f)
        lineTo(branchCx + branchRw / 2f, branchY + 30f)
      }
      close()
    }
    drawPath(branchCutout, Color(0xFF1E2228))

    // Glowing Neon Asphalt Directional Arrow on the Road Surface
    val arrowX = if (isLeft) branchCx - branchRw * 0.25f else branchCx + branchRw * 0.25f
    val arrowPaint = Paint().apply {
      color = android.graphics.Color.YELLOW
      textSize = 14f
      typeface = Typeface.DEFAULT_BOLD
      textAlign = Paint.Align.CENTER
    }
    val arrowText = if (isLeft) "««« ⮌ ${activeTurnBranch.titleAr}" else "${activeTurnBranch.titleAr} ⮎ »»»"
    drawContext.canvas.nativeCanvas.drawText(arrowText, arrowX, branchY + 12f, arrowPaint)
  }

  // ----------------------------------------------------
  // SANA'A OVERHEAD FLYOVER BRIDGES (جسور صنعاء العلوية العابرة)
  // ----------------------------------------------------
  val isBridgeDistrict = district == SanaaDistrict.SANAA_BRIDGES
  val bridgeCycle = (cameraScrollZ * 0.012f) % 1f
  if (isBridgeDistrict || bridgeCycle in 0.45f..0.85f) {
    val bT = if (isBridgeDistrict) 0.65f else bridgeCycle
    val bridgeY = horizonY + bT * bT * groundH
    val bCenterX = getRoadCenterX(bT)
    val bScale = bT
    val bridgeSpanW = (canvasW * 0.88f) * bScale + canvasW * 0.20f
    val bridgeDeckH = 28f * bScale + 12f
    val bridgeElevation = 85f * bScale + 35f

    // Shadow cast on asphalt underneath the bridge
    drawOval(
      color = Color(0x77000000),
      topLeft = Offset(bCenterX - bridgeSpanW * 0.5f, bridgeY - 8f),
      size = Size(bridgeSpanW, 28f * bScale + 10f)
    )

    // Concrete Support Columns
    val colW = 20f * bScale + 8f
    val leftColX = bCenterX - bridgeSpanW * 0.44f
    val rightColX = bCenterX + bridgeSpanW * 0.44f
    drawRect(Color(0xFF37474F), Offset(leftColX, bridgeY - bridgeElevation), Size(colW, bridgeElevation))
    drawRect(Color(0xFF37474F), Offset(rightColX, bridgeY - bridgeElevation), Size(colW, bridgeElevation))

    // Elevated Bridge Deck Span
    val deckTopY = bridgeY - bridgeElevation - bridgeDeckH
    drawRoundRect(Color(0xFF455A64), Offset(bCenterX - bridgeSpanW * 0.5f, deckTopY), Size(bridgeSpanW, bridgeDeckH), CornerRadius(4f, 4f))
    drawLine(Color(0xFF90A4AE), Offset(bCenterX - bridgeSpanW * 0.5f, deckTopY), Offset(bCenterX + bridgeSpanW * 0.5f, deckTopY), strokeWidth = 3f)

    // Traffic visibly crossing on the elevated bridge
    if (bScale > 0.4f) {
      val carProgress = ((animTime * 90f) % bridgeSpanW)
      val carOnBridgeX = bCenterX - bridgeSpanW * 0.5f + carProgress
      drawRoundRect(Color(0xFFD32F2F), Offset(carOnBridgeX, deckTopY - 10f * bScale), Size(22f * bScale, 10f * bScale), CornerRadius(2f, 2f))
    }
  }

  // ----------------------------------------------------
  // SANA'A FAMOUS ROUNDABOUT ISLAND (جولات صنعاء: كنتاكي، المصباحي، عصر)
  // ----------------------------------------------------
  val isRoundaboutDistrict = (district == SanaaDistrict.AL_ZUBAYRI_KENTUCKY || district == SanaaDistrict.HADDAH_MESBAHI || district == SanaaDistrict.ASR_PASS)
  val roundaboutCycle = (cameraScrollZ * 0.015f) % 1f

  if (isRoundaboutDistrict || roundaboutCycle in 0.35f..0.75f) {
    val t = if (isRoundaboutDistrict) 0.52f else roundaboutCycle
    val rY = horizonY + t * t * groundH
    val rCenterX = getRoadCenterX(t)
    val rScale = t
    val islandRadiusX = 90f * rScale + 25f
    val islandRadiusY = 32f * rScale + 9f

    // Circular Curb of the Roundabout Island
    drawOval(
      color = Color(0xFFD32F2F),
      topLeft = Offset(rCenterX - islandRadiusX - 4f, rY - islandRadiusY - 2f),
      size = Size((islandRadiusX + 4f) * 2f, (islandRadiusY + 2f) * 2f)
    )
    drawOval(
      color = Color(0xFFFAFAFA),
      topLeft = Offset(rCenterX - islandRadiusX, rY - islandRadiusY),
      size = Size(islandRadiusX * 2f, islandRadiusY * 2f)
    )

    // Green Turf lawn inside the Roundabout Island
    drawOval(
      color = Color(0xFF2E7D32),
      topLeft = Offset(rCenterX - islandRadiusX * 0.92f, rY - islandRadiusY * 0.90f),
      size = Size(islandRadiusX * 1.84f, islandRadiusY * 1.80f)
    )

    // Roundabout Monument
    if (district == SanaaDistrict.HADDAH_MESBAHI) {
      val obH = 85f * rScale + 25f
      val obW = 18f * rScale + 6f
      drawRect(Color(0xFFEEEEEE), Offset(rCenterX - obW * 0.5f, rY - obH), Size(obW, obH))
      val pyrPath = Path().apply {
        moveTo(rCenterX - obW * 0.5f, rY - obH)
        lineTo(rCenterX + obW * 0.5f, rY - obH)
        lineTo(rCenterX, rY - obH - 14f * rScale)
        close()
      }
      drawPath(pyrPath, Color(0xFFFFD54F))
    } else {
      val palmCount = 3
      val palmOffsets = listOf(-islandRadiusX * 0.45f, 0f, islandRadiusX * 0.45f)
      for (pOx in palmOffsets) {
        val pX = rCenterX + pOx
        val trunkH = 65f * rScale + 20f
        drawLine(Color(0xFF5D4037), Offset(pX, rY - 4f), Offset(pX, rY - trunkH), strokeWidth = 5.5f * rScale + 2f)
        for (a in 0 until 8) {
          val ang = (a * 45f + animTime * 15f) * (PI.toFloat() / 180f)
          val frondEndX = pX + cos(ang) * (26f * rScale + 10f)
          val frondEndY = rY - trunkH + sin(ang) * (14f * rScale + 5f)
          drawLine(Color(0xFF1B5E20), Offset(pX, rY - trunkH), Offset(frondEndX, frondEndY), strokeWidth = 3f * rScale + 1.2f)
        }
      }
    }

    if (rScale > 0.35f) {
      val arrowPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (12f * rScale + 4f).coerceAtLeast(10f)
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
      }
      val roundName = when (district) {
        SanaaDistrict.HADDAH_MESBAHI -> "⮌ جولة المصباحي ⮎"
        SanaaDistrict.ASR_PASS -> "⮌ جولة عصر ⮎"
        else -> "⮌ جولة كنتاكي ⮎"
      }
      drawContext.canvas.nativeCanvas.drawText(roundName, rCenterX, rY + islandRadiusY + 16f * rScale, arrowPaint)
    }
  }
}

// ----------------------------------------------------
// 4. Buildings on BOTH SIDES of Road in 10D Scale (Tower Houses, Government HQ, Schools, Souqs, Airport)
// ----------------------------------------------------
private fun DrawScope.drawOldSanaaArchitectureAndShops(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  roadCurvature: Float,
  animTime: Float,
  housesPhoto: ImageBitmap? = null,
  shops: List<SanaaCommercialShop> = emptyList(),
  is10DScale: Boolean = true
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY

  fun getRoadCenterX(t: Float): Float {
    val curveFactor = (1f - t) * (1f - t)
    return vanishingX + (roadCurvature * canvasW * 0.30f * curveFactor)
  }

  // 6 Continuous Tiers of buildings on BOTH sides so the corridor is dense and never empty
  val buildingTiers = listOf(0.10f, 0.25f, 0.42f, 0.58f, 0.74f, 0.90f)
  val scaleMultiplier = if (is10DScale) 1.28f else 1.0f

  val leftBuildingTitles = listOf(
    "منازل صنعاء القديمة والقمريات 🏰",
    "🏛️ رئاسة الوزراء والمقرات الحكومية",
    "🏫 مدرسة جمال عبد الناصر للمتفوقين",
    "سوق الملح والبهارات والتوابل 🧂",
    "✈️ صالة مطار صنعاء ومرافق السفر",
    "مطعم الشيباني للسلتة والفحسة 🍲"
  )

  val rightBuildingTitles = listOf(
    "منازل الياجور التراثية الشاهقة 🏰",
    "🏛️ وزارة الداخلية وأمانة العاصمة",
    "🏫 مدرسة الكويت الثانوية النموذجية",
    "دكان العقيق اليماني والفضة 💍",
    "✈️ مركز الشحن الجوي والملاحة",
    "بوفية الشاي العدني والكرك ☕"
  )

  for ((idx, baseT) in buildingTiers.withIndex()) {
    val t = (baseT + (cameraScrollZ * 0.02f) % 1f) % 1f
    if (t < 0.07f) continue

    val yBottom = horizonY + t * groundH
    val scale = t
    val bWidth = (115f * scale + 55f) * scaleMultiplier
    val bHeight = (195f * scale + 85f) * scaleMultiplier

    val rCenterX = getRoadCenterX(t)
    val rw = (canvasW * 0.16f) + (canvasW * 0.76f) * (t * t)

    val stoneColor = Color(0xFF263238)
    val brickColor = if (idx % 2 == 0) Color(0xFF5D4037) else Color(0xFF6D4C41)
    val gypsumWhite = Color(0xFFF5F5F5)

    // ==========================================
    // A. LEFT SIDE BUILDING (المباني على الجانب الأيسر)
    // ==========================================
    val leftX = (rCenterX - rw * 0.5f - (canvasW * 0.08f * scale) - bWidth).coerceAtLeast(-20f)

    when (idx % 6) {
      1 -> {
        // Government HQ on left
        drawRect(Color(0xFF37474F), Offset(leftX, yBottom - bHeight), Size(bWidth, bHeight))
        // Official arch portal
        drawRoundRect(Color(0xFF263238), Offset(leftX + bWidth * 0.2f, yBottom - bHeight * 0.45f), Size(bWidth * 0.6f, bHeight * 0.45f), CornerRadius(10f, 10f))
        // Flag of Yemen waving on roof
        val fPoleX = leftX + bWidth * 0.5f
        drawLine(Color(0xFFCFD8DC), Offset(fPoleX, yBottom - bHeight), Offset(fPoleX, yBottom - bHeight - 20f * scale), strokeWidth = 2f)
        drawRect(Color(0xFFD32F2F), Offset(fPoleX, yBottom - bHeight - 20f * scale), Size(18f * scale, 4f * scale))
        drawRect(Color.White, Offset(fPoleX, yBottom - bHeight - 16f * scale), Size(18f * scale, 4f * scale))
        drawRect(Color.Black, Offset(fPoleX, yBottom - bHeight - 12f * scale), Size(18f * scale, 4f * scale))
      }
      2 -> {
        // Historic School on left
        drawRect(Color(0xFFECEFF1), Offset(leftX, yBottom - bHeight), Size(bWidth, bHeight))
        // Classroom window rows
        for (row in 1..3) {
          val wy = yBottom - bHeight * (0.25f * row)
          drawRoundRect(Color(0xFF0288D1), Offset(leftX + bWidth * 0.15f, wy), Size(bWidth * 0.7f, 12f * scale), CornerRadius(2f, 2f))
        }
      }
      else -> {
        // Traditional Tower House or Souq
        if (scale > 0.35f && housesPhoto != null && idx % 3 == 0) {
          drawImage(
            image = housesPhoto,
            dstOffset = IntOffset(leftX.toInt(), (yBottom - bHeight).toInt()),
            dstSize = IntSize(bWidth.toInt(), (bHeight * 0.72f).toInt()),
            alpha = 0.78f
          )
        } else {
          drawRect(brickColor, Offset(leftX, yBottom - bHeight), Size(bWidth, bHeight))
        }
        drawRect(stoneColor, Offset(leftX, yBottom - bHeight * 0.30f), Size(bWidth, bHeight * 0.30f))

        // Gypsum crenellations on roof
        val roofY = yBottom - bHeight
        val merlonCount = 5
        val merlonW = bWidth / merlonCount
        for (m in 0 until merlonCount) {
          val mx = leftX + m * merlonW
          val mPath = Path().apply {
            moveTo(mx, roofY)
            lineTo(mx + merlonW * 0.5f, roofY - 10f * scale)
            lineTo(mx + merlonW, roofY)
            close()
          }
          drawPath(mPath, gypsumWhite)
        }

        // Qamariyat stained glass
        if (scale > 0.22f) {
          val qY = yBottom - bHeight * 0.65f
          val qR = (9f * scale + 3f) * scaleMultiplier
          val qCenterX = leftX + bWidth * 0.5f
          drawCircle(gypsumWhite, radius = qR + 3f * scale, center = Offset(qCenterX, qY))
          drawCircle(Color(0xFFD50000), radius = qR, center = Offset(qCenterX, qY))
          drawArc(Color(0xFF00C853), 0f, 90f, true, Offset(qCenterX - qR, qY - qR), Size(qR * 2f, qR * 2f))
          drawArc(Color(0xFFFFD600), 90f, 90f, true, Offset(qCenterX - qR, qY - qR), Size(qR * 2f, qR * 2f))
        }
      }
    }

    // Left Side Signboard & Awning
    if (scale > 0.35f) {
      val awningY = yBottom - bHeight * 0.28f
      val awningH = 18f * scale * scaleMultiplier
      val awningW = bWidth * 0.94f

      val awningColor = when (idx % 3) {
        0 -> Color(0xFF1B5E20)
        1 -> Color(0xFF0D47A1)
        else -> Color(0xFFB71C1C)
      }
      drawRect(awningColor, Offset(leftX, awningY), Size(awningW, awningH))
      drawRect(Color.White, Offset(leftX + awningW * 0.25f, awningY), Size(awningW * 0.18f, awningH))
      drawRect(Color.White, Offset(leftX + awningW * 0.65f, awningY), Size(awningW * 0.18f, awningH))

      val signW = bWidth * 0.90f
      val signH = 18f * scale * scaleMultiplier
      val signX = leftX + bWidth * 0.05f
      val signY = awningY - signH - 3f

      drawRoundRect(Color(0xFF0D1B2A), Offset(signX, signY), Size(signW, signH), CornerRadius(4f, 4f))
      drawRoundRect(Color(0xFFFFD54F), Offset(signX, signY), Size(signW, signH), CornerRadius(4f, 4f), style = Stroke(width = 1.6f))

      val leftTitle = leftBuildingTitles[idx % leftBuildingTitles.size]
      drawContext.canvas.nativeCanvas.apply {
        val p = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = (11.5f * scale * scaleMultiplier).coerceAtLeast(10f)
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText(leftTitle, signX + signW * 0.5f, signY + signH * 0.72f, p)
      }
    }

    // ==========================================
    // B. RIGHT SIDE BUILDING (المباني على الجانب الأيمن)
    // ==========================================
    val rightX = (rCenterX + rw * 0.5f + (canvasW * 0.08f * scale)).coerceAtMost(canvasW + 20f)

    when (idx % 6) {
      1 -> {
        // Government Ministry on right
        drawRect(Color(0xFF455A64), Offset(rightX, yBottom - bHeight), Size(bWidth, bHeight))
        drawRoundRect(Color(0xFF1B2436), Offset(rightX + bWidth * 0.2f, yBottom - bHeight * 0.45f), Size(bWidth * 0.6f, bHeight * 0.45f), CornerRadius(10f, 10f))
        val fPoleX = rightX + bWidth * 0.5f
        drawLine(Color(0xFFCFD8DC), Offset(fPoleX, yBottom - bHeight), Offset(fPoleX, yBottom - bHeight - 20f * scale), strokeWidth = 2f)
        drawRect(Color(0xFFD32F2F), Offset(fPoleX, yBottom - bHeight - 20f * scale), Size(18f * scale, 4f * scale))
        drawRect(Color.White, Offset(fPoleX, yBottom - bHeight - 16f * scale), Size(18f * scale, 4f * scale))
        drawRect(Color.Black, Offset(fPoleX, yBottom - bHeight - 12f * scale), Size(18f * scale, 4f * scale))
      }
      2 -> {
        // Kuwait Secondary School on right
        drawRect(Color(0xFFCFD8DC), Offset(rightX, yBottom - bHeight), Size(bWidth, bHeight))
        for (row in 1..3) {
          val wy = yBottom - bHeight * (0.25f * row)
          drawRoundRect(Color(0xFF1565C0), Offset(rightX + bWidth * 0.15f, wy), Size(bWidth * 0.7f, 12f * scale), CornerRadius(2f, 2f))
        }
      }
      else -> {
        if (scale > 0.35f && housesPhoto != null && idx % 3 == 1) {
          drawImage(
            image = housesPhoto,
            dstOffset = IntOffset(rightX.toInt(), (yBottom - bHeight).toInt()),
            dstSize = IntSize(bWidth.toInt(), (bHeight * 0.72f).toInt()),
            alpha = 0.78f
          )
        } else {
          drawRect(if (idx % 2 == 0) Color(0xFF4E342E) else Color(0xFF5D4037), Offset(rightX, yBottom - bHeight), Size(bWidth, bHeight))
        }
        drawRect(stoneColor, Offset(rightX, yBottom - bHeight * 0.30f), Size(bWidth, bHeight * 0.30f))

        val roofY = yBottom - bHeight
        val merlonCount = 5
        val merlonW = bWidth / merlonCount
        for (m in 0 until merlonCount) {
          val mx = rightX + m * merlonW
          val mPath = Path().apply {
            moveTo(mx, roofY)
            lineTo(mx + merlonW * 0.5f, roofY - 10f * scale)
            lineTo(mx + merlonW, roofY)
            close()
          }
          drawPath(mPath, gypsumWhite)
        }

        if (scale > 0.22f) {
          val qY = yBottom - bHeight * 0.65f
          val qR = (9f * scale + 3f) * scaleMultiplier
          val qCenterX = rightX + bWidth * 0.5f
          drawCircle(gypsumWhite, radius = qR + 3f * scale, center = Offset(qCenterX, qY))
          drawCircle(Color(0xFF1565C0), radius = qR, center = Offset(qCenterX, qY))
          drawArc(Color(0xFFFFD600), 0f, 90f, true, Offset(qCenterX - qR, qY - qR), Size(qR * 2f, qR * 2f))
          drawArc(Color(0xFF00C853), 90f, 90f, true, Offset(qCenterX - qR, qY - qR), Size(qR * 2f, qR * 2f))
        }
      }
    }

    // Right Side Signboard & Awning
    if (scale > 0.35f) {
      val awningY = yBottom - bHeight * 0.28f
      val awningH = 18f * scale * scaleMultiplier
      val awningW = bWidth * 0.94f

      val awningColor = when (idx % 3) {
        0 -> Color(0xFF0D47A1)
        1 -> Color(0xFF1B5E20)
        else -> Color(0xFFE65100)
      }
      drawRect(awningColor, Offset(rightX, awningY), Size(awningW, awningH))
      drawRect(Color.White, Offset(rightX + awningW * 0.25f, awningY), Size(awningW * 0.18f, awningH))
      drawRect(Color.White, Offset(rightX + awningW * 0.65f, awningY), Size(awningW * 0.18f, awningH))

      val signW = bWidth * 0.90f
      val signH = 18f * scale * scaleMultiplier
      val signX = rightX + bWidth * 0.05f
      val signY = awningY - signH - 3f

      drawRoundRect(Color(0xFF1A237E), Offset(signX, signY), Size(signW, signH), CornerRadius(4f, 4f))
      drawRoundRect(Color(0xFFFFD54F), Offset(signX, signY), Size(signW, signH), CornerRadius(4f, 4f), style = Stroke(width = 1.6f))

      val rightTitle = rightBuildingTitles[idx % rightBuildingTitles.size]
      drawContext.canvas.nativeCanvas.apply {
        val p = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = (11.5f * scale * scaleMultiplier).coerceAtLeast(10f)
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText(rightTitle, signX + signW * 0.5f, signY + signH * 0.72f, p)
      }
    }
  }
}

// ----------------------------------------------------
// 5. Overhead Highway Signs & Electric Cables (لوحات الشوارع المعلقة والأسلاك)
// ----------------------------------------------------
private fun DrawScope.drawSanaaOverheadSignsAndWires(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  district: SanaaDistrict
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY

  val signTiers = listOf(0.40f, 0.85f)
  for ((idx, baseT) in signTiers.withIndex()) {
    val t = (baseT + (cameraScrollZ * 0.035f) % 1f) % 1f
    if (t !in 0.18f..0.92f) continue

    val y = horizonY + t * t * groundH
    val scale = t
    val roadW = (canvasW * 0.16f) + (canvasW * 0.76f) * (t * t)

    val gantryH = 75f * scale + 30f
    val gantryY = y - gantryH

    val leftPostX = vanishingX - roadW * 0.58f
    val rightPostX = vanishingX + roadW * 0.58f

    // Gantry Posts
    drawLine(Color(0xFF78909C), Offset(leftPostX, y), Offset(leftPostX, gantryY), strokeWidth = 3.5f * scale + 1.5f)
    drawLine(Color(0xFF78909C), Offset(rightPostX, y), Offset(rightPostX, gantryY), strokeWidth = 3.5f * scale + 1.5f)
    drawLine(Color(0xFF546E7A), Offset(leftPostX, gantryY), Offset(rightPostX, gantryY), strokeWidth = 4.5f * scale + 2f)

    // Green Signboard (Sana'a Traffic Direction Authority)
    val signW = roadW * 0.85f
    val signH = 28f * scale + 12f
    val signTopY = gantryY - signH + 4f * scale
    val signLeftX = vanishingX - signW * 0.5f

    drawRoundRect(Color(0xFF1B5E20), Offset(signLeftX, signTopY), Size(signW, signH), CornerRadius(4f * scale, 4f * scale))
    drawRoundRect(Color.White, Offset(signLeftX, signTopY), Size(signW, signH), CornerRadius(4f * scale, 4f * scale), style = Stroke(width = 2f * scale + 1f))

    if (scale > 0.32f) {
      val signText = when (district) {
        SanaaDistrict.BAB_AL_YEMEN -> "⬋ سوق الملح | باب اليمن ⬈"
        SanaaDistrict.AL_SALEH_MOSQUE -> "⬋ جامع الصالح | ميدان السبعين ⬈"
        SanaaDistrict.AL_ZUBAYRI_KENTUCKY -> "⬋ جولة كنتاكي | شارع الزبيري ⬈"
        SanaaDistrict.HADDAH_MESBAHI -> "⬋ جولة المصباحي | شارع حدة ⬈"
        SanaaDistrict.AL_THAWRA_STADIUM -> "⬋ المدينة الرياضية | ستاد الثورة ⬈"
        else -> "⬋ باب اليمن | شارع السبعين ➔"
      }
      drawContext.canvas.nativeCanvas.apply {
        val p = Paint().apply {
          color = android.graphics.Color.WHITE
          textSize = (13f * scale + 5f).coerceAtLeast(11f)
          typeface = Typeface.DEFAULT_BOLD
          textAlign = Paint.Align.CENTER
        }
        drawText(signText, vanishingX, signTopY + signH * 0.68f, p)
      }
    }
  }

  // Iconic crisscrossing overhead electrical cables
  val wireColor = Color(0x99111827)
  val wireY1 = horizonY + 22f
  val wirePath1 = Path().apply {
    moveTo(0f, wireY1)
    quadraticBezierTo(canvasW * 0.5f, wireY1 + 22f, canvasW, wireY1 - 6f)
  }
  drawPath(wirePath1, wireColor, style = Stroke(width = 1.6f))
}

// ----------------------------------------------------
// 6. Street Trees & Traditional Streetlights
// ----------------------------------------------------
private fun DrawScope.drawTreesAndStreetlights(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  is10DScale: Boolean = true
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY
  val lampTiers = listOf(0.18f, 0.48f, 0.84f)
  val scaleMultiplier = if (is10DScale) 1.25f else 1.0f

  for (baseT in lampTiers) {
    val t = (baseT + (cameraScrollZ * 0.04f) % 1f) % 1f
    if (t < 0.08f) continue

    val y = horizonY + t * t * groundH
    val scale = t
    val roadW = (canvasW * 0.16f) + (canvasW * 0.76f) * (t * t)

    // Left Street Tree (Lush Date Palm or Acacia Tree)
    val treeX = vanishingX - roadW / 2f - (32f * scale * scaleMultiplier)
    val trunkH = (75f * scale + 20f) * scaleMultiplier
    val foliageR = (38f * scale + 15f) * scaleMultiplier

    drawOval(Color(0x55000000), Offset(treeX - foliageR * 0.8f, y - 5f), Size(foliageR * 1.6f, foliageR * 0.5f))
    drawLine(Color(0xFF3E2723), Offset(treeX, y), Offset(treeX, y - trunkH), strokeWidth = 6f * scale + 2.5f)
    drawCircle(Color(0xFF1B5E20), radius = foliageR, center = Offset(treeX, y - trunkH - foliageR * 0.5f))
    drawCircle(Color(0xFF2E7D32), radius = foliageR * 0.82f, center = Offset(treeX - 4f * scale, y - trunkH - foliageR * 0.6f))

    // Right Streetlight with warm radial illumination
    val lampX = vanishingX + roadW / 2f + (18f * scale * scaleMultiplier)
    val poleH = (98f * scale + 30f) * scaleMultiplier
    val coneR = (68f * scale + 24f) * scaleMultiplier

    drawOval(
      brush = Brush.radialGradient(
        colors = listOf(Color(0x66FFE082), Color(0x22FFD54F), Color.Transparent),
        center = Offset(lampX - 14f * scale, y),
        radius = coneR
      ),
      topLeft = Offset(lampX - coneR, y - coneR * 0.35f),
      size = Size(coneR * 2f, coneR * 0.7f)
    )

    val polePath = Path().apply {
      moveTo(lampX, y)
      lineTo(lampX, y - poleH)
      quadraticBezierTo(lampX, y - poleH - 16f * scale, lampX - 18f * scale, y - poleH - 14f * scale)
    }
    drawPath(polePath, Color(0xFF9E9E9E), style = Stroke(width = 3.5f * scale + 1.8f))

    val bulbPos = Offset(lampX - 18f * scale, y - poleH - 14f * scale)
    drawCircle(Color(0xFFFFF9C4), radius = 5.5f * scale + 2.5f, center = bulbPos)
    drawCircle(Color(0x88FFD54F), radius = 13f * scale + 5f, center = bulbPos)
  }
}

// ----------------------------------------------------
// 7. Sana'ani Pedestrians Walking & Crossing in 10D Scale (الناس تمر في الشوارع)
// ----------------------------------------------------
private fun DrawScope.drawSanaaPedestrians(
  pedestrians: List<SanaaPedestrian>,
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  walkCycleTick: Float,
  is10DScale: Boolean = true
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY
  val scaleMultiplier = if (is10DScale) 1.35f else 1.0f

  val activePedestrians = if (pedestrians.isNotEmpty()) pedestrians else listOf(
    SanaaPedestrian(1, 18f, -1.05f, SanaaPedestrianType.SANAANI_MAN, 0.7f),
    SanaaPedestrian(2, 42f, 1.05f, SanaaPedestrianType.STREET_POTATO_VENDOR, 0.5f),
    SanaaPedestrian(3, 68f, -1.08f, SanaaPedestrianType.ELDER_WITH_CANE, 0.4f),
    SanaaPedestrian(4, 88f, 1.06f, SanaaPedestrianType.SANAANI_SITARA_WOMAN, 0.6f)
  )

  for (ped in activePedestrians) {
    val t = ((ped.worldZ - cameraScrollZ) % 100f + 100f) % 100f / 100f
    if (t !in 0.12f..0.94f) continue

    val y = horizonY + t * t * groundH
    val scale = t
    val roadW = (canvasW * 0.16f) + (canvasW * 0.76f) * (t * t)

    val pedX = vanishingX + (ped.sideOffset * (roadW * 0.5f + 25f * scale))
    val pedH = (65f * scale + 22f) * scaleMultiplier
    val pedW = (18f * scale + 7f) * scaleMultiplier

    val legCycle = sin(walkCycleTick + ped.id) * 8f * scale
    val armCycle = -legCycle

    // Ground Shadow
    drawOval(Color(0x66000000), Offset(pedX - pedW * 0.6f, y - 3f), Size(pedW * 1.2f, 8f * scale))

    // Legs & Thobe
    val legY = y - pedH * 0.45f
    drawLine(Color(0xFF263238), Offset(pedX - 3f * scale, legY), Offset(pedX - 3f * scale + legCycle, y), strokeWidth = 3.5f * scale + 1.2f)
    drawLine(Color(0xFF263238), Offset(pedX + 3f * scale, legY), Offset(pedX + 3f * scale - legCycle, y), strokeWidth = 3.5f * scale + 1.2f)

    // Body Thobe / Dress
    val torsoY = y - pedH * 0.85f
    val torsoH = pedH * 0.50f
    drawRoundRect(ped.type.thobeColor, Offset(pedX - pedW * 0.5f, torsoY), Size(pedW, torsoH), CornerRadius(3f, 3f))

    // Shawl / Shemagh over shoulders
    drawRect(ped.type.shawlColor, Offset(pedX - pedW * 0.52f, torsoY), Size(pedW * 1.04f, torsoH * 0.35f))

    // Golden Jambiya at waist
    if (ped.type.hasJambiya && scale > 0.28f) {
      val jY = torsoY + torsoH - 2f
      drawRoundRect(Color(0xFFFFD54F), Offset(pedX - 4f * scale, jY), Size(8f * scale, 5f * scale), CornerRadius(1f, 1f))
      // Curved blade tip
      val jPath = Path().apply {
        moveTo(pedX, jY + 5f * scale)
        quadraticBezierTo(pedX - 3f * scale, jY + 10f * scale, pedX + 3f * scale, jY + 11f * scale)
      }
      drawPath(jPath, Color(0xFFEEEEEE), style = Stroke(width = 1.8f * scale))
    }

    // Street Vendor Cart (عربة بائع البطاط الخشبية)
    if (ped.type.hasCart && scale > 0.30f) {
      val cartW = 28f * scale * scaleMultiplier
      val cartH = 18f * scale * scaleMultiplier
      val cartX = pedX - cartW * 0.5f
      val cartY = y - cartH - 4f
      drawRect(Color(0xFF8D6E63), Offset(cartX, cartY), Size(cartW, cartH))
      // Wheels
      drawCircle(Color(0xFF3E2723), radius = 5f * scale, center = Offset(cartX + 4f, y - 4f))
      drawCircle(Color(0xFF3E2723), radius = 5f * scale, center = Offset(cartX + cartW - 4f, y - 4f))
      // Steaming pot on cart (قدر البطاط الصنعاني الساخن)
      drawRoundRect(Color(0xFFCFD8DC), Offset(cartX + cartW * 0.3f, cartY - 7f * scale), Size(cartW * 0.4f, 7f * scale), CornerRadius(2f, 2f))
    }

    // Elder Walking Cane (عكاز الحاج الصنعاني)
    if (ped.type.isElder && scale > 0.30f) {
      drawLine(Color(0xFF5D4037), Offset(pedX + pedW * 0.6f, torsoY + torsoH * 0.4f), Offset(pedX + pedW * 0.8f, y), strokeWidth = 2.2f * scale)
    }

    // Arms
    val armY = torsoY + 4f
    drawLine(ped.type.thobeColor, Offset(pedX - pedW * 0.5f, armY), Offset(pedX - pedW * 0.5f - 4f * scale, armY + 12f * scale + armCycle), strokeWidth = 3f * scale)
    drawLine(ped.type.thobeColor, Offset(pedX + pedW * 0.5f, armY), Offset(pedX + pedW * 0.5f + 4f * scale, armY + 12f * scale - armCycle), strokeWidth = 3f * scale)

    // Head & Yemeni Turban
    val headR = 5.5f * scale * scaleMultiplier
    val headY = torsoY - headR
    drawCircle(Color(0xFFD7A177), radius = headR, center = Offset(pedX, headY))
    drawCircle(ped.type.shawlColor, radius = headR * 0.95f, center = Offset(pedX, headY - 2.5f * scale))
  }
}

// ----------------------------------------------------
// 8. Authentic Yemeni Traffic Vehicles in 10D Real-Life Scale
// ----------------------------------------------------
private fun DrawScope.drawTrafficVehicles(
  vehicles: List<TrafficCar>,
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  cameraScrollZ: Float,
  animTime: Float,
  is10DScale: Boolean = true
) {
  val vanishingX = canvasW * 0.5f
  val groundH = canvasH - horizonY
  val scaleMultiplier = if (is10DScale) 1.55f else 1.0f

  for (car in vehicles) {
    val t = ((car.worldZ - cameraScrollZ) % 100f + 100f) % 100f / 100f
    if (t !in 0.10f..0.94f) continue

    val y = horizonY + t * t * groundH
    val scale = t
    val roadW = (canvasW * 0.16f) + (canvasW * 0.76f) * (t * t)

    val laneOffset = if (car.laneIndex < 0) -roadW * 0.28f else roadW * 0.28f
    val carX = vanishingX + laneOffset
    val carW = (85f * scale + 30f) * scaleMultiplier
    val carH = (52f * scale + 20f) * scaleMultiplier

    // 10D Vehicle Ground Shadow
    drawOval(Color(0x88000000), Offset(carX - carW * 0.54f, y - 6f), Size(carW * 1.08f, carH * 0.40f))

    when (car.carType) {
      // 1. TOYOTA SHAS 4x4 PICKUP (شاص يمني بيك آب تكتيكي)
      "SHAS" -> {
        // Big 4x4 Rugged Wheels with steel rims & tread
        val wheelR = (9.5f * scale + 3.5f) * scaleMultiplier
        drawCircle(Color(0xFF1E1E1E), wheelR, Offset(carX - carW * 0.38f, y - 3f))
        drawCircle(Color(0xFF1E1E1E), wheelR, Offset(carX + carW * 0.38f, y - 3f))
        drawCircle(Color(0xFFB0BEC5), wheelR * 0.45f, Offset(carX - carW * 0.38f, y - 3f))
        drawCircle(Color(0xFFB0BEC5), wheelR * 0.45f, Offset(carX + carW * 0.38f, y - 3f))

        // Truck Body (Desert Beige / Silver)
        val bodyColor = car.color
        drawRoundRect(bodyColor, Offset(carX - carW * 0.5f, y - carH), Size(carW, carH * 0.85f), CornerRadius(6f * scale, 6f * scale))

        // Cargo Bed & Black Steel Roll Bar (الشد الحديدي والقعادة)
        drawRect(Color(0x33000000), Offset(carX - carW * 0.44f, y - carH * 0.72f), Size(carW * 0.88f, carH * 0.42f))
        drawLine(Color(0xFF263238), Offset(carX - carW * 0.40f, y - carH * 0.68f), Offset(carX - carW * 0.40f, y - carH * 1.08f), strokeWidth = 3f * scale)
        drawLine(Color(0xFF263238), Offset(carX + carW * 0.40f, y - carH * 0.68f), Offset(carX + carW * 0.40f, y - carH * 1.08f), strokeWidth = 3f * scale)
        drawLine(Color(0xFF263238), Offset(carX - carW * 0.40f, y - carH * 1.08f), Offset(carX + carW * 0.40f, y - carH * 1.08f), strokeWidth = 3f * scale)

        // Rear window
        drawRect(Color(0xFF1E293B), Offset(carX - carW * 0.34f, y - carH * 0.98f), Size(carW * 0.68f, carH * 0.30f))

        // "TOYOTA" Lettering on Tailgate
        if (car.laneIndex > 0 && scale > 0.40f) {
          drawContext.canvas.nativeCanvas.apply {
            val p = Paint().apply {
              color = android.graphics.Color.WHITE
              textSize = (9f * scale + 3f) * scaleMultiplier
              typeface = Typeface.DEFAULT_BOLD
              textAlign = Paint.Align.CENTER
            }
            drawText("TOYOTA 4x4", carX, y - carH * 0.28f, p)
          }
        }

        // Realistic Headlight / Taillight Beams projecting on road
        val lightY = y - carH * 0.25f
        if (car.laneIndex > 0) {
          drawCircle(Color(0xFFFF1744), radius = 4.5f * scale * scaleMultiplier, center = Offset(carX - carW * 0.44f, lightY))
          drawCircle(Color(0xFFFF1744), radius = 4.5f * scale * scaleMultiplier, center = Offset(carX + carW * 0.44f, lightY))
        } else {
          // Intense Headlight Cones forward
          drawCircle(Color(0xFFFFF9C4), radius = 5.5f * scale * scaleMultiplier, center = Offset(carX - carW * 0.42f, lightY))
          drawCircle(Color(0xFFFFF9C4), radius = 5.5f * scale * scaleMultiplier, center = Offset(carX + carW * 0.42f, lightY))
          drawOval(
            brush = Brush.radialGradient(
              colors = listOf(Color(0x55FFF9C4), Color.Transparent),
              center = Offset(carX, y + 25f * scale),
              radius = 50f * scale
            ),
            topLeft = Offset(carX - 50f * scale, y),
            size = Size(100f * scale, 50f * scale)
          )
        }
      }

      // 2. SANA'A YELLOW DABAB BUS (دباب ركاب صنعاء الأصفر مع الشطرنج والعفش)
      "DABAB" -> {
        val busColor = Color(0xFFFBC02D)
        drawRoundRect(busColor, Offset(carX - carW * 0.5f, y - carH * 1.18f), Size(carW, carH * 1.10f), CornerRadius(8f * scale, 8f * scale))

        // Checkered Waistline Stripe (شطرنج دباب صنعاء)
        val stripeY = y - carH * 0.58f
        val stripeH = 7f * scale * scaleMultiplier
        val checkCount = 8
        val checkW = carW / checkCount
        for (c in 0 until checkCount) {
          val cx = carX - carW * 0.5f + c * checkW
          val cColor = if (c % 2 == 0) Color.Black else Color.White
          drawRect(cColor, Offset(cx, stripeY), Size(checkW, stripeH))
        }

        // Roof Luggage Carrier loaded with sacks & parcels (عفش الركاب)
        val rackY = y - carH * 1.18f - 11f * scale * scaleMultiplier
        val rackH = 9f * scale * scaleMultiplier
        drawRect(Color(0xFF37474F), Offset(carX - carW * 0.44f, rackY), Size(carW * 0.88f, rackH))
        drawCircle(Color(0xFF8D6E63), radius = 6f * scale * scaleMultiplier, center = Offset(carX - carW * 0.22f, rackY + 2f))
        drawCircle(Color(0xFF1976D2), radius = 6.5f * scale * scaleMultiplier, center = Offset(carX + carW * 0.12f, rackY + 2f))
        drawCircle(Color(0xFF558B2F), radius = 5.5f * scale * scaleMultiplier, center = Offset(carX + carW * 0.32f, rackY + 2f))

        // Destination Board ("باب اليمن ⇋ السبعين")
        drawRect(Color(0xFF1E293B), Offset(carX - carW * 0.40f, y - carH * 1.08f), Size(carW * 0.80f, carH * 0.44f))

        if (car.laneIndex > 0 && scale > 0.42f) {
          drawContext.canvas.nativeCanvas.apply {
            val p = Paint().apply {
              color = android.graphics.Color.WHITE
              textSize = (8f * scale + 3f) * scaleMultiplier
              typeface = Typeface.DEFAULT_BOLD
              textAlign = Paint.Align.CENTER
            }
            drawText("باب اليمن - السبعين", carX, y - carH * 0.78f, p)
          }
        }

        // Taillights
        val lightY = y - carH * 0.22f
        drawCircle(Color(0xFFFF1744), radius = 5f * scale * scaleMultiplier, center = Offset(carX - carW * 0.44f, lightY))
        drawCircle(Color(0xFFFF1744), radius = 5f * scale * scaleMultiplier, center = Offset(carX + carW * 0.44f, lightY))
      }

      // 3. SANA'A TAXI (تاكسي أجرة صنعاء - أبيض وأبواب صفراء)
      "TAXI" -> {
        drawRoundRect(Color(0xFFECEFF1), Offset(carX - carW * 0.5f, y - carH), Size(carW, carH * 0.90f), CornerRadius(7f * scale, 7f * scale))
        drawRect(Color(0xFFFBC02D), Offset(carX - carW * 0.48f, y - carH * 0.56f), Size(carW * 0.96f, carH * 0.34f))

        // Glowing illuminated Taxi rooftop light (أجرة)
        val taxiSignW = 20f * scale * scaleMultiplier + 8f
        val taxiSignH = 8f * scale * scaleMultiplier + 3f
        val taxiSignY = y - carH - taxiSignH
        drawRoundRect(Color(0xFFFFEB3B), Offset(carX - taxiSignW * 0.5f, taxiSignY), Size(taxiSignW, taxiSignH), CornerRadius(3f, 3f))

        drawRect(Color(0xFF1E293B), Offset(carX - carW * 0.38f, y - carH * 0.94f), Size(carW * 0.76f, carH * 0.42f))

        val lightY = y - carH * 0.24f
        drawCircle(Color(0xFFFF1744), radius = 4.5f * scale * scaleMultiplier, center = Offset(carX - carW * 0.44f, lightY))
        drawCircle(Color(0xFFFF1744), radius = 4.5f * scale * scaleMultiplier, center = Offset(carX + carW * 0.44f, lightY))
      }

      // 4. POLICE PATROL CRUISER (دورية شرطة النجدة الصنعانية مع إضاءة طوارئ نابضة)
      else -> {
        drawRoundRect(Color(0xFF0D47A1), Offset(carX - carW * 0.5f, y - carH), Size(carW, carH * 0.90f), CornerRadius(7f * scale, 7f * scale))
        drawRect(Color.White, Offset(carX - carW * 0.48f, y - carH * 0.58f), Size(carW * 0.96f, carH * 0.34f))

        // Emergency Flashing Siren Lightbar
        val flashState = (sin(animTime * 16f) > 0f)
        val leftLightColor = if (flashState) Color(0xFFFF1744) else Color(0xFF2979FF)
        val rightLightColor = if (flashState) Color(0xFF2979FF) else Color(0xFFFF1744)

        val lightbarW = 26f * scale * scaleMultiplier + 10f
        val lightbarH = 8f * scale * scaleMultiplier + 3f
        val lightbarY = y - carH - lightbarH

        drawRoundRect(Color(0xFF212121), Offset(carX - lightbarW * 0.5f, lightbarY), Size(lightbarW, lightbarH), CornerRadius(2f, 2f))
        drawCircle(leftLightColor, radius = 5f * scale * scaleMultiplier, center = Offset(carX - lightbarW * 0.26f, lightbarY + lightbarH * 0.5f))
        drawCircle(rightLightColor, radius = 5f * scale * scaleMultiplier, center = Offset(carX + lightbarW * 0.26f, lightbarY + lightbarH * 0.5f))

        // Pulsating emergency colored light flash on the asphalt
        drawCircle(
          color = if (flashState) Color(0x33FF1744) else Color(0x332979FF),
          radius = 50f * scale * scaleMultiplier,
          center = Offset(carX, y)
        )
      }
    }
  }
}

// ----------------------------------------------------
// 9. Projectiles (المقذوفات ورذاذ الغرافيتي)
// ----------------------------------------------------
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
        drawLine(Color(0xFFFFEB3B), Offset(px, py), Offset(px, py - 28f), strokeWidth = 4.2f)
        drawCircle(Color(0xFFFF5722), radius = 5.5f, center = Offset(px, py))
      }
      "SLING_SHOT" -> {
        drawCircle(Color(0xFFB0BEC5), radius = 6.5f, center = Offset(px, py))
      }
      "SPRAY" -> {
        drawCircle(Color(0xFF00E5FF).copy(alpha = 0.65f * p.lifeTime), radius = 18f, center = Offset(px, py))
      }
      else -> {
        drawCircle(Color(0xFFFF3D00), radius = 9f, center = Offset(px, py))
      }
    }
  }
}

// ----------------------------------------------------
// 10. Player Character in 10D Real-Life Human Scale (بالمعوز الصنعاني والجنبية والشال)
// ----------------------------------------------------
private fun DrawScope.drawPlayerCharacter(
  hero: UnifiedHeroId,
  playerX: Float,
  jumpHeight: Float,
  walkCycle: Float,
  isWalking: Boolean,
  isRunning: Boolean,
  isInsideVehicle: Boolean,
  playerAngleDeg: Float,
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  animTime: Float,
  is10DScale: Boolean = true
) {
  val centerX = canvasW * 0.5f + (playerX * canvasW * 0.35f)
  val baseGroundY = canvasH * 0.82f
  val jumpOffsetY = jumpHeight * 110f
  val playerY = baseGroundY - jumpOffsetY
  val sMultiplier = if (is10DScale) 1.48f else 1.0f

  if (isInsideVehicle) {
    // ----------------------------------------------------
    // In-Vehicle Mode: Real-Scale Toyota Shas Pickup or Yellow Dabab
    // ----------------------------------------------------
    val vehicleW = 190f * sMultiplier
    val vehicleH = 115f * sMultiplier

    // Dynamic ground shadow
    drawOval(Color(0x88000000), Offset(centerX - vehicleW * 0.55f, baseGroundY - 10f), Size(vehicleW * 1.1f, 44f * sMultiplier))

    val bodyColor = if (hero == UnifiedHeroId.AMMAR) Color(0xFFFBC02D) else Color(0xFFD7CCC8)
    drawRoundRect(
      color = bodyColor,
      topLeft = Offset(centerX - vehicleW * 0.5f, playerY - vehicleH),
      size = Size(vehicleW, vehicleH),
      cornerRadius = CornerRadius(14f * sMultiplier, 14f * sMultiplier)
    )

    // Luggage rack & rear window
    drawRect(Color(0xFF37474F), Offset(centerX - vehicleW * 0.42f, playerY - vehicleH - 14f * sMultiplier), Size(vehicleW * 0.84f, 14f * sMultiplier))
    drawRect(Color(0xFF1E293B), Offset(centerX - vehicleW * 0.38f, playerY - vehicleH + 14f * sMultiplier), Size(vehicleW * 0.76f, 36f * sMultiplier))

    // Taillights
    drawCircle(Color(0xFFFF1744), radius = 9f * sMultiplier, center = Offset(centerX - vehicleW * 0.44f, playerY - 22f * sMultiplier))
    drawCircle(Color(0xFFFF1744), radius = 9f * sMultiplier, center = Offset(centerX + vehicleW * 0.44f, playerY - 22f * sMultiplier))

    // Yemeni License Plate ("اليمن - صنعاء")
    val plateW = 55f * sMultiplier
    val plateH = 22f * sMultiplier
    drawRoundRect(Color(0xFFEEEEEE), Offset(centerX - plateW * 0.5f, playerY - 30f * sMultiplier), Size(plateW, plateH), CornerRadius(4f, 4f))
    drawRect(Color(0xFF1565C0), Offset(centerX - plateW * 0.45f, playerY - 28f * sMultiplier), Size(11f * sMultiplier, 16f * sMultiplier))

    // Exhaust smoke puffs when moving
    if (isWalking || isRunning) {
      val smokeX = centerX + vehicleW * 0.42f
      val smokeY = playerY - 16f * sMultiplier
      val smokeRadius = (sin(animTime * 12f) + 1.5f) * 6.5f * sMultiplier
      drawCircle(Color(0x6690A4AE), radius = smokeRadius, center = Offset(smokeX, smokeY))
    }
    return
  }

  // ----------------------------------------------------
  // On-Foot Mode: 10D Realistic Scale Yemeni Hero
  // ----------------------------------------------------
  val shadowScale = (1f - jumpHeight * 0.4f).coerceAtLeast(0.5f)
  val baseShadowW = 42f * sMultiplier
  drawOval(
    color = Color(0x77000000),
    topLeft = Offset(centerX - baseShadowW * 0.5f * shadowScale, baseGroundY - 8f),
    size = Size(baseShadowW * shadowScale, 22f * sMultiplier * shadowScale)
  )

  val isMoving = isWalking || isRunning
  val strideMultiplier = if (isRunning) 1.7f else 1.0f
  val legStride = if (isMoving) sin(walkCycle) * 22f * strideMultiplier * sMultiplier else 0f
  val armStride = if (isMoving) -sin(walkCycle) * 18f * strideMultiplier * sMultiplier else 0f

  // Dust Puffs when running or landing
  if (isMoving && abs(sin(walkCycle)) > 0.7f && jumpHeight <= 0.05f) {
    val dustFootX = if (sin(walkCycle) > 0f) centerX - 16f * sMultiplier else centerX + 16f * sMultiplier
    val dustR = (abs(sin(walkCycle)) * 8.5f) * strideMultiplier * sMultiplier
    drawCircle(Color(0x66BDBDBD), radius = dustR, center = Offset(dustFootX, baseGroundY - 5f))
    drawCircle(Color(0x44EEEEEE), radius = dustR * 0.6f, center = Offset(dustFootX + 4f, baseGroundY - 9f))
  }

  // 1. Legs & Dark Trousers
  val hipY = playerY - 68f * sMultiplier
  val footY = playerY

  val leftFootX = centerX - 14f * sMultiplier + legStride * 0.6f
  val leftFootY = footY - abs(cos(walkCycle)) * (if (isMoving) 7f * sMultiplier else 0f)
  drawLine(Color(0xFF1E293B), Offset(centerX - 10f * sMultiplier, hipY), Offset(leftFootX, leftFootY), strokeWidth = 16f * sMultiplier, cap = StrokeCap.Round)
  drawRoundRect(Color(0xFFEEEEEE), Offset(leftFootX - 10f * sMultiplier, leftFootY - 8f * sMultiplier), Size(20f * sMultiplier, 11f * sMultiplier), CornerRadius(4f, 4f))

  val rightFootX = centerX + 14f * sMultiplier - legStride * 0.6f
  val rightFootY = footY - abs(sin(walkCycle)) * (if (isMoving) 7f * sMultiplier else 0f)
  drawLine(Color(0xFF1E293B), Offset(centerX + 10f * sMultiplier, hipY), Offset(rightFootX, rightFootY), strokeWidth = 16f * sMultiplier, cap = StrokeCap.Round)
  drawRoundRect(Color(0xFFEEEEEE), Offset(rightFootX - 10f * sMultiplier, rightFootY - 8f * sMultiplier), Size(20f * sMultiplier, 11f * sMultiplier), CornerRadius(4f, 4f))

  // 2. Authentic Embroidered Yemeni Ma'waz (معوز يماني مطرز بزخارف أفقية ملونة)
  val mawazTopY = hipY - 8f * sMultiplier
  val mawazW = 48f * sMultiplier
  val mawazH = 32f * sMultiplier
  drawRoundRect(Color(0xFF37474F), Offset(centerX - mawazW * 0.5f, mawazTopY), Size(mawazW, mawazH), CornerRadius(5f, 5f))
  drawLine(Color(0xFFD32F2F), Offset(centerX - mawazW * 0.5f, mawazTopY + 8f * sMultiplier), Offset(centerX + mawazW * 0.5f, mawazTopY + 8f * sMultiplier), strokeWidth = 3.5f * sMultiplier)
  drawLine(Color(0xFFF5C518), Offset(centerX - mawazW * 0.5f, mawazTopY + 16f * sMultiplier), Offset(centerX + mawazW * 0.5f, mawazTopY + 16f * sMultiplier), strokeWidth = 3f * sMultiplier)
  drawLine(Color(0xFF1976D2), Offset(centerX - mawazW * 0.5f, mawazTopY + 24f * sMultiplier), Offset(centerX + mawazW * 0.5f, mawazTopY + 24f * sMultiplier), strokeWidth = 3f * sMultiplier)

  // 3. Torso: Authentic Shirt with Back Emblem
  val torsoTopY = playerY - 118f * sMultiplier
  val torsoH = 50f * sMultiplier
  val torsoW = 48f * sMultiplier

  drawRoundRect(hero.shirtColor, Offset(centerX - torsoW * 0.5f, torsoTopY), Size(torsoW, torsoH), CornerRadius(8f, 8f))

  // White Circular Emblem on Back
  drawCircle(Color.White.copy(alpha = 0.95f), radius = 11f * sMultiplier, center = Offset(centerX, torsoTopY + torsoH * 0.42f))
  drawCircle(hero.shirtColor, radius = 6f * sMultiplier, center = Offset(centerX, torsoTopY + torsoH * 0.42f))

  // 4. Authentic Yemeni Jambiya (جنبية صنعانية برأس صيفاني مذهب وحزام مقصب)
  if (hero.hasJambiya) {
    val beltY = hipY - 11f * sMultiplier
    drawRoundRect(Color(0xFFC5A059), Offset(centerX - 22f * sMultiplier, beltY), Size(44f * sMultiplier, 13f * sMultiplier), CornerRadius(4f, 4f))
    drawLine(Color(0xFF8D6E63), Offset(centerX - 20f * sMultiplier, beltY + 6.5f * sMultiplier), Offset(centerX + 20f * sMultiplier, beltY + 6.5f * sMultiplier), strokeWidth = 2f * sMultiplier)

    // Golden Hilt (رأس الجنبية الصيفاني)
    val jambiyaPath = Path().apply {
      moveTo(centerX - 4.5f * sMultiplier, beltY - 10f * sMultiplier)
      lineTo(centerX + 4.5f * sMultiplier, beltY - 10f * sMultiplier)
      lineTo(centerX + 6f * sMultiplier, beltY)
      lineTo(centerX - 6f * sMultiplier, beltY)
      close()
    }
    drawPath(jambiyaPath, Color(0xFFFFD54F))

    // Curved Silver Scabbard (غمد الجنبية الفضي المنحني)
    val sheathPath = Path().apply {
      moveTo(centerX - 6f * sMultiplier, beltY)
      quadraticBezierTo(centerX - 9f * sMultiplier, beltY + 17f * sMultiplier, centerX + 9f * sMultiplier, beltY + 19f * sMultiplier)
      quadraticBezierTo(centerX, beltY + 14f * sMultiplier, centerX + 6f * sMultiplier, beltY)
      close()
    }
    drawPath(sheathPath, Color(0xFFBCAAA4))
  }

  // 5. Arms & Shoulders
  val leftHandY = torsoTopY + 34f * sMultiplier + armStride
  drawLine(hero.shirtColor, Offset(centerX - torsoW * 0.5f, torsoTopY + 5f), Offset(centerX - torsoW * 0.5f - 11f * sMultiplier, torsoTopY + 22f * sMultiplier), strokeWidth = 12f * sMultiplier, cap = StrokeCap.Round)
  drawLine(Color(0xFFD7A177), Offset(centerX - torsoW * 0.5f - 11f * sMultiplier, torsoTopY + 22f * sMultiplier), Offset(centerX - torsoW * 0.5f - 14f * sMultiplier, leftHandY), strokeWidth = 10f * sMultiplier, cap = StrokeCap.Round)

  val rightHandY = torsoTopY + 34f * sMultiplier - armStride
  drawLine(hero.shirtColor, Offset(centerX + torsoW * 0.5f, torsoTopY + 5f), Offset(centerX + torsoW * 0.5f + 11f * sMultiplier, torsoTopY + 22f * sMultiplier), strokeWidth = 12f * sMultiplier, cap = StrokeCap.Round)
  drawLine(Color(0xFFD7A177), Offset(centerX + torsoW * 0.5f + 11f * sMultiplier, torsoTopY + 22f * sMultiplier), Offset(centerX + torsoW * 0.5f + 14f * sMultiplier, rightHandY), strokeWidth = 10f * sMultiplier, cap = StrokeCap.Round)

  // 6. Shemagh / Bandana around neck
  if (hero.hasBandana) {
    val bandanaPath = Path().apply {
      moveTo(centerX - 18f * sMultiplier, torsoTopY + 3f)
      lineTo(centerX + 18f * sMultiplier, torsoTopY + 3f)
      lineTo(centerX, torsoTopY + 21f * sMultiplier)
      close()
    }
    drawPath(bandanaPath, Color(0xFFB71C1C))
  }

  // 7. Head & Neck
  val headCenterY = torsoTopY - 20f * sMultiplier
  drawCircle(Color(0xFFD7A177), radius = 17.5f * sMultiplier, center = Offset(centerX, headCenterY))

  // 8. Headdress
  when (hero.hatType) {
    HatType.COWBOY_HAT -> {
      val brimPath = Path().apply {
        moveTo(centerX - 35f * sMultiplier, headCenterY - 6f * sMultiplier)
        quadraticBezierTo(centerX, headCenterY + 3f * sMultiplier, centerX + 35f * sMultiplier, headCenterY - 6f * sMultiplier)
        lineTo(centerX + 35f * sMultiplier, headCenterY - 13f * sMultiplier)
        quadraticBezierTo(centerX, headCenterY - 4f * sMultiplier, centerX - 35f * sMultiplier, headCenterY - 13f * sMultiplier)
        close()
      }
      drawPath(brimPath, Color(0xFF3E2723))

      val crownPath = Path().apply {
        moveTo(centerX - 20f * sMultiplier, headCenterY - 11f * sMultiplier)
        lineTo(centerX - 15f * sMultiplier, headCenterY - 38f * sMultiplier)
        quadraticBezierTo(centerX, headCenterY - 32f * sMultiplier, centerX + 15f * sMultiplier, headCenterY - 38f * sMultiplier)
        lineTo(centerX + 20f * sMultiplier, headCenterY - 11f * sMultiplier)
        close()
      }
      drawPath(crownPath, Color(0xFF4E342E))
      drawLine(Color(0xFFB71C1C), Offset(centerX - 18f * sMultiplier, headCenterY - 13f * sMultiplier), Offset(centerX + 18f * sMultiplier, headCenterY - 13f * sMultiplier), strokeWidth = 4f * sMultiplier)
    }
    HatType.BACKWARDS_CAP -> {
      drawCircle(Color(0xFFD84315), radius = 19f * sMultiplier, center = Offset(centerX, headCenterY - 5f * sMultiplier))
      drawRoundRect(Color(0xFFBF360C), Offset(centerX - 21f * sMultiplier, headCenterY - 3f * sMultiplier), Size(42f * sMultiplier, 9f * sMultiplier), CornerRadius(4f, 4f))
    }
    HatType.BERET -> {
      drawOval(Color(0xFF263238), Offset(centerX - 22f * sMultiplier, headCenterY - 26f * sMultiplier), Size(44f * sMultiplier, 22f * sMultiplier))
      drawCircle(Color(0xFFFFD54F), radius = 4f * sMultiplier, center = Offset(centerX - 10f * sMultiplier, headCenterY - 15f * sMultiplier))
    }
    HatType.SPORTS_BAND -> {
      drawRoundRect(Color(0xFF00E676), Offset(centerX - 19f * sMultiplier, headCenterY - 14f * sMultiplier), Size(38f * sMultiplier, 9f * sMultiplier), CornerRadius(3f, 3f))
    }
    HatType.NONE -> {
      drawCircle(Color(0xFF1A1A1A), radius = 18f * sMultiplier, center = Offset(centerX, headCenterY - 3f * sMultiplier))
    }
  }
}
