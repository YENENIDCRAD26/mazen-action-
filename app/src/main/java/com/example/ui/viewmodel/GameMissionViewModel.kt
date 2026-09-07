package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.PlayerEscapeSituation
import com.example.ai.SanaaGameAdvisorService
import com.example.ai.TacticalAdvice
import com.example.data.GameMissionRepository
import com.example.data.local.AppDatabase
import com.example.data.local.GameMissionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MissionFilter(val labelAr: String) {
  ALL("جميع المهام"),
  ACTIVE("قيد التنفيذ ⏳"),
  COMPLETED("المكتملة ✅"),
  EASY("سهل 🟢"),
  MEDIUM("متوسط 🟡"),
  HARD("صعب 🔴"),
  LEGENDARY("أسطوري 🟣")
}

data class AiAdvisorUiState(
  val isLoading: Boolean = false,
  val advice: TacticalAdvice? = null,
  val errorMessage: String? = null,
  val activeSituation: PlayerEscapeSituation = PlayerEscapeSituation()
)

class GameMissionViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: GameMissionRepository
  private val advisorService = SanaaGameAdvisorService()

  init {
    val database = AppDatabase.getDatabase(application)
    repository = GameMissionRepository(database.gameMissionDao())
    viewModelScope.launch {
      repository.ensureDefaultMissionsPopulated()
    }
  }

  private val _filter = MutableStateFlow(MissionFilter.ALL)
  val filter: StateFlow<MissionFilter> = _filter

  val allMissions: StateFlow<List<GameMissionEntity>> = repository.allMissions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filteredMissions: StateFlow<List<GameMissionEntity>> = combine(allMissions, _filter) { missions, currentFilter ->
    when (currentFilter) {
      MissionFilter.ALL -> missions
      MissionFilter.ACTIVE -> missions.filter { !it.isCompleted }
      MissionFilter.COMPLETED -> missions.filter { it.isCompleted }
      MissionFilter.EASY -> missions.filter { it.difficulty.contains("سهل") || it.difficulty.equals("EASY", true) }
      MissionFilter.MEDIUM -> missions.filter { it.difficulty.contains("متوسط") || it.difficulty.equals("MEDIUM", true) }
      MissionFilter.HARD -> missions.filter { it.difficulty.contains("صعب") || it.difficulty.equals("HARD", true) }
      MissionFilter.LEGENDARY -> missions.filter { it.difficulty.contains("أسطوري") || it.difficulty.equals("LEGENDARY", true) }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedMission = MutableStateFlow<GameMissionEntity?>(null)
  val selectedMission: StateFlow<GameMissionEntity?> = _selectedMission

  private val _aiAdvisorState = MutableStateFlow(AiAdvisorUiState())
  val aiAdvisorState: StateFlow<AiAdvisorUiState> = _aiAdvisorState

  fun setFilter(newFilter: MissionFilter) {
    _filter.value = newFilter
  }

  fun selectMission(mission: GameMissionEntity?) {
    _selectedMission.value = mission
    if (mission != null) {
      // Auto-configure escape situation from selected mission
      val newSituation = PlayerEscapeSituation(
        location = mission.location,
        wantedStars = when (mission.difficulty) {
          "سهل" -> 1
          "متوسط" -> 2
          "صعب" -> 3
          else -> 4
        },
        policeForces = when (mission.difficulty) {
          "سهل" -> "دورية راجلة واحدة عند زاوية الزقاق"
          "متوسط" -> "دوريتان راجلتان + سيارة شرطة لاندكروزر عند المدخل الرئيسي"
          "صعب" -> "سيارات شرطة رباعية الدفع وحواجز حديدية تطوق المخرج"
          else -> "طوق أمني شامل وقناصة فوق القمريات وكشافات بحث ليلية"
        },
        currentVehicleOrTool = "دباب ياباني سريع + قشور موز ومفرقعات طماق",
        missionContext = "مهمة '${mission.title}': ${mission.description}"
      )
      _aiAdvisorState.value = _aiAdvisorState.value.copy(activeSituation = newSituation)
    }
  }

  fun toggleMissionCompletion(mission: GameMissionEntity) {
    viewModelScope.launch {
      repository.toggleMissionCompletion(mission)
    }
  }

  fun addNewMission(
    title: String,
    description: String,
    difficulty: String,
    location: String,
    rewardCoins: Int,
    iconEmoji: String
  ) {
    viewModelScope.launch {
      val newMission = GameMissionEntity(
        title = title,
        description = description,
        difficulty = difficulty,
        location = location,
        rewardCoins = rewardCoins,
        rewardXp = rewardCoins / 2,
        iconEmoji = iconEmoji,
        isCompleted = false
      )
      repository.addMission(newMission)
    }
  }

  fun deleteMission(id: Long) {
    viewModelScope.launch {
      repository.deleteMission(id)
    }
  }

  fun updateSituation(situation: PlayerEscapeSituation) {
    _aiAdvisorState.value = _aiAdvisorState.value.copy(activeSituation = situation)
  }

  /**
   * Triggers Firebase AI to generate escape tactics for Old Sana'a alleys.
   */
  fun requestEscapeTactics(customSituation: PlayerEscapeSituation? = null) {
    val situation = customSituation ?: _aiAdvisorState.value.activeSituation
    _aiAdvisorState.value = _aiAdvisorState.value.copy(isLoading = true, errorMessage = null)

    viewModelScope.launch {
      try {
        val advice = advisorService.getEscapeTactics(situation)
        _aiAdvisorState.value = _aiAdvisorState.value.copy(
          isLoading = false,
          advice = advice,
          errorMessage = null
        )
      } catch (e: Exception) {
        val fallback = advisorService.generateTacticalFallback(situation)
        _aiAdvisorState.value = _aiAdvisorState.value.copy(
          isLoading = false,
          advice = fallback,
          errorMessage = null
        )
      }
    }
  }

  fun selectPresetScenario(presetIndex: Int) {
    val presets = listOf(
      PlayerEscapeSituation(
        location = "باب اليمن - أزقة الحدادين الضيقة",
        wantedStars = 2,
        policeForces = "دورية راجلة تلاحقني بالصفارات ودورية لاندكروزر عند البوابة الكبرى",
        currentVehicleOrTool = "أركض راجلاً + قشور موز ومقلاع حجري",
        missionContext = "سرقة حقيبة المؤن التكتيكية والهروب نحو أسواق الفضة"
      ),
      PlayerEscapeSituation(
        location = "شارع السايلة التراثي الصخري",
        wantedStars = 3,
        policeForces = "سيارتا شرطة مصفحة تحاصران المجرى من الأمام والخلف",
        currentVehicleOrTool = "دباب ياباني بمحرك تيربو سريع",
        missionContext = "نقل الخريطة السرية عبر السائلة قبل إغلاق جسر شعوب"
      ),
      PlayerEscapeSituation(
        location = "سوق الملح والبهارات المزدحم",
        wantedStars = 4,
        policeForces = "طوق أمني يغلق جميع المنافذ مع إطلاق قنابل غاز مسيل للدموع",
        currentVehicleOrTool = "مفرقعات طماق صوتية + سواتر عربات بطاطس وأكياس حلبة",
        missionContext = "مهمة الهروب الكبير بعد تفكيك حواجز التفتيش"
      ),
      PlayerEscapeSituation(
        location = "حي القاسمي وأسطح القمريات العالية",
        wantedStars = 5,
        policeForces = "استنفار شامل، قناصة فوق أسطح المنازل الطينية وكشافات هليكوبتر",
        currentVehicleOrTool = "مهارات باركور وتسلق الأسطح وجسور الحبال",
        missionContext = "النجاة بكنز قصر غمدان والتسلل نحو بستان السلطان"
      )
    )
    val chosen = presets.getOrElse(presetIndex) { presets[0] }
    _aiAdvisorState.value = _aiAdvisorState.value.copy(activeSituation = chosen)
    requestEscapeTactics(chosen)
  }
}
