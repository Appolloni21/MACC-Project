package com.example.macc



import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.AddTravelPageBinding
import com.example.macc.viewmodel.HomepageViewModel
import com.example.macc.utility.UIState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private const val TAG: String = "AddTravel Fragment"

class AddTravel : Fragment() {

    private var _binding: AddTravelPageBinding? = null
    private val binding get() = _binding!!
    private var imageCoverURI: Uri = Uri.EMPTY
    private val sharedViewModel: HomepageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = AddTravelPageBinding.inflate(inflater, container, false)
        return binding.root
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
                binding.travelCover.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val chooseTravelCoverButton = binding.chooseTravelCoverButton
        chooseTravelCoverButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val startDate = binding.startDate
        val endDate = binding.endDate
        val myCalendar = Calendar.getInstance()


        //Date picker per data di inizio e fine viaggio
        startDate.setOnClickListener {
            DatePickerDialog(requireContext(), picker(startDate,myCalendar), myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        endDate.setOnClickListener{
            DatePickerDialog(requireContext(), picker(endDate,myCalendar), myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }


        val addTravelButton = binding.addTravelButton
        addTravelButton.setOnClickListener{
            val travelName: String = binding.travelName.editText?.text.toString().trim { it <= ' ' }
            val destination: String = binding.destination.editText?.text.toString().trim { it <= ' ' }
            val startDateT: String = startDate.text.toString().trim { it <= ' ' }
            val endDateT: String = endDate.text.toString().trim { it <= ' ' }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun picker(editText: EditText, myCalendar: Calendar):  DatePickerDialog.OnDateSetListener{
        val picker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
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