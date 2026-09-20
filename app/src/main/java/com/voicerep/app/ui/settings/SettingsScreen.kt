package com.voicerep.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voicerep.app.data.preference.UserPreferencesRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefsRepository: UserPreferencesRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val marginDb by prefsRepository.triggerMarginDb.collectAsState(initial = 14.0)
    val minIntervalMs by prefsRepository.minRepIntervalMs.collectAsState(initial = 1200L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "오디오 감도 설정 (Settings)",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. 트리거 감도 마진 슬라이더
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "음성 감지 임계 마진 (dB)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "주변 소음 기준치(Noise Floor) 대비 몇 dB 이상 커질 때 발성으로 인식할지 설정합니다. (현재: +${marginDb.toInt()} dB)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = marginDb.toFloat(),
                    onValueChange = {
                        coroutineScope.launch { prefsRepository.setTriggerMarginDb(it.toDouble()) }
                    },
                    valueRange = 8f..24f,
                    steps = 15
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 디바운스 최소 반복 간격 슬라이더
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "최소 반복 쿨다운 간격 (ms)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1회 카운트 후 추가 발성이나 거친 숨소리를 무시하는 최소 시간 간격입니다. (현재: ${minIntervalMs} ms)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Slider(
                    value = minIntervalMs.toFloat(),
                    onValueChange = {
                        coroutineScope.launch { prefsRepository.setMinRepIntervalMs(it.toLong()) }
                    },
                    valueRange = 800f..2500f,
                    steps = 16
                )
            }
        }
    }
}
