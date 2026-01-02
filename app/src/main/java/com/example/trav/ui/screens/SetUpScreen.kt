package com.example.trav.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.ui.components.CustomCalendarDialog
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SetupScreen(onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var showCustomCalendar by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        // 타이틀
        Text(
            text = "Plan your trip.",
            fontFamily = PlayfairDisplay,
            fontSize = 42.sp,
            color = Color.Black,
            lineHeight = 50.sp
        )
        Spacer(modifier = Modifier.height(60.dp))

        // 1. 제목 입력
        MinimalTextField(
            value = title,
            onValueChange = { title = it },
            label = "Where are you going?",
            placeholder = "Enter city name"
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 2. 날짜 입력
        Box(modifier = Modifier.fillMaxWidth()) {
            val dateText = if (startDate != null && endDate != null) {
                "${startDate!!.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"))} — ${endDate!!.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"))}"
            } else {
                ""
            }

            MinimalTextField(
                value = dateText,
                onValueChange = {},
                label = "When is the date?",
                placeholder = "Select dates",
                readOnly = true,
                showIcon = true
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showCustomCalendar = true }
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        // 저장 버튼 (수정된 부분)
        Button(
            onClick = {
                if (title.isNotEmpty() && startDate != null && endDate != null) {
                    onSave(
                        title,
                        startDate!!.format(DateTimeFormatter.ISO_DATE),
                        endDate!!.format(DateTimeFormatter.ISO_DATE)
                    )
                }
            },
            enabled = title.isNotEmpty() && startDate != null && endDate != null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally) // 중앙 정렬
                .width(220.dp) // 너비 고정 (작게)
                .height(50.dp), // 높이 줄임
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFF0F0F0),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(0.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                "CREATE JOURNEY",
                fontFamily = NotoSansKR,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp, // 폰트 사이즈도 살짝 줄임
                letterSpacing = 1.sp
            )
        }
    }

    if (showCustomCalendar) {
        CustomCalendarDialog(
            initialStartDate = startDate,
            initialEndDate = endDate,
            onDismiss = { showCustomCalendar = false },
            onConfirm = { start, end ->
                startDate = start
                endDate = end
                showCustomCalendar = false
            }
        )
    }
}

@Composable
fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    readOnly: Boolean = false,
    showIcon: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = NotoSansKR,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.LightGray
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly,
                    textStyle = TextStyle(
                        fontFamily = NotoSansKR,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = SolidColor(Color.Black),
                    singleLine = true
                )
            }

            if (showIcon) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(Color.Black)
        )
    }
}