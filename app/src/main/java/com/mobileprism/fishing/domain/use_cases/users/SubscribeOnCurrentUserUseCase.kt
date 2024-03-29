package com.mobileprism.fishing.domain.use_cases.users

import com.mobileprism.fishing.domain.repository.AuthManager

class SubscribeOnCurrentUserUseCase(
    private val authManager: AuthManager
) {
    operator fun invoke() = authManager.currentUser
}