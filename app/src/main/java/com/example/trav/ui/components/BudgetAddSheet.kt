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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.data.BudgetCategory
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun BudgetAddSheet(
    tripDuration: Int,
    onConfirm: (String, Double, String, String, String, String, String, String, Int) -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val keyboardController = LocalSoftwareKeyboardController.current

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    BackHandler(enabled = isImeVisible) {
        keyboardController?.hide()
    }

    val standardPadding = 24.dp
    val wideBoxPadding = 8.dp

    val inputGray = Color(0xFFF5F5F5)
    val innerWhite = Color.White

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf("기타") }
    var selectedSubCategory by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("KRW") }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    var depLocation by remember { mutableStateOf("") }
    var arrLocation by remember { mutableStateOf("") }
    var generalLocation by remember { mutableStateOf("") }

    var startDay by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    var accCheckInDay by remember { mutableStateOf("") }
    var accCheckInTime by remember { mutableStateOf("") }
    var accCheckOutDay by remember { mutableStateOf("") }
    var accCheckOutTime by remember { mutableStateOf("") }

    var transDepDay by remember { mutableStateOf("") }
    var transDepTime by remember { mutableStateOf("") }
    var transArrDay by remember { mutableStateOf("") }
    var transArrTime by remember { mutableStateOf("") }

    var isGeneralTimeVisible by remember { mutableStateOf(false) }
    var isTransportVisible by remember { mutableStateOf(false) }

    var isDateRange by remember { mutableStateOf(false) }
    var isTimeRange by remember { mutableStateOf(false) }

    var showStartDayPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var dayPickerTarget by remember { mutableStateOf("start") }
    var timePickerTarget by remember { mutableStateOf("start") }

    if (showStartDayPicker) {
        val currentDay = when(dayPickerTarget) {
            "accIn" -> accCheckInDay
            "accOut" -> accCheckOutDay
            "transDep" -> transDepDay
            "transArr" -> transArrDay
            "end" -> endDay
            else -> startDay
        }
        val initialDay = if(currentDay.isBlank()) "Day 1" else currentDay

        WheelDayPickerDialog(tripDuration, initialDay, {
            when(dayPickerTarget) {
                "accIn" -> accCheckInDay = it
                "accOut" -> accCheckOutDay = it
                "transDep" -> transDepDay = it
                "transArr" -> transArrDay = it
                "end" -> endDay = it
                else -> startDay = it
            }
            showStartDayPicker = false
        }, { showStartDayPicker = false })
    }

    if (showTimePicker) {
        val currentTime = when(timePickerTarget) {
            "accIn" -> accCheckInTime
            "accOut" -> accCheckOutTime
            "transDep" -> transDepTime
            "transArr" -> transArrTime
            "end" -> endTime
            else -> startTime
        }
        val initialTime = if(currentTime.isBlank()) "09:00" else currentTime

        WheelTimePickerDialog(initialTime, {
            when(timePickerTarget) {
                "accIn" -> accCheckInTime = it
                "accOut" -> accCheckOutTime = it
                "transDep" -> transDepTime = it
                "transArr" -> transArrTime = it
                "end" -> endTime = it
                else -> startTime = it
            }
            showTimePicker = false
        }, { showTimePicker = false })
    }

    if (showCurrencyPicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { showCurrencyPicker = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                CurrencyPickerContent(onSelected = { currency = it; showCurrencyPicker = false })
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { keyboardController?.hide() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 24.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = standardPadding)
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add Expense", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                                    var finalLocation = ""
                                    var finalTime = ""
                                    var finalDayNum = 1

                                    when(selectedCategory) {
                                        "숙소" -> {
                                            finalLocation = "$accCheckInDay|$accCheckInTime"
                                            finalDayNum = accCheckInDay.replace("Day ", "").toIntOrNull() ?: 1
                                        }
                                        "교통" -> {
                                            finalLocation = "$depLocation -> $arrLocation"
                                            finalTime = "$transDepDay $transDepTime"
                                            finalDayNum = transDepDay.replace("Day ", "").toIntOrNull() ?: 1
                                        }
                                        "준비" -> { finalDayNum = 1 }
                                        else -> {
                                            finalLocation = generalLocation
                                            if (isGeneralTimeVisible) {
                                                finalTime = startTime
                                                finalDayNum = startDay.replace("Day ", "").toIntOrNull() ?: 1
                                            } else {
                                                finalDayNum = 1
                                            }
                                        }
                                    }
                                    val finalSubCat = if (selectedCategory == "숙소") "$accCheckOutDay|$accCheckOutTime" else if (selectedCategory == "교통") "$transArrDay $transArrTime" else selectedSubCategory
                                    onConfirm(title, amountVal, selectedCategory, finalSubCat, memo, finalLocation, finalTime, "", finalDayNum)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(32.dp)
                        ) { Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
                    }

                    PaddedContent(standardPadding) {
                        Text("Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = title, onValueChange = { title = it }, placeholder = "Expense Title", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PaddedContent(standardPadding) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = standardPadding),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BudgetCategory.budgetDisplayCategories) { cat ->
                            CategoryChip(text = cat, isSelected = selectedCategory == cat, onClick = { selectedCategory = cat; selectedSubCategory = "" })
                        }
                    }

                    val subList = BudgetCategory.subCategories[selectedCategory] ?: emptyList()

                    AnimatedVisibility(
                        visible = subList.isNotEmpty(),
                        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = standardPadding),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(subList) { sub ->
                                    CategoryChip(text = sub, isSelected = selectedSubCategory == sub, onClick = { selectedSubCategory = if (selectedSubCategory == sub) "" else sub }, backgroundColor = Color(0xFFE0E0E0))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = selectedCategory,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200)))
                                .togetherWith(fadeOut(animationSpec = tween(150)))
                        },
                        label = "CategoryTransition"
                    ) { targetCategory ->
                        Column {
                            when (targetCategory) {
                                "숙소" -> {
                                    PaddedContent(standardPadding) { Text("Check-in & Check-out", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = wideBoxPadding).clip(RoundedCornerShape(12.dp)).background(inputGray).padding(16.dp)) {
                                        Column {
                                            Text("Check-in", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                InnerInputBox(accCheckInDay, "Date", { dayPickerTarget="accIn"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                InnerInputBox(accCheckInTime, "Time", { timePickerTarget="accIn"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("Check-out", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                InnerInputBox(accCheckOutDay, "Date", { dayPickerTarget="accOut"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                InnerInputBox(accCheckOutTime, "Time", { timePickerTarget="accOut"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                "교통" -> {
                                    PaddedContent(standardPadding) {
                                        ToggleHeader(title = "Departure & Arrival", isVisible = isTransportVisible) { isTransportVisible = !isTransportVisible }
                                    }
                                    AnimatedVisibility(visible = isTransportVisible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                                        Column {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = wideBoxPadding).clip(RoundedCornerShape(12.dp)).background(inputGray).padding(16.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                    // 1. Departure Column
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Departure", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        InnerInputBox(transDepDay, "Date", { dayPickerTarget="transDep"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        InnerInputBox(transDepTime, "Time", { timePickerTarget="transDep"; showTimePicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        InnerInputBox(depLocation, "Start.", {}, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite, false, { depLocation = it })
                                                    }

                                                    // 2. Center Arrow
                                                    Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                                    }

                                                    // 3. Arrival Column
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Arrival", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        InnerInputBox(transArrDay, "Date", { dayPickerTarget="transArr"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        InnerInputBox(transArrTime, "Time", { timePickerTarget="transArr"; showTimePicker = true }, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        InnerInputBox(arrLocation, "Destination.", {}, boxHeight, fontSize, Modifier.fillMaxWidth(), innerWhite, false, { arrLocation = it })
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }

                                "준비" -> { /* No Content */ }

                                else -> {
                                    PaddedContent(standardPadding) {
                                        Text("Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        InfoTextField(value = generalLocation, onValueChange = { generalLocation = it }, placeholder = "Location", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        ToggleHeader(title = "Date & Time", isVisible = isGeneralTimeVisible) { isGeneralTimeVisible = !isGeneralTimeVisible }
                                    }
                                    AnimatedVisibility(visible = isGeneralTimeVisible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                                        Column {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = wideBoxPadding).clip(RoundedCornerShape(12.dp)).background(inputGray).padding(16.dp)) {
                                                Column {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                        SmallCircleButton(icon = if (isDateRange) Icons.Default.Remove else Icons.Default.Add, onClick = { isDateRange = !isDateRange }, size = 20.dp)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        if (!isDateRange) {
                                                            InnerInputBox(startDay, "Select Day", { dayPickerTarget="start"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                        } else {
                                                            InnerInputBox(startDay, "Start Day", { dayPickerTarget="start"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            InnerInputBox(endDay, "End Day", { dayPickerTarget="end"; showStartDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Time", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                        SmallCircleButton(icon = if (isTimeRange) Icons.Default.Remove else Icons.Default.Add, onClick = { isTimeRange = !isTimeRange }, size = 20.dp)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        if (!isTimeRange) {
                                                            InnerInputBox(startTime, "Select Time", { timePickerTarget="start"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                        } else {
                                                            InnerInputBox(startTime, "Start Time", { timePickerTarget="start"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            InnerInputBox(endTime, "End Time", { timePickerTarget="end"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f), innerWhite)
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PaddedContent(standardPadding) {
                        Text("Cost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f).height(boxHeight).clip(RoundedCornerShape(8.dp)).background(inputGray).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterEnd) {
                                if (amountStr.isEmpty()) { Text("0", style = TextStyle(fontFamily = NotoSansKR, fontSize = fontSize, color = Color.LightGray, textAlign = TextAlign.End), modifier = Modifier.fillMaxWidth()) }
                                BasicTextField(value = amountStr, onValueChange = { if(it.all { c -> c.isDigit() }) amountStr = it }, textStyle = TextStyle(fontFamily = NotoSansKR, fontSize = fontSize, color = Color.Black, textAlign = TextAlign.End), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), singleLine = true, modifier = Modifier.fillMaxWidth(), cursorBrush = SolidColor(Color.Black))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            CurrencySelectButton(currency = currency, onClick = { showCurrencyPicker = true })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Memo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = memo, onValueChange = { memo = it }, placeholder = "Details...", imeAction = ImeAction.Done, height = boxHeight, fontSize = fontSize)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PaddedContent(padding: Dp, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = padding)) { content() }
}

@Composable
fun ToggleHeader(title: String, isVisible: Boolean, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        SmallCircleButton(icon = if (isVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, onClick = onToggle)
    }
}

@Composable
private fun SmallCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, size: Dp = 20.dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape).background(Color.Black).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.6f))
    }
}

@Composable
private fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit, backgroundColor: Color = Color(0xFFF5F5F5)) {
    Surface(color = if (isSelected) Color.Black else backgroundColor, contentColor = if (isSelected) Color.White else Color.Black, shape = RoundedCornerShape(16.dp), modifier = Modifier.clickable { onClick() }) {
        Text(text = text, fontFamily = NotoSansKR, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun InnerInputBox(text: String, placeholder: String, onClick: () -> Unit, height: Dp, fontSize: androidx.compose.ui.unit.TextUnit, modifier: Modifier = Modifier, backgroundColor: Color, isReadOnly: Boolean = true, onValueChange: (String) -> Unit = {}) {
    Box(modifier = modifier.height(height).clip(RoundedCornerShape(8.dp)).background(backgroundColor).clickable(enabled = isReadOnly) { onClick() }.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
        if (isReadOnly) {
            Text(text = if (text.isEmpty()) placeholder else text, fontFamily = NotoSansKR, fontSize = fontSize, color = if (text.isEmpty()) Color.LightGray else Color.Black)
        } else {
            if (text.isEmpty()) { Text(text = placeholder, fontFamily = NotoSansKR, fontSize = fontSize, color = Color.LightGray) }
            BasicTextField(value = text, onValueChange = onValueChange, textStyle = TextStyle(fontFamily = NotoSansKR, fontSize = fontSize, color = Color.Black), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
    }
}