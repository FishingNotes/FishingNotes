package com.joesemper.fishing.model.api

import com.joesemper.fishing.model.entity.weather.WeatherForecast
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("onecall")
    suspend fun getWeather(
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
            @Query("units") units: String? = "metric",
            @Query("exclude") exclude: String? = "minutely,current,alerts",
            @Query("lang") lang: String? = "ru",
            @Query("appid") appid: String = "b2f26c3643e28be455da04e60ed90e16"
    ): WeatherForecast

}