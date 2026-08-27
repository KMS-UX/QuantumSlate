package com.quantumslate.dashboard.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Network logging that cannot leak credentials.
 *
 * `HttpLoggingInterceptor.Level.BASIC` logs the full request line — including the query
 * string. Two of this app's APIs pass their key as a query parameter (aviationstack's
 * `access_key`, OpenWeatherMap's `appid`), so BASIC would write live credentials into
 * logcat on every call. Bible §12 forbids logging sensitive information.
 *
 * This logs method, host and path plus timing, and replaces the value of any sensitive
 * parameter with `***`. Authorization headers are never logged at all.
 */
class RedactingLogInterceptor(private val enabled: Boolean) : Interceptor {

    private val sensitiveParams = setOf(
        "access_key", "appid", "apikey", "api_key", "key", "token", "access_token"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!enabled) return chain.proceed(request)

        val redacted = request.url.newBuilder().apply {
            request.url.queryParameterNames.forEach { name ->
                if (name.lowercase() in sensitiveParams) {
                    setQueryParameter(name, "***")
                }
            }
        }.build()

        val start = System.nanoTime()
        Log.d(TAG, "--> ${request.method} $redacted")

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.d(TAG, "<-- FAILED ${request.method} $redacted: ${e.javaClass.simpleName}")
            throw e
        }

        val ms = (System.nanoTime() - start) / 1_000_000
        Log.d(TAG, "<-- ${response.code} ${request.method} $redacted (${ms}ms)")
        return response
    }

    private companion object {
        const val TAG = "QuantumSlateHttp"
    }
}
