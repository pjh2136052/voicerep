package com.voicerep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voicerep.app.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long

    @Update
    suspend fun updateSet(set: SetEntity)

    @Delete
    suspend fun deleteSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY set_number ASC")
    fun getSetsForWorkout(workoutId: Long): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY set_number ASC")
    suspend fun getSetsForWorkoutSync(workoutId: Long): List<SetEntity>

    @Query("SELECT * FROM sets WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getAllCompletedSets(): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE is_completed = 1 AND completed_at BETWEEN :startMillis AND :endMillis ORDER BY completed_at ASC")
    fun getCompletedSetsBetween(startMillis: Long, endMillis: Long): Flow<List<SetEntity>>

    @Query("SELECT COALESCE(SUM(weight * completed_reps), 0.0) FROM sets WHERE is_completed = 1")
    fun getTotalVolume(): Flow<Double>
}
