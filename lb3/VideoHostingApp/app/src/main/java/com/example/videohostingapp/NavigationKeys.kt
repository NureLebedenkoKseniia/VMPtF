package com.example.videohostingapp

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Dashboard : NavKey
@Serializable data class Player(val videoId: String) : NavKey
@Serializable data object Upload : NavKey
@Serializable data object Inbox : NavKey
@Serializable data object Profile : NavKey
