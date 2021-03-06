package com.mobileprism.fishing.ui.home.weather

import android.content.Context
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserMapMarker
import com.mobileprism.fishing.domain.entity.weather.Daily
import com.mobileprism.fishing.ui.Arguments
import com.mobileprism.fishing.ui.MainDestinations
import com.mobileprism.fishing.ui.navigate
import com.mobileprism.fishing.ui.utils.enums.StringOperation
import com.mobileprism.fishing.utils.Constants.CURRENT_PLACE_ITEM_ID
import java.text.DecimalFormat

object WindFormat {
    val df = DecimalFormat("#.#")
}

fun createCurrentPlaceItem(latLng: LatLng, context: Context): UserMapMarker {
    return UserMapMarker(
        id = CURRENT_PLACE_ITEM_ID,
        title = context.getString(R.string.current_location),
        latitude = latLng.latitude,
        longitude = latLng.longitude
    )
}

fun getPressureList(
    forecast: List<Daily>,
    pressureUnit: PressureValues
): List<Int> {
    return forecast.map { /*pressureUnit.getPressureInt(it.pressure)*/it.pressure.toInt() }
}

fun getBounds(list: List<Int>): Pair<Int, Int> {
    var min = Int.MAX_VALUE
    var max = -Int.MAX_VALUE
    list.forEach {
        min = min.coerceAtMost(it)
        max = max.coerceAtLeast(it)
    }
    return Pair(min, max)
}

data class Point(
    val x: Float,
    val y: Float
)

enum class PressureValues(override val stringRes: Int) : StringOperation {
    Pa(R.string.pressure_pa),
    Bar(R.string.pressure_bar),
    mmHg(R.string.pressure_mm),
    Psi(R.string.pressure_psi),
    Hpa(R.string.pressure_hpa);

    fun getPressureFromHpa(hPa: Int): String {
        return when (this) {
            Pa -> (hPa * 100).toString()
            Bar -> (hPa / 1000).toString()
            mmHg -> (hPa * 0.75006375541921).toInt().toString()
            Psi -> String.format("%.5g", (hPa * 0.0145037738f))
            Hpa -> hPa.toString()
        }
    }

    fun getPressureFromMmhg(mmHg: Int): String {
        return when (this) {
            Pa -> (mmHg * 133.322).toString()
            Bar -> (mmHg * 0.00133322f).toString()
            PressureValues.mmHg -> mmHg.toString()
            Psi -> String.format("%.5g", (mmHg * 0.0193368f))
            Hpa -> (mmHg * 1.33).toString()
        }
    }

    fun getPressureMmhg(value: Double): Int {
        return when (this) {
            Pa -> (value * 0.0075006156130264f).toInt()
            Bar -> (value * 750.06168f).toInt()
            mmHg -> value.toInt()
            Psi -> (value * 51.71484f).toInt()
            Hpa -> (value * 1.33f).toInt()
        }
    }
}

enum class TemperatureValues(override val stringRes: Int) : StringOperation {
    C(R.string.celsius),
    F(R.string.fahrenheit),
    K(R.string.kelvin);

    fun getTemperature(temperature: Float): String {
        return when (this) {
            C -> temperature.toInt().toString()
            F -> (temperature * 9f / 5f + 32).toInt().toString()
            K -> (temperature + 273.15).toInt().toString()
        }
    }

    fun getDefaultTemperature(temperature: Double): Int {
        return when (this) {
            C -> temperature.toInt()
            F -> ((temperature - 32) * (5 / 9)).toInt()
            K -> (temperature - 273.15).toInt()
        }
    }
}

enum class WindSpeedValues(override val stringRes: Int) : StringOperation {
    metersps(R.string.wind_mps),
    milesph(R.string.wind_mph),
    knots(R.string.wind_knots),
    ftps(R.string.wind_ftps),
    kmph(R.string.wind_kmph);

    fun getWindSpeed(windSpeed: Double): String {
        return WindFormat.df.format(
            when (this) {
                metersps -> windSpeed
                knots -> (windSpeed * 1.9438444924574)
                milesph -> (windSpeed * 2.2369362920544)
                ftps -> (windSpeed * 3.28084)
                kmph -> (windSpeed * 3.6)
            }
        )
    }

    fun getDefaultWindSpeed(windSpeed: Double): String {
        return (when (this) {
            metersps -> windSpeed
            knots -> (windSpeed * 1.9438444924574)
            milesph -> (windSpeed * 2.2369362920544)
            ftps -> (windSpeed * 3.28084)
            kmph -> (windSpeed * 3.6)
        }).toInt().toString()
    }

}

fun navigateToDailyWeatherScreen(
    navController: NavController,
    index: Int,
    forecastDaily: List<Daily>
) {
    val argument = DailyWeatherData(
        selectedDay = index,
        dailyForecast = forecastDaily
    )
    navController.navigate(
        MainDestinations.DAILY_WEATHER_ROUTE,
        Arguments.WEATHER_DATA to argument
    )
}

fun navigateToAddNewPlace(navController: NavController) {
    navController.navigate("${MainDestinations.HOME_ROUTE}/${MainDestinations.MAP_ROUTE}?${Arguments.MAP_NEW_PLACE}=${true}")
}