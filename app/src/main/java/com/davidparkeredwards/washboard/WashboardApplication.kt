package com.davidparkeredwards.washboard

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class WashboardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this);
    }
}