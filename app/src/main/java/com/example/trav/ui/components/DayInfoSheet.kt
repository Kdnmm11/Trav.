package com.example.trav.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun DayInfoSheet(
    initialCity: String,
    initialStay: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current // [신규] 토스트 메시지용 Context
    val initialCityList = if (initialCity.isBlank()) listOf("") else initialCity.split(" > ")
    val cityList = remember { mutableStateListOf<String>().apply { addAll(initialCityList) } }
    var stay by remember { mutableStateOf(initialStay) }

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
                    text = "Day Info",
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 1. City 섹션
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("City", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // + 버튼
                    IconButton(
                        onClick = { cityList.add("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add City", tint = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // 도시 입력 리스트
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    cityList.forEachIndexed { index, city ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // [수정] 이름 변경된 InfoTextField 사용
                            InfoTextField(
                                value = city,
                                onValueChange = { cityList[index] = it },
                                placeholder = "City ${index + 1}",
                                imeAction = ImeAction.Next,
                                modifier = Modifier.weight(1f) // 버튼 자리 확보를 위해 weight 적용
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // [수정] 삭제 버튼 로직 변경
                            IconButton(
                                onClick = {
                                    if (cityList.size > 1) {
                                        cityList.removeAt(index)
                                    } else {
                                        // 1개 남았을 때 누르면 안내 메시지
                                        Toast.makeText(context, "최소 1개의 도시는 유지해야 합니다.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = "Remove", tint = Color.LightGray)
                            }
                        }
                    }
                }

                if (cityList.any { it.isNotBlank() }) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Route: " + cityList.filter { it.isNotBlank() }.joinToString(" > "),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = NotoSansKR
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Accommodation 섹션
                Text("Accommodation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // [수정] 이름 변경된 InfoTextField 사용
                InfoTextField(
                    value = stay,
                    onValueChange = { stay = it },
                    placeholder = "Hotel / Airbnb Name",
                    imeAction = ImeAction.Done
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        val finalCityString = cityList.filter { it.isNotBlank() }.joinToString(" > ")
                        onSave(finalCityString, stay)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Info", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// [수정] 함수 이름을 InfoTextField로 변경하여 충돌 해결
@Composable
private fun InfoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF7F7F7),
            unfocusedContainerColor = Color(0xFFF7F7F7),
            disabledContainerColor = Color(0xFFF7F7F7),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        singleLine = true
    )
}