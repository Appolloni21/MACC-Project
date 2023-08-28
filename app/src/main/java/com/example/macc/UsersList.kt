package com.example.macc

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import com.example.macc.LocationService.LocationService
import com.example.macc.adapter.UserAdapter
import com.example.macc.databinding.UsersListBinding
import com.example.macc.model.User
import com.example.macc.utility.OnActivityStateChanged
import com.example.macc.utility.UIDialogFragment
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.HomepageViewModel
import android.provider.Settings
import android.location.LocationManager



private const val TAG = "Users List Fragment"

class UsersList : Fragment() {

    private var _binding: UsersListBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()
    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter: UserAdapter

    var onActivityStateChanged: OnActivityStateChanged? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = UsersListBinding.inflate(inflater, container, false)
        val view: View = binding.root

        recyclerView = binding.recyclerViewUser
        adapter = UserAdapter(::actionToUserProfile,::removeUserFromTravel ,requireContext())
        recyclerView.adapter = adapter

        sharedViewModel.users.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                adapter.setUsersList(it)
                onActivityStateChanged  = adapter.registerActivityState()
            }
        }

        sharedViewModel.travelSelected.observe(viewLifecycleOwner){
            if(it != null){
                adapter.setTravelOwner(it.owner.toString())
            }
        }

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
        context?.let { checkAndEnableLocation(it) }

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
                R.id.action_addUser -> {
                    Log.d(TAG, "action add user")
                    val action = UsersListDirections.actionUsersListToAddUser()
                    view.findNavController().navigate(action)
                }
            }
            true
        }

        val startBtn = binding.startBtn
        val stopBtn = binding.stopBtn
        startBtn.setOnClickListener{
            Log.d(TAG, "In start")
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                context?.startService(this)
            }
        }
        stopBtn.setOnClickListener{

            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                context?.startService(this)
            }
        }

        val userPositions = binding.userpos
        userPositions.setOnClickListener {
            val action = UsersListDirections.actionUsersListToUserLocations()
            view.findNavController().navigate(action)
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context,"The user has been removed", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context,"Error, user not removed", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

    }

    override fun onPause() {
        if(onActivityStateChanged != null){
            Log.d(TAG,"onPause")
            onActivityStateChanged?.onPaused()
        }
        super.onPause()
    }

    override fun onResume() {
        if(onActivityStateChanged != null){
            Log.d(TAG,"onResume")
        onActivityStateChanged?.onResumed()
        }
        super.onResume()
    }

    private fun actionToUserProfile(position: Int){
        val action = UsersListDirections.actionUsersListToUserProfile(position)
        view?.findNavController()?.navigate(action)
    }

    fun requestEnableLocationServices(context: Context) {
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setMessage("Location services are disabled. Do you want to enable them?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                openLocationSettings(context)
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun checkAndEnableLocation(context: Context) {
        if (!isLocationEnabled(context)) {
            requestEnableLocationServices(context)
        }
    }

    private fun removeUserFromTravel(user: User){
        val newFragment = UIDialogFragment("Are you sure to remove '${user.name}' from this travel?")
        newFragment.show(requireActivity().supportFragmentManager, "UIDialog - removeUserFromTravel")
        sharedViewModel.selectUserToRemoveFromTravel(user)
    }
}