package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.PlayerLevelSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "sana_player_preferences")

data class PlayerProgressData(
  val playerXp: Int = 0,
  val playerLevel: Int = 1,
  val totalCoins: Int = 1500,
  val highChaseScore: Int = 0
)

class PlayerPreferencesDataStore(private val context: Context) {
  companion object {
    val KEY_PLAYER_XP = intPreferencesKey("player_xp")
    val KEY_PLAYER_LEVEL = intPreferencesKey("player_level")
    val KEY_TOTAL_COINS = intPreferencesKey("total_coins")
    val KEY_HIGH_CHASE_SCORE = intPreferencesKey("high_chase_score")
  }

  val playerProgressFlow: Flow<PlayerProgressData> = context.playerDataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { prefs ->
      val xp = prefs[KEY_PLAYER_XP] ?: 0
      val levelInfo = PlayerLevelSystem.getLevelInfo(xp)
      val coins = prefs[KEY_TOTAL_COINS] ?: 1500
      val highScore = prefs[KEY_HIGH_CHASE_SCORE] ?: 0
      PlayerProgressData(
        playerXp = xp,
        playerLevel = levelInfo.currentLevel,
        totalCoins = coins,
        highChaseScore = highScore
      )
    }

  suspend fun savePlayerProgress(xp: Int, level: Int, coins: Int) {
    context.playerDataStore.edit { prefs ->
      prefs[KEY_PLAYER_XP] = xp
      prefs[KEY_PLAYER_LEVEL] = level
      prefs[KEY_TOTAL_COINS] = coins
    }
  }

  suspend fun recordMissionVictory(xpEarned: Int, coinsEarned: Int, newScore: Int = 0): PlayerProgressData {
    var result = PlayerProgressData()
    context.playerDataStore.edit { prefs ->
      val currentXp = prefs[KEY_PLAYER_XP] ?: 0
      val currentCoins = prefs[KEY_TOTAL_COINS] ?: 1500
      val currentHigh = prefs[KEY_HIGH_CHASE_SCORE] ?: 0

      val updatedXp = currentXp + xpEarned
      val updatedCoins = currentCoins + coinsEarned
      val updatedHigh = maxOf(currentHigh, newScore)
      val levelInfo = PlayerLevelSystem.getLevelInfo(updatedXp)

      prefs[KEY_PLAYER_XP] = updatedXp
      prefs[KEY_PLAYER_LEVEL] = levelInfo.currentLevel
      prefs[KEY_TOTAL_COINS] = updatedCoins
      prefs[KEY_HIGH_CHASE_SCORE] = updatedHigh

      result = PlayerProgressData(
        playerXp = updatedXp,
        playerLevel = levelInfo.currentLevel,
        totalCoins = updatedCoins,
        highChaseScore = updatedHigh
      )
    }
    return result
  }
}
