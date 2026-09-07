package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.*

/**
 * تصنيف المعالم ونقاط الخريطة في صنعاء القديمة
 */
enum class SanaaMapPointType(
  val titleAr: String,
  val defaultColor: Color,
  val iconEmoji: String
) {
  PLAYER("موقع اللاعب", Color(0xFF00E676), "🟢"),
  MISSION_POINT("نقطة مهمة تكتيكية", Color(0xFFFFD54F), "🎯"),
  GOVERNMENT_CENTER("مركز ومقر حكومي سيادي", Color(0xFFFF3D00), "🏛️"),
  HISTORIC_GATE("بوابة تاريخية وسور", Color(0xFFD4AF37), "🏰"),
  CULTURAL_LANDMARK("معلم وسوق تراثي", Color(0xFF29B6F6), "🕌")
}

/**
 * بيانات نقطة على خارطة صنعاء القديمة
 */
data class SanaaMapPoint(
  val id: String,
  val titleAr: String,
  val subtitleAr: String,
  val type: SanaaMapPointType,
  val normX: Float, // Normalized -1f .. +1f relative to center of Old Sana'a
  val normY: Float, // Normalized -1f .. +1f (North is -1, South is +1)
  val iconEmoji: String,
  val customColor: Color? = null,
  val isUrgent: Boolean = false,
  val rewardCash: Int = 0
)

/**
 * قائمة المعالم الافتراضية الموثقة لصنعاء القديمة والمراكز السيادية والمهام
 */
val defaultOldSanaaPoints = listOf(
  // 1. المراكز الحكومية والسيادية (Government Centers)
  SanaaMapPoint(
    id = "gov_ordi_complex",
    titleAr = "مجمع الدفاع والعرضي التاريخي",
    subtitleAr = "المقر العسكري والسيادي العام المطل على أطراف صنعاء القديمة",
    type = SanaaMapPointType.GOVERNMENT_CENTER,
    normX = 0.52f,
    normY = 0.48f,
    iconEmoji = "🏛️",
    customColor = Color(0xFFFF1744)
  ),
  SanaaMapPoint(
    id = "gov_prime_ministry",
    titleAr = "رئاسة الوزراء والمقرات الحكومية",
    subtitleAr = "مقر القيادة التنفيذية وحراسة مشددة على المحور الغربي",
    type = SanaaMapPointType.GOVERNMENT_CENTER,
    normX = -0.58f,
    normY = -0.15f,
    iconEmoji = "🏢",
    customColor = Color(0xFFFF5252)
  ),
  SanaaMapPoint(
    id = "gov_police_station",
    titleAr = "قسم شرطة باب اليمن وأمن الأمانة",
    subtitleAr = "دائرة الرقابة الأمنية ودوريات الشرطة المركزية",
    type = SanaaMapPointType.GOVERNMENT_CENTER,
    normX = 0.08f,
    normY = 0.65f,
    iconEmoji = "🚔",
    customColor = Color(0xFFFF3D00)
  ),

  // 2. نقاط المهام التكتيكية (Mission Objectives)
  SanaaMapPoint(
    id = "mission_spice_heist",
    titleAr = "مهمة: شحنة بهارات سوق الملح",
    subtitleAr = "استرداد العقيق اليمني والتوابل النادرة قبل وصول الدورية",
    type = SanaaMapPointType.MISSION_POINT,
    normX = 0.12f,
    normY = 0.10f,
    iconEmoji = "🌶️",
    isUrgent = true,
    rewardCash = 850
  ),
  SanaaMapPoint(
    id = "mission_great_mosque",
    titleAr = "مهمة: لقاء سري بالجامع الكبير",
    subtitleAr = "استلام المخطوطة التكتيكية وتوجيه عصابة المشاغبين",
    type = SanaaMapPointType.MISSION_POINT,
    normX = -0.08f,
    normY = -0.05f,
    iconEmoji = "📜",
    rewardCash = 1200
  ),
  SanaaMapPoint(
    id = "mission_sailah_race",
    titleAr = "مهمة: تحدي سرعة مجرى السائلة",
    subtitleAr = "سباق وتفحيط الدباب بين الحجارة المرصوفة وتفادي حواجز التفتيش",
    type = SanaaMapPointType.MISSION_POINT,
    normX = -0.28f,
    normY = 0.35f,
    iconEmoji = "🚐",
    isUrgent = true,
    rewardCash = 1500
  ),

  // 3. المعالم والبوابات التاريخية (Historic Gates & Landmarks)
  SanaaMapPoint(
    id = "landmark_bab_al_yemen",
    titleAr = "باب اليمن (البوابة الكبرى)",
    subtitleAr = "المدخل الجنوبي الأسطوري لصنعاء القديمة المشيد منذ آلاف السنين",
    type = SanaaMapPointType.HISTORIC_GATE,
    normX = 0.0f,
    normY = 0.78f,
    iconEmoji = "🏰",
    customColor = Color(0xFFFFD54F)
  ),
  SanaaMapPoint(
    id = "landmark_bab_shaoob",
    titleAr = "باب شعوب (المدخل الشمالي)",
    subtitleAr = "بوابة القوافل والمسافرين نحو شعوب ومحيط صنعاء الشمالي",
    type = SanaaMapPointType.HISTORIC_GATE,
    normX = 0.05f,
    normY = -0.76f,
    iconEmoji = "🚪",
    customColor = Color(0xFFFFCA28)
  ),
  SanaaMapPoint(
    id = "landmark_jami_kabir",
    titleAr = "الجامع الكبير بصنعاء",
    subtitleAr = "صرح إسلامي تاريخي تتوسط مناراته بيوت الطين والقمريات",
    type = SanaaMapPointType.CULTURAL_LANDMARK,
    normX = -0.04f,
    normY = -0.02f,
    iconEmoji = "🕌",
    customColor = Color(0xFF4FC3F7)
  ),
  SanaaMapPoint(
    id = "landmark_souq_almilh",
    titleAr = "سوق الملح والحرف التراثية",
    subtitleAr = "دكاكين الفضة والجنابي والقهوة القشر والبهارات الصنعانية",
    type = SanaaMapPointType.CULTURAL_LANDMARK,
    normX = 0.16f,
    normY = 0.22f,
    iconEmoji = "☕",
    customColor = Color(0xFF81C784)
  )
)

/**
 * مكون Compose لعرض مصغر لخريطة "صنعاء القديمة"
 * يوضح مواقع اللاعب، نقاط المهام، والمراكز الحكومية والمعالم التاريخية.
 * يدعم العرض المصغر HUD داخل اللعبة مع إمكانية التوسيع لخريطة تفاعلية كاملة.
 */
@Composable
fun OldSanaaCityMiniMap(
  playerNormX: Float,
  playerNormY: Float,
  playerHeadingDeg: Float,
  modifier: Modifier = Modifier,
  points: List<SanaaMapPoint> = defaultOldSanaaPoints,
  isChased: Boolean = false,
  onPointSelected: ((SanaaMapPoint) -> Unit)? = null
) {
  var isExpandedDialogVisible by remember { mutableStateOf(false) }
  var selectedFilter by remember { mutableStateOf<SanaaMapPointType?>(null) }
  var focusedPoint by remember { mutableStateOf<SanaaMapPoint?>(null) }

  // Radar sweep animation
  val infiniteTransition = rememberInfiniteTransition(label = "sanaa_minimap_anim")
  val radarSweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_sweep"
  )

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "beacon_pulse"
  )

  // 1. الوضع المصغر في الواجهة (Compact HUD Mini-Map)
  Surface(
    color = Color(0xF20A1118),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.5.dp,
      brush = Brush.sweepGradient(
        listOf(
          Color(0xFFFFD54F),
          if (isChased) Color(0xFFFF1744) else Color(0xFF00E676),
          Color(0xFF00BCD4),
          Color(0xFFFFD54F)
        )
      )
    ),
    shadowElevation = 8.dp,
    modifier = modifier
      .size(118.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { isExpandedDialogVisible = true }
      .testTag("old_sanaa_city_minimap")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        drawOldSanaaMapCanvas(
          canvasSize = size,
          points = points,
          playerNormX = playerNormX,
          playerNormY = playerNormY,
          playerHeadingDeg = playerHeadingDeg,
          radarSweepAngle = radarSweepAngle,
          pulseScale = pulseScale,
          isChased = isChased,
          filter = null,
          isDetailed = false
        )
      }

      // شريط علوي صغير: بوصلة الشمال ورمز التكبير
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = Color(0xCC000000),
          shape = CircleShape
        ) {
          Text(
            text = "N ⇧",
            color = Color(0xFFFFD54F),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
          )
        }

        Surface(
          color = Color(0xCC000000),
          shape = CircleShape
        ) {
          Icon(
            imageVector = Icons.Default.ZoomOutMap,
            contentDescription = "تكبير خريطة صنعاء القديمة",
            tint = Color.White,
            modifier = Modifier.size(12.dp).padding(1.dp)
          )
        }
      }

      // شريط سفلي: اسم الخريطة
      Text(
        text = "خارطة صنعاء القديمة",
        color = Color(0xEEFFFFFF),
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 3.dp)
          .background(Color(0xDD000000), RoundedCornerShape(4.dp))
          .padding(horizontal = 4.dp, vertical = 1.dp)
      )
    }
  }

  // 2. النافذة الموسعة التكتيكية عند النقر (Full Tactical Dialog)
  if (isExpandedDialogVisible) {
    Dialog(onDismissRequest = { isExpandedDialogVisible = false }) {
      Surface(
        color = Color(0xF80B141E),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
          .testTag("dialog_old_sanaa_full_map")
      ) {
        Column(
          modifier = Modifier
            .padding(14.dp)
            .fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Header Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { isExpandedDialogVisible = false },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق الخريطة", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "خريطة صنعاء القديمة والمقرات 🇾🇪",
                color = Color(0xFFFFD54F),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "المواقع الاستراتيجية • المراكز الحكومية • نقاط المهام",
                color = Color(0xFFB0BEC5),
                fontSize = 10.sp
              )
            }

            Surface(
              color = if (isChased) Color(0xDDFF1744) else Color(0xDD00E676),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = if (isChased) "🚨 مطاردة" else "🟢 هدوء",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Filter Chips (تصفية المعالم)
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            item {
              FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("الكل", fontSize = 11.sp) }
              )
            }
            items(SanaaMapPointType.values().filter { it != SanaaMapPointType.PLAYER }) { type ->
              FilterChip(
                selected = selectedFilter == type,
                onClick = { selectedFilter = if (selectedFilter == type) null else type },
                label = { Text("${type.iconEmoji} ${type.titleAr}", fontSize = 11.sp) }
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Large Interactive Map Canvas
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFF070D13))
              .border(1.dp, Color(0x66FFD54F), RoundedCornerShape(16.dp))
          ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
              drawOldSanaaMapCanvas(
                canvasSize = size,
                points = points,
                playerNormX = playerNormX,
                playerNormY = playerNormY,
                playerHeadingDeg = playerHeadingDeg,
                radarSweepAngle = radarSweepAngle,
                pulseScale = pulseScale,
                isChased = isChased,
                filter = selectedFilter,
                isDetailed = true
              )
            }

            // Legend Overlay on top-left of canvas
            Column(
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xDD000000), RoundedCornerShape(8.dp))
                .padding(6.dp),
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Text("🟢 موقعك", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("🏛️ مراكز حكومية", color = Color(0xFFFF3D00), fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("🎯 مهام تكتيكية", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
              Text("🏰 باب اليمن وأسوار", color = Color(0xFFD4AF37), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Card of nearest/focused point
          val displayedPoint = focusedPoint ?: points.firstOrNull { it.isUrgent } ?: points.first()
          Surface(
            color = Color(0xEE111C28),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, displayedPoint.customColor ?: displayedPoint.type.defaultColor),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(displayedPoint.iconEmoji, fontSize = 24.sp)
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = displayedPoint.titleAr,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                  )
                  if (displayedPoint.rewardCash > 0) {
                    Text(
                      text = "+${displayedPoint.rewardCash}$",
                      color = Color(0xFF00E676),
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                  }
                }
                Text(
                  text = displayedPoint.subtitleAr,
                  color = Color(0xFFB0BEC5),
                  fontSize = 10.sp
                )
              }
              Button(
                onClick = {
                  onPointSelected?.invoke(displayedPoint)
                  isExpandedDialogVisible = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Text("توجه 🧭", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

/**
 * وظيفة الرسم الأساسية لخريطة صنعاء القديمة ومبانيها
 */
private fun DrawScope.drawOldSanaaMapCanvas(
  canvasSize: Size,
  points: List<SanaaMapPoint>,
  playerNormX: Float,
  playerNormY: Float,
  playerHeadingDeg: Float,
  radarSweepAngle: Float,
  pulseScale: Float,
  isChased: Boolean,
  filter: SanaaMapPointType?,
  isDetailed: Boolean
) {
  val centerX = canvasSize.width / 2f
  val centerY = canvasSize.height / 2f
  val radiusX = (canvasSize.width / 2f) * 0.88f
  val radiusY = (canvasSize.height / 2f) * 0.88f

  // 1. رسم سور صنعاء القديمة البيضاوي التاريخي (Historic Ancient City Wall)
  val wallPath = Path().apply {
    addOval(
      androidx.compose.ui.geometry.Rect(
        left = centerX - radiusX,
        top = centerY - radiusY,
        right = centerX + radiusX,
        bottom = centerY + radiusY
      )
    )
  }

  // ملء داخل المدينة بنسيج حجري خافت
  drawPath(
    path = wallPath,
    color = Color(0x223E2723)
  )

  // خط السور الحجري
  drawPath(
    path = wallPath,
    color = Color(0xBB8D6E63),
    style = Stroke(width = if (isDetailed) 4f else 2.5f)
  )

  // رسم أبراج المراقبة على طول السور
  val towerAngles = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
  for (angle in towerAngles) {
    val rad = Math.toRadians(angle)
    val tx = centerX + (radiusX * cos(rad)).toFloat()
    val ty = centerY + (radiusY * sin(rad)).toFloat()
    drawCircle(
      color = Color(0xFFD7CCC8),
      radius = if (isDetailed) 5f else 3f,
      center = Offset(tx, ty)
    )
  }

  // 2. مجرى السائلة الصنعانية التراثي (Al-Sailah Waterway running North-South)
  val sailahPath = Path().apply {
    moveTo(centerX - radiusX * 0.35f, centerY - radiusY * 0.95f)
    cubicTo(
      centerX - radiusX * 0.25f, centerY - radiusY * 0.45f,
      centerX - radiusX * 0.40f, centerY + radiusY * 0.25f,
      centerX - radiusX * 0.28f, centerY + radiusY * 0.95f
    )
  }
  drawPath(
    path = sailahPath,
    color = Color(0x9926A69A),
    style = Stroke(width = if (isDetailed) 6f else 3.5f, cap = StrokeCap.Round)
  )

  // 3. أزقة وشوارع حجرية داخلية متقاطعة
  val alleyStroke = Stroke(width = 1.2f, cap = StrokeCap.Round)
  drawLine(
    color = Color(0x33BCAAA4),
    start = Offset(centerX - radiusX * 0.7f, centerY),
    end = Offset(centerX + radiusX * 0.7f, centerY),
    strokeWidth = 1.5f
  )
  drawLine(
    color = Color(0x33BCAAA4),
    start = Offset(centerX, centerY - radiusY * 0.7f),
    end = Offset(centerX, centerY + radiusY * 0.7f),
    strokeWidth = 1.5f
  )

  // 4. مسح الرادار المضيء (Radar Sweep Beam)
  val sweepRad = Math.toRadians(radarSweepAngle.toDouble())
  val sweepEnd = Offset(
    centerX + (radiusX * cos(sweepRad)).toFloat(),
    centerY + (radiusY * sin(sweepRad)).toFloat()
  )
  drawLine(
    brush = Brush.radialGradient(
      colors = listOf(
        if (isChased) Color(0x88FF1744) else Color(0x8800E676),
        Color.Transparent
      ),
      center = Offset(centerX, centerY),
      radius = radiusX
    ),
    start = Offset(centerX, centerY),
    end = sweepEnd,
    strokeWidth = 2f
  )

  // 5. رسم المعالم والنقاط (Points of Interest)
  val filteredPoints = if (filter != null) points.filter { it.type == filter } else points
  for (point in filteredPoints) {
    val px = centerX + (point.normX * radiusX)
    val py = centerY + (point.normY * radiusY)
    val ptColor = point.customColor ?: point.type.defaultColor

    when (point.type) {
      SanaaMapPointType.GOVERNMENT_CENTER -> {
        // المراكز الحكومية: مستطيل بارز مع هالة تحذيرية
        drawCircle(
          color = ptColor.copy(alpha = 0.25f),
          radius = 12f * (if (point.isUrgent) pulseScale else 1.0f),
          center = Offset(px, py)
        )
        drawRect(
          color = ptColor,
          topLeft = Offset(px - 5f, py - 5f),
          size = Size(10f, 10f)
        )
        drawRect(
          color = Color.White,
          topLeft = Offset(px - 2f, py - 2f),
          size = Size(4f, 4f)
        )
      }
      SanaaMapPointType.MISSION_POINT -> {
        // نقاط المهام: هالة نابضة صفراء مع نقطة مركزية
        drawCircle(
          color = ptColor.copy(alpha = 0.35f),
          radius = 14f * pulseScale,
          center = Offset(px, py)
        )
        drawCircle(
          color = ptColor,
          radius = if (isDetailed) 6f else 4f,
          center = Offset(px, py)
        )
        drawCircle(
          color = Color.Black,
          radius = 2f,
          center = Offset(px, py)
        )
      }
      SanaaMapPointType.HISTORIC_GATE -> {
        // البوابات: شكل قوس معيني
        drawCircle(
          color = ptColor,
          radius = if (isDetailed) 5.5f else 3.5f,
          center = Offset(px, py)
        )
      }
      SanaaMapPointType.CULTURAL_LANDMARK -> {
        // معالم دينية وسياحية: ماسة زرقاء
        drawCircle(
          color = ptColor,
          radius = if (isDetailed) 5f else 3f,
          center = Offset(px, py)
        )
      }
      SanaaMapPointType.PLAYER -> {}
    }
  }

  // 6. رسم موقع اللاعب وسهم الاتجاه ومخروط الرؤية (Player Position & Heading Cone)
  val playerScreenX = centerX + (playerNormX * radiusX)
  val playerScreenY = centerY + (playerNormY * radiusY)

  // مخروط مجال الرؤية (Vision Cone)
  rotate(playerHeadingDeg, pivot = Offset(playerScreenX, playerScreenY)) {
    val conePath = Path().apply {
      moveTo(playerScreenX, playerScreenY)
      lineTo(playerScreenX - 16f, playerScreenY - 32f)
      lineTo(playerScreenX + 16f, playerScreenY - 32f)
      close()
    }
    drawPath(
      path = conePath,
      brush = Brush.verticalGradient(
        colors = listOf(
          Color(0x5500E676),
          Color.Transparent
        ),
        startY = playerScreenY - 32f,
        endY = playerScreenY
      )
    )
  }

  // هالة نبض اللاعب
  drawCircle(
    color = (if (isChased) Color(0xFFFF1744) else Color(0xFF00E676)).copy(alpha = 0.35f),
    radius = 10f * pulseScale,
    center = Offset(playerScreenX, playerScreenY)
  )

  // سهم اللاعب
  rotate(playerHeadingDeg, pivot = Offset(playerScreenX, playerScreenY)) {
    val arrowPath = Path().apply {
      moveTo(playerScreenX, playerScreenY - 8f)
      lineTo(playerScreenX - 5.5f, playerScreenY + 6f)
      lineTo(playerScreenX, playerScreenY + 2.5f)
      lineTo(playerScreenX + 5.5f, playerScreenY + 6f)
      close()
    }
    drawPath(
      path = arrowPath,
      color = if (isChased) Color(0xFFFF1744) else Color(0xFF00E676)
    )
    drawPath(
      path = arrowPath,
      color = Color.White,
      style = Stroke(width = 1.2f)
    )
  }
}
