package com.example.macc

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.adapter.ExpenseAdapter
import com.example.macc.databinding.ExpenseListPageBinding
import com.example.macc.model.Expense
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
        adapter = ExpenseAdapter(::deleteExpense, ::editExpense)
        recyclerView.adapter = adapter

        sharedViewModel.travelSelected.observe(viewLifecycleOwner){
            if(it != null){
                binding.travelNameLabel.text = it.name

                //Carichiamo l'immagine
                Glide.with(view).load(it.imgUrl).into(binding.travelCoverImg)

                //Per passare il numero di utenti nel viaggio all'adapter
                adapter.setTravelMembers(it.members?.size!!)
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

        //For the Search Widget
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

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

        //Search View widget
        searchWidget()

        Log.d(TAG,"Expense list")
    }

    private fun deleteExpense(expense: Expense) {
        sharedViewModel.deleteExpense(expense)
    }

    private fun editExpense(expenseID: String){
        sharedViewModel.selectExpense(expenseID)
        val action = ExpenseListDirections.actionExpenseListToEditExpense()
        view?.findNavController()?.navigate(action)
    }


    private fun filter(text: String?){
        sharedViewModel.expenses.observe(viewLifecycleOwner) {
            Log.d(TAG,"filter")
            val expensesFiltered = it.filter { expense -> expense.name!!.startsWith(text.toString()) }
            adapter.setExpensesList(expensesFiltered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun searchWidget(){
        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.homepage_toolbar, menu)
                // Get the SearchView and set the searchable configuration
                val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
                    // Assumes current activity is the searchable activity
                    setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                    setIconifiedByDefault(true) // Do not iconify the widget; expand it by default
                    isSubmitButtonEnabled = true
                    queryHint = "Search travel..."
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(text: String): Boolean {
                            Log.d(TAG,"onQueryTextSubmit p0: $text")
                            filter(text)
                            return true
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            Log.d(TAG,"onQueryTextChange p0: $newText")
                            filter(newText)
                            return true
                        }
                    })
                }

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    /*R.id.app_bar_search -> {
                        // tasks()
                        true
                    } */
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}