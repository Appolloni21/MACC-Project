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
import com.example.macc.adapter.UserAdapter
import com.example.macc.data.HomepageViewModel

private const val TAG = "Users List Fragment"

class UsersList : Fragment() {

    private var travelID: String = "travelID"
    private val sharedViewModel: HomepageViewModel by activityViewModels()
    private lateinit var recyclerView : RecyclerView
    lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.users_list, container,
            false)

        recyclerView = view.findViewById(R.id.recycler_view_user)
        adapter = UserAdapter()
        recyclerView.adapter = adapter

        travelID = arguments?.getString("travelID")!!
        sharedViewModel.getUsers(travelID)
        sharedViewModel.users.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                adapter.setUsersList(it)
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
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_addUser -> {
                    Log.d(TAG, "log")
                    val action = UsersListDirections.actionUsersListToAddUser(travelID)
                    view.findNavController().navigate(action)
                }
            }
            true
        }
    }
}