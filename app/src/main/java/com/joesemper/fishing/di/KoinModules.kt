package com.joesemper.fishing.di

import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.viewmodels.MainViewModel
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.domain.*
import com.joesemper.fishing.model.datasource.*
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.WeatherRepository
import com.joesemper.fishing.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    single<UserRepository> { FirebaseUserRepositoryImpl(androidContext()) }
    single<UserContentRepository> { CloudFireStoreDatabase(get()) }
    single<PhotoStorage> { CloudPhotoStorage() }
    single<WeatherRepository> { WeatherRepositoryRetrofitImpl() }
    single { Logger() }
    single { SnackbarManager }
    single { UserPreferences(androidContext()) }

}

val mainActivity = module {

    viewModel { MainViewModel(get()) }
    viewModel { LoginViewModel(get()) }

    viewModel { MapViewModel(get()) }
    viewModel { NewCatchViewModel(get(), get()) }
    viewModel { UserViewModel(get(), get()) }
    viewModel { NewPlaceViewModel(get()) }
    viewModel { UserCatchViewModel(get(), get()) }
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { UserPlaceViewModel(get(), get()) }
    viewModel { UserCatchesViewModel(get()) }
    viewModel { UserPlacesViewModel(get()) }

}