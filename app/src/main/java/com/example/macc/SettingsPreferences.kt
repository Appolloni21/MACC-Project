package com.example.macc

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.macc.viewmodel.AuthViewModel


class SettingsPreferences : PreferenceFragmentCompat() {

    private val sharedViewModel: AuthViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Display user email
        val emailPreference: Preference? = findPreference("email")
        sharedViewModel.userMyProfile.observe(viewLifecycleOwner) {
            if(it?.equals(null) == false){
                emailPreference?.summary = it.email
            }
        }

        //Handle change password
        val passwordPreference: Preference? = findPreference("password")
        passwordPreference?.setOnPreferenceClickListener {
            val action = SettingsDirections.actionSettingsToChangePassword()
            view.findNavController().navigate(action)
            true
        }
    }
}
