package com.example.macc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.macc.HomepageDirections
import com.example.macc.R
import com.example.macc.model.Travel

class TravelAdapter(private val travelList : ArrayList<Travel>) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    class TravelViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val travelName : TextView = view.findViewById(R.id.travel_name)
        val travelImage : ImageView = view.findViewById(R.id.travel_image)
        val travelMembers : TextView = view.findViewById(R.id.group_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_travel, parent, false)
        return TravelViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return travelList.size
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val item = travelList[position]
        holder.travelName.text = item.name
        holder.travelMembers.text = item.members?.size.toString()

        holder.travelImage.setOnClickListener{

            //Action from homepage to expense list page
            val action = HomepageDirections.actionHomepageToExpenseList()
            holder.view.findNavController().navigate(action)
        }
    }
}