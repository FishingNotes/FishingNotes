package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.weather.CurrentWeatherFree
import kotlinx.coroutines.flow.Flow

interface FreeWeatherRepository {
    suspend fun getCurrentWeatherFree(lat: Double, lon: Double): Flow<RetrofitWrapper<CurrentWeatherFree>>

}