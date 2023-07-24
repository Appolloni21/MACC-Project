package com.example.macc

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.InsertExpenseBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel

class InsertExpense : Fragment() {

    private var _binding: InsertExpenseBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()
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

        val addExpenseButton = binding.addExpenseButton
        addExpenseButton.setOnClickListener{
            val expenseName: String = binding.expenseName.editText?.text.toString().trim { it <= ' ' }
            val expensePlace: String = binding.place.editText?.text.toString().trim { it <= ' ' }

            when{
                TextUtils.isEmpty(expenseName) -> {
                    Toast.makeText(context, "Please enter expense name", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(expensePlace) -> {
                    Toast.makeText(context, "Please enter expense place", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    sharedViewModel.addExpense(expenseName,expensePlace)
                }
            }
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    //Il viaggio è stato aggiunto correttamente, facciamo ritornare l'utente alla homepage
                    Toast.makeText(context, "Expense added", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in adding expense", Toast.LENGTH_SHORT).show()
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