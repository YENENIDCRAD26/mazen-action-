package com.example.model

data class LevelRank(
  val level: Int,
  val titleAr: String,
  val badgeEmoji: String,
  val requiredTotalXp: Int,
  val perkDescAr: String,
  val rewardCoinsOnReach: Int
)

data class PlayerLevelInfo(
  val currentLevel: Int,
  val currentTitleAr: String,
  val badgeEmoji: String,
  val currentLevelXp: Int,
  val xpRequiredForCurrentLevelSpan: Int,
  val nextLevelTargetXp: Int,
  val totalXp: Int,
  val progressRatio: Float,
  val perkDescAr: String,
  val isMaxLevel: Boolean
)

data class XpBreakdown(
  val baseChaseXp: Int,
  val scoreBonusXp: Int,
  val copsEvadedXp: Int,
  val parkourXp: Int,
  val thugsCapturedXp: Int,
  val totalXpEarned: Int
)

object PlayerLevelSystem {
  val ranks = listOf(
    LevelRank(1, "مشاغب مبتدئ في الحارة", "🎒", 0, "بداية المشوار والمغامرة في شوارع وأزقة صنعاء", 0),
    LevelRank(2, "عدّاء أزقة باب اليمن", "🏃‍♂️", 350, "سرعة ركض إضافية ومراوغة أسرع لدوريات الشرطة", 200),
    LevelRank(3, "صقر أسطح القمريات", "🦅", 800, "قفزات بهلوانية أعلى ومرونة إضافية فوق الأسطح", 350),
    LevelRank(4, "شبح سوق الملح والتخفي", "🥷", 1400, "تبريد أسرع لمطاردة الشرطة وكسر خط النظر", 500),
    LevelRank(5, "مهجول جولة كنتاكي", "🏎️", 2150, "تحكم فائق بالدباب والشاص الصنعاني وتفحيط أسرع", 700),
    LevelRank(6, "خبير تكتيكات البيت الطيني", "🏰", 3050, "توليد أسرع لطاقة الأدرينالين وزمن إبطاء أطول", 900),
    LevelRank(7, "قائد عمليات شوارع صنعاء", "👑", 4100, "مكافأة غنائم لصوص إضافية بنسبة +30%", 1200),
    LevelRank(8, "أسطورة صخور دار الحجر", "🏛️", 5300, "تشتيت فوري ومضاعف للألعاب النارية التكتيكية", 1500),
    LevelRank(9, "ذئب أزقة صنعاء القديمة", "🐺", 6700, "حصانة أعلى ومضاعفة وتيرة تجميع العملات", 2000),
    LevelRank(10, "الزعيم الكبير لعاصمة الصمود", "🌟", 8300, "اللقب الأسطوري الأسمى وتاج السيطرة على المدينة", 3000)
  )

  fun getLevelInfo(totalXp: Int): PlayerLevelInfo {
    var currentRank = ranks.first()
    var nextRank: LevelRank? = null

    for (i in ranks.indices) {
      if (totalXp >= ranks[i].requiredTotalXp) {
        currentRank = ranks[i]
        nextRank = ranks.getOrNull(i + 1)
      } else {
        break
      }
    }

    return if (nextRank != null) {
      val xpIntoLevel = totalXp - currentRank.requiredTotalXp
      val levelSpan = nextRank.requiredTotalXp - currentRank.requiredTotalXp
      val ratio = (xpIntoLevel.toFloat() / levelSpan.toFloat()).coerceIn(0f, 1f)
      PlayerLevelInfo(
        currentLevel = currentRank.level,
        currentTitleAr = currentRank.titleAr,
        badgeEmoji = currentRank.badgeEmoji,
        currentLevelXp = xpIntoLevel,
        xpRequiredForCurrentLevelSpan = levelSpan,
        nextLevelTargetXp = nextRank.requiredTotalXp,
        totalXp = totalXp,
        progressRatio = ratio,
        perkDescAr = currentRank.perkDescAr,
        isMaxLevel = false
      )
    } else {
      PlayerLevelInfo(
        currentLevel = currentRank.level,
        currentTitleAr = currentRank.titleAr,
        badgeEmoji = currentRank.badgeEmoji,
        currentLevelXp = totalXp - currentRank.requiredTotalXp,
        xpRequiredForCurrentLevelSpan = 1,
        nextLevelTargetXp = currentRank.requiredTotalXp,
        totalXp = totalXp,
        progressRatio = 1.0f,
        perkDescAr = currentRank.perkDescAr,
        isMaxLevel = true
      )
    }
  }

  fun calculateChaseXp(
    isVictory: Boolean,
    score: Int,
    copsEvaded: Int,
    rooftopsCleared: Int,
    thugsCaptured: Int,
    distanceCovered: Float
  ): XpBreakdown {
    val baseChase = if (isVictory) 250 else 75
    val scoreBonus = (score / 25).coerceIn(0, 300)
    val copsXp = (copsEvaded * 40).coerceIn(0, 240)
    val parkour = (rooftopsCleared * 25).coerceIn(0, 150)
    val thugs = (thugsCaptured * 35).coerceIn(0, 180)
    val total = baseChase + scoreBonus + copsXp + parkour + thugs

    return XpBreakdown(
      baseChaseXp = baseChase,
      scoreBonusXp = scoreBonus,
      copsEvadedXp = copsXp,
      parkourXp = parkour,
      thugsCapturedXp = thugs,
      totalXpEarned = total
    )
  }
}
