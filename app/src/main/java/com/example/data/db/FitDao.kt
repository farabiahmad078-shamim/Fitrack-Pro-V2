package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitDao {
    // Gym log queries
    @Query("SELECT * FROM gym_logs")
    fun getAllGymLogs(): Flow<List<GymLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymLog(gymLog: GymLog)

    @Query("DELETE FROM gym_logs WHERE date = :date")
    suspend fun deleteGymLogByDate(date: String)

    // Meal log queries
    @Query("SELECT * FROM meal_logs")
    fun getAllMealLogs(): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE date = :date LIMIT 1")
    suspend fun getMealLogByDate(date: String): MealLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs WHERE date = :date")
    suspend fun deleteMealLogByDate(date: String)
}
