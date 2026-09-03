package com.example.ui.tactical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SanaGameRepository
import com.example.model.*
import com.example.sound.GameSoundEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

data class TacticalBattleUiState(
  val activeStageIndex: Int = 1,
  val mapGrid: StrategicMapGrid? = null,
  val units: List<TacticalUnit> = emptyList(),
  val tiles: List<TacticalTile> = emptyList(),
  val currentTurn: TurnState = TurnState.KIDS_TURN,
  val roundNumber: Int = 1,
  val selectedUnitId: String? = null,
  val kidMorale: Int = 100,
  val policeReadiness: Int = 100,
  val tutorialStep: Int = 1,
  val dababCarjacked: Boolean = false,
  val victoryMsg: String? = null,
  val battleLogs: List<String> = emptyList(),
  val detectedKidIds: Set<String> = emptySet(),
  val hiddenKidIds: Set<String> = emptySet(),
  val slippingUnitId: String? = null,
  val isShaking: Boolean = false
)

class TacticalBattleViewModel(
  private val repository: SanaGameRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(TacticalBattleUiState())
  val uiState: StateFlow<TacticalBattleUiState> = _uiState.asStateFlow()

  val gridCols = 6
  val gridRows = 6

  init {
    loadStage(1)
  }

  fun loadStage(stageIdx: Int) {
    val newTiles = mutableListOf<TacticalTile>()
    val newUnits = mutableListOf<TacticalUnit>()
    val hiredSpecialists = repository.hiredSpecialistIds.value

    val primaryZone = when (stageIdx) {
      1 -> MapZoneCategory.OLD_SANAA_NARROW_ALLEY
      2 -> MapZoneCategory.MODERN_OPEN_STREET
      3 -> MapZoneCategory.COVER_FORTIFIED_ZONE
      4 -> MapZoneCategory.OLD_SANAA_NARROW_ALLEY
      else -> MapZoneCategory.MODERN_OPEN_STREET
    }

    val locationName = when (stageIdx) {
      1 -> "أزقة حارة الفليحي - صنعاء القديمة"
      2 -> "جولة كنتاكي وشارع الزبيري"
      3 -> "محيط قسم شرطة صنعاء القديمة"
      4 -> "البيت الطيني المهجور - باب اليمن"
      else -> "طريق مناخة ومخارج العاصمة"
    }

    // Generate grid tiles with specialized terrain
    for (r in 0 until gridRows) {
      for (c in 0 until gridCols) {
        val tileType = when {
          // Stage 1: Tutorial
          stageIdx == 1 && r == 1 && c == 2 -> TacticalTileType.POTATO_CART
          stageIdx == 1 && r == 2 && c == 4 -> TacticalTileType.SPICE_SACK
          stageIdx == 1 && r == 4 && c == 4 -> TacticalTileType.DABAB_BUS
          stageIdx == 1 && r == 5 && c == 5 -> TacticalTileType.EXIT_ZONE
          stageIdx == 1 && (r == 0 && c == 4 || r == 3 && c == 0) -> TacticalTileType.MUD_WALL

          // Stage 2: Kentucky Roundabout
          stageIdx == 2 && r == 0 && c == 2 -> TacticalTileType.MUD_ROOF
          stageIdx == 2 && r == 0 && c == 4 -> TacticalTileType.MUD_ROOF
          stageIdx == 2 && r == 2 && c == 2 -> TacticalTileType.POLICE_BARRICADE
          stageIdx == 2 && r == 3 && c == 3 -> TacticalTileType.POLICE_BARRICADE
          stageIdx == 2 && r == 1 && c == 4 -> TacticalTileType.DABAB_BUS
          stageIdx == 2 && r == 4 && c == 1 -> TacticalTileType.POLICE_CAR
          stageIdx == 2 && r == 5 && c == 5 -> TacticalTileType.EXIT_ZONE
          stageIdx == 2 && r == 2 && c == 0 -> TacticalTileType.OPEN_ASPHALT_ROAD
          stageIdx == 2 && r == 3 && c == 0 -> TacticalTileType.ROUNDABOUT_CENTER

          // Stage 3: Police Station Siege
          stageIdx == 3 && r == 2 && c == 3 -> TacticalTileType.POLICE_HQ_FLAG
          stageIdx == 3 && r == 1 && c == 1 -> TacticalTileType.MUD_WALL
          stageIdx == 3 && r == 4 && c == 4 -> TacticalTileType.MUD_WALL
          stageIdx == 3 && r == 3 && c == 1 -> TacticalTileType.VEGGIE_STALL

          // Stage 4: Mud Tower
          stageIdx == 4 && (r == 1 && c == 2 || r == 3 && c == 4) -> TacticalTileType.MUD_ROOF
          stageIdx == 4 && r == 4 && c == 2 -> TacticalTileType.DABAB_BUS
          stageIdx == 4 && r == 0 && c == 0 -> TacticalTileType.EXIT_ZONE

          // Stage 5: Great Escape
          stageIdx == 5 && r == 0 && c == 5 -> TacticalTileType.EXIT_ZONE
          stageIdx == 5 && r == 2 && c == 1 -> TacticalTileType.DABAB_BUS
          stageIdx == 5 && r == 3 && c == 4 -> TacticalTileType.POLICE_CAR
          else -> if (stageIdx == 2) TacticalTileType.OPEN_ASPHALT_ROAD else TacticalTileType.ALLEY_ROAD
        }
        newTiles.add(TacticalTile(x = c, y = r, tileType = tileType))
      }
    }

    val grid = StrategicMapGrid(
      width = gridCols,
      height = gridRows,
      stageIndex = stageIdx,
      locationNameAr = locationName,
      primaryZone = primaryZone,
      tiles = newTiles
    )

    // Unit Spawns
    when (stageIdx) {
      1 -> {
        newUnits.add(TacticalUnit("boss", "الزعيم الصغير 👑", Faction.GANG, "👑", x = 0, y = 0, ap = 2, maxAp = 2))
        newUnits.add(
          TacticalUnit(
            "assistant",
            "الطفل المساعد 🎒",
            Faction.GANG,
            "🎒",
            x = 1,
            y = 0,
            ap = if (hiredSpecialists.contains("SPEED_SCOUT")) 3 else 2,
            maxAp = if (hiredSpecialists.contains("SPEED_SCOUT")) 3 else 2
          )
        )
        newUnits.add(TacticalUnit("cop1", "شرطي الدورية 👮‍♂️", Faction.POLICE, "👮‍♂️", x = 4, y = 2, ap = 2, maxAp = 2, isOverwatch = false))
      }
      2 -> {
        newUnits.add(TacticalUnit("boss", "الزعيم الصغير 👑", Faction.GANG, "👑", x = 0, y = 1, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("scout", "الطفل الركّيض 🏃‍♂️", Faction.GANG, "🏃‍♂️", x = 0, y = 3, ap = 3, maxAp = 3))
        newUnits.add(TacticalUnit("sniper", "الطفل القناص 🧗‍♂️", Faction.GANG, "🧗‍♂️", x = 1, y = 0, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("cop1", "شرطي المرور 🚦", Faction.POLICE, "🚦", x = 3, y = 2, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("cop2", "دورية التدخل 🚓", Faction.POLICE, "👮‍♂️", x = 5, y = 4, ap = 2, maxAp = 2, isOverwatch = true))
      }
      3 -> {
        newUnits.add(TacticalUnit("boss", "الزعيم الصغير 👑", Faction.GANG, "👑", x = 0, y = 0, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("kid1", "سارق الدبابات 🚐", Faction.GANG, "🚐", x = 0, y = 4, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("kid2", "طفل المفرقعات 🧨", Faction.GANG, "🧨", x = 1, y = 2, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("cop1", "حارس القسم 👮‍♂️", Faction.POLICE, "👮‍♂️", x = 2, y = 3, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("cop2", "ضابط القيادة 🛡️", Faction.POLICE, "🛡️", x = 4, y = 3, ap = 2, maxAp = 2, isOverwatch = true))
      }
      4 -> {
        newUnits.add(TacticalUnit("boss", "الزعيم الصغير 👑", Faction.GANG, "👑", x = 1, y = 0, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("kid1", "نائب الزعيم 🧗‍♂️", Faction.GANG, "🧗‍♂️", x = 3, y = 0, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("hostage_cop", "الضابط المحتجز 🪢", Faction.POLICE, "👮‍♂️", x = 2, y = 3, ap = 0, maxAp = 0, isHostage = true))
        newUnits.add(TacticalUnit("cop_rescue", "قوات الاقتحام 🛡️", Faction.POLICE, "🛡️", x = 5, y = 5, ap = 2, maxAp = 2))
      }
      else -> {
        newUnits.add(TacticalUnit("boss", "الزعيم الصغير 👑", Faction.GANG, "👑", x = 0, y = 2, ap = 2, maxAp = 2))
        newUnits.add(TacticalUnit("driver", "السائق المتهور 🚐", Faction.GANG, "🚐", x = 1, y = 2, ap = 3, maxAp = 3))
        newUnits.add(TacticalUnit("cop1", "دورية حدة 🚓", Faction.POLICE, "👮‍♂️", x = 3, y = 1, ap = 2, maxAp = 2, isOverwatch = true))
        newUnits.add(TacticalUnit("cop2", "شاص التدخل 🚨", Faction.POLICE, "🚨", x = 4, y = 4, ap = 2, maxAp = 2))
      }
    }

    _uiState.update {
      it.copy(
        activeStageIndex = stageIdx,
        mapGrid = grid,
        tiles = newTiles,
        units = newUnits,
        currentTurn = TurnState.KIDS_TURN,
        roundNumber = 1,
        selectedUnitId = newUnits.firstOrNull { u -> u.faction == Faction.GANG }?.id,
        kidMorale = 100,
        policeReadiness = 100,
        tutorialStep = 1,
        dababCarjacked = false,
        victoryMsg = null,
        battleLogs = listOf("بدأت المرحلة ${stageIdx}: ${GameData.tacticalStages.find { s -> s.stageIndex == stageIdx }?.titleAr ?: ""}"),
        slippingUnitId = null,
        isShaking = false
      )
    }

    updateLineOfSight()
  }

  fun selectUnit(unitId: String) {
    _uiState.update { it.copy(selectedUnitId = unitId) }
  }

  /**
   * Calculates Line of Sight between cops and kids using Raycasting / Obstacle detection.
   */
  fun updateLineOfSight() {
    val state = _uiState.value
    val cops = state.units.filter { it.faction == Faction.POLICE && !it.isHostage && !it.isStunned && it.hp > 0 }
    val kids = state.units.filter { it.faction == Faction.GANG && it.hp > 0 }
    val tiles = state.tiles

    val detected = mutableSetOf<String>()
    val hidden = mutableSetOf<String>()

    for (kid in kids) {
      var isSpottedByAnyCop = false
      for (cop in cops) {
        if (hasLineOfSight(cop.x, cop.y, kid.x, kid.y, tiles)) {
          isSpottedByAnyCop = true
          break
        }
      }
      if (isSpottedByAnyCop) {
        detected.add(kid.id)
      } else {
        hidden.add(kid.id)
      }
    }

    _uiState.update {
      it.copy(
        detectedKidIds = detected,
        hiddenKidIds = hidden
      )
    }
  }

  private fun hasLineOfSight(x0: Int, y0: Int, x1: Int, y1: Int, tiles: List<TacticalTile>): Boolean {
    val dist = abs(x1 - x0) + abs(y1 - y0)
    if (dist == 0) return true
    if (dist > 4) return false // Max police visual detection range in alleys

    // Check intermediate points along ray
    val steps = (dist * 2).coerceAtLeast(2)
    for (i in 1 until steps) {
      val t = i.toFloat() / steps
      val checkX = (x0 + t * (x1 - x0)).roundToInt().coerceIn(0, gridCols - 1)
      val checkY = (y0 + t * (y1 - y0)).roundToInt().coerceIn(0, gridRows - 1)

      // Skip origin and destination
      if ((checkX == x0 && checkY == y0) || (checkX == x1 && checkY == y1)) continue

      val tile = tiles.find { it.x == checkX && it.y == checkY }
      if (tile?.tileType?.blocksLineOfSight == true || tile?.tileType?.cover == TacticalCoverType.FULL) {
        return false // Blocked by wall, cart, or barricade
      }
    }
    return true
  }

  /**
   * Moves selected unit and deducts 1 AP (Action Point)
   */
  fun moveUnit(targetX: Int, targetY: Int) {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selUnit.ap <= 0 || state.currentTurn != TurnState.KIDS_TURN) return

    val newUnits = state.units.map { it.copy() }
    val unitToMove = newUnits.find { it.id == selUnit.id } ?: return
    val logs = state.battleLogs.toMutableList()

    // Check Overwatch Interception
    val overwatchCop = newUnits.find { it.faction == Faction.POLICE && it.isOverwatch && (abs(it.x - targetX) + abs(it.y - targetY) <= 2) }
    if (overwatchCop != null) {
      overwatchCop.isOverwatch = false
      unitToMove.hp = (unitToMove.hp - 20).coerceAtLeast(0)
      logs.add("⚠️ رصد Overwatch! ${overwatchCop.nameAr} أطلق طلقة تحذيرية أثناء حركة ${unitToMove.nameAr}!")
      GameSoundEffects.playGunshot()
    }

    unitToMove.x = targetX
    unitToMove.y = targetY
    unitToMove.ap -= 1
    GameSoundEffects.playJump()

    val targetTile = state.tiles.find { it.x == targetX && it.y == targetY }
    if (targetTile?.tileType?.cover == TacticalCoverType.FULL || targetTile?.tileType?.cover == TacticalCoverType.HALF) {
      logs.add("🛡️ ${unitToMove.nameAr} احتمى بنجاح خلف ${targetTile.tileType.nameAr} (${targetTile.tileType.cover.labelAr})")
    } else {
      logs.add("👟 ${unitToMove.nameAr} تحرك إلى مربع (${targetX + 1}, ${targetY + 1}) [خصم 1 AP]")
    }

    var newTutorialStep = state.tutorialStep
    if (state.activeStageIndex == 1 && state.tutorialStep == 1 && targetX == 1 && targetY == 2) {
      newTutorialStep = 2
      logs.add("✨ الخطوة 1 اكتملت! الآن حرك الطفل المساعد لباب الدباب.")
    } else if (state.activeStageIndex == 1 && state.tutorialStep == 2 && targetX == 3 && targetY == 4) {
      newTutorialStep = 3
      logs.add("✨ الخطوة 2 اكتملت! اضغط على 'اقتحام المركبة 🚐' لتشغيل الدباب.")
    }

    var newVictoryMsg = state.victoryMsg
    var newTurn = state.currentTurn

    if (targetTile?.tileType == TacticalTileType.EXIT_ZONE) {
      if (state.activeStageIndex == 1 && state.dababCarjacked) {
        newTurn = TurnState.VICTORY
        newVictoryMsg = "تمت سرقة الدباب الأول بنجاح! السمعة الشارعية +100 والمصروف +300 ريال!"
        GameSoundEffects.playVictoryFanfare()
        repository.markStageCompleted(1, 300)
      } else if (state.activeStageIndex == 2 && state.dababCarjacked) {
        newTurn = TurnState.VICTORY
        newVictoryMsg = "تم اقتحام جولة كنتاكي وتجاوز دوريات التدخل بنجاح! السمعة الشارعية +200 والمصروف +450 ريال!"
        GameSoundEffects.playVictoryFanfare()
        repository.markStageCompleted(2, 450)
      } else if (state.activeStageIndex == 5) {
        newTurn = TurnState.VICTORY
        newVictoryMsg = "نجح الهروب الكبير للزعيم نحو طريق مناخة! انتصار أسطوري!"
        GameSoundEffects.playVictoryFanfare()
        repository.markStageCompleted(5, 1000)
      }
    }

    _uiState.update {
      it.copy(
        units = newUnits,
        battleLogs = logs,
        tutorialStep = newTutorialStep,
        victoryMsg = newVictoryMsg,
        currentTurn = newTurn
      )
    }

    updateLineOfSight()
  }

  /**
   * Carjack Dabab Bus (Deducts 1 AP)
   */
  fun carjackVehicle() {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selUnit.ap < 1 || state.dababCarjacked) return

    val newUnits = state.units.map { if (it.id == selUnit.id) it.copy(ap = it.ap - 1, isInVehicle = true) else it.copy() }
    val logs = state.battleLogs.toMutableList()
    logs.add("🚐 ${selUnit.nameAr} اقتحم باص الدباب وقام بتشغيله بنجاح! المحرك يزمجر! [خصم 1 AP]")
    GameSoundEffects.playNitroBoost()

    var newTutorialStep = state.tutorialStep
    if (state.activeStageIndex == 1 && state.tutorialStep == 3) {
      newTutorialStep = 4
      logs.add("✨ الخطوة 3 اكتملت! اركب الدباب وانطلق نحو نقطة الهروب 🏁.")
    }

    _uiState.update {
      it.copy(
        units = newUnits,
        dababCarjacked = true,
        tutorialStep = newTutorialStep,
        battleLogs = logs
      )
    }
  }

  /**
   * Tactical Abduction (Deducts 2 AP)
   */
  fun abductPolice(targetCopId: String) {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selUnit.ap < 2) return

    val newUnits = state.units.map {
      when (it.id) {
        selUnit.id -> it.copy(ap = 0)
        targetCopId -> it.copy(isHostage = true, hp = 0)
        else -> it.copy()
      }
    }
    val logs = state.battleLogs.toMutableList()
    val targetCop = state.units.find { it.id == targetCopId }
    logs.add("🪢 كمين اختطاف محكم! تم تكبيل ${targetCop?.nameAr ?: "الشرطي"} وسحبه نحو المخبأ السري! [خصم 2 AP]")
    GameSoundEffects.playSiren()

    var newTurn = state.currentTurn
    var newVictory = state.victoryMsg
    if (newUnits.none { it.faction == Faction.POLICE && !it.isHostage }) {
      newTurn = TurnState.VICTORY
      newVictory = "تم أسر جميع أفراد الدورية بنجاح وتأمين المخبأ السري!"
      GameSoundEffects.playVictoryFanfare()
      repository.markStageCompleted(state.activeStageIndex, 500)
    }

    _uiState.update {
      it.copy(
        units = newUnits,
        policeReadiness = (it.policeReadiness - 35).coerceAtLeast(0),
        battleLogs = logs,
        currentTurn = newTurn,
        victoryMsg = newVictory
      )
    }
    updateLineOfSight()
  }

  /**
   * Attack with Slingshot / BB Gun (Deducts 1 AP or 2 AP)
   */
  fun shootPolice(targetCopId: String, isBbGun: Boolean = false) {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    val apCost = if (isBbGun) 2 else 1
    if (selUnit.ap < apCost) return

    val targetCop = state.units.find { it.id == targetCopId } ?: return
    val newUnits = state.units.map { it.copy() }
    val selUnitCopy = newUnits.find { it.id == selUnit.id } ?: return
    val targetCopCopy = newUnits.find { it.id == targetCopId } ?: return
    val logs = state.battleLogs.toMutableList()

    selUnitCopy.ap -= apCost

    val onMudRoof = state.tiles.find { it.x == selUnit.x && it.y == selUnit.y }?.tileType == TacticalTileType.MUD_ROOF
    val hitRate = if (onMudRoof) 100 else 75
    val roll = Random.nextInt(100)

    if (roll <= hitRate) {
      val damage = if (isBbGun) 40 else 25
      targetCopCopy.hp = (targetCopCopy.hp - damage).coerceAtLeast(0)
      targetCopCopy.isOverwatch = false
      if (isBbGun) {
        GameSoundEffects.playGunshot()
        logs.add("🔫 طلقة خرز دقيقة من مسدس الخرز على ${targetCop.nameAr}! [خصم $apCost AP]")
      } else {
        GameSoundEffects.playSlingSnap()
        logs.add("🪨 إصابة مباشرة بحجارة المقلاع على ${targetCop.nameAr}! إلغاء Overwatch. [خصم $apCost AP]")
      }

      if (targetCopCopy.hp <= 0) {
        targetCopCopy.isStunned = true
        logs.add("⭐ تم تحييد ${targetCop.nameAr} بالكامل وإسقاطه أرضاً!")
      }
    } else {
      logs.add("💨 الرمية أخطأت الهدف بسبب احتماء الشرطي! [خصم $apCost AP]")
    }

    _uiState.update {
      it.copy(
        units = newUnits,
        battleLogs = logs
      )
    }
    updateLineOfSight()
  }

  /**
   * Places a fruit peel trap (Deducts 1 AP)
   */
  fun placeBananaTrap(tileX: Int, tileY: Int) {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selUnit.ap < 1) return

    val newUnits = state.units.map { if (it.id == selUnit.id) it.copy(ap = it.ap - 1) else it.copy() }
    val newTiles = state.tiles.map { if (it.x == tileX && it.y == tileY) it.copy(hasBananaTrap = true) else it.copy() }
    val logs = state.battleLogs.toMutableList()
    logs.add("🍌 تم نصب فخ قشور الموز والمانجو في الممر (${tileX + 1}, ${tileY + 1}) [خصم 1 AP]")
    GameSoundEffects.playJump()

    _uiState.update {
      it.copy(
        units = newUnits,
        tiles = newTiles,
        battleLogs = logs
      )
    }
  }

  /**
   * Fireworks Area Attack (Deducts 2 AP)
   */
  fun fireFireworks() {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selUnit.ap < 2) return

    val newUnits = state.units.map {
      if (it.id == selUnit.id) {
        it.copy(ap = it.ap - 2)
      } else if (it.faction == Faction.POLICE) {
        it.copy(isOverwatch = false, hp = (it.hp - 20).coerceAtLeast(0))
      } else {
        it.copy()
      }
    }
    val logs = state.battleLogs.toMutableList()
    logs.add("🧨 تفجير مفرقعات طماق وشنير مساحي! إرباك صفوف الشرطة بالكامل وإلغاء المراقبة! [خصم 2 AP]")
    GameSoundEffects.playFirework()

    _uiState.update {
      it.copy(
        units = newUnits,
        battleLogs = logs
      )
    }
    updateLineOfSight()
  }

  /**
   * Megaphone rally buff (Deducts 2 AP)
   */
  fun shoutMegaphone() {
    val state = _uiState.value
    val selUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selUnit.ap < 2) return

    val newUnits = state.units.map {
      if (it.id == selUnit.id) {
        it.copy(ap = it.ap - 2)
      } else if (it.faction == Faction.GANG) {
        it.copy(ap = it.ap + 1)
      } else {
        it.copy()
      }
    }
    val logs = state.battleLogs.toMutableList()
    logs.add("📢 خطاب الزعيم الصغير بمكبر الصوت: حماس فائق و+1 AP لجميع أطفال العصابة! [خصم 2 AP]")
    GameSoundEffects.playWalkieTalkie()

    _uiState.update {
      it.copy(
        units = newUnits,
        kidMorale = (it.kidMorale + 20).coerceAtMost(100),
        battleLogs = logs
      )
    }
  }

  /**
   * AI Police Turn Execution
   */
  fun endTurnAndExecuteAi() {
    _uiState.update { it.copy(currentTurn = TurnState.POLICE_TURN) }
    GameSoundEffects.playWalkieTalkie()

    viewModelScope.launch {
      delay(800)
      val state = _uiState.value
      val policeUnits = state.units.filter { it.faction == Faction.POLICE && !it.isStunned && !it.isHostage && it.hp > 0 }
      val kidUnits = state.units.filter { it.faction == Faction.GANG && it.hp > 0 }
      val currentTiles = state.tiles.map { it.copy() }.toMutableList()
      val newUnits = state.units.map { it.copy() }.toMutableList()
      val logs = state.battleLogs.toMutableList()
      logs.add("--- بدء دور شرطة العاصمة 🚓 ---")

      for (cop in policeUnits) {
        val copInList = newUnits.find { it.id == cop.id } ?: continue
        val closestKid = kidUnits.minByOrNull { abs(it.x - copInList.x) + abs(it.y - copInList.y) }
        if (closestKid != null) {
          val dist = abs(closestKid.x - copInList.x) + abs(closestKid.y - copInList.y)
          if (dist <= 1) {
            // Adjacent: Police stun net
            val hitRoll = Random.nextInt(100)
            val kidInList = newUnits.find { it.id == closestKid.id }
            if (hitRoll > 30 && kidInList != null) {
              kidInList.isStunned = true
              kidInList.hp = (kidInList.hp - 25).coerceAtLeast(0)
              logs.add("🚨 ${copInList.nameAr} أطلق شبكة صعق على ${kidInList.nameAr}! (تجميد لدور)")
              GameSoundEffects.playPoliceWhistle()
            } else {
              logs.add("🛡️ ${closestKid.nameAr} احتمى بنجاح من شبكة الشرطي!")
            }
          } else {
            // Move towards kid
            val dx = (closestKid.x - copInList.x).coerceIn(-1, 1)
            val dy = (closestKid.y - copInList.y).coerceIn(-1, 1)
            val targetX = (copInList.x + if (dx != 0) dx else 0).coerceIn(0, gridCols - 1)
            val targetY = (copInList.y + if (dx == 0 && dy != 0) dy else 0).coerceIn(0, gridRows - 1)

            val tileIndex = currentTiles.indexOfFirst { it.x == targetX && it.y == targetY }
            if (tileIndex != -1 && currentTiles[tileIndex].hasBananaTrap) {
              // Trigger Slipping Mechanic
              currentTiles[tileIndex] = currentTiles[tileIndex].copy(hasBananaTrap = false)
              copInList.x = targetX
              copInList.y = targetY
              copInList.isStunned = true
              logs.add("🍌💫 تزحلق مضحك! ${copInList.nameAr} داس على قشرة الموز وفقد توازنه بالكامل! (شل حركة واهتزاز)")
              GameSoundEffects.playSlipBanana()

              // Trigger vibration / shake state
              _uiState.update {
                it.copy(
                  slippingUnitId = copInList.id,
                  isShaking = true,
                  tiles = currentTiles,
                  units = newUnits,
                  battleLogs = logs
                )
              }
              delay(900)
              _uiState.update { it.copy(isShaking = false) }
            } else {
              copInList.x = targetX
              copInList.y = targetY
              copInList.isOverwatch = true
              logs.add("👮‍♂️ ${copInList.nameAr} تحرك نحو الممر وفعل وضعية المراقبة (Overwatch)")
            }
          }
        }
        delay(500)
      }

      // Check Victory/Defeat or reset AP for kids
      val totalKidHp = newUnits.filter { it.faction == Faction.GANG }.sumOf { it.hp }
      if (totalKidHp <= 0) {
        _uiState.update {
          it.copy(
            units = newUnits,
            tiles = currentTiles,
            battleLogs = logs,
            currentTurn = TurnState.DEFEAT,
            victoryMsg = "تمت محاصرة العصابة بالكامل! حظاً أوفر."
          )
        }
        GameSoundEffects.playSiren()
      } else {
        // Reset AP for kid units
        newUnits.filter { it.faction == Faction.GANG }.forEach {
          it.ap = it.maxAp
          it.isStunned = false
        }
        newUnits.filter { it.faction == Faction.POLICE }.forEach {
          it.ap = it.maxAp
          it.isStunned = false
        }

        val nextRound = state.roundNumber + 1
        logs.add("--- بدء دور أبطال العصابة (الجولة $nextRound) 🎒 ---")
        GameSoundEffects.playJump()

        _uiState.update {
          it.copy(
            units = newUnits,
            tiles = currentTiles,
            battleLogs = logs,
            roundNumber = nextRound,
            currentTurn = TurnState.KIDS_TURN,
            slippingUnitId = null
          )
        }
      }
      updateLineOfSight()
    }
  }
}
