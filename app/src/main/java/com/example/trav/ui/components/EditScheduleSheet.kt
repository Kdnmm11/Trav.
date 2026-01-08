package com.example.trav.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.data.BudgetCategory
import com.example.trav.data.Schedule
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun EditScheduleSheet(
    schedule: Schedule,
    tripDuration: Int,
    startDate: String,
    onUpdate: (String, String, String, String, String, String, String, Double, String, String, String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val currentDayNumber = schedule.dayNumber
    val standardPadding = 24.dp
    val inputGray = Color(0xFFF5F5F5)
    val innerWhite = Color.White

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    BackHandler(enabled = isImeVisible) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    var title by remember { mutableStateOf(schedule.title) }
    var memo by remember { mutableStateOf(schedule.memo) }
    val displayCategories = remember { BudgetCategory.mainCategories.filter { it != "숙소" && it != "준비" } }
    var selectedCategory by remember { mutableStateOf(schedule.category.ifBlank { "기타" }) }
    var selectedSubCategory by remember { mutableStateOf(schedule.subCategory) }
    var time by remember { mutableStateOf(schedule.time) }

    val (parsedEndTime, parsedEndDay) = remember(schedule.endTime) {
        if (schedule.endTime.contains(" (Day ")) {
            val parts = schedule.endTime.split(" (")
            parts[0] to parts[1].replace(")", "")
        } else schedule.endTime to "Day $currentDayNumber"
    }
    var endTime by remember { mutableStateOf(parsedEndTime) }
    var endDay by remember { mutableStateOf(parsedEndDay) }
    var isEndTimeVisible by remember { mutableStateOf(schedule.endTime.isNotBlank()) }

    var location by remember { mutableStateOf(schedule.location) }
    var arrivalPlace by remember { mutableStateOf(schedule.arrivalPlace) }

    var transDepDay by remember { mutableStateOf("Day $currentDayNumber") }
    var transDepTime by remember { mutableStateOf("") }
    var transArrDay by remember { mutableStateOf("Day $currentDayNumber") }
    var transArrTime by remember { mutableStateOf("") }

    LaunchedEffect(schedule) {
        if (schedule.category == "교통") {
            transDepTime = schedule.time
            val locParts = schedule.location.split(" > ")
            location = locParts.getOrNull(0) ?: ""
            arrivalPlace = locParts.getOrNull(1) ?: schedule.arrivalPlace
            val subParts = schedule.subCategory.split(" ")
            if (subParts.size >= 3) {
                transArrDay = "${subParts[0]} ${subParts[1]}"
                transArrTime = subParts[2]
            }
        }
    }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf("") }
    var dayPickerTarget by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("Delete") }, text = { Text("Are you sure?") },
            confirmButton = { TextButton(onClick = onDelete) { Text("DELETE", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } }, containerColor = Color.White)
    }

    if (showDayPicker) {
        val current = when(dayPickerTarget) { "transDep" -> transDepDay; "transArr" -> transArrDay; else -> endDay }
        WheelDayPickerDialog(tripDuration, startDate, current, {
            when(dayPickerTarget) { "transDep" -> transDepDay = it; "transArr" -> transArrDay = it; else -> endDay = it }
            showDayPicker = false
        }, { showDayPicker = false })
    }
    if (showTimePicker) {
        val current = when(timePickerTarget) { "start" -> time; "end" -> endTime; "transDep" -> transDepTime; "transArr" -> transArrTime; else -> "09:00" }
        if (timePickerTarget == "end") {
            WheelEndTimePickerDialog(current.ifBlank { "09:00" }, { endTime = it; showTimePicker = false }, { dayPickerTarget = "end"; showDayPicker = true }, { showTimePicker = false })
        } else {
            WheelTimePickerDialog(current.ifBlank { "09:00" }, {
                when(timePickerTarget) { "start" -> time = it; "transDep" -> transDepTime = it; "transArr" -> transArrTime = it }
                showTimePicker = false
            }, { showTimePicker = false })
        }
    }

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(10.dp)) {
            Card(modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() },
                shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)) {

                Column(modifier = Modifier.padding(top = 20.dp, bottom = 24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                    // [헤더: 버튼 양식 완벽 유지]
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = standardPadding).padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Edit Schedule", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(32.dp).padding(end = 8.dp)
                            ) {
                                Text("DELETE", color = Color.Black, fontSize = 12.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        if (selectedCategory == "교통") {
                                            onUpdate(transDepTime, "", title, "$location > $arrivalPlace", memo, selectedCategory, "$transArrDay $transArrTime", 0.0, arrivalPlace, "", "")
                                        } else {
                                            val fEnd = if (isEndTimeVisible && endTime.isNotBlank()) { if (endDay != "Day $currentDayNumber") "$endTime ($endDay)" else endTime } else ""
                                            onUpdate(time, fEnd, title, location, memo, selectedCategory, selectedSubCategory, 0.0, arrivalPlace, "", "")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("SAVE", color = Color.White, fontSize = 12.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Title
                    EditPaddedContent(standardPadding) {
                        Text("Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(title, {title=it}, "Title", ImeAction.Next, boxHeight, fontSize)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category
                    EditPaddedContent(standardPadding) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                    }

                    LazyRow(contentPadding = PaddingValues(horizontal = standardPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayCategories) { cat ->
                            val isSelected = selectedCategory == cat
                            EditCategoryChip(text = cat, isSelected = isSelected, onClick = { selectedCategory = cat; selectedSubCategory = "" })
                        }
                    }

                    val subList = BudgetCategory.subCategories[selectedCategory] ?: emptyList()
                    // SubCategory: BudgetAddSheet의 부드러운 애니메이션 이식
                    AnimatedVisibility(
                        visible = subList.isNotEmpty(),
                        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = standardPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subList) { sub ->
                                    val isSelected = selectedSubCategory == sub
                                    EditCategoryChip(text = sub, isSelected = isSelected, onClick = { selectedSubCategory = if(isSelected) "" else sub }, backgroundColor = Color(0xFFE0E0E0))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // [본문: BudgetAddSheet의 AnimatedContent 방식 적용]
                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)))
                                .togetherWith(fadeOut(animationSpec = tween(150)))
                        },
                        label = "CategoryTransition"
                    ) { targetCategory ->
                        Column {
                            if (targetCategory == "교통") {
                                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).clip(RoundedCornerShape(12.dp)).background(inputGray).padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Departure", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                                            InnerInputBox(transDepDay, "Date", { dayPickerTarget="transDep"; showDayPicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(transDepTime, "Time", { timePickerTarget="transDep"; showTimePicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(location, "Start", {}, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite, false, { location = it })
                                        }
                                        Column(modifier = Modifier.padding(horizontal = 8.dp)) { Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Arrival", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                                            InnerInputBox(transArrDay, "Date", { dayPickerTarget="transArr"; showDayPicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(transArrTime, "Time", { timePickerTarget="transArr"; showTimePicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(arrivalPlace, "Dest", {}, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite, false, { arrivalPlace = it })
                                        }
                                    }
                                }
                            } else {
                                // Time
                                EditPaddedContent(standardPadding) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR)
                                        EditSmallCircleButton(if(!isEndTimeVisible) Icons.Default.Add else Icons.Default.Remove, { isEndTimeVisible = !isEndTimeVisible })
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        TimeInputBox(time, {timePickerTarget="start"; showTimePicker=true}, boxHeight, fontSize, Modifier.weight(1f), if(isEndTimeVisible) "Start" else "Select Time")

                                        // Time 칸 추가/삭제 애니메이션
                                        AnimatedVisibility(
                                            visible = isEndTimeVisible,
                                            enter = fadeIn(tween(200)) + expandHorizontally(tween(200)),
                                            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val endDisplay = if(endTime.isNotBlank()) (if(endDay != "Day $currentDayNumber") "$endTime ($endDay)" else endTime) else "End"
                                                ReadOnlyInputBox(endDisplay, "End", { timePickerTarget="end"; showTimePicker=true }, boxHeight, fontSize, Modifier.width(130.dp), endTime.isBlank())
                                            }
                                        }
                                    }
                                }

                                if (targetCategory != "기타") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    EditPaddedContent(standardPadding) {
                                        Text("Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                                        InfoTextField(location, {location=it}, if(selectedCategory=="식사") "Restaurant Location" else "Map Location", ImeAction.Next, boxHeight, fontSize)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            EditPaddedContent(standardPadding) {
                                Text("Memo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                                InfoTextField(memo, {memo=it}, "Details...", ImeAction.Done, boxHeight, fontSize)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPaddedContent(padding: Dp, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = padding)) { content() }
}

@Composable
private fun EditSmallCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, size: Dp = 20.dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(Color.Black).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.6f))
    }
}

@Composable
private fun EditCategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit, backgroundColor: Color = Color(0xFFF5F5F5)) {
    Surface(
        color = if (isSelected) Color.Black else backgroundColor,
        contentColor = if (isSelected) Color.White else Color.Black,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = text, fontFamily = NotoSansKR, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}