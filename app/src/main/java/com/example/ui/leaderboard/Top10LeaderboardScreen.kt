package com.example.ui.leaderboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SanaGameRepository
import com.example.data.local.HighScoreEntity
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import com.example.ui.theme.*

enum class LeaderboardFilterTab(val titleAr: String, val iconEmoji: String) {
  TOP_SCORES("أعلى 10 نقاط", "🏆"),
  FASTEST_CHASE_TIMES("أسرع أوقات المطاردة", "⏱️"),
  STAGE_CHAMPIONS("أبطال أزقة صنعاء", "🏰")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Top10LeaderboardScreen(
  repository: SanaGameRepository,
  onNavigateBack: () -> Unit,
  onStartChallenge: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(LeaderboardFilterTab.TOP_SCORES) }
  val top10Scores by repository.top10HighScores.collectAsState(initial = emptyList())
  val top10FastestTimes by repository.top10FastestChaseTimes.collectAsState(initial = emptyList())
  val stats by repository.stats.collectAsState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = DarkBackground,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "لوحة الشرف: العشرة الأوائل 🏆",
              color = GoldYemeniLight,
              fontSize = 17.sp,
              fontWeight = FontWeight.Black
            )
            Text(
              text = "تنافس على أفضل أوقات المطاردة وأعلى النقاط في صنعاء",
              color = TextWhiteSecondary,
              fontSize = 10.5.sp
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              GameSoundEffects.playJump()
              onNavigateBack()
            },
            modifier = Modifier.testTag("btn_back_leaderboard")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "الرجوع",
              tint = SanaaGold
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = DarkSurface
        ),
        actions = {
          Surface(
            color = SanaaGold.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SanaaGold.copy(alpha = 0.6f)),
            modifier = Modifier.padding(end = 12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text("رقمك القياسي:", color = TextWhiteSecondary, fontSize = 10.sp)
              Text(
                "${stats.highChaseScore} ⭐",
                color = GoldYemeniLight,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }
      )
    },
    bottomBar = {
      Surface(
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardStroke),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "جاهز لكسر الأرقام القياسية؟",
              color = TextWhitePrimary,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "انطلق في مطاردة 7D وتصدّر قائمة الـ Top 10!",
              color = TextWhiteSecondary,
              fontSize = 10.sp
            )
          }

          Button(
            onClick = {
              HapticManager.vibrateSuccess()
              GameSoundEffects.playEngineStart()
              onStartChallenge()
            },
            colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("btn_challenge_leaderboard")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = DarkBackground,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "تحدّي الآن 🚀",
              color = DarkBackground,
              fontWeight = FontWeight.Black,
              fontSize = 12.5.sp
            )
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 14.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Tab selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DarkSurface)
          .border(1.dp, DarkCardStroke, RoundedCornerShape(12.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        LeaderboardFilterTab.values().forEach { tab ->
          val isSelected = selectedTab == tab
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .then(
                if (isSelected) {
                  Modifier.background(
                    Brush.horizontalGradient(
                      listOf(SanaaGold.copy(alpha = 0.9f), GoldYemeniLight)
                    )
                  )
                } else {
                  Modifier.background(Color.Transparent)
                }
              )
              .clickable {
                selectedTab = tab
                HapticManager.vibrateMovement()
                GameSoundEffects.playJump()
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(tab.iconEmoji, fontSize = 13.sp)
              Text(
                text = tab.titleAr,
                color = if (isSelected) DarkBackground else TextWhiteSecondary,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Active Tab List
      val displayList: List<HighScoreEntity> = when (selectedTab) {
        LeaderboardFilterTab.TOP_SCORES -> top10Scores
        LeaderboardFilterTab.FASTEST_CHASE_TIMES -> top10FastestTimes
        LeaderboardFilterTab.STAGE_CHAMPIONS -> top10Scores
      }

      if (displayList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "جاري تحميل قائمة العشرة الأوائل...",
              color = GoldYemeniLight,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("leaderboard_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 16.dp)
        ) {
          itemsIndexed(displayList.take(10)) { index, entry ->
            LeaderboardRankCard(
              rankIndex = index + 1,
              entry = entry,
              showTimePriority = selectedTab == LeaderboardFilterTab.FASTEST_CHASE_TIMES
            )
          }
        }
      }
    }
  }
}

@Composable
fun LeaderboardRankCard(
  rankIndex: Int,
  entry: HighScoreEntity,
  showTimePriority: Boolean = false,
  modifier: Modifier = Modifier
) {
  val isTop3 = rankIndex in 1..3
  val rankEmoji = when (rankIndex) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> "🎖️"
  }

  val rankBorderBrush = when (rankIndex) {
    1 -> Brush.horizontalGradient(listOf(SanaaGold, GoldYemeniLight, SanaaGold))
    2 -> Brush.horizontalGradient(listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1)))
    3 -> Brush.horizontalGradient(listOf(Color(0xFFCD7F32), Color(0xFFD7CCC8)))
    else -> Brush.horizontalGradient(listOf(DarkCardStroke, DarkCardStroke))
  }

  val rankBgColor = when (rankIndex) {
    1 -> DarkSurface.copy(alpha = 0.95f)
    2 -> DarkSurface.copy(alpha = 0.90f)
    3 -> DarkSurface.copy(alpha = 0.85f)
    else -> DarkSurface.copy(alpha = 0.70f)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(if (isTop3) 6.dp else 2.dp, RoundedCornerShape(14.dp))
      .border(
        width = if (isTop3) 1.5.dp else 1.dp,
        brush = rankBorderBrush,
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("leaderboard_rank_$rankIndex"),
    shape = RoundedCornerShape(14.dp),
    color = rankBgColor
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Rank Badge
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(
            when (rankIndex) {
              1 -> SanaaGold.copy(alpha = 0.25f)
              2 -> Color(0xFF90A4AE).copy(alpha = 0.20f)
              3 -> Color(0xFF8D6E63).copy(alpha = 0.20f)
              else -> DarkCardBg
            }
          )
          .border(
            1.dp,
            when (rankIndex) {
              1 -> SanaaGold
              2 -> Color(0xFFCFD8DC)
              3 -> Color(0xFFBCAAA4)
              else -> DarkCardStroke
            },
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = rankEmoji,
            fontSize = 14.sp
          )
          Text(
            text = "#$rankIndex",
            color = if (isTop3) GoldYemeniLight else TextWhiteSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
          )
        }
      }

      // Player Details
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = entry.playerName,
            color = if (isTop3) GoldYemeniLight else TextWhitePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
          )
          if (entry.isPersonalBest) {
            Surface(
              color = EmeraldGreen.copy(alpha = 0.25f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "سجلك 🌟",
                color = EmeraldGreenLight,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = entry.titleAr,
            color = SanaaGold.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text("•", color = TextWhiteSecondary, fontSize = 9.sp)
          Text(
            text = entry.stageName,
            color = TextWhiteSecondary,
            fontSize = 9.5.sp
          )
        }
      }

      // Score / Time Display
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        if (showTimePriority) {
          // Highlight Chase Time
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text("⏱️", fontSize = 11.sp)
            Text(
              text = entry.getFormattedChaseTime(),
              color = TealCyanLight,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black
            )
          }
          Text(
            text = "${entry.score} نقطة",
            color = TextWhiteSecondary,
            fontSize = 10.sp
          )
        } else {
          // Highlight Score
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text("⭐", fontSize = 11.sp)
            Text(
              text = "%,d".format(entry.score),
              color = GoldYemeniLight,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black
            )
          }
          if (entry.chaseTimeSeconds > 0) {
            Text(
              text = "⏱️ ${entry.getFormattedChaseTime()}",
              color = TealCyanLight,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          } else {
            Text(
              text = "${entry.coinsEarned} ريال 💰",
              color = TextWhiteSecondary,
              fontSize = 9.5.sp
            )
          }
        }
      }
    }
  }
}
