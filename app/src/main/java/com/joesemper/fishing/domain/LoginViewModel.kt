package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.common.Progress
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<BaseViewState> =
        MutableStateFlow(BaseViewState.Success(null))

    fun subscribe(): StateFlow<BaseViewState> = mutableStateFlow

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.currentUser
                .catch { error -> handleError(error) }
                .collectLatest { user -> user?.let { onSuccess(it) } }
        }
    }

    private fun onSuccess(user: User) {
        viewModelScope.launch {
            repository.addNewUser(user).collect { progress ->
                when (progress) {
                    is Progress.Complete -> {
                        mutableStateFlow.value = BaseViewState.Success(user)
                    }
                    is Progress.Loading -> {
                        mutableStateFlow.value = BaseViewState.Loading(null)
                    }
                    is Progress.Error -> {
                        mutableStateFlow.value =
                            BaseViewState.Error(progress.error)
                    }
                }
            }
            //mutableStateFlow.value = BaseViewState.Success(user)
        }
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = BaseViewState.Error(error)
    }

}