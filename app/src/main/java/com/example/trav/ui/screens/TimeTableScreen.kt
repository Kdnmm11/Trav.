package com.example.trav.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import com.example.trav.data.AppDatabase
import com.example.trav.data.DayInfo
import com.example.trav.data.Schedule
import com.example.trav.data.Trip
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.min

// [설정] 애니메이션 속도 조절
const val POPUP_ANIM_DURATION = 150
const val SCREEN_ENTER_DURATION = 200

fun formatCityTextTable(cityString: String?): String {
    if (cityString.isNullOrBlank()) return "-"
    val cities = cityString.split(",").map { it.trim() }.filter { it.isNotBlank() }
    if (cities.isEmpty()) return "-"
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeTableScreen(trip: Trip) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val scheduleDao = database.scheduleDao()

    val schedulesState = scheduleDao.getAllSchedules(trip.id).collectAsState(initial = null)
    val dayInfosState = scheduleDao.getAllDayInfos(trip.id).collectAsState(initial = null)

    val rawSchedules = schedulesState.value
    val rawDayInfos = dayInfosState.value
    val appBackgroundColor = Color(0xFFFAFAFA)

    // 로딩 대기: 데이터가 없으면 빈 화면 유지
    if (rawSchedules == null || rawDayInfos == null) {
        Box(modifier = Modifier.fillMaxSize().background(appBackgroundColor))
        return
    }

    val allSchedules = rawSchedules
    val allDayInfos = rawDayInfos

    val originalStart = LocalDate.parse(trip.startDate)
    val originalEnd = LocalDate.parse(trip.endDate)
    val originalDuration = ChronoUnit.DAYS.between(originalStart, originalEnd).toInt() + 1

    val totalStartDayNum = 1 - trip.preDays
    val totalEndDayNum = originalDuration + trip.postDays
    val dayRange = (totalStartDayNum..totalEndDayNum).toList()

    val minDay = 1 - trip.preDays
    val currentStartDay = if (trip.startViewDay < minDay) minDay else trip.startViewDay

    val futureDays = dayRange.filter { it >= currentStartDay }
    val pastDays = dayRange.filter { it < currentStartDay }
    val reorderedDays = futureDays + pastDays

    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }

    var lastDismissTime by remember { mutableLongStateOf(0L) }

    val screenAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAlpha.animateTo(1f, animationSpec = tween(SCREEN_ENTER_DURATION))
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val dateColWidth = 121.0.dp
    val infoHeaderHeight = 40.0.dp
    val headerTotalHeight = 42.6.dp + infoHeaderHeight
    val cityStayFontSize = 11.0.sp
    val cityStayIconSize = 11.0.dp
    val hourHeight = 40.dp
    val timeLabelWidth = 22.dp

    val (startHour, endHour) = remember(allSchedules) {
        val defaultStart = 8
        val defaultEnd = 24
        if (allSchedules.isEmpty()) {
            defaultStart to defaultEnd
        } else {
            val adjustedHours = allSchedules.map {
                val h = parseTime(it.time).first
                if (h < 5) h + 24 else h
            }
            val minScheduleHour = adjustedHours.minOrNull() ?: defaultStart
            val maxScheduleHour = allSchedules.maxOfOrNull {
                val startH = parseTime(it.time).first
                val adjStartH = if (startH < 5) startH + 24 else startH
                if (it.endTime.isNotBlank()) {
                    val endH = parseTime(it.endTime).first
                    val adjEndH = if (endH < 5) endH + 24 else endH
                    if (adjEndH < adjStartH) adjEndH + 24 else adjEndH
                } else { adjStartH }
            }?.toInt() ?: (defaultEnd - 1)
            val finalStart = min(defaultStart, minScheduleHour)
            val finalEnd = kotlin.math.max(defaultEnd, maxScheduleHour + 1)
            finalStart to finalEnd
        }
    }

    val verticalScrollState = rememberScrollState()
    val horizontalLazyState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = horizontalLazyState)

    val timeColumnColor = Color(0xFFF7F7F7)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appBackgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().alpha(screenAlpha.value)) {
        Column(modifier = Modifier.fillMaxSize().background(appBackgroundColor)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.7.dp, bottom = 10.dp, start = 18.6.dp, end = 18.6.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Time Table", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 27.4.sp, color = Color.Black)
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = trip.title.uppercase(), fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text(text = "${trip.startDate.replace("-", ".")} — ${trip.endDate.replace("-", ".")}", fontFamily = NotoSansKR, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Row(modifier = Modifier.fillMaxSize().verticalScroll(verticalScrollState)) {
                Column(modifier = Modifier.width(timeLabelWidth).background(timeColumnColor)) {
                    Box(modifier = Modifier.width(timeLabelWidth).height(headerTotalHeight).background(timeColumnColor), contentAlignment = Alignment.Center) {
                        if (horizontalLazyState.canScrollBackward) {
                            Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.Gray, modifier = Modifier.size(24.dp).clickable { coroutineScope.launch { horizontalLazyState.animateScrollToItem(0) } })
                        }
                    }
                    for (hour in startHour..endHour) {
                        Box(modifier = Modifier.height(hourHeight).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            val displayHour = if (hour >= 24) hour - 24 else hour
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 2.dp)) {
                                Text(text = String.format("%02d", displayHour), fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR, lineHeight = 10.sp)
                                Text(text = "00", fontSize = 8.sp, color = Color.Gray, fontFamily = NotoSansKR, lineHeight = 8.sp)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    LazyRow(state = horizontalLazyState, flingBehavior = snapBehavior, modifier = Modifier.fillMaxWidth()) {
                        items(reorderedDays) { dayNum ->
                            val isPast = dayNum < currentStartDay
                            val date = originalStart.plusDays((dayNum - 1).toLong())
                            val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH).uppercase()
                            val dayStr = date.format(DateTimeFormatter.ofPattern("MM.dd"))
                            val dayInfo = allDayInfos.find { it.dayNumber == dayNum }

                            val cityText = formatCityTextTable(dayInfo?.city)
                            val stayText = dayInfo?.accommodation?.takeIf { it.isNotBlank() } ?: "-"

                            val dayLabel = when {
                                dayNum < 1 -> "Before ${1 - dayNum}"
                                dayNum > originalDuration -> "After ${dayNum - originalDuration}"
                                else -> "Day $dayNum"
                            }

                            // [정렬] 시간순 정렬 (색상 토글 로직을 위해 필수)
                            val daySchedules = allSchedules.filter { it.dayNumber == dayNum }
                                .sortedWith(compareBy {
                                    val (h, m) = parseTime(it.time)
                                    val adjH = if (h < 5) h + 24 else h
                                    adjH * 60 + m
                                })

                            Column(modifier = Modifier.width(dateColWidth).alpha(if (isPast) 0.3f else 1f)) {
                                Column(modifier = Modifier.fillMaxWidth().height(42.6.dp).background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(text = dayLabel, fontSize = 12.5.sp, color = Color.LightGray, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR, modifier = Modifier.offset(y = (-0.8).dp))
                                    Text(text = "$dayStr $dayName", fontSize = 12.5.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR, modifier = Modifier.offset(y = (-6.8).dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(infoHeaderHeight)
                                        .background(Color(0xFFF2F2F2))
                                        .pointerInput(dayNum, selectedDayNum) {
                                            detectTapGestures {
                                                val now = System.currentTimeMillis()
                                                if (now - lastDismissTime > 500) {
                                                    if (selectedDayNum == dayNum) {
                                                        selectedDayNum = null
                                                    } else {
                                                        selectedDayNum = dayNum
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.Center) {
                                            Icon(Icons.Default.LocationOn, null, tint = Color.Black, modifier = Modifier.size(cityStayIconSize).offset(y = 0.7.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = cityText, fontSize = cityStayFontSize, color = Color.Black, fontFamily = NotoSansKR, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), modifier = Modifier.offset(y = (-0.4).dp))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.Center) {
                                            Icon(Icons.Default.Home, null, tint = Color.Gray, modifier = Modifier.size(cityStayIconSize).offset(y = (-0.1).dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = stayText, fontSize = cityStayFontSize, color = Color.Gray, fontFamily = NotoSansKR, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), modifier = Modifier.offset(y = (-1.2).dp))
                                        }
                                    }

                                    val isSelected = (selectedDayNum == dayNum)
                                    val transition = updateTransition(targetState = isSelected, label = "DayInfoPopup")

                                    if (transition.currentState || transition.targetState) {
                                        Popup(
                                            alignment = Alignment.TopCenter,
                                            offset = IntOffset(0, with(density) { (infoHeaderHeight - 3.0.dp).roundToPx() }),
                                            onDismissRequest = {
                                                lastDismissTime = System.currentTimeMillis()
                                                selectedDayNum = null
                                            },
                                            properties = PopupProperties(dismissOnClickOutside = true)
                                        ) {
                                            transition.AnimatedVisibility(
                                                visible = { it },
                                                enter = fadeIn(tween(POPUP_ANIM_DURATION)) + scaleIn(initialScale = 0.95f, animationSpec = tween(POPUP_ANIM_DURATION)),
                                                exit = fadeOut(tween(POPUP_ANIM_DURATION)) + scaleOut(targetScale = 0.95f, animationSpec = tween(POPUP_ANIM_DURATION))
                                            ) {
                                                DayInfoPopupContent(dayInfo = dayInfo)
                                            }
                                        }
                                    }
                                }

                                Box(modifier = Modifier.width(dateColWidth).height(hourHeight * (endHour - startHour + 1)).border(BorderStroke(0.5.dp, Color(0xFFEEEEEE)))) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        for (i in 0..(endHour - startHour)) {
                                            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp, modifier = Modifier.offset(y = hourHeight * i))
                                        }
                                    }

                                    // [로직] 색상 교차 상태 변수
                                    var lastEndMins = -1
                                    var isGrayMode = false

                                    daySchedules.forEach { schedule ->
                                        val (startH, startM) = parseTime(schedule.time)
                                        val adjStartH = if (startH < 5) startH + 24 else startH
                                        val currentStartMins = adjStartH * 60 + startM

                                        // 연속된 일정(딱 맞물림)이면 색상 모드 토글
                                        if (lastEndMins != -1 && currentStartMins == lastEndMins) {
                                            isGrayMode = !isGrayMode
                                        } else {
                                            isGrayMode = false // 갭이 있으면 기본 검은색 리셋
                                        }

                                        val durationMins = if (schedule.endTime.isNotBlank()) {
                                            val (endH, endM) = parseTime(schedule.endTime.split(" ").first())
                                            val adjEndH = if (endH < 5) endH + 24 else endH
                                            val totalStart = adjStartH * 60 + startM
                                            var totalEnd = adjEndH * 60 + endM
                                            if (totalEnd <= totalStart) totalEnd = totalStart + 60
                                            (totalEnd - totalStart).toFloat()
                                        } else { 60f }

                                        // 다음 비교를 위해 끝나는 시간 저장
                                        lastEndMins = (currentStartMins + durationMins.toInt())

                                        if (adjStartH >= startHour) {
                                            val topOffset = (adjStartH - startHour) * hourHeight.value + (startM.toFloat() / 60f) * hourHeight.value
                                            val blockHeight = (durationMins / 60f) * hourHeight.value

                                            // [로직] 높이에서 1dp 빼기 (시각적 분리)
                                            val displayHeight = if (blockHeight > 1.0f) (blockHeight - 2).dp else blockHeight.dp

                                            // [로직] 색상 결정
                                            val containerColor = if (isGrayMode) Color(0xFFE0E0E0) else Color.Black.copy(alpha = 0.85f)
                                            val contentColor = if (isGrayMode) Color.Black else Color.White

                                            Box(modifier = Modifier.padding(horizontal = 2.dp).offset(y = topOffset.dp).width(dateColWidth - 4.dp).height(displayHeight)) {
                                                ScheduleBlock(
                                                    schedule = schedule,
                                                    heightDp = displayHeight,
                                                    containerColor = containerColor,
                                                    contentColor = contentColor,
                                                    modifier = Modifier.fillMaxSize().clickable { selectedSchedule = schedule }
                                                )

                                                val isScheduleSelected = (selectedSchedule == schedule)
                                                val scheduleTransition = updateTransition(targetState = isScheduleSelected, label = "SchedulePopup")

                                                if (scheduleTransition.currentState || scheduleTransition.targetState) {
                                                    val isUpperHalf = (topOffset + blockHeight / 2) < (hourHeight.value * (endHour - startHour) / 2)

                                                    Popup(
                                                        alignment = if (isUpperHalf) Alignment.TopStart else Alignment.BottomStart,
                                                        offset = IntOffset(
                                                            x = 0,
                                                            y = with(density) {
                                                                if (isUpperHalf) {
                                                                    displayHeight.roundToPx()
                                                                } else {
                                                                    -displayHeight.roundToPx()
                                                                }
                                                            }
                                                        ),
                                                        onDismissRequest = { selectedSchedule = null },
                                                        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
                                                    ) {
                                                        scheduleTransition.AnimatedVisibility(
                                                            visible = { it },
                                                            enter = fadeIn(tween(POPUP_ANIM_DURATION)) + scaleIn(initialScale = 0.95f, animationSpec = tween(POPUP_ANIM_DURATION)),
                                                            exit = fadeOut(tween(POPUP_ANIM_DURATION)) + scaleOut(targetScale = 0.95f, animationSpec = tween(POPUP_ANIM_DURATION))
                                                        ) {
                                                            SchedulePopupContent(selectedSchedule = schedule)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (horizontalLazyState.canScrollForward) {
                        val arrowTopPadding = (headerTotalHeight - 24.dp) / 2
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.align(Alignment.TopEnd).padding(top = arrowTopPadding).size(24.dp).clickable { coroutineScope.launch {
                            if (reorderedDays.isNotEmpty()) {
                                val targetIndex = if (futureDays.isNotEmpty()) futureDays.lastIndex else reorderedDays.lastIndex
                                val viewportWidth = horizontalLazyState.layoutInfo.viewportSize.width
                                val itemWidthPx = with(density) { dateColWidth.roundToPx() }
                                val targetEndPx = (targetIndex + 1) * itemWidthPx
                                val startPx = targetEndPx - viewportWidth
                                if (startPx <= 0) { horizontalLazyState.animateScrollToItem(0) } else {
                                    val anchorIndex = startPx / itemWidthPx
                                    val offset = startPx % itemWidthPx
                                    horizontalLazyState.animateScrollToItem(anchorIndex, scrollOffset = offset)
                                }
                            }
                        } })
                    }
                }
            }
        }
    }
}

fun parseTime(timeStr: String): Pair<Int, Int> {
    return try {
        val parts = timeStr.trim().split(":")
        parts[0].toInt() to parts[1].toInt()
    } catch (e: Exception) { 0 to 0 }
}

@Composable
fun ScheduleBlock(
    schedule: Schedule,
    heightDp: androidx.compose.ui.unit.Dp,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val displayEnd = if(schedule.endTime.isNotBlank()) schedule.endTime.split(" ").first() else ""
            val timeText = if (displayEnd.isNotBlank()) "${schedule.time}-$displayEnd" else schedule.time
            Text(text = timeText, fontSize = 9.sp, color = contentColor, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
            if (heightDp > 25.dp) {
                Text(text = schedule.title, fontSize = 10.sp, color = contentColor, fontWeight = FontWeight.Normal, fontFamily = NotoSansKR, maxLines = 1, overflow = TextOverflow.Ellipsis, lineHeight = 12.sp, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)))
            }
        }
    }
}

@Composable
private fun DayInfoPopupContent(dayInfo: DayInfo?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.widthIn(max = 200.dp).padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(dayInfo?.city?.ifBlank { "-" } ?: "-", color = Color.White, fontSize = 11.sp, fontFamily = NotoSansKR)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(dayInfo?.accommodation?.ifBlank { "-" } ?: "-", color = Color.White, fontSize = 11.sp, fontFamily = NotoSansKR)
            }
        }
    }
}

@Composable
private fun SchedulePopupContent(selectedSchedule: Schedule) {
    Card(
        modifier = Modifier.width(230.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)) {
            Text(text = selectedSchedule.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = NotoSansKR)
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFEEEEEE))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                val timeDisp = if(selectedSchedule.endTime.isNotBlank()) selectedSchedule.endTime.split(" ").first() else ""
                val timeText = if(timeDisp.isNotBlank()) "${selectedSchedule.time} - $timeDisp" else selectedSchedule.time
                Text(text = timeText, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR)

                Row(horizontalArrangement = Arrangement.End) {
                    Surface(color = Color.Black, shape = RoundedCornerShape(4.dp)) {
                        Text(text = selectedSchedule.category, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                    }
                    if (selectedSchedule.subCategory.isNotBlank() && selectedSchedule.subCategory != "교통") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp)) {
                            Text(text = selectedSchedule.subCategory, fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            if (selectedSchedule.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = selectedSchedule.location.replace("->", " > "), fontSize = 12.sp, color = Color.DarkGray, fontFamily = NotoSansKR)
                }
            }
            if (selectedSchedule.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "- ${selectedSchedule.memo}", fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp, fontFamily = NotoSansKR)
            }
        }
    }
}