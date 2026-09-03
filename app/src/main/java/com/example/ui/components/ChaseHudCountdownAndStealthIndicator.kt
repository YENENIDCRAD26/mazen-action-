package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.sanaa7d.ChaseCountdownState
import com.example.ui.sanaa7d.LevelProgressionUiState
import com.example.ui.sanaa7d.PlayerStealthMode
import com.example.ui.theme.*

/**
 * Modern 7D HUD Component combining:
 * 1. Chase Countdown Timer with urgency warning states.
 * 2. Dynamic Pulsing Stealth Indicator that morphs its shape, color, and pulse rhythm between 'مختبئ' and 'مكشوف'.
 * 3. Smoothly animating Player Level and XP progress bar.
 */
@Composable
fun ChaseHudCountdownAndStealthIndicator(
  stealthMode: PlayerStealthMode,
  countdownState: ChaseCountdownState,
  levelState: LevelProgressionUiState,
  modifier: Modifier = Modifier,
  onLevelClick: () -> Unit = {},
  onStealthClick: () -> Unit = {}
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("chase_hud_composite_panel"),
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    // ----------------------------------------------------------------------
    // Top Row: [Countdown Timer ⏳] & [Dynamic Pulsing Stealth Status 🥷/🚨]
    // ----------------------------------------------------------------------
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Chase Countdown Timer Component (عداد وقت تنازلي للمطاردة)
      ChaseCountdownTimerHud(
        countdownState = countdownState,
        modifier = Modifier
          .weight(1f)
          .testTag("chase_countdown_timer")
      )

      // 2. Dynamic Pulsing Stealth Status Indicator (مؤشر حالة متغير مع أنيميشن نبض)
      PulsingStealthStatusIndicator(
        stealthMode = stealthMode,
        onClick = onStealthClick,
        modifier = Modifier
          .weight(1.35f)
          .testTag("chase_stealth_indicator")
      )
    }

    // ----------------------------------------------------------------------
    // Visual Level & XP Progress Bar Strip (شريط التقدم المرئي للمستوى)
    // ----------------------------------------------------------------------
    VisualLevelAndXpProgressHud(
      levelState = levelState,
      onClick = onLevelClick,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("chase_hud_level_progress_bar")
    )
  }
}

/**
 * 1. Chase Countdown Timer HUD (عداد وقت تنازلي للمطاردة)
 */
@Composable
fun ChaseCountdownTimerHud(
  countdownState: ChaseCountdownState,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "countdown_urgency_pulse")
  val alertPulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = if (countdownState.isLowTime) 1.08f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "timer_pulse_scale"
  )

  val timerBorderColor = when {
    countdownState.isLowTime -> Color(0xFFFF1744)
    countdownState.progressRatio < 0.4f -> TaxiYellow
    else -> SanaaGold
  }

  val timerBgBrush = Brush.verticalGradient(
    colors = when {
      countdownState.isLowTime -> listOf(Color(0xFF3D0808), Color(0xFF1F0303))
      else -> listOf(DarkSurface.copy(alpha = 0.95f), DarkBg.copy(alpha = 0.92f))
    }
  )

  Surface(
    modifier = modifier
      .scale(if (countdownState.isLowTime) alertPulseScale else 1.0f)
      .shadow(elevation = if (countdownState.isLowTime) 8.dp else 3.dp, shape = RoundedCornerShape(12.dp))
      .semantics { contentDescription = "عداد وقت المطاردة المتبقي: ${countdownState.timeLeftSeconds} ثانية" },
    shape = RoundedCornerShape(12.dp),
    color = Color.Transparent,
    border = BorderStroke(if (countdownState.isLowTime) 1.8.dp else 1.2.dp, timerBorderColor)
  ) {
    Box(
      modifier = Modifier
        .background(timerBgBrush)
        .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = if (countdownState.isLowTime) Color(0xFFFF5252) else SanaaGold,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = countdownState.formattedTime,
            color = if (countdownState.isLowTime) Color(0xFFFF5252) else SanaaGold,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
          )
          if (countdownState.isLowTime) {
            Spacer(modifier = Modifier.width(3.dp))
            Text("⚠️", fontSize = 10.sp)
          }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Progress Bar
        LinearProgressIndicator(
          progress = { countdownState.progressRatio },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = if (countdownState.isLowTime) Color(0xFFFF1744) else SanaaGold,
          trackColor = Color(0xFF262626)
        )
      }
    }
  }
}

/**
 * 2. Dynamic Pulsing Stealth Status Indicator (مؤشر حالة متغير مع أنيميشن نبض)
 * Morphs shape and color dynamically:
 * - HIDDEN (مختبئ): Rounded shield shape, emerald green palette, gentle breathing pulse rhythm.
 * - EXPOSED (مكشوف): Angular warning polygon shape, fiery crimson red palette, fast alarm pulse rhythm.
 */
@Composable
fun PulsingStealthStatusIndicator(
  stealthMode: PlayerStealthMode,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isHiding = stealthMode.isHiding

  // Dynamic Pulsing Animation configured based on state
  val pulseTransition = rememberInfiniteTransition(label = "stealth_pulse_transition")

  // Fast pulse when exposed (400ms), calm breathing when hidden (900ms)
  val pulseScale by pulseTransition.animateFloat(
    initialValue = if (isHiding) 1.0f else 0.95f,
    targetValue = if (isHiding) 1.06f else 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (isHiding) 900 else 400,
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val pulseGlowAlpha by pulseTransition.animateFloat(
    initialValue = if (isHiding) 0.35f else 0.6f,
    targetValue = if (isHiding) 0.85f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = if (isHiding) 900 else 400,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_glow_alpha"
  )

  // Morphing Shape: Shield Capsule when hidden vs Sharp Octagonal / Cut polygon when exposed
  val dynamicShape: Shape = if (isHiding) {
    RoundedCornerShape(16.dp)
  } else {
    CutCornerShape(10.dp)
  }

  // Morphing Colors
  val primaryAccentColor = if (isHiding) GangNeonGreen else Color(0xFFFF1744)
  val backgroundBrush = if (isHiding) {
    Brush.horizontalGradient(
      listOf(
        Color(0xFF00381B).copy(alpha = 0.96f),
        Color(0xFF00220F).copy(alpha = 0.96f)
      )
    )
  } else {
    Brush.horizontalGradient(
      listOf(
        Color(0xFF4A0A0A).copy(alpha = 0.96f),
        Color(0xFF260505).copy(alpha = 0.96f)
      )
    )
  }

  Surface(
    modifier = modifier
      .scale(pulseScale)
      .clickable { onClick() }
      .semantics {
        contentDescription = if (isHiding) "حالة اللاعب: مختبئ في الأزقة" else "حالة اللاعب: مكشوف للدوريات"
      },
    shape = dynamicShape,
    color = Color.Transparent,
    border = BorderStroke(1.8.dp, primaryAccentColor.copy(alpha = pulseGlowAlpha))
  ) {
    Box(
      modifier = Modifier
        .background(backgroundBrush)
        .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Dynamic Icon Indicator
          Text(
            text = stealthMode.iconEmoji,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.width(5.dp))
          Column {
            Text(
              text = stealthMode.titleAr,
              color = primaryAccentColor,
              fontWeight = FontWeight.Black,
              fontSize = 10.5.sp,
              maxLines = 1
            )
            Text(
              text = stealthMode.subtitleAr,
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 8.sp,
              maxLines = 1
            )
          }
        }

        // State Tag Badge
        Surface(
          color = primaryAccentColor.copy(alpha = 0.25f),
          shape = if (isHiding) RoundedCornerShape(6.dp) else CutCornerShape(4.dp),
          border = BorderStroke(0.8.dp, primaryAccentColor)
        ) {
          Text(
            text = if (isHiding) "تخفي آمن ✓" else "إنذار خطر ⚠️",
            color = primaryAccentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 8.5.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}

/**
 * 3. Visual Level & XP Progress HUD (شريط التقدم المرئي للمستوى ونقاط الخبرة)
 */
@Composable
fun VisualLevelAndXpProgressHud(
  levelState: LevelProgressionUiState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val levelInfo = levelState.levelInfo

  // Smooth animated progress bar transition
  val animatedProgressRatio by animateFloatAsState(
    targetValue = levelInfo.progressRatio,
    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
    label = "xp_progress_ratio_animation"
  )

  Surface(
    color = DarkSurface.copy(alpha = 0.93f),
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.2.dp, SanaaGold.copy(alpha = 0.75f)),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
          Surface(
            color = SanaaGold,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "${levelInfo.badgeEmoji} Lv.${levelInfo.currentLevel}",
              color = DarkBg,
              fontWeight = FontWeight.Black,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = levelInfo.currentTitleAr,
              color = SanaaGold,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              maxLines = 1
            )
            Text(
              text = if (levelInfo.isMaxLevel) "المستوى الأقصى (الزعيم الكبير) 👑" else "${levelInfo.currentLevelXp} / ${levelInfo.xpRequiredForCurrentLevelSpan} XP",
              color = Color.LightGray,
              fontSize = 8.5.sp
            )
          }
        }

        // Interactive Progress Bar
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(0.8f)
        ) {
          LinearProgressIndicator(
            progress = { animatedProgressRatio },
            modifier = Modifier
              .weight(1f)
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = GangNeonGreen,
            trackColor = Color(0xFF232323)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${(animatedProgressRatio * 100).toInt()}%",
            color = GangNeonGreen,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black
          )
        }
      }

      // In-Game Floating XP Notice Banner
      levelState.recentXpNotice?.let { xpNotice ->
        Spacer(modifier = Modifier.height(3.dp))
        Surface(
          color = Color(0xFF00381B).copy(alpha = 0.95f),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(0.8.dp, GangNeonGreen),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "⚡ $xpNotice",
              color = GangNeonGreen,
              fontSize = 9.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "نقاط خبرة",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 8.sp
            )
          }
        }
      }

      // Level Up Celebratory Notification
      levelState.recentLeveledUpRank?.let { newRank ->
        Spacer(modifier = Modifier.height(3.dp))
        Surface(
          color = Color(0xFF2C1E03).copy(alpha = 0.98f),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.5.dp, SanaaGold),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(newRank.badgeEmoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "🎉 ارتقاء جديد: المستوى ${newRank.level}!",
                color = SanaaGold,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = "«${newRank.titleAr}» • ${newRank.perkDescAr}",
                color = Color.White,
                fontSize = 8.5.sp
              )
            }
            Surface(
              color = SanaaGold,
              shape = RoundedCornerShape(5.dp)
            ) {
              Text(
                text = "+${newRank.rewardCoinsOnReach}🪙",
                color = DarkBg,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}
