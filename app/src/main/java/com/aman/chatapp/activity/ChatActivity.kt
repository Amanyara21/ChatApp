package com.aman.chatapp.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.chatapp.BuildConfig
import com.aman.chatapp.R
import com.aman.chatapp.adapter.MessageAdaptar
import com.aman.chatapp.models.Message
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject
import java.io.InputStream
import java.util.*

//
//
class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sentBtn: ImageView
    private lateinit var messageAdapter: MessageAdaptar
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var receiverUid:String?=null
    private var senderUid:String?=null

    private lateinit var videoCallButton:ImageView
    private lateinit var audioCallButton:ImageView
    private lateinit var apiKey:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initializeViews()
        setupActionBar()
        setupFirebase()
        setupRecyclerView()

        sentBtn.setOnClickListener {
                sendMessage()
        }
        videoCallButton.setOnClickListener {
//            sendCallRequest("video")
        }

        audioCallButton.setOnClickListener {
//            sendCallRequest("audio")
        }


        val properties = Properties()
        val inputStream: InputStream = assets.open("local.properties")
        properties.load(inputStream)

        apiKey = properties.getProperty("api_key")
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
            val customImageView: ImageView = customView.findViewById(R.id.customImageView)
            videoCallButton = customView.findViewById(R.id.icon2)
            audioCallButton= customView.findViewById(R.id.icon3)
            val customName: TextView? = customView.findViewById(R.id.nameTextView)
            customName?.text = intent.getStringExtra("name")
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
//                    scrollToBottom()
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
                            sendFCMNotification(receiverUid, senderUid, messageText, false,"")
                        }
                }
            messageBox.setText("")
        }
    }



    private fun sendFCMNotification(receiverUid: String?, senderUid: String?, content: String, isCall: Boolean, callId: String?) {
        if (receiverUid != null && senderUid != null) {
            dbRef.child("user").child(receiverUid).child("fcmToken")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val receiverToken = dataSnapshot.getValue(String::class.java)

                        if (receiverToken != null) {
                            var title: String
                            var body: String

                            if (isCall) {
                                title = "Incoming Call"
                                body = "You have an incoming call from $senderUid"

                                if (callId != null) {
                                    val notificationData = JSONObject().apply {
                                        put("senderUid", senderUid)
                                        put("callType", "incomingCall")
                                        put("callId", callId)
                                    }

                                    // Modify the FCM request to include data payload
                                    println("Hello I am sending data")
                                    val json = JSONObject().apply {
                                        put("to", receiverToken)
                                        put("notification", JSONObject().apply {
                                            put("title", title)
                                            put("body", body)
                                        })
                                        put("data",notificationData)
                                    }

                                    sendFCMRequest(json)
                                }
                            } else {
                                // Handle message notification
                                // Get sender's name
                                dbRef.child("user").child(senderUid).child("name")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(senderDataSnapshot: DataSnapshot) {
                                            val senderName = senderDataSnapshot.getValue(String::class.java)
                                            if (senderName != null) {
                                                title = "New Message from $senderName"
                                                body = content

                                                // Modify the FCM request for message notification
                                                val json = JSONObject().apply {
                                                    put("to", receiverToken)
                                                    put("notification", JSONObject().apply {
                                                        put("title", title)
                                                        put("body", body)
                                                    })
                                                    put("data", JSONObject().apply {
                                                        put("senderUid", senderUid)
                                                        put("senderName", senderName)
                                                    })
                                                }

                                                sendFCMRequest(json)
                                            }
                                        }

                                        override fun onCancelled(senderError: DatabaseError) {
                                            println("Error retrieving sender name: ${senderError.message}")
                                        }
                                    })
                            }
                        } else {
                            println("Failed to retrieve receiver token")
                        }
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
            val serverKey = "$apiKey"
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
        when (item.itemId) {
            R.id.profileChange -> {
                return true
            }
            R.id.logOut -> {
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }







}





//import androidx.appcompat.app.ActionBar
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.android.volley.AuthFailureError
//import com.android.volley.Request
//import com.android.volley.Response
//import com.android.volley.toolbox.JsonObjectRequest
//import com.android.volley.toolbox.Volley
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.RemoteMessage
//import org.json.JSONObject

//class ChatActivity : AppCompatActivity() {
//    lateinit var chatRecyclerView: RecyclerView
//    lateinit var messageBox : EditText
//    lateinit var sentbtn : ImageView
//    lateinit var messageAdaptar: MessageAdaptar
//    lateinit var messageList: ArrayList<Message>
//
//    lateinit var DbRef : DatabaseReference
//
//    var senderRoom : String? = null
//    var receiverRoom : String? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_chat)
//
//        DbRef = FirebaseDatabase.getInstance().reference
//        val name = intent.getStringExtra("name")
//
//        val actionBar = supportActionBar
//        actionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
//        actionBar?.setDisplayShowCustomEnabled(true)
//        actionBar?.setCustomView(R.layout.header_chat)
//
//        val customImageView: ImageView = actionBar?.customView?.findViewById(R.id.customImageView) as ImageView
////        val icon1: ImageView = actionBar.customView?.findViewById(R.id.icon1) as ImageView
//        val icon2: ImageView = actionBar.customView?.findViewById(R.id.icon2) as ImageView
//        val icon3: ImageView = actionBar.customView?.findViewById(R.id.icon3) as ImageView
//        val customName: TextView? = actionBar.customView?.findViewById(R.id.nameTextView);
//        customName?.text= name
//
//
//
//
//
//        val receiveruid = intent.getStringExtra("uid")
//        val senderuid = FirebaseAuth.getInstance().currentUser?.uid
//
//        senderRoom = receiveruid + senderuid
//        receiverRoom =  senderuid + receiveruid
//
//
//
//        chatRecyclerView = findViewById(R.id.Chatrecycler)
//        messageBox = findViewById(R.id.Editmsg)
//        sentbtn = findViewById(R.id.sentbtn)
//
//        messageList = ArrayList()
//        messageAdaptar = MessageAdaptar(this, messageList)
//
//        chatRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,true)
//        chatRecyclerView.scrollToPosition(100000);
//        chatRecyclerView.adapter = messageAdaptar
//
//        DbRef.child("chat").child(senderRoom!!).child("messages")
//            .addValueEventListener(object: ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//
//                    messageList.clear()
//                    for (postSnapshot in snapshot.children){
//                        val message = postSnapshot.getValue(Message::class.java)
//                        messageList.add(message!!)
//                    }
//                    messageAdaptar.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//            })
//
//
//        sentbtn.setOnClickListener {
//            val message = messageBox.text.toString()
//            val messageObject = Message(message, senderuid)
//            if(messageBox.text.toString().trim()!="") {
//                DbRef.child("chat").child(senderRoom!!).child("messages").push()
//                    .setValue(messageObject).addOnSuccessListener {
//                        DbRef.child("chat").child(receiverRoom!!).child("messages").push()
//                            .setValue(messageObject)
//                            .addOnSuccessListener {
//                                sendFCMNotification(receiveruid,senderuid, message)
//                            }
//                    }
//
//                messageBox.setText("")
//
//            }
//        }
//
//    }
//
//    private fun sendFCMNotification(receiverUid: String?, senderUid: String?, message: String) {
//        if (receiverUid != null && senderUid != null) {
//            // Get receiver's token
//            DbRef.child("user").child(receiverUid).child("fcmToken").addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    val receiverToken = dataSnapshot.getValue(String::class.java)
//
//                    // Get sender's name
//                    DbRef.child("user").child(senderUid).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(senderDataSnapshot: DataSnapshot) {
//                            val senderName = senderDataSnapshot.getValue(String::class.java)
//
//                            // Now you have both receiver's token and sender's name
//                            println("Receiver Token: $receiverToken")
//                            println("Sender Name: $senderName")
//                            if (receiverToken != null && senderName != null) {
//                                val title = "New Message from $senderName"
//
//                                try{
//                                    val serverKey = "AAAAdlqTe9U:APA91bHi7JfcxN7I_ohqNjPc_2wfvbZAEPwjyg_ZaWGg0AcRpbb5D0EJOR4LOkBFKNFbv-5iNsUAbMj2I2HAjsTjjJYBNl1gK2RO8VLSPqZpu7pqjri_U-T9jkMkvNBhW7wiHfLmCRvw"
//                                    val fcmEndpoint = "https://fcm.googleapis.com/fcm/send"
//
//                                    val json = JSONObject().apply {
//                                        put("to", receiverToken)
//                                        put("notification", JSONObject().apply {
//                                            put("title", title)
//                                            put("body", message)
//                                            put("senderUid", senderUid)
//                                            put("senderName", senderName)
//                                        })
//                                    }
//
//                                    val request = object : JsonObjectRequest(Method.POST, fcmEndpoint, json,
//                                        Response.Listener { response ->
//                                            println("FCM notification sent successfully: $response")
//                                        },
//                                        Response.ErrorListener { error ->
//                                            println("Failed to send FCM notification: ${error.message}")
//                                        }) {
//                                        override fun getHeaders(): Map<String, String> {
//                                            val headers = HashMap<String, String>()
//                                            headers["Authorization"] = "key=$serverKey"
//                                            return headers
//                                        }
//                                    }
//                                    // Add the request to the Volley request queue
//                                    Volley.newRequestQueue(this@ChatActivity).add(request)
//                                }catch (e:Exception){
//                                    println("Error is $e")
//                                }
//                            } else {
//                                println("Failed to retrieve receiver token or sender name")
//                            }
//                        }
//
//                        override fun onCancelled(senderError: DatabaseError) {
//                            println("Error retrieving sender name: ${senderError.message}")
//                        }
//                    })
//                }
//
//                override fun onCancelled(receiverError: DatabaseError) {
//                    println("Error retrieving receiver token: ${receiverError.message}")
//                }
//            })
//        }
//    }
//}