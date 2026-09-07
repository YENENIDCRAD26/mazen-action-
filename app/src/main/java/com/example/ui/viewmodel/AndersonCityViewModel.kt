package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndersonCityViewModel : ViewModel() {

  data class AndersonCityUiState(
    val player: AndersonPlayerState = AndersonPlayerState(),
    val districts: List<DistrictControlState> = defaultDistricts(),
    val vehicles: List<AndersonVehicle> = defaultVehicles(),
    val weapons: List<AndersonWeapon> = defaultWeapons(),
    val missions: List<AndersonMission> = defaultMissions(),
    val activeMission: AndersonMission? = null,
    val currentDialogueIndex: Int = 0,
    val statusNotificationAr: String = "أهلاً بك في مدينة أندرسون الساحلية (1988) - ابدأ رحلتك لاستعادة السيطرة!"
  )

  private val _uiState = MutableStateFlow(AndersonCityUiState())
  val uiState: StateFlow<AndersonCityUiState> = _uiState.asStateFlow()

  fun selectDistrict(districtId: AndersonDistrictId) {
    _uiState.update { state ->
      state.copy(
        player = state.player.copy(currentDistrict = districtId),
        statusNotificationAr = "انتقلت إلى ${districtId.titleAr} (${districtId.titleEn})"
      )
    }
  }

  fun setWantedStars(stars: Int) {
    val clamped = stars.coerceIn(0, 5)
    _uiState.update { state ->
      val warning = when (clamped) {
        0 -> "الشرطة لا تطاردك - وضع آمن 🟢"
        1 -> "نجمة 1: دوريات شرطة الشوارع تترصد تحركاتك 🚓"
        2 -> "نجمتان 2: سيارات اعتراض سريعة تطاردك في التقاطعات 🚨"
        3 -> "3 نجوم: مروحية شرطة أندرسون تحلق وتغلق الممرات 🚁"
        4 -> "4 نجوم: وحدات التدخل السريع SWAT وحواجز شاحنات مصفحة 🛑"
        5 -> "5 نجوم: استنفار أمني مطلق وحصار كامل لمدينة أندرسون! 💥"
        else -> ""
      }
      state.copy(
        player = state.player.copy(wantedStars = clamped),
        statusNotificationAr = warning
      )
    }
  }

  fun startMission(mission: AndersonMission) {
    _uiState.update { state ->
      state.copy(
        activeMission = mission,
        currentDialogueIndex = 0,
        player = state.player.copy(
          currentDistrict = mission.district,
          wantedStars = mission.initialWantedStars
        ),
        statusNotificationAr = "بدأت المهمة: ${mission.titleAr} (${mission.titleEn})"
      )
    }
  }

  fun nextDialogue() {
    val current = _uiState.value
    val mission = current.activeMission ?: return
    val nextIdx = current.currentDialogueIndex + 1
    if (nextIdx < mission.dialogues.size) {
      _uiState.update { it.copy(currentDialogueIndex = nextIdx) }
    } else {
      completeActiveMission()
    }
  }

  fun completeActiveMission() {
    _uiState.update { state ->
      val mission = state.activeMission ?: return@update state
      val targetDistrict = mission.district

      // Update district control
      val updatedDistricts = state.districts.map { d ->
        if (d.district == targetDistrict) {
          val newControl = (d.andersonControlPercent + 30).coerceAtMost(100)
          val newRival = (100 - newControl).coerceAtLeast(0)
          d.copy(
            andersonControlPercent = newControl,
            rivalControlPercent = newRival,
            dailyRevenueCash = d.dailyRevenueCash + 1500
          )
        } else d
      }

      // Mark mission complete
      val updatedMissions = state.missions.map { m ->
        if (m.id == mission.id) m.copy(isCompleted = true) else m
      }

      state.copy(
        activeMission = null,
        currentDialogueIndex = 0,
        districts = updatedDistricts,
        missions = updatedMissions,
        player = state.player.copy(
          cashBalance = state.player.cashBalance + mission.rewardCash,
          familyRespect = (state.player.familyRespect + mission.respectReward).coerceAtMost(100),
          wantedStars = 0
        ),
        statusNotificationAr = "نجحت المهمة! استرداد أراضي عائلة أندرسون (+${mission.rewardCash}$ و +${mission.respectReward} احترام) 🏆"
      )
    }
  }

  fun dismissMission() {
    _uiState.update { it.copy(activeMission = null, currentDialogueIndex = 0) }
  }

  fun selectVehicle(vehicleId: String) {
    _uiState.update { state ->
      state.copy(
        player = state.player.copy(activeVehicleId = vehicleId),
        statusNotificationAr = "تم ركوب السيارة: ${state.vehicles.find { it.id == vehicleId }?.nameAr}"
      )
    }
  }

  fun equipWeapon(weaponId: String) {
    _uiState.update { state ->
      val updatedWeapons = state.weapons.map { w ->
        w.copy(isEquipped = w.id == weaponId)
      }
      state.copy(
        weapons = updatedWeapons,
        player = state.player.copy(equippedWeaponId = weaponId),
        statusNotificationAr = "تم تجهيز السلاح: ${state.weapons.find { it.id == weaponId }?.nameAr}"
      )
    }
  }

  companion object {
    fun defaultDistricts(): List<DistrictControlState> = listOf(
      DistrictControlState(
        district = AndersonDistrictId.SUBURBS_SLUMS,
        andersonControlPercent = 65,
        rivalControlPercent = 35,
        dailyRevenueCash = 3200,
        activeBusinesses = listOf("ورشة إصلاح سيارات أندرسون", "نادي البلياردو القديم", "مستودع وقود الضواحي")
      ),
      DistrictControlState(
        district = AndersonDistrictId.DOCKS,
        andersonControlPercent = 25,
        rivalControlPercent = 75,
        dailyRevenueCash = 5800,
        activeBusinesses = listOf("رصيف الشحن رقم 4", "مستودع تبريد الأسماك والتهريب")
      ),
      DistrictControlState(
        district = AndersonDistrictId.DOWNTOWN,
        andersonControlPercent = 10,
        rivalControlPercent = 90,
        dailyRevenueCash = 12500,
        activeBusinesses = listOf("برج أندرسون المالي القديم (محتل من المنافسين)")
      )
    )

    fun defaultVehicles(): List<AndersonVehicle> = listOf(
      AndersonVehicle(
        id = "stallion_1986",
        nameAr = "أندرسون ستاليون جي تي 1986 (Stallion GT)",
        eraYear = "1986",
        categoryAr = "سيارة عضلية كلاسيكية (Muscle V8)",
        topSpeedMph = 142,
        armorRating = 65,
        accelerationRating = 85,
        priceCash = 0,
        descriptionAr = "السيارة المفضلة لتوم أندرسون؛ صوت محرك V8 صاخب وثبات قوي على الطرق الساحلية.",
        iconEmoji = "🏎️",
        isOwned = true
      ),
      AndersonVehicle(
        id = "midnight_1991",
        nameAr = "ميدنايت توربو 1991 (Midnight Turbo)",
        eraYear = "1991",
        categoryAr = "سيدان يابانية معدلة (Tuned JDM)",
        topSpeedMph = 158,
        armorRating = 45,
        accelerationRating = 94,
        priceCash = 12000,
        descriptionAr = "سرعة خاطفة في شوارع وسط المدينة للمناورة وتجاوز حواجز شرطة أندرسون.",
        iconEmoji = "🚗",
        isOwned = true
      ),
      AndersonVehicle(
        id = "cargo_van_1984",
        nameAr = "فان التهريب الثقيل 1984 (Cargo Enforcer)",
        eraYear = "1984",
        categoryAr = "حافلة دعم مصفحة (Armored Van)",
        topSpeedMph = 95,
        armorRating = 92,
        accelerationRating = 40,
        priceCash = 16500,
        descriptionAr = "جدران فولاذية مصفحة تتحمل إطلاق النار الكثيف أثناء السطو على مستودعات الميناء.",
        iconEmoji = "🚐",
        isOwned = false
      ),
      AndersonVehicle(
        id = "police_interceptor",
        nameAr = "سيارة دورية أندرسون المصادرة (Interceptor Cruiser)",
        eraYear = "1990",
        categoryAr = "دورية شرطة معدلة (Police Spec)",
        topSpeedMph = 148,
        armorRating = 72,
        accelerationRating = 80,
        priceCash = 22000,
        descriptionAr = "مركبة شرطة سريعة مزودة بجهاز لاسلكي للتنصت على موجات دوريات شرطة المدينة.",
        iconEmoji = "🚓",
        isOwned = false
      )
    )

    fun defaultWeapons(): List<AndersonWeapon> = listOf(
      AndersonWeapon(
        id = "magnum_44",
        nameAr = "مسدس ريفولفر ماغنوم .44 (Steel Magnum)",
        caliberAr = ".44 Magnum",
        damageRating = 88,
        fireRateRpm = 110,
        magazineSize = 6,
        priceCash = 0,
        descriptionAr = "سلاح توم الشخصي منذ خدمته العسكرية؛ طلقة واحدة كفيلة بإنهاء النزاع فوراً.",
        iconEmoji = "🔫",
        isEquipped = true
      ),
      AndersonWeapon(
        id = "tommy_smg",
        nameAr = "رشاش شيكاغو الكلاسيكي (1928 Drum SMG)",
        caliberAr = ".45 ACP",
        damageRating = 74,
        fireRateRpm = 650,
        magazineSize = 50,
        priceCash = 6500,
        descriptionAr = "خزنة دائرية ضخمة؛ سلاح العصابات الأسطوري لحسم اشتباكات المستودعات.",
        iconEmoji = "💥",
        isEquipped = false
      ),
      AndersonWeapon(
        id = "spas_shotgun",
        nameAr = "بندقية اقتحام شوزن (Tactical SPAS-12)",
        caliberAr = "12 Gauge",
        damageRating = 95,
        fireRateRpm = 140,
        magazineSize = 8,
        priceCash = 8200,
        descriptionAr = "قوة تدميرية قصوى في القتال القريب واقتحام مقرات العصابات المنافسة.",
        iconEmoji = "🧨",
        isEquipped = false
      ),
      AndersonWeapon(
        id = "m16_rifle",
        nameAr = "بندقية هجومية عسكرية (M16A2 Assault)",
        caliberAr = "5.56mm NATO",
        damageRating = 82,
        fireRateRpm = 750,
        magazineSize = 30,
        priceCash = 14000,
        descriptionAr = "دقة متناهية ومدى إطلاق بعيد لمواجهة قناصة العصابات في أرصفة الميناء.",
        iconEmoji = "🎯",
        isEquipped = false
      )
    )

    fun defaultMissions(): List<AndersonMission> = listOf(
      AndersonMission(
        id = "mission_welcome",
        titleAr = "المهمة الأولى: العودة إلى أندرسون",
        titleEn = "Welcome to Anderson",
        district = AndersonDistrictId.SUBURBS_SLUMS,
        rewardCash = 4500,
        respectReward = 15,
        initialWantedStars = 1,
        synopsisAr = "يعود توم أندرسون بعد 7 سنوات ليجد حيه القديم تحت سيطرة عصابة الكوبرا، وعليه تفادي الدورية الأولى والوصول للمخبأ العائلي.",
        objectiveAr = "قُد سيارة Stallion GT الكلاسيكية، راوغ دوريات الشرطة، والتقِ بـ 'سال ماروني' في المخبأ القديم.",
        dialogues = listOf(
          DialogueLine(
            speakerNameAr = "توماس 'توم' أندرسون",
            speakerRoleAr = "البطل - زعيم العائلة العائد",
            textAr = "سبع سنوات في المنفى.. ورائحة البحر والبارود في أندرسون لم تتغير أبداً.",
            speakerAvatarEmoji = "🕶️"
          ),
          DialogueLine(
            speakerNameAr = "سال ماروني",
            speakerRoleAr = "ميكانيكي العائلة القديم والوفي",
            textAr = "توم! يا إلهي.. اعتقدت أنهم قتلوك! عصابة الكوبرا استولت على الميناء وفرضت الإتاوات على الجميع!",
            speakerAvatarEmoji = "🔧"
          ),
          DialogueLine(
            speakerNameAr = "توماس 'توم' أندرسون",
            speakerRoleAr = "البطل - زعيم العائلة العائد",
            textAr = "اهدأ يا سال. أنا هنا لاستعادة كل شبر من هذه المدينة.. عائلة أندرسون لم تمت بعد.",
            speakerAvatarEmoji = "🕶️"
          )
        )
      ),
      AndersonMission(
        id = "mission_docks_heist",
        titleAr = "مهمة منتصف اللعبة: سطو أرصفة الميناء",
        titleEn = "Dockside Heist",
        district = AndersonDistrictId.DOCKS,
        rewardCash = 9500,
        respectReward = 25,
        initialWantedStars = 2,
        synopsisAr = "وصول سفينة شحن روسية محملة بأسلحة ثقيلة لميناء أندرسون؛ يجب التسلل قبل عصابة الكوبرا ومصادرة الحاوية بالكامل.",
        objectiveAr = "تسلل إلى الرصيف 4، اعترض شحنة الأسلحة، واقضِ على حراس الكوبرا واهرب بالفان المصفح.",
        dialogues = listOf(
          DialogueLine(
            speakerNameAr = "المحقق فانس (Vance)",
            speakerRoleAr = "ضابط شرطة الميناء الفاسد",
            textAr = "أندرسون! هذا ليس عام 1980.. لن تخرج حياً من هذا الرصيف مع الشحنة!",
            speakerAvatarEmoji = "👮"
          ),
          DialogueLine(
            speakerNameAr = "توماس 'توم' أندرسون",
            speakerRoleAr = "البطل - زعيم العائلة العائد",
            textAr = "أنت تأخذ رشوتك من الغرباء يا فانس، وقد نسيت من الذي بنى هذه الأرصفة في الأصل.",
            speakerAvatarEmoji = "🕶️"
          ),
          DialogueLine(
            speakerNameAr = "سال ماروني",
            speakerRoleAr = "الدعم اللوجستي اللاسلكي",
            textAr = "توم، تم تحميل الصناديق في الفان المصفح! انطلق قبل وصول تعزيزات الشرطة!",
            speakerAvatarEmoji = "🔧"
          )
        )
      ),
      AndersonMission(
        id = "mission_takeover",
        titleAr = "المهمة النهائية: استعادة إمبراطورية أندرسون",
        titleEn = "The Anderson Takeover",
        district = AndersonDistrictId.DOWNTOWN,
        rewardCash = 25000,
        respectReward = 50,
        initialWantedStars = 4,
        synopsisAr = "هجوم شامل ومنسق على المقر الرئيسي لزعيم العصابة المنافسة في ناطحة السحاب بوسط مدينة أندرسون لإعلان النصر الكامل.",
        objectiveAr = "اقتحم البرج المالي، واجه زعيم الكوبرا 'فالنتينو' شخصياً، وافرض السيطرة المطلقة على مدينة أندرسون.",
        dialogues = listOf(
          DialogueLine(
            speakerNameAr = "فالنتينو (زعيم الكوبرا)",
            speakerRoleAr = "حاكم الجريمة الحالي في وسط المدينة",
            textAr = "ظننت أنك أذكى من ذلك يا توم.. القدوم إلى برجي بمفردك هو انتحار صريح!",
            speakerAvatarEmoji = "🐍"
          ),
          DialogueLine(
            speakerNameAr = "توماس 'توم' أندرسون",
            speakerRoleAr = "البطل - زعيم العائلة العائد",
            textAr = "لست بمفردي.. كل حي وشارع في أندرسون تذكر الليلة من هم أصحابه الحقيقيون. انتهت لعبتك يا فالنتينو.",
            speakerAvatarEmoji = "🕶️"
          ),
          DialogueLine(
            speakerNameAr = "توماس 'توم' أندرسون",
            speakerRoleAr = "البطل - زعيم العائلة العائد",
            textAr = "المدينة عادت لعائلة أندرسون. فلتبدأ الحقبة الجديدة!",
            speakerAvatarEmoji = "👑"
          )
        )
      )
    )
  }
}
