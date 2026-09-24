package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitViewModel
import com.example.ui.theme.ElectricBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GymLogScreen(viewModel: FitViewModel) {
    val isDark = isSystemInDarkTheme()
    val view = LocalView.current

    // Collect calendar states
    val currentYear by viewModel.calendarYear.collectAsState()
    val currentMonthVal by viewModel.calendarMonth.collectAsState() // 0-indexed
    val gymLogsState by viewModel.gymLogs.collectAsState()

    // Monthly name display
    val calendarInstance = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonthVal)
    }
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(calendarInstance.time)

    // Calculate details
    val gymDaysCount = viewModel.getTotalGymDaysCount(currentYear, currentMonthVal)
    val gymStreak = viewModel.getGymStreak()

    // Calculate dates list for calendar grid
    val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Set to 1st of month to find week day offset
    calendarInstance.set(Calendar.DAY_OF_MONTH, 1)
    val dayOfWeekOffset = calendarInstance.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0 offsets, Monday = 1 offset, etc.

    val daysList = remember(currentYear, currentMonthVal) {
        val list = mutableListOf<String?>()
        // Leading empty slots
        for (i in 0 until dayOfWeekOffset) {
            list.add(null)
        }
        // Month days
        for (day in 1..daysInMonth) {
            list.add(String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonthVal + 1, day))
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title
        Text(
            text = "জিম লগ (Gym Log)",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = if (isDark) Color.White else Color.Black
            )
        )

        // Summary Card at Top (Gym Specific)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 0.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0x1A000000)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF16161A) else Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Days Gymed this Month",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "$gymDaysCount দিন",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Fire",
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$gymStreak দিন",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF9500)
                        )
                    )
                }
            }
        }

        // Calendar Controller Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Back Arrow
                IconButton(
                    onClick = { viewModel.prevMonth() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isDark) Color(0xFF16161A) else Color(0xFFE5E5EA)
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month"
                    )
                }

                // Next Arrow
                IconButton(
                    onClick = { viewModel.nextMonth() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isDark) Color(0xFF16161A) else Color(0xFFE5E5EA)
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month"
                    )
                }
            }
        }

        // Week Days labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val weekDays = listOf("সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি", "রবি")
            val weekDaysEng = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            
            // Re-order if Sunday is first
            val displayDays = listOf("S", "M", "T", "W", "T", "F", "S")

            displayDays.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }

        // Calendar dates grid in LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysList) { dateStr ->
                if (dateStr == null) {
                    // Empty grid cell
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val isDone = gymLogsState.any { it.date == dateStr }
                    val dayNum = dateStr.substringAfterLast("-").toInt()

                    // Visual bubbly scaling when active state toggles
                    val scale by animateFloatAsState(
                        targetValue = if (isDone) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .scale(scale)
                            .shadow(
                                elevation = if (isDone && !isDark) 4.dp else 0.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = ElectricBlue
                            )
                            .background(
                                color = if (isDone) {
                                    ElectricBlue
                                } else if (isDark) {
                                    Color(0xFF16161A)
                                } else {
                                    Color.White
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isDone) {
                                    ElectricBlue
                                } else if (isDark) {
                                    Color(0x1AFFFFFF)
                                } else {
                                    Color(0x0D000000)
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                // Keyboard-tap haptics + visual spring triggers 'Haptic-like pop animation'
                                viewModel.toggleGymDone(dateStr) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) Color.White else MaterialTheme.colorScheme.onBackground
                                )
                            )
                            
                            if (isDone) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "Done",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
