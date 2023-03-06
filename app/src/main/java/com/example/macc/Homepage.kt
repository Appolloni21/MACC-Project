package com.example.macc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.ItemAdapter
import com.example.macc.data.Datasource


class Homepage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.homepage, container,
            false)

        /*val activity1 = activity as AppCompatActivity?
        val toolbar : Toolbar = view.findViewById(R.id.toolbar)
        activity1?.setSupportActionBar(toolbar)*/


        //TEST PER LA HOMEPAGE
        // Initialize data.
        val myDataset = Datasource().loadTravels()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val activity = activity as Context
        recyclerView.adapter = ItemAdapter(activity, myDataset)

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
        return view
    }


}