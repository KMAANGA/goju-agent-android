package com.maangatech.gojuagent.core.common

import javax.inject.Qualifier

/** Process-lifetime [kotlinx.coroutines.CoroutineScope], provided by `:app`'s `CoroutineScopeModule`. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
