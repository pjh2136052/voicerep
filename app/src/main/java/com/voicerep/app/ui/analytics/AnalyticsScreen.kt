package com.voicerep.app.ui.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voicerep.app.data.repository.WorkoutRepository

@Composable
fun AnalyticsScreen(
    repository: WorkoutRepository
) {
    val totalVolume by repository.totalVolume.collectAsState(initial = 0.0)
    val completedSets by repository.allCompletedSets.collectAsState(initial = emptyList())

    val totalReps = completedSets.sumOf { it.completedReps }
    val totalSets = completedSets.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "운동 통계 (Analytics)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 총 볼륨 하이라이트 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "누적 운동 볼륨",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${totalVolume.toInt()} kg",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("총 세트 수", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("$totalSets 세트", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Column {
                        Text("총 반복 횟수", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("$totalReps 회", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Column {
                        Text("평균 횟수/세트", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        val avg = if (totalSets > 0) totalReps / totalSets else 0
                        Text("$avg 회", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 볼륨 바 차트 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "최근 7세트 볼륨 추이",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val recentVolumes = completedSets.take(7).reversed().map { (it.weight * it.completedReps) }
                val maxVolume = (recentVolumes.maxOrNull() ?: 100.0).coerceAtLeast(100.0)

                val barColor = MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val barWidth = 32.dp.toPx()
                        val spacing = if (recentVolumes.size > 1) {
                            (canvasWidth - (barWidth * recentVolumes.size)) / (recentVolumes.size + 1)
                        } else 20.dp.toPx()

                        recentVolumes.forEachIndexed { index, vol ->
                            val barHeight = ((vol / maxVolume) * (canvasHeight * 0.85f)).toFloat()
                            val x = spacing + index * (barWidth + spacing)
                            val y = canvasHeight - barHeight

                            drawRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }
                }
            }
        }
    }
}
