package com.example.data

import com.example.data.local.GameMissionDao
import com.example.data.local.GameMissionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository handling in-game missions stored in Room Database.
 */
class GameMissionRepository(private val missionDao: GameMissionDao) {

  val allMissions: Flow<List<GameMissionEntity>> = missionDao.getAllMissions()

  suspend fun getMissionById(id: Long): GameMissionEntity? {
    return missionDao.getMissionById(id)
  }

  suspend fun addMission(mission: GameMissionEntity): Long {
    return missionDao.insertMission(mission)
  }

  suspend fun updateMission(mission: GameMissionEntity) {
    missionDao.updateMission(mission)
  }

  suspend fun setMissionCompletion(id: Long, isCompleted: Boolean) {
    missionDao.updateCompletionStatus(id, isCompleted)
  }

  suspend fun toggleMissionCompletion(mission: GameMissionEntity) {
    missionDao.updateCompletionStatus(mission.id, !mission.isCompleted)
  }

  suspend fun deleteMission(id: Long) {
    missionDao.deleteMission(id)
  }

  /**
   * Ensures default authentic Sana'a missions are present in Room database.
   */
  suspend fun ensureDefaultMissionsPopulated() {
    val count = missionDao.getMissionCount()
    if (count == 0) {
      val defaultMissions = listOf(
        GameMissionEntity(
          title = "الهروب عبر سائلة صنعاء",
          description = "الشرطة تحاصر باب اليمن، عليك المناورة بسرعة عبر مجرى السائلة التاريخي وصولاً إلى جسر شعوب دون الاصطدام بحواجز الدبابات.",
          difficulty = "متوسط",
          isCompleted = false,
          location = "شارع السايلة التراثي",
          rewardCoins = 350,
          rewardXp = 150,
          iconEmoji = "🌊"
        ),
        GameMissionEntity(
          title = "التسلل بين أزقة باب اليمن العتيقة",
          description = "تسلل بين الدكاكين الطينية المزدحمة وتفادَ دوريات الشرطة الراجلة باستخدام قشور الموز وأكياس البهارات كساتر تكتيكي.",
          difficulty = "سهل",
          isCompleted = true,
          location = "باب اليمن وأزقة الحدادين",
          rewardCoins = 200,
          rewardXp = 80,
          iconEmoji = "🏰"
        ),
        GameMissionEntity(
          title = "تأمين مخبأ سوق الملح والبهارات",
          description = "انقل حقيبة الأدوات التكتيكية إلى المخبأ السري أسفل متجر العقيق اليماني القديم قبل إغلاق بوابات السور.",
          difficulty = "متوسط",
          isCompleted = false,
          location = "سوق الملح والبهارات",
          rewardCoins = 450,
          rewardXp = 200,
          iconEmoji = "🧂"
        ),
        GameMissionEntity(
          title = "مراوغة دوريات جولة التحرير وسبأ",
          description = "نصبت شرطة العاصمة نقاط تفتيش بمركبات لاندكروزر. قُد الدباب الياباني بسرعة خاطفة وقم بالدرفت في المنعطفات الضيقة لتشتيتهم.",
          difficulty = "صعب",
          isCompleted = false,
          location = "جولة سبأ والتحرير",
          rewardCoins = 600,
          rewardXp = 300,
          iconEmoji = "🚐"
        ),
        GameMissionEntity(
          title = "سباق الأسطح والقمريات الملونة",
          description = "اصعد فوق منازل الياجور الطينية واهرب عبر الجسور الخشبية بين المنازل متجنباً كشافات المفتش ناصر من الأسفل.",
          difficulty = "صعب",
          isCompleted = false,
          location = "حي القاسمي والقمريات",
          rewardCoins = 750,
          rewardXp = 350,
          iconEmoji = "🏃‍♂️"
        ),
        GameMissionEntity(
          title = "عملية قصر غمدان الكبرى",
          description = "استدرج دوريات الشرطة المصفحة إلى أضيق ممر مغلق بالقرب من بستان السلطان لتعطيل محركاتها والفرار بغنيمة الكنز التاريخي.",
          difficulty = "أسطوري",
          isCompleted = false,
          location = "أطلال قصر غمدان وبستان السلطان",
          rewardCoins = 1200,
          rewardXp = 600,
          iconEmoji = "👑"
        )
      )
      missionDao.insertAll(defaultMissions)
    }
  }
}
