package com.example.trav.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trav.data.ScheduleDao
import com.example.trav.data.Trip
import com.example.trav.data.TripDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TripViewModel(
    private val tripDao: TripDao,
    private val scheduleDao: ScheduleDao
) : ViewModel() {

    val tripList: Flow<List<Trip>> = tripDao.getAllTrips()

    fun getTrip(id: Int): Flow<Trip> = tripDao.getTrip(id)

    fun addTrip(title: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val trip = Trip(title = title, startDate = startDate, endDate = endDate)
            tripDao.insertTrip(trip)
        }
    }

    // [중요] 화면 시작 위치(Day) 저장
    fun saveStartViewDay(trip: Trip, dayNum: Int) {
        viewModelScope.launch {
            // 변경된 Day 위치를 DB에 업데이트
            tripDao.updateTrip(trip.copy(startViewDay = dayNum))
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.updateTrip(trip)
        }
    }

    fun increasePreDays(trip: Trip) {
        viewModelScope.launch {
            val newPre = trip.preDays + 1
            // 날짜가 늘어나면 시작 위치도 조정
            val minDay = 1 - newPre
            val newStartView = if (trip.startViewDay > minDay) trip.startViewDay - 1 else minDay
            tripDao.updateTrip(trip.copy(preDays = newPre, startViewDay = newStartView))
        }
    }

    fun decreasePreDays(trip: Trip) {
        if (trip.preDays > 0) {
            viewModelScope.launch {
                val dayToDelete = 1 - trip.preDays
                // [오류 해결] ScheduleDao에 함수 추가됨
                scheduleDao.deleteSchedulesOnDay(trip.id, dayToDelete)
                scheduleDao.deleteDayInfoOnDay(trip.id, dayToDelete)

                val newStartView = trip.startViewDay + 1
                tripDao.updateTrip(trip.copy(preDays = trip.preDays - 1, startViewDay = newStartView))
            }
        }
    }

    fun increasePostDays(trip: Trip) {
        viewModelScope.launch {
            tripDao.updateTrip(trip.copy(postDays = trip.postDays + 1))
        }
    }

    fun decreasePostDays(trip: Trip, originalDuration: Int) {
        if (trip.postDays > 0) {
            viewModelScope.launch {
                val dayToDelete = originalDuration + trip.postDays
                // [오류 해결] ScheduleDao에 함수 추가됨
                scheduleDao.deleteSchedulesOnDay(trip.id, dayToDelete)
                scheduleDao.deleteDayInfoOnDay(trip.id, dayToDelete)
                tripDao.updateTrip(trip.copy(postDays = trip.postDays - 1))
            }
        }
    }
}

class TripViewModelFactory(
    private val tripDao: TripDao,
    private val scheduleDao: ScheduleDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripDao, scheduleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}