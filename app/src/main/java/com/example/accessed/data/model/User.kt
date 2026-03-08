package com.example.accessed.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student",
    val isGuest: Boolean = false
)
