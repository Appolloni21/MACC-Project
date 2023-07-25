package com.example.macc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.viewmodel.PriceViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

public class InsertExpense : Fragment() {

     private val viewModel: PriceViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.insert_expense, container,
            false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)

        val addExpenseButton = view.findViewById<Button>(R.id.addExpenseButton)
        addExpenseButton.setOnClickListener{
            navController.navigateUp()
        }

        /*val takeAPhotoBtn = view.findViewById<MaterialButton>(R.id.button_take_photo)
        val activityContext = requireActivity()
        takeAPhotoBtn.setOnClickListener {
            val intent = Intent(activityContext,TextRecognition::class.java)
            startActivity(intent)
        }
        val priceField = view.findViewById<EditText>(R.id.priceText)
        val loadThePhotoBtn = view.findViewById<MaterialButton>(R.id.loadPhotoBtn)
            loadThePhotoBtn.setOnClickListener {
                var Item = "5"
                priceField.setText(Item)
                viewModel.selected.observe(activityContext, Observer<String> { item ->
                    var Item = "5"
                    if(item.isNotEmpty()){
                        priceField.setText(Item)
                    }
                    else{
                        showToast("COGLIONEEEEEEEEEEEEEEEE")
                    }

                })
            }

*/
    }


    private fun showToast(message: String){
        val activityContext = requireActivity()
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
    }

}