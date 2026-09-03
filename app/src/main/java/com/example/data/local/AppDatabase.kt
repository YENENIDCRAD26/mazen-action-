package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database holder for Sana'a Chase High Scores and Top Players.
 */
@Database(entities = [HighScoreEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun highScoreDao(): HighScoreDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "sanaa_heroes_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              // Populate initial default leaderboard records asynchronously (Top 10)
              CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).highScoreDao()
                val seedPlayers = listOf(
                  HighScoreEntity(
                    playerName = "مازن (الزعيم الصغير)",
                    score = 9850,
                    mode = "GTA_SANAA_7D",
                    difficulty = "HARD",
                    titleAr = "أسطورة باب اليمن 👑",
                    rankBadgeEmoji = "🥇",
                    coinsEarned = 1420,
                    chaseTimeSeconds = 64.25f,
                    stageName = "باب اليمن الأثري",
                    isPersonalBest = true
                  ),
                  HighScoreEntity(
                    playerName = "أبو رعد الصنعاني",
                    score = 8400,
                    mode = "GTA_SANAA_7D",
                    difficulty = "HARD",
                    titleAr = "شبح أزقة صنعاء 🥷",
                    rankBadgeEmoji = "🥈",
                    coinsEarned = 1180,
                    chaseTimeSeconds = 71.80f,
                    stageName = "سوق الملح والبهارات",
                    isPersonalBest = true
                  ),
                  HighScoreEntity(
                    playerName = "عصام الباركور",
                    score = 7250,
                    mode = "GTA_SANAA_7D",
                    difficulty = "NORMAL",
                    titleAr = "قاهر الأسطح والقمريات 🏃‍♂️",
                    rankBadgeEmoji = "🥉",
                    coinsEarned = 950,
                    chaseTimeSeconds = 85.10f,
                    stageName = "حي القاسمي والقمريات",
                    isPersonalBest = true
                  ),
                  HighScoreEntity(
                    playerName = "حامد سائق الدباب",
                    score = 6600,
                    mode = "GTA_SANAA_7D",
                    difficulty = "NORMAL",
                    titleAr = "زعيم درفت جولة سبأ 🚗",
                    rankBadgeEmoji = "🎖️",
                    coinsEarned = 890,
                    chaseTimeSeconds = 92.40f,
                    stageName = "جولة سبأ والتحرير",
                    isPersonalBest = false
                  ),
                  HighScoreEntity(
                    playerName = "المفتش ناصر",
                    score = 5800,
                    mode = "GTA_SANAA_7D",
                    difficulty = "NORMAL",
                    titleAr = "قائد شرطة العاصمة 👮‍♂️",
                    rankBadgeEmoji = "🛡️",
                    coinsEarned = 750,
                    chaseTimeSeconds = 104.15f,
                    stageName = "شارع السايلة التراثي",
                    isPersonalBest = false
                  ),
                  HighScoreEntity(
                    playerName = "سامي القناص",
                    score = 5100,
                    mode = "GTA_SANAA_7D",
                    difficulty = "NORMAL",
                    titleAr = "صقر وادي السائلة 🦅",
                    rankBadgeEmoji = "🎖️",
                    coinsEarned = 670,
                    chaseTimeSeconds = 112.30f,
                    stageName = "وادي ظهر وقصر الحجر",
                    isPersonalBest = false
                  ),
                  HighScoreEntity(
                    playerName = "ياسر الميكانيكي",
                    score = 4600,
                    mode = "GTA_SANAA_7D",
                    difficulty = "EASY",
                    titleAr = "مهندس الدبابات السريعة 🔧",
                    rankBadgeEmoji = "🎖️",
                    coinsEarned = 580,
                    chaseTimeSeconds = 125.80f,
                    stageName = "جولة الرويشان",
                    isPersonalBest = false
                  ),
                  HighScoreEntity(
                    playerName = "فهد الصقر",
                    score = 4200,
                    mode = "GTA_SANAA_7D",
                    difficulty = "EASY",
                    titleAr = "عداء الحارات الطينية ⚡",
                    rankBadgeEmoji = "🎖️",
                    coinsEarned = 520,
                    chaseTimeSeconds = 138.50f,
                    stageName = "بستان السلطان",
                    isPersonalBest = false
                  ),
                  HighScoreEntity(
                    playerName = "صقر همدان",
                    score = 3800,
                    mode = "GTA_SANAA_7D",
                    difficulty = "EASY",
                    titleAr = "مخترق الحواجز الأمنية 🚧",
                    rankBadgeEmoji = "🎖️",
                    coinsEarned = 460,
                    chaseTimeSeconds = 145.20f,
                    stageName = "باب شعوب",
                    isPersonalBest = false
                  ),
                  HighScoreEntity(
                    playerName = "طارق المتمرد",
                    score = 3400,
                    mode = "GTA_SANAA_7D",
                    difficulty = "EASY",
                    titleAr = "شبل المشاغبين الصغار 💥",
                    rankBadgeEmoji = "🎖️",
                    coinsEarned = 390,
                    chaseTimeSeconds = 158.90f,
                    stageName = "سوق الفضة والعقيق",
                    isPersonalBest = false
                  )
                )
                dao.insertAll(seedPlayers)
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
