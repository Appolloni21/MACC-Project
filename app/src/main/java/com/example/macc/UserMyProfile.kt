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
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.macc.databinding.UserMyProfilePageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel

private const val TAG = "User My Profile Fragment"

class UserMyProfile : Fragment() {

    private var _binding: UserMyProfilePageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: AuthViewModel by activityViewModels()
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = UserMyProfilePageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        sharedViewModel.userMyProfile.observe(viewLifecycleOwner){
            if(it?.equals(null) == false){
                //Avatar
                val myUserAvatar = binding.myUserAvatar
                Glide.with(view).load(it.avatar).into(myUserAvatar)

                //Altri campi
                binding.userNameSurname.text = it.name + " " + it.surname
                binding.userNickname.text = it.nickname
                binding.userDescription.text = it.description
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homepage, R.id.settings, R.id.userMyProfile))
        //val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbar.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)


        val logoutButton: Button = binding.logoutBtn
        logoutButton.setOnClickListener {
            sharedViewModel.logOutUser()
        }

        val editMyProfileButton: Button = binding.editMyProfileBtn
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}