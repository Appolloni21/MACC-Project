package com.example.macc


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel

private const val TAG = "User My Profile Fragment"

class UserMyProfile : Fragment() {

    private val sharedViewModel: AuthViewModel by activityViewModels()
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.user_my_profile_page, container,
            false)

        sharedViewModel.userMyProfile.observe(viewLifecycleOwner){
            if(it?.equals(null) == false){
                //Avatar
                val userAvatar = view.findViewById<ImageView>(R.id.myUser_avatar)
                Glide.with(view).load(it.avatar).into(userAvatar)

                //Altri campi
                view.findViewById<TextView>(R.id.user_name_surname).text = it.name + " " + it.surname
                view.findViewById<TextView>(R.id.user_nickname).text = it.nickname
                view.findViewById<TextView>(R.id.user_description).text = it.description
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homepage, R.id.userMyProfile))
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    val action = UserMyProfileDirections.actionUserMyProfileToSettings()
                    view.findNavController().navigate(action)
                }
            }
            true
        }


        val logoutButton: Button = view.findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            sharedViewModel.logOutUser()
        }

        val editMyProfileButton: Button = view.findViewById(R.id.edit_myProfile_btn)
        editMyProfileButton.setOnClickListener {
            val action = UserMyProfileDirections.actionUserMyProfileToEditUserMyProfile()
            view.findNavController().navigate(action)
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context, "You are now logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity?.finish()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in logging out", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

        Log.d(TAG,"User My Profile Page")
    }

}