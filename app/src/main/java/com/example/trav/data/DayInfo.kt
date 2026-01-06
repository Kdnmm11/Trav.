package com.example.trav.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_info")
data class DayInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val dayNumber: Int,
    val city: String, // 콤마(,)로 구분된 도시 목록 문자열
    val accommodation: String,

    // [수정] 체크인/아웃 상세 정보 (날짜와 시간 분리)
    val checkInDay: String = "",   // 예: "Day 1"
    val checkInTime: String = "",  // 예: "15:00"
    val checkOutDay: String = "",  // 예: "Day 3"
    val checkOutTime: String = ""  // 예: "11:00"
)