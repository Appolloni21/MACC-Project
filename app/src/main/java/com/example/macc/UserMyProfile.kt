package com.example.macc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.ItemAdapter
import com.example.macc.data.Datasource

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class UserMyProfile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.user_my_profile_page, container,
            false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val logoutButton: Button = view.findViewById(R.id.logout_btn)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(
                activity,
                "You are now logged out",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
            //true
        }
    }

}