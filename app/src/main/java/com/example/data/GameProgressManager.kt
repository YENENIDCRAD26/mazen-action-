package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.gameProgressDataStore: DataStore<Preferences> by preferencesDataStore(name = "sana_game_progress")

/**
 * GameProgressManager using AndroidX DataStore to track unlocked levels.
 * Explicitly sets levels 1, 2, and 3 as unlocked by default for all users.
 */
class GameProgressManager(private val context: Context) {

  companion object {
    val KEY_UNLOCKED_LEVELS = stringSetPreferencesKey("unlocked_levels")
    
    // Explicitly set levels 1, 2, and 3 as default unlocked levels for all users
    val DEFAULT_UNLOCKED_LEVELS = setOf("1", "2", "3")
    val ALL_GAME_LEVELS = setOf("1", "2", "3", "4", "5")
  }

  /**
   * Flow of currently unlocked level IDs (as Integers).
   * Ensures levels 1, 2, and 3 are present by default.
   */
  val unlockedLevelsFlow: Flow<Set<Int>> = context.gameProgressDataStore.data
    .catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
    .map { preferences ->
      val savedLevels = preferences[KEY_UNLOCKED_LEVELS] ?: DEFAULT_UNLOCKED_LEVELS
      // Ensure levels 1, 2, 3 are always included by default
      val combined = (savedLevels + DEFAULT_UNLOCKED_LEVELS).mapNotNull { it.toIntOrNull() }.toSet()
      combined.ifEmpty { setOf(1, 2, 3) }
    }

  /**
   * Checks if a specific level index is currently unlocked.
   */
  fun isLevelUnlocked(level: Int): Flow<Boolean> {
    return unlockedLevelsFlow.map { unlocked ->
      // Levels 1, 2, 3 are always unlocked by default
      level in 1..3 || unlocked.contains(level)
    }
  }

  /**
   * Unlocks a specific level and saves it to DataStore.
   */
  suspend fun unlockLevel(level: Int) {
    context.gameProgressDataStore.edit { preferences ->
      val current = preferences[KEY_UNLOCKED_LEVELS] ?: DEFAULT_UNLOCKED_LEVELS
      preferences[KEY_UNLOCKED_LEVELS] = current + level.toString()
    }
  }

  /**
   * Unlocks all levels globally (e.g. for Admin 'mazengalab' mode).
   */
  suspend fun unlockAllLevels() {
    context.gameProgressDataStore.edit { preferences ->
      preferences[KEY_UNLOCKED_LEVELS] = ALL_GAME_LEVELS
    }
  }

  /**
   * Resets unlocked levels to default (levels 1, 2, and 3).
   */
  suspend fun resetToDefault() {
    context.gameProgressDataStore.edit { preferences ->
      preferences[KEY_UNLOCKED_LEVELS] = DEFAULT_UNLOCKED_LEVELS
    }
  }
}
