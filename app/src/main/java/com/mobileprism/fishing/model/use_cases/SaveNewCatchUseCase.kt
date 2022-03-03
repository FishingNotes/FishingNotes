package com.mobileprism.fishing.model.use_cases

import android.net.Uri
import com.mobileprism.fishing.domain.*
import com.mobileprism.fishing.model.datastore.WeatherPreferences
import com.mobileprism.fishing.model.entity.content.UserCatch
import com.mobileprism.fishing.model.entity.raw.NewCatchWeather
import com.mobileprism.fishing.model.repository.PhotoStorage
import com.mobileprism.fishing.model.repository.app.CatchesRepository
import com.mobileprism.fishing.utils.getCurrentUser
import com.mobileprism.fishing.utils.getNewCatchId
import com.mobileprism.fishing.utils.network.ConnectionManager
import com.mobileprism.fishing.utils.network.ConnectionState
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first

class SaveNewCatchUseCase(
    private val catchesRepository: CatchesRepository,
    private val catchesRepositoryOffline: CatchesRepository,
    private val photosRepository: PhotoStorage,
    private val connectionManager: ConnectionManager,
    private val weatherPreferences: WeatherPreferences
) {

    operator fun invoke(data: NewUserCatchData) = channelFlow {

        val userCatch = createUserCatch(
            placeAndTimeState = data.placeAndTimeState,
            fishAndWeightState = data.fishAndWeightState,
            catchInfoState = data.catchInfoState,
            weather = mapWeatherValues(data.catchWeatherState),
            photos = savePhotos(data.photos)
        )

        val repository = if (connectionManager.getConnectionState() is ConnectionState.Available) {
            catchesRepository
        } else catchesRepositoryOffline

        data.placeAndTimeState.place?.let { it ->
            repository.addNewCatch(markerId = it.id, newCatch = userCatch).collect { trySend(it) }
        }
    }

    private suspend fun savePhotos(photos: List<Uri>): List<String> {
        return photosRepository.uploadPhotos(photos)
    }

    private suspend fun mapWeatherValues(weatherState: CatchWeatherState): NewCatchWeather {
        val tempUnits = weatherPreferences.getTemperatureUnit().first()
        val pressureUnits = weatherPreferences.getPressureUnit().first()
        val windUnits = weatherPreferences.getWindSpeedUnit().first()

        return NewCatchWeather(
            weatherDescription = weatherState.primary.replaceFirstChar { it.uppercase() },
            icon = weatherState.icon,
            temperatureInC = tempUnits.getCelciusTemperature(weatherState.temperature.toFloat()),
            pressureInMmhg = pressureUnits.getDefaultPressure(weatherState.pressure.toFloat()),
            windInMs = windUnits.getWindSpeedInt(weatherState.windSpeed.toDouble()).toInt(),
            windDirInDeg = weatherState.windDeg.toFloat(),
            moonPhase = weatherState.moonPhase
        )
    }

    private fun createUserCatch(
        placeAndTimeState: CatchPlaceAndTimeState,
        fishAndWeightState: FishAndWeightState,
        catchInfoState: CatchInfoState,
        weather: NewCatchWeather,
        photos: List<String>
    ) = UserCatch(
        id = getNewCatchId(),
        userId = getCurrentUser()!!.uid,
        description = catchInfoState.note,
        date = placeAndTimeState.date,
        fishType = fishAndWeightState.fish,
        fishAmount = fishAndWeightState.fishAmount,
        fishWeight = fishAndWeightState.fishWeight,
        fishingRodType = catchInfoState.rod,
        fishingBait = catchInfoState.bait,
        fishingLure = catchInfoState.lure,
        userMarkerId = placeAndTimeState.place?.id ?: "",
        isPublic = false,
        downloadPhotoLinks = photos,
        placeTitle = placeAndTimeState.place?.title ?: "",
        weatherPrimary = weather.weatherDescription,
        weatherIcon = weather.icon,
        weatherTemperature = weather.temperatureInC.toFloat(),
        weatherWindSpeed = weather.windInMs.toFloat(),
        weatherWindDeg = weather.windDirInDeg.toInt(),
        weatherPressure = weather.pressureInMmhg,
        weatherMoonPhase = weather.moonPhase
    )

}