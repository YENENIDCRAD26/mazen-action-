package com.example

import com.example.model.PlayerLevelSystem
import com.example.model.SanaaHeroProgression
import com.example.sound.ChaseProximityIntensity
import com.example.sound.SanaaAmbientSoundManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testPlayerLevelProgressionInitial() {
    val level1 = PlayerLevelSystem.getLevelInfo(0)
    assertEquals(1, level1.currentLevel)
    assertEquals("🎒", level1.badgeEmoji)
    assertEquals(0f, level1.progressRatio, 0.001f)
    assertFalse(level1.isMaxLevel)
  }

  @Test
  fun testPlayerLevelProgressionMid() {
    val level2 = PlayerLevelSystem.getLevelInfo(500)
    assertEquals(2, level2.currentLevel)
    assertEquals("🏃‍♂️", level2.badgeEmoji)
    assertTrue(level2.progressRatio in 0.0f..1.0f)
  }

  @Test
  fun testPlayerLevelProgressionMax() {
    val maxLevel = PlayerLevelSystem.getLevelInfo(10000)
    assertEquals(10, maxLevel.currentLevel)
    assertTrue(maxLevel.isMaxLevel)
    assertEquals(1.0f, maxLevel.progressRatio, 0.001f)
  }

  @Test
  fun testChaseXpCalculation() {
    val xp = PlayerLevelSystem.calculateChaseXp(
      isVictory = true,
      score = 1500,
      copsEvaded = 3,
      rooftopsCleared = 4,
      thugsCaptured = 2,
      distanceCovered = 1000f
    )
    assertTrue(xp.totalXpEarned > 0)
    assertTrue(xp.baseChaseXp > 0)
    assertEquals(3 * 40, xp.copsEvadedXp)
    assertEquals(4 * 25, xp.parkourXp)
    assertEquals(2 * 35, xp.thugsCapturedXp)
  }

  @Test
  fun testSanaaHeroProgressionTiers() {
    // 0 missions: novice
    val tier0 = SanaaHeroProgression.getTier(0)
    assertEquals(0, tier0.requiredMissions)
    assertEquals(1.0f, tier0.speedMultiplier, 0.001f)
    assertEquals(0, tier0.speedBoostPercent)
    assertFalse(tier0.isSanaaHeroStatus)

    // 1 mission: tier 1 (Bab Al-Yemen Runner)
    val tier1 = SanaaHeroProgression.getTier(1)
    assertEquals(1.10f, tier1.speedMultiplier, 0.001f)
    assertEquals(10, tier1.speedBoostPercent)
    assertFalse(tier1.isSanaaHeroStatus)

    // 3 missions: tier 2 (Rooftop Falcon)
    val tier2 = SanaaHeroProgression.getTier(3)
    assertEquals(1.20f, tier2.speedMultiplier, 0.001f)
    assertEquals(20, tier2.speedBoostPercent)
    assertFalse(tier2.isSanaaHeroStatus)

    // 20 missions: max tier Sana'a Hero status
    val heroTier = SanaaHeroProgression.getTier(20)
    assertTrue(heroTier.isSanaaHeroStatus)
    assertEquals(1.35f, heroTier.speedMultiplier, 0.001f)
    assertEquals(35, heroTier.speedBoostPercent)

    // Dev mode instant unlock
    val devTier = SanaaHeroProgression.getTier(0, isDevMode = true)
    assertTrue(devTier.isSanaaHeroStatus)
    assertEquals(1.35f, devTier.speedMultiplier, 0.001f)
  }

  @Test
  fun testChaseProximityIntensityCalculation() {
    // Distant cop -> Calm
    val calm = SanaaAmbientSoundManager.calculateIntensity(120f, isHiding = false, isPaused = false, wantedStars = 1)
    assertEquals(ChaseProximityIntensity.CALM, calm)

    // Approaching cop (70m)
    val approaching = SanaaAmbientSoundManager.calculateIntensity(70f, isHiding = false, isPaused = false, wantedStars = 2)
    assertEquals(ChaseProximityIntensity.APPROACHING, approaching)

    // Hot pursuit (35m)
    val hotPursuit = SanaaAmbientSoundManager.calculateIntensity(35f, isHiding = false, isPaused = false, wantedStars = 3)
    assertEquals(ChaseProximityIntensity.HOT_PURSUIT, hotPursuit)

    // Critical proximity (<20m)
    val critical = SanaaAmbientSoundManager.calculateIntensity(10f, isHiding = false, isPaused = false, wantedStars = 4)
    assertEquals(ChaseProximityIntensity.CRITICAL_PROXIMITY, critical)

    // Stealth Hiding behind Sana'a wooden doors or pottery
    val stealth = SanaaAmbientSoundManager.calculateIntensity(10f, isHiding = true, isPaused = false, wantedStars = 4)
    assertEquals(ChaseProximityIntensity.STEALTH_HIDING, stealth)
  }
}
