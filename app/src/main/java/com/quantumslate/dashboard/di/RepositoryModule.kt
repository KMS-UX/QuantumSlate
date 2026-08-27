package com.quantumslate.dashboard.di

import android.content.Context
import com.quantumslate.dashboard.data.local.CalendarDao
import com.quantumslate.dashboard.data.local.FlightDao
import com.quantumslate.dashboard.data.local.MascotStateDao
import com.quantumslate.dashboard.data.local.NewsDao
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.local.SpotifyDao
import com.quantumslate.dashboard.data.local.WeatherDao
import com.quantumslate.dashboard.data.remote.flight.AviationStackDataSource
import com.quantumslate.dashboard.data.remote.flight.FlightDataSource
import com.quantumslate.dashboard.data.remote.flight.FlightRequestBudget
import com.quantumslate.dashboard.data.remote.spotify.SpotifyAuthManager
import com.quantumslate.dashboard.data.repository.CalendarRepository
import com.quantumslate.dashboard.data.repository.FlightRepository
import com.quantumslate.dashboard.data.repository.MascotRepository
import com.quantumslate.dashboard.data.repository.NewsRepository
import com.quantumslate.dashboard.data.repository.SpotifyRepository
import com.quantumslate.dashboard.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherDao: WeatherDao,
        preferencesManager: PreferencesManager,
        @ApplicationContext context: Context
    ): WeatherRepository {
        return WeatherRepository(weatherDao, preferencesManager, context)
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(
        calendarDao: CalendarDao,
        @ApplicationContext context: Context
    ): CalendarRepository {
        return CalendarRepository(calendarDao, context)
    }

    @Provides
    @Singleton
    fun provideNewsRepository(
        newsDao: NewsDao,
        preferencesManager: PreferencesManager
    ): NewsRepository {
        return NewsRepository(newsDao, preferencesManager)
    }

    /**
     * The active flight provider.
     *
     * Swapping providers means binding a different [FlightDataSource] here; nothing in the
     * repository, view model, or UI changes.
     */
    @Provides
    @Singleton
    fun provideFlightDataSource(
        aviationStack: AviationStackDataSource
    ): FlightDataSource = aviationStack

    @Provides
    @Singleton
    fun provideFlightRepository(
        flightDao: FlightDao,
        preferencesManager: PreferencesManager,
        dataSource: FlightDataSource,
        requestBudget: FlightRequestBudget
    ): FlightRepository {
        return FlightRepository(flightDao, preferencesManager, dataSource, requestBudget)
    }

    @Provides
    @Singleton
    fun provideSpotifyRepository(
        spotifyDao: SpotifyDao,
        preferencesManager: PreferencesManager,
        spotifyAuthManager: SpotifyAuthManager
    ): SpotifyRepository {
        return SpotifyRepository(spotifyDao, preferencesManager, spotifyAuthManager)
    }

    @Provides
    @Singleton
    fun provideMascotRepository(
        mascotDao: MascotStateDao,
        preferencesManager: PreferencesManager,
        weatherRepository: WeatherRepository,
        flightRepository: FlightRepository,
        spotifyRepository: SpotifyRepository
    ): MascotRepository {
        return MascotRepository(mascotDao, preferencesManager, weatherRepository, flightRepository, spotifyRepository)
    }
}
