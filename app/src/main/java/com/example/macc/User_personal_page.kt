package com.example.macc

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.ItemAdapter
import com.example.macc.data.Datasource

import com.google.android.material.bottomnavigation.BottomNavigationView


class User_personal_page : Fragment() {
    companion object {

        fun newInstance(): User_personal_page {
            return User_personal_page()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.user1_personal_page, container,
            false)
        return view
    }


}