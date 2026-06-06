package com.example.videohostingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.videohostingapp.data.UserRole
import com.example.videohostingapp.data.VideoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  repository: VideoRepository,
  modifier: Modifier = Modifier
) {
  val currentUser by repository.currentUserFlow.collectAsStateWithLifecycle()
  val subscriptions by repository.subscriptionsFlow.collectAsStateWithLifecycle()

  var nameText by remember { mutableStateOf(currentUser.username) }

  Column(
    modifier = modifier
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Профіль користувача",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    // User Profile Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "User Avatar",
          tint = Color(0xFFF43F5E),
          modifier = Modifier.size(64.dp)
        )

        OutlinedTextField(
          value = nameText,
          onValueChange = {
            nameText = it
            repository.updateCurrentUser(it, currentUser.role)
          },
          label = { Text("Ім'я користувача") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // Role Manager Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Рівень доступу (Роль):",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Оберіть роль для демонстрації обмеження доступу:",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          UserRole.values().forEach { roleVal ->
            val isSelected = currentUser.role == roleVal.name
            ElevatedButton(
              onClick = { repository.updateCurrentUser(currentUser.username, roleVal.name) },
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (isSelected) Color(0xFFF43F5E) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
            ) {
              Text(
                text = when (roleVal) {
                  UserRole.VIEWER -> "Глядач"
                  UserRole.CREATOR -> "Автор"
                  UserRole.ADMIN -> "Адмін"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Subscriptions Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Ваші підписки (${subscriptions.size}):",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.primary
        )

        if (subscriptions.isEmpty()) {
          Text(
            text = "Ви ще не підписалися на жодного автора",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          subscriptions.forEach { channel ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🔔 $channel",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              TextButton(
                onClick = { repository.toggleSubscription(channel) }
              ) {
                Text("Скасувати", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
              }
            }
          }
        }
      }
    }
  }
}
