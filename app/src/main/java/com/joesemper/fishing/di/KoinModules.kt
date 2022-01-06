package com.joesemper.fishing.di

import com.joesemper.fishing.compose.datastore.AppPreferences
import com.joesemper.fishing.compose.datastore.NotesPreferences
import com.joesemper.fishing.compose.datastore.UserPreferences
import com.joesemper.fishing.compose.datastore.WeatherPreferences
import com.joesemper.fishing.compose.ui.home.SnackbarManager
import com.joesemper.fishing.compose.viewmodels.MainViewModel
import com.joesemper.fishing.compose.viewmodels.MapViewModel
import com.joesemper.fishing.domain.*
import com.joesemper.fishing.model.datasource.*
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.app.CatchesRepository
import com.joesemper.fishing.model.repository.app.MarkersRepository
import com.joesemper.fishing.model.repository.app.SolunarRepository
import com.joesemper.fishing.model.repository.app.WeatherRepository
import com.joesemper.fishing.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    single { Logger() }
    single { SnackbarManager }

}

val repositoryModule = module {
    single { RepositoryCollections() }

    single<UserRepository> { FirebaseUserRepositoryImpl(get(), get(), androidContext()) }
    single<CatchesRepository> { FirebaseCatchesRepositoryImpl(get(), get()) }
    single<MarkersRepository> { FirebaseMarkersRepositoryImpl(get(), get()) }
    single<SolunarRepository> { SolunarRetrofitRepositoryImpl() }
    single<PhotoStorage> { CloudPhotoStorage(androidContext()) }
    single<WeatherRepository> { WeatherRepositoryRetrofitImpl() }
}

val settingsModule = module {
    single { AppPreferences(androidContext()) }
    single { UserPreferences(androidContext()) }
    single { WeatherPreferences(androidContext()) }
    single { NotesPreferences(androidContext()) }
}

val mainModule = module {

    viewModel { MainViewModel(get()) }
    viewModel { LoginViewModel(get()) }

    viewModel { MapViewModel(get(), get(), get()) }
    viewModel { NewCatchViewModel(get(), get(), get()) }
    viewModel { UserViewModel(get(), get(), get()) }
    viewModel { NewPlaceViewModel(get()) }
    viewModel { UserCatchViewModel(get(), get(), get()) }
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { UserPlaceViewModel(get(), get()) }
    viewModel { UserCatchesViewModel(get()) }
    viewModel { UserPlacesViewModel(get()) }

}