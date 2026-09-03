package com.example.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.sound.GameSoundEffects
import com.example.sound.HapticManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Milestone types for player achievements in Sana'a Chase.
 */
enum class MilestoneType(val defaultEmoji: String, val tagAr: String) {
  NEW_PERSONAL_BEST("🏆", "رقم قياسي شخصي جديد!"),
  LEVEL_UP("⭐", "ترقية المستوى!"),
  FASTEST_ESCAPE("⚡", "أسرع وقت هروب!"),
  STAGE_COMPLETED("🏰", "إنجاز المرحلة!"),
  POLICE_EVADED("🚨", "مراوغة الشرطة!"),
  COIN_HOARDER("💰", "جامع الغنائم!"),
  SANAA_HERO_UNLOCKED("👑", "فتح لقب بطل صنعاء!"),
  SPEED_BOOST_UNLOCKED("⚡", "ترقية سرعة الحركة!")
}

/**
 * Data model for an active Milestone Toast event.
 */
data class MilestoneToastEvent(
  val id: Long = System.currentTimeMillis(),
  val type: MilestoneType,
  val titleAr: String,
  val messageAr: String,
  val emoji: String = type.defaultEmoji,
  val timestamp: Long = System.currentTimeMillis()
)

/**
 * Centralized Toast & In-App Milestone Notification Manager.
 * Dispatches native Android Toasts on the UI thread as well as reactive Flow
 * events for in-game celebratory Toast overlays.
 */
object MilestoneToastManager {
  private val _milestoneEvents = MutableSharedFlow<MilestoneToastEvent>(extraBufferCapacity = 10)
  val milestoneEvents: SharedFlow<MilestoneToastEvent> = _milestoneEvents.asSharedFlow()

  private val mainHandler = Handler(Looper.getMainLooper())

  /**
   * Triggers a milestone toast alert with both Android Native Toast and animated in-game HUD banner.
   */
  fun notifyMilestone(
    context: Context,
    type: MilestoneType,
    titleAr: String,
    messageAr: String,
    emoji: String = type.defaultEmoji,
    showNativeToast: Boolean = true
  ) {
    val event = MilestoneToastEvent(
      type = type,
      titleAr = titleAr,
      messageAr = messageAr,
      emoji = emoji
    )

    // 1. Emit to in-game reactive HUD Toast flow
    _milestoneEvents.tryEmit(event)

    // 2. Play celebratory haptics and sounds
    when (type) {
      MilestoneType.NEW_PERSONAL_BEST -> {
        HapticManager.vibrateSuccess()
        GameSoundEffects.playLevelUp()
      }
      MilestoneType.LEVEL_UP -> {
        HapticManager.vibrateSuccess()
        GameSoundEffects.playLevelUp()
      }
      MilestoneType.FASTEST_ESCAPE -> {
        HapticManager.vibrateSuccess()
        GameSoundEffects.playCoin()
      }
      MilestoneType.SANAA_HERO_UNLOCKED -> {
        HapticManager.vibrateSuccess()
        GameSoundEffects.playVictoryFanfare()
      }
      MilestoneType.SPEED_BOOST_UNLOCKED -> {
        HapticManager.vibrateSuccess()
        GameSoundEffects.playNitroBoost()
      }
      else -> {
        HapticManager.vibrateMovement()
      }
    }

    // 3. Show native Android Toast safely on Main Thread
    if (showNativeToast) {
      mainHandler.post {
        try {
          val toastText = "$emoji $titleAr\n$messageAr"
          Toast.makeText(context.applicationContext, toastText, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
          // Fallback gracefully
        }
      }
    }
  }

  /**
   * Helper for "New Personal Best" milestone.
   */
  fun notifyNewPersonalBest(context: Context, newScore: Int, modeName: String = "مطاردة صنعاء") {
    notifyMilestone(
      context = context,
      type = MilestoneType.NEW_PERSONAL_BEST,
      titleAr = "🏆 رقم قياسي شخصي جديد! (New Personal Best)",
      messageAr = "سجلت $newScore نقطة في $modeName! تم تخليد اسمك في قائمة العشرة الأوائل 🥇",
      emoji = "🏆"
    )
  }

  /**
   * Helper for "Level Up" milestone.
   */
  fun notifyLevelUp(context: Context, newLevel: Int, titleAr: String = "بطل صنعاء") {
    notifyMilestone(
      context = context,
      type = MilestoneType.LEVEL_UP,
      titleAr = "⭐ ترقية المستوى: مستوى $newLevel! (Level Up)",
      messageAr = "مبروك! حصلت على لقب [$titleAr] وزادت قدراتك التكتيكية في الأزقة 💥",
      emoji = "⭐"
    )
  }

  /**
   * Helper for "Fastest Escape Time" milestone.
   */
  fun notifyFastestEscape(context: Context, timeFormatted: String, stageName: String) {
    notifyMilestone(
      context = context,
      type = MilestoneType.FASTEST_ESCAPE,
      titleAr = "⚡ أسرع وقت هروب قياسي! (Fastest Escape)",
      messageAr = "أنهيت مطاردة $stageName في زمن قياسي قدره $timeFormatted! ⏱️",
      emoji = "⚡"
    )
  }

  /**
   * Helper for unlocking 'Sana'a Hero' legendary status.
   */
  fun notifySanaaHeroUnlocked(context: Context, speedBoostPercent: Int = 35) {
    notifyMilestone(
      context = context,
      type = MilestoneType.SANAA_HERO_UNLOCKED,
      titleAr = "👑 فتح لقب: بطل صنعاء الأسطوري! (Sana'a Hero)",
      messageAr = "مبروك يا بطل! أصبحت رسمياً بطل صنعاء، تم تفعيل السرعة القصوى +$speedBoostPercent% والهالة الملكية!",
      emoji = "👑"
    )
  }

  /**
   * Helper for unlocking a movement speed boost tier.
   */
  fun notifySpeedBoostUnlocked(context: Context, statusTitleAr: String, speedBoostPercent: Int) {
    notifyMilestone(
      context = context,
      type = MilestoneType.SPEED_BOOST_UNLOCKED,
      titleAr = "⚡ سرعة حركة جديدة: +$speedBoostPercent%! ($statusTitleAr)",
      messageAr = "تمت ترقية رتبتك إلى [$statusTitleAr] واكتسبت سرعة ركض ومراوغة أعلى!",
      emoji = "⚡"
    )
  }
}
