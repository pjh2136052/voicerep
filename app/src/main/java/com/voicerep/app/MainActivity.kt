package com.voicerep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.voicerep.app.data.local.VoiceRepDatabase
import com.voicerep.app.data.preference.UserPreferencesRepository
import com.voicerep.app.data.repository.WorkoutRepository
import com.voicerep.app.feedback.FeedbackManager
import com.voicerep.app.ui.navigation.MainAppNavigation
import com.voicerep.app.ui.session.WorkoutSessionViewModel
import com.voicerep.app.ui.theme.VoiceRepTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: VoiceRepDatabase
    private lateinit var repository: WorkoutRepository
    private lateinit var prefsRepository: UserPreferencesRepository
    private lateinit var feedbackManager: FeedbackManager
    private lateinit var sessionViewModel: WorkoutSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = VoiceRepDatabase.getInstance(this)
        repository = WorkoutRepository(database.workoutDao(), database.setDao())
        prefsRepository = UserPreferencesRepository(this)
        feedbackManager = FeedbackManager(this)
        sessionViewModel = WorkoutSessionViewModel(repository, feedbackManager)

        setContent {
            VoiceRepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(
                        sessionViewModel = sessionViewModel,
                        repository = repository,
                        prefsRepository = prefsRepository
                    )
                }
            }
        }
    }
}
