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
import com.example.trav.data.Schedule
import com.example.trav.ui.theme.NotoSansKR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleSheet(
    schedule: Schedule,
    onUpdate: (String, String, String, String, String) -> Unit, // endTime 추가
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var time by remember { mutableStateOf(schedule.time) }
    var endTime by remember { mutableStateOf(schedule.endTime) }
    var title by remember { mutableStateOf(schedule.title) }
    var location by remember { mutableStateOf(schedule.location) }
    var memo by remember { mutableStateOf(schedule.memo) }

    val context = LocalContext.current

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
        Text("Edit Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansKR)
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { showTimePicker(true) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = time, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))

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
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = memo,
            onValueChange = { memo = it },
            label = { Text("Memo") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete")
            }

            Row {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onUpdate(time, endTime, title, location, memo)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}