package com.joesemper.fishing.viewmodel.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joesemper.fishing.model.entity.map.UserMarker
import com.joesemper.fishing.model.repository.map.MapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapViewModel(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val mutableStateFlow: MutableStateFlow<MapViewState> =
        MutableStateFlow(MapViewState.Loading)

    fun subscribe(): StateFlow<MapViewState> = mutableStateFlow

    init {
        loadUsersMarkers()
    }

    override fun onCleared() {
        super.onCleared()
        mutableStateFlow.value = MapViewState.Loading
    }

    private fun loadUsersMarkers() {
        viewModelScope.launch {
            mapRepository.getAllUserMarkers()
                .collect { markers -> onSuccess(markers) }
        }
    }

    fun addMarker(userMarker: UserMarker) {
        viewModelScope.launch {
            mapRepository.addMarker(userMarker)
        }
    }

    fun deleteMarker(userMarker: UserMarker) {
        viewModelScope.launch {
            mapRepository.deleteMarker(userMarker)
        }
    }

    private fun onSuccess(userMarkers: List<UserMarker?>) {
        mutableStateFlow.value = MapViewState.Success(userMarkers)
    }

    private fun handleError(error: Throwable) {
        mutableStateFlow.value = MapViewState.Error(error)
    }
}