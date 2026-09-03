package com.example.ui.tactical

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SanaGameRepository
import com.example.model.*
import com.example.sound.GameSoundEffects
import com.example.ui.components.SanaaTopBar
import com.example.ui.theme.*
import kotlin.math.abs

enum class TurnState {
  KIDS_TURN,
  POLICE_TURN,
  VICTORY,
  DEFEAT
}

@Composable
fun TacticalStrategyScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val viewModel = remember { TacticalBattleViewModel(repository) }
  val uiState by viewModel.uiState.collectAsState()

  val stats by repository.stats.collectAsState()
  val ownedItems by repository.ownedArmoryItemIds.collectAsState()
  val hiredSpecialists by repository.hiredSpecialistIds.collectAsState()
  val completedStages by repository.completedStageIndexes.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Battle, 1: Gang Store, 2: Specialists

  Scaffold(
    topBar = {
      SanaaTopBar(
        title = "المعارك التكتيكية (XCOM & Commandos)",
        subtitle = "حرب المربعات (2 AP) • خط الرؤية • المتجر السري",
        coins = stats.totalCoins,
        soundEnabled = stats.soundEnabled,
        onSoundToggle = { repository.toggleSound() },
        onBackClick = onNavigateBack
      )
    },
    containerColor = DarkBg,
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Navigation Tabs
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = DarkSurface,
        contentColor = SanaaGold,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("ساحة المعركة ⚔️", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("متجر العصابة 🛒", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("تجنيد المتخصصين 👑", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        )
      }

      when (selectedTab) {
        0 -> {
          TacticalBattleContent(
            uiState = uiState,
            viewModel = viewModel,
            completedStages = completedStages,
            ownedItems = ownedItems,
            onRestartStage = { viewModel.loadStage(uiState.activeStageIndex) }
          )
        }
        1 -> {
          GangStoreView(
            repository = repository,
            modifier = Modifier.weight(1f)
          )
        }
        2 -> {
          SpecialistRecruitmentView(
            repository = repository,
            hiredSpecialists = hiredSpecialists,
            playerCoins = stats.totalCoins
          )
        }
      }
    }
  }
}

@Composable
fun TacticalBattleContent(
  uiState: TacticalBattleUiState,
  viewModel: TacticalBattleViewModel,
  completedStages: Set<Int>,
  ownedItems: Set<String>,
  onRestartStage: () -> Unit
) {
  val selectedUnit = uiState.units.find { it.id == uiState.selectedUnitId }

  // Wobble animation for slipping units
  val infiniteTransition = rememberInfiniteTransition(label = "slip_shake")
  val shakeOffset by infiniteTransition.animateFloat(
    initialValue = -6f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(
      animation = tween(80, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shake_offset"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Stage Selector Carousel
    item {
      Text(
        text = "اختر المرحلة التكتيكية:",
        color = Color.LightGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(GameData.tacticalStages) { stage ->
          val isSelected = stage.stageIndex == uiState.activeStageIndex
          val isCompleted = completedStages.contains(stage.stageIndex)

          Card(
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .width(180.dp)
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) SanaaGold else if (isCompleted) GangNeonGreen else DarkCardBorder,
                shape = RoundedCornerShape(12.dp)
              )
              .clickable { viewModel.loadStage(stage.stageIndex) }
              .testTag("stage_card_${stage.stageIndex}")
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(stage.iconEmoji, fontSize = 20.sp)
                if (stage.stageIndex <= 3) {
                  Text("مجانية 🆓", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else if (isCompleted) {
                  Text("مكتملة ✓", color = GangNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else if (stage.isTutorial) {
                  Text("تعليمي 🎓", color = TaxiYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = stage.titleAr,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 12.sp
                ),
                maxLines = 1
              )
              Text(
                text = "+${stage.rewardCoins} ريال",
                color = SanaaGold,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // Map Zone & Location Header Badge
    uiState.mapGrid?.let { grid ->
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(grid.primaryZone.iconEmoji, fontSize = 18.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = grid.locationNameAr,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                  )
                )
                Text(
                  text = grid.primaryZone.titleAr,
                  color = SanaaGold,
                  fontSize = 10.sp
                )
              }
            }

            // Line of sight stealth status pill
            val hiddenCount = uiState.hiddenKidIds.size
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (hiddenCount > 0) GangNeonGreen.copy(alpha = 0.15f) else PoliceRedLight.copy(alpha = 0.15f))
                .border(1.dp, if (hiddenCount > 0) GangNeonGreen else PoliceAccent, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(if (hiddenCount > 0) "🙈 سواتر نشطة: $hiddenCount" else "👁️ مكشوف بالكامل", fontSize = 10.sp, color = if (hiddenCount > 0) GangNeonGreen else PoliceRedLight, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Tutorial Step-by-Step Banner (If Stage 1 Active)
    if (uiState.activeStageIndex == 1) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, TaxiYellow, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("🎓", fontSize = 18.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "البرنامج التعليمي خطوة بخطوة (الخطوة ${uiState.tutorialStep} من 4)",
                color = TaxiYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val stepDesc = when (uiState.tutorialStep) {
              1 -> "الخطوة 1: تعلم الحركة والاحتماء — اضغط على الزعيم الصغير (👑)، ثم انقر على المربع (1, 2) بجوار عربة البطاطس الحارة (🥔) للاحتماء بنسبة 100% وحجب خط الرؤية."
              2 -> "الخطوة 2: ميكانيكية التسلل — انتظر التفات الشرطي، ثم اختر الطفل المساعد (🎒) وحركه نحو المربع المحاذي لباب الدباب (🚐)."
              3 -> "الخطوة 3: خطف المركبة — استهلك (1 AP) للطفل المساعد واضغط على زر 'اقتحام المركبة 🚐' لتشغيل الدباب وإرباك الشرطة."
              4 -> "الخطوة 4: الهروب الكبير — اركب الدباب واستخدم نقاط العمل (2 AP) للتحرك بسرعة نحو المربع المضيء بنهاية الحارة (🏁 نقطة الهروب)."
              else -> "أحسنت! أنت جاهز للمعارك الكبرى في صنعاء!"
            }
            Text(
              text = stepDesc,
              color = Color.White,
              fontSize = 12.sp,
              lineHeight = 17.sp
            )
          }
        }
      }
    }

    // Turn & Morale Status Header
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Turn Indicator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (uiState.currentTurn == TurnState.KIDS_TURN) GangNeonGreen else PoliceRedLight)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (uiState.currentTurn == TurnState.KIDS_TURN) "دور عصابة الأطفال (الجولة ${uiState.roundNumber}) 🎒" else "دور شرطة العاصمة 🚓",
              color = if (uiState.currentTurn == TurnState.KIDS_TURN) SanaaGold else PoliceAccent,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          // Morale vs Readiness
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("معنويات: ${uiState.kidMorale}%", color = GangNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("جاهزية: ${uiState.policeReadiness}%", color = PoliceAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Interactive Tactical Grid Battlefield (6x6)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(DarkBg)
          .border(1.5.dp, SanaaMudWarm, RoundedCornerShape(16.dp))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          for (r in 0 until viewModel.gridRows) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              for (c in 0 until viewModel.gridCols) {
                val tile = uiState.tiles.find { it.x == c && it.y == r }
                val unitOnTile = uiState.units.find { it.x == c && it.y == r && it.hp > 0 }
                val isSelectedUnitTile = selectedUnit?.x == c && selectedUnit?.y == r

                val isReachable = selectedUnit != null &&
                  selectedUnit.ap > 0 &&
                  (abs(selectedUnit.x - c) + abs(selectedUnit.y - r) <= selectedUnit.ap) &&
                  (unitOnTile == null || unitOnTile.faction != selectedUnit.faction)

                val isSlipping = unitOnTile != null && (unitOnTile.id == uiState.slippingUnitId || (unitOnTile.faction == Faction.POLICE && unitOnTile.isStunned))

                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .graphicsLayer {
                      if (isSlipping && uiState.isShaking) {
                        translationX = shakeOffset
                        rotationZ = shakeOffset * 1.5f
                      }
                    }
                    .background(
                      when {
                        isSelectedUnitTile -> SanaaGold.copy(alpha = 0.35f)
                        isReachable -> SanaaQamariyaCyan.copy(alpha = 0.25f)
                        tile?.tileType == TacticalTileType.EXIT_ZONE -> GangNeonGreen.copy(alpha = 0.3f)
                        tile?.tileType == TacticalTileType.POLICE_HQ_FLAG -> GangShawlRed.copy(alpha = 0.3f)
                        tile?.tileType == TacticalTileType.MUD_ROOF -> SanaaClay.copy(alpha = 0.4f)
                        tile?.tileType?.cover == TacticalCoverType.FULL -> DarkSurface
                        else -> DarkSurfaceVariant
                      }
                    )
                    .border(
                      width = if (isSelectedUnitTile) 2.dp else if (isReachable) 1.dp else 0.5.dp,
                      color = if (isSelectedUnitTile) SanaaGold else if (isReachable) SanaaQamariyaCyan else DarkCardBorder,
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      if (unitOnTile != null && unitOnTile.faction == Faction.GANG) {
                        viewModel.selectUnit(unitOnTile.id)
                        GameSoundEffects.playJump()
                      } else if (isReachable && unitOnTile == null) {
                        viewModel.moveUnit(c, r)
                      }
                    }
                    .testTag("tactical_tile_${c}_$r"),
                  contentAlignment = Alignment.Center
                ) {
                  // Render Terrain Icon
                  if (tile != null && tile.tileType != TacticalTileType.ALLEY_ROAD && tile.tileType != TacticalTileType.OPEN_ASPHALT_ROAD && unitOnTile == null) {
                    Text(text = tile.tileType.iconEmoji, fontSize = 18.sp)
                  }

                  // Render Fruit Peel Trap
                  if (tile?.hasBananaTrap == true && unitOnTile == null) {
                    Text("🍌", fontSize = 14.sp)
                  }

                  // Render Unit on tile
                  if (unitOnTile != null) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      // Slip Icon Floating overlay
                      if (isSlipping) {
                        Text(
                          text = "🍌💫",
                          fontSize = 11.sp,
                          modifier = Modifier.graphicsLayer { translationY = -2f }
                        )
                      }

                      // Unit Main Emoji
                      Text(
                        text = if (unitOnTile.isHostage) "🪢" else unitOnTile.iconEmoji,
                        fontSize = 18.sp
                      )

                      // Line of Sight & Cover Status Tag for Kids
                      if (unitOnTile.faction == Faction.GANG) {
                        val isSpotted = uiState.detectedKidIds.contains(unitOnTile.id)
                        Text(
                          text = if (isSpotted) "👁️" else "🙈",
                          fontSize = 8.sp
                        )
                      }

                      // AP dots
                      Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        repeat(unitOnTile.ap) {
                          Box(
                            modifier = Modifier
                              .size(4.dp)
                              .clip(CircleShape)
                              .background(if (unitOnTile.faction == Faction.GANG) GangNeonGreen else PoliceAccent)
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Selected Unit Command Action Bar
    if (selectedUnit != null) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SanaaGold, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // Unit Info Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedUnit.iconEmoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = selectedUnit.nameAr,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                      )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val isHidden = uiState.hiddenKidIds.contains(selectedUnit.id)
                    Text(
                      text = if (isHidden) "🛡️ متخفٍ خلف ساتر" else "⚠️ مكشوف لخط الرؤية",
                      color = if (isHidden) GangNeonGreen else PoliceRedLight,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Text(
                    text = "نقاط العمل المتبقية: ${selectedUnit.ap}/${selectedUnit.maxAp} AP ⚡",
                    color = SanaaGold,
                    fontSize = 11.sp
                  )
                }
              }

              // Health Bar
              Column(horizontalAlignment = Alignment.End) {
                Text("الصحة: ${selectedUnit.hp}%", color = GangNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                  progress = { selectedUnit.hp / 100f },
                  modifier = Modifier
                    .width(70.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = GangNeonGreen,
                  trackColor = DarkSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Deducting AP accordingly)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Attack with Slingshot
              val nearbyCop = uiState.units.find { it.faction == Faction.POLICE && !it.isHostage && abs(it.x - selectedUnit.x) + abs(it.y - selectedUnit.y) <= 3 }
              Button(
                onClick = {
                  if (nearbyCop != null) {
                    viewModel.shootPolice(nearbyCop.id, isBbGun = false)
                  }
                },
                enabled = selectedUnit.ap >= 1 && nearbyCop != null && uiState.currentTurn == TurnState.KIDS_TURN,
                colors = ButtonDefaults.buttonColors(containerColor = SanaaClay),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("رمية المقلاع 🪨 (1 AP)", fontSize = 11.sp)
              }

              // Modified BB Gun Snipe
              Button(
                onClick = {
                  val targetCop = uiState.units.find { it.faction == Faction.POLICE && !it.isHostage && abs(it.x - selectedUnit.x) + abs(it.y - selectedUnit.y) <= 4 }
                  if (targetCop != null) {
                    viewModel.shootPolice(targetCop.id, isBbGun = true)
                  }
                },
                enabled = selectedUnit.ap >= 2 && uiState.currentTurn == TurnState.KIDS_TURN && ownedItems.contains("bb_gun"),
                colors = ButtonDefaults.buttonColors(containerColor = SanaaQamariyaCyan),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("مسدس خرز معدل 🔫 (2 AP)", fontSize = 11.sp, color = DarkBg, fontWeight = FontWeight.Bold)
              }

              // Carjack Dabab Bus
              val adjacentDababTile = uiState.tiles.find { it.tileType == TacticalTileType.DABAB_BUS && abs(it.x - selectedUnit.x) + abs(it.y - selectedUnit.y) <= 1 }
              Button(
                onClick = {
                  viewModel.carjackVehicle()
                },
                enabled = selectedUnit.ap >= 1 && adjacentDababTile != null && !uiState.dababCarjacked && uiState.currentTurn == TurnState.KIDS_TURN,
                colors = ButtonDefaults.buttonColors(containerColor = TaxiYellow),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("اقتحام المركبة 🚐 (1 AP)", fontSize = 11.sp, color = DarkBg, fontWeight = FontWeight.Bold)
              }

              // Abduct Isolated Police
              val adjacentCop = uiState.units.find { it.faction == Faction.POLICE && !it.isHostage && abs(it.x - selectedUnit.x) + abs(it.y - selectedUnit.y) <= 1 }
              Button(
                onClick = {
                  if (adjacentCop != null) {
                    viewModel.abductPolice(adjacentCop.id)
                  }
                },
                enabled = selectedUnit.ap >= 2 && adjacentCop != null && uiState.currentTurn == TurnState.KIDS_TURN,
                colors = ButtonDefaults.buttonColors(containerColor = GangShawlRed),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("خطف تكتيكي 🪢 (2 AP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              // Banana Trap
              Button(
                onClick = {
                  viewModel.placeBananaTrap(selectedUnit.x, selectedUnit.y)
                },
                enabled = selectedUnit.ap >= 1 && uiState.currentTurn == TurnState.KIDS_TURN && ownedItems.contains("banana_peels"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("فخ الموز 🍌 (1 AP)", fontSize = 11.sp)
              }

              // Fireworks Tammaq
              Button(
                onClick = {
                  viewModel.fireFireworks()
                },
                enabled = selectedUnit.ap >= 2 && uiState.currentTurn == TurnState.KIDS_TURN && ownedItems.contains("fireworks_tammaq"),
                colors = ButtonDefaults.buttonColors(containerColor = GangGraffitiPink),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("طماق وشنير 🧨 (2 AP)", fontSize = 11.sp)
              }

              // School Megaphone Buff (Leader only)
              if (selectedUnit.id == "boss") {
                Button(
                  onClick = {
                    viewModel.shoutMegaphone()
                  },
                  enabled = selectedUnit.ap >= 2 && uiState.currentTurn == TurnState.KIDS_TURN && ownedItems.contains("school_megaphone"),
                  colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text("خطاب حماسي 📢 (2 AP)", fontSize = 11.sp, color = DarkBg, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // End Turn Button
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { viewModel.endTurnAndExecuteAi() },
          enabled = uiState.currentTurn == TurnState.KIDS_TURN,
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(46.dp)
            .testTag("btn_end_tactical_turn")
        ) {
          Text("إنهاء الدور وتسليم المبادرة ⏳", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        IconButton(
          onClick = onRestartStage,
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = "إعادة المرحلة", tint = Color.White)
        }
      }
    }

    // Victory / Defeat Overlay Banner
    if (uiState.victoryMsg != null) {
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (uiState.currentTurn == TurnState.VICTORY) GangNeonGreen.copy(alpha = 0.2f) else PoliceRedLight.copy(alpha = 0.2f)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(
              2.dp,
              if (uiState.currentTurn == TurnState.VICTORY) GangNeonGreen else PoliceAccent,
              RoundedCornerShape(14.dp)
            )
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = if (uiState.currentTurn == TurnState.VICTORY) "🏆 انتصار تكتيكي ساحق!" else "🚨 تمت محاصرة العصابة!",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (uiState.currentTurn == TurnState.VICTORY) GangNeonGreen else PoliceRedLight
              )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = uiState.victoryMsg ?: "",
              color = Color.White,
              fontSize = 13.sp,
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = {
                if (uiState.activeStageIndex < 5) {
                  viewModel.loadStage(uiState.activeStageIndex + 1)
                } else {
                  viewModel.loadStage(1)
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = if (uiState.activeStageIndex < 5) "المرحلة التالية ⏩" else "إعادة من البداية 🔄",
                color = DarkBg,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Battle Logs Terminal
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📜", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "سجل التحركات التكتيكية والملاحظات:",
              color = SanaaGold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            uiState.battleLogs.takeLast(4).forEach { log ->
              Text(
                text = "• $log",
                color = Color.LightGray,
                fontSize = 11.sp,
                lineHeight = 15.sp
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun SpecialistRecruitmentView(
  repository: SanaGameRepository,
  hiredSpecialists: Set<String>,
  playerCoins: Int
) {
  var feedbackMsg by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, SanaaGold, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "تجنيد وتجهيز المتخصصين 👑",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = SanaaGold,
              fontSize = 16.sp
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "وظّف كفاءات خاصة من أطفال الحارات: الطفل الركّيض (العداء السريع)، الطفل القناص (خبير الأسطح)، وسارق الدبابات لمساندة الزعيم الصغير في المعارك.",
            color = Color.LightGray,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }

    items(TacticalSpecialistType.values()) { specialist ->
      val isHired = hiredSpecialists.contains(specialist.name)
      val canAfford = playerCoins >= specialist.hireCost && !isHired

      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, if (isHired) GangNeonGreen.copy(alpha = 0.6f) else DarkCardBorder, RoundedCornerShape(14.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(GangShawlRed.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
          ) {
            Text(text = specialist.iconEmoji, fontSize = 26.sp)
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = specialist.titleAr,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 13.sp
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "⚡ ${specialist.baseAp} AP",
                color = SanaaGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = specialist.roleDescAr,
              color = Color.LightGray,
              fontSize = 11.sp,
              lineHeight = 15.sp
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              val success = repository.hireSpecialist(specialist.name, specialist.hireCost)
              if (success) {
                GameSoundEffects.playCoin()
                feedbackMsg = "تم تجنيد ${specialist.titleAr} بنجاح! سينضم لتشكيلة المعركة ⚡"
              } else {
                feedbackMsg = "عفواً! لا تملك ريالات كافية للتجنيد 🪙"
              }
            },
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
              containerColor = SanaaClay,
              disabledContainerColor = DarkSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("hire_specialist_${specialist.name}")
          ) {
            if (isHired) {
              Text("مجند ✓", color = GangNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } else {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("تجنيد", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${specialist.hireCost} ريال", fontSize = 8.sp, color = TaxiYellow)
              }
            }
          }
        }
      }
    }

    if (feedbackMsg != null) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = feedbackMsg ?: "",
            modifier = Modifier.padding(10.dp),
            color = SanaaGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
