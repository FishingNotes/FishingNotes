package com.joesemper.fishing.compose.ui.home.weather

import android.os.Parcelable
import com.joesemper.fishing.model.entity.weather.Daily
import kotlinx.parcelize.Parcelize

@Parcelize
data class DailyWeatherData(
    val selectedDay: Int,
    val dailyForecast: List<Daily>
) : Parcelable