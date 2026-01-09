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
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun AddScheduleSheet(
    tripDuration: Int,
    startDate: String,
    currentDayNumber: Int,
    onConfirm: (String, String, String, String, String, String, String, Double, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val standardPadding = 24.dp
    val innerWhite = Color.White
    val inputGray = Color(0xFFF5F5F5)

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    BackHandler(enabled = isImeVisible) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("기타") }
    var selectedSubCategory by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf("Day $currentDayNumber") }

    var transDepDay by remember { mutableStateOf("Day $currentDayNumber") }
    var transDepTime by remember { mutableStateOf("") }
    var transArrDay by remember { mutableStateOf("Day $currentDayNumber") }
    var transArrTime by remember { mutableStateOf("") }

    var location by remember { mutableStateOf("") }
    var arrivalPlace by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    var isEndTimeVisible by remember { mutableStateOf(false) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf("") }
    var dayPickerTarget by remember { mutableStateOf("") }

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
            WheelEndTimePickerDialog(
                initialTime = if(current.isBlank()) "09:00" else current,
                onTimeSelected = { endTime = it; showTimePicker = false },
                onChangeDayClick = { dayPickerTarget = "end"; showDayPicker = true },
                onDismiss = { showTimePicker = false }
            )
        } else {
            WheelTimePickerDialog(
                initialTime = if(current.isBlank()) "09:00" else current,
                onTimeSelected = {
                    when(timePickerTarget) {
                        "start" -> time = it
                        "transDep" -> transDepTime = it
                        "transArr" -> transArrTime = it
                    }
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(10.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() },
                shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(top = 24.dp, bottom = 24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = standardPadding).padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Schedule", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    if (selectedCategory == "교통") {
                                        val finalLoc = if(location.isNotBlank() && arrivalPlace.isNotBlank()) "$location > $arrivalPlace" else location
                                        // [해결 1] 파라미터 순서 수정: transArrTime을 endTime 자리에 넣고, selectedSubCategory를 정확한 위치에 넣음
                                        // 순서: time, endTime, title, location, memo, category, subCategory, amount, arrivalPlace, ...
                                        onConfirm(
                                            transDepTime,
                                            transArrTime, // endTime 자리에 도착 시간 전달 (타임테이블 높이 해결)
                                            title,
                                            finalLoc,
                                            memo,
                                            selectedCategory,
                                            selectedSubCategory.ifBlank { "교통" }, // subCategory 자리에 "버스" 등 전달 (뒤섞임 해결)
                                            0.0,
                                            arrivalPlace,
                                            "",
                                            ""
                                        )
                                    } else {
                                        val fEnd = if (isEndTimeVisible && endTime.isNotBlank()) { if (endDay != "Day $currentDayNumber") "$endTime ($endDay)" else endTime } else ""
                                        onConfirm(time, fEnd, title, location, memo, selectedCategory, selectedSubCategory, 0.0, arrivalPlace, "", "")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("SAVE", color = Color.White, fontSize = 12.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    // [Title]
                    SchedulePaddedContent(standardPadding) {
                        Text("Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = title, onValueChange = { title = it }, placeholder = "Schedule Title", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // [Category]
                    SchedulePaddedContent(standardPadding) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    val displayCategories = BudgetCategory.mainCategories.filter { it != "숙소" && it != "준비" }
                    LazyRow(contentPadding = PaddingValues(horizontal = standardPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayCategories) { cat ->
                            ScheduleCategoryChip(text = cat, isSelected = (selectedCategory == cat), onClick = { selectedCategory = cat; selectedSubCategory = "" })
                        }
                    }

                    // [SubCategory]
                    val subList = BudgetCategory.subCategories[selectedCategory] ?: emptyList()
                    AnimatedVisibility(
                        visible = subList.isNotEmpty(),
                        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(contentPadding = PaddingValues(horizontal = standardPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(subList) { sub ->
                                    val subIsSelected = (selectedSubCategory == sub)
                                    ScheduleCategoryChip(text = sub, isSelected = subIsSelected, onClick = { selectedSubCategory = if(subIsSelected) "" else sub }, backgroundColor = Color(0xFFE0E0E0))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // [본문 영역]
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Departure", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                                            InnerInputBox(transDepDay, "Date", { dayPickerTarget="transDep"; showDayPicker=true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(transDepTime, "Time", { timePickerTarget="transDep"; showTimePicker=true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(location, "Start", {}, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite, false, { location = it })
                                        }
                                        Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp).size(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Arrival", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR); Spacer(modifier = Modifier.height(4.dp))
                                            InnerInputBox(transArrDay, "Date", { dayPickerTarget="transArr"; showDayPicker=true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(transArrTime, "Time", { timePickerTarget="transArr"; showTimePicker=true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InnerInputBox(arrivalPlace, "Dest", {}, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite, false, { arrivalPlace = it })
                                        }
                                    }
                                }
                            } else {
                                SchedulePaddedContent(standardPadding) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR)
                                        ScheduleSmallCircleButton(icon = if(!isEndTimeVisible) Icons.Default.Add else Icons.Default.Remove, onClick = { isEndTimeVisible = !isEndTimeVisible })
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        TimeInputBox(time, {timePickerTarget="start"; showTimePicker=true}, boxHeight, fontSize, Modifier.weight(1f), if(isEndTimeVisible) "Start" else "Select Time")

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
                                                ReadOnlyInputBox(text = endDisplay, placeholder = "End", onClick = { timePickerTarget="end"; showTimePicker=true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.width(130.dp), isHint = endTime.isBlank())
                                            }
                                        }
                                    }
                                }

                                if (targetCategory != "기타") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SchedulePaddedContent(standardPadding) {
                                        Text("Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        InfoTextField(value = location, onValueChange = { location = it }, placeholder = if(targetCategory=="식사") "Restaurant Location" else "Map Location", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            SchedulePaddedContent(standardPadding) {
                                Text("Memo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = NotoSansKR)
                                Spacer(modifier = Modifier.height(4.dp))
                                InfoTextField(value = memo, onValueChange = { memo = it }, placeholder = "Details...", imeAction = ImeAction.Done, height = boxHeight, fontSize = fontSize)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchedulePaddedContent(padding: Dp, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = padding)) { content() }
}

@Composable
private fun ScheduleSmallCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, size: Dp = 20.dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(Color.Black).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.6f))
    }
}

@Composable
private fun ScheduleCategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit, backgroundColor: Color = Color(0xFFF5F5F5)) {
    Surface(
        color = if (isSelected) Color.Black else backgroundColor,
        contentColor = if (isSelected) Color.White else Color.Black,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Text(text = text, fontFamily = NotoSansKR, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}