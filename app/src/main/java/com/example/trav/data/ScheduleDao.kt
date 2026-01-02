package com.example.trav.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    // ----------------------------------------------------
    // Schedule 관련
    // ----------------------------------------------------

    // [수정] 함수 이름 변경: getSchedules -> getSchedulesForDay
    @Query("SELECT * FROM schedules WHERE tripId = :tripId AND dayNumber = :dayNumber ORDER BY time ASC")
    fun getSchedulesForDay(tripId: Int, dayNumber: Int): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE tripId = :tripId ORDER BY dayNumber ASC, time ASC")
    fun getAllSchedules(tripId: Int): Flow<List<Schedule>>

    // [수정] 함수 이름 변경: insert -> insertSchedule
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule)

    // [수정] 함수 이름 변경: update -> updateSchedule
    @Update
    suspend fun updateSchedule(schedule: Schedule)

    // [수정] 함수 이름 변경: delete -> deleteSchedule
    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Query("DELETE FROM schedules WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteSchedulesOnDay(tripId: Int, dayNumber: Int)


    // ----------------------------------------------------
    // DayInfo (도시, 숙소) 관련
    // ----------------------------------------------------

    @Query("SELECT * FROM day_info_table WHERE tripId = :tripId AND dayNumber = :dayNumber LIMIT 1")
    fun getDayInfo(tripId: Int, dayNumber: Int): Flow<DayInfo?>

    @Query("SELECT * FROM day_info_table WHERE tripId = :tripId")
    fun getAllDayInfos(tripId: Int): Flow<List<DayInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayInfo(dayInfo: DayInfo)

    // [신규] ViewModel 오류 해결을 위해 추가
    @Update
    suspend fun updateDayInfo(dayInfo: DayInfo)

    @Query("DELETE FROM day_info_table WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteDayInfoOnDay(tripId: Int, dayNumber: Int)
}