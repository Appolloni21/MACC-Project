package com.example.macc

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.InsertExpenseBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel
import com.example.macc.viewmodel.PriceViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class InsertExpense : Fragment() {

    private var _binding: InsertExpenseBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()
    private val viewModel: PriceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = InsertExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar = binding.toolbar.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)

        val expenseDateShow = binding.expenseDateInput
        val myCalendar = Calendar.getInstance()

        sharedViewModel.travelSelected.observe(viewLifecycleOwner) {
            if(it != null){
                //Date picker
                val dpd = DatePickerDialog(requireContext(), picker(expenseDateShow,myCalendar), myCalendar.get(Calendar.YEAR), myCalendar.get(
                    Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                val startDate = dateToMillis(it.startDate.toString())
                val endDate = dateToMillis(it.endDate.toString())
                dpd.datePicker.minDate = startDate
                dpd.datePicker.maxDate = endDate

                expenseDateShow.setOnClickListener {
                    dpd.show()
                }
            }
        }

        val addExpenseButton = binding.addExpenseBtn
        addExpenseButton.setOnClickListener{
            val expenseName: String = binding.expenseName.editText?.text.toString().trim { it <= ' ' }
            val expenseAmount: String = binding.expenseAmount.editText?.text.toString().trim { it <= ' ' }
            val expenseDate: String = binding.expenseDate.editText?.text.toString().trim{it <= ' ' }
            val expensePlace: String = binding.expensePlace.editText?.text.toString().trim { it <= ' ' }
            val expenseNotes: String = binding.expenseNotes.editText?.text.toString().trim{it <= ' ' }
            val expenseCheck: Boolean = binding.checkboxPersonalExpense.isChecked

            when{
                TextUtils.isEmpty(expenseName) -> {
                    Toast.makeText(context, "Please enter expense name", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(expenseAmount) -> {
                    Toast.makeText(context, "Please enter expense amount", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(expensePlace) -> {
                    Toast.makeText(context, "Please enter expense place", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(expenseDate) -> {
                    Toast.makeText(context, "Please enter expense date", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(expenseNotes) -> {
                    Toast.makeText(context, "Please enter expense notes", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    sharedViewModel.addExpense(expenseName,expenseAmount, expenseDate, expensePlace, expenseNotes, expenseCheck)
                }
            }
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    //La spesa è stata aggiunta correttamente, facciamo ritornare l'utente alla homepage
                    Toast.makeText(context, "Expense added", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    viewModel.selectedItem("")
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in adding expense", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

        val takeAPhotoBtn = binding.takePhotoBtn
        takeAPhotoBtn.setOnClickListener {
            val action = InsertExpenseDirections.actionInsertExpenseToTextRecognition()
            view.findNavController().navigate(action)
        }

        val priceField = binding.expenseAmount
        viewModel.selected.observe(viewLifecycleOwner) { item ->
            if(item.isNotEmpty()){
                priceField.editText?.setText(item)
            }
        }


        /*val loadThePhotoBtn = binding.loadAmountBtn
        loadThePhotoBtn.setOnClickListener {

            viewModel.selected.observe(viewLifecycleOwner) { item ->
                if(item.isNotEmpty()){
                    priceField.editText?.setText(item)
                }
            }
        }*/


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun picker(textInputEditText: TextInputEditText, myCalendar: Calendar):  DatePickerDialog.OnDateSetListener{
        val picker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            textInputEditText.setText(updateFormat(myCalendar))
        }
        return picker
    }

    private fun updateFormat(myCalendar: Calendar): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        return sdf.format(myCalendar.time)
    }

    private fun dateToMillis(myDate: String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        val data: Date? = sdf.parse(myDate)
        return data!!.time
    }


    /*private fun showToast(message: String){
        val activityContext = requireActivity()
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
    }*/

}