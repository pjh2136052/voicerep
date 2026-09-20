package com.voicerep.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.voicerep.app.data.local.entity.WorkoutEntity
import com.voicerep.app.data.local.entity.WorkoutWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>): List<Long>

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY id ASC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutWithSets(workoutId: Long): Flow<WorkoutWithSets?>

    @Transaction
    @Query("SELECT * FROM workouts")
    fun getAllWorkoutsWithSets(): Flow<List<WorkoutWithSets>>
}
