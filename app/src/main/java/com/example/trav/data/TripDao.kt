package com.example.trav.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_table ORDER BY id DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trip_table WHERE id = :id")
    fun getTrip(id: Int): Flow<Trip>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    // [핵심] 여행 날짜 수정을 위한 업데이트 함수
    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("DELETE FROM trip_table")
    suspend fun deleteAll()
}