package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SanaaHeroProgression
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import com.example.sound.SanaaAmbientSoundManager
import com.example.ui.sanaa7d.AdaptivePursuitState
import com.example.ui.sanaa7d.Sanaa7DStage
import com.example.ui.theme.*

/**
 * Blur-effect Frosted Glass Pause Menu Overlay.
 * Displays when the user hits the back button or pause button during active chase gameplay.
 */
@Composable
fun BlurPauseMenuOverlay(
  currentStage: Sanaa7DStage,
  adaptivePursuitState: AdaptivePursuitState,
  currentScore: Int,
  coinsCollected: Int,
  distanceCovered: Float,
  copsEvaded: Int,
  onResumeGame: () -> Unit,
  onReturnToCharacterSelection: () -> Unit,
  onRestartStage: () -> Unit,
  onOpenLeaderboard: (() -> Unit)? = null,
  successfulMissionsCount: Int = 0,
  isDevMode: Boolean = false,
  onOpenAmbientSound: (() -> Unit)? = null,
  onOpenHeroProgression: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var isSoundMuted by remember { mutableStateOf(GameSoundEffects.isMuted) }
  var isHapticsEnabled by remember { mutableStateOf(HapticManager.isHapticsEnabled) }
  val heroTier = remember(successfulMissionsCount, isDevMode) {
    SanaaHeroProgression.getTier(successfulMissionsCount, isDevMode)
  }
  val ambientTrack by SanaaAmbientSoundManager.currentTrack.collectAsState()
  val ambientIntensity by SanaaAmbientSoundManager.intensityState.collectAsState()

  // Scrim box with blur-effect backdrop
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xBA070B12))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { /* Block touches to underlying game */ }
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    // Glassmorphism Container Card
    Card(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .widthIn(max = 480.dp)
        .shadow(24.dp, RoundedCornerShape(24.dp))
        .border(
          width = 1.5.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              GoldYemeni.copy(alpha = 0.8f),
              TealCyan.copy(alpha = 0.3f),
              Color(0x33FFFFFF)
            )
          ),
          shape = RoundedCornerShape(24.dp)
        )
        .testTag("pause_menu_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color(0xF2121B2A)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Pause Header with Animated Icon
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Brush.radialGradient(listOf(GoldYemeni, GoldYemeniDark)))
              .border(1.5.dp, GoldYemeniLight, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Pause,
              contentDescription = "Pause Icon",
              tint = Color.Black,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "اللعبة متوقفة مؤقتاً",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "أزقة صنعاء القديمة • استراحة تكتيكية",
              color = TealCyanLight,
              fontSize = 11.sp
            )
          }
        }

        Divider(color = Color(0x33FFFFFF), thickness = 1.dp)

        // Stage & Adaptive Difficulty Info Card
        Surface(
          color = Color(0x66080E18),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22F59E0B)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "📍 المرحلة: ${currentStage.titleAr}",
                color = GoldYemeniLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Surface(
                color = CrimsonRed.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, CrimsonRed)
              ) {
                Text(
                  text = "${adaptivePursuitState.tierBadgeEmoji} ${adaptivePursuitState.tierTitleAr}",
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            // Quick Stats Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              PauseStatMiniBadge("🏆 النقاط", "$currentScore", GoldYemeni)
              PauseStatMiniBadge("🪙 الريالات", "$coinsCollected", GoldYemeniLight)
              PauseStatMiniBadge("🏃‍♂️ المسافة", "${distanceCovered.toInt()}m", TealCyanLight)
              PauseStatMiniBadge("🚨 التملص", "$copsEvaded", EmeraldGreenLight)
            }

            // Sana'a Hero Progression Status & Faster Movement Speed Card
            Surface(
              color = if (heroTier.isSanaaHeroStatus) Color(0xFF2E1C02).copy(alpha = 0.95f) else Color(0x33000000),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (heroTier.isSanaaHeroStatus) GoldYemeni else Color(0xFFFF9100).copy(alpha = 0.6f)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenHeroProgression?.invoke() }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(heroTier.badgeEmoji, fontSize = 18.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = heroTier.statusTitleAr,
                      color = if (heroTier.isSanaaHeroStatus) GoldYemeni else Color.White,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${successfulMissionsCount} مهام ناجحة",
                      color = Color.LightGray,
                      fontSize = 8.5.sp
                    )
                  }
                }

                Surface(
                  color = if (heroTier.isSanaaHeroStatus) GoldYemeni else Color(0xFF00E676),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "⚡ +${heroTier.speedBoostPercent}% سرعة",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            // Interactive Ambient Music & Chase Intensity Pill
            Surface(
              color = Color(0x44050A10),
              shape = RoundedCornerShape(10.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(ambientIntensity.colorHex)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAmbientSound?.invoke() }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("🎵", fontSize = 16.sp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = ambientTrack.titleAr,
                      color = GoldYemeniLight,
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1
                    )
                    Text(
                      text = "${ambientIntensity.badgeEmoji} ${ambientIntensity.titleAr}",
                      color = Color(ambientIntensity.colorHex),
                      fontSize = 8.5.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }

                Surface(
                  color = Color(ambientIntensity.colorHex).copy(alpha = 0.25f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "${ambientIntensity.tempoBpm} BPM",
                    color = Color(ambientIntensity.colorHex),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }

        // Action Buttons
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // 1. Resume Game Button
          Button(
            onClick = {
              HapticManager.vibrateMovement()
              GameSoundEffects.playJump()
              onResumeGame()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldYemeni,
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("resume_game_button")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Resume",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "استئناف المطاردة",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // 2. Return to Main Character Selection Screen Button
          Button(
            onClick = {
              HapticManager.vibrateMovement()
              GameSoundEffects.playDoorCreak()
              onReturnToCharacterSelection()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF1E293B),
              contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, TealCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("return_to_character_selection_button")
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Character Selection",
              tint = TealCyanLight,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "العودة لاختيار الشخصيات والقائمة",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // 3. Restart Stage Button
          OutlinedButton(
            onClick = {
              HapticManager.vibrateMovement()
              GameSoundEffects.playRadioBeep()
              onRestartStage()
            },
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = Color.LightGray
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("restart_stage_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Restart Stage",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "إعادة محاولة المرحلة",
              fontSize = 12.sp
            )
          }

          // 4. Top 10 Leaderboard Button
          if (onOpenLeaderboard != null) {
            OutlinedButton(
              onClick = {
                HapticManager.vibrateMovement()
                GameSoundEffects.playCoin()
                onOpenLeaderboard()
              },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GoldYemeniLight
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, GoldYemeni.copy(alpha = 0.6f)),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("pause_open_leaderboard_button")
            ) {
              Text("🏆", fontSize = 14.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "لوحة الشرف: العشرة الأوائل (Top 10)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Quick Sound & Haptics Toggles
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          QuickToggleChip(
            icon = if (isHapticsEnabled) Icons.Default.Vibration else Icons.Default.PhoneAndroid,
            label = if (isHapticsEnabled) "الاهتزاز: مفعّل" else "الاهتزاز: معطّل",
            isActive = isHapticsEnabled,
            onClick = {
              val next = !isHapticsEnabled
              isHapticsEnabled = next
              HapticManager.isHapticsEnabled = next
              if (next) HapticManager.vibrateMovement()
            }
          )
        }
      }
    }
  }
}

@Composable
private fun PauseStatMiniBadge(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, color = Color.Gray, fontSize = 9.sp)
    Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun QuickToggleChip(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    color = if (isActive) Color(0x3310B981) else Color(0x22FFFFFF),
    shape = RoundedCornerShape(8.dp),
    border = androidx.compose.foundation.BorderStroke(
      0.8.dp,
      if (isActive) EmeraldGreenLight else Color.DarkGray
    ),
    modifier = Modifier.testTag("haptics_toggle_chip")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isActive) EmeraldGreenLight else Color.Gray,
        modifier = Modifier.size(14.dp)
      )
      Text(
        text = label,
        color = if (isActive) Color.White else Color.Gray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
