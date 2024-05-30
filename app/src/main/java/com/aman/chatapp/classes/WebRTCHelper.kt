package com.aman.chatapp.classes

import android.content.Context
import android.content.Intent
import android.os.Bundle.EMPTY
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.aman.chatapp.activity.VideoCallActivity
import com.aman.chatapp.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection.PeerConnectionState
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import java.util.*

class WebRTCHelper : WebRTCClient.Listener {

    var listener: Listener? = null
    private val gson = Gson()
    private val dbRef = FirebaseDatabase.getInstance().reference

    private val LATEST_EVENT_FIELD_NAME = "latest_event"

    private lateinit var webRTCClient: WebRTCClient

    private var currentUsername: String? = null

    private var remoteView: SurfaceViewRenderer? = null

    private var target: String? = null
    private fun updateCurrentUsername(username: String) {
        this.currentUsername = username
    }

    interface Listener {
        fun webrtcConnected()
        fun webrtcClosed()
        fun webrtcConnecting()
    }

    companion object {
        private val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        private var instance: WebRTCHelper? = null
        private const val LATEST_EVENT_FIELD_NAME = "latest_event"

        fun getInstance(): WebRTCHelper {
            if (instance == null) {
                instance = WebRTCHelper()
            }
            return instance!!
        }
    }


    fun login(username: String?, context: Context?, callBack: SuccessCallBack) {
        updateCurrentUsername(username!!)
        webRTCClient = WebRTCClient(context!!, object : MyPeerConnectionObserver() {
            override fun onAddStream(mediaStream: MediaStream) {
                super.onAddStream(mediaStream)
                try {
                    mediaStream.videoTracks[0].addSink(remoteView)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            override fun onConnectionChange(newState: PeerConnectionState) {
                Log.d("TAG", "onConnectionChange: $newState")
                super.onConnectionChange(newState)
                if (newState == PeerConnectionState.CONNECTED && listener != null) {
                    listener!!.webrtcConnected()
                }
                if (newState == PeerConnectionState.CLOSED || newState == PeerConnectionState.DISCONNECTED
                ) {
                    if (listener != null) {
                        listener!!.webrtcClosed()
                    }
                }

            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                super.onIceCandidate(iceCandidate)
                webRTCClient.sendIceCandidate(iceCandidate, target!!)
            }
        }, username)
        webRTCClient.listener = this
        callBack.onSuccess()
    }


    private fun observeIncomingLatestEvent(callBack: NewEventCallBack) {
        dbRef.child("calls").child(currentUsername!!).child(LATEST_EVENT_FIELD_NAME)
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val data = Objects.requireNonNull(snapshot.value).toString()
                            val dataModel = gson.fromJson(data, DataModel::class.java)
                            callBack.onNewEventReceived(dataModel)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                }
            )
    }

    fun initLocalView(view: SurfaceViewRenderer?) {
        webRTCClient.initLocalSurfaceView(view!!)
    }

    fun initRemoteView(view: SurfaceViewRenderer?) {
        webRTCClient.initRemoteSurfaceView(view!!)
        remoteView = view
    }

    fun startCall(target: String?) {
        webRTCClient.call(target!!)
    }

    fun switchCamera() {
        webRTCClient.switchCamera()
    }


    fun sendCallRequest(target: String?, errorCallBack: ErrorCallBack?) {
        sendMessageToOtherUser(
            DataModel(target, currentUsername, null, DataModelType.StartCall), errorCallBack!!
        )
    }

    fun endCall() {
//        webRTCClient.stopLocalVideo()
//        webRTCClient.releaseCamera()
        webRTCClient.closeConnection()
    }

    fun toggleAudio(shouldBeMuted: Boolean) {
        webRTCClient.toggleAudio(shouldBeMuted)
    }

    fun toggleVideo(shouldBeMuted: Boolean) {
        webRTCClient.toggleVideo(shouldBeMuted)
    }


    fun subscribeForLatestEvent(callBack: NewEventCallBack) {
        observeIncomingLatestEvent { model: DataModel ->
            when (model.type) {
                DataModelType.Offer -> {
                    target = model.sender
                    webRTCClient.onRemoteSessionReceived(
                        SessionDescription(
                            SessionDescription.Type.OFFER, model.data
                        )
                    )
                    webRTCClient.answer(model.sender)
                }
                DataModelType.Answer -> {
                    target = model.sender
                    webRTCClient.onRemoteSessionReceived(
                        SessionDescription(
                            SessionDescription.Type.ANSWER, model.data
                        )
                    )
                }
                DataModelType.IceCandidate -> try {
                    val candidate = gson.fromJson(
                        model.data,
                        IceCandidate::class.java
                    )
                    webRTCClient.addIceCandidate(candidate)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                DataModelType.StartCall -> {
                    target = model.sender
                    callBack.onNewEventReceived(model)
                }
            }
        }
    }

    override fun onTransferDataToOtherPeer(model: DataModel) {
        sendMessageToOtherUser(model) {}
    }


    fun sendMessageToOtherUser(dataModel: DataModel, errorCallBack: ErrorCallBack) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dbRef.child("calls").child(dataModel.target)
                    .child(LATEST_EVENT_FIELD_NAME)
                    .setValue(gson.toJson(dataModel))
            }

            override fun onCancelled(error: DatabaseError) {
                errorCallBack.onError()
            }
        })
    }
}



