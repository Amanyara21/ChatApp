package com.aman.chatapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class Adaptar(val context: Context, val userList : ArrayList<user>) : RecyclerView.Adapter<Adaptar.userViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.user_recycler, parent , false)
        return userViewHolder(view)
    }

    override fun onBindViewHolder(holder: userViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.textName.text = currentUser.name
        val storageReference = FirebaseStorage.getInstance().getReference().child("images/${currentUser.email}" )

        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            holder.profileImage.setImageBitmap(bitmap)

        }
//        holder.profileImage.setOnClickListener{
//            Toast.makeText(context, "Image clicked", Toast.LENGTH_SHORT).show()
//        }
        holder.itemView.setOnClickListener{

                val intent = Intent(context, Chat::class.java)
                intent.putExtra("name", currentUser.name)
                intent.putExtra("uid", currentUser.uid)
                intent.putExtra("email", currentUser.email)
                context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
    class userViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var textName = itemView.findViewById<TextView>(R.id.textName)
        val profileImage = itemView.findViewById<CircleImageView>(R.id.profile_image)
    }

}