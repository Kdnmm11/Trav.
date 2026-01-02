package com.example.trav.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
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
import androidx.core.view.WindowCompat
import com.example.trav.data.AppDatabase
import com.example.trav.data.Schedule
import com.example.trav.data.Trip
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun TimeTableScreen(trip: Trip) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val scheduleDao = database.scheduleDao()

    val allSchedules by scheduleDao.getAllSchedules(trip.id).collectAsState(initial = emptyList())
    val allDayInfos by scheduleDao.getAllDayInfos(trip.id).collectAsState(initial = emptyList())

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

    // [디자인 상수]
    val headerTopPadding = 5.7.dp
    val headerSidePadding = 18.6.dp
    val headerTitleSize = 27.4.sp
    val headerTripInfoSize = 14.sp
    val headerDateSize = 12.sp

    val dateColWidth = 121.0.dp
    val dateHeaderHeight = 42.6.dp
    val infoHeaderHeight = 40.0.dp

    val dateLabelSize = 12.5.sp
    val dateTextSize = 12.5.sp
    val cityStayFontSize = 11.0.sp
    val cityStayIconSize = 11.0.dp

    val dateLabelOffset = (-0.8).dp
    val dateTextOffset = (-6.8).dp

    val cityIconOffset = 0.7.dp
    val cityTextOffset = (-0.4).dp
    val stayIconOffset = (-0.1).dp
    val stayTextOffset = (-1.2).dp

    val hourHeight = 40.dp
    val timeLabelWidth = 22.dp

    // [시간 범위 계산]
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
            // 종료 시간도 고려해야 함 (일정이 길어질 수 있으므로)
            val maxScheduleHour = allSchedules.maxOfOrNull {
                val startH = parseTime(it.time).first
                val adjStartH = if (startH < 5) startH + 24 else startH

                // 종료 시간이 있으면 그 시간까지 포함
                if (it.endTime.isNotBlank()) {
                    val endH = parseTime(it.endTime).first
                    val adjEndH = if (endH < 5) endH + 24 else endH
                    // 만약 종료시간이 시작시간보다 작으면(다음날 넘어감 등) 보정
                    if (adjEndH < adjStartH) adjEndH + 24 else adjEndH
                } else {
                    adjStartH // 종료 시간 없으면 시작 시간 기준
                }
            }?.toInt() ?: (defaultEnd - 1)

            val finalStart = min(defaultStart, minScheduleHour)
            val finalEnd = max(defaultEnd, maxScheduleHour + 1)

            finalStart to finalEnd
        }
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val appBackgroundColor = Color(0xFFFAFAFA)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appBackgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundColor)
        ) {
            // [1. 메인 헤더]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = headerTopPadding,
                        bottom = 10.dp,
                        start = headerSidePadding,
                        end = headerSidePadding
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Table",
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = headerTitleSize,
                    color = Color.Black
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trip.title.uppercase(),
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Bold,
                        fontSize = headerTripInfoSize,
                        color = Color.Black
                    )
                    Text(
                        text = "${trip.startDate.replace("-", ".")} — ${trip.endDate.replace("-", ".")}",
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Normal,
                        fontSize = headerDateSize,
                        color = Color.Gray
                    )
                }
            }

            // [2. 날짜 헤더]
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.width(timeLabelWidth)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dateHeaderHeight)
                            .background(Color(0xFFF2F2F2))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(infoHeaderHeight)
                            .background(Color.Black)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    reorderedDays.forEach { dayNum ->
                        val isPast = dayNum < currentStartDay
                        val date = originalStart.plusDays((dayNum - 1).toLong())
                        val dayName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH).uppercase()
                        val dayStr = date.format(DateTimeFormatter.ofPattern("MM.dd"))

                        val dayInfo = allDayInfos.find { it.dayNumber == dayNum }
                        val cityText = dayInfo?.city?.takeIf { it.isNotBlank() } ?: "-"
                        val stayText = dayInfo?.accommodation?.takeIf { it.isNotBlank() } ?: "-"

                        val dayLabel = when {
                            dayNum < 1 -> "Before ${1 - dayNum}"
                            dayNum > originalDuration -> "After ${dayNum - originalDuration}"
                            else -> "Day $dayNum"
                        }

                        // 전체 날짜 컬럼 박스
                        Column(
                            modifier = Modifier
                                .width(dateColWidth)
                                .alpha(if (isPast) 0.3f else 1f)
                        ) {
                            // [상단] 검은색 배경
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dateHeaderHeight)
                                    .background(Color.Black),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = dateLabelSize,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NotoSansKR,
                                    modifier = Modifier.offset(y = dateLabelOffset)
                                )
                                Text(
                                    text = "$dayStr $dayName",
                                    fontSize = dateTextSize,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NotoSansKR,
                                    modifier = Modifier.offset(y = dateTextOffset)
                                )
                            }

                            // [하단] 회색 배경 (도시/숙소 정보)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(infoHeaderHeight)
                                    .background(Color(0xFFF2F2F2)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // City Info Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier
                                            .size(cityStayIconSize)
                                            .offset(y = cityIconOffset)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cityText,
                                        fontSize = cityStayFontSize,
                                        color = Color.Black,
                                        fontFamily = NotoSansKR,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        modifier = Modifier.offset(y = cityTextOffset)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Stay Info Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(cityStayIconSize)
                                            .offset(y = stayIconOffset)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stayText,
                                        fontSize = cityStayFontSize,
                                        color = Color.Gray,
                                        fontFamily = NotoSansKR,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                                        modifier = Modifier.offset(y = stayTextOffset)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // [3. 메인 그리드]
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
            ) {
                // Y축 (시간)
                Column(
                    modifier = Modifier
                        .width(timeLabelWidth)
                        .background(Color(0xFFF7F7F7))
                ) {
                    for (hour in startHour..endHour) {
                        Box(
                            modifier = Modifier
                                .height(hourHeight)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            val displayHour = if (hour >= 24) hour - 24 else hour

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = String.format("%02d", displayHour),
                                    fontSize = 10.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NotoSansKR,
                                    lineHeight = 10.sp
                                )
                                Text(
                                    text = "00",
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontFamily = NotoSansKR,
                                    lineHeight = 8.sp
                                )
                            }
                        }
                    }
                }

                // 스케줄 영역
                Row(
                    modifier = Modifier.horizontalScroll(horizontalScrollState)
                ) {
                    reorderedDays.forEach { dayNum ->
                        val isPast = dayNum < currentStartDay
                        val daySchedules = allSchedules.filter { it.dayNumber == dayNum }

                        Box(
                            modifier = Modifier
                                .width(dateColWidth)
                                .height(hourHeight * (endHour - startHour + 1))
                                .alpha(if (isPast) 0.3f else 1f)
                                .border(BorderStroke(0.5.dp, Color(0xFFEEEEEE)))
                        ) {
                            Column {
                                for (i in 0..(endHour - startHour)) {
                                    Divider(
                                        color = Color(0xFFF5F5F5),
                                        thickness = 1.dp,
                                        modifier = Modifier.offset(y = hourHeight * i)
                                    )
                                }
                            }

                            daySchedules.forEach { schedule ->
                                val (startH, startM) = parseTime(schedule.time)
                                val adjStartH = if (startH < 5) startH + 24 else startH

                                // [높이 계산 로직]
                                // 1. 종료 시간이 있으면 차이를 계산
                                // 2. 없으면 기본 1시간(60분)
                                val durationMins = if (schedule.endTime.isNotBlank()) {
                                    val (endH, endM) = parseTime(schedule.endTime)
                                    val adjEndH = if (endH < 5) endH + 24 else endH

                                    // 종료 시간이 시작 시간보다 같거나 빠르면 (오류 혹은 날짜 넘어감) -> 최소 30분 보장
                                    val totalStart = adjStartH * 60 + startM
                                    var totalEnd = adjEndH * 60 + endM

                                    if (totalEnd <= totalStart) {
                                        totalEnd = totalStart + 60 // 강제 1시간
                                    }
                                    totalEnd - totalStart
                                } else {
                                    60 // 기본 1시간
                                }

                                if (adjStartH >= startHour) {
                                    val topOffset = (adjStartH - startHour) * hourHeight.value + (startM / 60f) * hourHeight.value
                                    // 분 단위 -> dp 높이 변환
                                    val blockHeight = (durationMins / 60f) * hourHeight.value

                                    ScheduleBlock(
                                        schedule = schedule,
                                        heightDp = blockHeight.dp, // 계산된 높이 전달
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .offset(y = topOffset.dp)
                                            .width((dateColWidth.value - 4).dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseTime(timeStr: String): Pair<Int, Int> {
    return try {
        val parts = timeStr.split(":")
        parts[0].toInt() to parts[1].toInt()
    } catch (e: Exception) {
        0 to 0
    }
}

@Composable
fun ScheduleBlock(
    schedule: Schedule,
    heightDp: androidx.compose.ui.unit.Dp, // 높이 파라미터 추가
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(heightDp), // 계산된 높이 적용
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // 시간 표시 (종료 시간 있으면 ~ 표시)
            val timeText = if (schedule.endTime.isNotBlank()) {
                "${schedule.time} - ${schedule.endTime}"
            } else {
                schedule.time
            }

            Text(
                text = timeText,
                fontSize = 9.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = NotoSansKR,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )

            // 높이가 너무 작으면 제목 숨기기 (선택사항)
            if (heightDp > 25.dp) {
                Text(
                    text = schedule.title,
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontFamily = NotoSansKR,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }
    }
}