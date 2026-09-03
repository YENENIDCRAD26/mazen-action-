package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.MilestoneToastEvent
import com.example.util.MilestoneToastManager
import com.example.util.MilestoneType
import kotlinx.coroutines.delay

/**
 * Animated In-Game HUD Toast Alert for player milestones (e.g. New Personal Best, Level Up).
 * Displays a glowing banner with Yemeni architectural aesthetics and auto-dismiss.
 */
@Composable
fun MilestoneToastHudOverlay(
  modifier: Modifier = Modifier
) {
  var activeEvent by remember { mutableStateOf<MilestoneToastEvent?>(null) }

  // Listen to incoming milestone events
  LaunchedEffect(Unit) {
    MilestoneToastManager.milestoneEvents.collect { event ->
      activeEvent = event
    }
  }

  // Auto-dismiss after 4.5 seconds
  LaunchedEffect(activeEvent) {
    if (activeEvent != null) {
      delay(4500)
      activeEvent = null
    }
  }

  AnimatedVisibility(
    visible = activeEvent != null,
    enter = slideInVertically(
      initialOffsetY = { -it },
      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    ) + fadeIn(animationSpec = tween(250)),
    exit = slideOutVertically(
      targetOffsetY = { -it },
      animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(200)),
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 16.dp, start = 16.dp, end = 16.dp)
  ) {
    val event = activeEvent ?: return@AnimatedVisibility

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val borderGlowAlpha by infiniteTransition.animateFloat(
      initialValue = 0.6f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(
        animation = tween(800, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "glow_alpha"
    )

    val gradientColors = when (event.type) {
      MilestoneType.NEW_PERSONAL_BEST -> listOf(
        GoldYemeniDark.copy(alpha = 0.95f),
        DarkBackground.copy(alpha = 0.98f),
        SanaaGold.copy(alpha = 0.90f)
      )
      MilestoneType.LEVEL_UP -> listOf(
        Color(0xFF6A1B9A).copy(alpha = 0.95f),
        DarkBackground.copy(alpha = 0.98f),
        TealCyanLight.copy(alpha = 0.90f)
      )
      MilestoneType.FASTEST_ESCAPE -> listOf(
        Color(0xFF00695C).copy(alpha = 0.95f),
        DarkBackground.copy(alpha = 0.98f),
        EmeraldGreenLight.copy(alpha = 0.90f)
      )
      MilestoneType.SANAA_HERO_UNLOCKED -> listOf(
        GoldYemeniDark.copy(alpha = 0.98f),
        Color(0xFFB78103).copy(alpha = 0.95f),
        SanaaGold.copy(alpha = 0.95f)
      )
      MilestoneType.SPEED_BOOST_UNLOCKED -> listOf(
        Color(0xFFE65100).copy(alpha = 0.95f),
        DarkBackground.copy(alpha = 0.98f),
        Color(0xFFFFD600).copy(alpha = 0.90f)
      )
      else -> listOf(
        DarkSurface.copy(alpha = 0.95f),
        DarkBackground.copy(alpha = 0.98f)
      )
    }

    val accentColor = when (event.type) {
      MilestoneType.NEW_PERSONAL_BEST -> SanaaGold
      MilestoneType.LEVEL_UP -> GoldYemeniLight
      MilestoneType.FASTEST_ESCAPE -> TealCyan
      MilestoneType.SANAA_HERO_UNLOCKED -> SanaaGold
      MilestoneType.SPEED_BOOST_UNLOCKED -> Color(0xFFFFD600)
      else -> EmeraldGreenLight
    }

    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(16.dp))
        .border(
          width = 2.dp,
          brush = Brush.horizontalGradient(
            colors = listOf(accentColor.copy(alpha = borderGlowAlpha), GoldYemeniLight, accentColor.copy(alpha = borderGlowAlpha))
          ),
          shape = RoundedCornerShape(16.dp)
        )
        .testTag("milestone_toast_banner"),
      shape = RoundedCornerShape(16.dp),
      color = Color.Transparent
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Brush.horizontalGradient(colors = gradientColors))
          .padding(horizontal = 14.dp, vertical = 12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          // Milestone Icon Badge with Ring Glow
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(DarkBackground.copy(alpha = 0.85f))
              .border(1.5.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = event.emoji,
              fontSize = 24.sp
            )
          }

          // Title and Description
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = event.titleAr,
                color = GoldYemeniLight,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.3.sp
              )
              Surface(
                color = accentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "إنجاز 🌟",
                  color = accentColor,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }

            Text(
              text = event.messageAr,
              color = TextWhiteSecondary,
              fontSize = 11.5.sp,
              lineHeight = 15.sp,
              fontWeight = FontWeight.Medium
            )
          }

          // Close button
          IconButton(
            onClick = { activeEvent = null },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Dismiss",
              tint = TextWhiteSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
