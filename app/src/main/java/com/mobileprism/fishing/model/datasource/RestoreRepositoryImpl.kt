package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteConfirm
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteFind
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteReset
import com.mobileprism.fishing.domain.repository.RestoreRepository
import com.mobileprism.fishing.model.api.fishing.RestoreApiService
import com.mobileprism.fishing.model.utils.fishingSafeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow

class RestoreRepositoryImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val restoreApiService: RestoreApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RestoreRepository {

    override suspend fun searchAccount(find: RestoreRemoteFind) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("search_account", null)
            restoreApiService.searchAccount(find)
        }
        emit(result)
    }

    override suspend fun confirmOTP(confirm: RestoreRemoteConfirm) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("search_account", null)
            restoreApiService.confirmOTP(confirm)
        }
        emit(result)
    }

    override suspend fun restorePassword(reset: RestoreRemoteReset) = flow {
        val result = fishingSafeApiCall(dispatcher) {
            firebaseAnalytics.logEvent("search_account", null)
            restoreApiService.restorePassword(reset)
        }
        emit(result)
    }


}