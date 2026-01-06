package com.example.trav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.trav.data.BudgetCategory
import com.example.trav.data.Schedule
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun BudgetEditSheet(
    schedule: Schedule,
    tripDuration: Int,
    onSave: (Schedule) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val boxHeight = 44.dp
    val fontSize = 14.sp
    val spacing = 16.dp

    var title by remember { mutableStateOf(schedule.title) }
    var costStr by remember { mutableStateOf(if (schedule.amount > 0) schedule.amount.toInt().toString() else "") }
    var memo by remember { mutableStateOf(schedule.memo) }
    var subCategory by remember { mutableStateOf(schedule.subCategory) }

    val category = schedule.category

    val checkInParts = if (category == "숙소") schedule.location.split("|") else listOf()
    val checkOutParts = if (category == "숙소") schedule.subCategory.split("|") else listOf()

    var source by remember { mutableStateOf(schedule.bookingSource) }
    var currency by remember { mutableStateOf("KRW") }

    var checkInDay by remember { mutableStateOf(checkInParts.getOrNull(0) ?: "Day 1") }
    var checkInTime by remember { mutableStateOf(checkInParts.getOrNull(1) ?: "15:00") }
    var checkOutDay by remember { mutableStateOf(checkOutParts.getOrNull(0) ?: "Day 1") }
    var checkOutTime by remember { mutableStateOf(checkOutParts.getOrNull(1) ?: "11:00") }

    var location by remember { mutableStateOf(schedule.location) }
    var time by remember { mutableStateOf(schedule.time) }

    var showCheckInDayPicker by remember { mutableStateOf(false) }
    var showCheckOutDayPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    var timePickerTarget by remember { mutableStateOf("time") }

    if (showCheckInDayPicker) WheelDayPickerDialog(tripDuration, checkInDay, { checkInDay = it; showCheckInDayPicker = false }, { showCheckInDayPicker = false })
    if (showCheckOutDayPicker) WheelDayPickerDialog(tripDuration, checkOutDay, { checkOutDay = it; showCheckOutDayPicker = false }, { showCheckOutDayPicker = false })

    if (showTimePicker) {
        val initial = when(timePickerTarget) {
            "checkIn" -> checkInTime
            "checkOut" -> checkOutTime
            else -> if(time.isBlank()) "09:00" else time
        }
        WheelTimePickerDialog(initial, {
            when(timePickerTarget) {
                "checkIn" -> checkInTime = it
                "checkOut" -> checkOutTime = it
                else -> time = it
            }
            showTimePicker = false
        }, { showTimePicker = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showCurrencyPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .zIndex(2f)
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
                    CurrencyPickerContent(
                        onSelected = { currency = it; showCurrencyPicker = false }
                    )
                }
            }
        }

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
                        .padding(24.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edit", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                        Row {
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(32.dp).padding(end = 8.dp)
                            ) { Text("DELETE", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp) }

                            Button(
                                onClick = {
                                    val finalAmount = costStr.toDoubleOrNull() ?: 0.0
                                    val updatedSchedule = when (category) {
                                        "숙소" -> schedule.copy(
                                            title = title, amount = finalAmount, bookingSource = source,
                                            location = "$checkInDay|$checkInTime", subCategory = "$checkOutDay|$checkOutTime", memo = memo
                                        )
                                        else -> schedule.copy(
                                            title = title, amount = finalAmount, location = location,
                                            time = time, subCategory = subCategory, memo = memo
                                        )
                                    }
                                    onSave(updatedSchedule)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                modifier = Modifier.height(32.dp)
                            ) { Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) }
                        }
                    }

                    if (category == "숙소") {
                        Text("Accommodation Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = title, onValueChange = { title = it }, placeholder = "Hotel Name", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Booking Source", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = source, onValueChange = { source = it }, placeholder = "e.g. Agoda", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Cost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InfoTextField(
                                value = costStr,
                                onValueChange = { if(it.all { c -> c.isDigit() }) costStr = it },
                                placeholder = "0",
                                imeAction = ImeAction.Next,
                                height = boxHeight,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CurrencySelectButton(currency = currency, onClick = { showCurrencyPicker = true })
                        }

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Check-in", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ReadOnlyInputBox(checkInDay, "Day", { showCheckInDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            ReadOnlyInputBox(checkInTime, "Time", { timePickerTarget = "checkIn"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Check-out", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ReadOnlyInputBox(checkOutDay, "Day", { showCheckOutDayPicker = true }, boxHeight, fontSize, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            ReadOnlyInputBox(checkOutTime, "Time", { timePickerTarget = "checkOut"; showTimePicker = true }, boxHeight, fontSize, Modifier.weight(1f))
                        }

                    } else if (category == "준비") {
                        Text("Item Name", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = title, onValueChange = { title = it }, placeholder = "Item Name", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        val subList = BudgetCategory.subCategories["준비"] ?: emptyList()
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            subList.forEach { sub ->
                                CategoryChip(text = sub, isSelected = subCategory == sub, onClick = { subCategory = sub })
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Cost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InfoTextField(
                                value = costStr,
                                onValueChange = { if(it.all { c -> c.isDigit() }) costStr = it },
                                placeholder = "0",
                                imeAction = ImeAction.Next,
                                height = boxHeight,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CurrencySelectButton(currency = currency, onClick = { showCurrencyPicker = true })
                        }

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Memo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = memo, onValueChange = { memo = it }, placeholder = "Details...", imeAction = ImeAction.Done, height = boxHeight, fontSize = fontSize)

                    } else {
                        // General
                        Text("Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = title, onValueChange = { title = it }, placeholder = "Title", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Category: $category", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        val subList = BudgetCategory.subCategories[category] ?: emptyList()
                        if (subList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.layout.FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subList.forEach { sub ->
                                    CategoryChip(text = sub, isSelected = subCategory == sub, onClick = { subCategory = if(subCategory==sub) "" else sub })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Location / Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = location, onValueChange = { location = it }, placeholder = "Location", imeAction = ImeAction.Next, height = boxHeight, fontSize = fontSize)

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        ReadOnlyInputBox(if(time.isBlank()) "Select Time" else time, "Time", { timePickerTarget="time"; showTimePicker = true }, boxHeight, fontSize)

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Cost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InfoTextField(
                                value = costStr,
                                onValueChange = { if(it.all { c -> c.isDigit() }) costStr = it },
                                placeholder = "0",
                                imeAction = ImeAction.Next,
                                height = boxHeight,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CurrencySelectButton(currency = currency, onClick = { showCurrencyPicker = true })
                        }

                        Spacer(modifier = Modifier.height(spacing))

                        Text("Memo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoTextField(value = memo, onValueChange = { memo = it }, placeholder = "Details...", imeAction = ImeAction.Done, height = boxHeight, fontSize = fontSize)
                    }
                }
            }
        }
    }
}

// [복구] CurrencySelectButton 정의
@Composable
fun CurrencySelectButton(currency: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(text = currency, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

// [복구] CurrencyPickerContent 정의
@Composable
fun CurrencyPickerContent(
    onSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCurrencies = remember(searchQuery) {
        BudgetCategory.currencyMap.filter { (code, country) ->
            code.contains(searchQuery, ignoreCase = true) || country.contains(searchQuery, ignoreCase = true)
        }.toList()
    }

    Column(modifier = Modifier.padding(20.dp)) {
        Text("Select Currency", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = TextStyle(fontFamily = NotoSansKR, fontSize = 14.sp, color = Color.Black),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("Search", fontSize = 14.sp, color = Color.LightGray, fontFamily = NotoSansKR)
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(filteredCurrencies) { (code, country) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(code) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(country, fontFamily = NotoSansKR, fontSize = 14.sp, color = Color.Black)
                    Text(code, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

// [복구] CategoryChip 정의
@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color.Black else Color(0xFFF5F5F5),
        contentColor = if (isSelected) Color.White else Color.Black,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontFamily = NotoSansKR,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}