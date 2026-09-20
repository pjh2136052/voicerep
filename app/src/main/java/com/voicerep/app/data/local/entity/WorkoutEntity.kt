package com.voicerep.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "category")
    val category: String = "CUSTOM", // CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE, CUSTOM

    @ColumnInfo(name = "default_rest_seconds")
    val defaultRestSeconds: Int = 60,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
