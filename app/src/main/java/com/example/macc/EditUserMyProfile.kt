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
import com.example.macc.databinding.EditUserMyProfilePageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel

private const val TAG: String = "EditUserMyProfile Fragment"

class EditUserMyProfile : Fragment() {

    private var _binding: EditUserMyProfilePageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: AuthViewModel by activityViewModels()
    private var imageAvatarURI: Uri = Uri.EMPTY

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = EditUserMyProfilePageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        sharedViewModel.userMyProfile.observe(viewLifecycleOwner){
            if(it?.equals(null) == false){
                //Avatar
                val userAvatar = binding.editAvatar
                Glide.with(view).load(it.avatar).into(userAvatar)

                //Altri campi
                binding.editName.setText(it.name)
                binding.editSurname.setText(it.surname)
                binding.editNickname.setText(it.nickname)
                binding.editDescription.setText(it.description)
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

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
                imageAvatarURI = uri
                binding.editAvatar.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val editAvatarButton = binding.fabEditAvatar
        editAvatarButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val saveButton: Button = binding.editMyProfileSaveBtn
        saveButton.setOnClickListener {
            val name: String = binding.editName.text.toString().trim { it <= ' ' }
            val surname: String = binding.editSurname.text.toString().trim { it <= ' ' }
            val nickname: String = binding.editNickname.text.toString().trim { it <= ' ' }
            val description: String = binding.editDescription.text.toString().trim { it <= ' ' }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}