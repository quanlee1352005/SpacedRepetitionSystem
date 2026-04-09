package com.example.spacedrepetitionsystem.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.spacedrepetitionsystem.R
import com.example.spacedrepetitionsystem.data.AppDatabase
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val currentTime = System.currentTimeMillis()
        val cardsToReview = database.flashcardDao().getCardsToReview(currentTime).first()

        if (cardsToReview.isNotEmpty()) {
            showNotification(cardsToReview.size)
        }

        return Result.success()
    }

    private fun showNotification(count: Int) {
        val channelId = "srs_reminder_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SRS Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for SRS flashcard reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon for simplicity
            .setContentTitle("Thời gian ôn tập!")
            .setContentText("Bạn có $count thẻ cần ôn tập ngay bây giờ.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
