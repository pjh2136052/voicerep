package com.voicerep.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.voicerep.app.data.local.dao.SetDao
import com.voicerep.app.data.local.dao.WorkoutDao
import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.local.entity.WorkoutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WorkoutEntity::class, SetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VoiceRepDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao

    companion object {
        @Volatile
        private var INSTANCE: VoiceRepDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): VoiceRepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoiceRepDatabase::class.java,
                    "voicerep_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        seedDefaultWorkouts(database.workoutDao())
                    }
                }
            }

            private suspend fun seedDefaultWorkouts(dao: WorkoutDao) {
                val defaultList = listOf(
                    WorkoutEntity(title = "벤치프레스 (Bench Press)", category = "CHEST", defaultRestSeconds = 90),
                    WorkoutEntity(title = "스쿼트 (Squat)", category = "LEGS", defaultRestSeconds = 120),
                    WorkoutEntity(title = "데드리프트 (Deadlift)", category = "BACK", defaultRestSeconds = 120),
                    WorkoutEntity(title = "오버헤드 프레스 (OHP)", category = "SHOULDERS", defaultRestSeconds = 90),
                    WorkoutEntity(title = "바벨 로우 (Barbell Row)", category = "BACK", defaultRestSeconds = 90),
                    WorkoutEntity(title = "풀업 (Pull Up)", category = "BACK", defaultRestSeconds = 60),
                    WorkoutEntity(title = "덤벨 컬 (Dumbbell Curl)", category = "ARMS", defaultRestSeconds = 60)
                )
                dao.insertWorkouts(defaultList)
            }
        }
    }
}
