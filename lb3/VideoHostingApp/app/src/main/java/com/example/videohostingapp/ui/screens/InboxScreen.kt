package com.example.videohostingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.videohostingapp.data.Message
import com.example.videohostingapp.data.VideoRepository

@Composable
fun InboxScreen(
  repository: VideoRepository,
  onNavigateToPlayer: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val messages by repository.messagesFlow.collectAsStateWithLifecycle()
  val currentUser by repository.currentUserFlow.collectAsStateWithLifecycle()

  // Filter messages for current user recipient
  val myMessages = remember(messages, currentUser) {
    messages.filter { it.recipient == currentUser.username }
      .sortedByDescending { it.timestamp }
  }

  Column(
    modifier = modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = "Отримані відео",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    if (myMessages.isEmpty()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "У вас немає отриманих повідомлень",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 14.sp
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
      ) {
        items(myMessages) { message ->
          InboxMessageCard(
            message = message,
            onPlayClick = { onNavigateToPlayer(message.videoId) }
          )
        }
      }
    }
  }
}

@Composable
fun InboxMessageCard(
  message: Message,
  onPlayClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Від: ${message.sender}",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = Color(0xFFF43F5E)
        )
        Text(
          text = formatTimestamp(message.timestamp),
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "\"${message.text}\"",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 18.sp
      )
      Spacer(modifier = Modifier.height(12.dp))

      // Shared video attachment box
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onPlayClick() }
      ) {
        Row(
          modifier = Modifier.padding(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .background(Color(0xFFF43F5E), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play Icon", tint = Color.White)
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = message.videoTitle,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              maxLines = 1
            )
            Text(
              text = "Натисніть для перегляду",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
