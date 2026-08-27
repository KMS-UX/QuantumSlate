package com.quantumslate.dashboard.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.quantumslate.dashboard.data.remote.RedactingLogInterceptor
import com.quantumslate.dashboard.data.remote.flight.AviationStackApiService
import com.quantumslate.dashboard.data.remote.flight.FlightRequestCounter
import javax.inject.Qualifier
import okhttp3.OkHttpClient
import com.quantumslate.dashboard.BuildConfig
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = RedactingLogInterceptor(enabled = BuildConfig.DEBUG)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * A dedicated client for the flight API, because its free tier is metered per request
     * and the defaults quietly spend more than one request per fetch.
     */
    @Provides
    @Singleton
    @FlightHttpClient
    fun provideFlightOkHttpClient(counter: FlightRequestCounter): OkHttpClient {
        val logging = RedactingLogInterceptor(enabled = BuildConfig.DEBUG)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // Count actual calls, not intended ones.
            .addInterceptor(counter)
            // aviationstack's free tier is HTTP-only. If the host answers with a redirect to
            // HTTPS, following it silently costs a second billed request for the same data —
            // which is exactly the "one refresh, two quota" symptom. Fail loudly instead.
            .followRedirects(false)
            .followSslRedirects(false)
            // A transparent retry would also double-bill.
            .retryOnConnectionFailure(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}

/** Marks the metered flight-API client, so it is never confused with the general one. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlightHttpClient
