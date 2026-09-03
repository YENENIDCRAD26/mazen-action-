package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.HighScoreEntity
import com.example.model.Faction
import com.example.model.GameData
import com.example.model.GameStats
import com.example.model.PlayerLevelSystem
import com.example.model.UpgradeItem
import com.example.sound.HapticManager
import com.example.sound.SanaMediaPlayerSoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SanaGameRepository(context: Context) {
  private val appContext = context.applicationContext
  private val prefs: SharedPreferences =
    appContext.getSharedPreferences("sana_heroes_game_prefs", Context.MODE_PRIVATE)

  // Room Database for Top Players & High Scores
  val database: AppDatabase = AppDatabase.getDatabase(appContext)
  val highScoreDao = database.highScoreDao()
  val topHighScores: Flow<List<HighScoreEntity>> = highScoreDao.getTopScores(15)
  val top10HighScores: Flow<List<HighScoreEntity>> = highScoreDao.getTop10HighScores()
  val top10FastestChaseTimes: Flow<List<HighScoreEntity>> = highScoreDao.getTop10FastestChaseTimes()

  // Sound & Haptics Managers
  val soundManager: SanaMediaPlayerSoundManager = SanaMediaPlayerSoundManager(appContext)

  init {
    HapticManager.initialize(appContext)
  }

  private val _stats = MutableStateFlow(loadStats())
  val stats: StateFlow<GameStats> = _stats.asStateFlow()

  private val _upgrades = MutableStateFlow(GameData.initialUpgrades)
  val upgrades: StateFlow<List<UpgradeItem>> = _upgrades.asStateFlow()

  private val _ownedArmoryItemIds = MutableStateFlow(loadOwnedArmoryItems())
  val ownedArmoryItemIds: StateFlow<Set<String>> = _ownedArmoryItemIds.asStateFlow()

  private val _hiredSpecialistIds = MutableStateFlow(loadHiredSpecialists())
  val hiredSpecialistIds: StateFlow<Set<String>> = _hiredSpecialistIds.asStateFlow()

  private val _completedStageIndexes = MutableStateFlow(loadCompletedStages())
  val completedStageIndexes: StateFlow<Set<Int>> = _completedStageIndexes.asStateFlow()

  private val _isDeveloperModeActive = MutableStateFlow(prefs.getBoolean("is_dev_mode_active", false))
  val isDeveloperModeActive: StateFlow<Boolean> = _isDeveloperModeActive.asStateFlow()

  private val _adminPlayerName = MutableStateFlow(prefs.getString("admin_player_name", if (_isDeveloperModeActive.value) "mazengalab" else null))
  val adminPlayerName: StateFlow<String?> = _adminPlayerName.asStateFlow()

  // Offline / VS Computer Mode (Active by default, fully local AI and zero web reliance)
  private val _isOfflineVsComputerMode = MutableStateFlow(prefs.getBoolean("offline_vs_computer_mode", true))
  val isOfflineVsComputerMode: StateFlow<Boolean> = _isOfflineVsComputerMode.asStateFlow()

  private val _selectedFaction = MutableStateFlow(Faction.GANG)
  val selectedFaction: StateFlow<Faction> = _selectedFaction.asStateFlow()

  fun toggleOfflineVsComputerMode() {
    val next = !_isOfflineVsComputerMode.value
    _isOfflineVsComputerMode.value = next
    prefs.edit().putBoolean("offline_vs_computer_mode", next).apply()
  }

  fun saveHighScoreToRoom(
    playerName: String,
    score: Int,
    mode: String = "GTA_SANAA_7D",
    difficulty: String = "NORMAL",
    titleAr: String = "بطل أزقة صنعاء",
    rankEmoji: String = "🥇",
    coinsEarned: Int = 0,
    chaseTimeSeconds: Float = 0f,
    stageName: String = "باب اليمن",
    isPersonalBest: Boolean = false
  ) {
    CoroutineScope(Dispatchers.IO).launch {
      highScoreDao.insertHighScore(
        HighScoreEntity(
          playerName = playerName,
          score = score,
          mode = mode,
          difficulty = difficulty,
          dateEpoch = System.currentTimeMillis(),
          titleAr = titleAr,
          rankBadgeEmoji = rankEmoji,
          coinsEarned = coinsEarned,
          chaseTimeSeconds = chaseTimeSeconds,
          stageName = stageName,
          isPersonalBest = isPersonalBest
        )
      )
    }
  }

  suspend fun isNewPersonalBest(score: Int): Boolean {
    val currentHigh = _stats.value.highChaseScore
    val dbHigh = highScoreDao.getGlobalHighScore() ?: 0
    return score > maxOf(currentHigh, dbHigh)
  }

  /**
   * Activates Admin privileges for player mazengalab.
   * Unlocks all features, powers, weapons, specialists, upgrades, and unlimited coins.
   */
  fun activateAdminMazengalab(): Boolean {
    // 1. Unlimited Coins (99,999,999 YER) and Max Level (10)
    _stats.update { current ->
      val fullCoins = 99999999
      val maxLevelXp = 9999
      prefs.edit()
        .putInt("total_coins", fullCoins)
        .putInt("player_xp", maxLevelXp)
        .putInt("player_level", 10)
        .putInt("successful_missions_count", 10)
        .apply()
      current.copy(
        totalCoins = fullCoins,
        recruitedScouts = 99,
        rescuedOfficers = 50,
        tacticsMissionsWon = 99,
        successfulMissionsCount = 10,
        playerXp = maxLevelXp,
        playerLevel = 10
      )
    }

    // 2. Unlock all armory items
    val allArmoryIds = GameData.armoryItems.map { it.id }.toSet()
    _ownedArmoryItemIds.value = allArmoryIds
    prefs.edit().putStringSet("owned_armory_items", allArmoryIds).apply()

    // 3. Unlock all specialists
    val allSpecialists = setOf("SPEED_SCOUT", "ROOFTOP_SNIPER", "CARJACKER_MECHANIC", "DISTRACTOR")
    _hiredSpecialistIds.value = allSpecialists
    prefs.edit().putStringSet("hired_specialists", allSpecialists).apply()

    // 4. Unlock all stages (1, 2, 3, 4, 5)
    val allStages = setOf(1, 2, 3, 4, 5)
    _completedStageIndexes.value = allStages
    prefs.edit().putStringSet("completed_stages", allStages.map { it.toString() }.toSet()).apply()

    // 5. Maximize all headquarters upgrades
    val maxedUpgrades = _upgrades.value.map { it.copy(level = it.maxLevel) }
    _upgrades.value = maxedUpgrades

    _isDeveloperModeActive.value = true
    _adminPlayerName.value = "mazengalab"
    prefs.edit()
      .putBoolean("is_dev_mode_active", true)
      .putString("admin_player_name", "mazengalab")
      .apply()

    HapticManager.vibrateSuccess()
    soundManager.playScoreIncreaseSound(com.example.sound.ScoreSoundType.HIGH_SCORE_FANFARE)
    return true
  }

  fun activateDeveloperAccount(email: String, pass: String): Boolean {
    val cleanEmail = email.trim()
    val cleanPass = pass.trim()
    if (cleanEmail.equals("mazengalab", ignoreCase = true) ||
        cleanPass.equals("mazengalab", ignoreCase = true) ||
        (cleanEmail.equals("alaneedone@gmail.com", ignoreCase = true) && (cleanPass == "mazen27" || cleanPass == "mazengalab"))
    ) {
      return activateAdminMazengalab()
    }
    return false
  }

  private fun loadOwnedArmoryItems(): Set<String> {
    val saved = prefs.getStringSet("owned_armory_items", setOf("banana_peels", "stone_sling", "spray_graffiti")) ?: setOf("banana_peels", "stone_sling", "spray_graffiti")
    return saved
  }

  private fun loadHiredSpecialists(): Set<String> {
    val saved = prefs.getStringSet("hired_specialists", setOf("SPEED_SCOUT", "ROOFTOP_SNIPER")) ?: setOf("SPEED_SCOUT", "ROOFTOP_SNIPER")
    return saved
  }

  private fun loadCompletedStages(): Set<Int> {
    // First 3 stages are free and unlocked by default for simple entry!
    val saved = prefs.getStringSet("completed_stages", setOf("1", "2", "3")) ?: setOf("1", "2", "3")
    val loaded = saved.mapNotNull { it.toIntOrNull() }.toSet()
    return loaded.ifEmpty { setOf(1, 2, 3) }
  }

  fun buyArmoryItem(itemId: String, cost: Int): Boolean {
    val currentCoins = _stats.value.totalCoins
    if (currentCoins < cost || _ownedArmoryItemIds.value.contains(itemId)) return false
    val updated = _ownedArmoryItemIds.value + itemId
    _ownedArmoryItemIds.value = updated
    prefs.edit().putStringSet("owned_armory_items", updated).apply()
    addCoins(-cost)
    return true
  }

  fun hireSpecialist(specialistType: String, cost: Int): Boolean {
    val currentCoins = _stats.value.totalCoins
    if (currentCoins < cost || _hiredSpecialistIds.value.contains(specialistType)) return false
    val updated = _hiredSpecialistIds.value + specialistType
    _hiredSpecialistIds.value = updated
    prefs.edit().putStringSet("hired_specialists", updated).apply()
    addCoins(-cost)
    return true
  }

  fun markStageCompleted(stageIndex: Int, rewardCoins: Int) {
    val updated = _completedStageIndexes.value + stageIndex
    _completedStageIndexes.value = updated
    prefs.edit().putStringSet("completed_stages", updated.map { it.toString() }.toSet()).apply()
    addCoins(rewardCoins)
    addXp(300)
    recordSuccessfulMission()
  }

  /**
   * Records a successfully completed mission in the alleys of Sana'a.
   * Increments successfulMissionsCount and calculates if a new Hero Progression tier is unlocked.
   */
  fun recordSuccessfulMission(): Pair<com.example.model.HeroProgressionTier, Boolean> {
    var isNewTier = false
    val devMode = _isDeveloperModeActive.value
    var resultingTier = com.example.model.SanaaHeroProgression.getTier(0, devMode)

    _stats.update { current ->
      val newCount = current.successfulMissionsCount + 1
      val oldTier = com.example.model.SanaaHeroProgression.getTier(current.successfulMissionsCount, devMode)
      val newTier = com.example.model.SanaaHeroProgression.getTier(newCount, devMode)

      if (newTier.tierIndex > oldTier.tierIndex) {
        isNewTier = true
      }
      resultingTier = newTier

      prefs.edit().putInt("successful_missions_count", newCount).apply()
      current.copy(successfulMissionsCount = newCount)
    }

    return Pair(resultingTier, isNewTier)
  }

  private fun loadStats(): GameStats {
    val totalXp = prefs.getInt("player_xp", 0)
    val levelInfo = PlayerLevelSystem.getLevelInfo(totalXp)
    return GameStats(
      totalCoins = prefs.getInt("total_coins", 1500),
      highChaseScore = prefs.getInt("high_chase_score", 0),
      highVehicleScore = prefs.getInt("high_vehicle_score", 0),
      tacticsMissionsWon = prefs.getInt("tactics_missions_won", 0),
      successfulMissionsCount = prefs.getInt("successful_missions_count", 0),
      recruitedScouts = prefs.getInt("recruited_scouts", 6),
      rescuedOfficers = prefs.getInt("rescued_officers", 0),
      soundEnabled = prefs.getBoolean("sound_enabled", true),
      playerLevel = levelInfo.currentLevel,
      playerXp = totalXp
    )
  }

  fun setFaction(faction: Faction) {
    _selectedFaction.value = faction
  }

  fun addCoins(amount: Int) {
    _stats.update { current ->
      val newTotal = (current.totalCoins + amount).coerceAtLeast(0)
      prefs.edit().putInt("total_coins", newTotal).apply()
      current.copy(totalCoins = newTotal)
    }
  }

  fun addXp(amount: Int): Boolean {
    var leveledUp = false
    _stats.update { current ->
      val newXp = (current.playerXp + amount).coerceAtLeast(0)
      val info = PlayerLevelSystem.getLevelInfo(newXp)
      if (info.currentLevel > current.playerLevel) {
        leveledUp = true
      }
      prefs.edit()
        .putInt("player_xp", newXp)
        .putInt("player_level", info.currentLevel)
        .apply()
      current.copy(playerXp = newXp, playerLevel = info.currentLevel)
    }
    return leveledUp
  }

  fun recordChaseScore(score: Int, coinsEarned: Int, xpEarned: Int = 0) {
    _stats.update { current ->
      val newHigh = maxOf(current.highChaseScore, score)
      val newCoins = current.totalCoins + coinsEarned
      val calculatedXp = if (xpEarned > 0) xpEarned else (score / 20 + coinsEarned / 2).coerceAtLeast(50)
      val newXp = current.playerXp + calculatedXp
      val levelInfo = PlayerLevelSystem.getLevelInfo(newXp)
      prefs.edit()
        .putInt("high_chase_score", newHigh)
        .putInt("total_coins", newCoins)
        .putInt("player_xp", newXp)
        .putInt("player_level", levelInfo.currentLevel)
        .apply()
      current.copy(
        highChaseScore = newHigh,
        totalCoins = newCoins,
        playerXp = newXp,
        playerLevel = levelInfo.currentLevel
      )
    }
  }

  fun recordVehicleScore(score: Int, coinsEarned: Int) {
    _stats.update { current ->
      val newHigh = maxOf(current.highVehicleScore, score)
      val newCoins = current.totalCoins + coinsEarned
      val xp = (score / 25 + coinsEarned / 3).coerceAtLeast(40)
      val newXp = current.playerXp + xp
      val levelInfo = PlayerLevelSystem.getLevelInfo(newXp)
      prefs.edit()
        .putInt("high_vehicle_score", newHigh)
        .putInt("total_coins", newCoins)
        .putInt("player_xp", newXp)
        .putInt("player_level", levelInfo.currentLevel)
        .apply()
      current.copy(
        highVehicleScore = newHigh,
        totalCoins = newCoins,
        playerXp = newXp,
        playerLevel = levelInfo.currentLevel
      )
    }
  }

  fun recordTacticsVictory(asFaction: Faction, coinsEarned: Int) {
    _stats.update { current ->
      val newTacticsWon = current.tacticsMissionsWon + 1
      val newScouts = if (asFaction == Faction.GANG) current.recruitedScouts + 2 else current.recruitedScouts
      val newRescued = if (asFaction == Faction.POLICE) current.rescuedOfficers + 1 else current.rescuedOfficers
      val newCoins = current.totalCoins + coinsEarned
      val newXp = current.playerXp + 200
      val levelInfo = PlayerLevelSystem.getLevelInfo(newXp)
      prefs.edit()
        .putInt("tactics_missions_won", newTacticsWon)
        .putInt("recruited_scouts", newScouts)
        .putInt("rescued_officers", newRescued)
        .putInt("total_coins", newCoins)
        .putInt("player_xp", newXp)
        .putInt("player_level", levelInfo.currentLevel)
        .apply()
      current.copy(
        tacticsMissionsWon = newTacticsWon,
        recruitedScouts = newScouts,
        rescuedOfficers = newRescued,
        totalCoins = newCoins,
        playerXp = newXp,
        playerLevel = levelInfo.currentLevel
      )
    }
  }

  fun buyUpgrade(upgradeId: String): Boolean {
    val currentUpgrades = _upgrades.value
    val upgrade = currentUpgrades.find { it.id == upgradeId } ?: return false
    val currentCoins = _stats.value.totalCoins

    if (upgrade.level >= upgrade.maxLevel || currentCoins < upgrade.cost) {
      return false
    }

    val newCost = (upgrade.cost * 1.5).toInt()
    val updatedUpgrade = upgrade.copy(
      level = upgrade.level + 1,
      cost = newCost
    )

    _upgrades.value = currentUpgrades.map { if (it.id == upgradeId) updatedUpgrade else it }
    addCoins(-upgrade.cost)
    return true
  }

  fun toggleSound() {
    _stats.update { current ->
      val next = !current.soundEnabled
      prefs.edit().putBoolean("sound_enabled", next).apply()
      current.copy(soundEnabled = next)
    }
  }
}
