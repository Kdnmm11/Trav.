package com.example.trav.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val dayNumber: Int,
    val time: String,
    val endTime: String = "",
    val title: String,
    val location: String, // 출발지 or 장소
    val memo: String,

    // [예산 및 상세 정보]
    val category: String = "",
    val subCategory: String = "",
    val amount: Double = 0.0,

    // [신규 필드] B안 구현을 위한 필드
    val arrivalPlace: String = "",    // 교통: 도착지
    val reservationNum: String = "",  // 음식/액티비티: 예약번호
    val bookingSource: String = ""    // 액티비티: 예약처(하나투어 등)
)