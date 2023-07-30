package com.example.macc

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.ActivityMainBinding
import com.example.macc.utility.UIDialogFragment
import com.example.macc.viewmodel.AuthViewModel
import com.example.macc.viewmodel.HomepageViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        //val userID = intent.getStringExtra("userID")
        sharedViewModelAuth.getUserMyProfile()
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        //Toast.makeText(applicationContext, "dialog", Toast.LENGTH_SHORT).show()
        sharedViewModelHomepage.deleteTravel()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
    }
}