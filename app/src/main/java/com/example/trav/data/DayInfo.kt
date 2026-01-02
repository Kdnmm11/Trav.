package com.example.trav.data

import androidx.room.Entity

@Entity(tableName = "day_info_table", primaryKeys = ["tripId", "dayNumber"])
data class DayInfo(
    val tripId: Int,
    val dayNumber: Int,
    val city: String = "",
    val accommodation: String = ""
)