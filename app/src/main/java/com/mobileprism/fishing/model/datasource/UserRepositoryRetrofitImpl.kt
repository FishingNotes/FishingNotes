package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.entity.common.LoginPassword
import com.mobileprism.fishing.domain.repository.UserRepository
import com.mobileprism.fishing.model.api.UserApiService
import com.mobileprism.fishing.model.entity.user.UserApiResponse
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.utils.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val okHttpClient: OkHttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserRepository {

    companion object {
        private const val BASE_USER_URL =
            "https://virtserver.swaggerhub.com/MobilePrism/FishingNotesAPI/1.0.0"
    }

    private fun getService(): UserApiService {
        return createRetrofit().create(UserApiService::class.java)
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_USER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    override suspend fun registerNewUser(loginPassword: LoginPassword): Result<UserApiResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("register_new_user", null)

//            getService().registerNewUser(
//                email = loginPassword.login,
//                password = loginPassword.password
//            )

            UserApiResponse(
                token = "123",
                UserData(email = loginPassword.login, login = "Anonymous")
            )
        }

    override suspend fun loginUser(loginPassword: LoginPassword): Result<UserApiResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("register_new_user", null)

//            getService().loginUser(
//                email = loginPassword.login,
//                password = loginPassword.password
//            )

            UserApiResponse(
                token = "123",
                UserData(email = loginPassword.login, login = "Anonymous")
            )
        }

    override suspend fun loginUserWithGoogle(
        email: String,
        userId: String
    ): Result<UserApiResponse> = safeApiCall(dispatcher) {

        firebaseAnalytics.logEvent("register_new_user", null)

        getService().loginUserWithGoogle(
            email = email,
            googleAuthId = userId
        )
    }
}