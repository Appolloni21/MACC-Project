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
import com.example.macc.databinding.EditTravelPageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel

private const val TAG = "Edit Travel Fragment"

class EditTravel : Fragment() {
    private var _binding: EditTravelPageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()
    private var imageCoverURI: Uri = Uri.EMPTY

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = EditTravelPageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        sharedViewModel.travelSelected.observe(viewLifecycleOwner){
            if(it != null){
                binding.editTravelName.setText(it.name)
                binding.editDestination.setText(it.destination)

                //Carichiamo l'immagine
                Glide.with(view).load(it.imgUrl).into(binding.editTravelCover)
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
                imageCoverURI = uri
                binding.editTravelCover.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val editAvatarButton = binding.fabEditTravelCover
        editAvatarButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val saveButton: Button = binding.editTravelSaveBtn
        saveButton.setOnClickListener {
            val travelName: String = binding.editTravelName.text.toString().trim { it <= ' ' }
            val travelDestination: String = binding.editDestination.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(travelName) -> {
                    Toast.makeText(context, "Please enter travel name", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(travelDestination) -> {
                    Toast.makeText(context, "Please enter destination", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    sharedViewModel.editTravel(travelName,travelDestination,imageCoverURI)
                }
            }

        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context, "Travel updated!", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in updating the travel", Toast.LENGTH_SHORT).show()
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