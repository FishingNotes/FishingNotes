package com.mobileprism.fishing.ui.viewmodels.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.auth.restore.RestoreRemoteReset
import com.mobileprism.fishing.domain.repository.RestoreRepository
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.model.entity.FishingResponse
import com.mobileprism.fishing.model.utils.fold
import com.mobileprism.fishing.ui.viewmodels.login.RegisterInfo
import com.mobileprism.fishing.ui.viewstates.FishingViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetAccountViewModel(
    private val userLogin: UserLogin,
    private val validationUseCase: ValidationUseCase,
    private val restoreRepository: RestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FishingViewState<FishingResponse>?>(null)
    val uiState = _uiState.asStateFlow()

    private val _resetInfo = MutableStateFlow(RegisterInfo())
    val resetInfo = _resetInfo.asStateFlow()

    private var saveJob: Job? = null

    fun cancelSaveNewPassword() {
        saveJob?.cancel()
        resetUiState()
    }

    private fun resetUiState() {
        _uiState.update { null }
    }

    fun onPasswordSet(password: String) {
        _resetInfo.update { it.copy(password = password) }
    }

    fun onRepeatPasswordSet(repeatPassword: String) {
        _resetInfo.update { it.copy(repeatPassword = repeatPassword) }
        _resetInfo.value.let { regInfo ->
            if (regInfo.password.startsWith(repeatPassword, false).not()
                || regInfo.password.length < repeatPassword.length
            ) {
                _resetInfo.update {
                    it.copy(
                        repeatPasswordError =
                        validationUseCase.validateRepeatedPassword(it.password, repeatPassword)
                    )
                }
            } else {
                _resetInfo.update { it.copy(repeatPasswordError = ValidationResult(true)) }
            }
        }
    }

    fun validatePasswordInput() {
        _resetInfo.update {
            it.copy(passwordError = validationUseCase.validatePassword(it.password))
        }
    }

    fun saveNewPassword() {
        saveJob = viewModelScope.launch {
            resetInfo.value.apply {

                val passwordResult = validationUseCase.validatePassword(password)
                val repeatPasswordResult =
                    validationUseCase.validateRepeatedPassword(password, repeatPassword)

                val hasError = listOf(
                    passwordResult,
                    repeatPasswordResult,
                ).any { !it.successful }


                _resetInfo.update {
                    it.copy(
                        passwordError = passwordResult,
                        repeatPasswordError = repeatPasswordResult,
                    )
                }

                if (hasError) return@launch

                _uiState.update { FishingViewState.Loading }
                restoreRepository.restorePassword(RestoreRemoteReset(userLogin.login, password))
                    .single().fold(onSuccess = { result ->
                        if (result.success) {
                            _uiState.update { FishingViewState.Success(result) }
                        } else {
                            _uiState.update { FishingViewState.Error(result) }
                        }
                    }, onError = { error ->
                        _uiState.update { FishingViewState.Error(error) }
                    })
            }
        }
    }

}