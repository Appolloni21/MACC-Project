package com.example.macc.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.HomepageDirections
import com.example.macc.R
import com.example.macc.model.Travel

private const val TAG = "Travel Adapter"

class TravelAdapter(private val onDeleteCallback: (Travel) -> Unit) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    private val travelsList : ArrayList<Travel> = arrayListOf()

    class TravelViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val travelName : TextView = view.findViewById(R.id.travel_name)
        val travelImage : ImageView = view.findViewById(R.id.travelCover)
        val travelMembers : TextView = view.findViewById(R.id.group_number)
        val deleteIcon : ImageView = view.findViewById(R.id.delete_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_travel, parent, false)
        return TravelViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return travelsList.size
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val item = travelsList[position]
        Log.d(TAG, "item")

        holder.travelName.text = item.name
        holder.travelMembers.text = item.members?.size.toString()

        //Loads the image from the url with Glide
        Glide.with(holder.view)
            .load(item.imgUrl)
            .into(holder.travelImage)

        holder.travelImage.setOnClickListener{
            val travelID = item.travelID.toString()

            //Action from homepage to expense list page
            val action = HomepageDirections.actionHomepageToExpenseList(travelID, position)
            holder.view.findNavController().navigate(action)
        }

        holder.deleteIcon.setOnClickListener{
            onDeleteCallback(item)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTravelsList(travels : List<Travel>){
        this.travelsList.clear()
        this.travelsList.addAll(travels)
        notifyDataSetChanged()
    }
}