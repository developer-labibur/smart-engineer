package com.softylasa.smartengineer.network

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.softylasa.smartengineer.R
import com.softylasa.smartengineer.ui.activities.SmartDashboardActivity // Import your activity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId = "engineering_notifications_channel"
    private val channelName = "SoftyLasa Notifications"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification
        remoteMessage.notification?.let {
            // Show notification
            sendNotification(it.body ?: "New Notification")
        } ?: run {
            // Handle data-only messages here if needed
            Log.d("FCM", "Data Message Received: ${remoteMessage.data}")
        }
    }

    override fun onNewToken(token: String) {
        // This method is called whenever the FCM token is refreshed
        Log.d("FCM", "Refreshed token: $token")

        // You can send this token to your server or store it locally
        // For example, save it to shared preferences or your server database
        // Subscribe to a topic
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic"
                if (!task.isSuccessful) {
                    msg = "Subscription failed"
                }
                Log.d("FCM", msg)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(messageBody: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 (API 26) and above
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        // Create an Intent to open SmartDashboardActivity when notification is clicked
        val intent = Intent(this, SmartDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Smart Engineer Notification")
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.logo_blue) // Use your app's notification icon
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent to open the activity
            .build()

        // Show the notification in the status bar
        notificationManager.notify(0, notification)
    }
}
