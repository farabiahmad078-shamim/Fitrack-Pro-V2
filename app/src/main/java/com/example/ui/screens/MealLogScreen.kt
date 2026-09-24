package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitViewModel
import com.example.ui.theme.MintGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MealLogScreen(viewModel: FitViewModel) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    // Observe calendar states
    val currentYear by viewModel.calendarYear.collectAsState()
    val currentMonthVal by viewModel.calendarMonth.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val mealLogsState by viewModel.mealLogs.collectAsState()

    // Monthly tracked percentage
    val mealPercentageRaw = viewModel.getMealTrackedPercentage(currentYear, currentMonthVal)

    // Current selected date parsed
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsedDate = try {
        sdf.parse(selectedDateStr) ?: Date()
    } catch (e: Exception) {
        Date()
    }
    val dateDisplayHeader = SimpleDateFormat("EEEE, d MMMM yyyy (EEEE, d MMMM)", Locale.getDefault()).apply {
        // Bengali locale localized representations if needed, but let's use a nice readable format!
    }.format(parsedDate)

    // Fetch the checklist record for the currently selected date
    val currentMealLog = viewModel.getSelectedDateMealLog()

    // Construct days list for horizontal carousel inside the current month
    val daysInMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonthVal)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    val carouselDays = remember(currentYear, currentMonthVal) {
        (1..daysInMonth).map { day ->
            String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonthVal + 1, day)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        Text(
            text = "মিলের হিসাব (Meal Log)",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = if (isDark) Color.White else Color.Black
            )
        )

        // Month Selection Carousel Label
        Text(
            text = "তারিখ নির্বাচন করুন",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        )

        // Horizontal Carousel Day Picker
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(carouselDays) { dateItemStr ->
                val dayNum = dateItemStr.substringAfterLast("-").toInt()
                val isSelected = dateItemStr == selectedDateStr
                
                // Day of week letter
                val itemCal = Calendar.getInstance()
                val dateParts = dateItemStr.split("-")
                itemCal.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
                val dayOfWeekStr = when (itemCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "রবি"
                    Calendar.MONDAY -> "সোম"
                    Calendar.TUESDAY -> "মঙ্গল"
                    Calendar.WEDNESDAY -> "বুধ"
                    Calendar.THURSDAY -> "বৃহ"
                    Calendar.FRIDAY -> "শুক্র"
                    Calendar.SATURDAY -> "শনি"
                    else -> ""
                }

                val hasLoggedMeal = mealLogsState.any { it.date == dateItemStr && it.isAnyChecked() }

                val borderElevation by animateDpAsState(targetValue = if (isSelected) 4.dp else 0.dp)

                Card(
                    modifier = Modifier
                        .width(68.dp)
                        .height(84.dp)
                        .shadow(
                            elevation = borderElevation,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = MintGreen
                        )
                        .clickable { viewModel.selectedDate.value = dateItemStr },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MintGreen
                        } else if (isDark) {
                            Color(0xFF16161A)
                        } else {
                            Color.White
                        }
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dayOfWeekStr,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            text = dayNum.toString(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        )

                        // Visual indicator if some meal logged that day
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (isSelected) Color.White else if (hasLoggedMeal) MintGreen else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        }

        Divider(
            color = if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000),
            thickness = 1.dp
        )

        // Selected Date label
        Text(
            text = "তালিকাবদ্ধ দিন: ${selectedDateStr}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (isDark) Color.White else Color.Black
            )
        )

        // Checklist of Meals
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MealChecklistItem(
                title = "প্রাতরাশ (Breakfast)",
                isChecked = currentMealLog.breakfast,
                icon = Icons.Default.Restaurant,
                onCheckedChange = { isChecked ->
                    viewModel.updateMeal(selectedDateStr, "breakfast", isChecked)
                }
            )

            MealChecklistItem(
                title = "দুপুরের খাবার (Lunch)",
                isChecked = currentMealLog.lunch,
                icon = Icons.Default.LunchDining,
                onCheckedChange = { isChecked ->
                    viewModel.updateMeal(selectedDateStr, "lunch", isChecked)
                }
            )

            MealChecklistItem(
                title = "রাতের খাবার (Dinner)",
                isChecked = currentMealLog.dinner,
                icon = Icons.Default.SoupKitchen,
                onCheckedChange = { isChecked ->
                    viewModel.updateMeal(selectedDateStr, "dinner", isChecked)
                }
            )

            MealChecklistItem(
                title = "হালকা খাবার (Snacks)",
                isChecked = currentMealLog.snacks,
                icon = Icons.Default.Cookie,
                onCheckedChange = { isChecked ->
                    viewModel.updateMeal(selectedDateStr, "snacks", isChecked)
                }
            )
        }

        // Monthly Summary Indicator Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "মিলের মাসিক সারাংশ",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ট্র্যাক করা দিনের হার",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "${mealPercentageRaw.toInt()}%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                    )
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { mealPercentageRaw / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MintGreen,
                    trackColor = if (isDark) Color(0x1F30D158) else Color(0x0D000000),
                )

                Text(
                    text = "ঐ মাসে আপনি অন্তত ১টি মিল ট্র্যাক করেছেন এমন দিনের সূচক।",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Avoid bottom nav overlaps
    }
}

@Composable
fun MealChecklistItem(
    title: String,
    isChecked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Smooth background & text color state transition on toggle active
    val cardBgColor by animateColorAsState(
        targetValue = if (isChecked) {
            if (isDark) Color(0x3330D158) else Color(0x0D30D158)
        } else {
            if (isDark) Color(0xFF16161A) else Color.White
        },
        animationSpec = tween(300)
    )

    val itemBorderColor by animateColorAsState(
        targetValue = if (isChecked) MintGreen else if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000),
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0x0D000000)
            )
            .border(
                width = 1.dp,
                color = itemBorderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (isChecked) MintGreen else if (isDark) Color(0x1AFFFFFF) else Color(0xFFF2F2F7),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked) Color.White else if (isDark) Color.White else Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                )
            }

            // iOS style checkbox / check indicator
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        color = if (isChecked) MintGreen else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isChecked) MintGreen else if (isDark) Color(0x4DFFFFFF) else Color(0x4D000000),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Logged",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
