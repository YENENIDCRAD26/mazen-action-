package com.example.ui.hq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SanaGameRepository
import com.example.model.Faction
import com.example.model.UpgradeItem
import com.example.sound.GameSoundEffects
import com.example.ui.components.SanaaTopBar
import com.example.ui.theme.*

@Composable
fun HqUpgradesScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  val upgrades by repository.upgrades.collectAsState()
  val selectedFaction by repository.selectedFaction.collectAsState()

  var feedbackMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    SanaaTopBar(
      title = "متجر وترقيات المقر",
      subtitle = "تطوير معدات العصابة والدوريات التكتيكية",
      coins = stats.totalCoins,
      soundEnabled = stats.soundEnabled,
      onSoundToggle = {
        repository.toggleSound()
        GameSoundEffects.isMuted = !stats.soundEnabled
      },
      onBackClick = onNavigateBack
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Summary Stats Header Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SanaaGold, RoundedCornerShape(16.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🎒 الجواسيس المجندين", color = Color.Gray, fontSize = 11.sp)
              Text(
                text = "${stats.recruitedScouts} صغار",
                color = GangNeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🏆 مهمات التكتيك", color = Color.Gray, fontSize = 11.sp)
              Text(
                text = "${stats.tacticsMissionsWon} انتصار",
                color = SanaaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🛡️ الضباط المحررين", color = Color.Gray, fontSize = 11.sp)
              Text(
                text = "${stats.rescuedOfficers} ضباط",
                color = PoliceAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }
        }
      }

      // Upgrade Items List
      items(upgrades) { upgrade ->
        UpgradeItemCard(
          upgrade = upgrade,
          playerCoins = stats.totalCoins,
          onBuy = {
            val success = repository.buyUpgrade(upgrade.id)
            if (success) {
              GameSoundEffects.playCoin()
              feedbackMessage = "تمت ترقية ${upgrade.titleAr} بنجاح! ⚡"
            } else {
              feedbackMessage = "عفواً! لا تملك ريالات كافية للترقية 🪙"
            }
          }
        )
      }

      // Feedback message banner
      if (feedbackMessage != null) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = feedbackMessage ?: "",
              modifier = Modifier.padding(12.dp),
              color = SanaaGold,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun UpgradeItemCard(
  upgrade: UpgradeItem,
  playerCoins: Int,
  onBuy: () -> Unit
) {
  val isMax = upgrade.level >= upgrade.maxLevel
  val canAfford = playerCoins >= upgrade.cost && !isMax

  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon Box
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Text(text = upgrade.iconEmoji, fontSize = 24.sp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Title & Details
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = upgrade.titleAr,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          )
          Text(
            text = "المستوى ${upgrade.level}/${upgrade.maxLevel}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SanaaGold,
              fontSize = 10.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = upgrade.descriptionAr,
          style = MaterialTheme.typography.bodySmall.copy(
            color = Color.LightGray,
            fontSize = 11.sp,
            lineHeight = 15.sp
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Level Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
          for (i in 1..upgrade.maxLevel) {
            Box(
              modifier = Modifier
                .size(width = 16.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (i <= upgrade.level) SanaaGold else DarkSurfaceVariant)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Buy Button
      Button(
        onClick = onBuy,
        enabled = canAfford,
        colors = ButtonDefaults.buttonColors(
          containerColor = SanaaClay,
          disabledContainerColor = DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.testTag("buy_upgrade_${upgrade.id}")
      ) {
        if (isMax) {
          Text("مكتمل ✨", fontSize = 11.sp, color = SanaaGold, fontWeight = FontWeight.Bold)
        } else {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "ترقية ⚡",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (canAfford) Color.White else Color.Gray
            )
            Text(
              text = "${upgrade.cost} ريال",
              fontSize = 9.sp,
              color = if (canAfford) TaxiYellow else Color.Gray
            )
          }
        }
      }
    }
  }
}
