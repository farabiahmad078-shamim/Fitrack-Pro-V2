package com.example.data.repo

import com.example.data.db.FitDao
import com.example.data.db.GymLog
import com.example.data.db.MealLog
import kotlinx.coroutines.flow.Flow

class FitRepository(private val fitDao: FitDao) {
    val allGymLogs: Flow<List<GymLog>> = fitDao.getAllGymLogs()
    val allMealLogs: Flow<List<MealLog>> = fitDao.getAllMealLogs()

    suspend fun insertGymLog(gymLog: GymLog) {
        fitDao.insertGymLog(gymLog)
    }

    suspend fun deleteGymLogByDate(date: String) {
        fitDao.deleteGymLogByDate(date)
    }

    suspend fun getMealLogByDate(date: String): MealLog? {
        return fitDao.getMealLogByDate(date)
    }

    suspend fun insertMealLog(mealLog: MealLog) {
        fitDao.insertMealLog(mealLog)
    }

    suspend fun deleteMealLogByDate(date: String) {
        fitDao.deleteMealLogByDate(date)
    }
}
