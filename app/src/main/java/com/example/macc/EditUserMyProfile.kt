package com.example.macc

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val TAG: String = "EditUserMyProfile Fragment"

class EditUserMyProfile : Fragment() {

    private val sharedViewModel: AuthViewModel by activityViewModels()
    private var imageAvatarURI: Uri = Uri.EMPTY

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.edit_user_my_profile_page, container,
            false)

        sharedViewModel.userMyProfile.observe(viewLifecycleOwner){
            if(it?.equals(null) == false){
                //Avatar
                val userAvatar = view.findViewById<ImageView>(R.id.edit_avatar)
                Glide.with(view).load(it.avatar).into(userAvatar)

                //Altri campi
                view.findViewById<TextView>(R.id.edit_name).text = it.name
                view.findViewById<TextView>(R.id.edit_surname).text = it.surname
                view.findViewById<TextView>(R.id.edit_nickname).text = it.nickname
                view.findViewById<TextView>(R.id.edit_description).text = it.description
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

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
                imageAvatarURI = uri
                view.findViewById<ImageView>(R.id.edit_avatar)?.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val editAvatarButton = view.findViewById<FloatingActionButton>(R.id.fab_edit_avatar)
        editAvatarButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val saveButton: Button = view.findViewById(R.id.editMyProfile_save_btn)
        saveButton.setOnClickListener {
            val name: String = view.findViewById<TextView>(R.id.edit_name).text.toString().trim { it <= ' ' }
            val surname: String = view.findViewById<TextView>(R.id.edit_surname).text.toString().trim { it <= ' ' }
            val nickname: String = view.findViewById<TextView>(R.id.edit_nickname).text.toString().trim { it <= ' ' }
            val description: String = view.findViewById<TextView>(R.id.edit_description).text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(name) -> {
                    Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(surname) -> {
                    Toast.makeText(context, "Please enter surname", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(nickname) -> {
                    Toast.makeText(context, "Please enter nickname", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(description) -> {
                    Toast.makeText(context, "Please enter description", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    sharedViewModel.editUserMyProfile(name,surname,nickname,description, imageAvatarURI)
                }
            }
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in updating your profile", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

    }
}