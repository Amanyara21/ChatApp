package com.aman.chatapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aman.chatapp.models.Message
import com.aman.chatapp.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdaptar(val context : Context, val MessageList : ArrayList<Message>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE =1
    val ITEM_SENT =2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType ==1){
            val view : View = LayoutInflater.from(context).inflate(R.layout.receivemsg, parent , false)
            return receiveViewHolder(view)
        }
        else{
            val view : View = LayoutInflater.from(context).inflate(R.layout.sentmsg, parent , false)
            return sentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = MessageList[position]

        if (holder.javaClass == sentViewHolder::class.java) {
            val viewHolder = holder as sentViewHolder
            viewHolder.sentMessage.text = currentMessage.message
        } else {
            val viewHolder = holder as receiveViewHolder
            viewHolder.receiveMessage.text = currentMessage.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = MessageList[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }
        else{
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return MessageList.size
    }

    class sentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.textsent)
    }
    class receiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receiveMessage = itemView.findViewById<TextView>(R.id.textreceive)
    }
}