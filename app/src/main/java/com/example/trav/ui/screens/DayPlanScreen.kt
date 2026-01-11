package com.example.trav.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trav.data.AppDatabase
import com.example.trav.data.DayInfo
import com.example.trav.data.Schedule
import com.example.trav.data.Trip
import com.example.trav.ui.components.TripEditSheet
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import com.example.trav.viewmodel.TripViewModel
import com.example.trav.viewmodel.TripViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPlanScreen(trip: Trip?) {
    if (trip == null) return

    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val scheduleDao = database.scheduleDao()
    val tripDao = database.tripDao()

    val allSchedules by scheduleDao.getAllSchedules(trip.id).collectAsState(initial = emptyList())
    val allDayInfos by scheduleDao.getAllDayInfos(trip.id).collectAsState(initial = emptyList())
    val tripViewModel: TripViewModel = viewModel(factory = TripViewModelFactory(tripDao, scheduleDao))

    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    val minDay = 1 - trip.preDays
    val dbStartDay = if (trip.startViewDay < minDay) minDay else trip.startViewDay

    var startViewDay by remember(trip.startViewDay) { mutableIntStateOf(dbStartDay) }

    LaunchedEffect(trip.preDays) {
        val newMinDay = 1 - trip.preDays
        if (startViewDay < newMinDay) {
            startViewDay = newMinDay
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cardRatio = 0.592f
    val dateFontSize = 26.sp
    val dayInfoFontSize = 14.sp
    val dayWeekGap = (-6).dp
    val dateOffsetX = 0.dp
    val dateOffsetY = (-11).dp
    val dayInfoOffsetX = 5.dp
    val dayInfoOffsetY = (-10).dp
    val screenPadding = 9.dp
    val gridSpacing = 9.dp
    val headerOffsetY = (-17).dp
    val gridOffsetY = (-17).dp
    val backgroundColor = Color(0xFFFAFAFA)

    val scheduleTopPadding = (-2.5).dp
    val scheduleTextOffsetY = (-10).dp
    val scheduleItemSpacing = 0.dp

    val view = LocalView.current
    if (!view.isInEditMode && selectedDay == null) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = backgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    val originalStart = LocalDate.parse(trip.startDate)
    val originalEnd = LocalDate.parse(trip.endDate)
    val originalDuration = ChronoUnit.DAYS.between(originalStart, originalEnd).toInt() + 1

    val allDays = remember(trip) {
        val totalStartDayNum = 1 - trip.preDays
        val totalEndDayNum = originalDuration + trip.postDays

        (totalStartDayNum..totalEndDayNum).map { dayNum ->
            val date = originalStart.plusDays((dayNum - 1).toLong())
            dayNum to date
        }
    }

    val displayDays = remember(allDays, startViewDay) {
        val (past, upcoming) = allDays.partition { it.first < startViewDay }
        upcoming + past
    }

    Crossfade(targetState = selectedDay, animationSpec = tween(300)) { currentSelectedDay ->
        if (currentSelectedDay != null) {
            BackHandler { selectedDay = null }
            val currentDayPair = allDays.find { it.first == currentSelectedDay }
            val formattedDate = currentDayPair?.second?.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) ?: ""

            ScheduleScreen(
                tripId = trip.id,
                dayNumber = currentSelectedDay,
                tripTitle = trip.title,
                tripDate = formattedDate,
                onBackClick = { selectedDay = null }
            )
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                // [수정] 상단(Top)과 좌우(Horizontal) 시스템 바 영역만 적용하고, 하단(Bottom)은 무시하도록 설정
                // 이렇게 하면 상태바 여백은 확보되어 제목이 가려지지 않고, 하단은 화면 끝까지 확장됩니다.
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showEditSheet = true },
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Trip")
                    }
                }
            ) { padding ->
                // padding.calculateTopPadding()이 이제 정상적인 상태바 높이를 반환합니다.
                Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = screenPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().offset(y = headerOffsetY),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = trip.title.uppercase(),
                                fontFamily = PlayfairDisplay,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.offset(x = 13.dp),
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                            Text(
                                text = "${trip.startDate.replace("-", ".")} — ${trip.endDate.replace("-", ".")}",
                                fontFamily = NotoSansKR,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            modifier = Modifier.offset(y = gridOffsetY),
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                            verticalArrangement = Arrangement.spacedBy(gridSpacing),
                            // 하단 여백: 네비게이션 바를 고려하여 적절히 띄워줍니다.
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(displayDays) { (dayNumber, date) ->
                                val daysSchedules = allSchedules.filter { it.dayNumber == dayNumber }
                                val dayInfo = allDayInfos.find { it.dayNumber == dayNumber }

                                val isPast = dayNumber < startViewDay

                                val dayLabel = when {
                                    dayNumber < 1 -> "Day Before ${1 - dayNumber}"
                                    dayNumber > originalDuration -> "Day After ${dayNumber - originalDuration}"
                                    else -> "Day $dayNumber"
                                }

                                val labelSize = if (dayNumber < 1 || dayNumber > originalDuration) 11.sp else (dayInfoFontSize.value - 1).sp

                                GridDayCard(
                                    dayLabel = dayLabel,
                                    date = date,
                                    schedules = daysSchedules,
                                    dayInfo = dayInfo,
                                    isPast = isPast,
                                    onClick = { selectedDay = dayNumber },
                                    cardRatio = cardRatio,
                                    dateOffset = dateOffsetX to dateOffsetY,
                                    dayInfoOffset = dayInfoOffsetX to dayInfoOffsetY,
                                    dateFontSize = dateFontSize,
                                    dayInfoFontSize = dayInfoFontSize,
                                    dayLabelFontSize = labelSize,
                                    dayWeekGap = dayWeekGap,
                                    scheduleTopPadding = scheduleTopPadding,
                                    scheduleTextOffsetY = scheduleTextOffsetY,
                                    scheduleItemSpacing = scheduleItemSpacing
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            TripEditSheet(
                startViewDay = startViewDay,
                dayNumbers = allDays.map { it.first },
                originalDuration = originalDuration,
                preDays = trip.preDays,
                postDays = trip.postDays,
                onStartViewDayChange = {
                    tripViewModel.saveStartViewDay(trip, it)
                },
                onIncreasePre = { tripViewModel.increasePreDays(trip) },
                onDecreasePre = { tripViewModel.decreasePreDays(trip) },
                onIncreasePost = { tripViewModel.increasePostDays(trip) },
                onDecreasePost = { tripViewModel.decreasePostDays(trip, originalDuration) },
                onDismiss = { showEditSheet = false }
            )
        }
    }
}

@Composable
fun GridDayCard(
    dayLabel: String,
    date: LocalDate,
    schedules: List<Schedule>,
    dayInfo: DayInfo?,
    isPast: Boolean,
    onClick: () -> Unit,
    cardRatio: Float,
    dateOffset: Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp>,
    dayInfoOffset: Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp>,
    dateFontSize: androidx.compose.ui.unit.TextUnit,
    dayInfoFontSize: androidx.compose.ui.unit.TextUnit,
    dayLabelFontSize: androidx.compose.ui.unit.TextUnit,
    dayWeekGap: androidx.compose.ui.unit.Dp,
    scheduleTopPadding: androidx.compose.ui.unit.Dp,
    scheduleTextOffsetY: androidx.compose.ui.unit.Dp,
    scheduleItemSpacing: androidx.compose.ui.unit.Dp
) {
    val dayOfWeek = date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH).uppercase()
    val formattedDate = date.format(DateTimeFormatter.ofPattern("MM.dd"))

    val contentAlpha = if (isPast) 0.3f else 1f
    val cardColor = Color.White
    val borderColor = if (isPast) Color.LightGray.copy(alpha = 0.5f) else Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(cardRatio)
            .clickable { onClick() }
            .alpha(contentAlpha),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = formattedDate,
                        fontFamily = NotoSansKR,
                        fontSize = dateFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = (-1).sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeight = dateFontSize),
                        modifier = Modifier.offset(x = dateOffset.first, y = dateOffset.second)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(x = dayInfoOffset.first, y = dayInfoOffset.second)
                    ) {
                        Text(
                            text = dayOfWeek,
                            fontFamily = NotoSansKR,
                            fontSize = dayInfoFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = dayLabel,
                            fontFamily = NotoSansKR,
                            fontSize = dayLabelFontSize,
                            color = Color.Gray,
                            modifier = Modifier.offset(y = dayWeekGap)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = scheduleTopPadding),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val previewSchedules = schedules.take(4)

                    previewSchedules.forEachIndexed { index, schedule ->
                        val isLast = index == previewSchedules.lastIndex

                        Row(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                )
                                if (!isLast) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .weight(1f)
                                            .background(Color.LightGray)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(schedule.time)
                                    }
                                    append(" - ${schedule.title}")
                                },
                                fontFamily = NotoSansKR,
                                fontSize = 10.sp,
                                color = Color.DarkGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .offset(y = scheduleTextOffsetY)
                                    .padding(bottom = if(!isLast) scheduleItemSpacing else 0.dp)
                            )
                        }
                    }

                    if (schedules.size > 4) {
                        Text(
                            text = "+ ${schedules.size - 4}",
                            fontFamily = NotoSansKR,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 4.dp)
                                .offset(y = scheduleTopPadding)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dayInfo?.city?.ifBlank { null } ?: "City",
                        fontFamily = NotoSansKR,
                        fontSize = 10.sp,
                        color = if (dayInfo?.city.isNullOrBlank()) Color.Gray else Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dayInfo?.accommodation?.ifBlank { null } ?: "Accommodation",
                        fontFamily = NotoSansKR,
                        fontSize = 10.sp,
                        color = if (dayInfo?.accommodation.isNullOrBlank()) Color.Gray else Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}