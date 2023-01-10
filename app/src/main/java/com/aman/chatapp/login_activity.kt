package com.aman.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class login_activity : AppCompatActivity() {
    lateinit var EmailId : EditText
    lateinit var loginBtn : Button
    lateinit var pasword : EditText
    lateinit var signUp : TextView
    lateinit var Auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val i = Intent(this@login_activity, MainActivity::class.java)
            finish()
            startActivity(i)
        }
        EmailId = findViewById(R.id.Editmail)
        loginBtn = findViewById(R.id.btnlogin)
        pasword = findViewById(R.id.Editpass)
        signUp = findViewById(R.id.txtSign)
        Auth = FirebaseAuth.getInstance()
        signUp.setOnClickListener{
            val intent = Intent(this@login_activity, signup::class.java)
            startActivity(intent)
            finish()
        }
        loginBtn.setOnClickListener{
            val email = EmailId.text.toString().trim()
            val pass = pasword.text.toString()
            login(email, pass)
        }

        title ="Log In"
    }
    private fun login(email: String, pass : String){
        Auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@login_activity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@login_activity, "User Not Found or Wrong password", Toast.LENGTH_SHORT).show()
                }
            }
    }

}