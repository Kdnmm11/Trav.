package com.example.trav.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trav.data.DayInfo
import com.example.trav.data.Schedule
import com.example.trav.data.ScheduleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val scheduleDao: ScheduleDao,
    private val tripId: Int,
    private val dayNumber: Int
) : ViewModel() {

    val schedules: Flow<List<Schedule>> = scheduleDao.getSchedulesForDay(tripId, dayNumber)

    private val _dayInfo = MutableStateFlow<DayInfo?>(null)
    val dayInfo: StateFlow<DayInfo?> = _dayInfo

    init {
        viewModelScope.launch {
            scheduleDao.getDayInfo(tripId, dayNumber).collect {
                _dayInfo.value = it
            }
        }
    }

    // [수정] endTime 파라미터 추가
    fun addSchedule(time: String, endTime: String, title: String, location: String, memo: String) {
        viewModelScope.launch {
            val schedule = Schedule(
                tripId = tripId,
                dayNumber = dayNumber,
                time = time,
                endTime = endTime, // 추가
                title = title,
                location = location,
                memo = memo
            )
            scheduleDao.insertSchedule(schedule)
        }
    }

    // [수정] endTime 파라미터 추가
    fun updateSchedule(schedule: Schedule, time: String, endTime: String, title: String, location: String, memo: String) {
        viewModelScope.launch {
            val updatedSchedule = schedule.copy(
                time = time,
                endTime = endTime, // 추가
                title = title,
                location = location,
                memo = memo
            )
            scheduleDao.updateSchedule(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            scheduleDao.deleteSchedule(schedule)
        }
    }

    fun saveDayInfo(city: String, accommodation: String) {
        viewModelScope.launch {
            val currentInfo = _dayInfo.value
            if (currentInfo == null) {
                val newInfo = DayInfo(tripId = tripId, dayNumber = dayNumber, city = city, accommodation = accommodation)
                scheduleDao.insertDayInfo(newInfo)
            } else {
                scheduleDao.updateDayInfo(currentInfo.copy(city = city, accommodation = accommodation))
            }
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