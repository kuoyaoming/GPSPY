package com.gpsspy.gpstracker.di

import android.content.Context
import com.gpsspy.gpstracker.data.database.AppDatabase
import com.gpsspy.gpstracker.data.database.LocationDao
import com.gpsspy.gpstracker.data.preferences.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideRoutineAnalysisDao(database: AppDatabase): com.gpsspy.gpstracker.data.database.routine.RoutineAnalysisDao {
        return database.routineAnalysisDao()
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideGnssStatusManager(@ApplicationContext context: Context): com.gpsspy.gpstracker.service.GnssStatusManager {
        return com.gpsspy.gpstracker.service.GnssStatusManager(context)
    }
}



