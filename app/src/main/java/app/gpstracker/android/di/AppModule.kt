package app.gpstracker.android.di

import android.content.Context
import app.gpstracker.android.data.database.AppDatabase
import app.gpstracker.android.data.database.LocationDao
import app.gpstracker.android.data.preferences.SettingsRepository
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
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideGnssStatusManager(@ApplicationContext context: Context): app.gpstracker.android.service.GnssStatusManager {
        return app.gpstracker.android.service.GnssStatusManager(context)
    }
}
