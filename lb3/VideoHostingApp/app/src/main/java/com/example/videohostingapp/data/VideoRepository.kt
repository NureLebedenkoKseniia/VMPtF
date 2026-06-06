package com.example.videohostingapp.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class DatabaseState(
  val videos: List<Video>,
  val subscriptions: List<String>, // List of channel names/creators subscribed to
  val messages: List<Message>,
  val currentUsername: String = "Kseniya",
  val currentUserRole: String = "CREATOR" // Default role
)

class VideoRepository(private val context: Context) {
  private val dbFile = File(context.filesDir, "db_videohosting.json")
  private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

  private val _videosFlow = MutableStateFlow<List<Video>>(emptyList())
  val videosFlow: StateFlow<List<Video>> = _videosFlow.asStateFlow()

  private val _subscriptionsFlow = MutableStateFlow<List<String>>(emptyList())
  val subscriptionsFlow: StateFlow<List<String>> = _subscriptionsFlow.asStateFlow()

  private val _messagesFlow = MutableStateFlow<List<Message>>(emptyList())
  val messagesFlow: StateFlow<List<Message>> = _messagesFlow.asStateFlow()

  private val _currentUserFlow = MutableStateFlow(User("Kseniya", "CREATOR"))
  val currentUserFlow: StateFlow<User> = _currentUserFlow.asStateFlow()

  init {
    loadFromDisk()
  }

  private fun loadFromDisk() {
    if (dbFile.exists()) {
      try {
        val content = dbFile.readText()
        val state = json.decodeFromString<DatabaseState>(content)
        _videosFlow.value = state.videos
        _subscriptionsFlow.value = state.subscriptions
        _messagesFlow.value = state.messages
        _currentUserFlow.value = User(state.currentUsername, state.currentUserRole)
      } catch (e: Exception) {
        e.printStackTrace()
        loadSeedData()
      }
    } else {
      loadSeedData()
    }
  }

  private fun saveToDisk() {
    try {
      val state = DatabaseState(
        videos = _videosFlow.value,
        subscriptions = _subscriptionsFlow.value,
        messages = _messagesFlow.value,
        currentUsername = _currentUserFlow.value.username,
        currentUserRole = _currentUserFlow.value.role
      )
      val content = json.encodeToString(state)
      dbFile.writeText(content)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun loadSeedData() {
    val seedVideos = listOf(
      Video(
        id = "1",
        title = "Big Buck Bunny",
        description = "A large and lovable rabbit deals with bully squirrels in a forest.",
        creator = "Blender Foundation",
        duration = "9:56",
        views = 124500,
        likes = 1250,
        category = "Анімація",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        comments = listOf(
          Comment("c1", "Ivan", "Чудова класична анімація!", System.currentTimeMillis() - 86400000),
          Comment("c2", "Maria", "Кумедні білочки :)", System.currentTimeMillis() - 36000000)
        )
      ),
      Video(
        id = "2",
        title = "Elephants Dream",
        description = "The first open-source 3D animated movie, created by the Blender Foundation.",
        creator = "Blender Foundation",
        duration = "10:53",
        views = 89000,
        likes = 942,
        category = "Анімація",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        comments = listOf(
          Comment("c3", "TechFan", "Дуже сюрреалістичний сюжет.", System.currentTimeMillis() - 72000000)
        )
      ),
      Video(
        id = "3",
        title = "For Bigger Blazes",
        description = "Chromecast integration promotional video showing fire and action.",
        creator = "Google LLC",
        duration = "0:15",
        views = 432000,
        likes = 3490,
        category = "Промо",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
      ),
      Video(
        id = "4",
        title = "Tears of Steel",
        description = "A sci-fi film set in a dystopian future Amsterdam, featuring giant robots and VFX.",
        creator = "Mango Project",
        duration = "12:14",
        views = 230000,
        likes = 2100,
        category = "Фантастика",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        comments = listOf(
          Comment("c4", "SciFiGuy", "Ефекти просто бомба для 2012 року!", System.currentTimeMillis() - 12000000)
        )
      )
    )
    _videosFlow.value = seedVideos
    _subscriptionsFlow.value = listOf("Blender Foundation") // Default subscription
    _messagesFlow.value = emptyList()
    _currentUserFlow.value = User("Kseniya", "CREATOR")
    saveToDisk()
  }

  // Update current user
  fun updateCurrentUser(username: String, role: String) {
    _currentUserFlow.value = User(username, role)
    saveToDisk()
  }

  // Toggle Video Like
  fun toggleLikeVideo(videoId: String) {
    val username = _currentUserFlow.value.username
    val updated = _videosFlow.value.map { video ->
      if (video.id == videoId) {
        val likedBy = video.likedByUsers.toMutableList()
        val currentLikes = video.likes
        val (newLikes, newLikedBy) = if (likedBy.contains(username)) {
          likedBy.remove(username)
          Pair(currentLikes - 1, likedBy)
        } else {
          likedBy.add(username)
          Pair(currentLikes + 1, likedBy)
        }
        video.copy(likes = newLikes, likedByUsers = newLikedBy)
      } else {
        video
      }
    }
    _videosFlow.value = updated
    saveToDisk()
  }

  // Add Comment to Video
  fun addComment(videoId: String, commentText: String) {
    val author = _currentUserFlow.value.username
    val newComment = Comment(
      id = UUID.randomUUID().toString(),
      author = author,
      text = commentText,
      timestamp = System.currentTimeMillis()
    )
    val updated = _videosFlow.value.map { video ->
      if (video.id == videoId) {
        video.copy(comments = video.comments + newComment)
      } else {
        video
      }
    }
    _videosFlow.value = updated
    saveToDisk()
  }

  // Delete Video (Admin/Creator Only)
  fun deleteVideo(videoId: String): Boolean {
    val currentUser = _currentUserFlow.value
    val video = _videosFlow.value.find { it.id == videoId } ?: return false
    val isAllowed = currentUser.role == "ADMIN" || 
                    (currentUser.role == "CREATOR" && video.creator == currentUser.username)
    if (!isAllowed) return false

    val updated = _videosFlow.value.filterNot { it.id == videoId }
    _videosFlow.value = updated
    saveToDisk()
    return true
  }

  // Delete Comment (Admin Only)
  fun deleteComment(videoId: String, commentId: String): Boolean {
    if (_currentUserFlow.value.role != "ADMIN") return false
    val updated = _videosFlow.value.map { video ->
      if (video.id == videoId) {
        video.copy(comments = video.comments.filterNot { it.id == commentId })
      } else {
        video
      }
    }
    _videosFlow.value = updated
    saveToDisk()
    return true
  }

  // Upload Video (Creator/Admin Only)
  fun uploadVideo(title: String, description: String, category: String, url: String, duration: String): Boolean {
    val role = _currentUserFlow.value.role
    if (role != "CREATOR" && role != "ADMIN") return false

    val newVideo = Video(
      id = UUID.randomUUID().toString(),
      title = title,
      description = description,
      creator = _currentUserFlow.value.username,
      duration = duration,
      views = 0,
      likes = 0,
      likedByUsers = emptyList(),
      url = url,
      comments = emptyList(),
      category = category
    )
    _videosFlow.value = _videosFlow.value + newVideo
    saveToDisk()
    return true
  }

  // Toggle channel subscription
  fun toggleSubscription(channelName: String) {
    val currentSubs = _subscriptionsFlow.value.toMutableList()
    if (currentSubs.contains(channelName)) {
      currentSubs.remove(channelName)
    } else {
      currentSubs.add(channelName)
    }
    _subscriptionsFlow.value = currentSubs
    saveToDisk()
  }

  // Send/Share Video Message to other user
  fun shareVideo(videoId: String, videoTitle: String, recipientName: String, text: String) {
    val newMessage = Message(
      id = UUID.randomUUID().toString(),
      sender = _currentUserFlow.value.username,
      recipient = recipientName,
      videoId = videoId,
      videoTitle = videoTitle,
      text = text,
      timestamp = System.currentTimeMillis()
    )
    _messagesFlow.value = _messagesFlow.value + newMessage
    saveToDisk()
  }
}
