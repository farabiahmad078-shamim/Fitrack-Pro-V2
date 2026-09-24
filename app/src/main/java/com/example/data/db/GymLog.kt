package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_logs")
data class GymLog(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val isDone: Boolean = true
)
