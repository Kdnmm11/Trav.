package com.example.trav.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trav.data.Trip
import com.example.trav.ui.theme.NotoSansKR
import com.example.trav.ui.theme.PlayfairDisplay

@Composable
fun TripListScreen(
    tripList: List<Trip>,
    onAddClick: () -> Unit,
    onTripClick: (Trip) -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddClick,
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(70.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "여행 추가", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 헤더
            Text(
                text = "TRAV.",
                fontFamily = PlayfairDisplay,
                fontSize = 48.sp,
                color = Color.Black,
                letterSpacing = (-1).sp
            )
            // 서브 타이틀 변경
            Text(
                text = "Travel Planner",
                fontFamily = NotoSansKR,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(tripList) { trip ->
                    // 카드 디자인
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { onTripClick(trip) }, // 수정: 여기서 클릭 처리
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black
                        ),
                        shape = RoundedCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 텍스트 정보
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                // 소제목
                                Text(
                                    text = "TRIP TO",
                                    fontFamily = NotoSansKR,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f),
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // 여행 제목
                                Text(
                                    text = trip.title.uppercase(),
                                    fontFamily = PlayfairDisplay,
                                    fontSize = 28.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 날짜 (수정: 가시성 확보)
                                Text(
                                    text = "${trip.startDate} ~ ${trip.endDate}",
                                    fontFamily = NotoSansKR,
                                    color = Color.White, // 완전한 흰색으로 변경
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // 화살표 아이콘
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}