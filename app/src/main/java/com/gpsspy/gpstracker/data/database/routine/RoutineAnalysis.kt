package com.gpsspy.gpstracker.data.database.routine

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_analysis")
data class RoutineAnalysis(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val state: RoutineState
)
