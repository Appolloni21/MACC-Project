package com.example.macc

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.ActivityMainBinding
import com.example.macc.utility.UIDialogFragment
import com.example.macc.viewmodel.AuthViewModel
import com.example.macc.viewmodel.HomepageViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val TAG = "Main Activity"

class MainActivity : AppCompatActivity(), UIDialogFragment.NoticeDialogListener {
    private lateinit var navController: NavController

    private val sharedViewModelAuth: AuthViewModel by viewModels()
    private val sharedViewModelHomepage: HomepageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Instantiate the navController using the NavHostFragment
        navController = navHostFragment.navController

        //Bottom bar setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // always show selected Bottom Navigation item as selected (return true)
        bottomNavigationView.setOnItemSelectedListener { item ->
            // In order to get the expected behavior, you have to call default Navigation method manually
            NavigationUI.onNavDestinationSelected(item, navController)

            return@setOnItemSelectedListener true
        }

        //val userID = intent.getStringExtra("userID")
        sharedViewModelAuth.getUserMyProfile()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        //NOTIFICATION
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        //Toast.makeText(applicationContext, "dialog", Toast.LENGTH_SHORT).show()
        when(dialog.tag){
            "UIDialog - deleteTravel" ->{
                sharedViewModelHomepage.deleteTravel()
            }
            "UIDialog - deleteExpense" ->{
                sharedViewModelHomepage.deleteExpense()
            }
            "UIDialog - quitTravel" -> {
                sharedViewModelHomepage.quitFromTravel()
            }
            "UIDialog - removeUserFromTravel" -> {
                sharedViewModelHomepage.removeUserFromTravel()
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
        Log.d(TAG,"${dialog.tag}")
    }
}