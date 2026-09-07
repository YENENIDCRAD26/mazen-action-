package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for game missions.
 */
@Dao
interface GameMissionDao {

  @Query("SELECT * FROM game_missions ORDER BY is_completed ASC, id ASC")
  fun getAllMissions(): Flow<List<GameMissionEntity>>

  @Query("SELECT * FROM game_missions WHERE is_completed = :completed ORDER BY id ASC")
  fun getMissionsByStatus(completed: Boolean): Flow<List<GameMissionEntity>>

  @Query("SELECT * FROM game_missions WHERE id = :id LIMIT 1")
  suspend fun getMissionById(id: Long): GameMissionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMission(mission: GameMissionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(missions: List<GameMissionEntity>)

  @Update
  suspend fun updateMission(mission: GameMissionEntity)

  @Query("UPDATE game_missions SET is_completed = :isCompleted WHERE id = :id")
  suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

  @Query("DELETE FROM game_missions WHERE id = :id")
  suspend fun deleteMission(id: Long)

  @Query("SELECT COUNT(*) FROM game_missions")
  suspend fun getMissionCount(): Int
}
