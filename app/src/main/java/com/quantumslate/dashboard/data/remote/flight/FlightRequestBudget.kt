package com.quantumslate.dashboard.data.remote.flight

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks flight API requests against the provider's monthly allowance.
 *
 * With a 100-request free tier, an unnoticed polling loop can burn a month's quota in an
 * afternoon and leave the widget dead until the next billing cycle. Counting locally lets the
 * app stop *before* the provider does, and lets the UI show the user what is left.
 *
 * The counter resets on calendar-month change. That may not align exactly with the provider's
 * own billing window, so a small reserve is kept rather than spending to the last request.
 */
@Singleton
class FlightRequestBudget @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("flight_request_budget", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COUNT = "count"
        private const val KEY_PERIOD = "period"

        /**
         * Requests held back from the published limit, to absorb drift between our
         * calendar-month reset and the provider's billing period.
         */
        private const val RESERVE = 5
    }

    /** Requests recorded in the current calendar month. */
    val used: Int
        get() {
            rollOverIfNewPeriod()
            return prefs.getInt(KEY_COUNT, 0)
        }

    fun remaining(monthlyLimit: Int?): Int? {
        if (monthlyLimit == null) return null
        return (monthlyLimit - RESERVE - used).coerceAtLeast(0)
    }

    /** True when a request may be spent without risking the reserve. */
    fun canSpend(monthlyLimit: Int?): Boolean {
        val left = remaining(monthlyLimit) ?: return true
        return left > 0
    }

    fun record(count: Int = 1) {
        rollOverIfNewPeriod()
        prefs.edit()
            .putInt(KEY_COUNT, prefs.getInt(KEY_COUNT, 0) + count)
            .apply()
    }

    /** Test/support hook: clears the counter for the current period. */
    fun reset() {
        prefs.edit().putInt(KEY_COUNT, 0).putString(KEY_PERIOD, currentPeriod()).apply()
    }

    private fun rollOverIfNewPeriod() {
        val period = currentPeriod()
        if (prefs.getString(KEY_PERIOD, null) != period) {
            prefs.edit().putInt(KEY_COUNT, 0).putString(KEY_PERIOD, period).apply()
        }
    }

    private fun currentPeriod(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}"
    }
}
