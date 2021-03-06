package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.ui.home.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserCatchesViewModel(
    private val userCatchesUseCase: GetUserCatchesUseCase
    ) : ViewModel() {

    private val _currentContent = MutableStateFlow<List<UserCatch>>(mutableListOf())
    val currentContent = _currentContent.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.InProgress)
    val uiState: StateFlow<UiState>
        get() = _uiState

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        _uiState.value = UiState.InProgress
        viewModelScope.launch {
            userCatchesUseCase.invoke().collectLatest {
                _currentContent.emit(it)
                _uiState.value = UiState.Success
            }
        }
    }
}