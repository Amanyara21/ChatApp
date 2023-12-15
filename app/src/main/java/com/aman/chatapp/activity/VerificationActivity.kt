package com.aman.chatapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.aman.chatapp.R

class VerificationActivity : AppCompatActivity() {
    private lateinit var loginBtn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        loginBtn= findViewById(R.id.btnLogin)

        loginBtn.setOnClickListener{
            val intent = Intent(this@VerificationActivity, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }
    }
}
