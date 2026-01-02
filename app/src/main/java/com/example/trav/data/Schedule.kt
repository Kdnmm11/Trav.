package com.example.trav.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val dayNumber: Int,
    val time: String,       // 시작 시간 (필수)
    val endTime: String = "", // 종료 시간 (선택, 없으면 빈 문자열)
    val title: String,
    val location: String,
    val memo: String
)