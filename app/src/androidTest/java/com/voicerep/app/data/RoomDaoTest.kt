package com.voicerep.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.voicerep.app.data.local.VoiceRepDatabase
import com.voicerep.app.data.local.dao.SetDao
import com.voicerep.app.data.local.dao.WorkoutDao
import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var db: VoiceRepDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var setDao: SetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, VoiceRepDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workoutDao = db.workoutDao()
        setDao = db.setDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveWorkout() = runBlocking {
        val workout = WorkoutEntity(
            title = "벤치프레스",
            category = "CHEST",
            defaultRestSeconds = 90
        )
        val id = workoutDao.insertWorkout(workout)

        val retrieved = workoutDao.getWorkoutById(id)
        assertNotNull(retrieved)
        assertEquals("벤치프레스", retrieved?.title)
    }

    @Test
    fun testCascadeDelete() = runBlocking {
        val workoutId = workoutDao.insertWorkout(
            WorkoutEntity(title = "스쿼트", category = "LEGS")
        )

        setDao.insertSet(
            SetEntity(
                workoutId = workoutId,
                setNumber = 1,
                weight = 100f,
                targetReps = 5,
                completedReps = 5,
                isCompleted = true
            )
        )

        val setsBefore = setDao.getSetsForWorkoutSync(workoutId)
        assertEquals(1, setsBefore.size)

        // Workout 삭제 시 외래키 CASCADE로 연관 Set도 자동 삭제되는지 검증
        val workout = workoutDao.getWorkoutById(workoutId)!!
        workoutDao.deleteWorkout(workout)

        val setsAfter = setDao.getSetsForWorkoutSync(workoutId)
        assertTrue("Cascade 삭제로 세트 목록이 비어있어야 함", setsAfter.isEmpty())
    }
}
