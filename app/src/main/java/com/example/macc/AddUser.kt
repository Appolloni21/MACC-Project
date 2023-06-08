package com.example.macc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.viewmodel.HomepageViewModel
import com.example.macc.utility.UIState
import com.google.android.material.textfield.TextInputLayout

private const val TAG = "Add User Fragment"

class AddUser : Fragment() {

    private var travelID: String = "travelID"
    private val sharedViewModel: HomepageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.add_user, container,
            false)

        travelID = arguments?.getString("travelID")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        val addUserButton: Button = view.findViewById(R.id.add_user_button)
        addUserButton.setOnClickListener {
            val userEmail: String = view.findViewById<TextInputLayout>(R.id.add_user_email)?.editText?.text.toString().trim { it <= ' ' }
            sharedViewModel.addUser(userEmail, travelID)
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    //L'utente è stato aggiunto correttamente, facciamo ritornare l'utente alla schermata precedente
                    Toast.makeText(context,"The user has been added",Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context,"Error, the user has not been added",Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState._103 -> {
                    Toast.makeText(context,"Error, the user is already in this travel",Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState._104 -> {
                    Toast.makeText(context,"Error, user not found",Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

        Log.d(TAG,"Add User Page")
    }
}