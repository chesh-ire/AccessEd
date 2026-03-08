package com.example.accessed.data.model

data class CampusLocation(
    val locationId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val isAccessible: Boolean = true,
    val landmarkDescription: String = ""
)
