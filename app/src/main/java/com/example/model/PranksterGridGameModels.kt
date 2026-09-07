package com.example.model

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 2D Grid Coordinate for the Prankster & Police game world.
 */
data class GridPosition(
  val x: Int,
  val y: Int
) {
  fun distanceTo(other: GridPosition): Double {
    val dx = (x - other.x).toDouble()
    val dy = (y - other.y).toDouble()
    return sqrt(dx * dx + dy * dy)
  }

  fun manhattanDistanceTo(other: GridPosition): Int {
    return abs(x - other.x) + abs(y - other.y)
  }

  fun isWithinBounds(width: Int, height: Int): Boolean {
    return x in 0 until width && y in 0 until height
  }

  fun translated(direction: GridDirection): GridPosition {
    return GridPosition(x + direction.dx, y + direction.dy)
  }
}

/**
 * Movement directions on the 2D grid.
 */
enum class GridDirection(val dx: Int, val dy: Int, val labelAr: String) {
  UP(0, -1, "أعلى ⬆️"),
  DOWN(0, 1, "أسفل ⬇️"),
  LEFT(-1, 0, "يسار ⬅️"),
  RIGHT(1, 0, "يمين ➡️"),
  NONE(0, 0, "ثابت ⏹️")
}

/**
 * State of the Young Prankster (المشاغب الصغير).
 */
enum class PranksterState(val titleAr: String, val iconEmoji: String) {
  IDLE("واقف يتربص", "👀"),
  MOVING("يركض في الحارة", "🏃‍♂️"),
  PRANKING("ينفذ مقلباً", "🎭"),
  HIDING("مختبئ خلف الساتر", "🥷"),
  BUSTED("تم القبض عليه!", "🚨"),
  ESCAPED("نجح في الهروب!", "🏆")
}

/**
 * State of the Police Officer / Patrol (رجل الشرطة / الدورية).
 */
enum class PoliceState(val titleAr: String, val iconEmoji: String) {
  PATROLLING("دورية حراسة اعتيادية", "👮‍♂️"),
  ALERT("حذر واستطلاع", "⚠️"),
  CHASING("مطاردة سريعة للمشاغب!", "🚨"),
  ARRESTING("تنفيذ التوقيف", "🔒"),
  STUNNED("مشوش بفخ المقالب", "💫")
}

/**
 * Types of Grid Cells in the Old Sana'a Alleyway Map.
 */
enum class GridCellType(val titleAr: String, val isWalkable: Boolean) {
  ALLEY_STREET("زقاق أسفلتي", true),
  CLAY_WALL("جدار من الطين والحجر", false),
  HIDING_BOX("صندوق كرتون وساتر للتخفي", true),
  PRANK_TARGET("هدف المقلب (كشك البهارات / سطل الماء)", true)
}

/**
 * Data Model for the Player: The Young Prankster (المشاغب الصغير).
 */
data class YoungPrankster(
  val id: String = "prankster_mazen",
  val name: String = "مازن المشاغب",
  val position: GridPosition = GridPosition(1, 1),
  val state: PranksterState = PranksterState.IDLE,
  val stamina: Int = 100,
  val maxStamina: Int = 100,
  val pranksCount: Int = 0,
  val isHidden: Boolean = false,
  val facingDirection: GridDirection = GridDirection.RIGHT
) {
  val isBusted: Boolean
    get() = state == PranksterState.BUSTED

  val hasEscaped: Boolean
    get() = state == PranksterState.ESCAPED
}

/**
 * Data Model for Police Characters (رجال الشرطة / الدوريات).
 */
data class PoliceOfficer(
  val id: String,
  val name: String = "الضابط سالم",
  val position: GridPosition,
  val state: PoliceState = PoliceState.PATROLLING,
  val patrolRoute: List<GridPosition> = emptyList(),
  val patrolIndex: Int = 0,
  val visionRange: Int = 3,
  val stunDurationTicks: Int = 0,
  val facingDirection: GridDirection = GridDirection.DOWN
) {
  val isStunned: Boolean
    get() = stunDurationTicks > 0
}

/**
 * Complete Snapshot State of the Grid Game World.
 */
data class GridGameState(
  val gridWidth: Int = 10,
  val gridHeight: Int = 10,
  val player: YoungPrankster = YoungPrankster(),
  val policeOfficers: List<PoliceOfficer> = emptyList(),
  val obstacles: Set<GridPosition> = emptySet(),
  val hidingSpots: Set<GridPosition> = emptySet(),
  val prankTargets: Set<GridPosition> = emptySet(),
  val completedPranks: Set<GridPosition> = emptySet(),
  val escapeGate: GridPosition = GridPosition(9, 9),
  val isRunning: Boolean = false,
  val isGameOver: Boolean = false,
  val statusMessageAr: String = "ابدأ الجولة لتنفيذ المقالب والهروب من الشرطة!",
  val score: Int = 0,
  val tickCount: Long = 0L,
  val tickIntervalMs: Long = 300L // Set interval for the game loop coroutine
)
