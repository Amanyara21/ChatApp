package com.aman.chatapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aman.chatapp.activity.ChatActivity
import com.aman.chatapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class NotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationData = remoteMessage.data

        if (notificationData.containsKey("callType") && notificationData.containsKey("senderUid")) {
            val callType = notificationData["callType"]
            val callerUid = notificationData["senderUid"]

            if (callType != null && callerUid != null) {
                println(notificationData.toString())
                sendIncomingCallBroadcast(callType, callerUid)
            }
        } else {
            // Handle other types of notifications (e.g., messages)
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body
            showNotification(title, body, notificationData)
        }
    }
    private fun sendIncomingCallBroadcast(callType: String, callerUid: String) {
        val intent = Intent("INCOMING_CALL")
        intent.putExtra("callType", callType)
        intent.putExtra("callerUid", callerUid)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    private fun showNotification(title: String?, body: String?, data: MutableMap<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "YourChannelId"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }


        val senderName = data["senderName"]
        val senderUid = data["senderUid"]

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("name", senderName)
        intent.putExtra("uid", senderUid)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notifications)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}
