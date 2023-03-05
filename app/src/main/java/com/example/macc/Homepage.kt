package com.example.macc

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.adapter.ItemAdapter
import com.example.macc.data.Datasource

import com.google.android.material.bottomnavigation.BottomNavigationView


class Homepage : Fragment() {
    companion object {

        fun newInstance(): Homepage {
            return Homepage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.homepage, container,
            false)
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