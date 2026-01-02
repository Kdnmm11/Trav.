package com.example.trav.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// [수정] version 7로 변경 (앱 삭제 후 재설치 권장)
@Database(entities = [Trip::class, Schedule::class, DayInfo::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trav_database"
                )
                    .fallbackToDestructiveMigration() // 스키마 변경 시 기존 데이터 삭제하고 재생성
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}