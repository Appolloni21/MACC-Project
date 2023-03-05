package com.example.macc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.ItemAdapter
import com.example.macc.data.Datasource
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide();

        super.onCreate(savedInstanceState)
        //setContentView(R.layout.user_my_profile_page)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {

            supportFragmentManager.beginTransaction().add(R.id.container, Homepage.newInstance(), "Homepage").commit()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.profile -> {
                    supportFragmentManager.beginTransaction().add(R.id.container, User_personal_page.newInstance(), "Homepage").commit()                }
                R.id.home -> {
                    supportFragmentManager.beginTransaction().add(R.id.container, Homepage.newInstance(), "Homepage").commit()
                }
                R.id.notifications -> {
                    Toast.makeText(applicationContext,"notification not implemented yet", Toast.LENGTH_SHORT).show()
                }
            }
            false
        }
        //TEST PER LA HOMEPAGE
        // Initialize data.
        //val myDataset = Datasource().loadTravels()

        //val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        //recyclerView.adapter = ItemAdapter(this, myDataset)

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true)


        //Da spostare poi nel fragment della user_my_profile_page
        //Handle logout
        /*val logoutButton: Button = findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(
                this@MainActivity,
                "You are now logged out",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            this@MainActivity.finish()
            //true
        }*/

    }
}