package com.example.macc

import android.content.Intent
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
import com.google.android.material.button.MaterialButton

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

        val addExpenseButton = binding.addExpenseButton
        addExpenseButton.setOnClickListener{
            val expenseName: String = binding.expenseName.text.toString().trim { it <= ' ' }
            val expensePlace: String = binding.expensePlace.text.toString().trim { it <= ' ' }

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

        val takeAPhotoBtn = binding.buttonTakePhoto
        val activityContext = requireActivity()
        takeAPhotoBtn.setOnClickListener {
            //val intent = Intent(activityContext,TextRecognition::class.java)
            //startActivity(intent)
            val action = InsertExpenseDirections.actionInsertExpenseToTextRecognition()
            view.findNavController().navigate(action)
        }
        val priceField = binding.priceText
        val loadThePhotoBtn = binding.loadPhotoBtn
            loadThePhotoBtn.setOnClickListener {
                var Item = "5"
                priceField.setText(Item)
                viewModel.selected.observe(viewLifecycleOwner) { item ->
                    if(item.isNotEmpty()){
                        priceField.setText(Item)
                    }
                    else{
                        showToast("COGLIONEEEEEEEEEEEEEEEE")
                    }

                }
            }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showToast(message: String){
        val activityContext = requireActivity()
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
    }

}