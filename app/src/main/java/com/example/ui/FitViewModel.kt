package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.GymLog
import com.example.data.db.MealLog
import com.example.data.repo.FitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FitRepository

    private val calendar = Calendar.getInstance()
    
    // UI State for tabs & selections
    val currentTab = MutableStateFlow(Tab.Dashboard)
    val selectedDate = MutableStateFlow("") // Format: YYYY-MM-DD
    
    // Calendar month/year navigation state
    val calendarYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val calendarMonth = MutableStateFlow(calendar.get(Calendar.MONTH)) // 0-indexed (0 = Jan)

    // Raw databases flows
    val gymLogs = MutableStateFlow<List<GymLog>>(emptyList())
    val mealLogs = MutableStateFlow<List<MealLog>>(emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FitRepository(database.fitDao())
        
        // Initialize today's date
        selectedDate.value = getFormattedDate(calendar.time)

        // Collect database changes
        viewModelScope.launch {
            repository.allGymLogs.collect {
                gymLogs.value = it
            }
        }
        viewModelScope.launch {
            repository.allMealLogs.collect {
                mealLogs.value = it
            }
        }
    }

    private fun getFormattedDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(date)
    }

    enum class Tab {
        Dashboard, GymLog, MealLog
    }

    fun selectTab(tab: Tab) {
        currentTab.value = tab
    }

    // Toggle Gym Done
    fun toggleGymDone(dateStr: String, onHapticFeedback: () -> Unit) {
        viewModelScope.launch {
            val exists = gymLogs.value.any { it.date == dateStr }
            if (exists) {
                repository.deleteGymLogByDate(dateStr)
            } else {
                repository.insertGymLog(GymLog(date = dateStr, isDone = true))
                // subtle pop haptic feedback
                onHapticFeedback()
            }
        }
    }

    // Update Meal log
    fun updateMeal(dateStr: String, mealType: String, isChecked: Boolean) {
        viewModelScope.launch {
            val existing = mealLogs.value.find { it.date == dateStr } ?: MealLog(date = dateStr)
            val updated = when (mealType) {
                "breakfast" -> existing.copy(breakfast = isChecked)
                "lunch" -> existing.copy(lunch = isChecked)
                "dinner" -> existing.copy(dinner = isChecked)
                "snacks" -> existing.copy(snacks = isChecked)
                else -> existing
            }
            if (!updated.breakfast && !updated.lunch && !updated.dinner && !updated.snacks) {
                repository.deleteMealLogByDate(dateStr)
            } else {
                repository.insertMealLog(updated)
            }
        }
    }

    // Math/Statistic calculations for Monthly Gym
    fun getTotalGymDaysCount(year: Int, monthInt: Int): Int {
        val prefix = String.format(Locale.US, "%04d-%02d", year, monthInt + 1)
        return gymLogs.value.filter { it.date.startsWith(prefix) && it.isDone }.size
    }

    // Calculate current Gym Streak
    fun getGymStreak(): Int {
        val activeLogs = gymLogs.value.filter { it.isDone }.associateBy { it.date }
        if (activeLogs.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayCal = Calendar.getInstance()
        var streak = 0

        // Check if today is done. If not, start check from yesterday.
        val todayStr = dateFormat.format(todayCal.time)
        val hasToday = activeLogs.containsKey(todayStr)

        todayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(todayCal.time)
        val hasYesterday = activeLogs.containsKey(yesterdayStr)

        if (!hasToday && !hasYesterday) return 0

        // Reset check to starting point
        val checkCal = Calendar.getInstance()
        if (!hasToday) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1) // Start from yesterday if today is not logged yet
        }

        while (true) {
            val checkStr = dateFormat.format(checkCal.time)
            if (activeLogs.containsKey(checkStr)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    // Percentage of days meal was properly tracked
    // Properly tracked meaning at least one meal checked for that day.
    fun getMealTrackedPercentage(year: Int, monthInt: Int): Float {
        val prefix = String.format(Locale.US, "%04d-%02d", year, monthInt + 1)
        val trackedDaysCount = mealLogs.value.filter { it.date.startsWith(prefix) && it.isAnyChecked() }.size
        
        // Find total days in that month
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, year)
        tempCal.set(Calendar.MONTH, monthInt)
        val totalDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        return (trackedDaysCount.toFloat() / totalDaysInMonth.toFloat()) * 100f
    }

    // Calendar helper methods
    fun nextMonth() {
        if (calendarMonth.value == 11) {
            calendarMonth.value = 0
            calendarYear.value++
        } else {
            calendarMonth.value++
        }
    }

    fun prevMonth() {
        if (calendarMonth.value == 0) {
            calendarMonth.value = 11
            calendarYear.value--
        } else {
            calendarMonth.value--
        }
    }

    fun isSelectedDateDoneGym(): Boolean {
        return gymLogs.value.any { it.date == selectedDate.value }
    }

    fun getSelectedDateMealLog(): MealLog {
        return mealLogs.value.find { it.date == selectedDate.value } ?: MealLog(date = selectedDate.value)
    }

    // Export PDF monthly report
    fun generatePdfReport(context: Context): File? {
        val year = calendarYear.value
        val monthInt = calendarMonth.value
        val rCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthInt)
        }
        val monthDisplayName = SimpleDateFormat("MMMM yyyy", Locale.US).format(rCal.time)

        val fileName = "FitTrack_Report_${year}_${monthInt + 1}.pdf"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val pdfFile = File(storageDir, fileName)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1000, 1400, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Brush paints
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 44f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val gymColorPaint = Paint().apply {
            color = Color.parseColor("#007AFF") // Electric Blue
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val mealColorPaint = Paint().apply {
            color = Color.parseColor("#34C759") // Mint Green
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val lightGrayLinePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Title Header
        canvas.drawText("FitTrack Pro - Monthly Performance Report", 60f, 80f, headerPaint)
        canvas.drawText("Month: $monthDisplayName", 60f, 130f, textPaint)
        canvas.drawLine(60f, 160f, 940f, 160f, lightGrayLinePaint)

        // Summary Card Top section
        textPaint.textSize = 32f
        canvas.drawText("Monthly Summary Metrics:", 60f, 210f, textPaint)
        textPaint.textSize = 26f
        
        val totalGymDays = getTotalGymDaysCount(year, monthInt)
        val streak = getGymStreak()
        val mealPercentage = getMealTrackedPercentage(year, monthInt)

        canvas.drawText("• Total Gym Days in Month: $totalGymDays days", 80f, 260f, gymColorPaint)
        canvas.drawText("• Current Gym Streak: $streak days", 80f, 300f, gymColorPaint)
        canvas.drawText("• Meals Tracking Completion: ${String.format(Locale.US, "%.1f", mealPercentage)}%", 80f, 340f, mealColorPaint)
        
        canvas.drawLine(60f, 380f, 940f, 380f, lightGrayLinePaint)
        
        // Calendar & Meal Breakdown Table
        canvas.drawText("Daily Tracking Log Status:", 60f, 430f, textPaint)

        // Draw Table Header
        val startY = 470f
        val colDateX = 80f
        val colGymX = 280f
        val colMealsX = 480f
        
        canvas.drawText("Date", colDateX, startY, headerPaint.apply { textSize = 26f; color = Color.BLACK })
        canvas.drawText("Gym Done", colGymX, startY, gymColorPaint)
        canvas.drawText("Meals Tracked (B, L, D, S)", colMealsX, startY, mealColorPaint)
        
        canvas.drawLine(60f, startY + 15f, 940f, startY + 15f, lightGrayLinePaint)

        val prefix = String.format(Locale.US, "%04d-%02d", year, monthInt + 1)
        
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, year)
        tempCal.set(Calendar.MONTH, monthInt)
        val totalDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        var rowY = startY + 55f
        textPaint.textSize = 22f

        for (day in 1..totalDays) {
            val dayStr = String.format(Locale.US, "%04d-%02d-%02d", year, monthInt + 1, day)
            val gymDone = gymLogs.value.any { it.date == dayStr }
            val mealLog = mealLogs.value.find { it.date == dayStr }

            // Alternate light coloring for readability
            if (day % 2 == 0) {
                val backgroundPaint = Paint().apply { color = Color.parseColor("#F5F5F7") }
                canvas.drawRect(60f, rowY - 25f, 940f, rowY + 10f, backgroundPaint)
            }

            canvas.drawText(dayStr, colDateX, rowY, textPaint)
            if (gymDone) {
                canvas.drawText("Yes (Checkmark)", colGymX, rowY, gymColorPaint.apply { textSize = 22f })
            } else {
                canvas.drawText("-", colGymX, rowY, textPaint)
            }

            if (mealLog != null && mealLog.isAnyChecked()) {
                val mealsChecked = mutableListOf<String>()
                if (mealLog.breakfast) mealsChecked.add("Breakfast")
                if (mealLog.lunch) mealsChecked.add("Lunch")
                if (mealLog.dinner) mealsChecked.add("Dinner")
                if (mealLog.snacks) mealsChecked.add("Snacks")
                canvas.drawText(mealsChecked.joinToString(", "), colMealsX, rowY, textPaint)
            } else {
                canvas.drawText("-", colMealsX, rowY, textPaint)
            }

            rowY += 40f
        }

        // Footer is mandatory: "Generated by: Shamim Ahmad"
        canvas.drawLine(60f, 1310f, 940f, 1310f, lightGrayLinePaint)
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("Generated by: Shamim Ahmad", 60f, 1350f, footerPaint)

        document.finishPage(page)

        try {
            val fos = FileOutputStream(pdfFile)
            document.writeTo(fos)
            document.close()
            fos.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }
}
