package me.saro.dat.exception

class DatResult<out T> private constructor(
    val value: T?,
    val exception: Throwable?,
    val result: Result<T>?
) {
    val isSuccess: Boolean get() = exception == null && value != null
    val isFailure: Boolean get() = exception != null

    fun getOrNull(): T? = value
    fun getOrThrow(): T = value ?: throw exception!!
    fun getOrElse(or: () -> @UnsafeVariance T): T = value ?: or()
    fun exceptionOrNull(): Throwable? = exception

    fun toResult(): Result<T> = result ?: if (isSuccess) Result.success(value!!) else Result.failure(exception!!)

    fun <R> map(onSuccess: (value: T?) -> R): DatResult<R> {
        return if (isSuccess) {
            success(onSuccess(value))
        } else {
            failure(exception)
        }
    }

    fun <R> fold(onSuccess: (value: T) -> DatResult<R>): DatResult<R> {
        return if (isSuccess) {
            onSuccess(value!!)
        } else {
            failure(exception!!)
        }
    }

    fun <R> fold(onSuccess: (value: T) -> DatResult<R>, onFailure: (exception: Throwable) -> DatResult<R>): DatResult<R> {
        return if (isSuccess) {
            onSuccess(value!!)
        } else {
            onFailure(exception!!)
        }
    }

    companion object {
        fun <T> parse(result: Result<T>): DatResult<T> = result.fold(
            onSuccess = { value -> of(value, null, result) },
            onFailure = { exception -> of(null, exception, result) }
        )
        @JvmStatic
        fun <T> success(value: T?): DatResult<T> {
            return of(value, null)
        }
        @JvmStatic
        fun <T> failure(exception: Throwable?): DatResult<T> {
            return of(null, exception)
        }
        fun <T> of(success: T?, failure: Throwable?, result: Result<T>? = null): DatResult<T> {
            return if (failure != null) {
                DatResult(null, failure, result)
            } else if (success != null) {
                DatResult(success, null, result)
            } else {
                DatResult(null, DatException("value is null"), result)
            }
        }
    }
}
