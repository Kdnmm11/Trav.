package com.example.trav.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trav.data.DayInfo
import com.example.trav.data.Schedule
import com.example.trav.data.ScheduleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val scheduleDao: ScheduleDao,
    private val tripId: Int,
    private val dayNumber: Int
) : ViewModel() {

    val schedules: Flow<List<Schedule>> = scheduleDao.getSchedulesForDay(tripId, dayNumber)
    val dayInfo: Flow<DayInfo?> = scheduleDao.getDayInfo(tripId, dayNumber)

    fun addSchedule(
        time: String, endTime: String, title: String, location: String, memo: String,
        category: String, subCategory: String, amount: Double,
        arrivalPlace: String, reservationNum: String, bookingSource: String
    ) {
        viewModelScope.launch {
            scheduleDao.insertSchedule(
                Schedule(
                    tripId = tripId, dayNumber = dayNumber,
                    time = time, endTime = endTime, title = title, location = location, memo = memo,
                    category = category, subCategory = subCategory, amount = amount,
                    arrivalPlace = arrivalPlace, reservationNum = reservationNum, bookingSource = bookingSource
                )
            )
        }
    }

    fun updateSchedule(
        schedule: Schedule,
        time: String, endTime: String, title: String, location: String, memo: String,
        category: String, subCategory: String, amount: Double,
        arrivalPlace: String, reservationNum: String, bookingSource: String
    ) {
        viewModelScope.launch {
            scheduleDao.updateSchedule(
                schedule.copy(
                    time = time, endTime = endTime, title = title, location = location, memo = memo,
                    category = category, subCategory = subCategory, amount = amount,
                    arrivalPlace = arrivalPlace, reservationNum = reservationNum, bookingSource = bookingSource
                )
            )
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch { scheduleDao.deleteSchedule(schedule) }
    }

    // [수정] DayInfo 저장 (숙소 이름 변경 시 Budget 연동 문제 해결)
    fun saveDayInfo(
        city: String,
        accommodation: String,
        checkInDay: String,
        checkInTime: String,
        checkOutDay: String,
        checkOutTime: String
    ) {
        viewModelScope.launch {
            // 1. 기존 데이터 조회
            val existingInfo = scheduleDao.getDayInfoSuspend(tripId, dayNumber)
            val currentId = existingInfo?.id ?: 0
            val oldAccommodation = existingInfo?.accommodation ?: ""

            // [추가] 숙소 이름이 변경된 경우, 예산(Schedule) 테이블의 이름도 함께 업데이트하여 중복 생성 방지
            if (oldAccommodation.isNotBlank() && accommodation.isNotBlank() && oldAccommodation != accommodation) {
                // 예산 항목의 이름 변경
                scheduleDao.updateAccommodationScheduleTitle(tripId, oldAccommodation, accommodation)
                // 다른 날짜에 입력된 동일 숙소명도 일괄 변경
                scheduleDao.updateAllAccommodationNames(tripId, oldAccommodation, accommodation)
            }

            // 2. 현재 날짜 DayInfo 저장
            val info = DayInfo(
                id = currentId, // 기존 ID 유지
                tripId = tripId,
                dayNumber = dayNumber,
                city = city,
                accommodation = accommodation,
                checkInDay = checkInDay,
                checkInTime = checkInTime,
                checkOutDay = checkOutDay,
                checkOutTime = checkOutTime
            )
            scheduleDao.insertDayInfo(info)

            // 3. 체크인/아웃 기간 숙소 자동 입력 로직 (기존과 동일)
            val startDay = parseDayNumber(checkInDay)
            val endDay = parseDayNumber(checkOutDay)

            if (startDay != null && endDay != null && startDay < endDay) {
                for (i in startDay until endDay) {
                    if (i == dayNumber) continue

                    val targetInfo = scheduleDao.getDayInfoSuspend(tripId, i)
                    if (targetInfo != null) {
                        scheduleDao.updateAccommodation(tripId, i, accommodation)
                    } else {
                        scheduleDao.insertDayInfo(
                            DayInfo(tripId = tripId, dayNumber = i, city = "", accommodation = accommodation)
                        )
                    }
                }
            }
        }
    }

    private fun parseDayNumber(dayString: String): Int? {
        return try {
            if (dayString.startsWith("Day ")) {
                dayString.removePrefix("Day ").trim().toInt()
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}

class ScheduleViewModelFactory(
    private val scheduleDao: ScheduleDao,
    private val tripId: Int,
    private val dayNumber: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(scheduleDao, tripId, dayNumber) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}