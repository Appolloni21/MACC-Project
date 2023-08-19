package com.example.macc.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.databinding.ItemTravelBinding
import com.example.macc.model.Travel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "Travel Adapter"

class TravelAdapter(private val onDeleteCallback: (Travel) -> Unit,
                    private val onActionCallback: (String) -> Unit) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    private val travelsList : ArrayList<Travel> = arrayListOf()
    class TravelViewHolder(val binding: ItemTravelBinding) : RecyclerView.ViewHolder(binding.root){
        val travelName : TextView = binding.travelName
        val travelImage : ImageView = binding.travelCover
        val travelMembers : TextView = binding.groupNumber
        val deleteIcon : ImageView = binding.deleteIcon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val binding = ItemTravelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TravelViewHolder(binding)
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
        Glide.with(holder.binding.root)
            .load(item.imgUrl)
            .into(holder.travelImage)

        holder.travelImage.setOnClickListener{
            val travelID = item.travelID.toString()
            onActionCallback(travelID)
        }

        holder.deleteIcon.setOnClickListener{
            onDeleteCallback(item)
        }

        if(Firebase.auth.currentUser?.uid != item.owner){
            holder.deleteIcon.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTravelsList(travels : List<Travel>){
        this.travelsList.clear()
        this.travelsList.addAll(travels)
        notifyDataSetChanged()
    }
}