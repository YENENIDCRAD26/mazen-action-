package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * ViewModel managing the basic Coroutine Game Loop for the Young Prankster & Police Grid Game.
 * Updates game object positions (Player and Police) at a set interval.
 */
class PranksterGameLoopViewModel : ViewModel() {

  private val _gameState = MutableStateFlow(createInitialState())
  val gameState: StateFlow<GridGameState> = _gameState.asStateFlow()

  private var gameLoopJob: Job? = null
  private var queuedDirection: GridDirection = GridDirection.NONE

  init {
    // Optionally auto-start the game loop
    startGameLoop()
  }

  /**
   * Starts the Coroutine Game Loop at the set interval.
   */
  fun startGameLoop() {
    if (gameLoopJob?.isActive == true) return

    _gameState.update { it.copy(isRunning = true, statusMessageAr = "الجولة نشطة! راوغ دوريات الشرطة ونفّذ المقالب.") }

    gameLoopJob = viewModelScope.launch {
      while (isActive) {
        val currentState = _gameState.value
        if (currentState.isRunning && !currentState.isGameOver) {
          updateGameTick()
        }
        delay(currentState.tickIntervalMs)
      }
    }
  }

  /**
   * Pauses the game loop coroutine.
   */
  fun pauseGameLoop() {
    _gameState.update { it.copy(isRunning = false, statusMessageAr = "تم إيقاف اللعبة مؤقتاً ⏸️") }
  }

  /**
   * Adjusts the set tick interval for the game loop (e.g., 200ms for fast, 400ms for slow).
   */
  fun setTickInterval(intervalMs: Long) {
    val clamped = intervalMs.coerceIn(100L, 1000L)
    _gameState.update { it.copy(tickIntervalMs = clamped) }
  }

  /**
   * Sets player movement intent for the next game tick.
   */
  fun setPlayerDirection(direction: GridDirection) {
    queuedDirection = direction
  }

  /**
   * Resets the game to its initial state.
   */
  fun resetGame() {
    queuedDirection = GridDirection.NONE
    _gameState.value = createInitialState().copy(isRunning = true)
    if (gameLoopJob == null || gameLoopJob?.isActive == false) {
      startGameLoop()
    }
  }

  /**
   * Single Game Loop Tick:
   * 1. Updates player position based on queued direction and obstacles.
   * 2. Updates police AI positions and states (Patrol or Chase).
   * 3. Checks win / lose conditions and collisions.
   */
  fun updateGameTick() {
    _gameState.update { state ->
      if (state.isGameOver) return@update state

      var player = state.player
      var policeList = state.policeOfficers
      var completedPranks = state.completedPranks
      var score = state.score
      var isGameOver = false
      var statusMsg = state.statusMessageAr

      // 1. UPDATE PLAYER (Young Prankster) POSITION
      if (queuedDirection != GridDirection.NONE && !player.isBusted && !player.hasEscaped) {
        val nextPos = player.position.translated(queuedDirection)

        // Validate bounds and obstacles
        val canMove = nextPos.isWithinBounds(state.gridWidth, state.gridHeight) &&
            !state.obstacles.contains(nextPos)

        if (canMove) {
          val isNowHiding = state.hidingSpots.contains(nextPos)
          player = player.copy(
            position = nextPos,
            facingDirection = queuedDirection,
            state = if (isNowHiding) PranksterState.HIDING else PranksterState.MOVING,
            isHidden = isNowHiding
          )
        }
      } else if (player.isHidden) {
        player = player.copy(state = PranksterState.HIDING)
      } else {
        player = player.copy(state = PranksterState.IDLE)
      }

      // Check if player landed on a Prank Target
      if (state.prankTargets.contains(player.position) && !completedPranks.contains(player.position)) {
        completedPranks = completedPranks + player.position
        score += 150
        player = player.copy(
          pranksCount = player.pranksCount + 1,
          state = PranksterState.PRANKING
        )
        statusMsg = "🎭 مقلب ناجح! حصلت على 150 نقطة!"
      }

      // 2. UPDATE POLICE CHARACTERS' POSITIONS AND STATES
      policeList = policeList.map { officer ->
        if (officer.isStunned) {
          return@map officer.copy(
            stunDurationTicks = officer.stunDurationTicks - 1,
            state = if (officer.stunDurationTicks - 1 <= 0) PoliceState.PATROLLING else PoliceState.STUNNED
          )
        }

        val distToPlayer = officer.position.manhattanDistanceTo(player.position)
        val seesPlayer = !player.isHidden && distToPlayer <= officer.visionRange

        if (seesPlayer) {
          // CHASE MODE: Step closer to the young prankster
          val step = calculateNextStepTowards(
            from = officer.position,
            target = player.position,
            obstacles = state.obstacles,
            width = state.gridWidth,
            height = state.gridHeight
          )
          officer.copy(
            position = step,
            state = PoliceState.CHASING,
            facingDirection = getDirectionBetween(officer.position, step)
          )
        } else {
          // PATROL MODE: Follow designated route
          if (officer.patrolRoute.isNotEmpty()) {
            val targetWaypoint = officer.patrolRoute[officer.patrolIndex % officer.patrolRoute.size]
            if (officer.position == targetWaypoint) {
              val nextIndex = (officer.patrolIndex + 1) % officer.patrolRoute.size
              val nextTarget = officer.patrolRoute[nextIndex]
              val step = calculateNextStepTowards(
                from = officer.position,
                target = nextTarget,
                obstacles = state.obstacles,
                width = state.gridWidth,
                height = state.gridHeight
              )
              officer.copy(
                position = step,
                patrolIndex = nextIndex,
                state = PoliceState.PATROLLING,
                facingDirection = getDirectionBetween(officer.position, step)
              )
            } else {
              val step = calculateNextStepTowards(
                from = officer.position,
                target = targetWaypoint,
                obstacles = state.obstacles,
                width = state.gridWidth,
                height = state.gridHeight
              )
              officer.copy(
                position = step,
                state = PoliceState.PATROLLING,
                facingDirection = getDirectionBetween(officer.position, step)
              )
            }
          } else {
            officer.copy(state = PoliceState.PATROLLING)
          }
        }
      }

      // 3. COLLISION & GAME OVER DETECTION
      val caughtBy = policeList.find { it.position == player.position && !player.isHidden }
      if (caughtBy != null) {
        player = player.copy(state = PranksterState.BUSTED)
        policeList = policeList.map {
          if (it.id == caughtBy.id) it.copy(state = PoliceState.ARRESTING) else it
        }
        isGameOver = true
        statusMsg = "🚨 كشفتك الشرطة! تم القبض على مازن المشاغب!"
      } else if (completedPranks.size >= state.prankTargets.size && player.position == state.escapeGate) {
        // VICTORY: Escaped through the alley gate
        player = player.copy(state = PranksterState.ESCAPED)
        isGameOver = true
        score += 300
        statusMsg = "🏆 مبروك! نفذت جميع المقالب وهربت بنجاح عبر باب اليمن!"
      }

      state.copy(
        player = player,
        policeOfficers = policeList,
        completedPranks = completedPranks,
        score = score,
        isGameOver = isGameOver,
        isRunning = if (isGameOver) false else state.isRunning,
        statusMessageAr = statusMsg,
        tickCount = state.tickCount + 1
      )
    }
  }

  /**
   * Action to stun nearby police using a prank tool (e.g., banana peel or water splash).
   */
  fun triggerPrankAction() {
    _gameState.update { state ->
      val playerPos = state.player.position
      var scoreBonus = 0
      val updatedPolice = state.policeOfficers.map { officer ->
        if (officer.position.manhattanDistanceTo(playerPos) <= 2) {
          scoreBonus += 50
          officer.copy(
            state = PoliceState.STUNNED,
            stunDurationTicks = 5 // Stunned for 5 ticks
          )
        } else {
          officer
        }
      }

      state.copy(
        policeOfficers = updatedPolice,
        score = state.score + scoreBonus,
        statusMessageAr = if (scoreBonus > 0) "🍌 رميت قشر موز! تم شل حركة دورية الشرطة لـ 5 نبضات!" else "لا توجد دوريات قريبة لإصابتها بالمقلب."
      )
    }
  }

  /**
   * Pathfinding step towards target using Manhattan proximity and obstacle avoidance.
   */
  private fun calculateNextStepTowards(
    from: GridPosition,
    target: GridPosition,
    obstacles: Set<GridPosition>,
    width: Int,
    height: Int
  ): GridPosition {
    if (from == target) return from

    val candidates = listOf(
      GridPosition(from.x + 1, from.y),
      GridPosition(from.x - 1, from.y),
      GridPosition(from.x, from.y + 1),
      GridPosition(from.x, from.y - 1)
    ).filter { it.isWithinBounds(width, height) && !obstacles.contains(it) }

    return candidates.minByOrNull { it.manhattanDistanceTo(target) } ?: from
  }

  private fun getDirectionBetween(from: GridPosition, to: GridPosition): GridDirection {
    return when {
      to.x > from.x -> GridDirection.RIGHT
      to.x < from.x -> GridDirection.LEFT
      to.y > from.y -> GridDirection.DOWN
      to.y < from.y -> GridDirection.UP
      else -> GridDirection.NONE
    }
  }

  override fun onCleared() {
    super.onCleared()
    gameLoopJob?.cancel()
  }

  companion object {
    fun createInitialState(): GridGameState {
      val width = 8
      val height = 8

      // Traditional Sana'a clay walls / obstacles
      val obstacles = setOf(
        GridPosition(2, 1), GridPosition(2, 2), GridPosition(2, 3),
        GridPosition(5, 4), GridPosition(5, 5), GridPosition(5, 6),
        GridPosition(3, 5)
      )

      // Hiding spots (Sana'a alley carts / boxes)
      val hidingSpots = setOf(
        GridPosition(1, 4),
        GridPosition(4, 2),
        GridPosition(6, 2)
      )

      // Prank targets (Spice sack / Fruit stand / Water bucket)
      val prankTargets = setOf(
        GridPosition(1, 6),
        GridPosition(6, 1),
        GridPosition(4, 6)
      )

      // Player: Young Prankster Mazen
      val player = YoungPrankster(
        id = "prankster_mazen",
        name = "مازن المشاغب",
        position = GridPosition(0, 0),
        state = PranksterState.IDLE
      )

      // Police 1: Patrols vertical alley
      val officer1 = PoliceOfficer(
        id = "cop_1",
        name = "الشرطي سالم",
        position = GridPosition(3, 1),
        patrolRoute = listOf(GridPosition(3, 1), GridPosition(3, 4), GridPosition(4, 4)),
        visionRange = 3
      )

      // Police 2: Patrols southern market lane
      val officer2 = PoliceOfficer(
        id = "cop_2",
        name = "الشرطي فؤاد",
        position = GridPosition(7, 4),
        patrolRoute = listOf(GridPosition(7, 4), GridPosition(7, 7), GridPosition(3, 7)),
        visionRange = 2
      )

      return GridGameState(
        gridWidth = width,
        gridHeight = height,
        player = player,
        policeOfficers = listOf(officer1, officer2),
        obstacles = obstacles,
        hidingSpots = hidingSpots,
        prankTargets = prankTargets,
        completedPranks = emptySet(),
        escapeGate = GridPosition(7, 7),
        isRunning = false,
        isGameOver = false,
        score = 0,
        tickCount = 0L,
        tickIntervalMs = 300L
      )
    }
  }
}
