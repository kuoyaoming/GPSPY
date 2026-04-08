package com.gpsspy.gpstracker.data.database.routine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineAnalysisDao {

    @Insert
    suspend fun insert(analysis: RoutineAnalysis): Long

    @Update
    suspend fun update(analysis: RoutineAnalysis)

    @Query("SELECT * FROM routine_analysis ORDER BY startTime DESC")
    fun getAllAnalyses(): Flow<List<RoutineAnalysis>>

    @Query("SELECT * FROM routine_analysis WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentState(): RoutineAnalysis?

    @Query("SELECT * FROM routine_analysis WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime ASC")
    fun getAnalysesForDay(startOfDay: Long, endOfDay: Long): Flow<List<RoutineAnalysis>>
}
