package com.example.shakeup.data.repository

import com.example.shakeup.data.dao.AlarmDao
import com.example.shakeup.data.entity.AlarmEntity
import com.example.shakeup.domain.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {

    fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toDomain() }
    }

    suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    fun observeAlarmById(id: Long): Flow<Alarm?> {
        return alarmDao.observeAlarmById(id).map { it?.toDomain() }
    }

    suspend fun saveAlarm(alarm: Alarm): Long {
        val entity = AlarmEntity.fromDomain(alarm)
        return if (entity.id == 0L) {
            alarmDao.insertAlarm(entity)
        } else {
            alarmDao.updateAlarm(entity)
            entity.id
        }
    }

    suspend fun toggleAlarmEnabled(id: Long, isEnabled: Boolean) {
        val alarm = alarmDao.getAlarmById(id) ?: return
        alarmDao.updateAlarm(alarm.copy(isEnabled = isEnabled))
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(AlarmEntity.fromDomain(alarm))
    }

    suspend fun deleteAlarmById(id: Long) {
        alarmDao.deleteAlarmById(id)
    }
}
