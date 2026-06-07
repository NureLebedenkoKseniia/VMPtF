package com.example.pz3app.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level3Screen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var fileUri by remember { mutableStateOf<Uri?>(null) }
  var fileName by remember { mutableStateOf<String?>(null) }
  var fileSize by remember { mutableStateOf<Long?>(null) }
  var fileContentPreview by remember { mutableStateOf<String?>(null) }

  // Analysis results
  var letterCount by remember { mutableStateOf(0) }
  var spaceCount by remember { mutableStateOf(0) }
  var symbolCount by remember { mutableStateOf(0) }
  var sentenceCount by remember { mutableStateOf(0) }
  var totalChars by remember { mutableStateOf(0) }
  var isAnalyzed by remember { mutableStateOf(false) }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
    onResult = { uri ->
      if (uri != null) {
        fileUri = uri
        // Query file metadata (name & size)
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
          if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0) fileName = it.getString(nameIndex)
            if (sizeIndex >= 0) fileSize = it.getLong(sizeIndex)
          }
        }

        // Read and analyze content
        try {
          val inputStream = context.contentResolver.openInputStream(uri)
          val reader = BufferedReader(InputStreamReader(inputStream))
          val stringBuilder = java.lang.StringBuilder()
          var line: String? = reader.readLine()
          while (line != null) {
            stringBuilder.append(line).append("\n")
            line = reader.readLine()
          }
          val text = stringBuilder.toString()
          fileContentPreview = if (text.length > 500) text.take(500) + "..." else text

          // Count characters
          var letters = 0
          var spaces = 0
          var symbols = 0
          for (char in text) {
            when {
              char.isLetter() -> letters++
              char.isWhitespace() && char != '\n' && char != '\r' -> spaces++
              // Exclude newlines from symbols count
              char != '\n' && char != '\r' -> symbols++
            }
          }

          // Count sentences (delimited by ., !, ?, or ... followed by space or end)
          // Simple but reliable regex matching sentence endings
          val sentences = text.split(Regex("(?<=[.!?])\\s+|(?<=\\.\\.\\.)\\s+"))
            .filter { it.trim().isNotEmpty() }
            .size

          letterCount = letters
          spaceCount = spaces
          symbolCount = symbols
          sentenceCount = sentences
          totalChars = text.length
          isAnalyzed = true
        } catch (e: Exception) {
          fileName = "Помилка читання файлу: ${e.message}"
          isAnalyzed = false
        }
      }
    }
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Рівень 3: Аналізатор файлу", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
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
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.background,
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
          )
        )
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Аналіз текстових файлів (.txt)",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
      )

      Button(
        onClick = { filePickerLauncher.launch(arrayOf("text/plain")) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Text("Обрати текстовий файл", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }

      // Metadata card
      fileName?.let { name ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "File Info",
              tint = Color(0xFFF43F5E)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              fileSize?.let { size ->
                Text(
                  text = "Розмір: $size байт",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // Preview block
      fileContentPreview?.let { preview ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              "Попередній перегляд вмісту:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = preview,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
          }
        }
      }

      // Analysis Results Card
      if (isAnalyzed) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = "Результати аналізу",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF43F5E)
            )

            StatRow("Всього символів (включаючи пробіли)", "$totalChars", Color(0xFF475569))
            Divider()
            StatRow("Кількість літер", "$letterCount", Color(0xFF0D9488))
            Divider()
            StatRow("Кількість пробілів", "$spaceCount", Color(0xFF9333EA))
            Divider()
            StatRow("Кількість інших знаків", "$symbolCount", Color(0xFFEAB308))
            Divider()
            StatRow("Кількість речень", "$sentenceCount", Color(0xFFF43F5E))
          }
        }
      }
    }
  }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      fontSize = 14.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = value,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}
