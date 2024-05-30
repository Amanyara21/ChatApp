package com.aman.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aman.chatapp.activity.ChatActivity
import com.aman.chatapp.R
import com.aman.chatapp.models.user
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class Adaptar(val context: Context, val userList: ArrayList<user>) : RecyclerView.Adapter<Adaptar.UserViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.user_recycler, parent , false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.textName.text = currentUser.name
        println("user is "+currentUser.name)
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${currentUser.email}" )

        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            holder.profileImage.setImageBitmap(bitmap)

        }
//        holder.profileImage.setOnClickListener{
//            Toast.makeText(context, "Image clicked", Toast.LENGTH_SHORT).show()
//        }
        holder.itemView.setOnClickListener{

                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("name", currentUser.name)
                intent.putExtra("uid", currentUser.uid)
                intent.putExtra("email", currentUser.email)
                intent.putExtra("image", currentUser.image)
                context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var textName: TextView = itemView.findViewById(R.id.textName)
        val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
    }

}