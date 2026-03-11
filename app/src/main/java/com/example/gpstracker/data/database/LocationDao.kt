package com.example.gpstracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocationPoint(point: LocationPoint)

    @Query("SELECT * FROM location_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getLocationPointsForSession(sessionId: Long): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getLocationPointsForSessionSync(sessionId: Long): List<LocationPoint>

    @Query("SELECT MAX(sessionId) FROM location_points")
    suspend fun getLatestSessionId(): Long?

    @Query("DELETE FROM location_points")
    suspend fun clearAll()
}
