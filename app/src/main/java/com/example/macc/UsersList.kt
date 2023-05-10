package com.example.macc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.ExpenseAdapter
import com.example.macc.adapter.UserAdapter
import com.example.macc.data.HomepageViewModel

class UsersList : Fragment() {

    private var travelID: String = "travelID"
    private val sharedViewModel: HomepageViewModel by viewModels()
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


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)

        //val userProfileImage = view.findViewById<ImageView>(R.id.user_avatar)
        /*userProfileImage.setOnClickListener{
            val action = UsersListDirections.actionUsersListToUserProfile()
            view.findNavController().navigate(action)
        }*/
    }
}