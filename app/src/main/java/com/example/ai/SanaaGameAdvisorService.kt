package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Situation data class representing the player's current predicament in Old Sana'a.
 */
data class PlayerEscapeSituation(
  val location: String = "أزقة باب اليمن العتيقة",
  val wantedStars: Int = 2,
  val policeForces: String = "دورية راجلة بقيادة المفتش ناصر + سيارة لاندكروزر عند المدخل",
  val currentVehicleOrTool: String = "دباب ياباني سريع + قشور موز ومفرقعات طماق",
  val missionContext: String = "محاولة الهروب بعد تأمين غنيمة سوق الملح",
  val additionalNotes: String = ""
)

/**
 * Tactical Advice response model.
 */
data class TacticalAdvice(
  val title: String,
  val escapeRoute: String,
  val toolToUse: String,
  val dangerWarning: String,
  val fullAdviceText: String,
  val isLiveAiGenerated: Boolean = true
)

/**
 * "مرشد اللعبة التكتيكي" - Tactical Game Guide powered by Firebase AI (Gemini).
 * Provides actionable tactical tips for escaping police in the narrow labyrinth of Old Sana'a.
 */
class SanaaGameAdvisorService {

  companion object {
    private const val TAG = "SanaaGameAdvisor"
    private const val MODEL_NAME = "gemini-2.5-flash"
  }

  /**
   * Request tactical advice from Firebase AI based on the current in-game situation.
   */
  suspend fun getEscapeTactics(situation: PlayerEscapeSituation): TacticalAdvice = withContext(Dispatchers.IO) {
    try {
      val prompt = buildAdvisorPrompt(situation)
      
      // Call Firebase AI GenerativeModel
      val generativeModel = Firebase.ai.generativeModel(
        modelName = MODEL_NAME
      )

      val response = generativeModel.generateContent(prompt)
      val responseText = response.text?.trim()

      if (!responseText.isNullOrBlank()) {
        parseAiAdvice(responseText, situation, isAi = true)
      } else {
        generateTacticalFallback(situation)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firebase AI call failed or offline, generating procedural tactics: ${e.localizedMessage}")
      generateTacticalFallback(situation)
    }
  }

  private fun buildAdvisorPrompt(situation: PlayerEscapeSituation): String {
    return """
      أنت "مرشد اللعبة التكتيكي" الأسطوري في مدينة صنعاء القديمة (خبير الأزقة وحارة القاسمي وسوق الملح في الثمانينات والتسعينات).
      اللاعب حالياً يواجه مطاردة شرسة من شرطة العاصمة ويحتاج خطة هروب فورية ومحكمة ومفصلة.
      
      الموقف الحالي للاعب:
      - الموقع: ${situation.location}
      - مستوى المطاردة: ${situation.wantedStars} من 5 نجوم (Wanted Level)
      - قوات الشرطة المحاصرة: ${situation.policeForces}
      - وسيلة النقل والأدوات المتوفرة: ${situation.currentVehicleOrTool}
      - سياق المهمة: ${situation.missionContext}
      ${if (situation.additionalNotes.isNotBlank()) "- ملاحظات إضافية: ${situation.additionalNotes}" else ""}

      المطلوب منك كمرشد تكتيكي خبير:
      قدم للاعب خطة هروب تكتيكية محكمة وموجزة ومثيرة، مقسمة إلى:
      1. الخطة والمسار التكتيكي (استغل أزقة صنعاء الضيقة، السائلة، الأسطح، أو عربات الأسواق)
      2. الأداة أو الحيلة المناسبة لاستخدامها الآن
      3. تحذير من فخ أو نقطة خطر يجب تجنبها
      
      استخدم لغة حماسية ومشوقة مع لمسة صنعانية أصيلة (يا بطل، أزقة الياجور، السائلة، القمريات).
    """.trimIndent()
  }

  private fun parseAiAdvice(aiText: String, situation: PlayerEscapeSituation, isAi: Boolean): TacticalAdvice {
    val lines = aiText.lines().filter { it.isNotBlank() }
    val title = "خطة الهروب التكتيكية: ${situation.location}"
    
    return TacticalAdvice(
      title = title,
      escapeRoute = lines.getOrNull(0) ?: "تسلل عبر المنعطفات المتعرجة خلف سوق العقيق.",
      toolToUse = situation.currentVehicleOrTool,
      dangerWarning = "احذر من كشافات الشرطة ونقاط الغلق عند بوابات السور.",
      fullAdviceText = aiText,
      isLiveAiGenerated = isAi
    )
  }

  /**
   * High-fidelity tactical fallback generator tailored to Old Sana'a alleys.
   */
  fun generateTacticalFallback(situation: PlayerEscapeSituation): TacticalAdvice {
    val adviceText = when {
      situation.wantedStars >= 4 -> {
        """
        🚨 **تنبيه عالي الخطورة! النجوم 4+ (استنفار أمني شامل):**
        - **خطة الهروب:** سيارات الشرطة لا تستطيع دخول أزقة باب اليمن التي يقل عرضها عن متر ونصف! اترك الشارع الإسفلتي فوراً، وانعطف يميناً خلف سمسرة النحاس باتجاه زقاق القاسمي الضيق.
        - **الحيلة التكتيكية:** القِ مفرقعات الطماق عند منعطف الزقاق لإحداث سحابة دخانية وصوت انفجار يضلل كشافات الدورية، ثم تسلق السلالم الطينية باتجاه الأسطح العالية.
        - **نقطة الأمان:** مخبأ بستان السلطان السري خلف أشجار الرمان؛ المروحيات وكشافات المفتش ناصر لن ترصدك هناك حتى يهدأ البلاغ الأمني.
        """.trimIndent()
      }
      situation.location.contains("سائلة") || situation.location.contains("السايلة") -> {
        """
        🌊 **تكتيك السائلة التاريخية:**
        - **خطة الهروب:** مجرى السائلة مفتوح وسريع لكنه مكشوف! قُد الدباب بأقصى سرعة، وقبل وصول دورية اللاندكروزر بـ 50 متراً، انعطف فجأة في الممر المائل المؤدي إلى سوق البهارات.
        - **الحيلة التكتيكية:** ارمِ قشور الموز خلف إطارات دبابك لعرقلة أول دراجة نارية تطاردك وتفريغ توازنها عند صخور السائلة الرطبة.
        - **تحذير تكتيكي:** لا تصعد نحو جسر شعوب الرئيسي؛ هناك كمين أمني بمركبات مصفحة ينتظر القادمين من السائلة!
        """.trimIndent()
      }
      situation.location.contains("الملح") || situation.location.contains("سوق") -> {
        """
        🧂 **تكتيك سوق الملح وأزقة الحرفيين:**
        - **خطة الهروب:** ازدحام دكاكين الفضة والبهارات هو أفضل صديق لك. تسلل راجلاً بين أكياس الحلبة والكمون؛ الدخان والروائح القوية تعطل رصد كلاب الأثر وتشتت انتباه الجنود.
        - **الحيلة التكتيكية:** أسقط صندوق خضار أو عربة بطاطس فارغة عند المنعطف الضيق لإغلاق الطريق تماماً أمام دراجات الشرطة.
        - **المسار السري:** ادخل من باب دكان العم قاسم المؤدي عبر النفق الأرضي إلى ساحة الجامع الكبير، ومنها تخرج بزي تنكري.
        """.trimIndent()
      }
      else -> {
        """
        🏰 **تكتيك أزقة باب اليمن وحي القمريات:**
        - **خطة الهروب:** لا تركض في خط مستقيم أبداً! شوارع صنعاء القديمة مبنية على شكل متاهة دائرية. اتجه نحو "حارة الأبهر"، وانعطف يساراً عند القوس الحجري القديم.
        - **الحيلة التكتيكية:** استخدم المقلاع لإصابة مصابيح الإنارة عند التقاطع لتعمية الدورية الراجلة في الظلام الدامس، ثم اقفز فوق برميل الماء نحو الشرفة الخشبية.
        - **تحذير أمني:** بوابات السور الرئيسية (باب اليمن، باب شعوب، باب السباح) مغلقة بحواجز حديدية؛ مخرجك الوحيد هو فتحة السور الغربية خلف بستان خضير.
        """.trimIndent()
      }
    }

    return TacticalAdvice(
      title = "نصيحة مرشد اللعبة التكتيكي: ${situation.location}",
      escapeRoute = "الانسحاب الفوري نحو الأزقة المتعرجة خلف ${situation.location}",
      toolToUse = situation.currentVehicleOrTool,
      dangerWarning = "تجنب الشوارع الإسفلتية المفتوحة وبوابات السور الرئيسية",
      fullAdviceText = adviceText,
      isLiveAiGenerated = false
    )
  }
}
