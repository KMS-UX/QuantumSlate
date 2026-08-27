package com.quantumslate.dashboard.data.remote

import com.quantumslate.dashboard.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) QuantumSlate/1.0 (+dashboard feed reader)"

    private var weatherRetrofit: Retrofit? = null
    private var flightRetrofit: Retrofit? = null
    private var spotifyRetrofit: Retrofit? = null
    private var rssRetrofit: Retrofit? = null

    private fun createOkHttpClient(): OkHttpClient {
        val logging = RedactingLogInterceptor(enabled = BuildConfig.DEBUG)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // Many feed hosts (rss.app, Cloudflare-fronted sites, several news outlets)
            // reject requests with OkHttp's default User-Agent, or with none at all, and
            // return 403. A conventional UA is required in practice, not optional.
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
                        .build()
                )
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getWeatherApiService(): WeatherApiService {
        if (weatherRetrofit == null) {
            weatherRetrofit = Retrofit.Builder()
                .baseUrl(WeatherApiService.BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return weatherRetrofit!!.create(WeatherApiService::class.java)
    }

    fun getFlightApiService(baseUrl: String): FlightApiService {
        if (flightRetrofit == null || flightRetrofit?.baseUrl()?.toString() != baseUrl) {
            flightRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return flightRetrofit!!.create(FlightApiService::class.java)
    }

    fun getSpotifyApiService(): SpotifyApiService {
        if (spotifyRetrofit == null) {
            spotifyRetrofit = Retrofit.Builder()
                .baseUrl(SpotifyApiService.BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return spotifyRetrofit!!.create(SpotifyApiService::class.java)
    }

    fun getRssApiService(baseUrl: String): RssApiService {
        rssRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
        return rssRetrofit!!.create(RssApiService::class.java)
    }
}
