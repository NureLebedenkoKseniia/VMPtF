package com.example.videohostingapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.videohostingapp.data.VideoRepository
import com.example.videohostingapp.ui.screens.MainAppContainer
import com.example.videohostingapp.ui.screens.PlayerScreen

@Composable
fun MainNavigation(repository: VideoRepository) {
  val backStack = rememberNavBackStack(Dashboard)

  NavDisplay(
    modifier = Modifier.fillMaxSize(),
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Dashboard> {
          MainAppContainer(
            repository = repository,
            onNavigateToPlayer = { videoId -> backStack.add(Player(videoId)) },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<Player> { key ->
          PlayerScreen(
            videoId = key.videoId,
            repository = repository,
            onBack = { backStack.removeLastOrNull() },
            onNavigateToPlayer = { videoId ->
              backStack.removeLastOrNull()
              backStack.add(Player(videoId))
            },
            modifier = Modifier.fillMaxSize()
          )
        }
      },
  )
}
