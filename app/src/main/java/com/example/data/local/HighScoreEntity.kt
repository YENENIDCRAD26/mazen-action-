package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a high score and player chase record in the local Room database.
 * Supports tracking both high scores and fastest chase times for speedrun competitions.
 */
@Entity(tableName = "high_scores")
data class HighScoreEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val playerName: String,
  val score: Int,
  val mode: String = "GTA_SANAA_7D", // "GTA_SANAA_7D", "ALLEY_GRID", "TACTICAL_XCOM", etc.
  val difficulty: String = "NORMAL", // "EASY", "NORMAL", "HARD", "NIGHTMARE_SANAA"
  val dateEpoch: Long = System.currentTimeMillis(),
  val titleAr: String = "بطل أزقة صنعاء",
  val rankBadgeEmoji: String = "🥇",
  val coinsEarned: Int = 0,
  val chaseTimeSeconds: Float = 0f, // Chase duration in seconds for Top 10 speedrun competitions
  val stageName: String = "باب اليمن", // Specific stage where the record was achieved
  val isPersonalBest: Boolean = false
) {
  fun getFormattedChaseTime(): String {
    if (chaseTimeSeconds <= 0f) return "--:--"
    val minutes = (chaseTimeSeconds / 60).toInt()
    val seconds = (chaseTimeSeconds % 60).toInt()
    val millis = ((chaseTimeSeconds - (minutes * 60 + seconds)) * 100).toInt()
    return "%02d:%02d.%02d".format(minutes, seconds, millis)
  }
}

