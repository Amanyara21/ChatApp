package com.aman.chatapp.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SendEmail {
    private val Auth = FirebaseAuth.getInstance()
    fun sendVerificationEmail(user: FirebaseUser?, callback: (Boolean) -> Unit) {
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            callback(task.isSuccessful)
        }
    }
    fun sendResetPasswordEmail(email: String, callback: (Boolean) -> Unit) {
        Auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
    }

}