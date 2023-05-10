package com.example.macc


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.macc.data.HomepageViewModel


private const val TAG = "User Profile Fragment"
class UserProfile : Fragment() {

    private var userPosition: Int = 0
    private val sharedViewModel: HomepageViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.user_profile_page, container,
            false)

        userPosition = arguments?.getInt("userPosition")!!
        sharedViewModel.users.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                val user = it[userPosition]
                view.findViewById<TextView>(R.id.userNameSurname).text = user.name + " " + user.surname
                view.findViewById<TextView>(R.id.userNickname).text = user.nickname
                view.findViewById<TextView>(R.id.userDescription).text = user.description
                //Carichiamo l'avatar
                Glide.with(view).load(user.avatar).into(view.findViewById(R.id.userAvatar))
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)

    }

}
