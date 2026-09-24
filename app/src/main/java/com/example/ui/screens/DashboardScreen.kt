package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitViewModel
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MintGreen
import java.io.File
import java.util.*

@Composable
fun DashboardScreen(viewModel: FitViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    // Observe logs
    val gymLogsState by viewModel.gymLogs.collectAsState()
    val mealLogsState by viewModel.mealLogs.collectAsState()

    val currentYear by viewModel.calendarYear.collectAsState()
    val currentMonthVal by viewModel.calendarMonth.collectAsState()

    // Calculate completion rates
    val gymDaysCount = viewModel.getTotalGymDaysCount(currentYear, currentMonthVal)
    
    val tempCal = Calendar.getInstance()
    tempCal.set(Calendar.YEAR, currentYear)
    tempCal.set(Calendar.MONTH, currentMonthVal)
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val gymPercentage = if (daysInMonth > 0) gymDaysCount.toFloat() / daysInMonth.toFloat() else 0f
    val mealPercentageRaw = viewModel.getMealTrackedPercentage(currentYear, currentMonthVal)
    val mealPercentage = mealPercentageRaw / 100f

    // Animated values for rings
    val gymRingAnim by animateFloatAsState(targetValue = gymPercentage, animationSpec = tween(1200))
    val mealRingAnim by animateFloatAsState(targetValue = mealPercentage, animationSpec = tween(1200))

    val gymStreak = viewModel.getGymStreak()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ফিটট্র্যাক প্রো",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                )
                Text(
                    text = "FitTrack Pro • iOS 18 Edition",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }
        }

        // Beautiful iOS 18 Glassmorphism Progress Rings Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 0.dp else 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false,
                    spotColor = Color(0x33000000)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0x331C1C1E), Color(0x1A1C1C1E))
                        } else {
                            listOf(Color(0xE6FFFFFF), Color(0xCCFFFFFF))
                        }
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isDark) Color(0x33FFFFFF) else Color(0x33000000),
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drawing Concentric Rings
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val strokeW = 12.dp.toPx()
                        val spacing = 8.dp.toPx()

                        // Outer ring: Gym (Electric Blue)
                        val r1 = (size.width / 2) - (strokeW / 2)
                        // Track background (dimmed)
                        drawCircle(
                            color = if (isDark) Color(0x1A0A84FF) else Color(0x1F0A84FF),
                            radius = r1,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = strokeW)
                        )
                        // Active progress arc
                        drawArc(
                            color = ElectricBlue,
                            startAngle = -90f,
                            sweepAngle = 360f * gymRingAnim,
                            useCenter = false,
                            topLeft = Offset(centerX - r1, centerY - r1),
                            size = Size(r1 * 2, r1 * 2),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // Inner ring: Meals (Mint Green)
                        val r2 = r1 - strokeW - spacing
                        // Track background (dimmed)
                        drawCircle(
                            color = if (isDark) Color(0x1A30D158) else Color(0x1F30D158),
                            radius = r2,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = strokeW)
                        )
                        // Active progress arc
                        drawArc(
                            color = MintGreen,
                            startAngle = -90f,
                            sweepAngle = 360f * mealRingAnim,
                            useCenter = false,
                            topLeft = Offset(centerX - r2, centerY - r2),
                            size = Size(r2 * 2, r2 * 2),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }

                // Legend / Completion indicators
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(ElectricBlue, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "জিম ট্র্যাকার",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDark) Color.White else Color.Black
                                )
                            )
                        }
                        Text(
                            text = "${(gymPercentage * 100).toInt()}% সম্পন্ন",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MintGreen, RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "মিলের হিসাব",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isDark) Color.White else Color.Black
                                )
                            )
                        }
                        Text(
                            text = "${mealPercentageRaw.toInt()}% ট্র্যাকিং",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintGreen
                            )
                        )
                    }
                }
            }
        }

        // Summary Metric Section Heading
        Text(
            text = "চলতি মাসের অগ্রগতি",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDark) Color.White else Color.Black
            )
        )

        // Gym Summary Metrics Cards
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isDark) Color(0x1A0A84FF) else Color(0x0F0A84FF),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Gym done icon",
                            tint = ElectricBlue
                        )
                    }
                    Text(
                        text = "জিম ট্র্যাকার (Gym Summary)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "মোট সেশন",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "$gymDaysCount দিন",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = ElectricBlue
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "স্ট্রিক",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF9500),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "$gymStreak দিন",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF9500)
                            )
                        )
                    }
                }
            }
        }

        // Meals Summary Metrics Card
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isDark) Color(0x1A30D158) else Color(0x0F30D158),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Meal icon",
                            tint = MintGreen
                        )
                    }
                    Text(
                        text = "মিলের ট্র্যাকিং অগ্রগতি",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "মিলের হিসাব ট্র্যাকিং রেট",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", mealPercentageRaw)}%",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MintGreen
                            )
                        )
                    }

                    // Linear indicator
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(
                                color = if (isDark) Color(0x1FFF6B6B) else Color(0x1A000000),
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = mealPercentage)
                                .fillMaxHeight()
                                .background(
                                    color = MintGreen,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PDF Exporter Button
        Button(
            onClick = {
                val reportFile = viewModel.generatePdfReport(context)
                if (reportFile != null) {
                    Toast.makeText(context, "রিপোর্ট ডাউনলোড সফল! সংরক্ষিত স্থান: Document ফোল্ডার।", Toast.LENGTH_LONG).show()
                    // Propose opening or sharing the PDF!
                    sharePdf(context, reportFile)
                } else {
                    Toast.makeText(context, "রিপোর্ট তৈরি করতে ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0x22000000),
                    spotColor = Color(0x22007AFF)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) ElectricBlue else Color(0xFF007AFF)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download Report",
                    tint = Color.White
                )
                Text(
                    text = "Download Monthly Report (পিডিএফ ডাউনলোড)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Avoid navigation overlapping
    }
}

// Function to share pdf file nicely
private fun sharePdf(context: Context, file: File) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "মাসের রিপোর্ট শেয়ার করুন"))
    } catch (e: Exception) {
        Toast.makeText(context, "ফাইল শেয়ার করতে সমস্যা হচ্ছে!", Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}
