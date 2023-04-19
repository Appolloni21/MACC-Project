package com.example.macc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.adapter.ExpenseAdapter
import com.example.macc.data.HomepageViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val TAG = "Expense List Fragment"

class ExpenseList : Fragment() {

    private var travelPositionId: Int = 0
    private val sharedViewModel: HomepageViewModel by viewModels()
    private lateinit var recyclerView : RecyclerView
    lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.expense_list_page, container,
            false)

        recyclerView = view.findViewById(R.id.recycler_view_expense)
        adapter = ExpenseAdapter()
        recyclerView.adapter = adapter

        //Serve per sapere la posizione del Travel nella lista della homepage.
        travelPositionId = arguments?.getInt("position")!!
        Log.d(TAG, "Position: $travelPositionId")

        //Prendiamo la lista dei viaggi che è contenuta nel ViewModel
        sharedViewModel.travelArrayList.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()){
                //Ricaviamo il viaggio e applichiamo alla pagina il nome corretto del viaggio
                val travel = it[travelPositionId]
                view.findViewById<TextView>(R.id.travelNameLabel).text = travel.name

                //Carichiamo l'immagine
                Glide.with(view).load(travel.imgUrl).into(view.findViewById(R.id.travelCoverImg))

                /*********/
                sharedViewModel.loadTravelExpenses(travelPositionId)
                sharedViewModel.expenses.observe(viewLifecycleOwner){ exp ->
                    if(exp != null){
                        Log.d(TAG,"$exp")
                        //passare all'adapter la lista ed è fatta
                        adapter.setExpensesList(exp)
                    }
                }

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
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)

        val usersListButton = view.findViewById<ExtendedFloatingActionButton>(R.id.extended_fab)
        usersListButton.setOnClickListener{
            val action = ExpenseListDirections.actionExpenseListToUsersList()
            view.findNavController().navigate(action)
        }

        val addExpenseButton = view.findViewById<FloatingActionButton>(R.id.fab)
        addExpenseButton.setOnClickListener{
            //Action from ExpenseList to InsertExpense
            val action = ExpenseListDirections.actionExpenseListToInsertExpense()
            view.findNavController().navigate(action)
        }

    }


}