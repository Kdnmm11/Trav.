package com.example.trav.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer // graphicsLayer 사용을 위해 필수
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.ui.theme.PlayfairDisplay
import kotlinx.coroutines.launch

@Composable
fun DayInfoSheet(
    initialCity: String,
    initialStay: String,
    initialCheckInDay: String,
    initialCheckInTime: String,
    initialCheckOutDay: String,
    initialCheckOutTime: String,
    totalDays: Int,
    tripDuration: Int,
    startDate: String,
    onSave: (String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val topPadding = 20.dp
    val iconSize = 24.dp
    val fixedCityBoxWidth = 240.dp
    val slowDuration = 200 // 테스트용 2초 (완료 후 250으로 변경)

    // 애니메이션 상태 관리를 위한 클래스
    class CityState(
        val id: Int,
        initialName: String,
        initialHeight: Float = 1f
    ) {
        var name by mutableStateOf(initialName)
        val animHeight = Animatable(initialHeight)
    }

    val initialCityNames = remember(initialCity) {
        if (initialCity.isBlank()) listOf("") else initialCity.split(",").map { it.trim() }
    }

    val cityStates = remember {
        mutableStateListOf<CityState>().apply {
            initialCityNames.forEachIndexed { index, s -> add(CityState(index, s)) }
        }
    }
    var nextId by remember { mutableIntStateOf(initialCityNames.size) }
    val coroutineScope = rememberCoroutineScope()

    var stay by remember(initialStay) { mutableStateOf(initialStay) }
    var checkInDay by remember(initialCheckInDay) { mutableStateOf(initialCheckInDay) }
    var checkInTime by remember(initialCheckInTime) { mutableStateOf(initialCheckInTime) }
    var checkOutDay by remember(initialCheckOutDay) { mutableStateOf(initialCheckOutDay) }
    var checkOutTime by remember(initialCheckOutTime) { mutableStateOf(initialCheckOutTime) }

    var showCheckInDayPicker by remember { mutableStateOf(false) }
    var showCheckInTimePicker by remember { mutableStateOf(false) }
    var showCheckOutDayPicker by remember { mutableStateOf(false) }
    var showCheckOutTimePicker by remember { mutableStateOf(false) }

    if (showCheckInDayPicker) { WheelDayPickerDialog(totalDays = tripDuration, startDate = startDate, initialDay = if(checkInDay.isBlank()) "Day 1" else checkInDay, onDaySelected = { checkInDay = it; showCheckInDayPicker = false }, onDismiss = { showCheckInDayPicker = false }) }
    if (showCheckOutDayPicker) { WheelDayPickerDialog(totalDays = tripDuration, startDate = startDate, initialDay = if(checkOutDay.isBlank()) "Day 1" else checkOutDay, onDaySelected = { checkOutDay = it; showCheckOutDayPicker = false }, onDismiss = { showCheckOutDayPicker = false }) }
    if (showCheckInTimePicker) { WheelTimePickerDialog(if(checkInTime.isBlank()) "15:00" else checkInTime, { checkInTime = it; showCheckInTimePicker = false }, { showCheckInTimePicker = false }) }
    if (showCheckOutTimePicker) { WheelTimePickerDialog(if(checkOutTime.isBlank()) "11:00" else checkOutTime, { checkOutTime = it; showCheckOutTimePicker = false }, { showCheckOutTimePicker = false }) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(10.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = topPadding, bottom = 24.dp)
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(slowDuration, easing = LinearOutSlowInEasing))
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Day Info", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Button(
                            onClick = { onSave(cityStates.joinToString(", ") { it.name }, stay, checkInDay, checkInTime, checkOutDay, checkOutTime) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) { Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("City", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        DayInfoCircleIconButton(icon = Icons.Default.Add, containerColor = Color.Black, iconColor = Color.White, onClick = {
                            val newState = CityState(nextId++, "", initialHeight = 0f)
                            cityStates.add(newState)
                            coroutineScope.launch { newState.animHeight.animateTo(1f, tween(slowDuration, easing = LinearOutSlowInEasing)) }
                        }, size = iconSize)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        cityStates.forEachIndexed { index, state ->
                            // [수정된 애니메이션 영역]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((56 * state.animHeight.value).dp) // 44dp(박스) + 12dp(간격)를 애니메이션 값에 곱함
                                    .graphicsLayer {
                                        this.alpha = state.animHeight.value // 투명도 조절
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().height(44.dp).align(Alignment.TopCenter)
                                ) {
                                    Text(text = "City ${index + 1}", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp))
                                    InfoTextField(
                                        value = state.name,
                                        onValueChange = { state.name = it },
                                        placeholder = "Enter city name",
                                        imeAction = ImeAction.Next,
                                        height = boxHeight,
                                        fontSize = fontSize,
                                        modifier = Modifier.width(fixedCityBoxWidth)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.size(iconSize), contentAlignment = Alignment.Center) {
                                        if (cityStates.size > 1) {
                                            DayInfoCircleIconButton(
                                                icon = Icons.Default.Remove,
                                                containerColor = Color(0xFFF0F0F0),
                                                iconColor = Color.Black,
                                                onClick = {
                                                    coroutineScope.launch {
                                                        state.animHeight.animateTo(0f, tween(slowDuration, easing = LinearOutSlowInEasing))
                                                        cityStates.remove(state)
                                                    }
                                                },
                                                size = iconSize
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Accommodation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoTextField(value = stay, onValueChange = { stay = it }, placeholder = "Hotel / Airbnb Name", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Check-in", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReadOnlyInputBox(text = checkInDay, placeholder = "Day Select", onClick = { showCheckInDayPicker = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        ReadOnlyInputBox(text = checkInTime, placeholder = "Time", onClick = { showCheckInTimePicker = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Check-out", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReadOnlyInputBox(text = checkOutDay, placeholder = "Day Select", onClick = { showCheckOutDayPicker = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        ReadOnlyInputBox(text = checkOutTime, placeholder = "Time", onClick = { showCheckOutTimePicker = true }, height = boxHeight, fontSize = fontSize, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DayInfoCircleIconButton(icon: ImageVector, containerColor: Color, iconColor: Color, onClick: () -> Unit, size: Dp = 32.dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(containerColor).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(size / 2))
    }
}