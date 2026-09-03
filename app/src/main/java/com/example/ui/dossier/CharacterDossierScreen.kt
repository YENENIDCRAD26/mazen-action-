package com.example.ui.dossier

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SanaGameRepository
import com.example.model.CharacterProfile
import com.example.model.Faction
import com.example.model.GameData
import com.example.model.GameStats
import com.example.sound.GameSoundEffects
import com.example.ui.components.SanaaTopBar
import com.example.ui.theme.*

@Composable
fun CharacterDossierScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Cloned Real Hero, 1: Characters, 2: Story Moments, 3: Sanaa Map Locations
  var selectedCharacter by remember { mutableStateOf(GameData.characters.first()) }
  var filterFaction by remember { mutableStateOf<Faction?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    SanaaTopBar(
      title = "ملفات الأبطال والأحداث",
      subtitle = "صنعاء القديمة • السجلات التكتيكية والقصصية",
      coins = stats.totalCoins,
      soundEnabled = stats.soundEnabled,
      onSoundToggle = {
        repository.toggleSound()
        GameSoundEffects.isMuted = !stats.soundEnabled
      },
      onBackClick = onNavigateBack
    )

    // Segmented Navigation Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = DarkSurface,
      contentColor = SanaaGold
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = {
          selectedTab = 0
          GameSoundEffects.playJump()
        },
        text = { Text("البطل الحقيقي 👑", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = {
          selectedTab = 1
          GameSoundEffects.playJump()
        },
        text = { Text("شخصيات اللعبة", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = {
          selectedTab = 2
          GameSoundEffects.playJump()
        },
        text = { Text("أرشيف الأحداث", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 3,
        onClick = {
          selectedTab = 3
          GameSoundEffects.playJump()
        },
        text = { Text("خريطة صنعاء", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
      )
    }

    when (selectedTab) {
      0 -> {
        // Cloned Real Hero Studio Showcase
        RealClonedHeroStudio(stats = stats)
      }

      1 -> {
        // Characters Tab
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Faction Filter Chips
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = filterFaction == null,
                onClick = { filterFaction = null },
                label = { Text("الكل") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = SanaaGold,
                  selectedLabelColor = DarkBg
                )
              )
              FilterChip(
                selected = filterFaction == Faction.GANG,
                onClick = { filterFaction = Faction.GANG },
                label = { Text("👑 عصابة المشاغبين") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = GangShawlRed,
                  selectedLabelColor = Color.White
                )
              )
              FilterChip(
                selected = filterFaction == Faction.POLICE,
                onClick = { filterFaction = Faction.POLICE },
                label = { Text("👮‍♂️ رجال الشرطة") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PoliceAccent,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          // Horizontal Avatar Carousel
          item {
            val filteredCharacters = GameData.characters.filter { filterFaction == null || it.faction == filterFaction }
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(filteredCharacters) { char ->
                val isSelected = char.id == selectedCharacter.id
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                    .border(
                      width = if (isSelected) 2.dp else 1.dp,
                      color = if (isSelected) SanaaGold else DarkCardBorder,
                      shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                      selectedCharacter = char
                      GameSoundEffects.playJump()
                    }
                    .padding(12.dp)
                    .width(100.dp)
                ) {
                  Text(text = char.iconEmoji, fontSize = 32.sp)
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = char.nameAr.take(12),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = Color.White,
                      fontSize = 11.sp
                    ),
                    maxLines = 1
                  )
                }
              }
            }
          }

          // Selected Character Detail Card
          item {
            CharacterDetailCard(character = selectedCharacter)
          }
        }
      }

      2 -> {
        // Story Moments Archive Tab
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(GameData.storyMoments) { moment ->
            Card(
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = moment.iconEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = moment.titleAr,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = SanaaGold,
                          fontSize = 14.sp
                        )
                      )
                      Text(
                        text = "📍 ${moment.locationAr} • ${moment.categoryAr}",
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = Color.LightGray,
                          fontSize = 11.sp
                        )
                      )
                    }
                  }
                  AssistChip(
                    onClick = {},
                    label = { Text("#${moment.id}", fontSize = 11.sp) }
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = moment.descriptionAr,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                  )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(text = "💡 نصيحة تكتيكية: ", color = TaxiYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Text(text = moment.tacticalTipAr, color = Color.White, fontSize = 11.sp)
                }
              }
            }
          }
        }
      }

      3 -> {
        // Sana'a Locations Map Tab
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(GameData.sanaaLocations) { loc ->
            Card(
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SanaaMudWarm, RoundedCornerShape(16.dp))
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = loc.iconEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = loc.nameAr,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = SanaaGold,
                          fontSize = 15.sp
                        )
                      )
                      Text(
                        text = loc.typeAr,
                        color = Color.LightGray,
                        fontSize = 11.sp
                      )
                    }
                  }

                  // Danger Level Stars
                  Row {
                    repeat(loc.dangerLevel) {
                      Text("⭐", fontSize = 12.sp)
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = loc.descriptionAr,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun RealClonedHeroStudio(stats: GameStats) {
  var selectedOutfit by remember { mutableIntStateOf(0) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Cinematic Hero Banner
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, SanaaGold, RoundedCornerShape(20.dp))
      ) {
        Column {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_sanaa_hero_action),
              contentDescription = "مازن في شوارع صنعاء",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, DarkSurface.copy(alpha = 0.95f))
                  )
                )
            )
            Surface(
              color = GangShawlRed,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.padding(12.dp)
            ) {
              Text(
                text = "✨ بطل حقيقي مستنسخ من صنعاء",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_hero_avatar),
                contentDescription = "صورة وجه البطل المستنسخ",
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .border(2.5.dp, SanaaGold, CircleShape),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "مازن «بطل أزقة صنعاء» 👑",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                  )
                )
                Text(
                  text = "العمر: 10 سنوات • حارة باب السباح • صنعاء القديمة",
                  color = SanaaGold,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "الصفة: قائد المشاغبين الصغار وأسرع عداء على الأسطح الطينية",
                  color = Color.LightGray,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(10.dp)
            ) {
              Text(
                text = "«ما في شرطي يقدر يمسكني في سوق الملح.. الأزقة بيتنا والقمريات شواهدنا!»",
                color = TaxiYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Outfit Customization Selector
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "اختر زي ومظهر البطل في الشوارع 🥋",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val outfits = listOf(
              "الزي الصنعاني التقليدي 🇾🇪",
              "ستايل أبطال الباركور 🏃‍♂️",
              "بزة الزعيم التكتيكية 👑"
            )
            outfits.forEachIndexed { index, name ->
              val isSelected = selectedOutfit == index
              Surface(
                color = if (isSelected) SanaaClay else DarkSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SanaaGold else DarkCardBorder),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    selectedOutfit = index
                    GameSoundEffects.playJump()
                  }
              ) {
                Text(
                  text = name,
                  color = if (isSelected) Color.White else Color.LightGray,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(8.dp),
                  maxLines = 2
                )
              }
            }
          }
        }
      }
    }

    // Real Hero Physical & Action Attributes
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "القدرات الحركية والتكتيكية للبطل ⚡",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = SanaaGold,
              fontSize = 14.sp
            )
          )
          Spacer(modifier = Modifier.height(10.dp))

          StatBarRow(label = "السرعة والرشاقة", value = 0.96f, color = GangNeonGreen)
          StatBarRow(label = "باركور أسطح صنعاء", value = 0.98f, color = SanaaQamariyaCyan)
          StatBarRow(label = "دقة المقلاع ومسدس الخرز", value = 0.92f, color = TaxiYellow)
          StatBarRow(label = "التخفي والتمويه بالأزقة", value = 0.90f, color = GangGraffitiPink)
          StatBarRow(label = "قيادة وهجولة الدباب الصنعاني", value = 0.88f, color = GangShawlRed)
        }
      }
    }

    // Stunt Master Actions List
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "الحركات الاستعراضية في الشوارع 🎪",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          )
          Spacer(modifier = Modifier.height(8.dp))

          val stunts = listOf(
            Triple("قفزة القمريات العالية 🏰", "القفز بين أسطح البيوت الطينية بارتفاع 3 طوابق بدون إصابة", "98% نجاح"),
            Triple("رمية المقلاع المرتدة 🪨", "إصابة كاميرات ودوريات الشرطة عبر ارتداد الحصى في الجدران", "95% نجاح"),
            Triple("تفحيطة الدباب المزدوجة 🚐💨", "استعارة دباب الركاب والدوران 360 درجة لإرباك الطوق الأمني", "92% نجاح"),
            Triple("ستار قشور الموز والمانجو 🍌", "إلقاء الفواكه في المنعطفات لإسقاط الدراجات النارية الملاحقة", "99% نجاح")
          )

          stunts.forEachIndexed { index, (title, desc, success) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
                .padding(8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(title, color = SanaaGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(desc, color = Color.LightGray, fontSize = 10.sp)
              }
              Surface(
                color = GangNeonGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(success, color = GangNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CharacterDetailCard(character: CharacterProfile) {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.5.dp, if (character.faction == Faction.GANG) GangShawlRed else PoliceAccent, RoundedCornerShape(18.dp))
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header: Avatar, Name, Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (character.faction == Faction.GANG) GangShawlRed else PoliceAccent),
          contentAlignment = Alignment.Center
        ) {
          Text(text = character.iconEmoji, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = character.nameAr,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 17.sp
            )
          )
          Text(
            text = character.titleAr,
            style = MaterialTheme.typography.bodySmall.copy(
              color = SanaaGold,
              fontSize = 12.sp
            )
          )
          Text(
            text = "العمر: ${character.age} سنة • ${character.faction.badgeAr}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color.LightGray,
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Authentic Quote Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(DarkSurfaceVariant)
          .padding(10.dp)
      ) {
        Text(
          text = character.quoteAr,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TaxiYellow,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Biography
      Text(
        text = character.bioAr,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Weapons & Specials
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(DarkBg)
          .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row {
          Text("🔫 السلاح / المعدات: ", color = SanaaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(character.weaponAr, color = Color.White, fontSize = 11.sp)
        }
        Row {
          Text("⚡ المهارة الخاصة: ", color = GangNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(character.specialAbilityAr, color = Color.White, fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tactical Stat Bars
      Text("القدرات والمهارات التكتيكية", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))

      StatBarRow(label = "السرعة والمراوغة", value = character.speedStat, color = SanaaQamariyaCyan)
      StatBarRow(label = "التخفي والتمويه", value = character.stealthStat, color = GangGraffitiPink)
      StatBarRow(label = "التخطيط التكتيكي", value = character.tacticStat, color = SanaaGold)
      StatBarRow(label = "الباركور وتسلق الأسطح", value = character.parkourStat, color = GangNeonGreen)
    }
  }
}

@Composable
fun StatBarRow(label: String, value: Float, color: Color) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = Color.White,
      fontSize = 11.sp,
      modifier = Modifier.width(110.dp)
    )
    LinearProgressIndicator(
      progress = { value },
      modifier = Modifier
        .weight(1f)
        .height(7.dp)
        .clip(RoundedCornerShape(4.dp)),
      color = color,
      trackColor = DarkSurfaceVariant
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = "${(value * 100).toInt()}%",
      color = color,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.width(35.dp)
    )
  }
}
