package com.quantumslate.dashboard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WeatherEntity::class,
        CalendarEventEntity::class,
        NewsArticleEntity::class,
        SettingsEntity::class,
        FlightEntity::class,
        SpotifyTrackEntity::class,
        MascotStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun calendarDao(): CalendarDao
    abstract fun newsDao(): NewsDao
    abstract fun settingsDao(): SettingsDao
    abstract fun flightDao(): FlightDao
    abstract fun spotifyDao(): SpotifyDao
    abstract fun mascotStateDao(): MascotStateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quantumslate_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
