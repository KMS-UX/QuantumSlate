package com.quantumslate.dashboard

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Substitutes [HiltTestApplication] for the real application during instrumented tests.
 *
 * Without this, Hilt has no test component and every `@HiltAndroidTest` fails at startup.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
