package com.example.model

/**
 * Data models for "Anderson City" (مدينة أندرسون الساحلية - ثمانينيات/تسعينيات القرن الماضي).
 * Chronicles Thomas "Tom" Anderson's rise to reclaim his syndicate empire.
 */

enum class AndersonDistrictId(
  val titleAr: String,
  val titleEn: String,
  val descriptionAr: String,
  val dangerLevel: String,
  val iconEmoji: String,
  val themeColorHex: Long
) {
  DOWNTOWN(
    titleAr = "وسط المدينة المالي",
    titleEn = "Downtown Anderson",
    descriptionAr = "ناطحات سحاب، مراكز بنوك مالية، سيارات فارهة، وحضور مكثف للشرطة ووحدات التدخل السريع.",
    dangerLevel = "عالية جداً (High Security)",
    iconEmoji = "🏙️",
    themeColorHex = 0xFF0284C7
  ),
  DOCKS(
    titleAr = "أرصفة ومستودعات الميناء",
    titleEn = "Anderson Docks",
    descriptionAr = "الموانئ البحرية، رافعات الشحن، ومستودعات التهريب غير المشروع وساحات حرب العصابات الليلية.",
    dangerLevel = "منطقة حرب عصابات (Contested)",
    iconEmoji = "⚓",
    themeColorHex = 0xFFD97706
  ),
  SUBURBS_SLUMS(
    titleAr = "الضواحي والأحياء الشعبية",
    titleEn = "Suburbs & Slums",
    descriptionAr = "الأطراف السكنية والمنازل المتهالكة؛ نقطة البداية لتوم أندرسون لتجنيد الموالين وبناء القاعدة الأولى.",
    dangerLevel = "متوسطة (Gang Territory)",
    iconEmoji = "🏚️",
    themeColorHex = 0xFF10B981
  )
}

data class DistrictControlState(
  val district: AndersonDistrictId,
  val andersonControlPercent: Int, // 0 to 100%
  val rivalControlPercent: Int,
  val dailyRevenueCash: Int,
  val activeBusinesses: List<String>
)

data class AndersonVehicle(
  val id: String,
  val nameAr: String,
  val eraYear: String,
  val categoryAr: String,
  val topSpeedMph: Int,
  val armorRating: Int, // 1 - 100
  val accelerationRating: Int, // 1 - 100
  val priceCash: Int,
  val descriptionAr: String,
  val iconEmoji: String,
  val isOwned: Boolean = false
)

data class AndersonWeapon(
  val id: String,
  val nameAr: String,
  val caliberAr: String,
  val damageRating: Int, // 1 - 100
  val fireRateRpm: Int,
  val magazineSize: Int,
  val priceCash: Int,
  val descriptionAr: String,
  val iconEmoji: String,
  val isEquipped: Boolean = false
)

data class DialogueLine(
  val speakerNameAr: String,
  val speakerRoleAr: String,
  val textAr: String,
  val speakerAvatarEmoji: String
)

data class AndersonMission(
  val id: String,
  val titleAr: String,
  val titleEn: String,
  val district: AndersonDistrictId,
  val rewardCash: Int,
  val respectReward: Int,
  val initialWantedStars: Int,
  val synopsisAr: String,
  val objectiveAr: String,
  val dialogues: List<DialogueLine>,
  val isUnlocked: Boolean = true,
  val isCompleted: Boolean = false
)

data class AndersonPlayerState(
  val name: String = "توماس 'توم' أندرسون (Tom Anderson)",
  val titleAr: String = "المحارب القديم وزعيم عائلة أندرسون",
  val cashBalance: Int = 18500,
  val familyRespect: Int = 45, // 0 - 100
  val wantedStars: Int = 1, // 0 - 5
  val currentDistrict: AndersonDistrictId = AndersonDistrictId.SUBURBS_SLUMS,
  val equippedWeaponId: String = "magnum_44",
  val activeVehicleId: String = "stallion_1986",
  val totalTurfLiberated: Int = 1
)
