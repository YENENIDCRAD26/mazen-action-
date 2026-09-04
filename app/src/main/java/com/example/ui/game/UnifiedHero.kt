package com.example.ui.game

import androidx.compose.ui.graphics.Color

/**
 * The 4 Unified Heroes of the Game:
 * 1. Mazen (الزعيم الأخضر / القائد) - Matches GTA SA Green Shirt, Cowboy Hat, Bandana
 * 2. Faris (فارس المتسلق / بطل الباركور) - High Jump, Slingshot, Agile
 * 3. Ammar (عمار سائق الدباب / الهجولة) - Drifting, Vehicle Hijack Master
 * 4. Salem (سالم القناص / خبير التمويه) - Smoke Grenades, Graffiti Spray, Firecrackers
 */
enum class UnifiedHeroId(
  val id: String,
  val heroNameAr: String,
  val heroTitleAr: String,
  val avatarEmoji: String,
  val shirtColor: Color,
  val accentColor: Color,
  val hatType: HatType,
  val hasBandana: Boolean,
  val defaultWeaponNameAr: String,
  val defaultWeaponIcon: String,
  val defaultAmmo: String,
  val baseSpeed: Float,
  val jumpPower: Float,
  val perkDescriptionAr: String
) {
  MAZEN(
    id = "mazen_leader",
    heroNameAr = "مازن غلاب (الزعيم الأخضر)",
    heroTitleAr = "قائد المشاغبين - بطل أزقة صنعاء",
    avatarEmoji = "👑",
    shirtColor = Color(0xFF1B5E20), // Iconic GTA Grove Green
    accentColor = Color(0xFF76FF03),
    hatType = HatType.COWBOY_HAT,
    hasBandana = true,
    defaultWeaponNameAr = "رشاش Tec-9 تكتيكي",
    defaultWeaponIcon = "🔫",
    defaultAmmo = "437-50",
    baseSpeed = 1.0f,
    jumpPower = 1.0f,
    perkDescriptionAr = "كاريزما القيادة وسرعة الركض والمراوغة"
  ),
  FARIS(
    id = "faris_parkour",
    heroNameAr = "فارس المتسلق (صاحب الباركور)",
    heroTitleAr = "قناص الأسطح ونوافذ القمريات",
    avatarEmoji = "🧗‍♂️",
    shirtColor = Color(0xFFD84315), // Deep Orange
    accentColor = Color(0xFFFFAB40),
    hatType = HatType.SPORTS_BAND,
    hasBandana = true,
    defaultWeaponNameAr = "مقلاع الحجارة الدقيق",
    defaultWeaponIcon = "🪨",
    defaultAmmo = "99-10",
    baseSpeed = 1.15f,
    jumpPower = 1.35f,
    perkDescriptionAr = "قفزات بهلوانية فائقة واعتلاء الأسطح"
  ),
  AMMAR(
    id = "ammar_driver",
    heroNameAr = "عمار سائق الدباب (الهجولة)",
    heroTitleAr = "خبير خطف المركبات والتفحيط",
    avatarEmoji = "🚐",
    shirtColor = Color(0xFFF9A825), // Golden Amber / Taxi Yellow
    accentColor = Color(0xFFFFEE58),
    hatType = HatType.BACKWARDS_CAP,
    hasBandana = false,
    defaultWeaponNameAr = "مفتاح الدباب الصنعاني",
    defaultWeaponIcon = "🔧",
    defaultAmmo = "∞",
    baseSpeed = 0.95f,
    jumpPower = 0.9f,
    perkDescriptionAr = "قيادة فورية للسيارات وسرعة نيترو مضاعفة"
  ),
  SALEM(
    id = "salem_sniper",
    heroNameAr = "سالم القناص (خبير التشتيت)",
    heroTitleAr = "مسؤول التمويه ورذاذ الغرافيتي",
    avatarEmoji = "🎒",
    shirtColor = Color(0xFF283593), // Indigo / Night Ops
    accentColor = Color(0xFF40C4FF),
    hatType = HatType.BERET,
    hasBandana = true,
    defaultWeaponNameAr = "رذاذ الغرافيتي ومفرقعات الطماق",
    defaultWeaponIcon = "🧨",
    defaultAmmo = "60-15",
    baseSpeed = 1.05f,
    jumpPower = 1.1f,
    perkDescriptionAr = "ستار دخاني وتشتيت دوريات الشرطة"
  )
}

enum class HatType {
  NONE,
  COWBOY_HAT,
  BACKWARDS_CAP,
  BERET,
  SPORTS_BAND
}

data class WeaponItem(
  val id: String,
  val nameAr: String,
  val iconEmoji: String,
  val ammoText: String,
  val damage: Int,
  val soundEffectKey: String
)

val UNIFIED_WEAPONS = listOf(
  WeaponItem("tec9", "رشاش Tec-9", "🔫", "437-50", 35, "GUN_FIRE"),
  WeaponItem("slingshot", "مقلاع الحجارة", "🪨", "99-10", 25, "SLING_SHOT"),
  WeaponItem("graffiti", "رذاذ الغرافيتي", "🎨", "150-30", 15, "SPRAY"),
  WeaponItem("firecracker", "مفرقعات الطماق", "🧨", "20-5", 50, "EXPLOSION")
)
