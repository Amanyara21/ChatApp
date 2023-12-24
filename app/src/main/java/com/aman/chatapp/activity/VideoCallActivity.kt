package com.aman.chatapp.activity


import com.aman.chatapp.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


import android.view.View
import android.widget.Button
import android.widget.*

import com.aman.chatapp.classes.WebRTCHelper
import com.aman.chatapp.utils.*
import com.google.firebase.auth.FirebaseAuth
import org.webrtc.SurfaceViewRenderer

class VideoCallActivity : AppCompatActivity(), WebRTCHelper.Listener {

    private lateinit var webRTCHelper: WebRTCHelper
    private var isCameraMuted = false
    private var isMicrophoneMuted = false
    private lateinit var localView:SurfaceViewRenderer
    private lateinit var remoteView:SurfaceViewRenderer
    private lateinit var incomingCallLayout: LinearLayout
    private lateinit var callLayout: RelativeLayout
    private lateinit var micButton: ImageView
    private lateinit var videoButton: ImageView
    private lateinit var endCallButton: ImageView
    private lateinit var switchCameraButton: ImageView

    private lateinit var receiverUid:String
    private lateinit var senderUid:String
    private lateinit var currentUserName:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

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
        videoButton = findViewById(R.id.video_button)
        endCallButton = findViewById(R.id.end_call_button)
        switchCameraButton = findViewById(R.id.switch_camera_button)
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



        switchCameraButton.setOnClickListener {
                webRTCHelper.switchCamera()
            }

            micButton.setOnClickListener {
                if (isMicrophoneMuted) {
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24)
                } else {
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24)
                }
                webRTCHelper.toggleAudio(isMicrophoneMuted)
                isMicrophoneMuted = !isMicrophoneMuted
            }

            videoButton.setOnClickListener {
                if (isCameraMuted) {
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
                } else {
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
                }
                webRTCHelper.toggleVideo(isCameraMuted)
                isCameraMuted = !isCameraMuted
            }

            endCallButton.setOnClickListener {
                webRTCHelper.endCall()
                finish()
            }
    }
    override fun webrtcConnected() {
        runOnUiThread {
            val layoutParamsLocalView = localView.layoutParams
            layoutParamsLocalView.width = 120 // Replace R.dimen.local_view_width with the actual dimension resource
            layoutParamsLocalView.height = 120 // Replace R.dimen.local_view_height with the actual dimension resource
            localView.layoutParams = layoutParamsLocalView
        }
    }

    override fun webrtcClosed() {
        runOnUiThread {
            finish()
        }
    }
}


