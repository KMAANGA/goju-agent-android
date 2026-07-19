package com.maangatech.gojuagent.core.network

import com.maangatech.gojuagent.core.security.SecurePrefs
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the Sanctum bearer token to every request and flags [sessionExpired] on a 401 so
 * the app-level session observer can route to the login/unlock screen — this interceptor
 * itself must not clear the stored session, since a single flaky request shouldn't sign the
 * teller out from under an in-progress USSD transaction.
 */
class AuthInterceptor @Inject constructor(private val securePrefs: SecurePrefs) : Interceptor {

    var onUnauthorized: (() -> Unit)? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = securePrefs.apiToken

        val request = if (token != null) {
            original.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            original
        }

        val response = chain.proceed(request)
        if (response.code == 401) {
            onUnauthorized?.invoke()
        }
        return response
    }
}
