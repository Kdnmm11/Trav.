package com.example.trav.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trav.data.AppDatabase
import com.example.trav.data.Schedule
import com.example.trav.ui.components.AddScheduleSheet
import com.example.trav.ui.components.DayInfoSheet
import com.example.trav.ui.components.EditScheduleSheet
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import com.example.trav.ui.viewmodel.ScheduleViewModel
import com.example.trav.ui.viewmodel.ScheduleViewModelFactory
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    tripId: Int,
    dayNumber: Int,
    tripTitle: String = "FUKUOKA",
    tripDate: String = "2024.12.01",
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)

    val tripDao = database.tripDao()
    val trip by tripDao.getTrip(tripId).collectAsState(initial = null)

    val viewModel: ScheduleViewModel = viewModel(
        key = "schedule_${tripId}_${dayNumber}",
        factory = ScheduleViewModelFactory(database.scheduleDao(), tripId, dayNumber)
    )

    val schedules by viewModel.schedules.collectAsState(initial = emptyList())
    val dayInfo by viewModel.dayInfo.collectAsState(initial = null)

    val appBackgroundColor = Color(0xFFFAFAFA)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appBackgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    var showScheduleSheet by remember { mutableStateOf(false) }
    var showDayInfoSheet by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fixedHeaderOffsetY = (-20).dp
    val fixedListOffsetY = (-26).dp

    val bottomCardHeight = 84.4.dp
    val bottomCardOffset = 18.1.dp
    val fabSize = 58.dp
    val fabIconSize = 25.dp
    val cityFontSize = 12.5f
    val stayFontSize = 12.5f
    val infoTextOffset = 0.dp

    // 1. 전체 기간 (Pre/Post 포함)
    val totalDays = remember(trip) {
        trip?.let {
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)
            val originalDuration = ChronoUnit.DAYS.between(start, end).toInt() + 1
            originalDuration + it.preDays + it.postDays
        } ?: 1
    }

    // [수정] 2. 순수 여행 기간 (Pre/Post 제외) 계산 - Picker 제한용
    val tripDuration = remember(trip) {
        trip?.let {
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)
            ChronoUnit.DAYS.between(start, end).toInt() + 1
        } ?: 1
    }

    Scaffold(
        containerColor = appBackgroundColor,
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 24.dp)
                    .offset(y = bottomCardOffset)
            ) {
                // 하단 정보 카드 (Day Info)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .height(bottomCardHeight)
                        .clickable { showDayInfoSheet = true },
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.Center) {
                            val cityText = dayInfo?.city?.ifBlank { null } ?: "City (Set Route)"
                            val stayText = dayInfo?.accommodation?.ifBlank { null } ?: "Stay (Accommodation)"

                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                text = cityText,
                                isPlaceholder = dayInfo?.city.isNullOrBlank(),
                                fontSize = cityFontSize,
                                textOffset = infoTextOffset.value
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            InfoRow(
                                icon = Icons.Default.Home,
                                text = stayText,
                                isPlaceholder = dayInfo?.accommodation.isNullOrBlank(),
                                fontSize = stayFontSize,
                                textOffset = infoTextOffset.value
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                FloatingActionButton(
                    onClick = { showScheduleSheet = true },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(fabSize),
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(fabIconSize)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(appBackgroundColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = fixedHeaderOffsetY),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day $dayNumber",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.Black
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = tripTitle.uppercase(),
                            fontFamily = NotoSansKR,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = tripDate,
                            fontFamily = NotoSansKR,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (schedules.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.offset(y = fixedListOffsetY)) {
                        EmptyStateView()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 150.dp, top = 20.dp),
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .offset(y = fixedListOffsetY)
                    ) {
                        itemsIndexed(schedules) { index, schedule ->
                            TimelineItem(
                                schedule = schedule,
                                isFirst = index == 0,
                                isLast = index == schedules.lastIndex,
                                onClick = { selectedSchedule = schedule }
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedSchedule != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSchedule = null },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            EditScheduleSheet(
                schedule = selectedSchedule!!,
                onUpdate = { time, endTime, title, location, memo, category, subCategory, amount, arrivalPlace, reservationNum, bookingSource ->
                    viewModel.updateSchedule(
                        selectedSchedule!!, time, endTime, title, location, memo,
                        category, subCategory, amount, arrivalPlace, reservationNum, bookingSource
                    )
                    selectedSchedule = null
                },
                onDelete = {
                    viewModel.deleteSchedule(selectedSchedule!!)
                    selectedSchedule = null
                },
                onCancel = { selectedSchedule = null }
            )
        }
    }

    if (showDayInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDayInfoSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            DayInfoSheet(
                initialCity = dayInfo?.city ?: "",
                initialStay = dayInfo?.accommodation ?: "",
                initialCheckInDay = dayInfo?.checkInDay ?: "",
                initialCheckInTime = dayInfo?.checkInTime ?: "",
                initialCheckOutDay = dayInfo?.checkOutDay ?: "",
                initialCheckOutTime = dayInfo?.checkOutTime ?: "",
                totalDays = totalDays,
                // [수정] tripDuration 전달
                tripDuration = tripDuration,
                onSave = { city, stay, inDay, inTime, outDay, outTime ->
                    viewModel.saveDayInfo(city, stay, inDay, inTime, outDay, outTime)
                    showDayInfoSheet = false
                },
                onCancel = { showDayInfoSheet = false }
            )
        }
    }

    if (showScheduleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScheduleSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            AddScheduleSheet(
                onConfirm = { time, endTime, title, location, memo, category, subCategory, amount, arrivalPlace, reservationNum, bookingSource ->
                    viewModel.addSchedule(
                        time, endTime, title, location, memo,
                        category, subCategory, amount, arrivalPlace, reservationNum, bookingSource
                    )
                    showScheduleSheet = false
                },
                onCancel = { showScheduleSheet = false }
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    text: String,
    isPlaceholder: Boolean,
    fontSize: Float = 11f,
    textOffset: Float = 0f
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPlaceholder) Color.Gray else Color.White,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontFamily = NotoSansKR,
            fontSize = fontSize.sp,
            color = if (isPlaceholder) Color.Gray else Color.White,
            fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Bold,
            modifier = Modifier.offset(y = textOffset.dp)
        )
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Empty Plan", fontFamily = PlayfairDisplay, fontSize = 24.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Create your memory for this day.", fontFamily = NotoSansKR, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun TimelineItem(
    schedule: Schedule,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val dotSize = 10.dp
    val dotTopPadding = 25.dp
    val dotCenterY = dotTopPadding + (dotSize / 2)

    Row(modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
        Box(
            modifier = Modifier.width(50.dp).padding(top = 18.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(text = schedule.time, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier.width(16.dp).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isFirst) {
                Box(modifier = Modifier.width(1.dp).height(dotCenterY).background(Color.LightGray).align(Alignment.TopCenter))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().padding(top = dotCenterY).background(Color.LightGray).align(Alignment.TopCenter))
            }
            Box(modifier = Modifier.padding(top = dotTopPadding).size(dotSize).clip(CircleShape).background(Color.Black).align(Alignment.TopCenter))
        }
        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEFEFEF)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = schedule.title, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, modifier = Modifier.fillMaxWidth())

                if (schedule.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = schedule.location, fontSize = 12.sp, color = Color.Gray, fontFamily = NotoSansKR)
                    }
                }
                if (schedule.memo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = schedule.memo, fontSize = 13.sp, color = Color(0xFF555555), fontFamily = NotoSansKR, lineHeight = 18.sp)
                }
            }
        }
    }
}