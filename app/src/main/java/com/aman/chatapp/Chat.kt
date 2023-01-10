package com.aman.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Chat : AppCompatActivity() {
    lateinit var chatRecyclerView: RecyclerView
    lateinit var messageBox : EditText
    lateinit var sentbtn : ImageView
    lateinit var messageAdaptar: MessageAdaptar
    lateinit var messageList: ArrayList<Message>

    lateinit var DbRef : DatabaseReference

    var senderRoom : String? = null
    var receiverRoom : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        DbRef = FirebaseDatabase.getInstance().reference
        val name = intent.getStringExtra("name")
        val receiveruid = intent.getStringExtra("uid")
        val senderuid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = receiveruid + senderuid
        receiverRoom =  senderuid + receiveruid
        supportActionBar?.title = name


        chatRecyclerView = findViewById(R.id.Chatrecycler)
        messageBox = findViewById(R.id.Editmsg)
        sentbtn = findViewById(R.id.sentbtn)

        messageList = ArrayList()
        messageAdaptar = MessageAdaptar(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdaptar

        DbRef.child("chat").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()
                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdaptar.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


            sentbtn.setOnClickListener {
                val message = messageBox.text.toString()
                val messageObject = Message(message, senderuid)
                if(messageBox.text.toString().trim()!="") {
                DbRef.child("chat").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        DbRef.child("chat").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }

                messageBox.setText("")

            }
        }

    }
}