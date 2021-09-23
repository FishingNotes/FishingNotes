package com.joesemper.fishing.di

import com.joesemper.fishing.domain.*
import com.joesemper.fishing.model.datasource.*
import com.joesemper.fishing.model.repository.UserContentRepository
import com.joesemper.fishing.model.repository.UserRepository
import com.joesemper.fishing.model.repository.WeatherRepository
import com.joesemper.fishing.ui.LoginActivity
import com.joesemper.fishing.ui.MainActivity
import com.joesemper.fishing.ui.SplashActivity
import com.joesemper.fishing.ui.fragments.*
import com.joesemper.fishing.utils.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {
    single<UserRepository> { FirebaseUserRepositoryImpl(androidContext()) }
    single<UserContentRepository> { CloudFireStoreDatabase(get()) }
    single<PhotoStorage> { CloudPhotoStorage() }
    single<WeatherRepository> { WeatherRepositoryRetrofitImpl() }
    single { Logger() }

}

val mainActivity = module {
    scope(named<MainActivity>()) {
        viewModel { MainViewModel() }
    }
}

val splashScreen = module {
    scope(named<SplashActivity>()) {
        viewModel { SplashViewModel(get()) }
    }
}

val loginScreen = module {
    scope(named<LoginActivity>()) {
        viewModel { LoginViewModel(get()) }
    }
}

val mapScreen = module {
    scope(named<MapFragment>()) {
        viewModel { MapViewModel(get()) }
    }
}

val userFragment = module {
    scope(named<UserFragment>()) {
        viewModel { UserViewModel(get(), get()) }
    }
}

val newCatchFragment = module {
    scope(named<NewCatchFragment>()) {
        viewModel { NewCatchViewModel(get(), get()) }
    }
}

val newPlaceFragment = module {
    scope(named<NewPlaceFragment>()) {
        viewModel { NewPlaceViewModel(get()) }
    }
}

val userCatchFragment = module {
    scope(named<UserCatchFragment>()) {
        viewModel { UserCatchViewModel(get(), get()) }
    }
}

val weatherScreen = module {
    scope(named<WeatherFragment>()) {
        viewModel { WeatherViewModel(get(), get()) }
    }
}

val notesFragment = module {
    scope(named<NotesFragment>()) {
        viewModel { NotesViewModel(get()) }
    }
}

val userPlaceFragment = module {
    scope(named<UserPlaceFragment>()) {
        viewModel { UserPlaceViewModel(get(), get()) }
    }
}
val catchesFragment = module {
    viewModel { UserCatchesViewModel(get()) }
}

val placesFragment = module {
    viewModel { UserPlacesViewModel(get()) }
}
