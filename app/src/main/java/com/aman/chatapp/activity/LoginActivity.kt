package com.aman.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aman.chatapp.R
import com.aman.chatapp.services.SendEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var EmailId: EditText
    private lateinit var loginBtn: Button
    private lateinit var pasword: EditText
    private lateinit var signUp: TextView
    private lateinit var forgotPass: TextView
    private lateinit var auth: FirebaseAuth
    private val emailSender = SendEmail()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeVariables()

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }

        setupListeners()
        title = "Log In"
    }

    private fun initializeVariables() {
        EmailId = findViewById(R.id.Editmail)
        loginBtn = findViewById(R.id.btnlogin)
        pasword = findViewById(R.id.Editpass)
        signUp = findViewById(R.id.txtSign)
        forgotPass = findViewById(R.id.txtForgotPass)
        auth = FirebaseAuth.getInstance()
    }

    private fun setupListeners() {
        signUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }

        loginBtn.setOnClickListener {
            val email = EmailId.text.toString().trim()
            val password = pasword.text.toString()
            login(email, password)
        }

        forgotPass.setOnClickListener {
            val email = EmailId.text.toString().trim()
            if (email.isNotEmpty()) {
                sendResetPasswordEmail(email)
            } else {
                showToast("Please enter your email")
            }
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleSuccessfulLogin(auth.currentUser)
                } else {
                    showToast("User not found or wrong password. Please check your credentials.")
                }
            }
    }

    private fun handleSuccessfulLogin(user: FirebaseUser?) {
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        } else {
            sendVerificationEmail(user)
        }
    }

    private fun sendVerificationEmail(user: FirebaseUser?) {
        emailSender.sendVerificationEmail(user) { isSend ->
            if (isSend) {
                showToast("Verification email sent. Please check your email.")
                startActivity(Intent(this@LoginActivity, VerificationActivity::class.java))
                auth.signOut()
                finish()
            } else {
                showToast("Failed to send verification email")
            }
        }
    }

    private fun sendResetPasswordEmail(email: String) {
        emailSender.sendResetPasswordEmail(email) { isSend ->
            if (isSend) {
                showToast("Password reset email sent to $email")
            } else {
                showToast("Failed to send password reset email. Please check your email and try again.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }
}
