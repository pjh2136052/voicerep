package com.voicerep.app.ui.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voicerep.app.data.local.entity.WorkoutEntity
import com.voicerep.app.data.repository.WorkoutRepository
import kotlinx.coroutines.launch

@Composable
fun WorkoutManagementScreen(
    repository: WorkoutRepository
) {
    val workouts by repository.allWorkouts.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "운동 종목 관리 (Workouts)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(workouts) { workout ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = workout.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "부위: ${workout.category} • 권장 휴식: ${workout.defaultRestSeconds}초",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.deleteWorkout(workout)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, restSec ->
                coroutineScope.launch {
                    repository.insertWorkout(
                        WorkoutEntity(
                            title = title,
                            category = category,
                            defaultRestSeconds = restSec
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, restSeconds: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("CHEST") }
    var restSecondsText by remember { mutableStateOf("90") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 운동 종목 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("종목명 (예: 인클라인 덤벨프레스)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.uppercase() },
                    label = { Text("부위 (CHEST, BACK, LEGS, ARMS 등)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = restSecondsText,
                    onValueChange = { restSecondsText = it },
                    label = { Text("휴식 시간 (초)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val restSec = restSecondsText.toIntOrNull() ?: 60
                        onConfirm(title, category, restSec)
                    }
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
