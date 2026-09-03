package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for querying and recording high scores & chase times in Room.
 */
@Dao
interface HighScoreDao {
  @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT :limit")
  fun getTopScores(limit: Int = 20): Flow<List<HighScoreEntity>>

  @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 10")
  fun getTop10HighScores(): Flow<List<HighScoreEntity>>

  @Query("SELECT * FROM high_scores WHERE chaseTimeSeconds > 0 ORDER BY chaseTimeSeconds ASC LIMIT 10")
  fun getTop10FastestChaseTimes(): Flow<List<HighScoreEntity>>

  @Query("SELECT * FROM high_scores WHERE stageName = :stageName ORDER BY score DESC LIMIT :limit")
  fun getTopScoresByStage(stageName: String, limit: Int = 10): Flow<List<HighScoreEntity>>

  @Query("SELECT * FROM high_scores WHERE mode = :mode ORDER BY score DESC LIMIT :limit")
  fun getTopScoresByMode(mode: String, limit: Int = 20): Flow<List<HighScoreEntity>>

  @Query("SELECT MAX(score) FROM high_scores")
  suspend fun getGlobalHighScore(): Int?

  @Query("SELECT MIN(chaseTimeSeconds) FROM high_scores WHERE chaseTimeSeconds > 0")
  suspend fun getGlobalFastestTime(): Float?

  @Query("SELECT MAX(score) FROM high_scores WHERE playerName = :playerName")
  suspend fun getPersonalBestScore(playerName: String): Int?

  @Query("SELECT MIN(chaseTimeSeconds) FROM high_scores WHERE playerName = :playerName AND chaseTimeSeconds > 0")
  suspend fun getPersonalBestTime(playerName: String): Float?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHighScore(entry: HighScoreEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(entries: List<HighScoreEntity>)

  @Query("DELETE FROM high_scores WHERE id = :id")
  suspend fun deleteScoreById(id: Long)

  @Query("SELECT COUNT(*) FROM high_scores")
  suspend fun getScoresCount(): Int
}

