package com.example.trav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CustomCalendarDialog(
    initialStartDate: LocalDate?,
    initialEndDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedStartDate by remember { mutableStateOf(initialStartDate) }
    var selectedEndDate by remember { mutableStateOf(initialEndDate) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp), // 직각 디자인 유지
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 헤더 타이틀
                Text(
                    text = "Select Dates",
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 월 이동 네비게이션
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.Black)
                    }
                    Text(
                        text = "${currentMonth.year}. ${currentMonth.monthValue}",
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 요일 헤더 (S M T W T F S)
                Row(modifier = Modifier.fillMaxWidth()) {
                    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontFamily = NotoSansKR,
                            color = if (day == "S") Color.Red.copy(alpha = 0.5f) else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 달력 그리드
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
                    onDateSelected = { date ->
                        if (selectedStartDate == null || (selectedStartDate != null && selectedEndDate != null)) {
                            selectedStartDate = date
                            selectedEndDate = null
                        } else if (date.isBefore(selectedStartDate)) {
                            selectedStartDate = date
                        } else {
                            selectedEndDate = date
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 하단 버튼 (CANCEL / CONFIRM)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray, fontFamily = NotoSansKR, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedStartDate != null && selectedEndDate != null) {
                                onConfirm(selectedStartDate!!, selectedEndDate!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        enabled = selectedStartDate != null && selectedEndDate != null,
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text("CONFIRM", fontFamily = NotoSansKR, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    // 자바 time의 요일(월=1..일=7)을 일=0..토=6으로 변환
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
    val totalCells = daysInMonth + firstDayOfWeek

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(240.dp)
    ) {
        items(totalCells) { index ->
            if (index < firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                val day = index - firstDayOfWeek + 1
                val date = currentMonth.atDay(day)
                val isSelected = date == selectedStartDate || date == selectedEndDate
                val isInRange = selectedStartDate != null && selectedEndDate != null &&
                        date.isAfter(selectedStartDate) && date.isBefore(selectedEndDate)
                val isToday = date == LocalDate.now()

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> Color.Black
                                isInRange -> Color.LightGray.copy(alpha = 0.5f)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        fontFamily = NotoSansKR,
                        color = when {
                            isSelected -> Color.White
                            else -> Color.Black
                        },
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}