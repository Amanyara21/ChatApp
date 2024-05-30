package com.aman.chatapp.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.aman.chatapp.R
import com.aman.chatapp.classes.WebRTCHelper
import com.aman.chatapp.utils.DataModelType
import com.google.firebase.auth.FirebaseAuth
import org.webrtc.SurfaceViewRenderer


class AudioCallActivity : AppCompatActivity(), WebRTCHelper.Listener {

    private lateinit var webRTCHelper: WebRTCHelper
    private var isMicrophoneMuted = false
    private lateinit var localView: SurfaceViewRenderer
    private lateinit var remoteView: SurfaceViewRenderer
    private lateinit var callLayout: RelativeLayout
    private lateinit var micButton: ImageView
    private lateinit var endCallButton: ImageView

    private lateinit var receiverUid:String
    private lateinit var senderUid:String
    private lateinit var currentUserName:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        currentUserName= currentUser?.uid.toString()


        initializeViews()
        webRTCHelper.login(currentUserName, applicationContext) { }
        init()

    }
    private fun initializeViews() {
        callLayout = findViewById(R.id.callLayout)
        remoteView = findViewById(R.id.remote_view)
        localView = findViewById(R.id.local_view)
        micButton = findViewById(R.id.mic_button)
        endCallButton = findViewById(R.id.end_call_button)
        webRTCHelper = WebRTCHelper.getInstance()

        callLayout.visibility = View.VISIBLE


        senderUid = intent.getStringExtra("senderUid").toString()
        receiverUid = intent.getStringExtra("receiverUid").toString()
    }

    private fun init() {

        if(senderUid==currentUserName){
            webRTCHelper.sendCallRequest(receiverUid) { }
            webRTCHelper.initLocalView(localView)
            webRTCHelper.initRemoteView(remoteView)
        }

        webRTCHelper.listener = this

        webRTCHelper.subscribeForLatestEvent { model ->
            runOnUiThread {
                if (model.type == DataModelType.StartCall) {
                    if(receiverUid==currentUserName){
                        webRTCHelper.initLocalView(localView)
                        webRTCHelper.initRemoteView(remoteView)
                        webRTCHelper.startCall(model.sender)
                    }
                }
            }
        }

        micButton.setOnClickListener {
            if (isMicrophoneMuted) {
                micButton.setImageResource(R.drawable.ic_mic_off)
            } else {
                micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            webRTCHelper.toggleAudio(isMicrophoneMuted)
            isMicrophoneMuted = !isMicrophoneMuted
        }

        endCallButton.setOnClickListener {
            webRTCHelper.endCall()
            finish()
        }
    }
    override fun webrtcConnected() {
        runOnUiThread {

        }
    }
    override fun webrtcConnecting() {
        runOnUiThread {

        }
    }

    override fun webrtcClosed() {
        runOnUiThread {
            webRTCHelper.endCall()
            finish()
        }
    }
}


