package com.mobileprism.fishing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.entity.common.FishingFirebaseUser
import com.mobileprism.fishing.domain.entity.content.UserCatch
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.repository.app.OfflineRepository
import com.mobileprism.fishing.domain.use_cases.catches.GetUserCatchesUseCase
import com.mobileprism.fishing.domain.use_cases.users.SignOutCurrentUserUserCase
import com.mobileprism.fishing.domain.use_cases.users.SubscribeOnCurrentUserUseCase
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.ui.home.profile.findBestCatch
import com.mobileprism.fishing.ui.home.profile.findFavoritePlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val signOutCurrentUser: SignOutCurrentUserUserCase,
    private val subscribeOnCurrentUser: SubscribeOnCurrentUserUseCase,
    private val repository: OfflineRepository,
    private val getUserCatchUseCase: GetUserCatchesUseCase
) : ViewModel() {

    // TODO: current user listener
    private val _currentUser = MutableStateFlow<UserData>(UserData())
    val currentUser = _currentUser.asStateFlow()

    private val _currentPlaces = MutableStateFlow<List<UserMapMarker>?>(null)
    val currentPlaces = _currentPlaces.asStateFlow()

    private val _currentCatches = MutableStateFlow<List<UserCatch>?>(null)
    val currentCatches = _currentCatches.asStateFlow()

    private val _bestCatch = MutableStateFlow<UserCatch?>(null)
    val bestCatch = _bestCatch.asStateFlow()

    private val _favoritePlace = MutableStateFlow<UserMapMarker?>(null)
    val favoritePlace = _favoritePlace.asStateFlow()

    init {
        getUserCatches()
        getUserPlaces()
    }

    /*private val _uiState = MutableStateFlow<BaseViewState>(BaseViewState.Success(null))
    val uiState: StateFlow<BaseViewState>
        get() = _uiState*/

    private fun getUserPlaces() = viewModelScope.launch {
        repository.getAllUserMarkersList().collect {
            if (it.isEmpty()) {
                _currentCatches.value = listOf()
            }
            _currentPlaces.value = it
            _favoritePlace.value = findFavoritePlace(it)
        }
    }

    private fun getUserCatches() = viewModelScope.launch {
        // TODO: fix
        getUserCatchUseCase().collect {
            _currentCatches.value = it
            _bestCatch.value = findBestCatch(it)
        }

        /*val list = repository.getAllUserCatchesList().single()
        list.!ifEmpty {

        } ?: run {
            _currentCatches.value = list
        }
        */
    }

    fun logoutCurrentUser() {
        viewModelScope.launch {
            signOutCurrentUser()
        }
    }

}

