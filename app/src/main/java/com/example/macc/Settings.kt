package com.example.macc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class Settings : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings_container,
                SettingsPreferences()
            )
            .commit()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings_container,
                SettingsPreferences()
            )
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.settings_page, container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homepage, R.id.settings, R.id.userMyProfile))
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)

        toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}

