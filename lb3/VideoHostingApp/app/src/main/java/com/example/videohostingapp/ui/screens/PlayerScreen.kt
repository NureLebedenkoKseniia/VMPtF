@file:kotlin.OptIn(androidx.media3.common.util.UnstableApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.videohostingapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.videohostingapp.data.VideoRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlayerScreen(
  videoId: String,
  repository: VideoRepository,
  onBack: () -> Unit,
  onNavigateToPlayer: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val videos by repository.videosFlow.collectAsStateWithLifecycle()
  val currentUser by repository.currentUserFlow.collectAsStateWithLifecycle()
  val subscriptions by repository.subscriptionsFlow.collectAsStateWithLifecycle()

  val video = remember(videos, videoId) {
    videos.find { it.id == videoId }
  }

  var commentText by remember { mutableStateOf("") }
  var showShareDialog by remember { mutableStateOf(false) }

  if (video == null) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Відео видалено або недоступне.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Назад") }
      }
    }
    return
  }

  // Set up Media3 ExoPlayer instance
  val exoPlayer = remember(videoId) {
    ExoPlayer.Builder(context).build().apply {
      val mediaItem = MediaItem.fromUri(Uri.parse(video.url))
      setMediaItem(mediaItem)
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }

  val isLiked = remember(video, currentUser) {
    video.likedByUsers.contains(currentUser.username)
  }

  val isSubscribed = remember(video, subscriptions) {
    subscriptions.contains(video.creator)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(video.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          val isUploader = currentUser.role == "CREATOR" && video.creator == currentUser.username
          if (currentUser.role == "ADMIN" || isUploader) {
            IconButton(
              onClick = {
                val success = repository.deleteVideo(video.id)
                if (success) {
                  val msg = if (currentUser.role == "ADMIN") "Відео видалено адміністратором" else "Відео видалено автором"
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                  onBack()
                }
              }
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete Video", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      )
    },
    modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // AndroidView wrapping ExoPlayer PlayerView
      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = true
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(210.dp)
          .background(Color.Black)
      )

      // Video details & Actions Row
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = video.title,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${formatViews(video.views)} переглядів",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Like Button
            IconButton(onClick = { repository.toggleLikeVideo(video.id) }) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Like",
                  tint = if (isLiked) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${video.likes}",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Share Button
            IconButton(onClick = { showShareDialog = true }) {
              Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Channel Info & Subscription Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF9333EA)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = video.creator.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            }
            Column {
              Text(
                text = video.creator,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Канал",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Subscribe Button
          Button(
            onClick = { repository.toggleSubscription(video.creator) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF43F5E),
              contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(if (isSubscribed) "Ви підписані" else "Підписатися")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Description
        Text(
          text = video.description,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 18.sp
        )
      }

      // Comments section Header
      Text(
        text = "Коментарі (${video.comments.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )

      // Add comment input
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = commentText,
          onValueChange = { commentText = it },
          placeholder = { Text("Напишіть коментар...") },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        )
        Button(
          onClick = {
            if (commentText.trim().isNotEmpty()) {
              repository.addComment(video.id, commentText.trim())
              commentText = ""
            }
          },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
        ) {
          Text("ОК")
        }
      }

      // Lazy comments list
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(video.comments) { comment ->
          CommentRow(
            comment = comment,
            isAdmin = currentUser.role == "ADMIN",
            onDelete = { repository.deleteComment(video.id, comment.id) }
          )
        }
      }
    }
  }

  // Share Dialog Modal
  if (showShareDialog) {
    ShareDialog(
      onDismiss = { showShareDialog = false },
      onShare = { recipient, msg ->
        repository.shareVideo(video.id, video.title, recipient, msg)
        Toast.makeText(context, "Відео надіслано користувачу $recipient", Toast.LENGTH_SHORT).show()
        showShareDialog = false
      }
    )
  }
}

@Composable
fun CommentRow(
  comment: com.example.videohostingapp.data.Comment,
  isAdmin: Boolean,
  onDelete: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(50.dp))
        .background(Color(0xFF0D9488)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = comment.author.take(1).uppercase(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = comment.author,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
        Text(
          text = formatTimestamp(comment.timestamp),
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = comment.text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    if (isAdmin) {
      IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Видалити коментар",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

fun formatTimestamp(timestamp: Long): String {
  val date = Date(timestamp)
  val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
  return sdf.format(date)
}

// Share Modal Dialog Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDialog(
  onDismiss: () -> Unit,
  onShare: (recipient: String, message: String) -> Unit
) {
  val friendsList = listOf("Ivan", "Maria", "Olga", "Alex", "Dmytro")
  var selectedFriend by remember { mutableStateOf(friendsList.first()) }
  var shareMessage by remember { mutableStateOf("Глянь це відео, крута штука!") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Поділитися відео", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Оберіть одержувача:", fontSize = 14.sp)

        // Dropdown/Selector simulation
        Column {
          friendsList.forEach { friend ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedFriend = friend }
                .padding(vertical = 4.dp)
            ) {
              RadioButton(
                selected = selectedFriend == friend,
                onClick = { selectedFriend = friend }
              )
              Text(text = friend, modifier = Modifier.padding(start = 8.dp))
            }
          }
        }

        OutlinedTextField(
          value = shareMessage,
          onValueChange = { shareMessage = it },
          label = { Text("Супровідне повідомлення") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = { onShare(selectedFriend, shareMessage) }) {
        Text("Надіслати")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Скасувати")
      }
    }
  )
}
