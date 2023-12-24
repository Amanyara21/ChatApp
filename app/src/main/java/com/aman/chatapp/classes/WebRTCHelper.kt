package com.aman.chatapp.classes

import android.content.Context
import android.util.Log
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
                if (newState == PeerConnectionState.CLOSED ||
                    newState == PeerConnectionState.DISCONNECTED
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


//    private fun observeIncomingLatestEvent(callBack: (DataModel) -> Unit) {
//        currentUsername = currentUser?.uid
//        println("name is ${currentUser?.uid} ${currentUser?.email}")
//
//        dbRef.child("calls").child(currentUsername!!).addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    println("Again name is ${currentUser?.uid} $snapshot")
//                    try {
//                        val data: String? = snapshot.value as? String
//                        if (data != null) {
//                            println(snapshot)
//                            val dataModel = gson.fromJson(data, DataModel::class.java)
//                            callBack(dataModel)
//                        } else {
//                            // Handle the case where snapshot.value is null
//                            println("Snapshot value is null")
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            })
//    }
//class MainRepository2 private constructor() : WebRTCClient.Listener {
//    var listener: Listener? = null
//    private val gson = Gson()
////    private val firebaseClient: FirebaseClient
//    private val dbRef = FirebaseDatabase.getInstance().reference
//    private var webRTCClient: WebRTCClient? = null
//    private var currentUsername: String? = null
//    private var remoteView: SurfaceViewRenderer? = null
//    private var target: String? = null
//    private val LATEST_EVENT_FIELD_NAME = "latest_event"
//    private val firebaseAuth= FirebaseAuth.getInstance()
//    private val currentUser= firebaseAuth.currentUser
//    private fun updateCurrentUsername(username: String) {
//        currentUsername = username
//    }
//
//    fun login(username: String, context: Context?, callBack: SuccessCallBack) {
//
//            updateCurrentUsername(username)
//            webRTCClient = WebRTCClient(context!!, object : MyPeerConnectionObserver() {
//                override fun onAddStream(mediaStream: MediaStream) {
//                    super.onAddStream(mediaStream)
//                    try {
//                        mediaStream.videoTracks[0].addSink(remoteView)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                override fun onConnectionChange(newState: PeerConnectionState) {
//                    Log.d("TAG", "onConnectionChange: $newState")
//                    super.onConnectionChange(newState)
//                    if (newState == PeerConnectionState.CONNECTED && listener != null) {
//                        listener!!.webrtcConnected()
//                    }
//                    if (newState == PeerConnectionState.CLOSED ||
//                        newState == PeerConnectionState.DISCONNECTED
//                    ) {
//                        if (listener != null) {
//                            listener!!.webrtcClosed()
//                        }
//                    }
//                }
//
//                override fun onIceCandidate(iceCandidate: IceCandidate) {
//                    super.onIceCandidate(iceCandidate)
//                    webRTCClient!!.sendIceCandidate(iceCandidate, target)
//                }
//            }, username)
//            webRTCClient!!.listener = this
//            callBack.onSuccess()
//    }
//
//    fun initLocalView(view: SurfaceViewRenderer?) {
//        webRTCClient!!.initLocalSurfaceView(view!!)
//    }
//
//    fun initRemoteView(view: SurfaceViewRenderer?) {
//        webRTCClient!!.initRemoteSurfaceView(view!!)
//        remoteView = view
//    }
//
//    fun startCall(target: String?) {
//        webRTCClient!!.call(target)
//    }
//
//    fun switchCamera() {
//        webRTCClient!!.switchCamera()
//    }
//
//    fun toggleAudio(shouldBeMuted: Boolean?) {
//        webRTCClient!!.toggleAudio(shouldBeMuted)
//    }
//
//    fun toggleVideo(shouldBeMuted: Boolean?) {
//        webRTCClient!!.toggleVideo(shouldBeMuted)
//    }
//
//    fun sendCallRequest(target: String?, errorCallBack: ErrorCallBack?) {
//        sendMessageToOtherUser(
//            DataModel(target!!, currentUsername!!, null, StartCall), errorCallBack
//        )
//    }
//
//    private fun sendMessageToOtherUser(model: DataModel, errorCallBack: ErrorCallBack?) {
//        currentUsername= currentUser?.uid
//        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
////                if (snapshot.child(model.target).exists()) {
//                    dbRef.child("calls").child(model.target)
//                        .setValue(gson.toJson(model))
////                } else {
////                    errorCallBack?.onError()
////                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                errorCallBack?.onError()
//            }
//        })
//    }
//
//    fun endCall() {
//        webRTCClient!!.closeConnection()
//    }
//
//
//    fun subscribeForLatestEvent(callBack: NewEventCallBack) {
//        println("callback $callBack")
//        observeIncomingLatestEvent { model ->
//            println("model ${model.type}")
//            when (model.type) {
//                DataModelType.Offer -> {
//                    this.target = model.sender
//                    webRTCClient?.onRemoteSessionReceived(
//                        SessionDescription(
//                            SessionDescription.Type.OFFER,
//                            model.data
//                        )
//                    )
//                    webRTCClient?.answer(model.sender)
//                }
//                DataModelType.Answer -> {
//                    this.target = model.sender
//                    webRTCClient?.onRemoteSessionReceived(
//                        SessionDescription(
//                            SessionDescription.Type.ANSWER,
//                            model.sender
//                        )
//                    )
//                }
//                DataModelType.IceCandidate -> {
//                    try {
//                        val candidate =
//                            gson.fromJson(model.data, IceCandidate::class.java)
//                        println("candidate is $candidate")
//                        webRTCClient?.addIceCandidate(candidate)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                DataModelType.StartCall -> {
//                    this.target = model.sender
//                    callBack.onNewEventReceived(model)
//                }
//                else -> {
//                }
//            }
//        }
//    }
//
//    fun observeIncomingLatestEvent(callBack: (DataModel) -> Unit) {
//        currentUsername= currentUser!!.uid
//        dbRef.child("calls").child(currentUsername!!)
//            .addValueEventListener(
//                object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        try {
//                            val data: String? = snapshot.value as? String
//                            if (data != null) {
//                            println(snapshot)
//                            val dataModel = gson.fromJson(data, DataModel::class.java)
//                            callBack(dataModel)
//                        } else {
//                            // Handle the case where snapshot.value is null
//                            println("Snapshot value is null")
//                        }
//                        } catch (e: java.lang.Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {}
//                }
//            )
//    }
//
//    override fun onTransferDataToOtherPeer(model: DataModel?) {
//        if (model != null) {
//             sendMessageToOtherUser(model, object :ErrorCallBack {
//                override fun onError() {
//                }
//            })
//        }
//    }
//
////    private fun sendMessageToOtherUser(model: DataModel?, function: () -> Unit) {
////
////    }
//
//
//    interface Listener {
//        fun webrtcConnected()
//        fun webrtcClosed()
//    }
//
//
//    companion object {
//        private var instance: MainRepository2? = null
//        fun getInstance(): MainRepository2 {
//            if (instance == null) {
//                instance = MainRepository2()
//            }
//            return instance!!
//        }
//    }
//
//    init {
////        firebaseClient = FirebaseClient()
//    }
//}
//
//interface ErrorCallBack {
//    fun onError()
//}
//
//interface SuccessCallBack {
//    fun onSuccess()
//
//}
//
//interface NewEventCallBack {
//    fun onNewEventReceived(model: DataModel)
//
//}
