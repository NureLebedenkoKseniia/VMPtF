package com.example.pz3app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.pz3app.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            "Практична робота №3",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        )
      )
    },
    modifier = modifier
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.background,
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
          )
        )
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Student Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Student Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Лебеденко Ксенія",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "Група ПЗПІ-23-9 | Варіант 6",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
      }

      Text(
        text = "Оберіть рівень завдання:",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(vertical = 8.dp)
      )

      // Level Cards
      LevelCard(
        title = "Рівень 1",
        subtitle = "Частка чисел (a/b)",
        description = "Введення двох чисел з обчисленням частки та перевіркою ділення на нуль.",
        icon = Icons.Default.Build,
        color = Color(0xFF0D9488), // Teal
        onClick = { onItemClick(Level1) }
      )

      LevelCard(
        title = "Рівень 2",
        subtitle = "Гра «Кістки»",
        description = "Гра для двох гравців: кидання кубиків із визначенням переможця та веденням рахунку.",
        icon = Icons.Default.PlayArrow,
        color = Color(0xFF9333EA), // Purple
        onClick = { onItemClick(Level2) }
      )

      LevelCard(
        title = "Рівень 3",
        subtitle = "Аналізатор файлу",
        description = "Підрахунок кількості літер, пробілів, знаків та речень у вибраному текстовому файлі.",
        icon = Icons.Default.List,
        color = Color(0xFFF43F5E), // Coral/Red
        onClick = { onItemClick(Level3) }
      )

      LevelCard(
        title = "Рівень 4",
        subtitle = "Видалення дублікатів",
        description = "Рекурсивне сканування папки на наявність однакових файлів та їх видалення.",
        icon = Icons.Default.Delete,
        color = Color(0xFFEAB308), // Yellow
        onClick = { onItemClick(Level4) }
      )
    }
  }
}

@Composable
fun LevelCard(
  title: String,
  subtitle: String,
  description: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(
              color.copy(alpha = 0.15f),
              MaterialTheme.colorScheme.surface
            )
          )
        )
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = Color.White,
          modifier = Modifier.size(28.dp)
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
          )
          Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = subtitle,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
