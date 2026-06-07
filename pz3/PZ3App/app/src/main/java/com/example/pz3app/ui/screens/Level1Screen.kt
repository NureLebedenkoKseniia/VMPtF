package com.example.pz3app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level1Screen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var numA by remember { mutableStateOf("") }
  var numB by remember { mutableStateOf("") }
  var resultText by remember { mutableStateOf<String?>(null) }
  var errorText by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Рівень 1: Частка чисел", fontWeight = FontWeight.Bold) },
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
        .padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Обчислення частки a / b",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      OutlinedTextField(
        value = numA,
        onValueChange = {
          numA = it
          errorText = null
        },
        label = { Text("Введіть число a") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = numB,
        onValueChange = {
          numB = it
          errorText = null
        },
        label = { Text("Введіть число b") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          val valA = numA.toDoubleOrNull()
          val valB = numB.toDoubleOrNull()

          if (valA == null || valB == null) {
            errorText = "Будь ласка, введіть коректні числа"
            resultText = null
          } else if (valB == 0.0) {
            errorText = "Помилка: Ділення на нуль неможливе!"
            resultText = null
          } else {
            val res = valA / valB
            // Format result to prevent huge numbers
            resultText = String.format("Результат: %.4f", res)
            errorText = null
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF0D9488) // Teal matching Home design
        )
      ) {
        Text("Поділити", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Result or Error Box
      when {
        errorText != null -> {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer
            )
          ) {
            Text(
              text = errorText!!,
              color = MaterialTheme.colorScheme.onErrorContainer,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(16.dp),
              fontSize = 15.sp
            )
          }
        }
        resultText != null -> {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = Color(0xFF0D9488).copy(alpha = 0.15f)
            )
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Операція успішна",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D9488)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = resultText!!,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
              )
            }
          }
        }
      }
    }
  }
}
