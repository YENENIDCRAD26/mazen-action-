package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data Model for in-game missions stored in Room Database.
 * Includes title, description, difficulty, completion status, and tactical location in Sana'a.
 */
@Entity(tableName = "game_missions")
data class GameMissionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "title")
  val title: String,

  @ColumnInfo(name = "description")
  val description: String,

  @ColumnInfo(name = "difficulty")
  val difficulty: String, // "سهل", "متوسط", "صعب", "أسطوري"

  @ColumnInfo(name = "is_completed")
  val isCompleted: Boolean = false,

  @ColumnInfo(name = "location")
  val location: String = "أزقة صنعاء القديمة",

  @ColumnInfo(name = "reward_coins")
  val rewardCoins: Int = 250,

  @ColumnInfo(name = "reward_xp")
  val rewardXp: Int = 100,

  @ColumnInfo(name = "icon_emoji")
  val iconEmoji: String = "🎯",

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis()
)
