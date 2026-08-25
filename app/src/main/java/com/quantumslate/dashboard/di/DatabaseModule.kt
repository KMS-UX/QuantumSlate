package com.quantumslate.dashboard.di

import android.content.Context
import androidx.room.Room
import com.quantumslate.dashboard.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "quantumslate_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(database: AppDatabase) = database.weatherDao()

    @Provides
    @Singleton
    fun provideCalendarDao(database: AppDatabase) = database.calendarDao()

    @Provides
    @Singleton
    fun provideNewsDao(database: AppDatabase) = database.newsDao()

    @Provides
    @Singleton
    fun provideSettingsDao(database: AppDatabase) = database.settingsDao()

    @Provides
    @Singleton
    fun provideFlightDao(database: AppDatabase) = database.flightDao()

    @Provides
    @Singleton
    fun provideSpotifyDao(database: AppDatabase) = database.spotifyDao()

    @Provides
    @Singleton
    fun provideMascotStateDao(database: AppDatabase) = database.mascotStateDao()
}
