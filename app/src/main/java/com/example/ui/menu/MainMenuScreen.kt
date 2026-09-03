package com.example.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.Faction
import com.example.model.GameScreen
import com.example.model.PlayerLevelSystem
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import com.example.ui.components.FactionSelectorPill
import com.example.ui.components.QamariyaDecorativePattern
import com.example.ui.sanaa7d.PlayerLevelProgressionDialog
import com.example.ui.theme.*

@Composable
fun MainMenuScreen(
  repository: SanaGameRepository,
  onNavigateTo: (GameScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  val selectedFaction by repository.selectedFaction.collectAsState()
  val topHighScores by repository.topHighScores.collectAsState(initial = emptyList())
  val isDevActive by repository.isDeveloperModeActive.collectAsState()
  val adminPlayerName by repository.adminPlayerName.collectAsState()
  val isOfflineVsComputer by repository.isOfflineVsComputerMode.collectAsState()
  var showLevelDialog by remember { mutableStateOf(false) }
  val playerLevelInfo = remember(stats.playerXp) {
    PlayerLevelSystem.getLevelInfo(stats.playerXp)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(10.dp))
      // Top Bar: Game Title & Coins
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "🇾🇪 GTA MAZENGALAB SANAA",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 20.sp
            )
          )
          Text(
            text = "أبطال أزقة صنعاء القديمة • حرب الشوارع والجولات",
            style = MaterialTheme.typography.bodySmall.copy(
              color = SanaaGold,
              fontSize = 11.5.sp
            )
          )
        }

        // Coins & Sound Buttons
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          var showDevDialog by remember { mutableStateOf(false) }

          Button(
            onClick = { showDevDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isDevActive) GangNeonGreen.copy(alpha = 0.25f) else DarkSurfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDevActive) GangNeonGreen else SanaaGold),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("btn_developer_mode")
          ) {
            Text(
              text = if (isDevActive) "👑 أدمن mazengalab" else "⚡ ميزة الأدمن",
              fontSize = 10.sp,
              color = if (isDevActive) GangNeonGreen else SanaaGold,
              fontWeight = FontWeight.Bold
            )
          }

          if (showDevDialog) {
            DeveloperLoginDialog(
              repository = repository,
              isCurrentlyActive = isDevActive,
              onDismiss = { showDevDialog = false }
            )
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Brush.horizontalGradient(listOf(SanaaMudWarm, SanaaClay)))
              .border(1.dp, SanaaGold, RoundedCornerShape(20.dp))
              .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("🪙", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isDevActive) "∞" else "${stats.totalCoins}",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          IconButton(
            onClick = {
              repository.toggleSound()
              GameSoundEffects.isMuted = !stats.soundEnabled
            },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(DarkSurfaceVariant)
          ) {
            Icon(
              imageVector = if (stats.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
              contentDescription = "الصوت",
              tint = if (stats.soundEnabled) SanaaGold else Color.Gray,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      QamariyaDecorativePattern()
    }

    // Default Mode Banner: Offline / VS Computer Play (عدم الارتباط بالويب وتفعيل اللعب مع الكمبيوتر)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.2.dp, if (isOfflineVsComputer) GangNeonGreen.copy(alpha = 0.8f) else DarkCardBorder, RoundedCornerShape(14.dp))
          .testTag("offline_computer_mode_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              color = if (isOfflineVsComputer) GangNeonGreen.copy(alpha = 0.2f) else DarkSurfaceVariant,
              shape = CircleShape,
              modifier = Modifier.size(38.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(if (isOfflineVsComputer) "🤖" else "🌐", fontSize = 18.sp)
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "وضع اللعب مع الكمبيوتر (Offline AI)",
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 12.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = GangNeonGreen,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "افتراضي 100%",
                    color = DarkBg,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
              }
              Text(
                text = "غير مرتبط بالويب • ذكاء اصطناعي محلي للشرطة والعصابات بدون إنترنت",
                color = if (isOfflineVsComputer) GangNeonGreen else Color.Gray,
                fontSize = 9.5.sp
              )
            }
          }

          Switch(
            checked = isOfflineVsComputer,
            onCheckedChange = { repository.toggleOfflineVsComputerMode() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = GangNeonGreen,
              checkedTrackColor = GangNeonGreen.copy(alpha = 0.3f),
              uncheckedThumbColor = Color.Gray,
              uncheckedTrackColor = DarkBg
            ),
            modifier = Modifier.testTag("switch_offline_computer_mode")
          )
        }
      }
    }

    // Free Stages Banner (الثلاث المراحل الأولى مجانية دخول بسيط)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("🆓", fontSize = 22.sp)
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "المراحل الثلاث الأولى مجانية (دخول بسيط)",
              color = Color(0xFF00E5FF),
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp
            )
            Text(
              text = "المرحلة 1 و 2 و 3 مفتوحة بالكامل بدون دفع أو شروط للدخول الفوري والسلس!",
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // Admin Player Quick Unlock Banner (اللاعب الأدمن mazengalab)
    if (!isDevActive) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, SanaaGold, RoundedCornerShape(14.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Text("👑", fontSize = 24.sp)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "دخول كمسؤول / أدمن (mazengalab)",
                  color = SanaaGold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "فتح تلقائي لكافة الصلاحيات والميزات والمراحل الخمس بدون حدود",
                  color = Color.LightGray,
                  fontSize = 9.5.sp
                )
              }
            }

            Button(
              onClick = {
                repository.activateAdminMazengalab()
              },
              colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("btn_fast_admin_mazengalab")
            ) {
              Text("تفعيل الأدمن ⚡", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    } else {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = GangNeonGreen.copy(alpha = 0.15f)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, GangNeonGreen, RoundedCornerShape(12.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("👑", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "حساب الأدمن النشط: mazengalab (كامل الصلاحيات)",
                color = GangNeonGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
              Text(
                text = "رصيد مفتوح • كافة المراحل مفتوحة • الترسانة والمتخصصين بالكامل",
                color = Color.White,
                fontSize = 9.5.sp
              )
            }
          }
        }
      }
    }

    // Player Level & XP Progression Strip (المستوى الحالي وشريط التقدم)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.2.dp, SanaaGold.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
          .clickable { showLevelDialog = true }
          .testTag("main_menu_player_level_card")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = SanaaGold,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${playerLevelInfo.badgeEmoji} Lv.${playerLevelInfo.currentLevel}",
                  color = DarkBg,
                  fontWeight = FontWeight.Black,
                  fontSize = 11.5.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = playerLevelInfo.currentTitleAr,
                  color = SanaaGold,
                  fontWeight = FontWeight.Black,
                  fontSize = 13.sp
                )
                Text(
                  text = if (playerLevelInfo.isMaxLevel) "👑 الرتبة القصوى" else "الخبرة: ${playerLevelInfo.currentLevelXp} / ${playerLevelInfo.xpRequiredForCurrentLevelSpan} XP",
                  color = Color.LightGray,
                  fontSize = 9.5.sp
                )
              }
            }

            Surface(
              color = GangNeonGreen.copy(alpha = 0.15f),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, GangNeonGreen),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("⚡ ${stats.playerXp} XP", color = GangNeonGreen, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Progress Bar
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            LinearProgressIndicator(
              progress = { playerLevelInfo.progressRatio },
              modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = GangNeonGreen,
              trackColor = Color(0xFF262626)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${(playerLevelInfo.progressRatio * 100).toInt()}%",
              color = GangNeonGreen,
              fontSize = 10.sp,
              fontWeight = FontWeight.Black
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "اضغط لعرض خارطة الرتب والجوائز 🏆",
              color = Color.Gray,
              fontSize = 8.5.sp
            )
            val nextRank = PlayerLevelSystem.ranks.find { it.level == playerLevelInfo.currentLevel + 1 }
            if (nextRank != null) {
              Text(
                text = "المستوى التالي: ${nextRank.titleAr} (+${nextRank.rewardCoinsOnReach}🪙)",
                color = SanaaGold.copy(alpha = 0.9f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Hero Visual Banner: Cloned Real Character & Sana'a Action Art
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(listOf(SanaaGold, GangShawlRed, PoliceAccent)),
            shape = RoundedCornerShape(20.dp)
          )
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Cinematic Hero Image Banner
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(145.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_sanaa_hero_action),
              contentDescription = "بطل شوارع صنعاء الحقيقي - مازن",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            // Gradient Overlay
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(
                      Color.Transparent,
                      DarkSurface.copy(alpha = 0.9f)
                    )
                  )
                )
            )
            // Top Badge
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                color = GangShawlRed,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "🔥 شخصية مستنسخة حقيقية",
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              Surface(
                color = DarkBg.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "🇾🇪 صنعاء القديمة 100%",
                  color = SanaaGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          // Content Details & Actions
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_hero_avatar),
                  contentDescription = "أفاتار البطل الحقيقي",
                  modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, SanaaGold, CircleShape),
                  contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = if (selectedFaction == Faction.GANG) "مازن (الزعيم الصغير الحقيقي) 👑" else "مفتش أمن العاصمة 👮‍♂️",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      fontSize = 15.sp
                    )
                  )
                  Text(
                    text = if (selectedFaction == Faction.GANG)
                      "«الشوارع شوارعنا وأسطح صنعاء لعبتنا!»"
                    else
                      "«تطويق أمني ذكي لحفظ أمان حارات صنعاء!»",
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = SanaaGold,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Quick Play Button (GTA Sanaa 7D)
            Button(
              onClick = {
                GameSoundEffects.playNitroBoost()
                onNavigateTo(GameScreen.GTA_SANAA_7D)
              },
              colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_quick_action_gta_sanaa_7d")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DarkBg)
              Spacer(modifier = Modifier.width(6.dp))
              Text("العب الآن: مطاردة أزقة صنعاء 7D (GTA Sana'a) 🇾🇪🏃‍♂️", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Faction Switcher Pill
            Text(
              text = "اختر الجانب الذي تريد اللعب به:",
              color = Color.LightGray,
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FactionSelectorPill(
              selectedFaction = selectedFaction,
              onSelectFaction = { repository.setFaction(it) }
            )
          }
        }
      }
    }

    // Section Header: Game Modes
    item {
      Text(
        text = "أطوار ومهمات اللعبة 🎮",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontSize = 16.sp
        )
      )
    }

    // Featured Hero Mode #1: GTA SANAA 7D (Alleys, Mud Towers, Wanted Stars)
    item {
      GameModeCard(
        title = "🇾🇪 مطاردة أزقة صنعاء 7D (GTA Sana'a 7D)",
        subtitle = "محاكاة 7D فائقة الدقة! زقازيق باب اليمن، قفز أسطح الطين ونوافذ القمريات، خطف الدباب الصنعاني، ومطاردة العقيد ناصر 5 نجوم",
        badge = "🌟 عرض 7D فائق الدقة (الأساسي)",
        badgeColor = SanaaGold,
        accentColor = SanaaGold,
        onClick = {
          GameSoundEffects.playNitroBoost()
          onNavigateTo(GameScreen.GTA_SANAA_7D)
        },
        testTag = "mode_gta_sanaa_7d"
      )
    }

    // Featured Hero Mode #2: GTA BEIRUT 3D
    item {
      GameModeCard(
        title = "🇱🇧 حرامي سيارات بيروت 3D (GTA Beirut 3D)",
        subtitle = "عالم مفتوح ثلاثي الأبعاد بالمنظور الثالث! كورنيش الروشة، شارع الحمرا، سرقة مرسيدس سرفيس وفان 4، ومطاردة قوى الأمن الداخلي 5 نجوم",
        badge = "🔥 طور ثلاثي الأبعاد 3D",
        badgeColor = Color(0xFF00E5FF),
        accentColor = Color(0xFF00E5FF),
        onClick = {
          GameSoundEffects.playNitroBoost()
          onNavigateTo(GameScreen.GTA_BEIRUT_3D)
        },
        testTag = "mode_gta_beirut_3d"
      )
    }

    // Featured Primary Mode: Turn-Based Tactical Grid Battle (XCOM / Commandos Style)
    item {
      GameModeCard(
        title = "⚔️ التكتيك الاستراتيجي وتبادل الأدوار",
        subtitle = "حرب المربعات (2 AP)، نظام الاحتماء 100%، خطف الضباط تكتيكياً، اقتحام الدباب، والمتجر السري للمصروف",
        badge = "الطور التكتيكي الرئيسي (XCOM)",
        badgeColor = SanaaGold,
        accentColor = SanaaGold,
        onClick = {
          GameSoundEffects.playWalkieTalkie()
          onNavigateTo(GameScreen.TACTICAL_XCOM)
        },
        testTag = "mode_tactical_xcom"
      )
    }

    // Mode 1: Parkour & Alley Chase
    item {
      GameModeCard(
        title = "🏃‍♂️ مطاردة أزقة صنعاء والأسطح",
        subtitle = "باركور وقفز بين القمريات، قشور الموز، علب الغرافيتي، والمفرقعات",
        badge = "طور الأكشن السريع",
        badgeColor = GangShawlRed,
        accentColor = SanaaGold,
        onClick = {
          GameSoundEffects.playJump()
          onNavigateTo(GameScreen.CHASE_GAME)
        },
        testTag = "mode_chase_game"
      )
    }

    // Mode 2: Vehicle Heist & Drifting (GTA Sana'a)
    item {
      GameModeCard(
        title = "🚗 حرامي سيارات صنعاء (GTA Sana'a)",
        subtitle = "عالم مفتوح، سرقة وخطف المركبات والدبابات، تفحيط وهجولة، مطاردة 5 نجوم مع ورشة الدهان",
        badge = "أكشن حرامي السيارات (GTA)",
        badgeColor = TaxiYellow,
        accentColor = TaxiYellow,
        onClick = {
          GameSoundEffects.playNitroBoost()
          onNavigateTo(GameScreen.VEHICLE_HEIST)
        },
        testTag = "mode_vehicle_heist"
      )
    }

    // Mode 3: Secret Mud Tower Tactics & Hostage Rescue
    item {
      GameModeCard(
        title = "🏰 تكتيك البيت الطيني المهجور",
        subtitle = "إدارة المقر السري، فخاخ الطوابق، مفاوضات اللاسلكي، واقتحام الرهائن",
        badge = "طور التكتيك والإستراتيجية",
        badgeColor = PoliceAccent,
        accentColor = PoliceAccent,
        onClick = {
          GameSoundEffects.playWalkieTalkie()
          onNavigateTo(GameScreen.HIDEOUT_TACTICS)
        },
        testTag = "mode_hideout_tactics"
      )
    }

    // Mode 4 & 5: Lore Dossier & HQ Shop
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Character Dossier
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .weight(1f)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .clickable {
              GameSoundEffects.playJump()
              onNavigateTo(GameScreen.CHARACTER_DOSSIER)
            }
            .testTag("btn_character_dossier")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "📖", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "ملفات الأبطال",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
              )
            )
            Text(
              text = "23 حدثاً وقصة موثقة",
              color = Color.LightGray,
              fontSize = 11.sp
            )
          }
        }

        // HQ Upgrades Shop
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .weight(1f)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .clickable {
              GameSoundEffects.playCoin()
              onNavigateTo(GameScreen.HQ_UPGRADES)
            }
            .testTag("btn_hq_upgrades")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "⚡", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "ترقيات المقر",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SanaaGold,
                fontSize = 14.sp
              )
            )
            Text(
              text = "تطوير المهارات والعتاد",
              color = Color.LightGray,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // Top Players / High Scores Card (Room Database Persistence)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.2.dp, SanaaGold.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
          .testTag("main_menu_top_players_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("🏆", fontSize = 20.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "قائمة أفضل اللاعبين (Top Players)",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SanaaGold,
                    fontSize = 15.sp
                  )
                )
                Text(
                  text = "الأرقام القياسية المسجلة في قاعدة بيانات Room المحلية",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.LightGray,
                    fontSize = 10.5.sp
                  )
                )
              }
            }

            Surface(
              color = SanaaGold.copy(alpha = 0.2f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, SanaaGold)
            ) {
              Text(
                text = "${topHighScores.size} لاعبين",
                color = SanaaGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (topHighScores.isEmpty()) {
            Surface(
              color = DarkSurfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text("🎮 ابدأ المطاردة وسجل رقمك القياسي الآن!", color = Color.Gray, fontSize = 12.sp)
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              topHighScores.take(5).forEachIndexed { index, scoreEntity ->
                val medalEmoji = when (index) {
                  0 -> "🥇"
                  1 -> "🥈"
                  2 -> "🥉"
                  else -> "#${index + 1}"
                }
                Surface(
                  color = if (index == 0) SanaaGold.copy(alpha = 0.12f) else DarkSurfaceVariant.copy(alpha = 0.6f),
                  shape = RoundedCornerShape(10.dp),
                  border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    if (index == 0) SanaaGold else DarkCardBorder
                  ),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(medalEmoji, fontSize = if (index < 3) 16.sp else 12.sp, fontWeight = FontWeight.Bold, color = SanaaGold)
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(
                          text = scoreEntity.playerName,
                          color = Color.White,
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp
                        )
                        Text(
                          text = "${scoreEntity.titleAr} • ${scoreEntity.difficulty}",
                          color = Color.LightGray,
                          fontSize = 10.sp
                        )
                      }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                      Text(
                        text = "${scoreEntity.score} نقطة",
                        color = SanaaGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                      )
                      Text(
                        text = "+${scoreEntity.coinsEarned} 🪙",
                        color = GangNeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = {
              GameSoundEffects.playCoin()
              HapticManager.vibrateMovement()
              onNavigateTo(GameScreen.LEADERBOARD)
            },
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = SanaaGold
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, SanaaGold.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_view_full_top10_leaderboard")
          ) {
            Text("🏆", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "عرض لوحة الشرف: أفضل 10 لاعبين وأوقات المطاردة",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showLevelDialog) {
    PlayerLevelProgressionDialog(
      stats = stats,
      onDismiss = { showLevelDialog = false }
    )
  }
}

@Composable
fun GameModeCard(
  title: String,
  subtitle: String,
  badge: String,
  badgeColor: Color,
  accentColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    shape = RoundedCornerShape(18.dp),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, DarkCardBorder, RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
      .testTag(testTag)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 16.sp
          )
        )

        AssistChip(
          onClick = onClick,
          label = { Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
          colors = AssistChipDefaults.assistChipColors(
            containerColor = badgeColor.copy(alpha = 0.25f),
            labelColor = if (badgeColor == TaxiYellow) TaxiYellow else Color.White
          ),
          border = null
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = Color.LightGray,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "دخول المهمة ◀",
          style = MaterialTheme.typography.labelMedium.copy(
            color = accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        )
      }
    }
  }
}

@Composable
fun DeveloperLoginDialog(
  repository: com.example.data.SanaGameRepository,
  isCurrentlyActive: Boolean,
  onDismiss: () -> Unit
) {
  var emailText by remember { mutableStateOf("mazengalab") }
  var passwordText by remember { mutableStateOf("mazengalab") }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var successMsg by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("👑", fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "حساب الأدمن والمزايا الشاملة (mazengalab)",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = SanaaGold,
            fontSize = 15.sp
          )
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        if (isCurrentlyActive) {
          Card(
            colors = CardDefaults.cardColors(containerColor = GangNeonGreen.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("🔥 ميزة الأدمن (mazengalab) نشطة بالكامل!", color = GangNeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("• رصيد غير محدود: 99,999,999 ريال يمني\n• جميع الأسلحة والأدوات مفتوحة بالكامل\n• جميع المتخصصين مجندين وجاهزين\n• جميع المراحل الخمس مفتوحة بدون قيود", color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
            }
          }
        } else {
          Text(
            text = "تسجيل الدخول كأدمن ومسؤول النظام (mazengalab) لفتح كافة الصلاحيات والمراحل بدون حدود:",
            color = Color.LightGray,
            fontSize = 11.5.sp,
            lineHeight = 16.sp
          )

          Button(
            onClick = {
              repository.activateAdminMazengalab()
              successMsg = "تم تفعيل حساب الأدمن mazengalab بنجاح! رصيد مفتوح وكافة الميزات والمراحل متاحة!"
              errorMsg = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("⚡ تفعيل سريع بنقرة واحدة كـ (mazengalab)", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          OutlinedTextField(
            value = emailText,
            onValueChange = { emailText = it },
            label = { Text("اسم المستخدم / البريد") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_dev_email"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SanaaGold,
              unfocusedBorderColor = DarkCardBorder,
              focusedLabelColor = SanaaGold
            )
          )

          OutlinedTextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            label = { Text("كلمة المرور") },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("input_dev_password"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = SanaaGold,
              unfocusedBorderColor = DarkCardBorder,
              focusedLabelColor = SanaaGold
            )
          )

          if (errorMsg != null) {
            Text(errorMsg!!, color = PoliceRedLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          if (successMsg != null) {
            Text(successMsg!!, color = GangNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      if (!isCurrentlyActive) {
        Button(
          onClick = {
            val success = repository.activateDeveloperAccount(emailText, passwordText)
            if (success) {
              com.example.sound.GameSoundEffects.playVictoryFanfare()
              successMsg = "تم تفعيل حساب الأدمن (mazengalab) بنجاح! رصيد غير محدود وفتح كافة الترسانة والمراحل!"
              errorMsg = null
            } else {
              errorMsg = "بيانات الاعتماد غير صحيحة! يرجى التحقق من اسم المستخدم وكلمة المرور."
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_confirm_dev_login")
        ) {
          Text("تأكيد الدخول كأدمن ⚡", color = DarkBg, fontWeight = FontWeight.Bold)
        }
      } else {
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = GangNeonGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("إغلاق", color = DarkBg, fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      if (!isCurrentlyActive) {
        TextButton(onClick = onDismiss) {
          Text("إلغاء", color = Color.Gray)
        }
      }
    },
    containerColor = DarkSurface,
    shape = RoundedCornerShape(16.dp)
  )
}

