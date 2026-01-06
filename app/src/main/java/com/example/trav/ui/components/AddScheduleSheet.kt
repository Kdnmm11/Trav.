package com.example.trav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.data.BudgetCategory
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun AddScheduleSheet(
    onConfirm: (String, String, String, String, String, String, String, Double, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val topPadding = 20.dp
    val spacing = 16.dp

    var time by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    // [상세 필드]
    var location by remember { mutableStateOf("") }
    var arrivalPlace by remember { mutableStateOf("") }
    var reservationNum by remember { mutableStateOf("") }
    var bookingSource by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    // [카테고리] - 기본값 ON
    var isCategoryEnabled by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubCategory by remember { mutableStateOf("") }

    var isEndTimeVisible by remember { mutableStateOf(false) }
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }

    if (showTimePickerForStart) {
        WheelTimePickerDialog(
            initialTime = if(time.isBlank()) "09:00" else time,
            onTimeSelected = { time = it; if (endTime.isNotBlank() && endTime < it) endTime = ""; showTimePickerForStart = false },
            onDismiss = { showTimePickerForStart = false }
        )
    }
    if (showTimePickerForEnd) {
        WheelTimePickerDialog(
            initialTime = if (endTime.isNotBlank()) endTime else (if(time.isBlank()) "09:00" else time),
            onTimeSelected = { endTime = it; showTimePickerForEnd = false },
            onDismiss = { showTimePickerForEnd = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp)
                        .padding(top = topPadding, bottom = 24.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // [헤더]
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Schedule", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)

                        Button(
                            onClick = {
                                if (title.isNotBlank() && time.isNotBlank()) {
                                    onConfirm(
                                        time, endTime, title, location, memo,
                                        if(isCategoryEnabled) selectedCategory else "",
                                        if(isCategoryEnabled) selectedSubCategory else "",
                                        0.0,
                                        arrivalPlace, reservationNum, bookingSource
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(32.dp)
                        ) { Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
                    }

                    // 1. Title (맨 위)
                    Text("Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoTextField(value = title, onValueChange = { title = it }, placeholder = "Schedule Title", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                    Spacer(modifier = Modifier.height(spacing))

                    // 2. Category (On/Off 스위치 포함)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Switch(
                            checked = isCategoryEnabled,
                            onCheckedChange = { isCategoryEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Black,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            ),
                            modifier = Modifier.graphicsLayer(scaleX = 0.7f, scaleY = 0.7f).height(30.dp)
                        )
                    }

                    if (isCategoryEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        CategorySelector(
                            categories = BudgetCategory.mainCategories,
                            selected = selectedCategory,
                            onSelect = { selectedCategory = if (selectedCategory == it) "" else it; selectedSubCategory = "" }
                        )

                        if (selectedCategory.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            CategorySelector(
                                categories = BudgetCategory.subCategories[selectedCategory] ?: emptyList(),
                                selected = selectedSubCategory,
                                onSelect = { selectedSubCategory = if (selectedSubCategory == it) "" else it }
                            )

                            Spacer(modifier = Modifier.height(spacing))

                            // [카테고리별 입력 필드] - 시간보다 먼저 나옴 (교통의 경우 출발/도착지 -> 시간 순서)
                            when (selectedCategory) {
                                "교통" -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Departure", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            InfoTextField(value = location, onValueChange = { location = it }, placeholder = "Start", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Arrival", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            InfoTextField(value = arrivalPlace, onValueChange = { arrivalPlace = it }, placeholder = "Dest", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                        }
                                    }
                                }
                                "식사" -> {
                                    Text("Restaurant Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    InfoTextField(value = location, onValueChange = { location = it }, placeholder = "Map Location", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                    Spacer(modifier = Modifier.height(spacing))
                                    Text("Reservation No.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    InfoTextField(value = reservationNum, onValueChange = { reservationNum = it }, placeholder = "Optional", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                }
                                "관광" -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Booking Source", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            InfoTextField(value = bookingSource, onValueChange = { bookingSource = it }, placeholder = "e.g. Klook", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Res No.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            InfoTextField(value = reservationNum, onValueChange = { reservationNum = it }, placeholder = "Optional", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                        }
                                    }
                                }
                                else -> { // 기타
                                    Text("Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    InfoTextField(value = location, onValueChange = { location = it }, placeholder = "Place", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                }
                            }
                        }
                    } else {
                        // Category Off일 때는 아무것도 표시 안함 (공백 처리)
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // 3. Time (카테고리 아래로 이동)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        if (!isEndTimeVisible) {
                            CircleIconButton(
                                icon = Icons.Default.Add,
                                containerColor = Color.Black, iconColor = Color.White,
                                onClick = { isEndTimeVisible = true }
                            )
                        } else {
                            CircleIconButton(
                                icon = Icons.Default.Remove,
                                containerColor = Color.Black, iconColor = Color.White,
                                onClick = { isEndTimeVisible = false; endTime = "" }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isEndTimeVisible) {
                            TimeInputBox(time = time, onClick = { showTimePickerForStart = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f), placeholder = "Select Time")
                        } else {
                            TimeInputBox(time = time, onClick = { showTimePickerForStart = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f), placeholder = "Start")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            TimeInputBox(time = endTime, onClick = { showTimePickerForEnd = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f), placeholder = "End")
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // 4. Memo
                    Text("Memo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoTextField(value = memo, onValueChange = { memo = it }, placeholder = "Details...", imeAction = ImeAction.Done, height = boxHeight, fontSize = fontSize)
                }
            }
        }
    }
}