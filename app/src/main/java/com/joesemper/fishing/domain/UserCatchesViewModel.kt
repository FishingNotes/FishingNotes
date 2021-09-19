package com.joesemper.fishing.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.repository.UserContentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserCatchesViewModel(private val repository: UserContentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Loading(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState

    init {
        loadAllUserCatches()
    }

    private fun loadAllUserCatches() {
        val start = System.currentTimeMillis()
        viewModelScope.launch {
            repository.getAllUserCatchesList().collect { catches ->
                //for loading animation
                if (System.currentTimeMillis() - start < 1500) delay(1500)
                _uiState.value = BaseViewState.Success(catches)
            }
        }
    }
}