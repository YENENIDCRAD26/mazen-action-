package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.GameProgressManager
import com.example.data.SanaGameRepository
import com.example.model.GameScreen
import com.example.sound.GameSoundEffects
import com.example.ui.beirut.GtaBeirut3DScreen
import com.example.ui.dossier.CharacterDossierScreen
import com.example.ui.game.ChaseGameScreen
import com.example.ui.game.MainGameScreen
import com.example.ui.hideout.HideoutTacticsScreen
import com.example.ui.hq.HqUpgradesScreen
import com.example.ui.leaderboard.Top10LeaderboardScreen
import com.example.ui.components.MilestoneToastHudOverlay
import com.example.ui.menu.MainMenuScreen
import com.example.ui.sanaa7d.GtaSanaa7DChaseScreen
import com.example.ui.tactical.TacticalStrategyScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vehicle.VehicleHeistScreen
import com.example.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val repository = SanaGameRepository(applicationContext)
    val progressManager = GameProgressManager(applicationContext)
    val userViewModel = UserViewModel(application, repository, progressManager)

    setContent {
      MyApplicationTheme(darkTheme = true) {
        var currentScreen by remember { mutableStateOf(GameScreen.MAIN_GAME) }

        BackHandler(enabled = currentScreen != GameScreen.MAIN_GAME && currentScreen != GameScreen.MAIN_MENU) {
          GameSoundEffects.playJump()
          currentScreen = GameScreen.MAIN_GAME
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
              fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
            },
            label = "screen_transition",
            modifier = Modifier.padding(innerPadding)
          ) { screen ->
            when (screen) {
              GameScreen.MAIN_GAME -> {
                MainGameScreen(
                  userViewModel = userViewModel,
                  onStartGame = { stageId ->
                    currentScreen = GameScreen.GTA_UNIFIED_ENGINE
                  },
                  onNavigateTo = { target -> currentScreen = target }
                )
              }
              GameScreen.GTA_UNIFIED_ENGINE -> {
                val selectedCharId = userViewModel.selectedCharacter.collectAsState().value.id
                val heroId = when (selectedCharId) {
                  "faris_parkour" -> com.example.ui.game.UnifiedHeroId.FARIS
                  "ammar_driver" -> com.example.ui.game.UnifiedHeroId.AMMAR
                  "salem_sniper" -> com.example.ui.game.UnifiedHeroId.SALEM
                  else -> com.example.ui.game.UnifiedHeroId.MAZEN
                }
                com.example.ui.game.UnifiedGtaGameEngineScreen(
                  initialHeroId = heroId,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.MAIN_MENU -> {
                MainMenuScreen(
                  repository = repository,
                  onNavigateTo = { target -> currentScreen = target }
                )
              }
              GameScreen.GTA_SANAA_7D -> {
                GtaSanaa7DChaseScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.GTA_BEIRUT_3D -> {
                GtaBeirut3DScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.TACTICAL_XCOM -> {
                TacticalStrategyScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.CHASE_GAME -> {
                ChaseGameScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.VEHICLE_HEIST -> {
                VehicleHeistScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.HIDEOUT_TACTICS -> {
                HideoutTacticsScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.CHARACTER_DOSSIER, GameScreen.STORY_GALLERY -> {
                CharacterDossierScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.HQ_UPGRADES -> {
                HqUpgradesScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME }
                )
              }
              GameScreen.LEADERBOARD -> {
                Top10LeaderboardScreen(
                  repository = repository,
                  onNavigateBack = { currentScreen = GameScreen.MAIN_GAME },
                  onStartChallenge = { currentScreen = GameScreen.GTA_SANAA_7D }
                )
              }
            }
          }

          // Global Milestone Toast HUD Overlay
          MilestoneToastHudOverlay()
        }
      }
    }
  }
}


