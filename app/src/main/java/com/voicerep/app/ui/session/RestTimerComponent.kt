package com.voicerep.app.ui.session

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RestTimerComponent(
    remainingSeconds: Int,
    totalSeconds: Int,
    onSkip: () -> Unit
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "restProgress")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "휴식 중 (Resting)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(160.dp),
                    color = Color.DarkGray.copy(alpha = 0.5f),
                    strokeWidth = 10.dp
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(160.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 10.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$remainingSeconds",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "초 남음",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSkip,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("휴식 건너뛰기")
            }
        }
    }
}
