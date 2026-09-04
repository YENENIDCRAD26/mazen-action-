package com.example.model

enum class Faction(val titleAr: String, val badgeAr: String) {
  GANG("عصابة المشاغبين الصغار", "المتمردون الأذكياء"),
  POLICE("شرطة العاصمة صنعاء", "حماة النظام والأمن")
}

enum class GameScreen {
  MAIN_MENU,
  MAIN_GAME,
  GTA_UNIFIED_ENGINE,
  GTA_SANAA_7D,
  GTA_BEIRUT_3D,
  CHASE_GAME,
  VEHICLE_HEIST,
  HIDEOUT_TACTICS,
  TACTICAL_XCOM,
  CHARACTER_DOSSIER,
  STORY_GALLERY,
  HQ_UPGRADES,
  LEADERBOARD
}

enum class TacticalItemCategory(val titleAr: String, val iconEmoji: String) {
  OFFENSIVE("أسلحة الهجوم وإلحاق الضرر", "🎯"),
  CROWD_CONTROL("أدوات شل الحركة والفخاخ", "🛑"),
  SUPPORT("أدوات التشتيت والدعم التكتيكي", "🎒")
}

data class TacticalArmoryItem(
  val id: String,
  val nameAr: String,
  val category: TacticalItemCategory,
  val priceCoins: Int,
  val apCost: Int,
  val iconEmoji: String,
  val typeDescAr: String,
  val tacticalEffectAr: String,
  val rangeTiles: Int = 3,
  val damageOrMorale: Int = 25
)

enum class TacticalSpecialistType(val titleAr: String, val roleDescAr: String, val iconEmoji: String, val baseAp: Int, val hireCost: Int) {
  SPEED_SCOUT("الطفل الركّيض (العداء السريع)", "3 نقاط حركة AP، سرعة فائقة لحمل الأدوات وقشور الموز والتسلل", "🏃‍♂️", 3, 400),
  ROOFTOP_SNIPER("الطفل القناص (صاحب المقلاع)", "مدى رمي بعيد بالمقلاع الحجري، بونص إصابة 100% فوق أسطح القمريات", "🧗‍♂️", 2, 450),
  CARJACKER_MECHANIC("الطفل الميكانيكي (سارق الدبابات)", "اقتحام فوري للدباب وتشغيله بـ 1 AP وإصلاح المركبات المصدومة", "🚐", 2, 500),
  DISTRACTOR("الطفل المشتت (خبير المفرقعات)", "إلقاء علب الحليب ومفرقعات الطماق لإلغاء مراقبة Overwatch للشرطة", "🧨", 2, 350)
}

enum class MapZoneCategory(val titleAr: String, val descAr: String, val iconEmoji: String) {
  OLD_SANAA_NARROW_ALLEY("أزقة صنعاء القديمة الضيقة", "ممرات متعرجة ومبانٍ طينية توفر احتواءً كبيراً وتسللاً مثالياً", "🏰"),
  COVER_FORTIFIED_ZONE("مناطق السواتر والتحصينات", "عربات خضار وبطاطس، أكياس بهارات، ودبابات توفر حماية 50% - 100%", "🥔"),
  MODERN_OPEN_STREET("المساحات المفتوحة والشوارع الحديثة", "جولات وشوارع إسفلتية واسعة تكشف الرؤية وتزيد من خطورة نيران الشرطة", "🚦")
}

enum class TacticalCoverType(val percent: Int, val labelAr: String) {
  NONE(0, "مكشوف 0%"),
  HALF(50, "احتماء جزئي 50%"),
  FULL(100, "احتماء كامل 100%")
}

enum class TacticalTileType(
  val iconEmoji: String,
  val nameAr: String,
  val cover: TacticalCoverType,
  val isPassable: Boolean = true,
  val blocksLineOfSight: Boolean = false,
  val zoneCategory: MapZoneCategory = MapZoneCategory.OLD_SANAA_NARROW_ALLEY
) {
  ALLEY_ROAD("▫️", "أزقة صنعاء الضيقة", TacticalCoverType.NONE, isPassable = true, blocksLineOfSight = false, MapZoneCategory.OLD_SANAA_NARROW_ALLEY),
  OPEN_ASPHALT_ROAD("🛣️", "شارع إسفلتي مفتوح", TacticalCoverType.NONE, isPassable = true, blocksLineOfSight = false, MapZoneCategory.MODERN_OPEN_STREET),
  ROUNDABOUT_CENTER("⛲", "قلب جولة كنتاكي المفتوحة", TacticalCoverType.NONE, isPassable = true, blocksLineOfSight = false, MapZoneCategory.MODERN_OPEN_STREET),
  MUD_ROOF("🧱", "سطح طيني وقمرية", TacticalCoverType.HALF, isPassable = true, blocksLineOfSight = false, MapZoneCategory.OLD_SANAA_NARROW_ALLEY),
  POTATO_CART("🥔", "عربة بيع البطاطس الحارة", TacticalCoverType.FULL, isPassable = false, blocksLineOfSight = true, MapZoneCategory.COVER_FORTIFIED_ZONE),
  VEGGIE_STALL("🥕", "بسطة خضار وفواكه سوق الملح", TacticalCoverType.FULL, isPassable = false, blocksLineOfSight = true, MapZoneCategory.COVER_FORTIFIED_ZONE),
  SPICE_SACK("🏺", "أكياس بهارات وزعتر", TacticalCoverType.HALF, isPassable = false, blocksLineOfSight = true, MapZoneCategory.COVER_FORTIFIED_ZONE),
  MUD_WALL("🏰", "جدار طيني تاريخي ضخم", TacticalCoverType.FULL, isPassable = false, blocksLineOfSight = true, MapZoneCategory.OLD_SANAA_NARROW_ALLEY),
  POLICE_BARRICADE("🚧", "حاجز أمني حديدي مسلح", TacticalCoverType.HALF, isPassable = false, blocksLineOfSight = true, MapZoneCategory.COVER_FORTIFIED_ZONE),
  DABAB_BUS("🚐", "باص دباب صنعاني أخضر", TacticalCoverType.FULL, isPassable = true, blocksLineOfSight = true, MapZoneCategory.COVER_FORTIFIED_ZONE),
  POLICE_CAR("🚓", "سيارة دورية أمن", TacticalCoverType.FULL, isPassable = true, blocksLineOfSight = true, MapZoneCategory.MODERN_OPEN_STREET),
  EXIT_ZONE("🏁", "نقطة الهروب الآمنة", TacticalCoverType.NONE, isPassable = true, blocksLineOfSight = false, MapZoneCategory.OLD_SANAA_NARROW_ALLEY),
  POLICE_HQ_FLAG("🚩", "مربع السيطرة والراية", TacticalCoverType.HALF, isPassable = true, blocksLineOfSight = false, MapZoneCategory.COVER_FORTIFIED_ZONE)
}

data class TacticalTile(
  val x: Int,
  val y: Int,
  val tileType: TacticalTileType,
  var hasBananaTrap: Boolean = false,
  var hasGraffitiSmoke: Boolean = false
)

data class StrategicMapGrid(
  val width: Int = 6,
  val height: Int = 6,
  val stageIndex: Int,
  val locationNameAr: String,
  val primaryZone: MapZoneCategory,
  val tiles: List<TacticalTile>
)

data class TacticalUnit(
  val id: String,
  val nameAr: String,
  val faction: Faction,
  val iconEmoji: String,
  var hp: Int = 100,
  val maxHp: Int = 100,
  var ap: Int = 2,
  val maxAp: Int = 2,
  var x: Int,
  var y: Int,
  var isStunned: Boolean = false,
  var isOverwatch: Boolean = false,
  var isHostage: Boolean = false,
  var isInVehicle: Boolean = false,
  var isPanic: Boolean = false,
  var equippedItemId: String? = null,
  var specialistType: TacticalSpecialistType? = null
)

data class TacticalStageInfo(
  val stageIndex: Int,
  val id: String,
  val titleAr: String,
  val subtitleAr: String,
  val locationAr: String,
  val isTutorial: Boolean,
  val objectiveTextAr: String,
  val rewardCoins: Int,
  val iconEmoji: String
)

data class CharacterProfile(
  val id: String,
  val nameAr: String,
  val titleAr: String,
  val faction: Faction,
  val age: Int,
  val iconEmoji: String,
  val bioAr: String,
  val quoteAr: String,
  val weaponAr: String,
  val specialAbilityAr: String,
  val speedStat: Float,       // 0f..1f
  val stealthStat: Float,     // 0f..1f
  val tacticStat: Float,      // 0f..1f
  val parkourStat: Float,     // 0f..1f
  val isUnlocked: Boolean = true
)

data class StoryMoment(
  val id: Int,
  val titleAr: String,
  val categoryAr: String,
  val locationAr: String,
  val descriptionAr: String,
  val tacticalTipAr: String,
  val iconEmoji: String
)

data class SanaaLocation(
  val id: String,
  val nameAr: String,
  val typeAr: String,
  val descriptionAr: String,
  val dangerLevel: Int, // 1..5
  val iconEmoji: String
)

data class UpgradeItem(
  val id: String,
  val titleAr: String,
  val descriptionAr: String,
  val cost: Int,
  val level: Int,
  val maxLevel: Int,
  val faction: Faction,
  val iconEmoji: String
)

data class GameStats(
  val totalCoins: Int = 1250,
  val highChaseScore: Int = 0,
  val highVehicleScore: Int = 0,
  val tacticsMissionsWon: Int = 0,
  val successfulMissionsCount: Int = 0,
  val recruitedScouts: Int = 6,
  val rescuedOfficers: Int = 0,
  val soundEnabled: Boolean = true,
  val playerLevel: Int = 1,
  val playerXp: Int = 0
)

object GameData {
  val characters = listOf(
    CharacterProfile(
      id = "little_boss",
      nameAr = "الزعيم الصغير (العقل المدبر)",
      titleAr = "قائد عصابة أطفال شوارع صنعاء",
      faction = Faction.GANG,
      age = 10,
      iconEmoji = "👑",
      bioAr = "طفل عبقري يبلغ من العمر 10 سنوات، يرتدي ثوباً وشالاً مميزاً على كتفه مع نظارات شمسية سوداء وحقيبة ظهر مدرسية مليئة بالخطط والمعدات. يمتلك كاريزما استثنائية وقدرة على حشد أطفال الحارات.",
      quoteAr = "«يا رجال ركزوا.. الشارع شارعنا وأسطح صنعاء لعبتنا!»",
      weaponAr = "بندقية خفيفة معدلة تناسب حجمه ومسدس تكتيكي",
      specialAbilityAr = "التجنيد الفوري: تحويل أطفال الحارات إلى جواسيس ومراقبين",
      speedStat = 0.95f,
      stealthStat = 0.92f,
      tacticStat = 0.98f,
      parkourStat = 0.96f
    ),
    CharacterProfile(
      id = "deputy_boss",
      nameAr = "نائب الزعيم ومسؤول الاستطلاع",
      titleAr = "قناص الأسطح وخبير التسلق",
      faction = Faction.GANG,
      age = 9,
      iconEmoji = "🧗‍♂️",
      bioAr = "الذراع الأيمن للزعيم، متخصص في التسلق الحر عبر النوافذ القمرية وجدران الطين، ويمتلك مهارات القفز بين البيوت في صنعاء القديمة.",
      quoteAr = "«الطريق من فوق السطح سالكة.. جهزوا الشباك للشرطي!»",
      weaponAr = "مقلاع كرات طلاء ومفرقعات صوتية",
      specialAbilityAr = "قفزة القمريات: القفز العالي وتخطي العوائق المزدوجة",
      speedStat = 0.92f,
      stealthStat = 0.88f,
      tacticStat = 0.85f,
      parkourStat = 0.99f
    ),
    CharacterProfile(
      id = "dabbab_driver",
      nameAr = "سائق الدباب والمشاغب الصغير",
      titleAr = "خبير خطف المركبات والهجولة",
      faction = Faction.GANG,
      age = 8,
      iconEmoji = "🚐",
      bioAr = "يستغل قصر قامته للاختباء داخل باصات النقل الصغيرة (الدبابات) والتاكسي الأصفر ليقودها بمهارة عالية في جولات صنعاء لتهريب الزعيم.",
      quoteAr = "«دوس على البنزين.. جولة كنتاكي تحت السيطرة!»",
      weaponAr = "قشور الموز وزيت المحركات لزحلقة الدوريات",
      specialAbilityAr = "الهجولة التكتيكية: دوران مفاجئ وتخطي الحواجز الأمنية",
      speedStat = 0.98f,
      stealthStat = 0.80f,
      tacticStat = 0.82f,
      parkourStat = 0.78f
    ),
    CharacterProfile(
      id = "scout_girl",
      nameAr = "مراقبة الأزقة وجاسوسة الحارة",
      titleAr = "مسؤولة التشتيت والمقايضة",
      faction = Faction.GANG,
      age = 8,
      iconEmoji = "🎒",
      bioAr = "تراقب تحركات دوريات الشرطة من زوايا سوق الملح، تقوم بتشتيت انتباه الضباط لتسهيل خطفهم إلى البيت الطيني المهجور.",
      quoteAr = "«الدورية قادمة من باب اليمن، انشروا الغرافيتي!»",
      weaponAr = "علب رذاذ الطلاء وألعاب نارية تشتيتية",
      specialAbilityAr = "ستار الغرافيتي: حجب رؤية الشرطة لمدة 5 ثوانٍ",
      speedStat = 0.90f,
      stealthStat = 0.96f,
      tacticStat = 0.90f,
      parkourStat = 0.88f
    ),
    CharacterProfile(
      id = "police_chief",
      nameAr = "مدير شرطة العاصمة",
      titleAr = "قائد غرفة العمليات وحامي النظام",
      faction = Faction.POLICE,
      age = 44,
      iconEmoji = "👮‍♂️",
      bioAr = "قائد محنك يرتدي الزي الرسمي ذو البيريه العسكري، يدير شبكة اللاسلكي والدوريات للسيطرة على الشغب مع الالتزام بقواعد الاشتباك لعدم إيذاء الأطفال.",
      quoteAr = "«انتباه لجميع الوحدات: طوقوا جولة كنتاكي دون استخدام القوة المفرطة!»",
      weaponAr = "جهاز اتصال لاسلكي مشفر وقنابل دخان حاشدة",
      specialAbilityAr = "طوق أمني شامل: إغلاق كافة المنافذ المحيطة بالأزقة",
      speedStat = 0.70f,
      stealthStat = 0.65f,
      tacticStat = 0.98f,
      parkourStat = 0.55f
    ),
    CharacterProfile(
      id = "rapid_officer",
      nameAr = "ضابط التدخل السريع",
      titleAr = "مسؤول تحرير الرهائن والمهام الخاصة",
      faction = Faction.POLICE,
      age = 35,
      iconEmoji = "🛡️",
      bioAr = "يرتدي الدرع التكتيكي الخفيف والسترة الزرقاء، متخصص في اقتحام المباني الطينية المهجورة لتحرير زملائه الضباط المخطوفين.",
      quoteAr = "«جهزوا شباك الإمساك، سندخل عبر بوابة البيت الطيني بحذر!»",
      weaponAr = "مسدس شل حركة وشباك إمساك سريعة",
      specialAbilityAr = "شبكة الصعق الآمن: إيقاف المشاغبين بدون أذى",
      speedStat = 0.85f,
      stealthStat = 0.75f,
      tacticStat = 0.92f,
      parkourStat = 0.70f
    ),
    CharacterProfile(
      id = "traffic_cop",
      nameAr = "شرطي المرور والجولات",
      titleAr = "حارس الجولات والميادين",
      faction = Faction.POLICE,
      age = 29,
      iconEmoji = "🚦",
      bioAr = "يرتدي السترة الفسفورية ويحرس جولة كنتاكي وميدان السبعين، خبير في نصب حواجز المسامير وإيقاف المركبات المخطوفة.",
      quoteAr = "«الدباب الأصفر متجه نحو ميدان السبعين.. جهزوا الحاجز!»",
      weaponAr = "صافرة إنذار وحواجز طرق متمددة",
      specialAbilityAr = "سد الطرق: إجبار المركبات المسرعة على التباطؤ الحاد",
      speedStat = 0.80f,
      stealthStat = 0.60f,
      tacticStat = 0.86f,
      parkourStat = 0.60f
    )
  )

  val sanaaLocations = listOf(
    SanaaLocation(
      id = "old_sanaa",
      nameAr = "أزقة صنعاء القديمة",
      typeAr = "مباني طينية ونوافذ قمرية",
      descriptionAr = "أزقة ملتوية ومبانٍ تاريخية فريدة تمنح المشاغبين فرصة القفز والباركور بين الأسطح والاختباء من الدوريات.",
      dangerLevel = 4,
      iconEmoji = "🏛️"
    ),
    SanaaLocation(
      id = "souq_almelh",
      nameAr = "سوق الملح التراثي",
      typeAr = "ممرات تجارية وتوابل",
      descriptionAr = "أزقة ضيقة ممتلئة بعربات البضائع وأكياس البهارات، مثالية لرمي قشور الفواكه وإعاقة ملاحقي الشرطة.",
      dangerLevel = 3,
      iconEmoji = "🏺"
    ),
    SanaaLocation(
      id = "kentucky_roundabout",
      nameAr = "جولة كنتاكي",
      typeAr = "ميدان حيوي وشوارع حديثة",
      descriptionAr = "مسرح مطاردات الهجولة وخطف الدبابات وسيارات الأجرة وسط زحام المركبات والدوريات الرسمية.",
      dangerLevel = 5,
      iconEmoji = "🏎️"
    ),
    SanaaLocation(
      id = "sabeen_square",
      nameAr = "ميدان السبعين",
      typeAr = "شوارع واسعة ولوحات إعلانية",
      descriptionAr = "شوارع مفتوحة تتيح قيادة جنونية وتفحيط تكتيكي أثناء تبادل مفرقعات التشتيت مع فرق الدعم.",
      dangerLevel = 4,
      iconEmoji = "🛣️"
    ),
    SanaaLocation(
      id = "secret_mud_hideout",
      nameAr = "البيت الطيني المهجور (المخبأ السري)",
      typeAr = "برج صنعاني متعدد الطوابق",
      descriptionAr = "المقر السري للزعيم الصغير، حيث يتم احتجاز الضباط المخطوفين والتخطيط للعمليات القادمة.",
      dangerLevel = 5,
      iconEmoji = "🏰"
    )
  )

  val storyMoments = listOf(
    StoryMoment(1, "الزعيم الصغير يتولى القيادة", "القيادة والتنظيم", "صنعاء القديمة", "طفل الـ 10 سنوات يرتدي شال الهيبة ونظاراته السوداء معلناً سيطرته على أزقة المدينة.", "استخدم مهارة التجنيد لزيادة عدد أفراد العصابة.", "👑"),
    StoryMoment(2, "غرفة عمليات الشرطة والمتابعة", "رصد النظام", "مقر القيادة", "مدير الشرطة يتابع باللاسلكي تحركات عصابة الأطفال لتطويق الأزقة دون استخدام عنف.", "اعتمد على القنابل الدخانية وشباك الإمساك.", "👮‍♂️"),
    StoryMoment(3, "تجنيد أطفال الحارات الجدد", "بناء الشبكة", "أزقة حارة الطبري", "الزعيم يقنع صغار الحي بالانضمام ليتحولوا إلى عيون وجواسيس في زوايا الحارات.", "الجواسيس يحذرونك بوميض أحمر عند اقتراب الدورية.", "🎒"),
    StoryMoment(4, "تدريبات التسلق والباركور على الجبال", "اللياقة والمهارة", "جبل عيبان ونقم", "نائب الزعيم وأفراد الخلية يتدربون على تسلق الصخور والمباني الشاهقة.", "الباركور يتيح لك الركض فوق أسطح صنعاء بدون توقف.", "🧗‍♂️"),
    StoryMoment(5, "اقتحام النادي الرياضي", "استعراض القوة", "نادي العاصمة", "أفراد المشاغبين يقتحمون النادي لتطوير مهاراتهم الميدانية والمراوغة التكتيكية.", "تزيد من سرعة الحركة بنسبة 15%.", "🥋"),
    StoryMoment(6, "اقتحام حديقة الثورة العامة", "تشتيت الدوريات", "حديقة الثورة", "تنفيذ عمليات بهلوانية مباغتة لإرباك نقاط التفتيش في محيط الحديقة.", "استخدم الألعاب النارية لتفريغ الساحة من الدوريات.", "🎡"),
    StoryMoment(7, "خطف باص الدباب الأصفر", "هجولة الشوارع", "جولة كنتاكي", "الطفل المشاغب يقود الدباب الصنعاني بتهور ويهرب الزعيم تحت وابل المفرقعات.", "شغّل التوربو عند الانعطاف الحاد لتفادي الصدم.", "🚐"),
    StoryMoment(8, "تكتيك خطف رجل الشرطة المنفرد", "التكتيك المحكم", "سوق الملح", "تشتيت انتباه الضابط ومحاصرته بشباك الحبال ونقله للمخبأ السري.", "فريق التشتيت هو مفتاح نجاح الخطف التكتيكي.", "🪢"),
    StoryMoment(9, "الاجتماع السري والتخطيط", "العقل المدبر", "البيت الطيني", "الزعيم الصغير يضع خطة توزيع المهام على أسطح صنعاء بالطباشير والخرائط.", "اختر مواقع الجواسيس بدقة على الخريطة.", "🗺️"),
    StoryMoment(10, "شفاء الزعيم واستعادة السيطرة", "عودة الأسطورة", "صنعاء القديمة", "بعد تعافي الزعيم الصغير، تعود العصابة بحماس مضاعف لفرض نفوذها في الحارات.", "تزداد طاقة الزعيم وسرعته إلى الحد الأقصى.", "💪"),
    StoryMoment(11, "حصار الدورية ومطاردة الأسطح", "المواجهة الكبرى", "باب اليمن", "ضباط التدخل السريع يطاردون أفراد العصابة بين النوافذ القمرية والأسطح الطينية.", "القفز بين المباني يعطيك مسافة أمان من رجال الشرطة.", "🏃‍♂️"),
    StoryMoment(12, "تحرير الرهائن والمقايضة التكتيكية", "لحظة الحسم", "المخبأ السري", "الشرطة تقتحم البيت الطيني بالقنابل الدخانية لإنقاذ الضباط المحتجزين بسلام.", "عطل الفخاخ المعلقة قبل التقدم لغرفة الرهائن.", "🛡️")
  )

  val initialUpgrades = listOf(
    UpgradeItem("shoe_parkour", "حذاء الباركور الصنعاني", "زيادة سرعة القفز بين الأسطح والتسلق بنسبة 25%", 300, 1, 5, Faction.GANG, "👟"),
    UpgradeItem("graffiti_ammo", "رذاذ الغرافيتي المزدوج", "تمديد مدة حجب رؤية الشرطة وكاميرات المراقبة", 450, 1, 5, Faction.GANG, "🎨"),
    UpgradeItem("dabbab_turbo", "توربو الدباب الصنعاني", "تسريع الانطلاق وتفحيط أسرع في جولة كنتاكي", 600, 1, 5, Faction.GANG, "⚡"),
    UpgradeItem("banana_traps", "قشور الموز الفائقة", "إسقاط وتزحلق دوريات الشرطة والسيارات المطاردة", 250, 1, 5, Faction.GANG, "🍌"),
    UpgradeItem("scout_whistle", "صافرة التجنيد السريع", "استدعاء 3 أطفال جواسيس فوريين في أي حارة", 500, 1, 5, Faction.GANG, "📢"),
    UpgradeItem("police_net", "شباك الإمساك الآلية", "إطلاق شباك واسعة لشل حركة المشاغبين بسرعة", 400, 1, 5, Faction.POLICE, "🕸️"),
    UpgradeItem("smoke_canister", "قنابل الدخان الكثيف", "كشف الفخاخ وتسهيل اقتحام البيت الطيني", 350, 1, 5, Faction.POLICE, "💨"),
    UpgradeItem("police_drone", "درون الاستطلاع الجوي", "مسح أسطح صنعاء القديمة وكشف أماكن الزعيم", 700, 1, 5, Faction.POLICE, "🛸")
  )

  val armoryItems = listOf(
    // Offensive
    TacticalArmoryItem(
      id = "bb_gun",
      nameAr = "مسدس الخرز المعدّل",
      category = TacticalItemCategory.OFFENSIVE,
      priceCoins = 500,
      apCost = 2,
      iconEmoji = "🔫",
      typeDescAr = "سلاح مدى متوسط",
      tacticalEffectAr = "يستهلك 2 AP، يلحق ضرراً بمعنويات الشرطي مع فرصة 15% للعمى المؤقت لدور كامل",
      rangeTiles = 4,
      damageOrMorale = 35
    ),
    TacticalArmoryItem(
      id = "stone_sling",
      nameAr = "المقلاع الصنعاني الحجري",
      category = TacticalItemCategory.OFFENSIVE,
      priceCoins = 300,
      apCost = 1,
      iconEmoji = "🪨",
      typeDescAr = "سلاح بعيد المدى",
      tacticalEffectAr = "يستخدم حجارة صنعاء القديمة، ممتاز لكسر كشافات الشرطة وإنشاء مناطق تسلل آمنة",
      rangeTiles = 5,
      damageOrMorale = 25
    ),
    TacticalArmoryItem(
      id = "fireworks_tammaq",
      nameAr = "الألعاب النارية والمفرقعات (الطماق والشنير)",
      category = TacticalItemCategory.OFFENSIVE,
      priceCoins = 600,
      apCost = 2,
      iconEmoji = "🧨",
      typeDescAr = "سلاح مساحي وتشتيتي (AOE)",
      tacticalEffectAr = "صوت صاخب ودخان كثيف يجبر أفراد الشرطة في المربعات على إلغاء وضعية المراقبة (Overwatch)",
      rangeTiles = 3,
      damageOrMorale = 45
    ),
    // Crowd Control & Traps
    TacticalArmoryItem(
      id = "banana_peels",
      nameAr = "قشور الموز وبقايا المانجو",
      category = TacticalItemCategory.CROWD_CONTROL,
      priceCoins = 100,
      apCost = 1,
      iconEmoji = "🍌",
      typeDescAr = "فخ أرضي بالممرات الضيقة",
      tacticalEffectAr = "أي شرطي يمر فوقه يتزحلق فوراً ويفقد دوره بالكامل (Stun) مما يسهل خطفه تكتيكياً",
      rangeTiles = 1,
      damageOrMorale = 10
    ),
    TacticalArmoryItem(
      id = "spray_graffiti",
      nameAr = "علب الطلاء السائل (الغرافيتي بخاخ)",
      category = TacticalItemCategory.CROWD_CONTROL,
      priceCoins = 400,
      apCost = 1,
      iconEmoji = "🎨",
      typeDescAr = "أداة حجب رؤية الشرطة",
      tacticalEffectAr = "يرش على زجاج الدورية أو خوذة الشرطي ليقلص مدى رؤيته إلى مربع واحد فقط لدورين",
      rangeTiles = 2,
      damageOrMorale = 20
    ),
    TacticalArmoryItem(
      id = "clothesline_trap",
      nameAr = "حبال الغسيل المتينة",
      category = TacticalItemCategory.CROWD_CONTROL,
      priceCoins = 350,
      apCost = 1,
      iconEmoji = "🪢",
      typeDescAr = "فخ للدراجات والدوريات",
      tacticalEffectAr = "يربط بين بيتين متقابلين لإسقاط دراجات الشرطة السريعة ومصادرتها فوراً",
      rangeTiles = 1,
      damageOrMorale = 30
    ),
    // Distraction & Support
    TacticalArmoryItem(
      id = "milk_cans",
      nameAr = "علب «الحليب الممتاز» الصنعانية الفارغة",
      category = TacticalItemCategory.SUPPORT,
      priceCoins = 150,
      apCost = 1,
      iconEmoji = "🥫",
      typeDescAr = "أداة تشتيت صوتي ذكية",
      tacticalEffectAr = "تصدر ضجيجاً في زاوية معينة لجذب ذكاء الشرطة الصناعي بعيداً وفتح ممر تسلل آمن",
      rangeTiles = 4,
      damageOrMorale = 15
    ),
    TacticalArmoryItem(
      id = "school_megaphone",
      nameAr = "مكبر الصوت المدرسي الصغير",
      category = TacticalItemCategory.SUPPORT,
      priceCoins = 700,
      apCost = 2,
      iconEmoji = "📢",
      typeDescAr = "أداة تعزيز معنويات للزعيم",
      tacticalEffectAr = "خطاب حماسي للزعيم الصغير يمنح جميع أطفال الخريطة +1 AP إضافي لدورين متتاليين",
      rangeTiles = 6,
      damageOrMorale = 50
    ),
    TacticalArmoryItem(
      id = "armor_backpack",
      nameAr = "حقيبة الظهر المدرسية الواقية",
      category = TacticalItemCategory.SUPPORT,
      priceCoins = 900,
      apCost = 0,
      iconEmoji = "🎒",
      typeDescAr = "درع تكتيكي خفيف بالكتب",
      tacticalEffectAr = "تمتص صدمة أو رصاصة شل الحركة بالكامل لمرة واحدة أثناء الهروب",
      rangeTiles = 0,
      damageOrMorale = 0
    )
  )

  val tacticalStages = listOf(
    TacticalStageInfo(
      stageIndex = 1,
      id = "stage_1_tutorial",
      titleAr = "المرحلة التعليمية: «عملية الدباب الأول»",
      subtitleAr = "حارة صنعاء القديمة • تعلم التسلل والاحتماء وخطف الباص",
      locationAr = "أزقة حارة الفليحي - صنعاء القديمة",
      isTutorial = true,
      objectiveTextAr = "احتمِ خلف عربة البطاطس، تسلل دون كشف، اقتحم باص الدباب واهرب لنقطة النهاية 🏁",
      rewardCoins = 300,
      iconEmoji = "🚐"
    ),
    TacticalStageInfo(
      stageIndex = 2,
      id = "stage_2_kentucky",
      titleAr = "المرحلة الثانية: «معركة جولة كنتاكي»",
      subtitleAr = "صنعاء الحديثة • هجولة الدباب واختراق الحواجز الأمنية والشاصات",
      locationAr = "جولة كنتاكي وشارع الزبيري",
      isTutorial = false,
      objectiveTextAr = "اخترق حواجز شرطة المرور، وانصب فخاخ الموز والغرافيتي واقضِ على الدورية 🚦",
      rewardCoins = 450,
      iconEmoji = "🏎️"
    ),
    TacticalStageInfo(
      stageIndex = 3,
      id = "stage_3_station_siege",
      titleAr = "المرحلة الثالثة: «حصار واقتحام قسم صنعاء القديمة»",
      subtitleAr = "السيطرة على المربعات الأمنية ورفع راية العصابة",
      locationAr = "محيط قسم شرطة صنعاء القديمة",
      isTutorial = false,
      objectiveTextAr = "سيطر على مربع الراية والقيادة لـ 3 أدوار متتالية لطرد القوات وفرض السيطرة 🚩",
      rewardCoins = 600,
      iconEmoji = "🏰"
    ),
    TacticalStageInfo(
      stageIndex = 4,
      id = "stage_4_mud_tower_rescue",
      titleAr = "المرحلة الرابعة: «اقتحام البرج الطيني وتحرير الرهائن»",
      subtitleAr = "مواجهة الطوابق • تحرير الضباط أو تأمين فدية المليار ريال",
      locationAr = "البيت الطيني المهجور - باب اليمن",
      isTutorial = false,
      objectiveTextAr = "داهم المقر، عطل الفخاخ، وحرر الضباط المحتجزين أو أمن هروبهم بالدباب 🪢",
      rewardCoins = 750,
      iconEmoji = "🛡️"
    ),
    TacticalStageInfo(
      stageIndex = 5,
      id = "stage_5_great_escape",
      titleAr = "المرحلة الخامسة: «الهروب الكبير للزعيم»",
      subtitleAr = "المطاردة الكبرى • نقل الزعيم الصغير نحو طريق مناخة",
      locationAr = "طريق مناخة ومخارج العاصمة",
      isTutorial = false,
      objectiveTextAr = "أمّن موكب الزعيم الصغير وتجاوز أطواق الطائرات والدرون والمدرعات نحو النقطة النهائية 🌟",
      rewardCoins = 1000,
      iconEmoji = "👑"
    )
  )
}
