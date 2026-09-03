package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.sanaa7d.ChaseScoreState
import com.example.ui.sanaa7d.GameDifficulty
import com.example.ui.theme.*

/**
 * Composite Score Management & Difficulty Adjustment HUD
 */
@Composable
fun ChaseDifficultyAndScoreHud(
  scoreState: ChaseScoreState,
  currentDifficulty: GameDifficulty,
  onDifficultySelected: (GameDifficulty) -> Unit,
  onResetScore: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDifficultyModal by remember { mutableStateOf(false) }

  // Animated Combo Pop Scale
  val infiniteTransition = rememberInfiniteTransition(label = "score_combo_pulse")
  val comboPulse by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = if (scoreState.comboMultiplier > 1.2f) 1.08f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "combo_scale"
  )

  Surface(
    color = DarkSurface.copy(alpha = 0.94f),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.2.dp, SanaaGold.copy(alpha = 0.75f)),
    modifier = modifier.testTag("chase_difficulty_and_score_hud")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      // Top Row: Score Counter, Combo & Difficulty Trigger
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Score Display
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, tint = SanaaGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Column {
            Text(
              text = "${scoreState.currentScore} نقطة",
              color = SanaaGold,
              fontSize = 13.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "الأعلى: ${scoreState.highChaseScore} 🏆",
              color = Color.LightGray,
              fontSize = 8.5.sp
            )
          }
        }

        // Combo Multiplier Badge
        if (scoreState.comboMultiplier > 1.0f) {
          Surface(
            color = Color(0xFF6A1B9A),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFCE93D8)),
            modifier = Modifier.scale(comboPulse)
          ) {
            Text(
              text = "⚡ x${String.format("%.1f", scoreState.comboMultiplier)}",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Difficulty Button
        Surface(
          color = Color(0xFF1E2638),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, TaxiYellow.copy(alpha = 0.8f)),
          modifier = Modifier
            .clickable { showDifficultyModal = true }
            .testTag("open_difficulty_settings_btn")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(currentDifficulty.badgeEmoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = currentDifficulty.titleAr.split(" ").firstOrNull() ?: "صعوبة",
              color = TaxiYellow,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(Icons.Default.Tune, contentDescription = "إعداد الصعوبة", tint = TaxiYellow, modifier = Modifier.size(12.dp))
          }
        }
      }

      // Floating Score Gain Notification
      scoreState.recentScoreGain?.let { gain ->
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
              text = "+$gain نقطة ${scoreState.scoreReasonAr ?: ""}",
              color = GangNeonGreen,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
            Text("نقاط مطاردة", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
          }
        }
      }
    }
  }

  // ----------------------------------------------------
  // Difficulty Selection Modal Dialog
  // ----------------------------------------------------
  if (showDifficultyModal) {
    AlertDialog(
      onDismissRequest = { showDifficultyModal = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Speed, contentDescription = null, tint = SanaaGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ضبط صعوبة مطاردات صنعاء",
            fontWeight = FontWeight.Black,
            color = SanaaGold,
            fontSize = 15.sp
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "تعديل سرعة دوريات الشرطة واستجابة الملاحقة في أزقة صنعاء القديمة:",
            fontSize = 11.5.sp,
            color = Color.LightGray
          )

          GameDifficulty.values().forEach { diff ->
            val isSelected = (diff == currentDifficulty)
            Surface(
              color = if (isSelected) Color(0xFF263238) else DarkSurface,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(
                if (isSelected) 1.8.dp else 1.dp,
                if (isSelected) SanaaGold else Color.DarkGray
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onDifficultySelected(diff)
                  showDifficultyModal = false
                }
                .testTag("difficulty_option_${diff.name}")
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(diff.badgeEmoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = diff.titleAr,
                    color = if (isSelected) SanaaGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                  Text(
                    text = "${diff.subtitleAr} • مضاعف النقاط x${diff.scoreMultiplier}",
                    color = Color.LightGray,
                    fontSize = 9.5.sp
                  )
                  Text(
                    text = "سرعة الشرطة: ${((diff.policeSpeedMultiplier) * 100).toInt()}%",
                    color = if (diff.policeSpeedMultiplier > 1.3f) Color(0xFFFF5252) else GangNeonGreen,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
                if (isSelected) {
                  Surface(
                    color = SanaaGold,
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "مفعّل ✓",
                      color = DarkBg,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Black,
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showDifficultyModal = false },
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold)
        ) {
          Text("تأكيد وحفظ", color = DarkBg, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkBg,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
