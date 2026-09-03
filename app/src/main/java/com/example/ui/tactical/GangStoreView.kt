package com.example.ui.tactical

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.example.model.GameData
import com.example.model.TacticalArmoryItem
import com.example.model.TacticalItemCategory
import com.example.sound.GameSoundEffects
import com.example.ui.theme.*

@Composable
fun GangStoreView(
  repository: SanaGameRepository,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  val ownedItems by repository.ownedArmoryItemIds.collectAsState()
  var selectedCategory by remember { mutableStateOf<TacticalItemCategory?>(null) }
  var feedbackMsg by remember { mutableStateOf<String?>(null) }

  val filteredItems = remember(selectedCategory) {
    if (selectedCategory == null) GameData.armoryItems
    else GameData.armoryItems.filter { it.category == selectedCategory }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Banner
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                listOf(DarkSurface, SanaaGold.copy(alpha = 0.15f))
              )
            )
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "🛒 متجر أدوات العصابة والتسليح",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SanaaGold,
                fontSize = 16.sp
              )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "اشترِ قشور الموز، علب الطلاء، والمقاليع باستخدام المصروف المتجمع من المراحل!",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.LightGray,
                fontSize = 11.sp
              )
            )
          }

          // Balance Pill
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(DarkSurfaceVariant)
              .border(1.dp, SanaaGold, RoundedCornerShape(20.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("🪙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${stats.totalCoins} ريال",
              color = SanaaGold,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }

    // Category Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { selectedCategory = null },
            label = { Text("الكل (جميع الأدوات)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SanaaGold,
              selectedLabelColor = DarkBg
            )
          )
        }
        items(TacticalItemCategory.values()) { category ->
          FilterChip(
            selected = selectedCategory == category,
            onClick = { selectedCategory = category },
            label = { Text("${category.iconEmoji} ${category.titleAr}") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SanaaGold,
              selectedLabelColor = DarkBg
            )
          )
        }
      }
    }

    // Feedback message
    if (feedbackMsg != null) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = GangNeonGreen.copy(alpha = 0.15f)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("✨", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = feedbackMsg!!,
              color = GangNeonGreen,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Store Items List
    items(filteredItems) { item ->
      val isOwned = ownedItems.contains(item.id)
      GangStoreItemCard(
        item = item,
        isOwned = isOwned,
        userCoins = stats.totalCoins,
        onBuy = {
          val success = repository.buyArmoryItem(item.id, item.priceCoins)
          if (success) {
            GameSoundEffects.playVictoryFanfare()
            feedbackMsg = "تم شراء ${item.nameAr} بنجاح وإضافته لترسانة العصابة!"
          } else {
            feedbackMsg = "عفواً! المصروف الحالي لا يكفي لشراء ${item.nameAr}."
          }
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun GangStoreItemCard(
  item: TacticalArmoryItem,
  isOwned: Boolean,
  userCoins: Int,
  onBuy: () -> Unit
) {
  val canAfford = userCoins >= item.priceCoins

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isOwned) DarkSurface.copy(alpha = 0.8f) else DarkSurface
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isOwned) GangNeonGreen.copy(alpha = 0.5f) else DarkCardBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("armory_item_${item.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon Box
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(DarkSurfaceVariant)
          .border(
            1.dp,
            if (isOwned) GangNeonGreen else SanaaGold.copy(alpha = 0.3f),
            RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(item.iconEmoji, fontSize = 24.sp)
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Info
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = item.nameAr,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          )

          Text(
            text = "(${item.apCost} AP)",
            color = SanaaGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = item.typeDescAr,
          style = MaterialTheme.typography.bodySmall.copy(
            color = Color.Gray,
            fontSize = 11.sp
          )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "🎯 ${item.tacticalEffectAr}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = SanaaQamariyaCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
          )
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Buy / Owned Button
      if (isOwned) {
        Button(
          onClick = { },
          enabled = false,
          colors = ButtonDefaults.buttonColors(
            disabledContainerColor = GangNeonGreen.copy(alpha = 0.2f),
            disabledContentColor = GangNeonGreen
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = "Owned", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("مملوك", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      } else {
        Button(
          onClick = onBuy,
          enabled = canAfford,
          colors = ButtonDefaults.buttonColors(
            containerColor = SanaaGold,
            disabledContainerColor = Color.DarkGray
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("btn_buy_${item.id}")
        ) {
          Text(
            text = "${item.priceCoins} 🪙",
            color = if (canAfford) DarkBg else Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
