package com.example.trav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
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
    totalDays: Int, // 전체 페이지 수 (Pre/Post 포함, 사용 안 할 수도 있음)
    tripDuration: Int, // [추가] 순수 여행 일수 (Picker 제한용)
    onSave: (String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val topPadding = 20.dp
    val spacing = 16.dp

    // City List
    val initialCityList = remember(initialCity) {
        if (initialCity.isBlank()) listOf("") else initialCity.split(",").map { it.trim() }
    }
    val cityList = remember(initialCity) { mutableStateListOf(*initialCityList.toTypedArray()) }

    var stay by remember(initialStay) { mutableStateOf(initialStay) }

    // Check-in/out
    var checkInDay by remember(initialCheckInDay) { mutableStateOf(initialCheckInDay) }
    var checkInTime by remember(initialCheckInTime) { mutableStateOf(initialCheckInTime) }
    var checkOutDay by remember(initialCheckOutDay) { mutableStateOf(initialCheckOutDay) }
    var checkOutTime by remember(initialCheckOutTime) { mutableStateOf(initialCheckOutTime) }

    // Picker States
    var showCheckInDayPicker by remember { mutableStateOf(false) }
    var showCheckInTimePicker by remember { mutableStateOf(false) }
    var showCheckOutDayPicker by remember { mutableStateOf(false) }
    var showCheckOutTimePicker by remember { mutableStateOf(false) }

    // --- Pickers (tripDuration 사용) ---
    if (showCheckInDayPicker) {
        // [수정] totalDays 대신 tripDuration 사용
        WheelDayPickerDialog(tripDuration, if(checkInDay.isBlank()) "Day 1" else checkInDay, { checkInDay = it; showCheckInDayPicker = false }, { showCheckInDayPicker = false })
    }
    if (showCheckOutDayPicker) {
        // [수정] totalDays 대신 tripDuration 사용
        WheelDayPickerDialog(tripDuration, if(checkOutDay.isBlank()) "Day 1" else checkOutDay, { checkOutDay = it; showCheckOutDayPicker = false }, { showCheckOutDayPicker = false })
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
                                val cityString = cityList.filter { it.isNotBlank() }.joinToString(", ")
                                onSave(cityString, stay, checkInDay, checkInTime, checkOutDay, checkOutTime)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(32.dp)
                        ) { Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
                    }

                    // 1. City (Vertical List)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("City", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        DayInfoCircleIconButton(
                            icon = Icons.Default.Add,
                            containerColor = Color.Black, iconColor = Color.White,
                            onClick = { cityList.add("") },
                            size = 24.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        cityList.forEachIndexed { index, cityValue ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "City ${index + 1}",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(48.dp)
                                )

                                InfoTextField(
                                    value = cityValue,
                                    onValueChange = { cityList[index] = it },
                                    placeholder = "Enter city name",
                                    imeAction = ImeAction.Next,
                                    height = boxHeight,
                                    fontSize = fontSize,
                                    modifier = Modifier.weight(1f)
                                )

                                if (index > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    DayInfoCircleIconButton(
                                        icon = Icons.Default.Remove,
                                        containerColor = Color(0xFFF0F0F0), iconColor = Color.Black,
                                        onClick = { cityList.removeAt(index) },
                                        size = 24.dp
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(32.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // 2. Accommodation
                    Text("Accommodation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoTextField(value = stay, onValueChange = { stay = it }, placeholder = "Hotel / Airbnb Name", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                    Spacer(modifier = Modifier.height(spacing))

                    // 3. Check-in
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
                            height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // 4. Check-out
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
                            height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayInfoCircleIconButton(
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    size: Dp = 32.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(size / 2))
    }
}