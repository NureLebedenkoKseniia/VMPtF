package com.example.videohostingapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
  val id: String,
  val author: String,
  val text: String,
  val timestamp: Long
)

@Serializable
data class Video(
  val id: String,
  val title: String,
  val description: String,
  val creator: String,
  val duration: String,
  val views: Int,
  val likes: Int,
  val likedByUsers: List<String> = emptyList(), // List of usernames who liked it
  val url: String, // Stream URL or local path
  val comments: List<Comment> = emptyList(),
  val category: String
)

@Serializable
data class Message(
  val id: String,
  val sender: String,
  val recipient: String,
  val videoId: String,
  val videoTitle: String,
  val text: String,
  val timestamp: Long
)

enum class UserRole {
  VIEWER,
  CREATOR,
  ADMIN
}

@Serializable
data class User(
  val username: String,
  val role: String // VIEWER, CREATOR, ADMIN
)
