package com.example.trav.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trav.data.Schedule
import com.example.trav.data.ScheduleDao
import com.example.trav.data.TripDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BudgetViewModel(
    private val scheduleDao: ScheduleDao,
    private val tripDao: TripDao,
    private val tripId: Int
) : ViewModel() {

    private val _schedules = scheduleDao.getAllSchedules(tripId)
    private val _dayInfos = scheduleDao.getAllDayInfos(tripId)
    private val _trip = tripDao.getTrip(tripId)

    val allSchedules: Flow<List<Schedule>> = combine(_schedules, _dayInfos, _trip) { schedules, dayInfos, trip ->

        val maxDay = trip?.let {
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)
            val duration = ChronoUnit.DAYS.between(start, end).toInt() + 1
            duration + it.preDays + it.postDays
        } ?: 365

        // 숙소 비용/예약처 정보 (Schedule 테이블에서 가져옴)
        val costItems = schedules
            .filter { it.category == "숙소" }
            .associateBy { it.title }

        // DayInfo 가공 (숙소 리스트)
        val accommodationItems = dayInfos
            .asSequence()
            .filter { it.accommodation.isNotBlank() && it.dayNumber <= maxDay }
            .groupBy { it.accommodation }
            .map { (name, infoList) ->
                val distinctDays = infoList.map { it.dayNumber }.distinct().sorted()

                val matchedSchedule = costItems[name]
                val cost = matchedSchedule?.amount ?: 0.0
                val source = matchedSchedule?.bookingSource ?: ""

                val firstInfo = infoList.minByOrNull { it.dayNumber }!!

                // [수정] memo에서 ($dayCount nights) 자동 삽입 로직 제거
                Schedule(
                    tripId = tripId,
                    dayNumber = distinctDays.firstOrNull() ?: 1,
                    time = "",
                    title = name,
                    location = "${firstInfo.checkInDay}|${firstInfo.checkInTime}",
                    memo = "", // nights 표시 제거
                    category = "숙소",
                    subCategory = "${firstInfo.checkOutDay}|${firstInfo.checkOutTime}",
                    amount = cost,
                    bookingSource = source
                )
            }
            .toList()

        // 나머지 스케줄 (숙소 제외)
        val otherSchedules = schedules.filter { it.category != "숙소" }

        // "준비" 카테고리는 otherSchedules에 포함되어 자동으로 나옴
        otherSchedules + accommodationItems
    }

    val totalExpense: Flow<Double> = allSchedules.map { list ->
        list.sumOf { it.amount }
    }

    // 일반 지출 추가
    fun addExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            scheduleDao.insertSchedule(
                Schedule(
                    tripId = tripId,
                    dayNumber = -1,
                    time = "",
                    title = title,
                    location = "",
                    memo = "",
                    category = category,
                    amount = amount
                )
            )
        }
    }

    // [통합 업데이트 함수]
    fun updateBudgetItem(item: Schedule, oldTitle: String? = null) {
        viewModelScope.launch {
            if (item.category == "숙소") {
                val currentTitle = item.title
                val effectiveOldTitle = oldTitle ?: currentTitle

                // 이름이 바뀌었다면 DB 내의 모든 관련 명칭 일괄 업데이트
                if (effectiveOldTitle != currentTitle) {
                    scheduleDao.updateAccommodationScheduleTitle(tripId, effectiveOldTitle, currentTitle)
                    scheduleDao.updateAllAccommodationNames(tripId, effectiveOldTitle, currentTitle)
                }

                val checkInParts = item.location.split("|")
                val checkOutParts = item.subCategory.split("|")

                val checkInDay = checkInParts.getOrNull(0) ?: ""
                val checkInTime = checkInParts.getOrNull(1) ?: ""
                val checkOutDay = checkOutParts.getOrNull(0) ?: ""
                val checkOutTime = checkOutParts.getOrNull(1) ?: ""

                // 비용 업데이트 (Schedule 테이블)
                val existingCost = _schedules.first().find { it.category == "숙소" && it.title == currentTitle }
                if (existingCost != null) {
                    scheduleDao.updateSchedule(existingCost.copy(amount = item.amount, bookingSource = item.bookingSource))
                } else {
                    scheduleDao.insertSchedule(
                        Schedule(
                            tripId = tripId, dayNumber = -1, time = "",
                            title = currentTitle, location = "", memo = "Cost",
                            category = "숙소", subCategory = "",
                            amount = item.amount, bookingSource = item.bookingSource
                        )
                    )
                }

                // DayInfo (체크인/아웃) 업데이트
                val currentDayInfos = _dayInfos.first().filter { it.accommodation == currentTitle }
                currentDayInfos.forEach {
                    scheduleDao.insertDayInfo(it.copy(
                        checkInDay = checkInDay, checkInTime = checkInTime,
                        checkOutDay = checkOutDay, checkOutTime = checkOutTime
                    ))
                }

            } else {
                // [일반/준비] 단순 Schedule 업데이트
                if (item.id != 0) {
                    scheduleDao.updateSchedule(item)
                } else {
                    scheduleDao.insertSchedule(item)
                }
            }
        }
    }

    // [수정] 삭제 시 DayInfo와 연동하여 삭제
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            if (schedule.category == "숙소") {
                // 1. 예산(Schedule) 테이블에서 해당 숙소 비용 정보 삭제
                val costItem = _schedules.first().find { it.category == "숙소" && it.title == schedule.title }
                if (costItem != null) {
                    scheduleDao.deleteSchedule(costItem)
                }
                // 2. 일정(DayInfo) 테이블에서 해당 숙소 이름을 빈 값으로 초기화 (항목 삭제 효과)
                scheduleDao.clearAccommodationByName(tripId, schedule.title)
            } else {
                scheduleDao.deleteSchedule(schedule)
            }
        }
    }
}

class BudgetViewModelFactory(
    private val scheduleDao: ScheduleDao,
    private val tripDao: TripDao,
    private val tripId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(scheduleDao, tripDao, tripId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}