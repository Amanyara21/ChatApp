package com.aman.chatapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.chatapp.R
import com.aman.chatapp.adapter.MessageAdaptar
import com.aman.chatapp.models.Message
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sentBtn: ImageView
    private lateinit var messageAdapter: MessageAdaptar
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var receiverUid: String? = null
    private var senderUid: String? = null

    private lateinit var videoCallButton: ImageView
    private lateinit var customImageView: CircleImageView
    private lateinit var customName:TextView
    private lateinit var audioCallButton: ImageView
    val apiKey = "add_api_key" //Github security warnings




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initializeViews()
        setupActionBar()
        setupFirebase()
        setupRecyclerView()

        val image: Int = intent.getIntExtra("image", 0)
        val name: String? = intent.getStringExtra("name")

        sentBtn.setOnClickListener {
            sendMessage()
        }
        audioCallButton.setOnClickListener {
            println("Clicked Audio Call Button")
            val callId =UUID.randomUUID().toString()
            senderUid?.let { it1 -> receiverUid?.let { it2 ->
                updateCallStatus(callId, it1, it2, "initiated")
            } }

            sendIntent(senderUid, receiverUid, name!!, image, callId,"sender", AudioCallActivity::class.java)
            sendFCMNotification(receiverUid, senderUid, "", true, callId , "audioCall")
        }

        videoCallButton.setOnClickListener {
            val callId =UUID.randomUUID().toString()
            sendFCMNotification(receiverUid, senderUid, "", true, callId , "videoCall")

            sendIntent(senderUid, receiverUid,name, image, callId,"sender", VideoCallActivity::class.java)

        }

    }

    private fun sendIntent(
        senderUid: String?,
        receiverUid: String?,
        name:String?,
        image:Int,
        callId: String,
        s: String,
        java: Class<*>) {
        val intent = Intent(this, java)
        intent.putExtra("senderUid", senderUid)
        intent.putExtra("receiverUid", receiverUid)
        intent.putExtra("name", name)
        intent.putExtra("image", image)
        intent.putExtra("callId", callId)
        intent.putExtra("user", s)
        startActivity(intent)

    }

    fun updateCallStatus(callId: String, senderUid: String, receiverUid: String, status: String) {
        val callRef = FirebaseDatabase.getInstance().getReference("calls").child(callId)
        callRef.child("status").setValue(status)
        callRef.child("participants").child(senderUid).setValue(true)
        callRef.child("participants").child(receiverUid).setValue(true)
    }



    private fun initializeViews() {
        chatRecyclerView = findViewById(R.id.Chatrecycler)
        messageBox = findViewById(R.id.Editmsg)
        sentBtn = findViewById(R.id.sentbtn)
        messageList = ArrayList()
        messageAdapter = MessageAdaptar(this, messageList)
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.header_chat)
            customImageView = customView.findViewById(R.id.customImageView)
            audioCallButton = customView.findViewById(R.id.icon2)
            videoCallButton = customView.findViewById(R.id.icon3)
            customName = customView.findViewById(R.id.nameTextView)
            customName.text = intent.getStringExtra("name")
            val temp:Int = intent.getIntExtra("image", 0)
            if(temp!=0) { customImageView.setImageResource(temp)}
        }
    }

    private fun setupFirebase() {
        dbRef = FirebaseDatabase.getInstance().reference
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
    }

    private fun setupRecyclerView() {
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity, RecyclerView.VERTICAL, true)
            adapter = messageAdapter
        }

        dbRef.child("chat").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageList.reverse()
                    messageAdapter.notifyDataSetChanged()
                }


                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun sendMessage() {
        val messageText = messageBox.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val messageObject = Message(messageText, FirebaseAuth.getInstance().currentUser?.uid)
            dbRef.child("chat").child(senderRoom!!).child("messages").push()
                .setValue(messageObject)
                .addOnSuccessListener {
                    dbRef.child("chat").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                        .addOnSuccessListener {
                            sendFCMNotification(receiverUid, senderUid, messageText, false, "",null)
                        }
                }
            messageBox.setText("")
        }
    }


    private fun sendFCMNotification(
        receiverUid: String?,
        senderUid: String?,
        content: String,
        isCall: Boolean,
        callId: String?,
        type:String?
    ) {
        if (receiverUid != null && senderUid != null) {
            dbRef.child("user").child(receiverUid).child("fcmToken")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val receiverToken = dataSnapshot.getValue(String::class.java)

                        dbRef.child("user").child(senderUid).child("name")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(senderDataSnapshot: DataSnapshot) {

                                    if (receiverToken != null) {
                                        var title: String
                                        var body: String

                                        if (isCall) {
                                            title = "Incoming Call"
                                            body = "You have an incoming call from $senderUid"

                                            if (callId != null) {

                                                val notificationData = JSONObject().apply {
                                                    put("title", title)
                                                    put("body", body)
                                                    put("senderUid", senderUid)
                                                    put("receiverUid", receiverUid)
                                                    put("callType", "incomingCall")
                                                    put("callId", callId)
                                                    put("type", type)
                                                }

                                                // Modify the FCM request to include data payload
                                                println("Hello I am sending data")
                                                val json = JSONObject().apply {
                                                    put("to", receiverToken)
//                                                    put("notification", JSONObject().apply {
//                                                        put("title", title)
//                                                        put("body", body)
//                                                    })
                                                    put("data", notificationData)
                                                }

                                                sendFCMRequest(json)
                                            }
                                        } else {
                                            // Handle message notification
                                            // Get sender's name

                                            val senderName =
                                                senderDataSnapshot.getValue(String::class.java)
                                            if (senderName != null) {
                                                title = "New Message from $senderName"
                                                body = content

                                                // Modify the FCM request for message notification
                                                val json = JSONObject().apply {
                                                    put("to", receiverToken)
//                                                    put("notification", JSONObject().apply {
//                                                        put("title", title)
//                                                        put("body", body)
//                                                    })
                                                    put("data", JSONObject().apply {
                                                        put("title", title)
                                                        put("body", body)
                                                        put("senderUid", senderUid)
                                                        put("senderName", senderName)
                                                    })
                                                }

                                                sendFCMRequest(json)
                                            }
                                        }
                                    }
                                }
                                override fun onCancelled(senderError: DatabaseError) {
                                    println("Error retrieving sender name: ${senderError.message}")
                                }
                            })
                    }
                    override fun onCancelled(receiverError: DatabaseError) {
                        println("Error retrieving receiver token: ${receiverError.message}")
                    }
                })
        }
    }

    // Function to send the FCM request
    private fun sendFCMRequest(json: JSONObject) {
        try {
            val serverKey = apiKey
            val fcmEndpoint = "https://fcm.googleapis.com/fcm/send"

            val request = object :
                JsonObjectRequest(Method.POST, fcmEndpoint, json,
                    Response.Listener { response ->
                        println("FCM notification sent successfully: $response")
                    },
                    Response.ErrorListener { error ->
                        println("Failed to send FCM notification: ${error.message}")
                    }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "key=$serverKey"
                    return headers
                }
            }

            // Add the request to the Volley request queue
            Volley.newRequestQueue(this@ChatActivity).add(request)
        } catch (e: Exception) {
            println("Error is $e")
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.more_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profileChange -> {
                true
            }

            R.id.logOut -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
