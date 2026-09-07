package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.viewmodel.AndersonCityViewModel

enum class AndersonTab(val titleAr: String, val iconEmoji: String) {
  TERRITORY("خريطة الأحياء", "🗺️"),
  MISSIONS("المهام والقصة", "🎯"),
  GARAGE("كراج السيارات", "🏎️"),
  ARMORY("ترسانة الأسلحة", "🔫"),
  WANTED_SYSTEM("مستوى المطلوبة", "🚨")
}

@Composable
fun AndersonCityScreen(
  onNavigateBack: () -> Unit,
  viewModel: AndersonCityViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var selectedTab by remember { mutableStateOf(AndersonTab.TERRITORY) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090D16))
  ) {
    // 1. Header Bar: 80s/90s Synthwave & Coastal Noir
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF311042))
          )
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0x55000000))
            .testTag("btn_anderson_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "عودة",
            tint = Color.White
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "مدينة أندرسون الساحلية (1988)",
            color = Color(0xFF38BDF8),
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
          Text(
            text = "ANDERSON CITY: 80s/90s SYNDICATE",
            color = Color(0xFFE879F9),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Active Wanted Stars Badge
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x88000000))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(5) { starIdx ->
            val isLit = starIdx < uiState.player.wantedStars
            Text(
              text = if (isLit) "⭐" else "☆",
              fontSize = 13.sp,
              color = if (isLit) Color(0xFFFACC15) else Color(0xFF475569)
            )
          }
        }
      }
    }

    // 2. Player Status Bar (Tom Anderson - Cash & Respect)
    Surface(
      color = Color(0xFF131A29),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("🕶️", fontSize = 20.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = uiState.player.name,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = "المنطقة: ${uiState.player.currentDistrict.titleAr}",
              color = Color(0xFF94A3B8),
              fontSize = 10.sp
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Column(horizontalAlignment = Alignment.End) {
            Text("الخزينة", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("$${uiState.player.cashBalance}", color = Color(0xFF4ADE80), fontWeight = FontWeight.Black, fontSize = 13.sp)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text("الاحترام", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text("${uiState.player.familyRespect}%", color = Color(0xFFF59E0B), fontWeight = FontWeight.Black, fontSize = 13.sp)
          }
        }
      }
    }

    // 3. Notification Ticker
    Surface(
      color = Color(0xFF1E293B),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "📢 ${uiState.statusNotificationAr}",
        color = Color(0xFFE2E8F0),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
      )
    }

    // 4. Navigation Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab.ordinal,
      containerColor = Color(0xFF0D131F),
      contentColor = Color(0xFF38BDF8),
      edgePadding = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      AndersonTab.values().forEach { tab ->
        Tab(
          selected = selectedTab == tab,
          onClick = { selectedTab = tab },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(tab.iconEmoji, fontSize = 13.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text(tab.titleAr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          },
          selectedContentColor = Color(0xFF38BDF8),
          unselectedContentColor = Color(0xFF64748B)
        )
      }
    }

    // 5. Tab Content Body
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      when (selectedTab) {
        AndersonTab.TERRITORY -> TerritoryTabContent(uiState, viewModel)
        AndersonTab.MISSIONS -> MissionsTabContent(uiState, viewModel)
        AndersonTab.GARAGE -> GarageTabContent(uiState, viewModel)
        AndersonTab.ARMORY -> ArmoryTabContent(uiState, viewModel)
        AndersonTab.WANTED_SYSTEM -> WantedSystemTabContent(uiState, viewModel)
      }

      // Mission Dialogue Player Modal (Overlay)
      uiState.activeMission?.let { activeMission ->
        MissionDialogueOverlay(
          mission = activeMission,
          dialogueIndex = uiState.currentDialogueIndex,
          onNext = { viewModel.nextDialogue() },
          onDismiss = { viewModel.dismissMission() }
        )
      }
    }
  }
}

// ----------------------------------------------------
// TAB 1: TERRITORY CONTROL & CITY MAP
// ----------------------------------------------------
@Composable
private fun TerritoryTabContent(
  uiState: AndersonCityViewModel.AndersonCityUiState,
  viewModel: AndersonCityViewModel
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "خريطة السيطرة على أحياء مدينة أندرسون (Territory Control)",
      color = Color(0xFF38BDF8),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )

    uiState.districts.forEach { districtState ->
      val district = districtState.district
      val isSelected = uiState.player.currentDistrict == district

      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) Color(0xFF162032) else Color(0xFF0F1522)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) Color(district.themeColorHex) else Color(0x33475569)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.selectDistrict(district) }
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(district.iconEmoji, fontSize = 24.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "${district.titleAr} (${district.titleEn})",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = district.dangerLevel,
                  color = Color(district.themeColorHex),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
            if (isSelected) {
              Surface(
                color = Color(district.themeColorHex),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "موقعك الحالي 📍",
                  color = Color.Black,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = district.descriptionAr,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Control Percentage Bar
          Text(
            text = "نسبة سيطرة عائلة أندرسون: ${districtState.andersonControlPercent}% (المنافسون: ${districtState.rivalControlPercent}%)",
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { districtState.andersonControlPercent / 100f },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF10B981),
            trackColor = Color(0xFFEF4444)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Daily Revenue & Businesses
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "العائد اليومي: +$${districtState.dailyRevenueCash}",
              color = Color(0xFF4ADE80),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${districtState.activeBusinesses.size} أنشطة تجارية مؤمنة",
              color = Color(0xFF38BDF8),
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// TAB 2: STORY MISSIONS & CINEMATIC DIALOGUES
// ----------------------------------------------------
@Composable
private fun MissionsTabContent(
  uiState: AndersonCityViewModel.AndersonCityUiState,
  viewModel: AndersonCityViewModel
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "مهام قصة صعود عائلة أندرسون (Story Missions)",
      color = Color(0xFF38BDF8),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )

    uiState.missions.forEach { mission ->
      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (mission.isCompleted) Color(0xFF0F1E19) else Color(0xFF131A29)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (mission.isCompleted) Color(0xFF10B981) else Color(0x4438BDF8)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = mission.titleAr,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = mission.titleEn,
                color = Color(0xFFE879F9),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
            if (mission.isCompleted) {
              Surface(
                color = Color(0xFF10B981),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "مكتملة 🏆",
                  color = Color.Black,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            } else {
              Surface(
                color = Color(0xFFF59E0B),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "مطلوب ${mission.initialWantedStars} ⭐",
                  color = Color.Black,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = mission.synopsisAr,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "الهدف: ${mission.objectiveAr}",
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "المكافأة: +$${mission.rewardCash} | +${mission.respectReward}% احترام",
              color = Color(0xFF4ADE80),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )

            Button(
              onClick = { viewModel.startMission(mission) },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (mission.isCompleted) Color(0xFF334155) else Color(0xFF0284C7)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("btn_start_mission_${mission.id}")
            ) {
              Text(
                text = if (mission.isCompleted) "إعادة الحوار 🔄" else "بدء المهمة ▶",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// TAB 3: 80s/90s VEHICLE GARAGE
// ----------------------------------------------------
@Composable
private fun GarageTabContent(
  uiState: AndersonCityViewModel.AndersonCityUiState,
  viewModel: AndersonCityViewModel
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "كراج سيارات مدينة أندرسون الكلاسيكية (80s & 90s Cars)",
      color = Color(0xFF38BDF8),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )

    uiState.vehicles.forEach { car ->
      val isActive = uiState.player.activeVehicleId == car.id

      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (isActive) Color(0xFF1A2333) else Color(0xFF101622)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isActive) Color(0xFF38BDF8) else Color(0x33475569)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(car.iconEmoji, fontSize = 26.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(car.nameAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(car.categoryAr, color = Color(0xFFE879F9), fontSize = 11.sp)
              }
            }
            if (isActive) {
              Surface(
                color = Color(0xFF38BDF8),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "قيد القيادة 🚗",
                  color = Color.Black,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(car.descriptionAr, color = Color(0xFF94A3B8), fontSize = 11.sp)

          Spacer(modifier = Modifier.height(10.dp))
          // Metrics
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("السرعة: ${car.topSpeedMph} MPH", color = Color(0xFFFACC15), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("التدريع: ${car.armorRating}/100", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("التسارع: ${car.accelerationRating}/100", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          Spacer(modifier = Modifier.height(10.dp))
          if (car.isOwned) {
            Button(
              onClick = { viewModel.selectVehicle(car.id) },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) Color(0xFF334155) else Color(0xFF0284C7)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (isActive) "السيارة الحالية في الخدمة" else "اختيار وقيادة السيارة 🔑",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          } else {
            Button(
              onClick = { viewModel.selectVehicle(car.id) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "شراء وتعديل السيارة ($${car.priceCash}) 💵",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// TAB 4: WEAPONRY ARMORY
// ----------------------------------------------------
@Composable
private fun ArmoryTabContent(
  uiState: AndersonCityViewModel.AndersonCityUiState,
  viewModel: AndersonCityViewModel
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "ترسانة أسلحة توم أندرسون الكلاسيكية (Underworld Armory)",
      color = Color(0xFF38BDF8),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )

    uiState.weapons.forEach { gun ->
      val isEquipped = uiState.player.equippedWeaponId == gun.id

      Card(
        colors = CardDefaults.cardColors(
          containerColor = if (isEquipped) Color(0xFF1D2230) else Color(0xFF101622)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isEquipped) Color(0xFFEF4444) else Color(0x33475569)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(gun.iconEmoji, fontSize = 24.sp)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(gun.nameAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("العيار: ${gun.caliberAr}", color = Color(0xFFF97316), fontSize = 11.sp)
              }
            }
            if (isEquipped) {
              Surface(
                color = Color(0xFFEF4444),
                shape = RoundedCornerShape(12.dp)
              ) {
                Text(
                  text = "مجهز حالياً 💥",
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(gun.descriptionAr, color = Color(0xFF94A3B8), fontSize = 11.sp)

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("الضرر: ${gun.damageRating}/100", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("معدل الرمي: ${gun.fireRateRpm} RPM", color = Color(0xFFFACC15), fontSize = 11.sp)
            Text("الخزنة: ${gun.magazineSize} طلقة", color = Color(0xFF38BDF8), fontSize = 11.sp)
          }

          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = { viewModel.equipWeapon(gun.id) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isEquipped) Color(0xFF334155) else Color(0xFFDC2626)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (isEquipped) "السلاح مجهز في حزام توم" else "تجهيز السلاح للقتال 🎯",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }
  }
}

// ----------------------------------------------------
// TAB 5: WANTED LEVEL SIMULATOR (1 TO 5 STARS)
// ----------------------------------------------------
@Composable
private fun WantedSystemTabContent(
  uiState: AndersonCityViewModel.AndersonCityUiState,
  viewModel: AndersonCityViewModel
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "نظام استجابة شرطة أندرسون (Wanted Level: 1 - 5 Stars)",
      color = Color(0xFF38BDF8),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold
    )

    Text(
      text = "قم بتجربة محاكاة وتصعيد استجابة الشرطة في شوارع مدينة أندرسون من مطاردة الدوريات الفردية وحتى الحصار العسكري الشامل:",
      color = Color(0xFF94A3B8),
      fontSize = 12.sp,
      lineHeight = 17.sp
    )

    // Interactive Star Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      (0..5).forEach { stars ->
        val isSelected = uiState.player.wantedStars == stars
        Button(
          onClick = { viewModel.setWantedStars(stars) },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFEF4444) else Color(0xFF1E293B)
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = if (stars == 0) "آمن 0" else "$stars ⭐",
            color = if (isSelected) Color.White else Color(0xFFFACC15),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Detailed police dispatch status card
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131A29)),
      shape = RoundedCornerShape(14.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("🚨", fontSize = 24.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "تقرير غرفة عمليات شرطة مدينة أندرسون",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val details = when (uiState.player.wantedStars) {
          0 -> "لا توجد بلاغات نشطة ضد توم أندرسون. الدوريات في حالة استرخاء اعتيادية."
          1 -> "بلاغ سرقة أو اعتداء خفيف: سيارة دورية واحدة تبحث في شوارع الحي القريب."
          2 -> "اشتباك مسلح في الشارع: استدعاء سيارات اعتراض سريعة (Interceptors) وإطلاق صفارات الإنذار."
          3 -> "مطاردة عالية الخطورة: إقلاع مروحية شرطة أندرسون ونشر حواجز شوكية عند مداخل الجسور البحرية."
          4 -> "إعلان حالة طوارئ بالمدينة: وصول شاحنات فرق التدخل السريع SWAT مدرعة، ورماة قناصة فوق أسطح المباني."
          5 -> "حصار أمني مطلق (Full Lockdown): استنفار عسكري كامل، مدرعات ثقيلة تسد الطرقات الرئيسية، وأوامر مباشرة بإطلاق النار الفوري!"
          else -> ""
        }

        Text(
          text = details,
          color = Color(0xFFF87171),
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(14.dp))
        Button(
          onClick = { viewModel.setWantedStars(0) },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("تغيير المظهر ورش السيارة لإنهاء المطاردة (Lose Wanted Level) 🧼", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}

// ----------------------------------------------------
// MISSION DIALOGUE OVERLAY MODAL
// ----------------------------------------------------
@Composable
private fun MissionDialogueOverlay(
  mission: AndersonMission,
  dialogueIndex: Int,
  onNext: () -> Unit,
  onDismiss: () -> Unit
) {
  val currentLine = mission.dialogues.getOrNull(dialogueIndex)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xCC000000))
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      shape = RoundedCornerShape(18.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF38BDF8)),
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Mission Title Header
        Text(
          text = mission.titleAr,
          color = Color(0xFF38BDF8),
          fontSize = 16.sp,
          fontWeight = FontWeight.Black
        )
        Text(
          text = mission.district.titleAr,
          color = Color(0xFFE879F9),
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (currentLine != null) {
          // Speaker Avatar and Details
          Text(currentLine.speakerAvatarEmoji, fontSize = 42.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = currentLine.speakerNameAr,
            color = Color(0xFFFACC15),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = currentLine.speakerRoleAr,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Dialogue Bubble
          Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "\"${currentLine.textAr}\"",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center,
              lineHeight = 20.sp,
              modifier = Modifier.padding(14.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            OutlinedButton(
              onClick = onDismiss,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("إلغاء ✖", fontSize = 12.sp)
            }

            Button(
              onClick = onNext,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("btn_next_dialogue")
            ) {
              val isLast = dialogueIndex >= mission.dialogues.size - 1
              Text(
                text = if (isLast) "إكمال المهمة والسيطرة على الحي 🏁" else "التالي ▶",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }
}
