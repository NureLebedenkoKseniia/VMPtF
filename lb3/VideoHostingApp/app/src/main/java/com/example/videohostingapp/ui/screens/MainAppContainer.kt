package com.example.videohostingapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.videohostingapp.data.VideoRepository

enum class MainTab {
  DASHBOARD,
  UPLOAD,
  INBOX,
  PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
  repository: VideoRepository,
  onNavigateToPlayer: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

  val videos by repository.videosFlow.collectAsStateWithLifecycle()
  val subscriptions by repository.subscriptionsFlow.collectAsStateWithLifecycle()
  val currentUser by repository.currentUserFlow.collectAsStateWithLifecycle()

  // Track the size of video list to trigger new video notification
  var previousVideoCount by remember { mutableStateOf(videos.size) }

  LaunchedEffect(videos) {
    if (videos.size > previousVideoCount) {
      val newVideo = videos.last()
      if (subscriptions.contains(newVideo.creator)) {
        // Subscribed creator uploaded a video! Trigger in-app notification
        Toast.makeText(
          context,
          "🔔 Нове відео від ${newVideo.creator}: ${newVideo.title}!",
          Toast.LENGTH_LONG
        ).show()
      }
    }
    previousVideoCount = videos.size
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Logo",
              tint = Color(0xFFF43F5E),
              modifier = Modifier.size(32.dp)
            )
            Text(
              text = "NureTube",
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        NavigationBarItem(
          selected = currentTab == MainTab.DASHBOARD,
          onClick = { currentTab = MainTab.DASHBOARD },
          icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
          label = { Text("Відео") }
        )
        NavigationBarItem(
          selected = currentTab == MainTab.UPLOAD,
          onClick = { currentTab = MainTab.UPLOAD },
          icon = { Icon(Icons.Default.AddCircle, contentDescription = "Upload") },
          label = { Text("Завантажити") }
        )
        NavigationBarItem(
          selected = currentTab == MainTab.INBOX,
          onClick = { currentTab = MainTab.INBOX },
          icon = { Icon(Icons.Default.MailOutline, contentDescription = "Inbox") },
          label = { Text("Повідомлення") }
        )
        NavigationBarItem(
          selected = currentTab == MainTab.PROFILE,
          onClick = { currentTab = MainTab.PROFILE },
          icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
          label = { Text("Профіль") }
        )
      }
    },
    modifier = modifier
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.background,
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
          )
        )
    ) {
      when (currentTab) {
        MainTab.DASHBOARD -> DashboardScreen(
          repository = repository,
          onNavigateToPlayer = onNavigateToPlayer,
          modifier = Modifier.fillMaxSize()
        )
        MainTab.UPLOAD -> UploadScreen(
          repository = repository,
          onSuccess = { currentTab = MainTab.DASHBOARD },
          modifier = Modifier.fillMaxSize()
        )
        MainTab.INBOX -> InboxScreen(
          repository = repository,
          onNavigateToPlayer = onNavigateToPlayer,
          modifier = Modifier.fillMaxSize()
        )
        MainTab.PROFILE -> ProfileScreen(
          repository = repository,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}
