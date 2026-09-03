package com.example.ui.hideout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SanaGameRepository
import com.example.model.Faction
import com.example.sound.GameSoundEffects
import com.example.ui.components.SanaaTopBar
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class MudTowerFloor(
  val floorNumber: Int,
  val nameAr: String,
  var trapType: String? = null,
  var isBreached: Boolean = false,
  var hasHostage: Boolean = false,
  var hasScoutGuard: Boolean = false
)

@Composable
fun HideoutTacticsScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val stats by repository.stats.collectAsState()
  val selectedFaction by repository.selectedFaction.collectAsState()

  val floors = remember {
    mutableStateListOf(
      MudTowerFloor(4, "سطح البرج الطيني والقمريات", trapType = "مفرقعات صوتية", hasScoutGuard = true),
      MudTowerFloor(3, "غرفة الاجتماعات السرية (المحتجز)", trapType = "حبال تعثر", hasHostage = true, hasScoutGuard = true),
      MudTowerFloor(2, "مخزن الغرافيتي والمؤن", trapType = "دلو زيت", hasScoutGuard = false),
      MudTowerFloor(1, "المدخل الرئيسي وباب الزقاق", trapType = null, hasScoutGuard = true)
    )
  }

  var missionTimer by remember { mutableIntStateOf(90) }
  var gangMorale by remember { mutableIntStateOf(100) }
  var policeReadiness by remember { mutableIntStateOf(20) }
  var isMissionActive by remember { mutableStateOf(false) }
  var missionResult by remember { mutableStateOf<String?>(null) }
  val tacticalLogs = remember { mutableStateListOf<String>() }

  // Walkie Talkie Dialogue
  var currentDialogue by remember {
    mutableStateOf(
      if (selectedFaction == Faction.GANG)
        "الزعيم الصغير: «الشرطي في الدور الثالث.. لا تدعوا أحد يقترب من الباب!»"
      else
        "مدير الشرطة: «تم تطويق البيت الطيني.. استعدوا للاقتحام الآمن!»"
    )
  }

  // Timer Tick
  LaunchedEffect(isMissionActive) {
    if (isMissionActive) {
      while (missionTimer > 0 && missionResult == null) {
        delay(1000)
        missionTimer--
        if (selectedFaction == Faction.GANG) {
          policeReadiness = (policeReadiness + 1).coerceAtMost(100)
          if (policeReadiness >= 100) {
            // Police attempt breach
            val unbreachedIndex = floors.indexOfFirst { !it.isBreached }
            if (unbreachedIndex != -1) {
              val currentFloor = floors[unbreachedIndex]
              floors[unbreachedIndex] = currentFloor.copy(isBreached = true)
              tacticalLogs.add("🚨 الشرطة اقتحمت: ${currentFloor.nameAr}")
              GameSoundEffects.playSiren()
              if (currentFloor.hasHostage) {
                missionResult = "تم تحرير الضابط المحتجز من قبل الشرطة!"
                GameSoundEffects.playPoliceWhistle()
                repository.recordTacticsVictory(Faction.POLICE, 100)
              }
            }
          }
        } else {
          // As Police
          if (missionTimer == 0) {
            missionResult = "انتهى الوقت وتمكنت العصابة من تهريب المحتجز!"
          }
        }
      }
    }
  }

  fun startMission() {
    missionTimer = 90
    gangMorale = 100
    policeReadiness = 20
    missionResult = null
    tacticalLogs.clear()
    tacticalLogs.add("بدأت العملية التكتيكية في البيت الطيني المهجور.")
    isMissionActive = true
    GameSoundEffects.playWalkieTalkie()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBg)
  ) {
    SanaaTopBar(
      title = "تكتيك البيت الطيني المهجور",
      subtitle = "المقر السري • إدارة الرهائن والاقتحام التكتيكي",
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
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Top Status Banner: Timer, Morale, Readiness
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⏱️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "الوقت المتبقي: $missionTimer ثانية",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SanaaGold,
                    fontSize = 14.sp
                  )
                )
              }

              // Faction Status
              Text(
                text = if (selectedFaction == Faction.GANG) "معنويات العصابة: $gangMorale%" else "جاهزية القوات: $policeReadiness%",
                color = if (selectedFaction == Faction.GANG) GangNeonGreen else PoliceAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Walkie Talkie Radio Dialogue Card
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "📻", fontSize = 20.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = currentDialogue,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = Color.White,
                  fontSize = 12.sp,
                  lineHeight = 17.sp
                )
              )
            }
          }
        }
      }

      // Floors of the Sana'a Historic Mud Tower
      itemsIndexed(floors) { index, floor ->
        Card(
          colors = CardDefaults.cardColors(
            containerColor = if (floor.isBreached) DarkSurfaceVariant.copy(alpha = 0.5f) else DarkSurface
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(
              width = if (floor.hasHostage) 1.5.dp else 1.dp,
              color = if (floor.hasHostage) SanaaGold else DarkCardBorder,
              shape = RoundedCornerShape(14.dp)
            )
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
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SanaaClay),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${floor.floorNumber}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = floor.nameAr,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      fontSize = 14.sp
                    )
                  )
                  if (floor.isBreached) {
                    Text(
                      text = "🚨 مقتحم ومطوق من قبل الشرطة",
                      color = PoliceRedLight,
                      fontSize = 11.sp
                    )
                  }
                }
              }

              // Badges for Hostage or Scout
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (floor.hasHostage) {
                  AssistChip(
                    onClick = {},
                    label = { Text("👮‍♂️ المحتجز", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                      containerColor = GangShawlRed.copy(alpha = 0.3f),
                      labelColor = Color.White
                    )
                  )
                }
                if (floor.hasScoutGuard) {
                  AssistChip(
                    onClick = {},
                    label = { Text("🎒 حارس مشاغب", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                      containerColor = SanaaMudWarm.copy(alpha = 0.4f),
                      labelColor = Color.White
                    )
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trap & Action Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "الفخ المنصوب: ${floor.trapType ?: "لا يوجد فخ"}",
                color = if (floor.trapType != null) TaxiYellow else Color.Gray,
                fontSize = 12.sp
              )

              // Tactical Action per Floor
              if (selectedFaction == Faction.GANG && !floor.isBreached) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = {
                      val trapOptions = listOf("مفرقعات صوتية", "حبال تعثر", "دلو زيت", "قشور موز")
                      val nextTrap = trapOptions.random()
                      floors[index] = floor.copy(trapType = nextTrap)
                      tacticalLogs.add("تم نصب $nextTrap في ${floor.nameAr}")
                      GameSoundEffects.playFirework()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SanaaClay),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text("نصب فخ ⚙️", fontSize = 11.sp)
                  }

                  if (floor.hasHostage) {
                    Button(
                      onClick = {
                        val availableIndices = floors.indices.filter { it != index && !floors[it].isBreached }
                        if (availableIndices.isNotEmpty()) {
                          val targetIdx = availableIndices.random()
                          floors[index] = floor.copy(hasHostage = false)
                          floors[targetIdx] = floors[targetIdx].copy(hasHostage = true)
                          tacticalLogs.add("تم نقل المحتجز سراً إلى ${floors[targetIdx].nameAr}")
                          GameSoundEffects.playJump()
                        }
                      },
                      colors = ButtonDefaults.buttonColors(containerColor = GangShawlRed),
                      shape = RoundedCornerShape(10.dp),
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                      Text("نقل المحتجز 🔄", fontSize = 11.sp)
                    }
                  }
                }
              } else if (selectedFaction == Faction.POLICE && !floor.isBreached) {
                Button(
                  onClick = {
                    floors[index] = floor.copy(isBreached = true)
                    if (floor.trapType != null) {
                      tacticalLogs.add("تم تفكيك فخ (${floor.trapType}) في ${floor.nameAr}")
                    }
                    if (floor.hasHostage) {
                      missionResult = "نجحت الشرطة في تحرير الضابط المحتجز بسلام! 🎉"
                      GameSoundEffects.playPoliceWhistle()
                      repository.recordTacticsVictory(Faction.POLICE, 250)
                    } else {
                      tacticalLogs.add("تم تأمين ${floor.nameAr} - المحتجز في دور آخر!")
                      GameSoundEffects.playNoise(200)
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = PoliceAccent),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text("اقتحام وتأمين 🛡️", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }

      // Tactical Mission Logs
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "سجل العمليات الميدانية 📜",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = SanaaGold,
                fontSize = 13.sp
              )
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (tacticalLogs.isEmpty()) {
              Text("لا توجد تحركات بعد..", color = Color.Gray, fontSize = 12.sp)
            } else {
              tacticalLogs.takeLast(5).reversed().forEach { log ->
                Text(
                  text = "• $log",
                  color = Color.White.copy(alpha = 0.9f),
                  fontSize = 11.sp,
                  lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
              }
            }
          }
        }
      }

      // Mission Result Banner
      if (missionResult != null) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.5.dp, SanaaGold, RoundedCornerShape(16.dp))
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "نتيجة المهمة التكتيكية",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = SanaaGold
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = missionResult ?: "",
                color = Color.White,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = { startMission() },
                colors = ButtonDefaults.buttonColors(containerColor = SanaaClay)
              ) {
                Text("بدء مهمة جديدة", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }

    // Bottom Action Bar: Start or Negotiate
    Surface(
      color = DarkSurface,
      tonalElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (!isMissionActive) {
          Button(
            onClick = { startMission() },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (selectedFaction == Faction.GANG) GangShawlRed else PoliceAccent
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
              .testTag("start_tactics_mission_btn")
          ) {
            Text(
              text = if (selectedFaction == Faction.GANG) "بدء خطة حماية المخبأ 🏰" else "بدء خطة تحرير الرهائن 🛡️",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          // Walkie Talkie Negotiation Button
          Button(
            onClick = {
              val gangQuotes = listOf(
                "الزعيم الصغير: «نطلب كرتون عصير ومفرقعات مقابل إطلاق سراح الضابط!»",
                "الزعيم الصغير: «الحارة كلها معنا.. انسحبوا من الزقاق!»",
                "الزعيم الصغير: «ممنوع اقتراب الدرون من النوافذ القمرية!»"
              )
              val policeQuotes = listOf(
                "مدير الشرطة: «يا زعيم سلم نفسك والضابط ولن يُعاقب أحد!»",
                "مدير الشرطة: «المبنى محاصر بالكامل من باب اليمن إلى السبعين!»",
                "مدير الشرطة: «سلموا المحتجز وسنقدم لكم هدايا ونشاطات رياضية!»"
              )
              currentDialogue = if (selectedFaction == Faction.GANG) gangQuotes.random() else policeQuotes.random()
              GameSoundEffects.playWalkieTalkie()
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
          ) {
            Text("مفاوضات اللاسلكي 📻", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SanaaGold)
          }
        }
      }
    }
  }
}
