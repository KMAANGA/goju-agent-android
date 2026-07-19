package com.maangatech.gojuagent.core.network.di

import javax.inject.Qualifier

/**
 * Bound by the `:app` module (per build flavor — dev/staging/production), since only the
 * application knows which environment it was built for. See `AppNetworkModule` in `:app`.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl
