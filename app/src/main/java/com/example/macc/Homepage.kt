package com.example.macc


import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.example.macc.viewmodel.HomepageViewModel
import com.example.macc.model.Travel
import com.example.macc.utility.UIState


private const val TAG = "Homepage Fragment"

class Homepage : Fragment() {

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

        //sharedViewModel = ViewModelProvider(this).get(HomepageViewModel::class.java)
        sharedViewModel.travelArrayList.observe(viewLifecycleOwner) {
            adapter.setTravelsList(it)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun deleteTravel(travel: Travel) {
        sharedViewModel.deleteTravel(travel)
    }

    private fun actionToExpenseList(travelID: String, position: Int){
        //Action from homepage to expense list page
        val action = HomepageDirections.actionHomepageToExpenseList(travelID, position)
        view?.findNavController()?.navigate(action)
    }
}