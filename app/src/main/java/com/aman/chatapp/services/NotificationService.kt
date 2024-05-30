package com.aman.chatapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.aman.chatapp.activity.ChatActivity
import com.aman.chatapp.R
import com.aman.chatapp.activity.AudioCallActivity
import com.aman.chatapp.activity.MainActivity
import com.aman.chatapp.activity.VideoCallActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class NotificationService : FirebaseMessagingService() {

    val callRejectReceiver = CallRejectReceiver()
    val filter = IntentFilter("ACTION_REJECT_CALL")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notificationData = remoteMessage.data

        if (notificationData.containsKey("callType") && notificationData.containsKey("senderUid")) {
            val callType = notificationData["callType"]
            val callerUid = notificationData["senderUid"]
            val receiverUid = notificationData["receiverUid"]

            if (callType != null && callerUid != null) {
                println(notificationData.toString())
                handleCallNotification(callType,  notificationData)
            }
        } else {
            // Handle other types of notifications (e.g., messages)
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            showNotification(title, body, notificationData)
        }
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



    @SuppressLint("UnspecifiedImmutableFlag")
    private fun handleCallNotification(callType: String, data: MutableMap<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "CallNotificationChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Call Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val senderName = data["senderName"]
        val senderUid = data["senderUid"]
        val receiverUid = data["receiverUid"]
        val callId= data["callId"]
        val type = data["type"]


        val acceptIntent:Intent
        if(type=="audioCall"){
            println("audioCall")
            acceptIntent = Intent(this, AudioCallActivity::class.java)
        }else{
            acceptIntent = Intent(this, VideoCallActivity::class.java)
        }
        val notificationId = System.currentTimeMillis().toInt()
        acceptIntent.action = "ACCEPT_CALL"
        acceptIntent.putExtra("senderUid", senderUid)
        acceptIntent.putExtra("receiverUid", receiverUid)
        acceptIntent.putExtra("callId", callId)
        acceptIntent.putExtra("notificationId", notificationId)
        acceptIntent.putExtra("user", "receiver")
        val acceptPendingIntent = PendingIntent.getActivity(this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val rejectIntent = Intent("ACTION_REJECT_CALL")
        rejectIntent.putExtra("callId", callId)
        rejectIntent.putExtra("senderUid", senderUid)
        rejectIntent.putExtra("receiverUid",receiverUid)
        rejectIntent.putExtra("notificationId",notificationId)

        val rejectPendingIntent = PendingIntent.getBroadcast(this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        registerReceiver(callRejectReceiver, filter)


        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)



        val contentView = RemoteViews(packageName, R.layout.call_notification)
        contentView.setTextViewText(R.id.notificationText, "Call from ${data["senderName"]}")


        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setCustomContentView(contentView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_call, "Accept", acceptPendingIntent)
            .addAction(R.drawable.ic_baseline_call_end_24, "Reject", rejectPendingIntent)
            .setSound(ringtoneUri)
            .setOngoing(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callRejectReceiver)
    }


}
