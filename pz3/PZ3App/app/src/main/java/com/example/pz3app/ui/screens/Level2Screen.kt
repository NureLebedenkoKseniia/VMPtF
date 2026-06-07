package com.example.pz3app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level2Screen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  var diceCount by remember { mutableStateOf(2) }
  var player1Dice by remember { mutableStateOf(List(diceCount) { 1 }) }
  var player2Dice by remember { mutableStateOf(List(diceCount) { 1 }) }
  var isRolling by remember { mutableStateOf(false) }
  var gameResult by remember { mutableStateOf<String?>(null) }
  var p1Score by remember { mutableStateOf(0) }
  var p2Score by remember { mutableStateOf(0) }
  var history by remember { mutableStateOf(listOf<String>()) }

  // Animated rotation/scale when rolling
  val transition = rememberInfiniteTransition(label = "rolling")
  val animatedScale by transition.animateFloat(
    initialValue = 1f,
    targetValue = if (isRolling) 1.15f else 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(150, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Рівень 2: Гра «Кістки»", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              p1Score = 0
              p2Score = 0
              history = emptyList()
              gameResult = null
              player1Dice = List(diceCount) { 1 }
              player2Dice = List(diceCount) { 1 }
            }
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Score")
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Scorecard Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Гравець", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("$p1Score", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9333EA))
          }
          Text("VS", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Суперник", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("$p2Score", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
          }
        }
      }

      // Dice count selector
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Кількість кубиків:  ", fontWeight = FontWeight.Medium)
        listOf(1, 2, 3, 4).forEach { count ->
          FilterChip(
            selected = diceCount == count,
            onClick = {
              if (!isRolling) {
                diceCount = count
                player1Dice = List(count) { 1 }
                player2Dice = List(count) { 1 }
                gameResult = null
              }
            },
            label = { Text("$count") },
            modifier = Modifier.padding(horizontal = 4.dp)
          )
        }
      }

      // Playing Field
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Player 1 Dice Rows
        Text("Гравець", fontWeight = FontWeight.Bold, color = Color(0xFF9333EA))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          player1Dice.forEach { val1 ->
            DiceView(
              value = val1,
              modifier = Modifier
                .padding(6.dp)
                .size(if (isRolling) (55.dp * animatedScale) else 55.dp),
              color = Color(0xFF9333EA)
            )
          }
        }
        Text(
          "Сума: ${player1Dice.sum()}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF9333EA)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Player 2 Dice Rows
        Text("Суперник", fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          player2Dice.forEach { val2 ->
            DiceView(
              value = val2,
              modifier = Modifier
                .padding(6.dp)
                .size(if (isRolling) (55.dp * animatedScale) else 55.dp),
              color = Color(0xFFF43F5E)
            )
          }
        }
        Text(
          "Сума: ${player2Dice.sum()}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFF43F5E)
        )
      }

      // Result overlay
      gameResult?.let { res ->
        Text(
          text = res,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = when {
            res.contains("Виграв Гравець") -> Color(0xFF9333EA)
            res.contains("Виграв Суперник") -> Color(0xFFF43F5E)
            else -> MaterialTheme.colorScheme.onSurface
          },
          modifier = Modifier.padding(vertical = 4.dp)
        )
      }

      // Roll Button
      Button(
        onClick = {
          scope.launch {
            isRolling = true
            gameResult = null
            // Simulate rolling physics
            for (i in 1..8) {
              player1Dice = List(diceCount) { Random.nextInt(1, 7) }
              player2Dice = List(diceCount) { Random.nextInt(1, 7) }
              delay(100)
            }
            isRolling = false

            val sum1 = player1Dice.sum()
            val sum2 = player2Dice.sum()

            val res = when {
              sum1 > sum2 -> {
                p1Score++
                "Виграв Гравець! ($sum1 проти $sum2)"
              }
              sum2 > sum1 -> {
                p2Score++
                "Виграв Суперник! ($sum2 проти $sum1)"
              }
              else -> "Нічия! ($sum1 : $sum2)"
            }
            gameResult = res
            history = listOf(res) + history.take(4) // Keep last 5 rounds
          }
        },
        enabled = !isRolling,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
      ) {
        Text(
          text = if (isRolling) "Кидаємо..." else "Кинути кубики",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      // History section
      if (history.isNotEmpty()) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Історія останніх ігор", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              items(history) { record ->
                Text(record, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun DiceView(value: Int, modifier: Modifier = Modifier, color: Color = Color.Black) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(Color.White)
      .border(2.dp, color, RoundedCornerShape(10.dp))
      .padding(4.dp)
  ) {
    // Custom grid arrangement for dice dots
    val arrangement = when (value) {
      1 -> listOf(Pair(1, 1))
      2 -> listOf(Pair(0, 0), Pair(2, 2))
      3 -> listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
      4 -> listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
      5 -> listOf(Pair(0, 0), Pair(0, 2), Pair(1, 1), Pair(2, 0), Pair(2, 2))
      6 -> listOf(Pair(0, 0), Pair(0, 2), Pair(1, 0), Pair(1, 2), Pair(2, 0), Pair(2, 2))
      else -> emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
      arrangement.forEach { (row, col) ->
        val alignMod = Modifier
          .align(
            when {
              row == 0 && col == 0 -> Alignment.TopStart
              row == 0 && col == 1 -> Alignment.TopCenter
              row == 0 && col == 2 -> Alignment.TopEnd
              row == 1 && col == 0 -> Alignment.CenterStart
              row == 1 && col == 1 -> Alignment.Center
              row == 1 && col == 2 -> Alignment.CenterEnd
              row == 2 && col == 0 -> Alignment.BottomStart
              row == 2 && col == 1 -> Alignment.BottomCenter
              else -> Alignment.BottomEnd
            }
          )
          .size(7.dp)
          .clip(RoundedCornerShape(50.dp))
          .background(color)

        Box(modifier = alignMod)
      }
    }
  }
}
