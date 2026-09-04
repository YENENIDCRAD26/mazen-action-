package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameProgressManager
import com.example.data.SanaGameRepository
import com.example.model.Faction
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Character selectable in the main game screen
 */
data class PlayableCharacter(
  val id: String,
  val nameAr: String,
  val titleAr: String,
  val faction: Faction,
  val avatarEmoji: String,
  val descriptionAr: String,
  val speedStat: Int,
  val stealthStat: Int,
  val combatStat: Int,
  val signatureAbilityAr: String
)

val DEFAULT_PLAYABLE_CHARACTERS = listOf(
  PlayableCharacter(
    id = "mazen_leader",
    nameAr = "مازن غلاب (الزعيم)",
    titleAr = "قائد المشاغبين - بطل أزقة صنعاء",
    faction = Faction.GANG,
    avatarEmoji = "👑",
    descriptionAr = "خبير الأزقة والقفز بين أسطح المنازل، يرتدي القميص الأخضر وقبعة الكاوبوي ويمتلك سرعة مباغتة",
    speedStat = 95,
    stealthStat = 90,
    combatStat = 92,
    signatureAbilityAr = "هروب الأسطح السريع والمراوغة ورشاش Tec-9"
  ),
  PlayableCharacter(
    id = "faris_parkour",
    nameAr = "فارس المتسلق (الباركور)",
    titleAr = "قناص الأسطح ونوافذ القمريات",
    faction = Faction.GANG,
    avatarEmoji = "🧗‍♂️",
    descriptionAr = "بطل الباركور والقفزات البهلوانية العالية واعتلاء المباني الأثرية بدقة المقلاع الحجري",
    speedStat = 98,
    stealthStat = 92,
    combatStat = 80,
    signatureAbilityAr = "قفزات بهلوانية فائقة وتصويب مقلاع دقيق"
  ),
  PlayableCharacter(
    id = "ammar_driver",
    nameAr = "عمار سائق الدباب (الهجولة)",
    titleAr = "خبير خطف المركبات والتفحيط",
    faction = Faction.GANG,
    avatarEmoji = "🚐",
    descriptionAr = "متخصص في تشغيل دبابات سوزوكي واقتحام المركبات وتفعيل النيترو للتفحيط في جولات صنعاء",
    speedStat = 92,
    stealthStat = 75,
    combatStat = 85,
    signatureAbilityAr = "قيادة فورية للمركبات وسرعة نيترو مضاعفة"
  ),
  PlayableCharacter(
    id = "salem_sniper",
    nameAr = "سالم القناص (التمويه)",
    titleAr = "مسؤول التمويه ورذاذ الغرافيتي",
    faction = Faction.GANG,
    avatarEmoji = "🎒",
    descriptionAr = "خبير التشتيت وصاحب الستار الدخاني ورذاذ الغرافيتي ومفرقعات الطماق لتعطيل دوريات الأمن",
    speedStat = 88,
    stealthStat = 96,
    combatStat = 78,
    signatureAbilityAr = "ستار دخاني وغرافيتي وتشتيت دوريات الشرطة"
  )
)

/**
 * UserViewModel that checks for the 'mazengalab' username to toggle a state property 'isAdmin'
 * which unlocks all levels and features globally across the app.
 * Also monitors network connectivity and guarantees 100% functional offline play.
 */
class UserViewModel(
  application: Application,
  private val repository: SanaGameRepository,
  private val progressManager: GameProgressManager
) : AndroidViewModel(application) {

  // Current username entered or saved
  private val _username = MutableStateFlow("")
  val username: StateFlow<String> = _username.asStateFlow()

  // Admin state toggled globally when username == 'mazengalab'
  private val _isAdmin = MutableStateFlow(false)
  val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

  // Selected Character for Main Game Screen
  private val _selectedCharacter = MutableStateFlow(DEFAULT_PLAYABLE_CHARACTERS[0])
  val selectedCharacter: StateFlow<PlayableCharacter> = _selectedCharacter.asStateFlow()

  // Selected Level Index (1..5)
  private val _selectedLevel = MutableStateFlow(1)
  val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

  // Unlocked levels combined from DataStore and Admin overrides
  val unlockedLevels: StateFlow<Set<Int>> = combine(
    progressManager.unlockedLevelsFlow,
    _isAdmin
  ) { dataStoreLevels, adminActive ->
    if (adminActive) {
      setOf(1, 2, 3, 4, 5)
    } else {
      dataStoreLevels
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = setOf(1, 2, 3)
  )

  // Network Connectivity Tracking
  private val _isNetworkAvailable = MutableStateFlow(false)
  val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

  // Offline Mode Active flag (default true: bypasses cloud-sync dependencies completely)
  private val _isOfflineMode = MutableStateFlow(true)
  val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  init {
    // Monitor system network changes
    setupNetworkMonitor(application)

    // Check repository state on start
    viewModelScope.launch {
      repository.isDeveloperModeActive.collect { isDev ->
        if (isDev && !_isAdmin.value) {
          _isAdmin.value = true
          _username.value = "mazengalab"
        }
      }
    }
  }

  /**
   * Checks the username. If username matches 'mazengalab' (case-insensitive),
   * sets isAdmin = true, unlocking all levels and features globally across the app.
   */
  fun setUsernameAndCheckAdmin(inputName: String) {
    _username.value = inputName
    val trimmed = inputName.trim()
    val isMatch = trimmed.equals("mazengalab", ignoreCase = true)

    if (isMatch) {
      grantAdminPrivileges()
    } else {
      _isAdmin.value = false
      _statusMessage.value = "مرحباً باللاعب: $trimmed (المراحل 1، 2، 3 مجانية بالكامل)"
    }
  }

  /**
   * Directly activates admin privileges for 'mazengalab'
   */
  fun grantAdminPrivileges() {
    _isAdmin.value = true
    _username.value = "mazengalab"
    _statusMessage.value = "👑 تم تفعيل صلاحيات الأدمن (mazengalab): جميع المراحل والميزات مفتوحة عالمياً!"

    viewModelScope.launch {
      // 1. Unlock all levels in DataStore
      progressManager.unlockAllLevels()
      // 2. Unlock all features & unlimited coins in Repository
      repository.activateAdminMazengalab()
      HapticManager.vibrateSuccess()
      GameSoundEffects.playVictoryFanfare()
    }
  }

  /**
   * Toggles the admin state explicitly
   */
  fun toggleAdmin() {
    if (_isAdmin.value) {
      _isAdmin.value = false
      _statusMessage.value = "تم تعطيل وضع الأدمن"
      viewModelScope.launch {
        progressManager.resetToDefault()
      }
    } else {
      grantAdminPrivileges()
    }
  }

  fun selectCharacter(character: PlayableCharacter) {
    _selectedCharacter.value = character
    repository.setFaction(character.faction)
    HapticManager.vibrateMovement()
    GameSoundEffects.playJump()
  }

  fun selectLevel(level: Int) {
    if (unlockedLevels.value.contains(level) || _isAdmin.value || level <= 3) {
      _selectedLevel.value = level
      HapticManager.vibrateMovement()
      GameSoundEffects.playFootstep()
    } else {
      _statusMessage.value = "🔒 المرحلة $level مقفلة! أكمل المرحلة السابقة أو فعل حساب الأدمن (mazengalab)"
      HapticManager.vibrateCollision()
    }
  }

  fun toggleOfflineMode() {
    _isOfflineMode.value = !_isOfflineMode.value
    repository.toggleOfflineVsComputerMode()
    HapticManager.vibrateMovement()
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  private fun setupNetworkMonitor(context: Context) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    if (connectivityManager == null) {
      _isNetworkAvailable.value = false
      return
    }

    // Initial check
    val activeNetwork = connectivityManager.activeNetwork
    val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
    _isNetworkAvailable.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    // Register callback for changes
    val networkRequest = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    try {
      connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          _isNetworkAvailable.value = true
        }

        override fun onLost(network: Network) {
          _isNetworkAvailable.value = false
        }
      })
    } catch (_: Exception) {
      // Fallback gracefully without throwing
    }
  }
}
