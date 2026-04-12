package com.example.spacedrepetitionsystem.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.spacedrepetitionsystem.data.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        val database = AppDatabase.getDatabase(applicationContext)
        val currentTime = System.currentTimeMillis()
        val cardsToReview = database.flashcardDao().getCardsToReview(userId, currentTime).first()

        if (cardsToReview.isNotEmpty()) {
            showNotification(cardsToReview.size)
        }

        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(count: Int) {
        val channelId = "srs_reminder_channel"
        
        // 1. Tạo Channel (Bắt buộc từ Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, "SRS Reminder", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Xây dựng nội dung thông báo
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Thời gian ôn tập!")
            .setContentText("Bạn có $count thẻ cần ôn tập ngay bây giờ.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // 3. Kiểm tra quyền và Hiển thị
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasPermission) {
            NotificationManagerCompat.from(applicationContext).notify(1, notification)
        }
    }
}
