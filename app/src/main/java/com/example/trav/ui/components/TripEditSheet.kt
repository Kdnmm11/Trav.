package com.example.trav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun TripEditSheet(
    startViewDay: Int,
    dayNumbers: List<Int>,
    originalDuration: Int, // [신규] After 계산을 위해 필요
    preDays: Int,
    postDays: Int,
    onStartViewDayChange: (Int) -> Unit,
    onIncreasePre: () -> Unit,
    onDecreasePre: () -> Unit,
    onIncreasePost: () -> Unit,
    onDecreasePost: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 20.dp)
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
                Text(
                    text = "Edit Trip Plan",
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 1. 기간 조절
                Text("Extend Trip", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // Before Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Before Trip", fontSize = 14.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold)
                        if (preDays > 0) Text("Added: $preDays days", fontSize = 11.sp, color = Color.Blue)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ControlIconButton(icon = Icons.Default.Remove, onClick = onDecreasePre, enabled = preDays > 0)
                        Spacer(modifier = Modifier.width(12.dp))
                        ControlIconButton(icon = Icons.Default.Add, onClick = onIncreasePre)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))

                // After Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("After Trip", fontSize = 14.sp, fontFamily = NotoSansKR, fontWeight = FontWeight.Bold)
                        if (postDays > 0) Text("Added: $postDays days", fontSize = 11.sp, color = Color.Blue)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ControlIconButton(icon = Icons.Default.Remove, onClick = onDecreasePost, enabled = postDays > 0)
                        Spacer(modifier = Modifier.width(12.dp))
                        ControlIconButton(icon = Icons.Default.Add, onClick = onIncreasePost)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. 시작 뷰 설정 (스크롤 선택)
                Text("View Options (Start from)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(dayNumbers) { dayNum ->
                        // [수정] 라벨 로직 변경 (-1, -2, +1, +2)
                        val label = when {
                            dayNum < 1 -> "${dayNum - 1}" // 0 -> -1, -1 -> -2
                            dayNum > originalDuration -> "+${dayNum - originalDuration}" // 4 -> +1 (if dur=3)
                            else -> "$dayNum"
                        }

                        DaySelectorChip(
                            text = label,
                            isSelected = dayNum == startViewDay,
                            onClick = { onStartViewDayChange(dayNum) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Select the first day to display.", fontSize = 12.sp, color = Color.LightGray, fontFamily = NotoSansKR)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Done", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ControlIconButton(icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    val backgroundColor = if (enabled) Color(0xFFF7F7F7) else Color(0xFFEEEEEE)
    val iconColor = if (enabled) Color.Black else Color.LightGray
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(backgroundColor).clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp)) }
}

@Composable
fun DaySelectorChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isSelected) Color.Black else Color.White).border(1.dp, if (isSelected) Color.Black else Color.LightGray, CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black) }
}