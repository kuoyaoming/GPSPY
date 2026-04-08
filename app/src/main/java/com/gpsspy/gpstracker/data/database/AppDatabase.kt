package com.gpsspy.gpstracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.gpsspy.gpstracker.data.database.routine.RoutineAnalysis
import com.gpsspy.gpstracker.data.database.routine.RoutineAnalysisDao

@Database(entities = [LocationPoint::class, RoutineAnalysis::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun routineAnalysisDao(): RoutineAnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gps_tracker_database"
                )
                // Enable migration strategy here for future updates
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

