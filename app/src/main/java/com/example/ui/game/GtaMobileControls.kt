package com.example.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.HapticManager
import kotlin.math.*

/**
 * Virtual Analog Joystick (Matching GTA San Andreas Mobile Screenshot 3).
 * Clean dark circular ring outline with solid white thumb knob that smoothly follows touch/drag.
 */
@Composable
fun GtaAnalogJoystick(
  modifier: Modifier = Modifier,
  baseRadiusDp: Float = 64f,
  knobRadiusDp: Float = 26f,
  onMove: (deltaX: Float, deltaY: Float) -> Unit,
  onRelease: () -> Unit
) {
  var knobOffset by remember { mutableStateOf(Offset.Zero) }

  Box(
    modifier = modifier
      .size((baseRadiusDp * 2).dp)
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val diff = offset - center
            val dist = diff.getDistance()
            val maxDist = size.width / 2f - knobRadiusDp
            val clampedOffset = if (dist > maxDist) diff * (maxDist / dist) else diff
            knobOffset = clampedOffset
            val normX = (clampedOffset.x / maxDist).coerceIn(-1f, 1f)
            val normY = (clampedOffset.y / maxDist).coerceIn(-1f, 1f)
            onMove(normX, normY)
            HapticManager.vibrateMovement()
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val maxDist = size.width / 2f - knobRadiusDp
            val newOffset = knobOffset + dragAmount
            val dist = newOffset.getDistance()
            val clamped = if (dist > maxDist) newOffset * (maxDist / dist) else newOffset
            knobOffset = clamped
            val normX = (clamped.x / maxDist).coerceIn(-1f, 1f)
            val normY = (clamped.y / maxDist).coerceIn(-1f, 1f)
            onMove(normX, normY)
          },
          onDragEnd = {
            knobOffset = Offset.Zero
            onRelease()
          },
          onDragCancel = {
            knobOffset = Offset.Zero
            onRelease()
          }
        )
      }
      .testTag("gta_analog_joystick"),
    contentAlignment = Alignment.Center
  ) {
    // Canvas for the outer guide ring and center knob
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = size.width / 2f - 4f

      // Outer dark circle background
      drawCircle(
        color = Color(0x77000000),
        radius = maxRadius,
        center = center
      )

      // Outer ring border (subtle grey / white outline like in screenshot)
      drawCircle(
        color = Color(0xBBFFFFFF),
        radius = maxRadius,
        center = center,
        style = Stroke(width = 3.5f)
      )

      // Direction cross ticks (subtle directional indicators)
      val tickLength = 10f
      // Top
      drawLine(Color(0x88FFFFFF), Offset(center.x, center.y - maxRadius + 2f), Offset(center.x, center.y - maxRadius + 2f + tickLength), strokeWidth = 2.5f)
      // Bottom
      drawLine(Color(0x88FFFFFF), Offset(center.x, center.y + maxRadius - 2f), Offset(center.x, center.y + maxRadius - 2f - tickLength), strokeWidth = 2.5f)
      // Left
      drawLine(Color(0x88FFFFFF), Offset(center.x - maxRadius + 2f, center.y), Offset(center.x - maxRadius + 2f + tickLength, center.y), strokeWidth = 2.5f)
      // Right
      drawLine(Color(0x88FFFFFF), Offset(center.x + maxRadius - 2f, center.y), Offset(center.x + maxRadius - 2f - tickLength, center.y), strokeWidth = 2.5f)

      // Inner Knob position
      val knobCenter = center + knobOffset

      // Knob outer glow / shadow
      drawCircle(
        color = Color(0x66000000),
        radius = knobRadiusDp * 1.15f,
        center = knobCenter + Offset(0f, 2f)
      )

      // Solid white inner thumb knob (Matching GTA Mobile Screenshot 3)
      drawCircle(
        color = Color(0xFFEEEEEE),
        radius = knobRadiusDp,
        center = knobCenter
      )

      // Knob inner accent ring
      drawCircle(
        color = Color(0xFFBDBDBD),
        radius = knobRadiusDp * 0.65f,
        center = knobCenter,
        style = Stroke(width = 2.5f)
      )
    }
  }
}

/**
 * GTA Action Buttons Cluster on Right Screen (Matching GTA San Andreas Mobile).
 * Includes:
 * - 🎯 Action / Shoot / Attack Button (large round button with bullet icon)
 * - 🚗 Vehicle Enter / Exit Button (round button with car icon)
 * - 🏃 Sprint / Parkour Jump Button
 * - 🔄 Quick 4-Hero Switcher Button
 * - 🔫 Weapon Switcher Button
 */
@Composable
fun GtaActionButtonsCluster(
  currentHero: UnifiedHeroId,
  currentWeapon: WeaponItem,
  isInsideVehicle: Boolean,
  onShoot: () -> Unit,
  onVehicleToggle: () -> Unit,
  onJumpOrSprint: () -> Unit,
  onSwitchHero: () -> Unit,
  onSwitchWeapon: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .wrapContentSize()
      .padding(bottom = 12.dp, end = 12.dp),
    contentAlignment = Alignment.BottomEnd
  ) {
    Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Top row of action cluster: [Weapon Switch] + [Hero Switcher]
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Weapon Switch Button
        Surface(
          onClick = {
            HapticManager.vibrateLightTap()
            onSwitchWeapon()
          },
          shape = CircleShape,
          color = Color(0xCC1A1A1A),
          border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF5C518)),
          shadowElevation = 6.dp,
          modifier = Modifier
            .size(46.dp)
            .testTag("btn_gta_switch_weapon")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(currentWeapon.iconEmoji, fontSize = 20.sp)
          }
        }

        // 4-Hero Quick Switcher Button with active hero avatar & counter badge
        Surface(
          onClick = {
            HapticManager.vibrateSuccess()
            onSwitchHero()
          },
          shape = CircleShape,
          color = currentHero.shirtColor.copy(alpha = 0.92f),
          border = androidx.compose.foundation.BorderStroke(2.5.dp, currentHero.accentColor),
          shadowElevation = 8.dp,
          modifier = Modifier
            .size(54.dp)
            .testTag("btn_gta_switch_hero")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(currentHero.avatarEmoji, fontSize = 22.sp)
            }
            // Small badge showing "4 أبطال"
            Surface(
              color = Color.Black.copy(alpha = 0.85f),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
            ) {
              Text(
                text = "🔄 تبديل",
                color = currentHero.accentColor,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }
      }

      // Middle / Lower Action Cluster: Jump & Vehicle
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Vehicle Enter / Exit Button
        Surface(
          onClick = {
            HapticManager.vibrateHeavyImpact()
            onVehicleToggle()
          },
          shape = CircleShape,
          color = if (isInsideVehicle) Color(0xFFE65100).copy(alpha = 0.90f) else Color(0xCC212121),
          border = androidx.compose.foundation.BorderStroke(2.dp, if (isInsideVehicle) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.8f)),
          shadowElevation = 8.dp,
          modifier = Modifier
            .size(52.dp)
            .testTag("btn_gta_vehicle")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(if (isInsideVehicle) "🚪" else "🚗", fontSize = 24.sp)
          }
        }

        // Sprint / Parkour Jump Button
        Surface(
          onClick = {
            HapticManager.vibrateMovement()
            onJumpOrSprint()
          },
          shape = CircleShape,
          color = Color(0xDD1B5E20),
          border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF76FF03)),
          shadowElevation = 8.dp,
          modifier = Modifier
            .size(54.dp)
            .testTag("btn_gta_jump")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🏃‍♂️", fontSize = 22.sp)
            }
          }
        }
      }

      // Primary Shoot / Attack Button (Large circular button matching Screenshot 3!)
      Surface(
        onClick = {
          HapticManager.vibrateExplosion()
          onShoot()
        },
        shape = CircleShape,
        color = Color(0xDDF44336), // Vibrant Red
        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
        shadowElevation = 12.dp,
        modifier = Modifier
          .size(68.dp)
          .testTag("btn_gta_shoot")
      ) {
        Box(contentAlignment = Alignment.Center) {
          // Circular target / bullet icon matching Screenshot 3
          Canvas(modifier = Modifier.size(34.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            // Outer crosshair circle
            drawCircle(
              color = Color.White,
              radius = size.width / 2f - 2f,
              style = Stroke(width = 3.5f)
            )
            // Inner bullet/dot
            drawCircle(
              color = Color.White,
              radius = 5.5f,
              center = center
            )
            // Crosshair lines
            drawLine(Color.White, Offset(center.x, 0f), Offset(center.x, size.height * 0.28f), strokeWidth = 3f)
            drawLine(Color.White, Offset(center.x, size.height * 0.72f), Offset(center.x, size.height), strokeWidth = 3f)
            drawLine(Color.White, Offset(0f, center.y), Offset(size.width * 0.28f, center.y), strokeWidth = 3f)
            drawLine(Color.White, Offset(size.width * 0.72f, center.y), Offset(size.width, center.y), strokeWidth = 3f)
          }
        }
      }
    }
  }
}

/**
 * Authentic GTA San Andreas Mobile HUD:
 * - Top-Left: Circular Radar Mini-Map (Compass 'N', roads, player triangle, police blips)
 * - Top-Right: GTA Clock (21:13), Green Money Counter ($80872), Health Bar, Armor Bar, Weapon Box (437-50)
 * - Bottom-Right: Location name badge ("حي صنعاء - باب اليمن" / "Palisades")
 */
@Composable
fun GtaAuthenticHud(
  hero: UnifiedHeroId,
  weapon: WeaponItem,
  healthPercent: Float,
  armorPercent: Float,
  cashAmount: Int,
  gameTimeMinutes: Int,
  locationNameAr: String,
  playerWorldAngle: Float,
  policeDistance: Float,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxSize()) {
    // 1. Top-Left Circular Radar Mini-Map (GTA Style)
    GtaCircularRadarMiniMap(
      playerHeadingDeg = playerWorldAngle,
      policeDist = policeDistance,
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(top = 10.dp, start = 12.dp)
        .testTag("gta_radar_minimap")
    )

    // 2. Top-Right GTA San Andreas Status HUD (Clock, Cash, Health/Armor, Weapon)
    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 8.dp, end = 12.dp),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Clock: "21:13" in classic GTA Digital font style
      val hours = (gameTimeMinutes / 60) % 24
      val mins = gameTimeMinutes % 60
      val timeText = String.format("%02d:%02d", hours, mins)

      Text(
        text = timeText,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.shadow(4.dp)
      )

      // Cash: Green bold digits with leading zeros "$00080872" or "$80872"
      val cashStr = String.format("$%08d", cashAmount)
      Text(
        text = cashStr,
        color = Color(0xFF43A047), // Authentic GTA Green
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.shadow(6.dp)
      )

      // Health Bar (Solid vibrant red horizontal bar) & Armor (White/Light blue bar)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          // Health Bar: Solid horizontal red bar in dark container
          Box(
            modifier = Modifier
              .width(96.dp)
              .height(10.dp)
              .background(Color(0xFF261010), RoundedCornerShape(2.dp))
              .border(1.dp, Color(0xFF5A1A1A), RoundedCornerShape(2.dp))
          ) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(healthPercent.coerceIn(0f, 1f))
                .background(
                  Brush.horizontalGradient(
                    listOf(Color(0xFFE53935), Color(0xFFFF5252))
                  ),
                  RoundedCornerShape(2.dp)
                )
            )
          }

          // Armor / Adrenaline Bar: Light blue / white
          Box(
            modifier = Modifier
              .width(96.dp)
              .height(8.dp)
              .background(Color(0xFF101B26), RoundedCornerShape(2.dp))
              .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(2.dp))
          ) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(armorPercent.coerceIn(0f, 1f))
                .background(
                  Brush.horizontalGradient(
                    listOf(Color(0xFF0288D1), Color(0xFF4FC3F7))
                  ),
                  RoundedCornerShape(2.dp)
                )
            )
          }
        }

        // Weapon Box: Rounded square container showing weapon icon & ammo
        Surface(
          color = Color(0xCC111111),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xAA888888)),
          modifier = Modifier.size(width = 54.dp, height = 48.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(weapon.iconEmoji, fontSize = 20.sp)
            Text(
              text = weapon.ammoText,
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 3. Bottom-Right Location Badge (GTA Gothic Typography Style)
    Surface(
      color = Color(0xBB000000),
      shape = RoundedCornerShape(6.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFFFFF)),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 120.dp, end = 14.dp)
    ) {
      Text(
        text = locationNameAr,
        color = Color(0xFFEEEEEE),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
      )
    }
  }
}

/**
 * Circular GTA Radar Mini-Map (Top-Left corner)
 * Features:
 * - Round circular map border with 'N' (North) indicator
 * - Street grid line segments
 * - Player location pointer (yellow triangle facing player heading)
 * - Police radar blip (flashing blue/red)
 */
@Composable
fun GtaCircularRadarMiniMap(
  playerHeadingDeg: Float,
  policeDist: Float,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(76.dp)
      .clip(CircleShape)
      .background(Color(0xDD12151B))
      .border(2.dp, Color(0xAAFFFFFF), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = size.width / 2f - 4f

      // Background subtle water/land grid
      drawCircle(Color(0xFF1E293B), radius = radius, center = center)

      // Road geometry lines
      val roadColor = Color(0xFF94A3B8)
      // Main avenue (double lane)
      drawLine(roadColor, Offset(center.x - 10f, 4f), Offset(center.x - 10f, size.height - 4f), strokeWidth = 3f)
      drawLine(roadColor, Offset(center.x + 10f, 4f), Offset(center.x + 10f, size.height - 4f), strokeWidth = 3f)
      // Cross streets
      drawLine(roadColor, Offset(4f, center.y), Offset(size.width - 4f, center.y), strokeWidth = 4f)
      drawLine(roadColor, Offset(4f, center.y - 18f), Offset(size.width - 4f, center.y - 18f), strokeWidth = 2.5f)
      drawLine(roadColor, Offset(4f, center.y + 18f), Offset(size.width - 4f, center.y + 18f), strokeWidth = 2.5f)

      // Police Blip on Radar
      val policeAngleRad = (playerHeadingDeg + 60f) * (PI / 180f).toFloat()
      val policeR = (radius * 0.65f).coerceAtMost(radius - 6f)
      val policePos = center + Offset(cos(policeAngleRad) * policeR, sin(policeAngleRad) * policeR)
      drawCircle(Color(0xFFFF1744), radius = 4f, center = policePos)
      drawCircle(Color(0xFF2979FF), radius = 2f, center = policePos)

      // North 'N' letter indicator at top edge
      val northPath = Path().apply {
        moveTo(center.x - 3f, 9f)
        lineTo(center.x - 3f, 3f)
        lineTo(center.x + 3f, 9f)
        lineTo(center.x + 3f, 3f)
      }
      drawPath(northPath, Color(0xFFFFD600), style = Stroke(width = 1.8f))

      // Center Player Arrow (Yellow triangle pointing forward in heading)
      val headingRad = (-playerHeadingDeg) * (PI / 180f).toFloat()
      val tipDist = 7.5f
      val baseDist = 5f
      val baseWidth = 5f

      val tip = center + Offset(sin(headingRad) * tipDist, -cos(headingRad) * tipDist)
      val leftBase = center + Offset(-cos(headingRad) * baseWidth - sin(headingRad) * baseDist, -sin(headingRad) * baseWidth + cos(headingRad) * baseDist)
      val rightBase = center + Offset(cos(headingRad) * baseWidth - sin(headingRad) * baseDist, sin(headingRad) * baseWidth + cos(headingRad) * baseDist)

      val arrowPath = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(leftBase.x, leftBase.y)
        lineTo(center.x, center.y)
        lineTo(rightBase.x, rightBase.y)
        close()
      }
      drawPath(arrowPath, Color(0xFFFFEB3B))
      drawPath(arrowPath, Color.Black, style = Stroke(width = 1.2f))
    }
  }
}
