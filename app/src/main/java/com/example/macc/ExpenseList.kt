package com.example.macc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.macc.viewmodel.HomepageViewModel


private const val TAG = "Expense List Fragment"

class ExpenseList : Fragment() {

    private var _binding: ExpenseListPageBinding? = null
    private val binding get() = _binding!!
    private var travelID: String = "travelID"
    private var travelPosition: Int = 0
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
        adapter = ExpenseAdapter()
        recyclerView.adapter = adapter

        //Serve per sapere la posizione del Travel nella lista della homepage.
        travelID = arguments?.getString("travelID")!!
        travelPosition = arguments?.getInt("travelPosition")!!

        //Prendiamo la lista dei viaggi che è contenuta nel ViewModel
        sharedViewModel.travelArrayList.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()){
                //Ricaviamo il viaggio e applichiamo alla pagina il nome corretto del viaggio
                val travel = it[travelPosition]
                binding.travelNameLabel.text = travel.name

                //Carichiamo l'immagine
                Glide.with(view).load(travel.imgUrl).into(binding.travelCoverImg)
            }
        }
        sharedViewModel.getExpenses(travelID)
        sharedViewModel.expenses.observe(viewLifecycleOwner){ expenseList ->
            if(expenseList != null){
                //passare all'adapter la lista
                adapter.setExpensesList(expenseList)
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

        val usersListButton = binding.extendedFab
        usersListButton.setOnClickListener{
            val action = ExpenseListDirections.actionExpenseListToUsersList(travelID)
            view.findNavController().navigate(action)
        }

        val addExpenseButton = binding.fab
        addExpenseButton.setOnClickListener{
            //Action from ExpenseList to InsertExpense
            val action = ExpenseListDirections.actionExpenseListToInsertExpense()
            view.findNavController().navigate(action)
        }

        //TODO: rendere l'icona dei tre puntini dentro la expense cliccabile e collegarla la funzione di edit della expense
        Log.d(TAG,"Expense list")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}