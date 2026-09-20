package com.voicerep.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workout_id"], name = "idx_sets_workout_id"),
        Index(value = ["completed_at"], name = "idx_sets_completed_at")
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "workout_id")
    val workoutId: Long,

    @ColumnInfo(name = "set_number")
    val setNumber: Int,

    @ColumnInfo(name = "weight")
    val weight: Float = 0.0f,

    @ColumnInfo(name = "target_reps")
    val targetReps: Int = 10,

    @ColumnInfo(name = "completed_reps")
    val completedReps: Int = 0,

    @ColumnInfo(name = "duration_millis")
    val durationMillis: Long = 0L,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)
