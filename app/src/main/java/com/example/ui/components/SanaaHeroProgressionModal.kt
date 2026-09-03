package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameStats
import com.example.model.HeroProgressionTier
import com.example.model.SanaaHeroProgression
import com.example.ui.theme.*

/**
 * Celebratory and informative modal showcasing the 'Sana'a Hero' Progression Model,
 * player's active movement speed boosts, missions accomplished, and upcoming perk tiers.
 */
@Composable
fun SanaaHeroProgressionModal(
  stats: GameStats,
  isDevMode: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTier = SanaaHeroProgression.getTier(stats.successfulMissionsCount, isDevMode)
  val nextTier = SanaaHeroProgression.getNextTier(stats.successfulMissionsCount, isDevMode)
  val progressRatio = SanaaHeroProgression.getProgressToNextTier(stats.successfulMissionsCount, isDevMode)
  val allTiers = SanaaHeroProgression.tiers

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(4.dp)
        .testTag("sanaa_hero_progression_modal"),
      shape = RoundedCornerShape(24.dp),
      color = DarkSurface,
      border = BorderStroke(2.dp, if (currentTier.isSanaaHeroStatus) SanaaGold else Color(0xFFFF9100))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    if (currentTier.isSanaaHeroStatus)
                      listOf(GoldYemeni, GoldYemeniDark)
                    else
                      listOf(Color(0xFFFF9100), Color(0xFFE65100))
                  )
                )
                .border(1.2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(currentTier.badgeEmoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "نظام بطل صنعاء وسرعة الحركة",
                color = SanaaGold,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
              )
              Text(
                text = "Sana'a Hero & Movement Speed Progression",
                color = Color.LightGray,
                fontSize = 9.sp
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("close_hero_modal_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Status Spotlight Card
        Surface(
          color = if (currentTier.isSanaaHeroStatus)
            Color(0xFF2E1C02).copy(alpha = 0.95f)
          else
            DarkBg.copy(alpha = 0.90f),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(
            1.5.dp,
            if (currentTier.isSanaaHeroStatus) SanaaGold else Color(0xFFFF9100).copy(alpha = 0.7f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            if (currentTier.isSanaaHeroStatus) {
              Surface(
                color = SanaaGold,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
              ) {
                Text(
                  text = "👑 رتبة بطل صنعاء الأسطورية مفتوحة بالكامل 👑",
                  color = DarkBg,
                  fontWeight = FontWeight.Black,
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Text(
              text = currentTier.statusTitleAr,
              color = if (currentTier.isSanaaHeroStatus) SanaaGold else Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 17.sp,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = currentTier.descriptionAr,
              color = Color.LightGray,
              fontSize = 11.sp,
              textAlign = TextAlign.Center,
              lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Badges: Speed Multiplier + Missions Accomplished
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                color = Color(0xFF00E676).copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("سرعة الحركة", color = Color.LightGray, fontSize = 9.sp)
                  }
                  Text(
                    text = "+${currentTier.speedBoostPercent}% (${currentTier.speedMultiplier}x)",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                  )
                }
              }

              Surface(
                color = SanaaGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SanaaGold.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = SanaaGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("المهام الناجحة", color = Color.LightGray, fontSize = 9.sp)
                  }
                  Text(
                    text = "${stats.successfulMissionsCount} مهام",
                    color = SanaaGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                  )
                }
              }
            }

            // Progress to next tier
            if (nextTier != null && !isDevMode) {
              val remaining = nextTier.requiredMissions - stats.successfulMissionsCount
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "التقدم نحو: ${nextTier.statusTitleAr}",
                  color = TaxiYellow,
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "متبقي $remaining مهام",
                  color = Color.LightGray,
                  fontSize = 9.sp
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = TaxiYellow,
                trackColor = Color(0xFF222222)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progression Tiers Roadmap
        Text(
          text = "🏆 خارطة رتب بطل صنعاء وتعزيزات السرعة:",
          color = SanaaGold,
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          allTiers.forEach { tier ->
            val isCurrent = tier.tierIndex == currentTier.tierIndex
            val isUnlocked = (stats.successfulMissionsCount >= tier.requiredMissions) || isDevMode

            Surface(
              color = when {
                isCurrent -> if (tier.isSanaaHeroStatus) Color(0xFF332002) else DarkBg.copy(alpha = 0.95f)
                isUnlocked -> Color(0xFF0B1713).copy(alpha = 0.8f)
                else -> DarkBg.copy(alpha = 0.5f)
              },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(
                width = if (isCurrent) 1.8.dp else 1.dp,
                color = when {
                  isCurrent -> if (tier.isSanaaHeroStatus) SanaaGold else Color(0xFFFF9100)
                  isUnlocked -> Color(0xFF00E676).copy(alpha = 0.5f)
                  else -> Color(0x33FFFFFF)
                }
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Tier Icon Badge
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                      if (isUnlocked)
                        if (tier.isSanaaHeroStatus) SanaaGold.copy(alpha = 0.25f) else Color(0xFF00E676).copy(alpha = 0.2f)
                      else
                        Color.DarkGray.copy(alpha = 0.4f)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(tier.badgeEmoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = tier.statusTitleAr,
                      color = if (isCurrent) SanaaGold else if (isUnlocked) Color.White else Color.Gray,
                      fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                      fontSize = 11.5.sp
                    )
                    if (isCurrent) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Surface(
                        color = SanaaGold,
                        shape = RoundedCornerShape(4.dp)
                      ) {
                        Text(
                          text = "الرتبة الحالية",
                          color = DarkBg,
                          fontWeight = FontWeight.Black,
                          fontSize = 7.5.sp,
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                      }
                    }
                  }

                  Text(
                    text = "المتطلب: ${if (tier.requiredMissions == 0) "البداية" else "${tier.requiredMissions} مهام ناجحة"} • سرعة حركة: +${tier.speedBoostPercent}%",
                    color = if (isUnlocked) Color(0xFF00E676) else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                if (isUnlocked) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "مفتوح",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(20.dp)
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "مغلق",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Close Button
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("dismiss_hero_progression_btn")
        ) {
          Text(
            text = "متابعة المطاردة في أزقة صنعاء 🦘",
            color = DarkBg,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}
