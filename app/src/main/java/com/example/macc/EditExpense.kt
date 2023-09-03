package com.example.macc

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.EditExpensePageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "Edit Expense Fragment"

class EditExpense : Fragment() {

    private var _binding: EditExpensePageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = EditExpensePageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        sharedViewModel.expenseSelected.observe(viewLifecycleOwner) { expenseSelected ->
           if(expenseSelected != null){
               binding.editExpenseName.setText(expenseSelected.name)
               binding.editExpenseAmount.setText(expenseSelected.amount)
               binding.editExpenseDate.setText(expenseSelected.date)
               binding.editExpensePlace.setText(expenseSelected.place)
               binding.editExpenseNotes.setText(expenseSelected.notes)
               Log.d(TAG,"$expenseSelected")
           }
        }
        sharedViewModel.travelSelected.observe(viewLifecycleOwner){
            sharedViewModel.checkCurrentUserInTravel()
            sharedViewModel.expenseSelected.observe(viewLifecycleOwner) {expenseSelected ->
                sharedViewModel.checkExpenseInTravel(expenseSelected.expenseID.toString())
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbar.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)

        val expenseDateShow = binding.editExpenseDate
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

        val saveButton = binding.editExpenseSaveBtn
        saveButton.setOnClickListener {
            val expenseName: String = binding.editExpenseName.text.toString().trim { it <= ' ' }
            val expenseAmount: String = binding.editExpenseAmount.text.toString().trim { it <= ' ' }
            val expenseDate: String = binding.editExpenseDate.text.toString().trim { it <= ' ' }
            val expensePlace: String = binding.editExpensePlace.text.toString().trim { it <= ' ' }
            val expenseNotes: String = binding.editExpenseNotes.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(expenseName) -> {
                    Toast.makeText(context, "Please enter expense name", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(expenseAmount) -> {
                    Toast.makeText(context, "Please enter amount", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(expenseDate) -> {
                    Toast.makeText(context, "Please enter date", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(expensePlace) -> {
                    Toast.makeText(context, "Please enter place", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(expenseNotes) -> {
                    Toast.makeText(context, "Please enter notes", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    sharedViewModel.editExpense(expenseName, expenseAmount, expenseDate, expensePlace, expenseNotes)
                }
            }

        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context, "Expense updated!", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in updating the expense", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.WARN_104 -> {
                    Toast.makeText(context,"You are not anymore in the travel", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigate(R.id.homepage)
                }
                UIState.WARN_105 -> {
                    Toast.makeText(context,"The expense not anymore in the travel", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
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

    private fun dateToMillis(myDate: String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        val data: Date? = sdf.parse(myDate)
        return data!!.time
    }
}