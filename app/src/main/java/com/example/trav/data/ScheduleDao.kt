package com.example.trav.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    // [DayPlanScreen] 특정 날짜의 스케줄 조회
    @Query("SELECT * FROM schedules WHERE tripId = :tripId AND dayNumber = :dayNumber ORDER BY time ASC")
    fun getSchedulesForDay(tripId: Int, dayNumber: Int): Flow<List<Schedule>>

    // [DayPlanScreen] 특정 날짜의 DayInfo 조회
    @Query("SELECT * FROM day_info WHERE tripId = :tripId AND dayNumber = :dayNumber LIMIT 1")
    fun getDayInfo(tripId: Int, dayNumber: Int): Flow<DayInfo?>

    // [TimeTableScreen] 전체 스케줄 조회 (누락되었던 함수 추가)
    @Query("SELECT * FROM schedules WHERE tripId = :tripId ORDER BY dayNumber ASC, time ASC")
    fun getAllSchedules(tripId: Int): Flow<List<Schedule>>

    // [TimeTableScreen] 전체 DayInfo 조회 (누락되었던 함수 추가)
    @Query("SELECT * FROM day_info WHERE tripId = :tripId ORDER BY dayNumber ASC")
    fun getAllDayInfos(tripId: Int): Flow<List<DayInfo>>

    // [기본 CRUD]
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule)

    @Update
    suspend fun updateSchedule(schedule: Schedule)

    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayInfo(dayInfo: DayInfo)

    // [TripViewModel] 날짜 감소 시 데이터 삭제 (누락되었던 함수 추가)
    @Query("DELETE FROM schedules WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteSchedulesOnDay(tripId: Int, dayNumber: Int)

    @Query("DELETE FROM day_info WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteDayInfoOnDay(tripId: Int, dayNumber: Int)
}