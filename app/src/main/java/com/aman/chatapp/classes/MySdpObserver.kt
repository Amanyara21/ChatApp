package com.aman.chatapp.classes

import org.webrtc.*
import org.webrtc.PeerConnection.*


open class MyPeerConnectionObserver : Observer {
    override fun onSignalingChange(signalingState: SignalingState) {}
    override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {}
    override fun onIceConnectionReceivingChange(b: Boolean) {}
    override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {}
    override fun onIceCandidate(iceCandidate: IceCandidate) {}
    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {}
    override fun onAddStream(mediaStream: MediaStream) {}
    override fun onRemoveStream(mediaStream: MediaStream) {}
    override fun onDataChannel(dataChannel: DataChannel) {}
    override fun onRenegotiationNeeded() {}
    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
}
open class MySdpObserver : SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(s: String) {}
    override fun onSetFailure(s: String) {}
}