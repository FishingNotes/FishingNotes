package com.joesemper.fishing.model.datasource

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.joesemper.fishing.model.api.WeatherApiService
import com.joesemper.fishing.model.entity.weather.WeatherForecast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRetrofitImplementation : WeatherProvider {

    companion object {
        private const val BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @ExperimentalCoroutinesApi
    override fun getWeather(lat: Double, lon: Double) = flow {
        val weather = getService().getWeather(latitude = lat, longitude = lon)
        emit(weather)
    }

    override suspend fun getHistoricalWeather(
        lat: Double,
        lon: Double,
        date: Long
    ): WeatherForecast =
        getService().getHistoricalWeather(latitude = lat, longitude = lon, dt = date)

    override suspend fun getWeatherForecast(lat: Double, lon: Double) =
        getService().getWeather(latitude = lat, longitude = lon)

    private fun getService(): WeatherApiService {
        return createRetrofit().create(WeatherApiService::class.java)
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_WEATHER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(createOkHttpClient())
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

}