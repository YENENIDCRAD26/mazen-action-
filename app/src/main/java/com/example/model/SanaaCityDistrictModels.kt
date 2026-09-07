package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Sana'a 10D City Districts, Landmarks, Roundabouts, Bridges, Airport, Schools, Pedestrians and Commercial Shops.
 * Faithfully represents the real geographic, cultural, and architectural reality of Sana'a.
 */

enum class SanaaDistrict(
  val id: String,
  val titleAr: String,
  val iconEmoji: String,
  val landmarkNameAr: String,
  val descriptionAr: String,
  val themeColor: Color,
  val landmarkType: LandmarkVisualType
) {
  BAB_AL_YEMEN(
    id = "bab_al_yemen",
    titleAr = "باب اليمن وأسواق صنعاء القديمة",
    iconEmoji = "🏰",
    landmarkNameAr = "بوابة باب اليمن التاريخية وسوق الملح",
    descriptionAr = "البوابة الحجرية الألفية لأسوار صنعاء القديمة، أزقة مرصوفة، ودكاكين التوابل والفضة والعقيق.",
    themeColor = Color(0xFF8D6E63),
    landmarkType = LandmarkVisualType.BAB_AL_YEMEN_GATE
  ),
  AL_SALEH_MOSQUE(
    id = "al_saleh_mosque",
    titleAr = "جامع الصالح وميدان السبعين",
    iconEmoji = "🕌",
    landmarkNameAr = "جامع الصالح الكبير وميدان السبعين الفسيح",
    descriptionAr = "أكبر صرح معماري إسلامي في اليمن؛ قباب ذهبية ضخمة، 6 مآذن شاهقة، وبوليفارد السبعين الفسيح.",
    themeColor = Color(0xFFD4AF37),
    landmarkType = LandmarkVisualType.AL_SALEH_MOSQUE
  ),
  GRAND_MOSQUE_QASIMI(
    id = "grand_mosque_qasimi",
    titleAr = "الجامع الكبير وحارة القاسمي",
    iconEmoji = "🌙",
    landmarkNameAr = "الجامع الكبير العتيق وبستان السلطان",
    descriptionAr = "أقدم جوامع اليمن، منازل الياجور الشاهقة بنوافذ القمريات، وبساتين الخضار التاريخية.",
    themeColor = Color(0xFF2E7D32),
    landmarkType = LandmarkVisualType.GRAND_MOSQUE
  ),
  AL_ZUBAYRI_KENTUCKY(
    id = "al_zubayri_kentucky",
    titleAr = "شارع الزبيري وجولة كنتاكي",
    iconEmoji = "🏢",
    landmarkNameAr = "تقاطع الزبيري وجولة كنتاكي التجارية",
    descriptionAr = "شريان العاصمة التجاري النابض، بنوك، مطاعم سلتة وفحسة شعبية، وحركة سيارات دؤوبة.",
    themeColor = Color(0xFF0284C7),
    landmarkType = LandmarkVisualType.KENTUCKY_ROUNDABOUT
  ),
  HADDAH_MESBAHI(
    id = "haddah_mesbahi",
    titleAr = "شارع حدة وجولة المصباحي",
    iconEmoji = "🛍️",
    landmarkNameAr = "شارع حدة وجولة المصباحي المركزية",
    descriptionAr = "أرقى شوارع صنعاء الحديثة، مراكز تسوق، واجهات زجاجية مضيئة، وأشجار النخيل.",
    themeColor = Color(0xFF7C3AED),
    landmarkType = LandmarkVisualType.MESBAHI_ROUNDABOUT
  ),
  DEFENCE_MINISTRY_ORDI(
    id = "defence_ministry_ordi",
    titleAr = "المقرات الحكومية ومجمع الدفاع",
    iconEmoji = "🏛️",
    landmarkNameAr = "مجمع العرضي والمقرات الوزارية والسيادية",
    descriptionAr = "المقرات والمباني الحكومية السيادية، بوابات حجرية ضخمة، حراسات أمنية، وأسوار دفاعية.",
    themeColor = Color(0xFF475569),
    landmarkType = LandmarkVisualType.GOVERNMENT_COMPOUND
  ),
  AL_THAWRA_STADIUM(
    id = "al_thawra_stadium",
    titleAr = "ستاد الثورة والمدينة الرياضية",
    iconEmoji = "🏟️",
    landmarkNameAr = "ستاد الثورة الدولي والمجمع الرياضي",
    descriptionAr = "الملعب الرياضي الرئيسي في صنعاء؛ أبراج كشافات عملاقة، مدرجات بيضاوية، وساحات مرور واسعة.",
    themeColor = Color(0xFF059669),
    landmarkType = LandmarkVisualType.AL_THAWRA_STADIUM
  ),
  SANAA_SAILAH(
    id = "sanaa_sailah",
    titleAr = "سائلة صنعاء التراثية",
    iconEmoji = "🌊",
    landmarkNameAr = "سائلة صنعاء المرصوفة بالأحجار التراثية",
    descriptionAr = "مجرى مائي وتاريخي يخترق صنعاء القديمة، مرصوف بالأحجار الكلسية، مناسب للمطاردات والسرعة.",
    themeColor = Color(0xFF0891B2),
    landmarkType = LandmarkVisualType.SANAA_SAILAH
  ),
  ASR_PASS(
    id = "asr_pass",
    titleAr = "جولة عصر ومدخل وادي ظهر",
    iconEmoji = "⛰️",
    landmarkNameAr = "مرتفعات عصر وطريق قصر دار الحجر",
    descriptionAr = "البوابة الغربية لصنعاء، إطلالات على جبال عيبان ومزارع العنب، وقصر دار الحجر الأسطوري.",
    themeColor = Color(0xFFD97706),
    landmarkType = LandmarkVisualType.ASR_GATEWAY
  ),
  SANAA_AIRPORT(
    id = "sanaa_airport",
    titleAr = "طريق ومطار صنعاء الدولي",
    iconEmoji = "✈️",
    landmarkNameAr = "مطار صنعاء الدولي وبرج المراقبة",
    descriptionAr = "شريان السفر الرئيسي، صالة المطار الزجاجية، برج المراقبة الجوية، مدارج الطائرات وأضواء الملاحة.",
    themeColor = Color(0xFF0288D1),
    landmarkType = LandmarkVisualType.SANAA_AIRPORT
  ),
  SANAA_SCHOOLS(
    id = "sanaa_schools",
    titleAr = "شارع المدارس والجامعة",
    iconEmoji = "🏫",
    landmarkNameAr = "مدرسة جمال عبد الناصر ومدرسة الكويت",
    descriptionAr = "مجمع المدارس التاريخية الكبرى، بوابات تعليمية عريضة، ساحات طابور الصباح، وأعلام الجمهورية.",
    themeColor = Color(0xFFE65100),
    landmarkType = LandmarkVisualType.SANAA_SCHOOLS
  ),
  SANAA_BRIDGES(
    id = "sanaa_bridges",
    titleAr = "جسور وتقاطعات صنعاء العلوية",
    iconEmoji = "🌉",
    landmarkNameAr = "جسر عصر وجسر كنتاكي وجسور المشاة",
    descriptionAr = "الجسور المعلقة والأنفاق المرورية في صنعاء؛ عبور سيارات علوي، جسور مشاة ومسارات متعددة المستويات.",
    themeColor = Color(0xFF546E7A),
    landmarkType = LandmarkVisualType.SANAA_BRIDGES
  )
}

enum class LandmarkVisualType {
  BAB_AL_YEMEN_GATE,
  AL_SALEH_MOSQUE,
  GRAND_MOSQUE,
  KENTUCKY_ROUNDABOUT,
  MESBAHI_ROUNDABOUT,
  GOVERNMENT_COMPOUND,
  AL_THAWRA_STADIUM,
  SANAA_SAILAH,
  ASR_GATEWAY,
  SANAA_AIRPORT,
  SANAA_SCHOOLS,
  SANAA_BRIDGES
}

/**
 * Types of buildings displayed continuously on BOTH left and right sides of the streets.
 */
enum class RoadsideBuildingType {
  TOWER_HOUSE,          // منازل الياجور الصنعانية مع القمريات
  GOVERNMENT_HQ,        // مقرات حكومية مع بوابات رسمية وأعلام
  HISTORIC_SCHOOL,      // مدارس حكومية (مدرسة عبد الناصر، مدرسة الكويت)
  BUSTLING_SOUQ,        // أسواق وبازارات مفتوحة مع بضائع ومظلات
  AIRPORT_FACILITY,     // منشآت ومرافق المطار وبرج المراقبة
  OVERPASS_BRIDGE       // جسور علوية وتقاطعات
}

/**
 * Interactive Street Turn / Alleyway Branching Model (الانعطاف والتنقل بين الشوارع والأزقة)
 */
data class SanaaTurnBranch(
  val id: String,
  val triggerZ: Float,
  val titleAr: String,
  val subtitleAr: String,
  val targetDistrict: SanaaDistrict,
  val turnDirection: Int // -1: Left turn ⮌, 1: Right turn ⮎, 0: Roundabout exit 🔄
)

/**
 * Types of Sana'ani pedestrians walking on the sidewalks and crossing streets.
 */
enum class SanaaPedestrianType(
  val titleAr: String,
  val emoji: String,
  val thobeColor: Color,
  val shawlColor: Color,
  val hasJambiya: Boolean,
  val isElder: Boolean = false,
  val hasCart: Boolean = false
) {
  SANAANI_MAN(
    titleAr = "مواطن صنعاني بالثوب والشال",
    emoji = "🧔",
    thobeColor = Color(0xFFFAFAFA),
    shawlColor = Color(0xFF8D6E63),
    hasJambiya = true
  ),
  ELDER_WITH_CANE(
    titleAr = "حاج صنعاني مسن بالعكاز والعمامة",
    emoji = "👴",
    thobeColor = Color(0xFFECEFF1),
    shawlColor = Color(0xFFB71C1C),
    hasJambiya = true,
    isElder = true
  ),
  STREET_POTATO_VENDOR(
    titleAr = "بائع بطاط وبليلة بعربة خشبية",
    emoji = "🥔",
    thobeColor = Color(0xFFCFD8DC),
    shawlColor = Color(0xFFF57F17),
    hasJambiya = false,
    hasCart = true
  ),
  SANAANI_SITARA_WOMAN(
    titleAr = "امرأة صنعانية بالستارة التراثية",
    emoji = "🧕",
    thobeColor = Color(0xFF212121),
    shawlColor = Color(0xFFD50000), // Iconic red & black Sanaani Sitara pattern
    hasJambiya = false
  ),
  SANAANI_YOUTH(
    titleAr = "شاب صنعاني بالمعوز الرياضي",
    emoji = "🧑",
    thobeColor = Color(0xFF37474F),
    shawlColor = Color(0xFF00E676),
    hasJambiya = true
  )
}

data class SanaaPedestrian(
  val id: Long,
  var worldZ: Float,
  var sideOffset: Float, // -1.1f (left sidewalk) to 1.1f (right sidewalk)
  val type: SanaaPedestrianType,
  var walkSpeed: Float = 0.6f,
  var isWalkingForward: Boolean = true,
  var walkCycle: Float = 0f
)

data class SanaaCommercialShop(
  val id: Int,
  val nameAr: String,
  val categoryAr: String,
  val awningColor1: Color,
  val awningColor2: Color,
  val neonColor: Color,
  val side: Int, // -1: Left sidewalk, 1: Right sidewalk
  val worldZ: Float
)
