package com.example.ui.missions

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.PlayerEscapeSituation
import com.example.data.local.GameMissionEntity
import com.example.sound.GameSoundEffects
import com.example.ui.viewmodel.GameMissionViewModel
import com.example.ui.viewmodel.MissionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionListScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: GameMissionViewModel = viewModel()
) {
  val missions by viewModel.filteredMissions.collectAsState()
  val allMissions by viewModel.allMissions.collectAsState()
  val activeFilter by viewModel.filter.collectAsState()
  val advisorState by viewModel.aiAdvisorState.collectAsState()

  var showAdvisorModal by remember { mutableStateOf(false) }
  var showAddDialog by remember { mutableStateOf(false) }

  val totalCount = allMissions.size
  val completedCount = allMissions.count { it.isCompleted }
  val progressPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()) else 0f

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "مهام عالم صنعاء القديمة",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "نظام المهام المدعوم بقاعدة بيانات Room ومرشد Firebase AI",
              style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              GameSoundEffects.playJump()
              onNavigateBack()
            },
            modifier = Modifier.testTag("mission_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "العودة للعبة الرئيسية"
            )
          }
        },
        actions = {
          FilledTonalButton(
            onClick = {
              GameSoundEffects.playCoin()
              showAdvisorModal = true
              if (advisorState.advice == null && !advisorState.isLoading) {
                viewModel.requestEscapeTactics()
              }
            },
            modifier = Modifier
              .padding(end = 8.dp)
              .testTag("open_ai_advisor_button"),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SmartToy,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("مرشد اللعبة الذكي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          GameSoundEffects.playCoin()
          showAddDialog = true
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("add_mission_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "إضافة مهمة جديدة")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Mission Stats Banner
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = "📊", fontSize = 20.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "تقدم المهام التكتيكية",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Text(
              text = "$completedCount من $totalCount مكتملة (${(progressPercent * 100).toInt()}%)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          LinearProgressIndicator(
            progress = { progressPercent },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            StatItem(label = "إجمالي المهام", value = "$totalCount", emoji = "📜")
            StatItem(label = "قيد الإنجاز", value = "${totalCount - completedCount}", emoji = "⏳")
            StatItem(label = "المكتملة", value = "$completedCount", emoji = "✅")
            StatItem(label = "مكافآت العملات", value = "${allMissions.sumOf { it.rewardCoins }}", emoji = "💰")
          }
        }
      }

      // 2. Filter Tabs
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(MissionFilter.values()) { filterOption ->
          FilterChip(
            selected = activeFilter == filterOption,
            onClick = {
              GameSoundEffects.playJump()
              viewModel.setFilter(filterOption)
            },
            label = { Text(filterOption.labelAr) },
            modifier = Modifier.testTag("filter_chip_${filterOption.name}")
          )
        }
      }

      // 3. Missions List
      if (missions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Text(text = "📭", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "لا توجد مهام تطابق الفلتر الحالي",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "يمكنك إضافة مهام جديدة باستخدام الزر بالأسفل (+)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
        ) {
          items(missions, key = { it.id }) { mission ->
            MissionCardItem(
              mission = mission,
              onToggleCompletion = {
                GameSoundEffects.playCoin()
                viewModel.toggleMissionCompletion(mission)
              },
              onConsultAdvisor = {
                GameSoundEffects.playJump()
                viewModel.selectMission(mission)
                viewModel.requestEscapeTactics()
                showAdvisorModal = true
              },
              onDelete = {
                GameSoundEffects.playCarCrash()
                viewModel.deleteMission(mission.id)
              }
            )
          }
        }
      }
    }
  }

  // AI Advisor Modal Sheet
  if (showAdvisorModal) {
    TacticalAdvisorBottomSheet(
      advisorState = advisorState,
      onDismiss = { showAdvisorModal = false },
      onPresetSelected = { presetIndex ->
        GameSoundEffects.playJump()
        viewModel.selectPresetScenario(presetIndex)
      },
      onRefreshTactics = {
        GameSoundEffects.playCoin()
        viewModel.requestEscapeTactics()
      }
    )
  }

  // Add Mission Dialog
  if (showAddDialog) {
    AddNewMissionDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { title, desc, diff, loc, reward, emoji ->
        viewModel.addNewMission(title, desc, diff, loc, reward, emoji)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun MissionCardItem(
  mission: GameMissionEntity,
  onToggleCompletion: () -> Unit,
  onConsultAdvisor: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val difficultyColor = when {
    mission.difficulty.contains("سهل") || mission.difficulty.equals("EASY", true) -> Color(0xFF4CAF50)
    mission.difficulty.contains("متوسط") || mission.difficulty.equals("MEDIUM", true) -> Color(0xFFFFA000)
    mission.difficulty.contains("صعب") || mission.difficulty.equals("HARD", true) -> Color(0xFFE53935)
    else -> Color(0xFF8E24AA) // أسطوري
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("mission_card_${mission.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (mission.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    border = if (mission.isCompleted) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: Title + Difficulty Badge + Completion Icon
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Text(text = mission.iconEmoji, fontSize = 24.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = mission.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (mission.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "📍 ${mission.location}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Difficulty Badge
        Surface(
          color = difficultyColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = mission.difficulty,
            color = difficultyColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Description
      Text(
        text = mission.description,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 13.sp
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Rewards info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "💰 ${mission.rewardCoins} نقدية",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "⭐ ${mission.rewardXp} XP",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
              ),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        // Status badge
        Text(
          text = if (mission.isCompleted) "مكتملة ✅" else "قيد التنفيذ ⏳",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = if (mission.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
          )
        )
      }

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )

      // Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Completion Checkbox Button
        OutlinedButton(
          onClick = onToggleCompletion,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (mission.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
          ),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("toggle_mission_${mission.id}")
        ) {
          Icon(
            imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (mission.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (mission.isCompleted) "إلغاء الإنجاز" else "تعيين كمكتملة",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // AI Advisor Shortcut for this mission
          FilledTonalButton(
            onClick = onConsultAdvisor,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("advisor_for_mission_${mission.id}")
          ) {
            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("نصائح الهروب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Delete button
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              Icons.Default.DeleteOutline,
              contentDescription = "حذف المهمة",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalAdvisorBottomSheet(
  advisorState: com.example.ui.viewmodel.AiAdvisorUiState,
  onDismiss: () -> Unit,
  onPresetSelected: (Int) -> Unit,
  onRefreshTactics: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 24.dp)
    ) {
      // Header with Bot Avatar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary
                  )
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "🤖", fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "مرشد اللعبة التكتيكي (Firebase AI)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "خبير الهروب من الشرطة في أزقة صنعاء القديمة",
              style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "إغلاق")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Current Situation Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "📍 الموقع: ${advisorState.activeSituation.location}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "⭐ النجوم: ${"★".repeat(advisorState.activeSituation.wantedStars)}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300)
              )
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "🚔 المحاصرة: ${advisorState.activeSituation.policeForces}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "🎒 الأدوات المتوفرة: ${advisorState.activeSituation.currentVehicleOrTool}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Scenario Presets
      Text(
        text = "سيناريوهات سريعة في أزقة وحارات صنعاء:",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val presets = listOf("باب اليمن 🏰", "السائلة التاريخية 🌊", "سوق الملح 🧂", "أسطح القاسمي 🏃‍♂️")
        items(presets.size) { index ->
          OutlinedButton(
            onClick = { onPresetSelected(index) },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("preset_scenario_$index")
          ) {
            Text(presets[index], fontSize = 12.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // AI Response Section
      if (advisorState.isLoading) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(36.dp),
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "مرشد اللعبة يحلل أزقة صنعاء ومسارات الشرطة عبر Firebase AI...",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              textAlign = TextAlign.Center
            )
          }
        }
      } else if (advisorState.advice != null) {
        val advice = advisorState.advice
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "⚡ خطة الهروب التكتيكية",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )

              Surface(
                color = if (advice.isLiveAiGenerated) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = if (advice.isLiveAiGenerated) "ذكاء اصطناعي مباشر (Firebase AI)" else "تكتيك احتياطي ذكي",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = advice.fullAdviceText,
              style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Refresh Button
      Button(
        onClick = onRefreshTactics,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("refresh_ai_tactics_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("تحديث نصيحة الهروب من مرشد اللعبة 🔄", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun AddNewMissionDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, desc: String, diff: String, loc: String, reward: Int, emoji: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var difficulty by remember { mutableStateOf("متوسط") }
  var location by remember { mutableStateOf("أزقة باب اليمن") }
  var rewardCoinsText by remember { mutableStateOf("300") }
  var selectedEmoji by remember { mutableStateOf("🎯") }

  val difficultyOptions = listOf("سهل", "متوسط", "صعب", "أسطوري")
  val emojiOptions = listOf("🎯", "🏰", "🚐", "🌊", "🧂", "🏃‍♂️", "👑", "🧨")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = "إضافة مهمة جديدة إلى Room", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("عنوان المهمة") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("الوصف والتفاصيل") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3
        )

        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text("الموقع في صنعاء القديمة") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text(text = "مستوى الصعوبة:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          difficultyOptions.forEach { diff ->
            FilterChip(
              selected = difficulty == diff,
              onClick = { difficulty = diff },
              label = { Text(diff, fontSize = 11.sp) }
            )
          }
        }

        OutlinedTextField(
          value = rewardCoinsText,
          onValueChange = { rewardCoinsText = it.filter { ch -> ch.isDigit() } },
          label = { Text("مكافأة العملات") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text(text = "أيقونة المهمة:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(emojiOptions) { emoji ->
            Surface(
              shape = CircleShape,
              color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .size(36.dp)
                .clickable { selectedEmoji = emoji }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 18.sp)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && description.isNotBlank()) {
            val reward = rewardCoinsText.toIntOrNull() ?: 300
            onConfirm(title, description, difficulty, location, reward, selectedEmoji)
          }
        },
        enabled = title.isNotBlank() && description.isNotBlank()
      ) {
        Text("حفظ المهمة في Room")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    }
  )
}

@Composable
private fun StatItem(label: String, value: String, emoji: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = emoji, fontSize = 16.sp)
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    )
  }
}
