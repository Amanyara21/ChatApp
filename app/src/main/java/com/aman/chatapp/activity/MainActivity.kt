package com.aman.chatapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.chatapp.R
import com.aman.chatapp.adapter.Adaptar
import com.aman.chatapp.classes.PermissionManager
import com.aman.chatapp.models.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    private lateinit var userView: RecyclerView
    private lateinit var userList: ArrayList<user>
    private lateinit var adapter: Adaptar
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var permissionManager: PermissionManager
    private val permissionsRequestCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        dbRef = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        userList = ArrayList()
        adapter = Adaptar(this, userList)

        userView = findViewById(R.id.userView)
        userView.layoutManager = LinearLayoutManager(this)
        userView.adapter = adapter

        setupUserListListener()

        fetchAndStoreFCMToken()



        askPermission();
}
    private fun askPermission() {
        val list = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
        permissionManager = PermissionManager(this,list,permissionsRequestCode)
        permissionManager.checkPermissions()
    }




    private fun setupUserListListener() {
        dbRef.child("user").addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in snapshot.children){
                    val currentUser = snapshot.getValue(user::class.java)
                    if (auth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun fetchAndStoreFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Token: $token")

                val tokenUpdate = mapOf(
                    "fcmToken" to token
                )

                auth.currentUser?.uid?.let { uid ->
                    dbRef.child("user").child(uid).updateChildren(tokenUpdate)
                }
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        when (menu) {
            is MenuBuilder -> {
                val m = menu
                m.setOptionalIconsVisible(true)
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logOut -> {
                auth.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                true
            }

            R.id.profileChange -> {
                startActivity(Intent(this@MainActivity, VideoCallActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
