package com.example.model

/**
 * Represents a progression tier for unlocking 'Sana'a Hero' status and faster movement speeds
 * as the player accomplishes successful missions in the alleys of Sana'a.
 */
data class HeroProgressionTier(
  val tierIndex: Int,
  val requiredMissions: Int,
  val statusTitleAr: String,
  val badgeEmoji: String,
  val speedBoostPercent: Int, // 0%, 10%, 20%, 35%
  val speedMultiplier: Float, // 1.0f, 1.10f, 1.20f, 1.35f
  val isSanaaHeroStatus: Boolean,
  val titleEn: String,
  val descriptionAr: String,
  val perksAr: List<String>
)

/**
 * Progression model managing player's 'Sana'a Hero' status and movement speed boosts
 * gained by completing successful missions.
 */
object SanaaHeroProgression {

  val tiers: List<HeroProgressionTier> = listOf(
    HeroProgressionTier(
      tierIndex = 0,
      requiredMissions = 0,
      statusTitleAr = "مشاغب أزقة صنعاء",
      badgeEmoji = "👟",
      speedBoostPercent = 0,
      speedMultiplier = 1.0f,
      isSanaaHeroStatus = false,
      titleEn = "Alley Rookie",
      descriptionAr = "البداية الأولى في أزقة صنعاء القديمة، سرعة الحركة والمراوغة الأساسية.",
      perksAr = listOf(
        "سرعة الركض والمراوغة الأساسية 1.0x",
        "القدرة على القفز والتخفي خلف الأبواب العتيقة"
      )
    ),
    HeroProgressionTier(
      tierIndex = 1,
      requiredMissions = 1,
      statusTitleAr = "عدّاء أزقة باب اليمن",
      badgeEmoji = "🏃‍♂️",
      speedBoostPercent = 10,
      speedMultiplier = 1.10f,
      isSanaaHeroStatus = false,
      titleEn = "Bab Al-Yemen Runner",
      descriptionAr = "سرعة حركة إضافية +10% بعد اجتياز أول مهمة هروب ومطاردة بنجاح.",
      perksAr = listOf(
        "زيادة سرعة الحركة والركض بنسبة +10%",
        "تسارع وتيرة استعادة طاقة الأدرينالين",
        "تخطي أسرع للعوائق الطينية وحواجز التفتيش"
      )
    ),
    HeroProgressionTier(
      tierIndex = 2,
      requiredMissions = 3,
      statusTitleAr = "صقر أسطح القمريات",
      badgeEmoji = "🦅",
      speedBoostPercent = 20,
      speedMultiplier = 1.20f,
      isSanaaHeroStatus = false,
      titleEn = "Rooftop Falcon",
      descriptionAr = "سرعة مراوغة وركض فائقة +20% بعد إكمال 3 مهام ناجحة بنجاح باهر.",
      perksAr = listOf(
        "زيادة سرعة الحركة والركض بنسبة +20%",
        "انعطاف وانزلاق فائق الرشاقة في المنعطفات الضيقة",
        "مقاومة أعلى للعرقلة عند القفز بين الأسطح"
      )
    ),
    HeroProgressionTier(
      tierIndex = 3,
      requiredMissions = 5,
      statusTitleAr = "بطل صنعاء الأسطوري (Sana'a Hero)",
      badgeEmoji = "👑",
      speedBoostPercent = 35,
      speedMultiplier = 1.35f,
      isSanaaHeroStatus = true,
      titleEn = "Sana'a Hero",
      descriptionAr = "فتح رتبة 'بطل صنعاء' الأسطورية! أقصى سرعة حركة +35% وهالة ذهبية خاصة للبطل.",
      perksAr = listOf(
        "وسام ولقب 'بطل صنعاء' (Sana'a Hero) الأسطوري 👑",
        "أقصى تعزيز لسرعة الحركة والركض والتفحيط بنسبة +35%",
        "هالة ذهبية صنعانية ملكية تضيء طريق الهروب في الأزقة",
        "احترام كافة أفراد الحارات وأطفال الأزقة"
      )
    )
  )

  /**
   * Returns current tier based on missions completed.
   * If developer mode is active (mazengalab), automatically grants 'Sana'a Hero' maximum tier.
   */
  fun getTier(successfulMissions: Int, isDevMode: Boolean = false): HeroProgressionTier {
    if (isDevMode) return tiers.last()
    return tiers.lastOrNull { successfulMissions >= it.requiredMissions } ?: tiers.first()
  }

  /**
   * Returns next tier to unlock, or null if maximum tier ('Sana'a Hero') is already achieved.
   */
  fun getNextTier(successfulMissions: Int, isDevMode: Boolean = false): HeroProgressionTier? {
    if (isDevMode) return null
    return tiers.firstOrNull { it.requiredMissions > successfulMissions }
  }

  /**
   * Calculates progress percentage towards the next progression tier (0.0f to 1.0f).
   */
  fun getProgressToNextTier(successfulMissions: Int, isDevMode: Boolean = false): Float {
    if (isDevMode) return 1.0f
    val current = getTier(successfulMissions, false)
    val next = getNextTier(successfulMissions, false) ?: return 1.0f

    val span = next.requiredMissions - current.requiredMissions
    val progress = successfulMissions - current.requiredMissions
    return (progress.toFloat() / span.toFloat()).coerceIn(0.0f, 1.0f)
  }

  /**
   * Returns the speed multiplier factor (1.0f .. 1.35f) applied directly to player movement speed.
   */
  fun getSpeedMultiplier(successfulMissions: Int, isDevMode: Boolean = false): Float {
    return getTier(successfulMissions, isDevMode).speedMultiplier
  }
}
