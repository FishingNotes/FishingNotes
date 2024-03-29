package com.mobileprism.fishing.model.datastore.impl

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.model.auth.AuthState
import com.mobileprism.fishing.model.datastore.UserDatastore
import com.mobileprism.fishing.model.entity.user.Token
import com.mobileprism.fishing.model.entity.user.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserDatastoreImpl(private val context: Context): UserDatastore {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("appSettings")

        private val USER_KEY = stringPreferencesKey("user")
        private val FIREBASE_USER_KEY = stringPreferencesKey("firebase_user")
        private val TOKEN_KEY = stringPreferencesKey("token")
    }

    override val getAuthState: Flow<AuthState> = context.dataStore.data
        .map { preferences ->
            val user = Gson().fromJson(preferences[USER_KEY], UserData::class.java)
            val token = Gson().fromJson(preferences[TOKEN_KEY], Token::class.java)

            when {
                user != null && token.token.isBlank().not() -> {
                    Log.d("AUTH", "AuthState.LoggedIn")
                    AuthState.LoggedIn(user, token)
                }
                else -> {
                    Log.d("AUTH", "AuthState.NotLoggedIn")
                    AuthState.NotLoggedIn
                }
            }
        }.catch { error ->
            Log.d("AUTH_ERROR", error.message ?: "")
            emit(AuthState.NotLoggedIn)
        }

    //get the saved value
    override val getUser: Flow<UserData> = context.dataStore.data
        .map { preferences ->
            Gson().fromJson(preferences[USER_KEY], UserData::class.java) ?: UserData()
        }.catch { emit(UserData()) }

    override val getFirebaseUser: Flow<FishingFirebaseUser?> = context.dataStore.data
        .map { preferences ->
            Gson().fromJson(preferences[FIREBASE_USER_KEY], FishingFirebaseUser::class.java) ?: null
        }.catch { emit(null) }

    override suspend fun saveUser(user: UserData) {
        context.dataStore.edit { preferences ->
            preferences[USER_KEY] = Gson().toJson(user)
        }
    }

    override suspend fun saveFirebaseUser(firebaseUser: FishingFirebaseUser) {
        context.dataStore.edit { preferences ->
            preferences[FIREBASE_USER_KEY] = Gson().toJson(firebaseUser)
        }
    }

    override suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    override suspend fun setToken(newToken: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = Gson().toJson(Token(newToken))
        }
    }

    override val currentToken = context.dataStore.data.map { preferences ->
        Gson().fromJson(preferences[TOKEN_KEY], Token::class.java) ?: Token()
    }

}