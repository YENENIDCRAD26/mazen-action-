package com.example.ui.sanaa7d

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PlayerPreferencesDataStore
import com.example.data.SanaGameRepository
import com.example.model.LevelRank
import com.example.model.PlayerLevelInfo
import com.example.model.PlayerLevelSystem
import com.example.model.XpBreakdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlayerStealthMode(
  val titleAr: String,
  val subtitleAr: String,
  val iconEmoji: String,
  val isHiding: Boolean
) {
  HIDDEN(
    titleAr = "مختبئ في أزقة صنعاء",
    subtitleAr = "خط نظر الشرطة مكسور • أمان نسبي",
    iconEmoji = "🥷",
    isHiding = true
  ),
  EXPOSED(
    titleAr = "مكشوف للدوريات والأعين",
    subtitleAr = "في نطاق الرؤية • استعد للمناورة والفرار",
    iconEmoji = "🚨",
    isHiding = false
  )
}

enum class GameDifficulty(
  val titleAr: String,
  val subtitleAr: String,
  val badgeEmoji: String,
  val policeSpeedMultiplier: Float,
  val policeDetectionRadius: Float,
  val scoreMultiplier: Float,
  val descriptionAr: String
) {
  EASY(
    titleAr = "حارة هادئة (مبتدئ)",
    subtitleAr = "دوريات بطيئة ورؤية محدودة",
    badgeEmoji = "🟢",
    policeSpeedMultiplier = 0.85f,
    policeDetectionRadius = 3.5f,
    scoreMultiplier = 1.0f,
    descriptionAr = "سرعة الشرطة منخفضة وتسامح أعلى في الاصطدام"
  ),
  NORMAL(
    titleAr = "سوق الملح (متمرس)",
    subtitleAr = "دوريات منتظمة وحركة واقعية",
    badgeEmoji = "🟡",
    policeSpeedMultiplier = 1.15f,
    policeDetectionRadius = 4.8f,
    scoreMultiplier = 1.35f,
    descriptionAr = "الوتيرة القياسية لمطاردات أزقة صنعاء القديمة"
  ),
  HARD(
    titleAr = "طوارئ باب اليمن (محترف)",
    subtitleAr = "شرطة سريعة وانتشار مكثف",
    badgeEmoji = "🔴",
    policeSpeedMultiplier = 1.55f,
    policeDetectionRadius = 6.2f,
    scoreMultiplier = 1.8f,
    descriptionAr = "استجابة سريعة من الشرطة ومناورات ضيقة"
  ),
  NIGHTMARE_SANAA(
    titleAr = "كابوس الأزقة الصنعانية (أسطوري)",
    subtitleAr = "مطاردة شرسة وسرعة فائقة",
    badgeEmoji = "🔥",
    policeSpeedMultiplier = 2.05f,
    policeDetectionRadius = 8.0f,
    scoreMultiplier = 2.5f,
    descriptionAr = "سرعة جنونية للشرطة ومكافآت نقاط مضاعفة"
  )
}

data class ChaseCountdownState(
  val timeLeftSeconds: Int = 300,
  val totalDurationSeconds: Int = 300,
  val isLowTime: Boolean = false,
  val progressRatio: Float = 1.0f,
  val formattedTime: String = "05:00"
)

data class CollisionFeedbackState(
  val isColliding: Boolean = false,
  val collisionType: String = "",
  val messageAr: String = "",
  val damagePenalty: Int = 0,
  val timestamp: Long = 0L
)

data class ChaseScoreState(
  val currentScore: Int = 0,
  val highChaseScore: Int = 0,
  val comboMultiplier: Float = 1.0f,
  val evasionStreak: Int = 0,
  val stuntsCompleted: Int = 0,
  val coinsCollected: Int = 0,
  val recentScoreGain: Int? = null,
  val scoreReasonAr: String? = null
)

data class LevelProgressionUiState(
  val totalXp: Int = 0,
  val levelInfo: PlayerLevelInfo = PlayerLevelSystem.getLevelInfo(0),
  val animatedProgress: Float = 0f,
  val recentXpNotice: String? = null,
  val recentLeveledUpRank: LevelRank? = null,
  val lastXpBreakdown: XpBreakdown? = null
)

data class AdaptivePursuitState(
  val completedLevelsCount: Int = 0,
  val baseSpeedMultiplier: Float = 1.0f,
  val adaptiveBonusMultiplier: Float = 1.0f,
  val effectivePolicePursuitSpeed: Float = 1.15f,
  val policeDetectionRadiusMultiplier: Float = 1.0f,
  val tierTitleAr: String = "مطاردة اعتيادية",
  val tierBadgeEmoji: String = "⚡",
  val descriptionAr: String = "سرعة واستجابة دوريات الشرطة تتكيف تلقائياً مع عدد المراحل التي أنجزتها"
)

class ChaseGameViewModel @JvmOverloads constructor(
  application: Application,
  private val repository: SanaGameRepository = SanaGameRepository(application),
  private val dataStore: PlayerPreferencesDataStore = PlayerPreferencesDataStore(application)
) : AndroidViewModel(application) {

  private val _stealthState = MutableStateFlow(PlayerStealthMode.EXPOSED)
  val stealthState: StateFlow<PlayerStealthMode> = _stealthState.asStateFlow()

  private val _difficultyState = MutableStateFlow(GameDifficulty.NORMAL)
  val difficultyState: StateFlow<GameDifficulty> = _difficultyState.asStateFlow()

  private val _adaptivePursuitState = MutableStateFlow(
    calculateAdaptiveState(
      completedCount = repository.completedStageIndexes.value.size,
      baseDifficulty = GameDifficulty.NORMAL
    )
  )
  val adaptivePursuitState: StateFlow<AdaptivePursuitState> = _adaptivePursuitState.asStateFlow()

  private val _countdownState = MutableStateFlow(ChaseCountdownState())
  val countdownState: StateFlow<ChaseCountdownState> = _countdownState.asStateFlow()

  private val _scoreState = MutableStateFlow(ChaseScoreState())
  val scoreState: StateFlow<ChaseScoreState> = _scoreState.asStateFlow()

  private val _collisionFeedback = MutableStateFlow(CollisionFeedbackState())
  val collisionFeedback: StateFlow<CollisionFeedbackState> = _collisionFeedback.asStateFlow()

  private val _levelProgressionState = MutableStateFlow(
    LevelProgressionUiState(
      totalXp = repository.stats.value.playerXp,
      levelInfo = PlayerLevelSystem.getLevelInfo(repository.stats.value.playerXp),
      animatedProgress = PlayerLevelSystem.getLevelInfo(repository.stats.value.playerXp).progressRatio
    )
  )
  val levelProgressionState: StateFlow<LevelProgressionUiState> = _levelProgressionState.asStateFlow()

  init {
    // Observe DataStore for player XP and high score persistence synchronization
    viewModelScope.launch {
      dataStore.playerProgressFlow.collect { progress ->
        _scoreState.update { it.copy(highChaseScore = progress.highChaseScore) }
        if (progress.playerXp > _levelProgressionState.value.totalXp) {
          val info = PlayerLevelSystem.getLevelInfo(progress.playerXp)
          _levelProgressionState.update { current ->
            current.copy(
              totalXp = progress.playerXp,
              levelInfo = info,
              animatedProgress = info.progressRatio
            )
          }
        }
      }
    }

    // Observe completed stages to dynamically adapt police pursuit speed
    viewModelScope.launch {
      repository.completedStageIndexes.collect { completedSet ->
        updateAdaptivePursuitDifficulty(completedSet.size, _difficultyState.value)
      }
    }
  }

  fun setDifficulty(difficulty: GameDifficulty) {
    _difficultyState.value = difficulty
    updateAdaptivePursuitDifficulty(_adaptivePursuitState.value.completedLevelsCount, difficulty)
  }

  fun syncCompletedLevelsCount(completedCount: Int) {
    updateAdaptivePursuitDifficulty(completedCount, _difficultyState.value)
  }

  private fun updateAdaptivePursuitDifficulty(completedCount: Int, difficulty: GameDifficulty) {
    _adaptivePursuitState.value = calculateAdaptiveState(completedCount, difficulty)
  }

  companion object {
    fun provideFactory(
      application: Application,
      repository: SanaGameRepository = SanaGameRepository(application),
      dataStore: PlayerPreferencesDataStore = PlayerPreferencesDataStore(application)
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChaseGameViewModel(application, repository, dataStore) as T
      }
    }

    fun calculateAdaptiveState(completedCount: Int, baseDifficulty: GameDifficulty): AdaptivePursuitState {
      val adaptiveBonus = (1.0f + (completedCount * 0.18f)).coerceIn(1.0f, 2.6f)
      val effectiveSpeed = baseDifficulty.policeSpeedMultiplier * adaptiveBonus
      val detectionRadius = (1.0f + (completedCount * 0.12f)).coerceIn(1.0f, 2.2f)

      val (tierTitle, tierBadge, desc) = when {
        completedCount == 0 -> Triple(
          "مطاردة استطلاعية (0 مراحل)",
          "🟢",
          "سرعة الشرطة اعتيادية ومناسبة للاستكشاف الأولي لأزقة صنعاء"
        )
        completedCount in 1..2 -> Triple(
          "استنفار دوريات متوسط (+${((adaptiveBonus - 1f) * 100).toInt()}%)",
          "🟡",
          "تم إنجاز $completedCount مراحل: زادت سرعة استجابة دوريات الشرطة بنسبة ${((adaptiveBonus - 1f) * 100).toInt()}%"
        )
        completedCount in 3..4 -> Triple(
          "طوارئ أمنية عليا (+${((adaptiveBonus - 1f) * 100).toInt()}%)",
          "🔴",
          "تم إنجاز $completedCount مراحل: استجابة فائقة وانتشار مكثف لدوريات نجدة صنعاء"
        )
        else -> Triple(
          "كابوس الأزقة الصنعانية (+${((adaptiveBonus - 1f) * 100).toInt()}%)",
          "🔥",
          "مستوى احترافي أسطوري: سرعة ومناورات شرطية قصوى ومكافآت نقاط مضاعفة"
        )
      }

      return AdaptivePursuitState(
        completedLevelsCount = completedCount,
        baseSpeedMultiplier = baseDifficulty.policeSpeedMultiplier,
        adaptiveBonusMultiplier = adaptiveBonus,
        effectivePolicePursuitSpeed = effectiveSpeed,
        policeDetectionRadiusMultiplier = detectionRadius,
        tierTitleAr = tierTitle,
        tierBadgeEmoji = tierBadge,
        descriptionAr = desc
      )
    }
  }

  fun setStealth(isHiding: Boolean) {
    _stealthState.value = if (isHiding) PlayerStealthMode.HIDDEN else PlayerStealthMode.EXPOSED
  }

  fun updateCountdown(secondsRemaining: Int, totalSeconds: Int = 300) {
    val clamped = secondsRemaining.coerceAtLeast(0)
    val total = if (totalSeconds > 0) totalSeconds else 300
    val ratio = (clamped.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val minutes = clamped / 60
    val seconds = clamped % 60
    val formatted = String.format("%02d:%02d", minutes, seconds)
    val isLow = clamped <= 15

    _countdownState.value = ChaseCountdownState(
      timeLeftSeconds = clamped,
      totalDurationSeconds = total,
      isLowTime = isLow,
      progressRatio = ratio,
      formattedTime = formatted
    )
  }

  /**
   * Score Management Logic
   */
  fun addScorePoints(basePoints: Int, reasonAr: String) {
    val difficultyMultiplier = _difficultyState.value.scoreMultiplier
    val combo = _scoreState.value.comboMultiplier
    val finalPoints = (basePoints * difficultyMultiplier * combo).toInt().coerceAtLeast(1)

    _scoreState.update { current ->
      val newScore = current.currentScore + finalPoints
      val newStreak = current.evasionStreak + 1
      val newCombo = (1.0f + (newStreak * 0.1f)).coerceAtMost(3.5f)
      current.copy(
        currentScore = newScore,
        evasionStreak = newStreak,
        comboMultiplier = newCombo,
        recentScoreGain = finalPoints,
        scoreReasonAr = reasonAr
      )
    }
  }

  fun recordStunt(stuntNameAr: String, points: Int = 150) {
    _scoreState.update { it.copy(stuntsCompleted = it.stuntsCompleted + 1) }
    addScorePoints(points, "حركة بهلوانية: $stuntNameAr")
  }

  fun recordCoinCollected(coinValue: Int = 25) {
    _scoreState.update { it.copy(coinsCollected = it.coinsCollected + 1) }
    addScorePoints(coinValue, "التقاط غنيمة عتيقة 🪙")
  }

  fun triggerCollisionFeedback(collisionType: String, messageAr: String, damagePenalty: Int = 50) {
    _collisionFeedback.value = CollisionFeedbackState(
      isColliding = true,
      collisionType = collisionType,
      messageAr = messageAr,
      damagePenalty = damagePenalty,
      timestamp = System.currentTimeMillis()
    )

    // Reset combo streak on collision with police
    _scoreState.update { current ->
      current.copy(
        comboMultiplier = 1.0f,
        evasionStreak = 0,
        currentScore = (current.currentScore - damagePenalty).coerceAtLeast(0)
      )
    }
  }

  fun clearCollisionFeedback() {
    _collisionFeedback.update { it.copy(isColliding = false) }
  }

  fun clearRecentScoreGain() {
    _scoreState.update { it.copy(recentScoreGain = null, scoreReasonAr = null) }
  }

  fun resetSessionScore() {
    _scoreState.update {
      it.copy(
        currentScore = 0,
        comboMultiplier = 1.0f,
        evasionStreak = 0,
        stuntsCompleted = 0,
        coinsCollected = 0,
        recentScoreGain = null,
        scoreReasonAr = null
      )
    }
  }

  /**
   * Progression & XP
   */
  fun grantInGameXp(amount: Int, reason: String) {
    val previousTotalXp = _levelProgressionState.value.totalXp
    val newTotalXp = previousTotalXp + amount
    val previousLevel = _levelProgressionState.value.levelInfo.currentLevel
    val newLevelInfo = PlayerLevelSystem.getLevelInfo(newTotalXp)

    val leveledUpRank = if (newLevelInfo.currentLevel > previousLevel) {
      PlayerLevelSystem.ranks.find { it.level == newLevelInfo.currentLevel }
    } else null

    _levelProgressionState.update { current ->
      current.copy(
        totalXp = newTotalXp,
        levelInfo = newLevelInfo,
        animatedProgress = newLevelInfo.progressRatio,
        recentXpNotice = "+$amount XP ($reason)",
        recentLeveledUpRank = leveledUpRank ?: current.recentLeveledUpRank
      )
    }

    viewModelScope.launch {
      dataStore.savePlayerProgress(
        xp = newTotalXp,
        level = newLevelInfo.currentLevel,
        coins = repository.stats.value.totalCoins
      )
      repository.addXp(amount)
    }
  }

  fun completeMission(
    isVictory: Boolean,
    score: Int,
    copsEvaded: Int,
    rooftopsCleared: Int,
    thugsCaptured: Int,
    distanceCovered: Float,
    stageRewardCoins: Int
  ): XpBreakdown {
    val breakdown = PlayerLevelSystem.calculateChaseXp(
      isVictory = isVictory,
      score = score,
      copsEvaded = copsEvaded,
      rooftopsCleared = rooftopsCleared,
      thugsCaptured = thugsCaptured,
      distanceCovered = distanceCovered
    )

    val previousXp = _levelProgressionState.value.totalXp
    val newTotalXp = previousXp + breakdown.totalXpEarned
    val previousLevel = _levelProgressionState.value.levelInfo.currentLevel
    val newLevelInfo = PlayerLevelSystem.getLevelInfo(newTotalXp)

    val leveledUpRank = if (newLevelInfo.currentLevel > previousLevel) {
      PlayerLevelSystem.ranks.find { it.level == newLevelInfo.currentLevel }
    } else null

    _levelProgressionState.update { current ->
      current.copy(
        totalXp = newTotalXp,
        levelInfo = newLevelInfo,
        animatedProgress = newLevelInfo.progressRatio,
        lastXpBreakdown = breakdown,
        recentLeveledUpRank = leveledUpRank
      )
    }

    val coinsToReward = if (isVictory) stageRewardCoins else 0
    viewModelScope.launch {
      dataStore.recordMissionVictory(
        xpEarned = breakdown.totalXpEarned,
        coinsEarned = coinsToReward,
        newScore = score
      )
      repository.recordChaseScore(score, coinsToReward, breakdown.totalXpEarned)
      if (isVictory) {
        repository.recordSuccessfulMission()
      }
    }

    return breakdown
  }

  fun completeMission(totalXpEarned: Int, isVictory: Boolean) {
    val previousXp = _levelProgressionState.value.totalXp
    val newTotalXp = previousXp + totalXpEarned
    val previousLevel = _levelProgressionState.value.levelInfo.currentLevel
    val newLevelInfo = PlayerLevelSystem.getLevelInfo(newTotalXp)

    val leveledUpRank = if (newLevelInfo.currentLevel > previousLevel) {
      PlayerLevelSystem.ranks.find { it.level == newLevelInfo.currentLevel }
    } else null

    _levelProgressionState.update { current ->
      current.copy(
        totalXp = newTotalXp,
        levelInfo = newLevelInfo,
        animatedProgress = newLevelInfo.progressRatio,
        recentLeveledUpRank = leveledUpRank
      )
    }

    viewModelScope.launch {
      dataStore.savePlayerProgress(
        xp = newTotalXp,
        level = newLevelInfo.currentLevel,
        coins = repository.stats.value.totalCoins
      )
      if (isVictory) {
        repository.recordSuccessfulMission()
      }
    }
  }

  fun clearRecentXpNotice() {
    _levelProgressionState.update { it.copy(recentXpNotice = null) }
  }

  fun dismissLevelUpDialog() {
    _levelProgressionState.update { it.copy(recentLeveledUpRank = null) }
  }
}

