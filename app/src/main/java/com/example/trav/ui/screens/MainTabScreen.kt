package com.example.trav.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.trav.R
import com.example.trav.data.Trip
import com.example.trav.ui.theme.NotoSansKR

enum class TabItem(val title: String, val iconResId: Int) {
    DayPlan("DAY PLAN", R.drawable.ic_dayplan),
    TimeTable("TIMETABLE", R.drawable.ic_timetable),
    Budget("BUDGET", R.drawable.ic_budget),
    Checklist("CHECKLIST", R.drawable.ic_checklist)
}

@Composable
fun MainTabScreen(trip: Trip?) {
    var currentTab by remember { mutableStateOf(TabItem.DayPlan) }

    // DayPlan 화면 강제 초기화용 Key
    var dayPlanKey by remember { mutableIntStateOf(0) }

    val appBackgroundColor = Color(0xFFFAFAFA)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appBackgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    if (trip == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(appBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.Black)
        }
        return
    }

    Scaffold(
        containerColor = appBackgroundColor,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                TabItem.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconResId),
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                tab.title,
                                fontFamily = NotoSansKR,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        onClick = {
                            if (currentTab == tab && tab == TabItem.DayPlan) {
                                dayPlanKey++ // 탭 다시 누르면 초기화
                            } else {
                                currentTab = tab
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(appBackgroundColor)
        ) {
            when (currentTab) {
                TabItem.DayPlan -> {
                    AnimatedContent(
                        targetState = dayPlanKey,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "DayPlanFade"
                    ) { targetKey ->
                        key(targetKey) {
                            DayPlanScreen(trip)
                        }
                    }
                }
                // [수정] TimeTableScreen 연결
                TabItem.TimeTable -> TimeTableScreen(trip)

                TabItem.Budget -> PlaceholderContent("BUDGET")
                TabItem.Checklist -> PlaceholderContent("CHECK LIST")
            }
        }
    }
}

@Composable
fun PlaceholderContent(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = Color.Black, fontFamily = NotoSansKR)
    }
}