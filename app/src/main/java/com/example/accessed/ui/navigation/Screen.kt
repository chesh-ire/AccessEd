package com.example.accessed.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    // Auth Screens
    object Welcome : Screen("welcome", "Welcome", null)
    object Login : Screen("login", "Login", null)
    object Signup : Screen("signup", "Sign Up", null)

    // Main App Screens
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Map : Screen("map", "Campus Map", Icons.Rounded.Map)
    object OCR : Screen("ocr", "OCR Reader", Icons.Rounded.Description)
    object Transcription : Screen("transcription", "Transcription", Icons.Rounded.RecordVoiceOver)
    object Dialogue : Screen("dialogue", "Dialogue", Icons.Rounded.Chat)
    object SafetyHub : Screen("safety_hub", "Safety Hub", Icons.Rounded.Warning)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Map,
    Screen.OCR,
    Screen.Transcription,
    Screen.Dialogue,
    Screen.SafetyHub
)
