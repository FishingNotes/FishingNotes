package com.mobileprism.fishing.domain.use_cases.catches

import androidx.core.net.toUri
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.repository.app.catches.CatchesRepository
import com.mobileprism.fishing.domain.use_cases.SavePhotosUseCase


class UpdateUserCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val savePhotos: SavePhotosUseCase,
) {
    suspend operator fun invoke(newCatch: UserCatch) {
        catchesRepository.updateUserCatch(
            markerId = newCatch.userMarkerId,
            catchId = newCatch.id,
            data = mapOf(
                "downloadPhotoLinks" to savePhotos(newCatch.downloadPhotoLinks.map { it.toUri() }),
                "fishType" to newCatch.fishType,
                "fishAmount" to newCatch.fishAmount,
                "fishWeight" to newCatch.fishWeight,
                "fishingRodType" to newCatch.fishingRodType,
                "fishingBait" to newCatch.fishingBait,
                "fishingLure" to newCatch.fishingLure,
                "note" to newCatch.note
            )
        )
    }
}