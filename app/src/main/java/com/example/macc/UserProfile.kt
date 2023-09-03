package com.example.macc


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.macc.databinding.UserProfilePageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel


private const val TAG = "User Profile Fragment"
class UserProfile : Fragment() {

    private var _binding: UserProfilePageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = UserProfilePageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        sharedViewModel.userSelected.observe(viewLifecycleOwner){ user ->
            if(user != null){
                binding.userNameSurname.text = user.name + " " + user.surname
                binding.userNickname.text = user.nickname
                binding.userDescription.text = user.description
                //Carichiamo l'avatar
                Glide.with(view).load(user.avatar).into(binding.userAvatar)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbar.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)
        Log.d(TAG,"User profile")

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.WARN_104 ->{
                    Toast.makeText(context,"You are not anymore in this travel", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigate(R.id.homepage)
                }
            }
        }

    }

}
