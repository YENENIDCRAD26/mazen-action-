package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.PlayerEscapeSituation
import com.example.ai.SanaaGameAdvisorService
import com.example.data.local.AppDatabase
import com.example.data.local.GameMissionDao
import com.example.data.local.GameMissionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameMissionRoomTest {

  private lateinit var db: AppDatabase
  private lateinit var missionDao: GameMissionDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    missionDao = db.gameMissionDao()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun insertAndRetrieveMission() = runBlocking {
    val mission = GameMissionEntity(
      title = "اختراق طوق باب اليمن",
      description = "الهروب من دوريات الشرطة عبر أزقة باب اليمن الضيقة",
      difficulty = "صعب",
      isCompleted = false,
      location = "باب اليمن",
      rewardCoins = 500
    )

    val id = missionDao.insertMission(mission)
    assertTrue(id > 0)

    val retrieved = missionDao.getMissionById(id)
    assertNotNull(retrieved)
    assertEquals("اختراق طوق باب اليمن", retrieved?.title)
    assertEquals("صعب", retrieved?.difficulty)
    assertEquals(false, retrieved?.isCompleted)

    // Update status
    missionDao.updateCompletionStatus(id, true)
    val updated = missionDao.getMissionById(id)
    assertEquals(true, updated?.isCompleted)

    val all = missionDao.getAllMissions().first()
    assertEquals(1, all.size)
  }

  @Test
  fun testSanaaGameAdvisorTacticalFallback() {
    val advisorService = SanaaGameAdvisorService()
    val situation = PlayerEscapeSituation(
      location = "سائلة صنعاء القديمة",
      wantedStars = 3,
      policeForces = "دورية لاندكروزر عند مدخل السائلة",
      currentVehicleOrTool = "دباب ياباني سريع"
    )

    val advice = advisorService.generateTacticalFallback(situation)
    assertNotNull(advice)
    assertTrue(advice.fullAdviceText.contains("السائلة"))
    assertTrue(advice.escapeRoute.isNotBlank())
  }
}
