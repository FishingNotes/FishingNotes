package com.mobileprism.fishing.model.utils

import com.mobileprism.fishing.model.entity.FishingCodes
import com.mobileprism.fishing.model.entity.FishingResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.success(apiCall.invoke())
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}


suspend fun <T> fishingSafeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> Response<T>,
): ResultWrapper<T> {
    return withContext(dispatcher) {
        try {
            val result = apiCall.invoke()
            when {
                result.isSuccessful -> ResultWrapper.Success(result.body()!!)
                else -> ResultWrapper.GenericError(convertFishingErrorBody(result.errorBody()))
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is UnknownHostException, is IOException -> ResultWrapper.NetworkError
                else -> ResultWrapper.GenericError(
                    FishingResponse(
                        success = false,
                        description = throwable.message ?: "",
                        fishingCode = FishingCodes.UNKNOWN_ERROR
                    )
                )
            }
        }
    }
}

private fun convertFishingErrorBody(errorBody: ResponseBody?): FishingResponse? {
    return try {
        errorBody?.source()?.let {
            val moshiAdapter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build().adapter(FishingResponse::class.java)
            moshiAdapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T): ResultWrapper<T>()
    data class GenericError(val data: FishingResponse?): ResultWrapper<Nothing>()
    object NetworkError: ResultWrapper<Nothing>()
}

@OptIn(ExperimentalContracts::class)
inline fun <R, T : Any> ResultWrapper<T>.fold(
    onSuccess: (value: T) -> R,
    onError: (exception: FishingResponse?) -> R,
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onError, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is ResultWrapper.Success -> onSuccess(this.data)
        is ResultWrapper.GenericError -> onError(this.data)
        else -> onError(null)
    }
}
