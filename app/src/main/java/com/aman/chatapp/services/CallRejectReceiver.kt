package com.aman.chatapp.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase

class CallRejectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val senderUid = intent?.getStringExtra("senderUid")
        val receiverUid = intent?.getStringExtra("receiverUid")
        val notificationId = intent?.getIntExtra("notificationId",0)

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationId != null) {
            notificationManager.cancel(notificationId)
        }

    }


}
