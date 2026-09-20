package com.voicerep.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voicerep.app.data.local.entity.SetEntity
import com.voicerep.app.data.repository.WorkoutRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    repository: WorkoutRepository
) {
    val completedSets by repository.allCompletedSets.collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "운동 기록 (History)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (completedSets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "아직 완료된 운동 세트 기록이 없습니다.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(completedSets) { set ->
                    SetHistoryCard(set = set, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
fun SetHistoryCard(
    set: SetEntity,
    dateFormat: SimpleDateFormat
) {
    val dateText = set.completedAt?.let { dateFormat.format(Date(it)) } ?: "방금 전"
    val volume = (set.weight * set.completedReps).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                    text = "${set.setNumber}세트 • ${set.completedReps}회 완료",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "중량: ${set.weight} kg (목표: ${set.targetReps}회)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${volume} kg",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "총 볼륨",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
