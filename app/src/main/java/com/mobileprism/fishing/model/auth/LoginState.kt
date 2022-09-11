package com.mobileprism.fishing.model.auth

sealed class LoginState {
    object LoggedIn : LoginState()
    object GoogleAuthInProcess : LoginState()
    class LoginFailure(val throwable: Throwable) : LoginState()
    object NotLoggedIn : LoginState()
}