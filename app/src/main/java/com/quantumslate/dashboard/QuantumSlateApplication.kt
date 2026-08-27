package com.quantumslate.dashboard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QuantumSlateApplication : Application(), Configuration.Provider {

    /**
     * [DashboardUpdateWorker] is a @HiltWorker, so WorkManager must be given the
     * Hilt-aware factory. The default initializer is removed in the manifest and
     * WorkManager is configured on demand from here instead.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
