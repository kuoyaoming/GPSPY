package com.example.gpstracker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long, // Group points by tracking session
    val latitude: Double,
    val longitude: Double,
    val altitude: Double, // WGS 84 elevation in meters
    val speed: Float,     // m/s
    val bearing: Float,   // degrees
    val timestamp: Long   // ms since epoch
)
