package com.mobileprism.fishing.domain.repository

import com.mobileprism.fishing.domain.entity.common.EmailPassword
import com.mobileprism.fishing.model.entity.user.UserResponse

interface UserRepository {
    suspend fun registerNewUser(emailPassword: EmailPassword): Result<UserResponse>
    suspend fun loginUser(emailPassword: EmailPassword): Result<UserResponse>
    suspend fun loginUserWithGoogle(email: String, userId: String): Result<UserResponse>
}