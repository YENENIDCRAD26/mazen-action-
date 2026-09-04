package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Faction
import com.example.model.GameScreen
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import com.example.ui.sanaa7d.Sanaa7DStage
import com.example.ui.theme.*
import com.example.ui.viewmodel.DEFAULT_PLAYABLE_CHARACTERS
import com.example.ui.viewmodel.PlayableCharacter
import com.example.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(
  userViewModel: UserViewModel,
  onStartGame: (selectedStageIndex: Int) -> Unit,
  onNavigateTo: (GameScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  val username by userViewModel.username.collectAsState()
  val isAdmin by userViewModel.isAdmin.collectAsState()
  val selectedCharacter by userViewModel.selectedCharacter.collectAsState()
  val selectedLevel by userViewModel.selectedLevel.collectAsState()
  val unlockedLevels by userViewModel.unlockedLevels.collectAsState()
  val isNetworkAvailable by userViewModel.isNetworkAvailable.collectAsState()
  val isOfflineMode by userViewModel.isOfflineMode.collectAsState()
  val statusMessage by userViewModel.statusMessage.collectAsState()

  var showSettingsDialog by remember { mutableStateOf(false) }
  var showStageSelectionModal by remember { mutableStateOf(false) }

  val currentStage = remember(selectedLevel) {
    Sanaa7DStage.values().find { it.id == selectedLevel } ?: Sanaa7DStage.values().first()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "GTA MAZENGALAB SANAA",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = SanaaGold,
                fontSize = 18.sp
              )
            )
            Text(
              text = "أبطال أزقة صنعاء القديمة",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.LightGray.copy(alpha = 0.8f),
                fontSize = 11.sp
              )
            )
          }
        },
        actions = {
          if (isAdmin) {
            Surface(
              color = GangNeonGreen.copy(alpha = 0.2f),
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, GangNeonGreen),
              modifier = Modifier
                .padding(end = 6.dp)
                .clickable { showSettingsDialog = true }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text("👑", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "أدمن",
                  color = GangNeonGreen,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.5.sp
                )
              }
            }
          }

          // Settings / Offline AI gear icon button
          IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier.testTag("btn_settings_dialog")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "الإعدادات ووضع عدم الاتصال",
              tint = SanaaGold
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = DarkSurface
        )
      )
    },
    bottomBar = {
      // Clean, Broad & Vibrant START Button at Bottom
      Surface(
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkCardBorder),
        shadowElevation = 12.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
          Button(
            onClick = {
              HapticManager.vibrateSuccess()
              GameSoundEffects.playNitroBoost()
              onStartGame(selectedLevel)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = SanaaGold,
              contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(58.dp)
              .testTag("start_game_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "بدء اللعبة",
                modifier = Modifier.size(28.dp),
                tint = Color.Black
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "ابدأ المطاردة الآن (START)",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp,
                  color = Color.Black
                )
              )
            }
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkBg)
        .padding(innerPadding)
        .padding(horizontal = 18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {

      // Status Toast Banner (if any)
      statusMessage?.let { msg ->
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isAdmin) GangNeonGreen else SanaaGold),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = msg,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = { userViewModel.clearStatusMessage() },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.LightGray, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // 1. CHARACTER SELECTION: Large Circular Cartoon Avatars
      item {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "اختر بطل المطاردة",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Circular Avatars Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            DEFAULT_PLAYABLE_CHARACTERS.forEach { character ->
              val isSelected = selectedCharacter.id == character.id
              val avatarDrawableRes = when (character.id) {
                "mazen_leader" -> R.drawable.sanaa_kid_leader
                "faris_parkour" -> R.drawable.img_hero_avatar
                "ammar_driver" -> R.drawable.gta_sanaa_7d_chase
                "salem_sniper" -> R.drawable.img_sanaa_hero_action
                else -> null
              }

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clickable {
                    HapticManager.vibrateClick()
                    userViewModel.selectCharacter(character)
                  }
                  .padding(4.dp)
                  .testTag("character_card_${character.id}")
              ) {
                // Large Circular Avatar
                Box(
                  modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(
                      width = if (isSelected) 3.5.dp else 1.5.dp,
                      color = if (isSelected) {
                        if (character.faction == Faction.GANG) SanaaGold else PoliceBlue
                      } else {
                        Color.White.copy(alpha = 0.2f)
                      },
                      shape = CircleShape
                    )
                    .background(DarkSurfaceVariant),
                  contentAlignment = Alignment.Center
                ) {
                  if (avatarDrawableRes != null) {
                    Image(
                      painter = painterResource(id = avatarDrawableRes),
                      contentDescription = character.nameAr,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                  } else {
                    Text(text = character.avatarEmoji, fontSize = 42.sp)
                  }

                  // Selected Glowing Indicator
                  if (isSelected) {
                    Box(
                      modifier = Modifier
                        .fillMaxSize()
                        .background(
                          Brush.radialGradient(
                            colors = listOf(
                              Color.Transparent,
                              (if (character.faction == Faction.GANG) SanaaGold else PoliceBlue).copy(alpha = 0.35f)
                            )
                          )
                        )
                    )
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Character Name Label
                Text(
                  text = character.nameAr.split(" ").firstOrNull() ?: character.nameAr,
                  color = if (isSelected) SanaaGold else Color.White.copy(alpha = 0.8f),
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                  fontSize = 13.sp
                )

                val heroRoleTag = when (character.id) {
                  "mazen_leader" -> "الزعيم 👑"
                  "faris_parkour" -> "الباركور 🧗‍♂️"
                  "ammar_driver" -> "الهجولة 🚐"
                  "salem_sniper" -> "التمويه 🎒"
                  else -> "بطل 🌟"
                }
                Text(
                  text = heroRoleTag,
                  color = SanaaGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Compact, Sleek Mini-Stats Bar for Selected Character
          Surface(
            color = Color(0xFF161E2E),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              MiniStatItem(icon = "⚡", label = "السرعة", value = selectedCharacter.speedStat, color = Color(0xFFFFD54F))
              VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.15f))
              MiniStatItem(icon = "🥷", label = "التسلل", value = selectedCharacter.stealthStat, color = Color(0xFF81C784))
              VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(alpha = 0.15f))
              MiniStatItem(icon = "🥊", label = "القتال", value = selectedCharacter.combatStat, color = Color(0xFFE57373))
            }
          }
        }
      }

      // 2. POP-UP LEVEL SELECTION: Single Prominent Clean Card
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "المرحلة الحالية",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            color = Color(0xFF161E2E),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.5.dp, SanaaGold.copy(alpha = 0.7f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                HapticManager.vibrateClick()
                showStageSelectionModal = true
              }
              .testTag("btn_select_stage_popup")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Surface(
                  color = SanaaGold.copy(alpha = 0.15f),
                  shape = CircleShape,
                  modifier = Modifier.size(46.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text("🗺️", fontSize = 24.sp)
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "المرحلة ${currentStage.id}: ${currentStage.titleAr}",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = "المكافأة: +${currentStage.rewardCoins} 🪙 ريال",
                    color = SanaaGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              Surface(
                color = SanaaGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text(
                  text = "اختر المرحلة ▾",
                  color = SanaaGold,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      // 3. Quick Tactical & HQ Shortcuts
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = { onNavigateTo(GameScreen.TACTICAL_XCOM) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(46.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
          ) {
            Text("⚔️ استراتيجية الأزقة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = { onNavigateTo(GameScreen.HQ_UPGRADES) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(46.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
          ) {
            Text("🏛️ ترقيات المقر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // ----------------------------------------------------
  // Pop-up Dialog for Stage Selection (نافذة منبثقة للمراحل)
  // ----------------------------------------------------
  if (showStageSelectionModal) {
    AlertDialog(
      onDismissRequest = { showStageSelectionModal = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("🗺️", fontSize = 22.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "اختر مرحلة المطاردة",
            color = SanaaGold,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Sanaa7DStage.values().forEach { stage ->
            val isSelected = selectedLevel == stage.id
            val isFreeDefault = stage.id <= 3
            val isUnlocked = isFreeDefault || isAdmin || unlockedLevels.contains(stage.id)

            Surface(
              color = if (isSelected) SanaaGold.copy(alpha = 0.2f) else if (isUnlocked) Color(0xFF1E2638) else Color(0xFF121620),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) SanaaGold else if (isUnlocked) DarkCardBorder else Color.DarkGray
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isUnlocked) {
                  HapticManager.vibrateClick()
                  userViewModel.selectLevel(stage.id)
                  showStageSelectionModal = false
                }
                .testTag("stage_item_${stage.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Surface(
                    color = if (isUnlocked) SanaaGold.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = if (isUnlocked) "${stage.id}" else "🔒",
                        color = if (isUnlocked) SanaaGold else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = stage.titleAr,
                        color = if (isUnlocked) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                      )
                      if (isFreeDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🆓", fontSize = 10.sp)
                      }
                    }
                    Text(
                      text = "+${stage.rewardCoins} 🪙 ريال",
                      color = if (isUnlocked) SanaaGold else Color.Gray,
                      fontSize = 10.5.sp
                    )
                  }
                }

                if (isSelected) {
                  Text("✓ محددة", color = SanaaGold, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showStageSelectionModal = false }) {
          Text("إغلاق", color = SanaaGold, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface,
      shape = RoundedCornerShape(20.dp)
    )
  }

  // ----------------------------------------------------
  // Settings Dialog (Offline AI Mode & Admin Management)
  // ----------------------------------------------------
  if (showSettingsDialog) {
    var inputName by remember { mutableStateOf(username.ifEmpty { "mazengalab" }) }

    AlertDialog(
      onDismissRequest = { showSettingsDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("⚙️", fontSize = 22.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "الإعدادات والنظام",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          // Offline AI Mode Toggle
          Surface(
            color = Color(0xFF161E2E),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isOfflineMode) GangNeonGreen.copy(alpha = 0.5f) else DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "وضع عدم الاتصال (Offline AI)",
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 12.sp
                )
                Text(
                  text = "تشغيل الذكاء الاصطناعي محلياً بدون خادم",
                  color = if (isOfflineMode) GangNeonGreen else Color.LightGray,
                  fontSize = 10.sp
                )
              }
              Switch(
                checked = isOfflineMode,
                onCheckedChange = { userViewModel.toggleOfflineMode() },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = GangNeonGreen,
                  checkedTrackColor = GangNeonGreen.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("switch_offline_mode")
              )
            }
          }

          // Admin Login (mazengalab)
          Surface(
            color = Color(0xFF161E2E),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, SanaaGold.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "حساب الأدمن (mazengalab)",
                color = SanaaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
              OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                label = { Text("اسم المستخدم", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_admin_username"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = SanaaGold,
                  unfocusedBorderColor = DarkCardBorder,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White
                )
              )
              Button(
                onClick = {
                  userViewModel.setUsernameAndCheckAdmin(inputName)
                  showSettingsDialog = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_submit_admin_check")
              ) {
                Text("تفعيل صلاحيات الأدمن 👑", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
              }
              if (isAdmin) {
                OutlinedButton(
                  onClick = {
                    userViewModel.toggleAdmin()
                    showSettingsDialog = false
                  },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("تعطيل وضع الأدمن", color = Color.Red, fontSize = 11.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSettingsDialog = false }) {
          Text("تم", color = SanaaGold, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface,
      shape = RoundedCornerShape(18.dp)
    )
  }
}

@Composable
private fun MiniStatItem(icon: String, label: String, value: Int, color: Color) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(icon, fontSize = 13.sp)
    Text(label, color = Color.LightGray, fontSize = 11.sp)
    Text("$value%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
  }
}


