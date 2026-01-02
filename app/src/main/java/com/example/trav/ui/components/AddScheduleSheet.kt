package com.example.trav.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.ui.theme.NotoSansKR
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    onConfirm: (String, String, String, String, String) -> Unit, // endTime 파라미터 추가
    onCancel: () -> Unit
) {
    var time by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("") } // 종료 시간 (기본값 없음)
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    val context = LocalContext.current

    // 시간 선택 다이얼로그
    fun showTimePicker(isStartTime: Boolean) {
        val initialTime = if (isStartTime) time else (if (endTime.isNotBlank()) endTime else time)
        val parts = initialTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formatted = String.format("%02d:%02d", hour, minute)
                if (isStartTime) {
                    time = formatted
                    // 시작 시간이 바뀌었는데 종료 시간이 더 빠르면 초기화
                    if (endTime.isNotBlank() && endTime < formatted) endTime = ""
                } else {
                    endTime = formatted
                }
            },
            h, m, true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(24.dp)
    ) {
        Text("Add New Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR)
        Spacer(modifier = Modifier.height(20.dp))

        // [시간 입력 Row]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 시작 시간 버튼
            OutlinedButton(
                onClick = { showTimePicker(true) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = time, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))

            // 종료 시간 버튼 (없으면 추가 버튼 표시)
            if (endTime.isBlank()) {
                TextButton(onClick = { showTimePicker(false) }) {
                    Text("+ End Time", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { showTimePicker(false) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = endTime, color = Color.Black, fontWeight = FontWeight.Bold)
                }
                // 종료 시간 삭제 버튼
                IconButton(onClick = { endTime = "" }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("Memo (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(time, endTime, title, location, memo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Add", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}