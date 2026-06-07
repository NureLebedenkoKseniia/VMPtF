package com.example.pz3app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

// Data classes representing files
data class ScannedFile(
  val name: String,
  val size: Long,
  val hash: String,
  val uri: Uri,
  val relativePath: String,
  val isLocal: Boolean, // Local internal file or SAF DocumentFile
  val systemFile: File? = null,
  val docFile: DocumentFile? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level4Screen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var isScanning by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var duplicateGroups by remember { mutableStateOf<Map<String, List<ScannedFile>>>(emptyMap()) }
  var testFilesFolderCreated by remember { mutableStateOf(false) }

  // Check if test duplicates are present
  LaunchedEffect(Unit) {
    val testDir = File(context.filesDir, "test_duplicates")
    testFilesFolderCreated = testDir.exists() && testDir.list()?.isNotEmpty() == true
  }

  // SAF Folder picker
  val openDocumentTreeLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { treeUri ->
      if (treeUri != null) {
        scope.launch {
          isScanning = true
          statusMessage = "Сканування вибраної директорії..."
          val groups = scanSafDirectory(context, treeUri)
          duplicateGroups = groups
          isScanning = false
          statusMessage = if (groups.isEmpty()) "Дублікатів не знайдено." else "Знайдено дублікати."
        }
      }
    }
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Рівень 4: Видалення дублікатів", fontWeight = FontWeight.Bold) },
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Пошук однакових файлів за хешем SHA-256",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )

      // Setup mock files for easy testing
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Локальне тестування:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
          Text(
            "Ви можете згенерувати тестові дублікати файлів у внутрішній пам'яті програми та миттєво відсканувати їх без вибору папки.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
          )
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Button(
              onClick = {
                createTestDuplicates(context)
                testFilesFolderCreated = true
                statusMessage = "Тестові файли створено в 'test_duplicates'."
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Створити тест-файли", fontSize = 11.sp)
            }

            Button(
              onClick = {
                scope.launch {
                  isScanning = true
                  statusMessage = "Сканування внутрішніх файлів..."
                  val groups = scanInternalDirectory(context)
                  duplicateGroups = groups
                  isScanning = false
                  statusMessage = if (groups.isEmpty()) "Дублікатів не знайдено." else "Знайдено локальні дублікати."
                }
              },
              enabled = testFilesFolderCreated,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("Сканувати локально", fontSize = 11.sp)
            }
          }
        }
      }

      // Main scan button
      Button(
        onClick = { openDocumentTreeLauncher.launch(null) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308)),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
      ) {
        Text("Обрати та відсканувати папку", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }

      statusMessage?.let { msg ->
        Text(
          text = msg,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.align(Alignment.CenterHorizontally)
        )
      }

      if (isScanning) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = Color(0xFFEAB308))
        }
      } else {
        // List duplicates
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(duplicateGroups.entries.toList()) { entry ->
            val hash = entry.key
            val filesList = entry.value
            val firstFile = filesList.firstOrNull()

            if (filesList.size > 1 && firstFile != null) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${firstFile.name} (${formatSize(firstFile.size)})",
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "${filesList.size} дублікатів",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFEAB308)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Хеш: ${hash.take(16)}...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Divider()
                  Spacer(modifier = Modifier.height(8.dp))

                  filesList.forEachIndexed { index, file ->
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = if (file.relativePath.isEmpty()) "Корінь" else file.relativePath,
                          fontSize = 12.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (index == 0) {
                          Text(
                            text = "Оригінал (зберігається)",
                            fontSize = 11.sp,
                            color = Color(0xFF0D9488),
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }

                      if (index > 0) {
                        IconButton(
                          onClick = {
                            scope.launch {
                              val success = deleteFile(context, file)
                              if (success) {
                                // Refresh current UI state
                                val updatedList = filesList.toMutableList()
                                updatedList.removeAt(index)
                                val updatedGroups = duplicateGroups.toMutableMap()
                                if (updatedList.size > 1) {
                                  updatedGroups[hash] = updatedList
                                } else {
                                  updatedGroups.remove(hash)
                                }
                                duplicateGroups = updatedGroups
                                statusMessage = "Дублікат видалено."
                              } else {
                                statusMessage = "Не вдалося видалити файл."
                              }
                            }
                          }
                        ) {
                          Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Видалити",
                            tint = MaterialTheme.colorScheme.error
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

// Traverse selected SAF document tree
suspend fun scanSafDirectory(context: Context, treeUri: Uri): Map<String, List<ScannedFile>> =
  withContext(Dispatchers.IO) {
    val result = mutableListOf<ScannedFile>()
    val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
    if (rootDoc != null) {
      traverseSaf(context, rootDoc, "", result)
    }
    return@withContext result.groupBy { it.hash }.filter { it.value.size > 1 }
  }

fun traverseSaf(context: Context, directory: DocumentFile, relativePath: String, result: MutableList<ScannedFile>) {
  val files = directory.listFiles()
  for (file in files) {
    if (file.isDirectory) {
      traverseSaf(context, file, "$relativePath/${file.name}", result)
    } else if (file.isFile) {
      try {
        val hash = calculateSafHash(context, file.uri)
        result.add(
          ScannedFile(
            name = file.name ?: "Unnamed",
            size = file.length(),
            hash = hash,
            uri = file.uri,
            relativePath = relativePath,
            isLocal = false,
            docFile = file
          )
        )
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}

// Compute hash for a SAF DocumentFile
fun calculateSafHash(context: Context, uri: Uri): String {
  val digest = MessageDigest.getInstance("SHA-256")
  context.contentResolver.openInputStream(uri)?.use { inputStream ->
    val buffer = ByteArray(8192)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
  }
  return digest.digest().joinToString("") { String.format("%02x", it) }
}

// Traverse local internal app files
suspend fun scanInternalDirectory(context: Context): Map<String, List<ScannedFile>> =
  withContext(Dispatchers.IO) {
    val result = mutableListOf<ScannedFile>()
    val testDir = File(context.filesDir, "test_duplicates")
    if (testDir.exists()) {
      traverseLocal(testDir, "", result)
    }
    return@withContext result.groupBy { it.hash }.filter { it.value.size > 1 }
  }

fun traverseLocal(directory: File, relativePath: String, result: MutableList<ScannedFile>) {
  val files = directory.listFiles() ?: return
  for (file in files) {
    if (file.isDirectory) {
      traverseLocal(file, "$relativePath/${file.name}", result)
    } else if (file.isFile) {
      try {
        val hash = calculateLocalHash(file)
        result.add(
          ScannedFile(
            name = file.name,
            size = file.length(),
            hash = hash,
            uri = Uri.fromFile(file),
            relativePath = relativePath,
            isLocal = true,
            systemFile = file
          )
        )
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}

// Compute hash for local system File
fun calculateLocalHash(file: File): String {
  val digest = MessageDigest.getInstance("SHA-256")
  file.inputStream().use { inputStream ->
    val buffer = ByteArray(8192)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
  }
  return digest.digest().joinToString("") { String.format("%02x", it) }
}

// Create mock file structure for testing
fun createTestDuplicates(context: Context) {
  val testDir = File(context.filesDir, "test_duplicates")
  testDir.mkdirs()

  // Clean existing first
  testDir.deleteRecursively()
  testDir.mkdirs()

  // Original & Duplicates of Group 1
  File(testDir, "original_doc.txt").writeText("Hello from variant 6! This is a test file.")
  File(testDir, "duplicate_doc_1.txt").writeText("Hello from variant 6! This is a test file.")

  // Original & Duplicates of Group 2
  val subDir = File(testDir, "photos_backup")
  subDir.mkdirs()
  File(testDir, "sea_photo.png").writeText("Fake PNG content: blue sea.")
  File(subDir, "sea_photo_copy.png").writeText("Fake PNG content: blue sea.")
  File(subDir, "sea_photo_backup.png").writeText("Fake PNG content: blue sea.")

  // Unique files
  File(testDir, "unique_readme.md").writeText("This is an entirely unique file.")
}

// Helper to delete duplicate file
suspend fun deleteFile(context: Context, file: ScannedFile): Boolean =
  withContext(Dispatchers.IO) {
    return@withContext try {
      if (file.isLocal) {
        file.systemFile?.delete() == true
      } else {
        file.docFile?.delete() == true
      }
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

// Size formatting helper
fun formatSize(size: Long): String {
  if (size < 1024) return "$size B"
  val kb = size / 1024
  if (kb < 1024) return "$kb KB"
  val mb = kb / 1024
  return "$mb MB"
}
