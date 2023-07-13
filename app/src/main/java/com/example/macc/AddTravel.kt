package com.example.macc



import android.app.DatePickerDialog
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.viewmodel.HomepageViewModel
import com.example.macc.utility.UIState
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private const val TAG: String = "AddTravel Fragment"

class AddTravel : Fragment() {

    private var imageCoverURI: Uri = Uri.EMPTY
    private val sharedViewModel: HomepageViewModel by activityViewModels()

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

        val startDate = view.findViewById<EditText>(R.id.startDate)
        val endDate = view.findViewById<EditText>(R.id.endDate)
        val myCalendar = Calendar.getInstance()


        //Date picker per data di inizio e fine viaggio
        startDate.setOnClickListener {
            DatePickerDialog(requireContext(), picker(startDate,myCalendar), myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        endDate.setOnClickListener{
            DatePickerDialog(requireContext(), picker(endDate,myCalendar), myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }


        val addTravelButton = view.findViewById<Button>(R.id.addTravelButton)
        addTravelButton.setOnClickListener{
            val travelName: String = view.findViewById<TextInputLayout>(R.id.travelName)?.editText?.text.toString().trim { it <= ' ' }
            val destination: String = view.findViewById<TextInputLayout>(R.id.destination)?.editText?.text.toString().trim { it <= ' ' }
            val startDateT: String = startDate?.text.toString().trim { it <= ' ' }
            val endDateT: String = endDate?.text.toString().trim { it <= ' ' }

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

                TextUtils.isEmpty(startDateT) -> {
                    makeToast("Please enter start date")
                }

                TextUtils.isEmpty(endDateT) -> {
                    makeToast("Please enter end date")
                }

                else -> {
                    sharedViewModel.addTravel(travelName,destination,startDateT,endDateT,imageCoverURI)
                }
            }
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                //Il viaggio è stato aggiunto correttamente, facciamo ritornare l'utente alla homepage
                    makeToast("The travel has been added")
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    makeToast("Error, the travel has not been added")
                    sharedViewModel.resetUiState()
                }
            }
        }
    }


    private fun picker(editText: EditText, myCalendar: Calendar):  DatePickerDialog.OnDateSetListener{
        val picker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editText.setText(updateFormat(myCalendar))
        }
        return picker
    }

    private fun updateFormat(myCalendar: Calendar): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        return sdf.format(myCalendar.time)
    }

    private fun makeToast(msg:String){
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}