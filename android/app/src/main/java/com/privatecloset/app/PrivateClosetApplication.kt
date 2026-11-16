package com.privatecloset.app

import android.app.Application
import com.privatecloset.app.di.AppContainer

class PrivateClosetApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
