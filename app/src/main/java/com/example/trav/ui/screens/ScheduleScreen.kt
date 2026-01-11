package com.example.trav.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
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

const val ANIMATION_DURATION = 300

fun formatCityText(cityString: String?): String {
    if (cityString.isNullOrBlank()) return "City"
    val cities = cityString.split(",").map { it.trim() }.filter { it.isNotBlank() }
    if (cities.isEmpty()) return "City"
    val maxLength = 16
    val formattedCities = cities.map { city ->
        var currentLen = 0
        var result = ""
        var isTruncated = false
        for (char in city) {
            val charLen = if (char.code <= 128) 1 else 2
            if (currentLen + charLen > maxLength) {
                isTruncated = true
                break
            }
            result += char
            currentLen += charLen
        }
        if (isTruncated) "$result..." else city
    }
    return formattedCities.joinToString(" > ")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    val schedules by viewModel.schedules.collectAsState(initial = emptyList<Schedule>())
    val dayInfo by viewModel.dayInfo.collectAsState(initial = null)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val fixedTitleOffsetX = -3.9f
    val fixedTitleOffsetY = 12.2f
    val fixedTitleFontSize = 20.4f
    val fixedTitleAreaHeight = 30.3f
    val fixedChipFontSize = 9.0f
    val fixedLeftTimeLineHeight = 12.6f
    val fixedCardContentSpacing = 2.6f

    var showScheduleSheet by remember { mutableStateOf(false) }
    var showDayInfoSheet by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }

    val totalDays = remember(trip) {
        trip?.let {
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)
            ChronoUnit.DAYS.between(start, end).toInt() + 1 + it.preDays + it.postDays
        } ?: 1
    }
    val tripDuration = remember(trip) {
        trip?.let {
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)
            ChronoUnit.DAYS.between(start, end).toInt() + 1
        } ?: 1
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp).offset(y = 18.dp)) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black), modifier = Modifier.weight(1f).height(84.dp).clickable { showDayInfoSheet = true }, elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            InfoRow(Icons.Default.LocationOn, formatCityText(dayInfo?.city), dayInfo?.city.isNullOrBlank())
                            Spacer(modifier = Modifier.height(4.dp))
                            InfoRow(Icons.Default.Home, dayInfo?.accommodation ?: "Stay", dayInfo?.accommodation.isNullOrBlank())
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(onClick = { showScheduleSheet = true }, containerColor = Color.Black, contentColor = Color.White, shape = CircleShape) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Day $dayNumber", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(tripTitle.uppercase(), fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(tripDate, fontFamily = NotoSansKR, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                if (schedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EmptyStateView() }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 150.dp, top = 10.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                        itemsIndexed(schedules, key = { _, item -> item.id }) { index, schedule ->
                            // Custom Layout 사용
                            TimelineLayout(
                                schedule = schedule,
                                isFirst = index == 0,
                                isLast = index == schedules.lastIndex,
                                leftTimeH = fixedLeftTimeLineHeight,
                                onLongClick = { selectedSchedule = schedule },
                                content = {
                                    TimelineCardContent(
                                        schedule = schedule,
                                        titleX = fixedTitleOffsetX, titleY = fixedTitleOffsetY,
                                        titleSize = fixedTitleFontSize, titleH = fixedTitleAreaHeight,
                                        chipSize = fixedChipFontSize, cardSpacing = fixedCardContentSpacing
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val sheetProps = remember { ModalBottomSheetProperties(shouldDismissOnBackPress = false) }
    if (showScheduleSheet) {
        ModalBottomSheet(onDismissRequest = { showScheduleSheet = false }, sheetState = sheetState, containerColor = Color.Transparent, dragHandle = null, properties = sheetProps) {
            AddScheduleSheet(tripDuration, trip?.startDate ?: "", dayNumber, { t1, t2, tt, loc, mm, cat, sub, amt, arr, res, src ->
                viewModel.addSchedule(t1, t2, tt, loc, mm, cat, sub, amt, arr, res, src); showScheduleSheet = false
            }, { showScheduleSheet = false })
        }
    }
    if (selectedSchedule != null) {
        ModalBottomSheet(onDismissRequest = { selectedSchedule = null }, sheetState = sheetState, containerColor = Color.Transparent, dragHandle = null, properties = sheetProps) {
            EditScheduleSheet(selectedSchedule!!, tripDuration, trip?.startDate ?: "", { t1, t2, tt, loc, mm, cat, sub, amt, arr, res, src ->
                viewModel.updateSchedule(selectedSchedule!!, t1, t2, tt, loc, mm, cat, sub, amt, arr, res, src); selectedSchedule = null
            }, { viewModel.deleteSchedule(selectedSchedule!!); selectedSchedule = null }, { selectedSchedule = null })
        }
    }
    if (showDayInfoSheet) {
        ModalBottomSheet(onDismissRequest = { showDayInfoSheet = false }, sheetState = sheetState, containerColor = Color.Transparent, dragHandle = null, properties = sheetProps) {
            DayInfoSheet(dayInfo?.city ?: "", dayInfo?.accommodation ?: "", dayInfo?.checkInDay ?: "", dayInfo?.checkInTime ?: "", dayInfo?.checkOutDay ?: "", dayInfo?.checkOutTime ?: "", totalDays, tripDuration, trip?.startDate ?: "", { c, s, d1, t1, d2, t2 ->
                viewModel.saveDayInfo(c, s, d1, t1, d2, t2); showDayInfoSheet = false
            }, { showDayInfoSheet = false })
        }
    }
}

// [Custom Layout] 오른쪽(카드) 높이 == 왼쪽(선) 높이 강제 동기화
@Composable
fun TimelineLayout(
    schedule: Schedule,
    isFirst: Boolean,
    isLast: Boolean,
    leftTimeH: Float,
    onLongClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Layout(
        content = {
            // [1] 왼쪽 타임라인
            TimelineLeftSidebar(
                schedule = schedule,
                isFirst = isFirst,
                isLast = isLast,
                leftTimeH = leftTimeH
            )
            // [2] 오른쪽 카드
            Surface(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .combinedClickable(onClick = { isExpanded = !isExpanded }, onLongClick = onLongClick),
                // [중요] animateContentSize 제거!
                // 내부의 AnimatedVisibility가 높이 변화를 주도하면 Surface 크기도 자동으로 변하고,
                // Custom Layout이 그 크기를 감지해 선 길이도 줄여줍니다.
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                CompositionLocalProvider(LocalIsExpanded provides isExpanded) {
                    content()
                }
            }
        }
    ) { measurables, constraints ->
        val leftWidthPx = 80.dp.roundToPx()

        // 1. 카드 측정
        val cardConstraints = constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth - leftWidthPx)
        val cardPlaceable = measurables[1].measure(cardConstraints)
        val cardHeight = cardPlaceable.height

        // 2. 타임라인 측정 (높이를 카드 높이로 고정)
        val leftConstraints = Constraints.fixed(width = leftWidthPx, height = cardHeight)
        val leftPlaceable = measurables[0].measure(leftConstraints)

        // 3. 배치
        layout(width = constraints.maxWidth, height = cardHeight) {
            leftPlaceable.place(0, 0)
            cardPlaceable.place(leftWidthPx, 0)
        }
    }
}

val LocalIsExpanded = compositionLocalOf { false }

@Composable
fun TimelineCardContent(
    schedule: Schedule,
    titleX: Float, titleY: Float, titleSize: Float, titleH: Float,
    chipSize: Float, cardSpacing: Float
) {
    val isExpanded = LocalIsExpanded.current
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, animationSpec = tween(ANIMATION_DURATION))

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).padding(start = 18.dp, end = 12.dp)) {
        // 헤더
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).height(titleH.dp), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = schedule.title,
                    fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = titleSize.sp, color = Color.Black,
                    style = TextStyle(lineHeight = titleSize.sp, platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier.offset(x = titleX.dp, y = titleY.dp)
                )
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.rotate(rotationState).size(22.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 칩
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color.Black, shape = RoundedCornerShape((chipSize * 1.5f).dp), modifier = Modifier.height((chipSize * 2.2f).dp)) {
                Box(modifier = Modifier.padding(horizontal = (chipSize * 1.0f).dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(text = schedule.category, fontFamily = NotoSansKR, fontSize = chipSize.sp, color = Color.White, fontWeight = FontWeight.Bold, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                }
            }
            if (schedule.subCategory.isNotBlank() && schedule.subCategory != "교통") {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape((chipSize * 1.5f).dp), modifier = Modifier.height((chipSize * 2.2f).dp)) {
                    Box(modifier = Modifier.padding(horizontal = (chipSize * 1.0f).dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(text = schedule.subCategory, fontFamily = NotoSansKR, fontSize = chipSize.sp, color = Color.Black, fontWeight = FontWeight.Medium, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
                    }
                }
            }
        }

        // [수정 완료] if문 대신 AnimatedVisibility 사용
        // shrinkVertically(Alignment.Top): 아래쪽이 위로 딸려 올라가면서 줄어듦 (Slide Up)
        // fadeOut: 서서히 사라짐
        // 이 애니메이션이 진행되는 동안 카드의 높이가 변하고 -> Custom Layout이 감지 -> 선 길이도 줄어듦 (완벽 연동)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(tween(ANIMATION_DURATION), expandFrom = Alignment.Top) + fadeIn(tween(ANIMATION_DURATION)),
            exit = shrinkVertically(tween(ANIMATION_DURATION), shrinkTowards = Alignment.Top) + fadeOut(tween(ANIMATION_DURATION))
        ) {
            Column(modifier = Modifier.padding(top = cardSpacing.dp)) {
                if (schedule.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = schedule.location.replace("->", " > "), fontSize = 13.sp, color = Color.DarkGray, fontFamily = NotoSansKR)
                    }
                    Spacer(modifier = Modifier.height(cardSpacing.dp))
                }
                if (schedule.memo.isNotBlank()) {
                    Text(text = schedule.memo, fontSize = 13.sp, color = Color(0xFF444444), fontFamily = NotoSansKR, lineHeight = 20.sp, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// 왼쪽 타임라인 (Canvas로 그리기) - 높이는 부모 Layout에서 결정됨
@Composable
fun TimelineLeftSidebar(
    schedule: Schedule,
    isFirst: Boolean,
    isLast: Boolean,
    leftTimeH: Float
) {
    val displayEndTime = remember(schedule) { if (schedule.endTime.isNotBlank()) schedule.endTime.split(" ").first() else "" }
    val hasEndTime = displayEndTime.isNotBlank()
    val timeText = if (hasEndTime) "${schedule.time}\n-\n$displayEndTime" else schedule.time

    val textMeasurer = rememberTextMeasurer()
    val timeTextStyle = TextStyle(
        fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black, textAlign = TextAlign.Center,
        lineHeight = leftTimeH.sp, platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val dotX = 54.dp.toPx() + 8.dp.toPx()
        val dotY = 40.2.dp.toPx()

        // 텍스트
        val measuredText = textMeasurer.measure(timeText, timeTextStyle)
        drawText(measuredText, topLeft = Offset((54.dp.toPx() - measuredText.size.width) / 2, 31.1.dp.toPx()))

        // 선
        val lineStrokeWidth = 1.dp.toPx()
        val lineColor = Color(0xFFE0E0E0)

        if (!isLast) {
            drawLine(lineColor, start = Offset(dotX, dotY), end = Offset(dotX, size.height), strokeWidth = lineStrokeWidth)
        }
        if (!isFirst) {
            drawLine(lineColor, start = Offset(dotX, 0f), end = Offset(dotX, dotY), strokeWidth = lineStrokeWidth)
        }

        // 점
        drawCircle(Color.Black, radius = 4.5.dp.toPx(), center = Offset(dotX, dotY))
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String, isPlaceholder: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (isPlaceholder) Color.Gray else Color.White, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontFamily = NotoSansKR, fontSize = 12.sp, color = if (isPlaceholder) Color.Gray else Color.White, fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Bold)
    }
}

@Composable
fun EmptyStateView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Empty Plan", fontFamily = PlayfairDisplay, fontSize = 24.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
        Text("Create your memory for this day.", fontFamily = NotoSansKR, fontSize = 14.sp, color = Color.Gray)
    }
}