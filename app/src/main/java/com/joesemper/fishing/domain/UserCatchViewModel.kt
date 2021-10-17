package com.joesemper.fishing.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import kotlinx.coroutines.launch

class UserCatchViewModel(
    private val contentRepository: UserContentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val catch: MutableState<UserCatch> = mutableStateOf(UserCatch())

    fun deleteCatch() {
        viewModelScope.launch {
            contentRepository.deleteCatch(catch.value)
        }
    }

    fun getCurrentUser() = userRepository.currentUser

    fun getMapMarker(markerId: String) = contentRepository.getMapMarker(markerId)
}