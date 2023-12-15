package com.aman.chatapp.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.aman.chatapp.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class ChangeProfilePic : AppCompatActivity() {
    lateinit var image : CircleImageView
    lateinit var StrgRef : StorageReference
    lateinit var Setbtn : Button
    val gallery_request =1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_profile_pic)
        image = findViewById(R.id.profile_image)
        StrgRef = FirebaseStorage.getInstance().reference
        Setbtn = findViewById(R.id.setImg)
        image.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK)
            gallery.type ="image/*"

            startActivityForResult(gallery, gallery_request)
        }

    }
    private fun uploadToFirebase(imageUri : Uri, email: String?) {
        val uploadTask = StrgRef.child("images/${email}").putFile(imageUri)
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == gallery_request){
            image.setImageURI(data?.data)
            Setbtn.setOnClickListener{
                val email = intent.getStringExtra("email")
                println(email)
                uploadToFirebase(data?.data!!, email)
//                val backmain = Intent(this, MainActivity::class.java )
//                startActivity(backmain)
                finish()
            }
        }

    }
}