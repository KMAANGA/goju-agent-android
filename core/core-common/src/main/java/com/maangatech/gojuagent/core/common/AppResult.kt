package com.maangatech.gojuagent.core.common

/**
 * Uniform result wrapper used across repositories and use-cases so callers never
 * have to catch exceptions at the UI layer.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }

    fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Typed failure reasons. Kept small and specific so the UI layer can render
 * the right message (e.g. "no connection" vs "session expired") without
 * parsing exception messages.
 */
sealed class AppError(val message: String) {
    data object NoConnection : AppError("No internet connection.")
    data object Timeout : AppError("The request timed out. Please try again.")
    data object Unauthorized : AppError("Your session has expired. Please sign in again.")
    data object DeviceNotApproved : AppError("This device is awaiting approval. Contact your branch supervisor.")
    data class ServerError(val code: Int, val detail: String?) :
        AppError(detail ?: "Something went wrong (code $code).")
    data class Validation(val fieldErrors: Map<String, String>) :
        AppError(fieldErrors.values.firstOrNull() ?: "Please check the form and try again.")
    data class Unknown(val cause: Throwable) : AppError(cause.message ?: "An unexpected error occurred.")
}

inline fun <T> runCatchingAppResult(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (t: Throwable) {
        AppResult.Error(AppError.Unknown(t))
    }
