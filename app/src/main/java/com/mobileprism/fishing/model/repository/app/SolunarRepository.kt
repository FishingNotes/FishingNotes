package com.mobileprism.fishing.model.repository.app

import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.solunar.Solunar
import kotlinx.coroutines.flow.Flow

interface SolunarRepository {
    fun getSolunar(latitude: Double, longitude: Double): Flow<RetrofitWrapper<Solunar>>
}