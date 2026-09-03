package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Faction
import com.example.sound.GameSoundEffects
import com.example.ui.theme.*

@Composable
fun SanaaTopBar(
  title: String,
  subtitle: String? = null,
  coins: Int,
  soundEnabled: Boolean,
  onSoundToggle: () -> Unit,
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = DarkSurface,
    tonalElevation = 8.dp,
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(DarkSurface, DarkBg)
          )
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          if (onBackClick != null) {
            IconButton(
              onClick = {
                GameSoundEffects.playCoin()
                onBackClick()
              },
              modifier = Modifier
                .testTag("back_button")
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "الرجوع",
                tint = SanaaGold
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
          }

          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
              )
            )
            if (subtitle != null) {
              Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 12.sp,
                  color = SanaaGold
                )
              )
            }
          }
        }

        // Right side: Coins badge & Audio toggle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Coins badge
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Brush.horizontalGradient(listOf(SanaaMudWarm, SanaaClay)))
              .border(1.dp, SanaaGold, RoundedCornerShape(20.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "🪙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$coins ريال",
              style = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            )
          }

          // Sound Toggle Button
          IconButton(
            onClick = onSoundToggle,
            modifier = Modifier
              .testTag("sound_toggle_btn")
              .size(36.dp)
              .clip(CircleShape)
              .background(DarkSurfaceVariant)
          ) {
            Icon(
              imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
              contentDescription = "الصوت",
              tint = if (soundEnabled) SanaaGold else Color.Gray,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Decorative Yemeni Qamariya Bar below
      Spacer(modifier = Modifier.height(8.dp))
      QamariyaDecorativePattern()
    }
  }
}

@Composable
fun QamariyaDecorativePattern(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(4.dp)
      .clip(RoundedCornerShape(2.dp)),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    val qamariyaColors = listOf(
      SanaaQamariyaCyan,
      SanaaGold,
      SanaaQamariyaRuby,
      SanaaQamariyaEmerald,
      PoliceAccent,
      GangGraffitiPink,
      TaxiYellow,
      SanaaQamariyaAmber
    )
    qamariyaColors.forEach { color ->
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .background(color)
      )
    }
  }
}

@Composable
fun FactionSelectorPill(
  selectedFaction: Faction,
  onSelectFaction: (Faction) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(DarkSurfaceVariant)
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Faction.values().forEach { faction ->
      val isSelected = faction == selectedFaction
      val targetBg = if (isSelected) {
        if (faction == Faction.GANG) GangShawlRed else PoliceAccent
      } else Color.Transparent

      val animatedBg by animateColorAsState(targetValue = targetBg, label = "faction_bg")

      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(12.dp))
          .background(animatedBg)
          .clickable {
            GameSoundEffects.playJump()
            onSelectFaction(faction)
          }
          .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = if (faction == Faction.GANG) "👑" else "👮‍♂️",
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (faction == Faction.GANG) "عصابة المشاغبين" else "شرطة صنعاء",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else Color.LightGray,
              fontSize = 13.sp
            )
          )
        }
      }
    }
  }
}
