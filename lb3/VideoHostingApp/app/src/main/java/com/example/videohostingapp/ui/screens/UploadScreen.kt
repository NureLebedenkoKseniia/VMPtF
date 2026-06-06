package com.example.videohostingapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.videohostingapp.data.VideoRepository

@Composable
fun UploadScreen(
  repository: VideoRepository,
  onSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentUser by repository.currentUserFlow.collectAsStateWithLifecycle()

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Анімація") }
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var selectedFileName by remember { mutableStateOf<String?>(null) }

  // Restrict access based on user role (Level 3 restriction)
  val isAllowed = remember(currentUser) {
    currentUser.role == "CREATOR" || currentUser.role == "ADMIN"
  }

  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      if (uri != null) {
        selectedFileUri = uri
        selectedFileName = uri.lastPathSegment ?: "video.mp4"
      }
    }
  )

  if (!isAllowed) {
    Box(
      modifier = modifier.padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Access Denied",
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(48.dp)
          )
          Text(
            text = "Доступ обмежено",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer
          )
          Text(
            text = "Ваш поточний рівень доступу: ${currentUser.role}.\nТільки Автори (Creators) та Адміністратори (Admins) можуть завантажувати нові відео.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
          )
        }
      }
    }
    return
  }

  Column(
    modifier = modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Завантажити нове відео",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    OutlinedTextField(
      value = title,
      onValueChange = { title = it },
      label = { Text("Назва відео") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      label = { Text("Опис відео") },
      modifier = Modifier.fillMaxWidth()
    )

    // Category Selector
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text("Категорія:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Анімація", "Фантастика", "Промо").forEach { cat ->
          ElevatedFilterChip(
            selected = category == cat,
            onClick = { category = cat },
            label = { Text(cat) }
          )
        }
      }
    }

    // Pick Video Button
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { videoPickerLauncher.launch("video/*") },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
        ) {
          Text("Обрати відеофайл")
        }

        selectedFileName?.let { name ->
          Text(
            text = "Вибрано: $name",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
          )
        } ?: Text(
          text = "Файл не обрано",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Submit
    Button(
      onClick = {
        if (title.trim().isEmpty() || description.trim().isEmpty()) {
          Toast.makeText(context, "Будь ласка, заповніть назву та опис", Toast.LENGTH_SHORT).show()
        } else if (selectedFileUri == null) {
          Toast.makeText(context, "Будь ласка, оберіть відеофайл", Toast.LENGTH_SHORT).show()
        } else {
          // Save mock video metadata referencing the picked video uri
          val success = repository.uploadVideo(
            title = title.trim(),
            description = description.trim(),
            category = category,
            url = selectedFileUri.toString(),
            duration = "0:30" // Mock duration
          )
          if (success) {
            Toast.makeText(context, "Відео успішно завантажено!", Toast.LENGTH_SHORT).show()
            onSuccess()
          } else {
            Toast.makeText(context, "Помилка завантаження", Toast.LENGTH_SHORT).show()
          }
        }
      },
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
    ) {
      Text("Опублікувати відео", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
  }
}
