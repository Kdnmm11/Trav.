package com.example.trav.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { item ->
            val isSelected = (item == selected)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(item) },
                label = { Text(item, fontSize = 12.sp, fontFamily = NotoSansKR) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Black,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF7F7F7),
                    labelColor = Color.Gray
                ),
                border = null,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun InfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
    height: Dp,
    fontSize: TextUnit,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontSize = fontSize,
            color = Color.Black,
            fontFamily = NotoSansKR
        ),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        singleLine = singleLine,
        maxLines = maxLines,
        cursorBrush = SolidColor(Color.Black),
        decorationBox = { innerTextField ->
            Row(
                modifier = modifier
                    .height(height)
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.LightGray, fontSize = fontSize, fontFamily = NotoSansKR)
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun TimeInputBox(
    time: String,
    onClick: () -> Unit,
    height: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    placeholder: String = "Time"
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F7F7)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (time.isBlank()) placeholder else time,
                fontSize = fontSize,
                color = if (time.isBlank()) Color.LightGray else Color.Black,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (time.isBlank()) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(24.dp),
        shape = CircleShape,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WheelTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = initialTime.split(":").getOrElse(0) { "09" }.toIntOrNull() ?: 9
    val initialMinute = initialTime.split(":").getOrElse(1) { "00" }.toIntOrNull() ?: 0

    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp).clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Select Time", fontFamily = PlayfairDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.height(140.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        WheelPicker(items = (0..23).toList(), initialItem = initialHour, onItemSelected = { selectedHour = it }, itemContent = { item ->
                            Text(text = String.format("%02d", item), fontSize = 22.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        })
                        Text(text = ":", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-2).dp))
                        WheelPicker(items = (0..59).toList(), initialItem = initialMinute, onItemSelected = { selectedMinute = it }, itemContent = { item ->
                            Text(text = String.format("%02d", item), fontSize = 22.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        })
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute)) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WheelEndTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onChangeDayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = initialTime.split(":").getOrElse(0) { "09" }.toIntOrNull() ?: 9
    val initialMinute = initialTime.split(":").getOrElse(1) { "00" }.toIntOrNull() ?: 0

    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp).clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Select Time", fontFamily = PlayfairDisplay, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.height(140.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        WheelPicker(items = (0..23).toList(), initialItem = initialHour, onItemSelected = { selectedHour = it }, itemContent = { item ->
                            Text(text = String.format("%02d", item), fontSize = 22.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        })
                        Text(text = ":", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-2).dp))
                        WheelPicker(items = (0..59).toList(), initialItem = initialMinute, onItemSelected = { selectedMinute = it }, itemContent = { item ->
                            Text(text = String.format("%02d", item), fontSize = 22.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = {
                        onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
                        onChangeDayClick()
                    }) {
                        Text("Different Day?", color = Color.Gray, fontSize = 13.sp, fontFamily = NotoSansKR)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute)) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WheelDayPickerDialog(
    totalDays: Int,
    startDate: String,
    initialDay: String,
    onDaySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val days = (1..totalDays).map { "Day $it" }
    val initialIndex = days.indexOf(initialDay).coerceAtLeast(0)
    var selectedDay by remember { mutableStateOf(days[initialIndex]) }

    val startLocalDate = remember(startDate) {
        try {
            LocalDate.parse(startDate)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    val itemHeight = 66.dp
    val visibleItemsCount = 3
    val wheelTotalHeight = itemHeight * visibleItemsCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 36.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Day",
                        fontFamily = PlayfairDisplay,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    Box(
                        modifier = Modifier.height(wheelTotalHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        WheelPicker(
                            items = days,
                            initialItem = selectedDay,
                            itemHeight = itemHeight,
                            onItemSelected = { selectedDay = it },
                            itemContent = { dayStr ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text(
                                        text = dayStr,
                                        fontSize = 22.sp,
                                        fontFamily = NotoSansKR,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    val dayNum = dayStr.removePrefix("Day ").toIntOrNull() ?: 1
                                    val dateStr = startLocalDate.plusDays((dayNum - 1).toLong())
                                        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = dateStr,
                                        fontSize = 12.sp,
                                        fontFamily = NotoSansKR,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = { onDaySelected(selectedDay) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReadOnlyInputBox(
    text: String,
    placeholder: String,
    onClick: () -> Unit,
    height: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    isHint: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F7F7)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            val isActualHint = isHint || text.isBlank() || text == placeholder

            Text(
                text = if (text.isBlank()) placeholder else text,
                fontSize = fontSize,
                color = if (isActualHint) Color.LightGray else Color.Black,
                fontFamily = NotoSansKR,
                fontWeight = if (isActualHint) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    initialItem: T,
    onItemSelected: (T) -> Unit,
    itemHeight: Dp = 44.dp,
    itemContent: @Composable (T) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = items.indexOf(initialItem))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val visibleItemsCount = 3

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex.coerceIn(0, items.lastIndex)
            onItemSelected(items[centerIndex])
        }
    }

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items.size) { index ->
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    itemContent(items[index])
                }
            }
        }
    }
}