package com.mobileprism.fishing.ui.viewmodels.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.BuildConfig
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthManager
import com.mobileprism.fishing.domain.use_cases.validation.ValidationResult
import com.mobileprism.fishing.domain.use_cases.validation.ValidationUseCase
import com.mobileprism.fishing.model.auth.LoginState
import com.mobileprism.fishing.ui.home.UiState
import com.mobileprism.fishing.utils.LoginInputType
import com.mobileprism.fishing.utils.checkLoginInputType
import com.mobileprism.fishing.utils.network.ConnectionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authManager: AuthManager,
    private val connectionManager: ConnectionManager,
    private val validationUseCase: ValidationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val _loginInfo = MutableStateFlow(LoginInfo())
    val loginInfo = _loginInfo.asStateFlow()

    private var loginJob: Job? = null

    init {
        subscribeOnLoginState()
    }

    fun cancelLogin() {
        loginJob?.cancel()
        _uiState.update { null }
    }

    fun setLogin(login: String) {
        _loginInfo.update {
            _loginInfo.value.copy(
                login = login,
                loginError = ValidationResult(true)
            )
        }
    }

    fun setPassword(password: String) {
        _loginInfo.update {
            _loginInfo.value.copy(
                password = password,
                passwordError = ValidationResult(true)
            )
        }
    }

    fun validateLogin(skipEmpty: Boolean = false) {
        if (skipEmpty && _loginInfo.value.login.isEmpty()) return

        loginInfo.value.apply {
            when (login.contains("@")) {
                true -> {
                    _loginInfo.update {
                        it.copy(loginError = validationUseCase.validateEmail(login))
                    }
                }
                false -> {
                    _loginInfo.update {
                        it.copy(loginError = validationUseCase.validateUsername(login))
                    }
                }
            }
        }
    }

    fun signInUser() {
        loginJob = viewModelScope.launch {
            loginInfo.value.apply {

                val loginResult = validationUseCase.validateLogin(login)
                val passwordResult = validationUseCase.validatePassword(password)

                val hasError = listOf(
                    loginResult,
                    passwordResult,
                ).any { !it.successful }


                _loginInfo.update {
                    it.copy(
                        loginError = loginResult,
                        passwordError = passwordResult,
                    )
                }

                if (hasError) return@launch

                _uiState.update { UiState.InProgress }
                if (BuildConfig.DEBUG) delay(2000)
                _loginInfo.value.let { loginInfo ->

                    when (checkLoginInputType(loginInfo.login)) {
                        LoginInputType.Email -> {
                            authManager.loginUser(
                                EmailPassword(
                                    loginInfo.login,
                                    loginInfo.password
                                )
                            )
                        }
                        LoginInputType.Username -> {
                            authManager.loginUser(
                                UsernamePassword(
                                    loginInfo.login,
                                    loginInfo.password
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun subscribeOnLoginState() {
        viewModelScope.launch {
            authManager.loginState.collectLatest { loginState ->
                when (loginState) {
                    is LoginState.LoggedIn -> {
                        _uiState.update { UiState.Success }
                        /*_uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                isError = false
                            )
                        }*/
                    }
                    is LoginState.LoginFailure -> {
                        _uiState.update { UiState.Error }

                        /*_uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isError = true,
                                errorText = loginState.throwable.localizedMessage
                            )
                        }*/
                    }
                    LoginState.NotLoggedIn -> {
                        // TODO:
                        /*_uiState.update {
                            _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = false,
                                isError = false
                            )
                        }*/
                    }
                }
            }
        }
    }
}

data class LoginInfo(
    val login: String = "",
    val loginError: ValidationResult = ValidationResult(true),
    val password: String = "",
    val passwordError: ValidationResult = ValidationResult(true),
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isLoggedIn: Boolean = false
)