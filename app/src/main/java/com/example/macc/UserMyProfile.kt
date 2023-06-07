package com.example.macc


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel

private const val TAG = "User My Profile Fragment"

class UserMyProfile : Fragment() {

    private val sharedViewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.user_my_profile_page, container,
            false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homepage, R.id.userMyProfile))
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)


        val logoutButton: Button = view.findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            sharedViewModel.logOutUser()
        }

        sharedViewModel.logOutState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context, "You are now logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity?.finish()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in logging out", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}