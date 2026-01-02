package com.example.trav.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_table")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startDate: String,
    val endDate: String,
    val preDays: Int = 0,
    val postDays: Int = 0,
    val startViewDay: Int = 1 // [신규] 시작 뷰 위치 저장 (기본값 1)
)