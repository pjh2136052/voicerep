package com.voicerep.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithSets(
    @Embedded
    val workout: WorkoutEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "workout_id"
    )
    val sets: List<SetEntity> = emptyList()
)
