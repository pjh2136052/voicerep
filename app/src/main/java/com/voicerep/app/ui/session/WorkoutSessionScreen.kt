package com.voicerep.app.ui.session

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voicerep.app.audio.AudioEngineState

@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutSessionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 종목 선택 가로 리스트
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.availableWorkouts) { workout ->
                FilterChip(
                    selected = uiState.currentWorkout?.id == workout.id,
                    onClick = { viewModel.selectWorkout(workout) },
                    label = { Text(workout.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 상단 음성 엔진 상태 & 데시벨 인디케이터
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val stateText = when (val state = uiState.engineState) {
                    is AudioEngineState.Idle -> "대기 중 (IDLE)"
                    is AudioEngineState.Calibrating -> "소음 측정 중 (${state.progressPercent}%)"
                    is AudioEngineState.Listening -> "음성 감지 중 (기준: ${state.thresholdDb.toInt()}dB)"
                    is AudioEngineState.CandidateDetected -> "발성 감지!"
                    is AudioEngineState.Cooldown -> "쿨다운 중..."
                    is AudioEngineState.Paused -> "일시정지"
                }

                Text(
                    text = stateText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.isSessionActive) MaterialTheme.colorScheme.secondary else Color.Gray
                )

                Text(
                    text = "${uiState.currentDb.toInt()} dB",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 휴식 중일 때 RestTimer, 아닐 때 메인 카운터 표시
        AnimatedVisibility(visible = uiState.isResting) {
            RestTimerComponent(
                remainingSeconds = uiState.restTimeRemainingSeconds,
                totalSeconds = uiState.restTotalSeconds,
                onSkip = { viewModel.skipRest() }
            )
        }

        AnimatedVisibility(visible = !uiState.isResting) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 세트 번호 및 중량 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("세트", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = "${uiState.currentSetNumber} SET",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("중량 (kg)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.adjustWeight(-2.5f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Weight")
                            }
                            Text(
                                text = "${uiState.weight} kg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.adjustWeight(2.5f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Weight")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 초대형 렙 카운터 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.completedReps}",
                            fontSize = 96.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "/ 목표 ${uiState.targetReps} 회",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 수동 횟수 보정 버튼 (+1 / -1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { viewModel.manualAdjustCount(-1) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-1")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("-1 보정")
                    }

                    OutlinedButton(
                        onClick = { viewModel.manualAdjustCount(1) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+1")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1 보정")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 세트 시작 / 세트 완료 메인 액션 버튼
                if (!uiState.isSessionActive) {
                    Button(
                        onClick = { viewModel.startSet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Set")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("세트 시작 (마이크 ON)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeSet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Complete Set")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("세트 완료 및 기록 저장", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}
