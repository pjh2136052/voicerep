package com.voicerep.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.voicerep.app.data.preference.UserPreferencesRepository
import com.voicerep.app.data.repository.WorkoutRepository
import com.voicerep.app.ui.analytics.AnalyticsScreen
import com.voicerep.app.ui.history.HistoryScreen
import com.voicerep.app.ui.screens.AudioMeterScreen
import com.voicerep.app.ui.session.WorkoutSessionScreen
import com.voicerep.app.ui.session.WorkoutSessionViewModel
import com.voicerep.app.ui.settings.SettingsScreen
import com.voicerep.app.ui.workouts.WorkoutManagementScreen

enum class ScreenTab(val title: String, val icon: ImageVector) {
    SESSION("운동", Icons.Default.FitnessCenter),
    HISTORY("기록", Icons.Default.History),
    ANALYTICS("통계", Icons.Default.BarChart),
    WORKOUTS("종목", Icons.Default.List),
    SETTINGS("설정", Icons.Default.Settings)
}

@Composable
fun MainAppNavigation(
    sessionViewModel: WorkoutSessionViewModel,
    repository: WorkoutRepository,
    prefsRepository: UserPreferencesRepository
) {
    var selectedTab by remember { mutableStateOf(ScreenTab.SESSION) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ScreenTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                ScreenTab.SESSION -> WorkoutSessionScreen(viewModel = sessionViewModel)
                ScreenTab.HISTORY -> HistoryScreen(repository = repository)
                ScreenTab.ANALYTICS -> AnalyticsScreen(repository = repository)
                ScreenTab.WORKOUTS -> WorkoutManagementScreen(repository = repository)
                ScreenTab.SETTINGS -> SettingsScreen(prefsRepository = prefsRepository)
            }
        }
    }
}
