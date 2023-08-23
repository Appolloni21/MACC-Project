package com.example.macc



import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.example.macc.adapter.TravelAdapter
import com.example.macc.databinding.HomepageBinding
import com.example.macc.model.Travel
import com.example.macc.utility.UIDialogFragment
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel


private const val TAG = "Homepage Fragment"

class Homepage : Fragment(){

    private var _binding: HomepageBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: TravelAdapter
    private lateinit var recyclerView : RecyclerView
    private val sharedViewModel: HomepageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = HomepageBinding.inflate(inflater, container, false)
        val view: View = binding.root

        recyclerView = binding.recyclerView
        adapter = TravelAdapter(::deleteTravel, ::actionToExpenseList)
        recyclerView.adapter = adapter

        sharedViewModel.travelArrayList.observe(viewLifecycleOwner) {
            adapter.setTravelsList(it)
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
                R.id.addTravel -> {
                    val action = HomepageDirections.actionHomepageToAddTravel()
                    view.findNavController().navigate(action)
                }
            }
            true
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context,"The travel has been deleted", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context,"Error, the travel has not been deleted", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

        //Search View widget
        searchWidget()

        Log.d(TAG, "Homepage")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun deleteTravel(travel: Travel) {
        val newFragment = UIDialogFragment("Are you sure to delete travel '${travel.name}'?")
        newFragment.show(requireActivity().supportFragmentManager, "UIDialog - deleteTravel")
        //sharedViewModel.deleteTravel(travel)
        sharedViewModel.selectTravelToDelete(travel)
    }

    private fun actionToExpenseList(travelID: String){
        //Action from homepage to expense list page
        sharedViewModel.selectTravel(travelID)
        val action = HomepageDirections.actionHomepageToExpenseList()
        view?.findNavController()?.navigate(action)
    }

    private fun filter(text: String?){
        sharedViewModel.travelArrayList.observe(viewLifecycleOwner) {
            Log.d(TAG,"filter")
            val travelsFiltered = it.filter { travel -> travel.name!!.startsWith(text.toString()) }
            adapter.setTravelsList(travelsFiltered)
        }
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
                val searchManager = activity?.getSystemService(SEARCH_SERVICE) as SearchManager
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
                    /*R.id.menu_clear -> {
                        // clearCompletedTasks()
                        true
                    }
                    R.id.menu_refresh -> {
                        // loadTasks(true)
                        true
                    }*/
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}