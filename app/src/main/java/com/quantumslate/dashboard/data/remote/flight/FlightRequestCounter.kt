package com.quantumslate.dashboard.data.remote.flight

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Counts every real HTTP call to the flight provider.
 *
 * The budget used to be incremented in the repository, before the call. That under-counted
 * whenever one logical fetch produced more than one HTTP request — most importantly when
 * aviationstack's HTTP endpoint redirects to HTTPS, which the provider bills as two requests
 * against a 100/month allowance. Counting in an interceptor means our meter reflects what
 * they actually charge.
 */
class FlightRequestCounter @Inject constructor(
    private val budget: FlightRequestBudget
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.host == AviationStackApiService.HOST) {
            budget.record()
        }
        return chain.proceed(request)
    }
}
