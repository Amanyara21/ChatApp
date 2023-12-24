package com.aman.chatapp.activity

import android.app.NotificationManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import com.aman.chatapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class AudioCallActivity : AppCompatActivity() {


    private lateinit var tvCallStatus: TextView
    private lateinit var btnToggleAudio: ImageButton
    private lateinit var btnEndCall: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)

        val senderUid = intent.getStringExtra("senderUid")
        val reciverUid = intent.getStringExtra("receiverUid")
        val callId = intent.getStringExtra("callId")
        val notificationId = intent.getIntExtra("notificationId",0)
        val user = intent.getStringExtra("user")

        if(user != "reciever" ){
            updateCallStatus(callId,senderUid,reciverUid,"accepted")
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        val callRef = FirebaseDatabase.getInstance().getReference("calls").child(callId.toString())

        callRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val callData = snapshot.getValue(CallData::class.java)
                    callData?.let {
                        val status = it.status
                        // Check the call status and initiate the call if accepted
                        if (status == "accepted") {
                            // Initiate the call
                            initiateCall()
                        } else if (status == "rejected") {
                            // Handle the case when the call is rejected
                            finish()
                        }
                        // Add more cases as needed
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })



        // Initialize UI components
        tvCallStatus = findViewById(R.id.tvCallStatus)
        btnToggleAudio = findViewById(R.id.btnToggleAudio)
        btnEndCall = findViewById(R.id.btnEndCall)

        // Set up your WebRTC manager to handle audio call logic
//        val webRTCManager= WebRTCManager(this)
        // Set click listeners for call controls
        btnToggleAudio.setOnClickListener {
            // Toggle audio mute/unmute
//             webRTCManager.toggleAudio()
        }

        btnEndCall.setOnClickListener {
            // End the call
//             webRTCManager.endCall()
            finish()
        }
    }


    private fun updateCallStatus(callId: String?, senderUid: String?, receiverUid: String?, status: String) {
        val callRef = FirebaseDatabase.getInstance().getReference("calls").child(callId.toString())
        callRef.child("status").setValue(status)
        if (senderUid != null) {
            callRef.child("participants").child(senderUid).setValue(true)
        }
        if (receiverUid != null) {
            callRef.child("participants").child(receiverUid).setValue(true)
        }
    }
    private fun initiateCall() {
        // Initialize your WebRTC manager to handle audio call logic
//        val webRTCManager = WebRTCManager(this)

        // Set up the remote renderer for audio-only call

        // Start the call
//        webRTCManager.startAudioCall()
    }



}

data class CallData(
    val participants: Map<String, Boolean>? = null,
    val status: String? = null
)

