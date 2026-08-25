package com.quantumslate.dashboard.di

import android.content.Context
import com.quantumslate.dashboard.data.local.FlightDao
import com.quantumslate.dashboard.data.local.MascotStateDao
import com.quantumslate.dashboard.data.local.NewsDao
import com.quantumslate.dashboard.data.local.PreferencesManager
import com.quantumslate.dashboard.data.local.SpotifyDao
import com.quantumslate.dashboard.data.local.WeatherDao
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
    fun provideNewsRepository(
        newsDao: NewsDao,
        preferencesManager: PreferencesManager
    ): NewsRepository {
        return NewsRepository(newsDao, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideFlightRepository(
        flightDao: FlightDao,
        preferencesManager: PreferencesManager
    ): FlightRepository {
        return FlightRepository(flightDao, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideSpotifyRepository(
        spotifyDao: SpotifyDao,
        preferencesManager: PreferencesManager
    ): SpotifyRepository {
        return SpotifyRepository(spotifyDao, preferencesManager)
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
