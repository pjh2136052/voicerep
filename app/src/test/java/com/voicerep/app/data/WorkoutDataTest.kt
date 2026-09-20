package com.voicerep.app.data

import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.local.entity.WorkoutEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutDataTest {

    @Test
    fun test_set_entity_volume_calculation() {
        val set = SetEntity(
            id = 1L,
            workoutId = 100L,
            setNumber = 1,
            weight = 80.0f,
            targetReps = 10,
            completedReps = 8,
            isCompleted = true,
            completedAt = System.currentTimeMillis()
        )

        val volume = set.weight * set.completedReps
        assertEquals(640.0f, volume, 0.001f)
        assertTrue(set.isCompleted)
    }

    @Test
    fun test_workout_entity_creation() {
        val workout = WorkoutEntity(
            id = 1L,
            title = "스쿼트",
            category = "LEGS",
            defaultRestSeconds = 120
        )

        assertEquals("스쿼트", workout.title)
        assertEquals("LEGS", workout.category)
        assertEquals(120, workout.defaultRestSeconds)
    }
}
