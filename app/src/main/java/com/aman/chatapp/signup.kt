package com.aman.chatapp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.LiveFolders.INTENT
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class signup : AppCompatActivity() {
    lateinit var Name : EditText
    lateinit var EmailId : EditText
    lateinit var pasword : EditText
    lateinit var signUpButton: Button
    lateinit var Auth : FirebaseAuth
    lateinit var DbRef : DatabaseReference
    lateinit var image : CircleImageView
    lateinit var StrgRef : StorageReference
    val gallery_request =1000;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        EmailId = findViewById(R.id.Editmail)
        Name = findViewById(R.id.Editname)
        pasword = findViewById(R.id.Editpass)
        signUpButton = findViewById(R.id.btnSign)
        Auth = FirebaseAuth.getInstance()
        image = findViewById(R.id.profile_image)

        var clicked = false
        StrgRef = FirebaseStorage.getInstance().reference
        image.setOnClickListener{
            val gallery = Intent(ACTION_PICK)
            gallery.type ="image/*"
            clicked = true
            startActivityForResult(gallery, gallery_request)
        }
        if (clicked== false){
            signUpButton.setOnClickListener{
                val name = Name.text.toString()
                val email = EmailId.text.toString().trim()
                val pass = pasword.text.toString()

                signUp(name, email, pass)
            }
        }


        title ="Sign Up"
    }

    private fun uploadToFirebase(imageUri : Uri, name: String) {
        val user =0;
        val uploadTask = StrgRef.child("images/${name}").putFile(imageUri)
    }

    private fun signUp(name : String, email: String, pass: String) {
        Auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addToDatabase(name, email, Auth.currentUser?.uid!!)
                    val intent = Intent(this@signup, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@signup, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == gallery_request){
            image.setImageURI(data?.data)
            signUpButton.setOnClickListener{
                val name = Name.text.toString()
                val email = EmailId.text.toString().trim()
                val pass = pasword.text.toString()
                uploadToFirebase(data?.data!!, email)

                signUp(name, email, pass)
            }
        }

    }
    private fun addToDatabase(name: String, email: String, uid: String) {
        DbRef = FirebaseDatabase.getInstance().getReference()
        DbRef.child("user").child(uid).setValue(user(name, email, uid))
    }
}