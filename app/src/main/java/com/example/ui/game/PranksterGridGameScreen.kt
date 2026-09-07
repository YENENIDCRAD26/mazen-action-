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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.viewmodel.PranksterGameLoopViewModel

/**
 * Interactive UI for the Coroutine-driven Grid Game Loop.
 * Displays the Young Prankster and Police characters updating in real time.
 */
@Composable
fun PranksterGridGameScreen(
  onNavigateBack: () -> Unit,
  viewModel: PranksterGameLoopViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val gameState by viewModel.gameState.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF0F172A))
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier
          .clip(CircleShape)
          .background(Color(0xFF1E293B))
          .testTag("btn_grid_game_back")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = Color.White)
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "مطاردة المشاغب والشرطة",
          color = Color(0xFFF5C518),
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "حلقة اللعبة الزمنية (Coroutine Game Loop: ${gameState.tickIntervalMs}ms)",
          color = Color(0xFF94A3B8),
          fontSize = 11.sp
        )
      }

      IconButton(
        onClick = { viewModel.resetGame() },
        modifier = Modifier
          .clip(CircleShape)
          .background(Color(0xFF1E293B))
          .testTag("btn_grid_game_reset")
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = Color(0xFF38BDF8))
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 2. Status & Metrics HUD
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "النقاط: ${gameState.score} ⭐",
            color = Color(0xFFF5C518),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Text(
            text = "النبضة (Tick): #${gameState.tickCount}",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
          )
          Surface(
            color = if (gameState.isRunning) Color(0xFF166534) else Color(0xFF991B1B),
            shape = RoundedCornerShape(16.dp)
          ) {
            Text(
              text = if (gameState.isRunning) "حلقة نشطة 🟢" else "متوقفة مؤقتاً ⏸️",
              color = Color.White,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = gameState.statusMessageAr,
          color = Color(0xFFE2E8F0),
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Characters Info Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Player (Young Prankster) Card
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
        modifier = Modifier.weight(1f)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Text("👦 ${gameState.player.name}", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text("الموقع: (${gameState.player.position.x}, ${gameState.player.position.y})", color = Color.White, fontSize = 11.sp)
          Text("الحالة: ${gameState.player.state.titleAr} ${gameState.player.state.iconEmoji}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
          Text("المقالب: ${gameState.player.pranksCount}/${gameState.prankTargets.size}", color = Color(0xFFFDE047), fontSize = 11.sp)
        }
      }

      // Police Officers Card
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1015)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
        modifier = Modifier.weight(1f)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          val activeOfficer = gameState.policeOfficers.firstOrNull()
          Text("👮 ${activeOfficer?.name ?: "الدورية"}", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text("الموقع: (${activeOfficer?.position?.x ?: 0}, ${activeOfficer?.position?.y ?: 0})", color = Color.White, fontSize = 11.sp)
          Text("الحالة: ${activeOfficer?.state?.titleAr ?: ""} ${activeOfficer?.state?.iconEmoji ?: ""}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
          Text("مدى الرؤية: ${activeOfficer?.visionRange ?: 0} مربعات", color = Color(0xFF93C5FD), fontSize = 11.sp)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. Game World Grid (8x8 cells)
    Box(
      modifier = Modifier
        .size(310.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0xFF1E2430))
        .border(2.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
        .padding(4.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
      ) {
        for (y in 0 until gameState.gridHeight) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            for (x in 0 until gameState.gridWidth) {
              val pos = GridPosition(x, y)
              val isObstacle = gameState.obstacles.contains(pos)
              val isHidingSpot = gameState.hidingSpots.contains(pos)
              val isPrankTarget = gameState.prankTargets.contains(pos)
              val isCompletedPrank = gameState.completedPranks.contains(pos)
              val isEscape = gameState.escapeGate == pos

              val isPlayer = gameState.player.position == pos
              val policeAtCell = gameState.policeOfficers.find { it.position == pos }

              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(
                    when {
                      isObstacle -> Color(0xFF451A03) // Clay Wall
                      isPlayer -> Color(0xFF15803D)
                      policeAtCell != null -> Color(0xFF991B1B)
                      isEscape -> Color(0xFF0369A1)
                      isHidingSpot -> Color(0xFF334155)
                      isPrankTarget -> if (isCompletedPrank) Color(0xFF1E293B) else Color(0xFF854D0E)
                      else -> Color(0xFF0F172A)
                    }
                  )
                  .border(
                    0.5.dp,
                    if (isPlayer) Color(0xFF4ADE80) else Color(0x3364748B),
                    RoundedCornerShape(4.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                when {
                  isPlayer -> {
                    Text(
                      text = if (gameState.player.isHidden) "🥷" else "👦",
                      fontSize = 18.sp
                    )
                  }
                  policeAtCell != null -> {
                    Text(
                      text = if (policeAtCell.isStunned) "💫" else "👮",
                      fontSize = 18.sp
                    )
                  }
                  isObstacle -> {
                    Text("🧱", fontSize = 14.sp)
                  }
                  isHidingSpot -> {
                    Text("📦", fontSize = 14.sp)
                  }
                  isPrankTarget -> {
                    Text(if (isCompletedPrank) "✅" else "🎭", fontSize = 14.sp)
                  }
                  isEscape -> {
                    Text("🚪", fontSize = 14.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 5. Gamepad & Loop Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // DPAD Controller for Young Prankster
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("dpad_controls")
      ) {
        IconButton(
          onClick = { viewModel.setPlayerDirection(GridDirection.UP) },
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF334155))
        ) {
          Icon(Icons.Default.KeyboardArrowUp, contentDescription = "أعلى", tint = Color.White)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = { viewModel.setPlayerDirection(GridDirection.LEFT) },
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF334155))
          ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "يسار", tint = Color.White)
          }

          IconButton(
            onClick = { viewModel.setPlayerDirection(GridDirection.NONE) },
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E293B))
          ) {
            Text("⏹️", fontSize = 16.sp)
          }

          IconButton(
            onClick = { viewModel.setPlayerDirection(GridDirection.RIGHT) },
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF334155))
          ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "يمين", tint = Color.White)
          }
        }

        IconButton(
          onClick = { viewModel.setPlayerDirection(GridDirection.DOWN) },
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF334155))
        ) {
          Icon(Icons.Default.KeyboardArrowDown, contentDescription = "أسفل", tint = Color.White)
        }
      }

      // Action Buttons
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(start = 12.dp)
      ) {
        // Prank Tool Button
        Button(
          onClick = { viewModel.triggerPrankAction() },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_prank_action")
        ) {
          Text("رمي فخ الموز 🍌", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Loop Play / Pause Button
        Button(
          onClick = {
            if (gameState.isRunning) viewModel.pauseGameLoop() else viewModel.startGameLoop()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (gameState.isRunning) Color(0xFFEF4444) else Color(0xFF10B981)
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_toggle_game_loop")
        ) {
          Text(
            text = if (gameState.isRunning) "إيقاف مؤقت ⏸️" else "تشغيل الحلقة ▶️",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        // Interval Speed Picker
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf(150L to "سريع", 300L to "عادي", 500L to "بطيء").forEach { (interval, label) ->
            FilterChip(
              selected = gameState.tickIntervalMs == interval,
              onClick = { viewModel.setTickInterval(interval) },
              label = { Text(label, fontSize = 10.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF38BDF8),
                selectedLabelColor = Color.Black
              )
            )
          }
        }
      }
    }
  }
}
