package com.aman.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    lateinit var userView: RecyclerView
    lateinit var userList: ArrayList<user>
    lateinit var adaptar: Adaptar
    lateinit var DbRef : DatabaseReference
    lateinit var strgRef : StorageReference

    lateinit var Auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DbRef = FirebaseDatabase.getInstance().reference


        Auth= FirebaseAuth.getInstance()
        userList = ArrayList()
        adaptar = Adaptar(this, userList)

        userView = findViewById(R.id.userView)
        userView.layoutManager = LinearLayoutManager(this)
        userView.adapter = adaptar



        DbRef.child("user").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (Snapshot in snapshot.children){
                    val currentUser = Snapshot.getValue(user::class.java)
                    if (Auth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adaptar.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logOut ){
            Auth.signOut()
            val intent = Intent(this@MainActivity, login_activity::class.java)
            finish()
            startActivity(intent)
            return true
        }
        if(item.itemId == R.id.profileChange) {
            DbRef.child("user").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                        val currentUser = snapshot.getValue(user::class.java)
                        val intent = Intent(this@MainActivity, ChangeProfilePic::class.java)
                        intent.putExtra("email", Auth.currentUser?.email)
                        startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            return true
        }
        return true
    }
}