package com.example.trav.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun DayInfoSheet(
    initialCity: String,
    initialStay: String,
    initialCheckInDay: String,
    initialCheckInTime: String,
    initialCheckOutDay: String,
    initialCheckOutTime: String,
    totalDays: Int, // [추가] 여행 전체 일수
    onSave: (String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val topPadding = 20.dp
    val spacing = 16.dp

    // [City List 관리] DB 문자열("A, B") -> 리스트 변환
    val initialCityList = if (initialCity.isBlank()) listOf("") else initialCity.split(",").map { it.trim() }
    val cityList = remember { mutableStateListOf(*initialCityList.toTypedArray()) }

    var stay by remember { mutableStateOf(initialStay) }

    // [체크인/아웃 상태]
    var checkInDay by remember { mutableStateOf(initialCheckInDay) }
    var checkInTime by remember { mutableStateOf(initialCheckInTime) }
    var checkOutDay by remember { mutableStateOf(initialCheckOutDay) }
    var checkOutTime by remember { mutableStateOf(initialCheckOutTime) }

    // Picker Dialog States
    var showCheckInDayPicker by remember { mutableStateOf(false) }
    var showCheckInTimePicker by remember { mutableStateOf(false) }
    var showCheckOutDayPicker by remember { mutableStateOf(false) }
    var showCheckOutTimePicker by remember { mutableStateOf(false) }

    // --- Pickers ---
    if (showCheckInDayPicker) {
        WheelDayPickerDialog(totalDays, if(checkInDay.isBlank()) "Day 1" else checkInDay, { checkInDay = it; showCheckInDayPicker = false }, { showCheckInDayPicker = false })
    }
    if (showCheckOutDayPicker) {
        WheelDayPickerDialog(totalDays, if(checkOutDay.isBlank()) "Day 1" else checkOutDay, { checkOutDay = it; showCheckOutDayPicker = false }, { showCheckOutDayPicker = false })
    }
    if (showCheckInTimePicker) {
        WheelTimePickerDialog(if(checkInTime.isBlank()) "15:00" else checkInTime, { checkInTime = it; showCheckInTimePicker = false }, { showCheckInTimePicker = false })
    }
    if (showCheckOutTimePicker) {
        WheelTimePickerDialog(if(checkOutTime.isBlank()) "11:00" else checkOutTime, { checkOutTime = it; showCheckOutTimePicker = false }, { showCheckOutTimePicker = false })
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
                        Text("Day Info", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Button(
                            onClick = {
                                // City 리스트를 콤마로 합쳐서 저장
                                val cityString = cityList.filter { it.isNotBlank() }.joinToString(", ")
                                onSave(cityString, stay, checkInDay, checkInTime, checkOutDay, checkOutTime)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(32.dp)
                        ) { Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
                    }

                    // 1. City (Multi-row with + / - buttons)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("City", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.weight(1f))
                        // [City 추가 버튼]
                        CircleIconButton(
                            icon = Icons.Default.Add,
                            containerColor = Color.Black, iconColor = Color.White,
                            onClick = { cityList.add("") } // 빈 칸 추가
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    cityList.forEachIndexed { index, cityValue ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            InfoTextField(
                                value = cityValue,
                                onValueChange = { cityList[index] = it },
                                placeholder = "City Name",
                                imeAction = ImeAction.Next,
                                height = boxHeight,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                            // 2개 이상일 때만 삭제 버튼 표시
                            if (cityList.size > 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircleIconButton(
                                    icon = Icons.Default.Remove,
                                    containerColor = Color(0xFFF0F0F0), iconColor = Color.Black,
                                    onClick = { cityList.removeAt(index) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // 2. Accommodation
                    Text("Accommodation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoTextField(value = stay, onValueChange = { stay = it }, placeholder = "Hotel / Airbnb Name", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                    Spacer(modifier = Modifier.height(spacing))

                    // 3. Check-in (Date + Time)
                    Text("Check-in", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReadOnlyInputBox(
                            text = checkInDay, placeholder = "Day Select",
                            onClick = { showCheckInDayPicker = true },
                            height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ReadOnlyInputBox(
                            text = checkInTime, placeholder = "Time",
                            onClick = { showCheckInTimePicker = true },
                            height = boxHeight, fontSize = fontSize, modifier = Modifier.width(100.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // 4. Check-out (Date + Time)
                    Text("Check-out", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReadOnlyInputBox(
                            text = checkOutDay, placeholder = "Day Select",
                            onClick = { showCheckOutDayPicker = true },
                            height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ReadOnlyInputBox(
                            text = checkOutTime, placeholder = "Time",
                            onClick = { showCheckOutTimePicker = true },
                            height = boxHeight, fontSize = fontSize, modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }
        }
    }
}