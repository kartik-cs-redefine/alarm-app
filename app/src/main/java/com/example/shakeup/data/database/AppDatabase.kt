package com.example.shakeup.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shakeup.data.converter.Converters
import com.example.shakeup.data.dao.AlarmDao
import com.example.shakeup.data.entity.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
