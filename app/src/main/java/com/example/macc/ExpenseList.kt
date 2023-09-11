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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.ParseException
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
import com.example.macc.utility.UIDialogFragment
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


private const val TAG = "Expense List Fragment"

class ExpenseList : Fragment(), AdapterView.OnItemSelectedListener {

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
        val toolbar: Toolbar = binding.toolbarExpListPage.toolbar

        sharedViewModel.travelSelected.observe(viewLifecycleOwner){
            if(it != null){
                binding.travelNameLabel.text = it.name

                //Carichiamo l'immagine
                Glide.with(view).load(it.imgUrl).into(binding.travelCoverImg)

                //Per passare il numero di utenti nel viaggio all'adapter
                adapter.setTravelMembers(it.members?.size!!)

                toolbar.setOnMenuItemClickListener {menu ->
                    when (menu.itemId) {
                        R.id.action_edit -> {
                            if(Firebase.auth.currentUser?.uid != it.owner){
                                Toast.makeText(context, "You can't perform edit, you are not the owner of this travel", Toast.LENGTH_LONG).show()
                            }else{
                                val action = ExpenseListDirections.actionExpenseListToEditTravel()
                                view.findNavController().navigate(action)
                            }
                        }
                        R.id.action_exit ->{
                            if(Firebase.auth.currentUser?.uid.equals(it.owner)){
                                Toast.makeText(context, "You can't quit this travel, you are the owner!", Toast.LENGTH_LONG).show()
                            }else{
                                val newFragment = UIDialogFragment("Are you sure to quit the Travel '${it.name}'?")
                                newFragment.show(requireActivity().supportFragmentManager, "UIDialog - quitTravel")
                            }
                        }
                    }
                    true
                }

                //Spinner for selecting days
                val daySpinner = binding.daySpinner
                val travelDays = getDatesBetween(it.startDate.toString(),it.endDate.toString())
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_item, travelDays)
                    .also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        daySpinner.adapter = adapter
                    }
                daySpinner.onItemSelectedListener = this

            }
        }

        /*sharedViewModel.expenses.observe(viewLifecycleOwner){ expenses ->
            if(expenses != null){
                //passare all'adapter la lista
                adapter.setExpensesList(expenses)
            }
        }*/

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        //For the Search Widget
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarExpListPage.toolbar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbarExpListPage.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)

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
                UIState.SUCC_101 -> {
                    Toast.makeText(context,"You successfully quit the travel", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAIL_105 -> {
                    Toast.makeText(context,"Error in quitting the travel", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.WARN_104 -> {
                    Toast.makeText(context,"You are not anymore in the travel", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
            }
        }

        //Search View widget
        searchWidget()

        Log.d(TAG,"Expense list")
    }

    private fun deleteExpense(expense: Expense) {
        val newFragment = UIDialogFragment("Are you sure to delete the expense '${expense.name}'?")
        newFragment.show(requireActivity().supportFragmentManager, "UIDialog - deleteExpense")
        sharedViewModel.selectExpenseToDelete(expense)
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

    private fun getDatesBetween(startDate: String, endDate: String): List<String>{
        val dates = ArrayList<String>()
        val input = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var date1: Date? = null
        var date2: Date? = null
        try
        {
            date1 = input.parse(startDate)
            date2 = input.parse(endDate)
        }
        catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        while (!cal1.after(cal2))
        {
            val output = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dates.add(output.format(cal1.time))
            cal1.add(Calendar.DATE, 1)
        }
        return dates
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
                menuInflater.inflate(R.menu.expense_list_toolbar, menu)

                // Get the SearchView and set the searchable configuration
                val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
                (menu.findItem(R.id.app_bar_search_expense).actionView as SearchView).apply {
                    // Assumes current activity is the searchable activity
                    setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
                    setIconifiedByDefault(true) // Do not iconify the widget; expand it by default
                    isSubmitButtonEnabled = true
                    queryHint = "Search expense..."
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

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        val date = parent.getItemAtPosition(pos).toString()
        sharedViewModel.expenses.observe(viewLifecycleOwner){ expenses ->
            if(expenses != null){
                val expensesFiltered = expenses.filter { expense -> expense.date.equals(date) }
                adapter.setExpensesList(expensesFiltered)
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

}