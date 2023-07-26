package com.example.macc


import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
/*import android.widget.SearchView*/
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.TravelAdapter
import com.example.macc.databinding.HomepageBinding
import com.example.macc.model.Travel
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel


private const val TAG = "Homepage Fragment"

class Homepage : Fragment(), SearchView.OnQueryTextListener {

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
        //@Suppress("DEPRECATION")
        //setHasOptionsMenu(true)
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

        Log.d(TAG, "Homepage")
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
    }*/

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the options menu from XML
        /*menu.clear()
        inflater.inflate(R.menu.homepage_toolbar, menu)
        val item = menu.findItem(R.id.app_bar_search)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)*/

        //val menuItem : MenuItem = menu.findItem(R.id.app_bar_search)
        //val searchView = menuItem.actionView as? SearchView
        //searchView?.isSubmitButtonEnabled = true
        //searchView?.setOnQueryTextListener(this)

        // Get the SearchView and set the searchable configuration
        /*val searchManager = activity?.getSystemService(SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(text: String): Boolean {
                    Log.d(TAG,"onQueryTextSubmit p0: $text")
                    filterHomepage(text)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Log.d(TAG,"onQueryTextChange p0: $newText")
                    filterHomepage(newText)
                    return true
                }
            })
            //isIconifiedByDefault = false // Do not iconify the widget; expand it by default

        }*/
        /*val searchManager = activity?.getSystemService(SEARCH_SERVICE) as SearchManager
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView?.setOnQueryTextListener(SearchView.OnQueryTextListener(){

        })*/

        /*searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.getFilter().filter(newText)
                return false
            }
        })*/

        Log.d(TAG,"onCreateOptionsMenu")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun deleteTravel(travel: Travel) {
        sharedViewModel.deleteTravel(travel)
    }

    private fun actionToExpenseList(travelID: String){
        //Action from homepage to expense list page
        sharedViewModel.selectTravel(travelID)
        val action = HomepageDirections.actionHomepageToExpenseList()
        view?.findNavController()?.navigate(action)
    }

    private fun filterHomepage(p0: String?){
        sharedViewModel.travelArrayList.observe(viewLifecycleOwner) {
            Log.d(TAG,"filterHomepage")
            val travelsFiltered = it.filter { travel -> travel.name!!.contains(p0!!) }
            adapter.setTravelsList(travelsFiltered)
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        Log.d(TAG,"onQueryTextSubmit p0: $p0")
        filterHomepage(p0)
        return true
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        Log.d(TAG,"onQueryTextChange p0: $p0")
        filterHomepage(p0)
        return true
    }
}