package com.example.videohostingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.videohostingapp.data.Video
import com.example.videohostingapp.data.VideoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  repository: VideoRepository,
  onNavigateToPlayer: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val videos by repository.videosFlow.collectAsStateWithLifecycle()
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Всі") }

  // Extract unique categories from videos list
  val categories = remember(videos) {
    listOf("Всі") + videos.map { it.category }.distinct()
  }

  // Filter video lists based on query and category
  val filteredVideos = remember(videos, searchQuery, selectedCategory) {
    videos.filter { video ->
      val matchesSearch = video.title.contains(searchQuery, ignoreCase = true) ||
          video.description.contains(searchQuery, ignoreCase = true) ||
          video.creator.contains(searchQuery, ignoreCase = true)
      val matchesCategory = selectedCategory == "Всі" || video.category == selectedCategory
      matchesSearch && matchesCategory
    }
  }

  Column(
    modifier = modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Spacer(modifier = Modifier.height(4.dp))

    // Search input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Пошук відео, авторів...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFF43F5E),
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
      )
    )

    // Category filter chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(categories) { category ->
        FilterChip(
          selected = selectedCategory == category,
          onClick = { selectedCategory = category },
          label = { Text(category) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFF43F5E).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFFF43F5E)
          ),
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    // Video grid list
    if (filteredVideos.isEmpty()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Відео не знайдено",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 14.sp
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
      ) {
        items(filteredVideos) { video ->
          VideoCard(
            video = video,
            onClick = { onNavigateToPlayer(video.id) }
          )
        }
      }
    }
  }
}

@Composable
fun VideoCard(
  video: Video,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column {
      // Mock video thumbnail with category/duration overlay
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF0F172A)
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        // Play Overlay Icon
        Box(
          modifier = Modifier
            .size(56.dp)
            .background(Color(0xFFF43F5E), RoundedCornerShape(50.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
          )
        }

        // Duration Label
        Text(
          text = video.duration,
          fontSize = 11.sp,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // Category Tag
        Text(
          text = video.category,
          fontSize = 11.sp,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
            .background(Color(0xFFF43F5E), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }

      // Metadata section
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Channel Initials Icon placeholder
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF9333EA)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = video.creator.take(2).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = video.title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = video.creator,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "•",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${formatViews(video.views)} переглядів",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

fun formatViews(views: Int): String {
  return when {
    views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000.0)
    views >= 1_000 -> String.format("%.1fK", views / 1_000.0)
    else -> views.toString()
  }
}
