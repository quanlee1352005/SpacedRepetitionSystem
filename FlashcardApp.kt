package com.example.baicuoiki

import android.app.Application
import com.example.baicuoiki.worker.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlashcardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Lập lịch nhắc nhở khi ứng dụng khởi chạy
        WorkManagerHelper.scheduleDailyReminder(this)
    }
}
