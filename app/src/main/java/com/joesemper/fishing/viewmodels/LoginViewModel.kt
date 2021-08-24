package com.joesemper.fishing.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.data.auth.AuthManager
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.viewstates.LoginViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthManager) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<LoginViewState> =
        MutableStateFlow(LoginViewState.Loading)

    fun subscribe(): StateFlow<LoginViewState> = mutableStateFlow

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> onSuccess(user) }
        }
    }

    private fun onSuccess(user: User?) {
        mutableStateFlow.value = LoginViewState.Success(user)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = LoginViewState.Error(error)
    }

}