package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val breakfast: Boolean = false,
    val lunch: Boolean = false,
    val dinner: Boolean = false,
    val snacks: Boolean = false
) {
    fun isAnyChecked(): Boolean = breakfast || lunch || dinner || snacks
    fun totalCheckedCount(): Int {
        var count = 0
        if (breakfast) count++
        if (lunch) count++
        if (dinner) count++
        if (snacks) count++
        return count
    }
}
