package com.example.macc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.adapter.ExpenseAdapter
import com.example.macc.databinding.ExpenseListPageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel


private const val TAG = "Expense List Fragment"

class ExpenseList : Fragment() {

    private var _binding: ExpenseListPageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()
    private lateinit var recyclerView : RecyclerView
    lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ExpenseListPageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        recyclerView = binding.recyclerViewExpense
        adapter = ExpenseAdapter(::deleteExpense)
        recyclerView.adapter = adapter

        sharedViewModel.travelSelected.observe(viewLifecycleOwner){
            if(it != null){
                binding.travelNameLabel.text = it.name

                //Carichiamo l'immagine
                Glide.with(view).load(it.imgUrl).into(binding.travelCoverImg)
            }
        }

        sharedViewModel.expenses.observe(viewLifecycleOwner){ expenses ->
            if(expenses != null){
                //passare all'adapter la lista
                adapter.setExpensesList(expenses)
            }
        }

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbar.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit -> {
                    val action = ExpenseListDirections.actionExpenseListToEditTravel()
                    view.findNavController().navigate(action)
                }
            }
            true
        }

        val usersListButton = binding.extendedFab
        usersListButton.setOnClickListener{
            val action = ExpenseListDirections.actionExpenseListToUsersList()
            view.findNavController().navigate(action)
        }

        val addExpenseButton = binding.fab
        addExpenseButton.setOnClickListener{
            //Action from ExpenseList to InsertExpense
            val action = ExpenseListDirections.actionExpenseListToInsertExpense()
            view.findNavController().navigate(action)
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context,"The expense has been deleted", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context,"Error, the expense has not been deleted", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

        //TODO: rendere l'icona dei tre puntini dentro la expense cliccabile e collegarla la funzione di edit della expense
        Log.d(TAG,"Expense list")
    }

    private fun deleteExpense(expenseID: String, travelID: String) {
        sharedViewModel.deleteExpense(expenseID,travelID)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}