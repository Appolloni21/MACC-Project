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
import com.example.macc.databinding.UsersListBinding
import com.example.macc.viewmodel.HomepageViewModel

private const val TAG = "Users List Fragment"

class UsersList : Fragment() {

    private var _binding: UsersListBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: HomepageViewModel by activityViewModels()
    private lateinit var recyclerView : RecyclerView
    lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = UsersListBinding.inflate(inflater, container, false)
        val view: View = binding.root

        recyclerView = binding.recyclerViewUser
        adapter = UserAdapter(::actionToUserProfile)
        recyclerView.adapter = adapter

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
    }

    private fun actionToUserProfile(position: Int){
        val action = UsersListDirections.actionUsersListToUserProfile(position)
        view?.findNavController()?.navigate(action)
    }
}