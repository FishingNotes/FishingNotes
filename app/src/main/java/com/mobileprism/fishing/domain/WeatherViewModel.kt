package com.mobileprism.fishing.domain

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprism.fishing.domain.viewstates.RetrofitWrapper
import com.mobileprism.fishing.model.entity.content.UserMapMarker
import com.mobileprism.fishing.model.entity.weather.WeatherForecast
import com.mobileprism.fishing.model.repository.app.MarkersRepository
import com.mobileprism.fishing.model.repository.app.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val repository: MarkersRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<RetrofitWrapper<WeatherForecast>>(RetrofitWrapper.Loading())
    val weatherState: StateFlow<RetrofitWrapper<WeatherForecast>>
        get() = _weatherState

    private val _weather = MutableStateFlow<WeatherForecast>(WeatherForecast())
    val weather: StateFlow<WeatherForecast>
        get() = _weather

    val markersList = mutableStateListOf<UserMapMarker>()

    init {
        getAllMarkers()
    }

    private fun getAllMarkers() {
        _weatherState.value = RetrofitWrapper.Loading()
        viewModelScope.launch {
            repository.getAllUserMarkersList().collect {
                markersList.clear()
                markersList.addAll(it as List<UserMapMarker>)
            }
        }

    }

    fun getWeather(latitude: Double, longitude: Double) {
        _weatherState.value = RetrofitWrapper.Loading()
        viewModelScope.launch {
            weatherRepository.getWeather(latitude, longitude).collect { result ->
                when (result) {
                    is RetrofitWrapper.Success<WeatherForecast> -> {
                        //weather.value = result.data
                        _weatherState.value = RetrofitWrapper.Success(result.data)
                        _weather.value = result.data
                    }
                    is RetrofitWrapper.Loading -> {
                        _weatherState.value = RetrofitWrapper.Loading()
                    }
                    is RetrofitWrapper.Error -> {
                        _weatherState.value = RetrofitWrapper.Error(result.errorType)
                    }
                }

            }
        }
    }


}