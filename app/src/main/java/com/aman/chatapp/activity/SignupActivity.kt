package com.aman.chatapp.activity

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.aman.chatapp.R
import com.aman.chatapp.models.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class SignupActivity : AppCompatActivity() {
    private lateinit var Name : EditText
    private lateinit var EmailId : EditText
    lateinit var password : EditText
    lateinit var signUpButton: Button
    private lateinit var Auth : FirebaseAuth
    private lateinit var DbRef : DatabaseReference
    lateinit var image : CircleImageView
    private lateinit var StrgRef : StorageReference
    private val gallery_request =1000;
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        EmailId = findViewById(R.id.Editmail)
        Name = findViewById(R.id.Editname)
        password = findViewById(R.id.Editpass)
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
        if (!clicked){
            signUpButton.setOnClickListener{
                val name = Name.text.toString()
                val email = EmailId.text.toString().trim()
                val pass = password.text.toString()

                signUp(name!!, email!!, pass!!)
            }
        }


        title ="Sign Up"
    }

    private fun uploadToFirebase(imageUri: Uri, uid: String, name: String, email: String) {
        val imageRef = StrgRef.child("profile_images/${uid}.jpg")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                storeInDatabase(uri.toString(), uid, name, email)
            }
        }.addOnFailureListener {

        }
    }
    private fun storeInDatabase(imageUrl: String, uid: String, name: String, email: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("user")
        println("uid + $uid")
        val user = user(uid, name, email, imageUrl)

        databaseReference.child(uid).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this@SignupActivity, "User registered successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@SignupActivity, "Failed to register user", Toast.LENGTH_SHORT).show()
            }
    }



    private fun signUp(name: String, email: String, pass: String) {
        Auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendVerificationEmail()
                    val uid = Auth.currentUser?.uid
                    if (imageUri != null) {
                        uid?.let {
                            uploadToFirebase(imageUri!!, it, name, email)
                        }
                    } else {
                        addToDatabase(name, email, uid!!)
                    }
                    Auth.signOut()
                    val intent = Intent(this@SignupActivity, VerificationActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignupActivity, "Some Error Occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == gallery_request){
            image.setImageURI(data?.data)
            imageUri= data?.data
        }

    }
    private fun addToDatabase(name: String, email: String, uid: String) {
        DbRef = FirebaseDatabase.getInstance().reference
        println("uid  $uid name $name email $email ")
        DbRef.child("user").child(uid).setValue(user(name, email, uid))
    }
    private fun sendVerificationEmail() {
        val user = Auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@SignupActivity, "Verification email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignupActivity, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}