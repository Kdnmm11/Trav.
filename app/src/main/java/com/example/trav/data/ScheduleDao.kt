package com.example.trav.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    // [DayPlanScreen] 특정 날짜의 스케줄 조회
    @Query("SELECT * FROM schedules WHERE tripId = :tripId AND dayNumber = :dayNumber ORDER BY time ASC")
    fun getSchedulesForDay(tripId: Int, dayNumber: Int): Flow<List<Schedule>>

    // [DayPlanScreen] 특정 날짜의 DayInfo 조회 (Flow)
    @Query("SELECT * FROM day_info WHERE tripId = :tripId AND dayNumber = :dayNumber LIMIT 1")
    fun getDayInfo(tripId: Int, dayNumber: Int): Flow<DayInfo?>

    // [수정] ViewModel 로직 처리를 위한 Suspend 조회 함수 추가
    @Query("SELECT * FROM day_info WHERE tripId = :tripId AND dayNumber = :dayNumber LIMIT 1")
    suspend fun getDayInfoSuspend(tripId: Int, dayNumber: Int): DayInfo?

    // [TimeTableScreen] 전체 스케줄 조회
    @Query("SELECT * FROM schedules WHERE tripId = :tripId ORDER BY dayNumber ASC, time ASC")
    fun getAllSchedules(tripId: Int): Flow<List<Schedule>>

    // [TimeTableScreen] 전체 DayInfo 조회
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

    // [수정] 특정 날짜의 숙소 정보만 업데이트하는 쿼리 추가
    @Query("UPDATE day_info SET accommodation = :accommodation WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun updateAccommodation(tripId: Int, dayNumber: Int, accommodation: String)

    // [TripViewModel] 날짜 감소 시 데이터 삭제
    @Query("DELETE FROM schedules WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteSchedulesOnDay(tripId: Int, dayNumber: Int)

    @Query("DELETE FROM day_info WHERE tripId = :tripId AND dayNumber = :dayNumber")
    suspend fun deleteDayInfoOnDay(tripId: Int, dayNumber: Int)
}