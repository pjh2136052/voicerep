package com.voicerep.app.data.repository

import com.voicerep.app.data.local.dao.SetDao
import com.voicerep.app.data.local.dao.WorkoutDao
import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.local.entity.WorkoutEntity
import com.voicerep.app.data.local.entity.WorkoutWithSets
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao
) {
    // Workout 관련
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insertWorkout(workout: WorkoutEntity): Long = workoutDao.insertWorkout(workout)
    suspend fun updateWorkout(workout: WorkoutEntity) = workoutDao.updateWorkout(workout)
    suspend fun deleteWorkout(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)
    suspend fun getWorkoutById(id: Long): WorkoutEntity? = workoutDao.getWorkoutById(id)

    fun getWorkoutWithSets(workoutId: Long): Flow<WorkoutWithSets?> = workoutDao.getWorkoutWithSets(workoutId)
    val allWorkoutsWithSets: Flow<List<WorkoutWithSets>> = workoutDao.getAllWorkoutsWithSets()

    // Set 관련
    fun getSetsForWorkout(workoutId: Long): Flow<List<SetEntity>> = setDao.getSetsForWorkout(workoutId)
    suspend fun getSetsForWorkoutSync(workoutId: Long): List<SetEntity> = setDao.getSetsForWorkoutSync(workoutId)
    suspend fun insertSet(set: SetEntity): Long = setDao.insertSet(set)
    suspend fun updateSet(set: SetEntity) = setDao.updateSet(set)
    suspend fun deleteSet(set: SetEntity) = setDao.deleteSet(set)

    val allCompletedSets: Flow<List<SetEntity>> = setDao.getAllCompletedSets()
    val totalVolume: Flow<Double> = setDao.getTotalVolume()

    fun getCompletedSetsBetween(startMillis: Long, endMillis: Long): Flow<List<SetEntity>> =
        setDao.getCompletedSetsBetween(startMillis, endMillis)
}
