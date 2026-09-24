package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GymLogScreen
import com.example.ui.screens.MealLogScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.FitTrackTheme
import com.example.ui.theme.MintGreen

class MainActivity : ComponentActivity() {
    private val viewModel: FitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackTheme {
                FitTrackApp(viewModel)
            }
        }
    }
}

@Composable
fun FitTrackApp(viewModel: FitViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isDark = isSystemInDarkTheme()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FitTrackBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                FitViewModel.Tab.Dashboard -> DashboardScreen(viewModel = viewModel)
                FitViewModel.Tab.GymLog -> GymLogScreen(viewModel = viewModel)
                FitViewModel.Tab.MealLog -> MealLogScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FitTrackBottomNavigation(
    selectedTab: FitViewModel.Tab,
    onTabSelected: (FitViewModel.Tab) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Premium custom iOS style bottom navigation with haptic buttons
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                spotColor = Color(0x33000000),
                ambientColor = Color(0x33000000)
            ),
        color = if (isDark) Color(0xFF0C0C0E) else Color.White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dashboard
            FitTrackBottomNavItem(
                icon = Icons.Default.Dashboard,
                label = "ড্যাশবোর্ড",
                isSelected = selectedTab == FitViewModel.Tab.Dashboard,
                activeColor = ElectricBlue,
                testTag = "dashboard_tab",
                onClick = { onTabSelected(FitViewModel.Tab.Dashboard) }
            )

            // Gym Log
            FitTrackBottomNavItem(
                icon = Icons.Default.FitnessCenter,
                label = "জিম লগ",
                isSelected = selectedTab == FitViewModel.Tab.GymLog,
                activeColor = ElectricBlue,
                testTag = "gym_log_tab",
                onClick = { onTabSelected(FitViewModel.Tab.GymLog) }
            )

            // Meal Log
            FitTrackBottomNavItem(
                icon = Icons.Default.Restaurant,
                label = "মিলের হিসাব",
                isSelected = selectedTab == FitViewModel.Tab.MealLog,
                activeColor = MintGreen,
                testTag = "meal_log_tab",
                onClick = { onTabSelected(FitViewModel.Tab.MealLog) }
            )
        }
    }
}

@Composable
fun RowScope.FitTrackBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else if (isDark) Color(0x80FFFFFF) else Color(0x80000000),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) activeColor else if (isDark) Color(0x99FFFFFF) else Color(0x99000000)
                )
            )
        }
    }
}
