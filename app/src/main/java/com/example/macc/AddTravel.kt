package com.example.macc


import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.data.HomepageViewModel
import com.google.android.material.textfield.TextInputLayout


private const val TAG: String = "AddTravel Fragment"

class AddTravel : Fragment() {

    private var imageCoverURI: Uri = Uri.EMPTY
    private val sharedViewModel: HomepageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.add_travel_page, container,
            false)
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
                imageCoverURI = uri
                view.findViewById<ImageView>(R.id.travel_cover)?.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val chooseTravelCoverButton = view.findViewById<Button>(R.id.chooseTravelCoverButton)
        chooseTravelCoverButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val addTravelButton = view.findViewById<Button>(R.id.addTravelButton)
        addTravelButton.setOnClickListener{
            val travelName: String = view.findViewById<TextInputLayout>(R.id.travelName)?.editText?.text.toString().trim { it <= ' ' }
            val destination: String = view.findViewById<TextInputLayout>(R.id.destination)?.editText?.text.toString().trim { it <= ' ' }
            val startDate: String = view.findViewById<EditText>(R.id.startDate)?.text.toString().trim { it <= ' ' }
            val endDate: String = view.findViewById<EditText>(R.id.endDate)?.text.toString().trim { it <= ' ' }
            //TODO: gestire casistica in cui si inseriscano altri utenti (anche lato frontend)

            when{
                TextUtils.isEmpty(imageCoverURI.toString()) -> {
                    makeToast("Please enter cover image")
                }

                TextUtils.isEmpty(travelName) -> {
                    makeToast("Please enter travel name")
                }

                TextUtils.isEmpty(destination) -> {
                    makeToast("Please enter destination")
                }

                TextUtils.isEmpty(startDate) -> {
                    makeToast("Please enter start date")
                }

                TextUtils.isEmpty(endDate) -> {
                    makeToast("Please enter end date")
                }

                else -> {
                    sharedViewModel.addTravel(travelName,destination,startDate,endDate,imageCoverURI, context)
                }
            }
        }

        sharedViewModel.travelAdded.observe(viewLifecycleOwner){
            if(it != null){

                //Il viaggio è stato aggiunto correttamente, facciamo ritornare l'utente alla homepage
                navController.navigateUp()
            }
        }
    }

    private fun makeToast(msg:String){
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}