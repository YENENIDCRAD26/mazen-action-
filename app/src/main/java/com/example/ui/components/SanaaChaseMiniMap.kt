package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.sanaa7d.Sanaa7DHidingSpot
import com.example.ui.sanaa7d.Sanaa7DPoliceOfficer
import com.example.ui.sanaa7d.SanaaGangThug
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tactical Radar / Mini-Map UI component for Sana'a 7D Chase.
 * Rendered using Compose Canvas to show player, police pursuers, gang thugs, hiding spots, and escape gate.
 */
@Composable
fun SanaaChaseMiniMap(
  playerWorldX: Float,
  isPlayerHiding: Boolean,
  isVehicleHijacked: Boolean,
  policePursuers: List<Sanaa7DPoliceOfficer>,
  gangThugs: List<SanaaGangThug>,
  hidingSpots: List<Sanaa7DHidingSpot>,
  distanceCovered: Float,
  targetDistance: Float,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "minimap_radar_sweep")
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_sweep_angle"
  )

  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "radar_pulse_alpha"
  )

  val progress = (distanceCovered / targetDistance.coerceAtLeast(1f)).coerceIn(0f, 1f)
  val distanceRemaining = (targetDistance - distanceCovered).coerceAtLeast(0f).toInt()

  Box(
    modifier = modifier
      .testTag("sanaa_chase_minimap")
      .size(130.dp)
      .shadow(12.dp, CircleShape)
      .clip(CircleShape)
      .background(
        Brush.radialGradient(
          colors = listOf(
            Color(0xEE0B141E),
            Color(0xF9050A0F)
          )
        )
      )
      .border(
        width = 2.dp,
        brush = Brush.sweepGradient(
          colors = listOf(
            GoldYemeni,
            TealCyan,
            GoldYemeniLight,
            CrimsonRed,
            GoldYemeni
          )
        ),
        shape = CircleShape
      )
      .padding(4.dp)
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
    ) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = (size.width / 2f) - 4f

      // 1. Draw Radar Concentric Circles & Crosshairs
      drawCircle(
        color = Color(0x2238BDF8),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
      )
      drawCircle(
        color = Color(0x1838BDF8),
        radius = radius * 0.65f,
        center = center,
        style = Stroke(width = 1f)
      )
      drawCircle(
        color = Color(0x1538BDF8),
        radius = radius * 0.35f,
        center = center,
        style = Stroke(width = 1f)
      )

      // Crosshair lines
      drawLine(
        color = Color(0x1A38BDF8),
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 1f
      )
      drawLine(
        color = Color(0x1A38BDF8),
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 1f
      )

      // 2. Draw Sana'a Alley Corridor Lines
      val alleyLeftX = center.x - (radius * 0.55f)
      val alleyRightX = center.x + (radius * 0.55f)
      drawLine(
        color = Color(0x33F59E0B),
        start = Offset(alleyLeftX, center.y - radius * 0.85f),
        end = Offset(alleyLeftX, center.y + radius * 0.85f),
        strokeWidth = 1.5f
      )
      drawLine(
        color = Color(0x33F59E0B),
        start = Offset(alleyRightX, center.y - radius * 0.85f),
        end = Offset(alleyRightX, center.y + radius * 0.85f),
        strokeWidth = 1.5f
      )

      // 3. Radar Sweep Line & Sector Glow
      rotate(sweepAngle, pivot = center) {
        drawLine(
          brush = Brush.linearGradient(
            colors = listOf(Color(0x0038BDF8), Color(0x8038BDF8)),
            start = center,
            end = Offset(center.x, center.y - radius)
          ),
          start = center,
          end = Offset(center.x, center.y - radius),
          strokeWidth = 2f,
          cap = StrokeCap.Round
        )
      }

      // 4. Draw Objective / Escape Gate at Top (باب اليمن)
      val goalY = center.y - (radius * 0.78f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(GoldYemeniLight, Color(0x00F59E0B)),
          center = Offset(center.x, goalY),
          radius = 12f
        ),
        radius = 10f,
        center = Offset(center.x, goalY)
      )
      drawCircle(
        color = GoldYemeni,
        radius = 4f,
        center = Offset(center.x, goalY)
      )

      // 5. Draw Hiding Spots (Cyan safe squares)
      for (spot in hidingSpots) {
        val spotZRatio = (spot.worldZ / 240f).coerceIn(0f, 1f)
        val spotY = center.y + (radius * 0.6f) - (spotZRatio * radius * 1.3f)
        val spotX = if (spot.side < 0) alleyLeftX - 5f else alleyRightX + 5f

        if (spotY in (center.y - radius)..(center.y + radius)) {
          drawRect(
            color = Color(0xFF06B6D4),
            topLeft = Offset(spotX - 3f, spotY - 3f),
            size = Size(6f, 6f)
          )
        }
      }

      // 6. Draw Gang Thugs (Purple/Amber Diamonds)
      for (thug in gangThugs) {
        val thugZRatio = (thug.worldZ / 240f).coerceIn(0f, 1f)
        val thugY = center.y + (radius * 0.6f) - (thugZRatio * radius * 1.3f)
        val thugX = center.x + (thug.worldX * radius * 0.45f)

        if (thugY in (center.y - radius)..(center.y + radius)) {
          val path = Path().apply {
            moveTo(thugX, thugY - 4f)
            lineTo(thugX + 4f, thugY)
            lineTo(thugX, thugY + 4f)
            lineTo(thugX - 4f, thugY)
            close()
          }
          drawPath(
            path = path,
            color = if (thug.isBoundInRopes) Color(0xFF10B981) else Color(0xFFA855F7)
          )
        }
      }

      // 7. Draw Police Pursuers (Red & Blue Flashing Dots with Warning Rings)
      for (cop in policePursuers) {
        val copZRatio = (cop.worldZ / 240f).coerceIn(0f, 1f)
        val copY = center.y + (radius * 0.6f) - (copZRatio * radius * 1.3f)
        val copX = center.x + (cop.worldX * radius * 0.45f)

        if (copY in (center.y - radius)..(center.y + radius)) {
          val copColor = if (cop.isVehicle) CrimsonRed else Color(0xFFEF4444)
          
          // Outer pulsing alert ring
          drawCircle(
            color = copColor.copy(alpha = pulseAlpha * 0.4f),
            radius = if (cop.isVehicle) 9f else 7f,
            center = Offset(copX, copY)
          )
          // Solid police dot
          drawCircle(
            color = if (cop.isStunned) Color(0xFF9CA3AF) else copColor,
            radius = if (cop.isVehicle) 4.5f else 3.5f,
            center = Offset(copX, copY)
          )
        }
      }

      // 8. Draw Player Marker (Bottom Center with Heading Pointer)
      val playerRadarY = center.y + (radius * 0.52f)
      val playerRadarX = center.x + (playerWorldX * radius * 0.45f)

      if (isPlayerHiding) {
        // Stealth Shield Glow
        drawCircle(
          color = Color(0x6606B6D4),
          radius = 10f,
          center = Offset(playerRadarX, playerRadarY)
        )
        drawCircle(
          color = Color(0xFF22D3EE),
          radius = 4.5f,
          center = Offset(playerRadarX, playerRadarY)
        )
      } else {
        // Player Beacon & Directional Arrowhead
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              if (isVehicleHijacked) GoldYemeniLight else EmeraldGreenLight,
              Color(0x0010B981)
            ),
            center = Offset(playerRadarX, playerRadarY),
            radius = 14f
          ),
          radius = 12f,
          center = Offset(playerRadarX, playerRadarY)
        )

        // Triangular Pointer facing forward
        val pointerPath = Path().apply {
          moveTo(playerRadarX, playerRadarY - 6f)
          lineTo(playerRadarX + 4.5f, playerRadarY + 4f)
          lineTo(playerRadarX, playerRadarY + 2f)
          lineTo(playerRadarX - 4.5f, playerRadarY + 4f)
          close()
        }
        drawPath(
          path = pointerPath,
          color = if (isVehicleHijacked) GoldYemeni else EmeraldGreen
        )
      }
    }

    // Top & Bottom Overlay Labels (Distance & Purser Count)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(4.dp),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Tag: Gate & Remaining Distance
      Surface(
        color = Color(0xCC000000),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(top = 1.dp)
      ) {
        Text(
          text = "🏁 ${distanceRemaining}m",
          color = GoldYemeniLight,
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
      }

      // Bottom Status: Stealth or Active Police Count
      Surface(
        color = Color(0xDD0B131E),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(bottom = 1.dp)
      ) {
        if (isPlayerHiding) {
          Text(
            text = "🥷 مخفي",
            color = Color(0xFF22D3EE),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
          )
        } else {
          Text(
            text = "🚨 ${policePursuers.size} دوريات",
            color = if (policePursuers.isNotEmpty()) CrimsonRedLight else Color.LightGray,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
          )
        }
      }
    }
  }
}
