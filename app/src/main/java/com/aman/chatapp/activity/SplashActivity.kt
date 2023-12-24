package com.aman.chatapp.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.aman.chatapp.R
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentValue = sharedPreferences.getBoolean("isPersistenceEnabled",false)
        if(!currentValue){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            sharedPreferences.edit().putBoolean("isPersistenceEnabled", true).apply()

        }


        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}