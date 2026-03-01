package com.example.study

import android.app.Application
import com.cloudinary.android.MediaManager

class StudyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "do13gblpz",
            "api_key" to "225295576215534",
            "api_secret" to "opeN3rj7FoVunLcQIcyT8lSKX2M"
        )
        MediaManager.init(this, config)
    }
}
