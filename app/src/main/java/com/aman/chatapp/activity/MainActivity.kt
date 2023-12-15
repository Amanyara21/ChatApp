package com.aman.chatapp.activity

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.chatapp.adapter.Adaptar
import com.aman.chatapp.R
import com.aman.chatapp.classes.PermissionHandler
import com.aman.chatapp.models.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var userView: RecyclerView
    private lateinit var userList: ArrayList<user>
    private lateinit var adapter: Adaptar
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var permissionHandler: PermissionHandler

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



        permissionHandler = PermissionHandler(this)
        if (!permissionHandler.checkReadExternalStoragePermission()) {
            permissionHandler.requestReadExternalStoragePermission()
        }

        if (!permissionHandler.checkInternetPermission()) {
            permissionHandler.requestInternetPermission()
        }
        if (!permissionHandler.checkAccessNetworkStatePermission()) {
            permissionHandler.requestAccessNetworkStatePermission()
        }

        if (!permissionHandler.checkRecordAudioPermission()) {
            permissionHandler.requestRecordAudioPermission()
        }

        if (!permissionHandler.checkModifyAudioSettingsPermission()) {
            permissionHandler.requestModifyAudioSettingsPermission()
        }

        if (!permissionHandler.checkCameraPermission()) {
            permissionHandler.requestCameraPermission()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupUserListListener() {
        dbRef.child("user").addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (Snapshot in snapshot.children){
                    val currentUser = Snapshot.getValue(user::class.java)
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
        when (item.itemId) {
            R.id.logOut -> {
                auth.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return true
            }
            R.id.profileChange -> {
                startActivity(Intent(this@MainActivity, ChangeProfilePic::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
