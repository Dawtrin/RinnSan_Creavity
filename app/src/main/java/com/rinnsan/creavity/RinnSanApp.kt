package com.rinnsan.creavity

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RinnSanApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize các services khác nếu cần
        // - Analytics
        // - Crash reporting
        // - Remote config
    }
}