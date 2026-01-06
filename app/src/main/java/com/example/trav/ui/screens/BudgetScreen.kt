package com.example.trav.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trav.data.AppDatabase
import com.example.trav.data.BudgetCategory
import com.example.trav.data.Schedule
import com.example.trav.data.Trip
import com.example.trav.ui.components.BudgetAddSheet
import com.example.trav.ui.components.BudgetEditSheet
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import com.example.trav.ui.viewmodel.BudgetViewModel
import com.example.trav.ui.viewmodel.BudgetViewModelFactory
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(trip: Trip) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(database.scheduleDao(), database.tripDao(), trip.id)
    )

    val schedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    val totalExpense by viewModel.totalExpense.collectAsState(initial = 0.0)

    // Sheet State
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    // 순서 변경 가능한 카테고리 리스트 (State)
    val categories = remember { mutableStateListOf(*BudgetCategory.budgetDisplayCategories.toTypedArray()) }

    // Drag & Drop State
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItemOffset by remember { mutableStateOf(0f) }

    // 여행 기간 계산
    val tripDuration = remember(trip) {
        try {
            val start = LocalDate.parse(trip.startDate)
            val end = LocalDate.parse(trip.endDate)
            ChronoUnit.DAYS.between(start, end).toInt() + 1
        } catch (e: Exception) {
            1
        }
    }

    // 날짜 포맷팅
    val formattedDate = remember(trip.startDate) {
        try {
            LocalDate.parse(trip.startDate).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            trip.startDate
        }
    }

    val appBackgroundColor = Color(0xFFFAFAFA)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appBackgroundColor.toArgb()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackgroundColor)
                .padding(horizontal = 24.dp)
        ) {
            // [1. 헤더 (오른쪽에 날짜/제목 표시)]
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget",
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.Black
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trip.title.uppercase(),
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = formattedDate,
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // [2. Total Expense (상단 배치)]
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Expense", fontFamily = NotoSansKR, color = Color.Gray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(formatCurrency(totalExpense), fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // [3. 카테고리 리스트 (Drag & Drop 적용)]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    val isDragging = index == draggingItemIndex
                    val zIndex = if (isDragging) 1f else 0f
                    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scale")
                    val shadow = if (isDragging) 8.dp else 0.dp

                    val categoryItems = if (category == "기타") {
                        schedules.filter { it.category == "기타" || it.category.isBlank() || !BudgetCategory.budgetDisplayCategories.contains(it.category) }
                    } else {
                        schedules.filter { it.category == category }
                    }

                    val categorySum = categoryItems.sumOf { it.amount }
                    val isExpanded = expandedCategories[category] == true

                    Box(
                        modifier = Modifier
                            .zIndex(zIndex)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationY = if (isDragging) draggingItemOffset else 0f
                            }
                            .shadow(shadow, RoundedCornerShape(16.dp))
                            // [드래그 제스처 감지]
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingItemIndex = index
                                        draggingItemOffset = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        draggingItemOffset += dragAmount.y

                                        val currentOffset = draggingItemOffset
                                        val itemHeight = 60.dp.toPx() // 대략적인 아이템 높이

                                        // 아래로 이동
                                        if (currentOffset > itemHeight && index < categories.lastIndex) {
                                            val nextIndex = index + 1
                                            categories.add(nextIndex, categories.removeAt(index))
                                            draggingItemIndex = nextIndex
                                            draggingItemOffset -= itemHeight
                                        }
                                        // 위로 이동
                                        else if (currentOffset < -itemHeight && index > 0) {
                                            val prevIndex = index - 1
                                            categories.add(prevIndex, categories.removeAt(index))
                                            draggingItemIndex = prevIndex
                                            draggingItemOffset += itemHeight
                                        }
                                    },
                                    onDragEnd = {
                                        draggingItemIndex = null
                                        draggingItemOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggingItemIndex = null
                                        draggingItemOffset = 0f
                                    }
                                )
                            }
                    ) {
                        CategoryBudgetGroup(
                            category = category,
                            amount = categorySum,
                            isExpanded = isExpanded,
                            onToggle = { expandedCategories[category] = !isExpanded },
                            items = categoryItems,
                            onItemClick = { item -> selectedSchedule = item }
                        )
                    }
                }
                // 하단 FAB 공간 확보
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // [4. FAB (BudgetAddSheet 연결)]
        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = Color.Black,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    // [BudgetAddSheet]
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            BudgetAddSheet(
                tripDuration = tripDuration,
                onConfirm = { title, amount, category, subCategory, memo, location, time, source, dayNum ->
                    // ViewModel에 업데이트 요청 (신규 추가)
                    val newSchedule = Schedule(
                        tripId = trip.id,
                        dayNumber = dayNum,
                        time = time,
                        title = title,
                        location = location,
                        memo = memo,
                        category = category,
                        subCategory = subCategory,
                        amount = amount,
                        bookingSource = source
                    )
                    viewModel.updateBudgetItem(newSchedule)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    // [BudgetEditSheet]
    if (selectedSchedule != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSchedule = null },
            sheetState = sheetState,
            containerColor = Color.Transparent,
            dragHandle = null
        ) {
            BudgetEditSheet(
                schedule = selectedSchedule!!,
                tripDuration = tripDuration,
                onSave = { updatedItem ->
                    viewModel.updateBudgetItem(updatedItem)
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
}

@Composable
fun CategoryBudgetGroup(
    category: String,
    amount: Double,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    items: List<Schedule>,
    onItemClick: (Schedule) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 카테고리 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable { onToggle() }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (amount > 0 || items.isNotEmpty()) Color.Black else Color.LightGray))
                Spacer(modifier = Modifier.width(16.dp))
                Text(category, fontFamily = NotoSansKR, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (amount > 0 || items.isNotEmpty()) Color.Black else Color.Gray)
                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("(${items.size})", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatCurrency(amount), fontFamily = NotoSansKR, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (amount > 0) Color.Black else Color.LightGray)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }

        // 상세 리스트
        AnimatedVisibility(visible = isExpanded) {
            Column {
                if (items.isEmpty()) {
                    Text("No items", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 13.sp, color = Color.Black, fontFamily = NotoSansKR, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                if (item.memo.isNotBlank()) {
                                    Text(item.memo, fontSize = 11.sp, color = Color.Gray, fontFamily = NotoSansKR, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                            Text(formatCurrency(item.amount), fontSize = 13.sp, color = if(item.amount > 0) Color.Black else Color.LightGray, fontFamily = NotoSansKR)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).format(amount.toInt())
}