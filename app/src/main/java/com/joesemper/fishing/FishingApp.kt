package com.joesemper.fishing

import android.app.Application
import android.util.Log.ERROR
import com.joesemper.fishing.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class FishingApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@FishingApp)
            modules(appModule, mainModule, repositoryModule, settingsModule)

        }
    }
}