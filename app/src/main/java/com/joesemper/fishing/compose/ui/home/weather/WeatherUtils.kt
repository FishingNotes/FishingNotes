package com.joesemper.fishing.compose.ui.home.weather

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.google.android.libraries.maps.model.LatLng
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.content.UserMapMarker

fun createCurrentPlaceItem(latLng: LatLng, context: Context): UserMapMarker {
    return UserMapMarker(
        title = context.getString(R.string.current_location),
        latitude = latLng.latitude,
        longitude = latLng.longitude
    )
}

fun getPressure(hPa: Int, pressureValue: PressureValues): String {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100).toString()
        PressureValues.Bar -> (hPa / 1000f).toString()
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt().toString()
        PressureValues.Psi -> (hPa * 0.0145037738).toString()
        else -> {
            (hPa * 0.75006375541921).toInt().toString()
        }
    }
}

fun getPressureInt(hPa: Int, pressureValue: PressureValues): Int {
    return when (pressureValue) {
        PressureValues.Pa -> (hPa * 100)
        PressureValues.Bar -> (hPa / 1000f).toInt()
        PressureValues.mmHg -> (hPa * 0.75006375541921).toInt()
        PressureValues.Psi -> (hPa * 0.0145037738).toInt()
        else -> {
            (hPa * 0.75006375541921).toInt().toInt()
        }
    }
}


fun getTemperature(temperature: Float, temperatureValue: TemperatureValues): String {
    return when (temperatureValue) {
        TemperatureValues.C -> temperature.toInt().toString()
        TemperatureValues.F -> (temperature * 9f/5f + 32).toInt().toString()
        TemperatureValues.K -> (temperature + 273.15).toInt().toString()
        else -> "1"
    }
}

@Composable
fun getTemperatureIcon(valueOf: TemperatureValues): Painter {
    return painterResource(R.drawable.ic_temperature_celsius)
}

enum class PressureValues {
    Pa,
    Bar,
    mmHg,
    Psi
}

enum class TemperatureValues {
    C, F, K
}