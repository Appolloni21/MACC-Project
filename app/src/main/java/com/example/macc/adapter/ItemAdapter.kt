package com.example.macc.adapter

import android.content.Context
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

/*
* ATTUALMENTE NON USATA, NON CANCELLARE
*
*
* */
class ItemAdapter (private val context: Context, private val dataset: List<Travel>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(){
    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        //val textView: TextView = view.findViewById(R.id.travel_name)
        //val imageView: ImageView = view.findViewById(R.id.travel_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_travel, parent, false)

        return ItemViewHolder(adapterLayout)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //val item = dataset[position]
        //holder.textView.text =  context.resources.getString("ciao")
        //holder.imageView.setImageResource(item.imageResourceId)
        /*holder.imageView.setOnClickListener{

            //Action from homepage to expense list page
            val action = HomepageDirections.actionHomepageToExpenseList()
            holder.view.findNavController().navigate(action)
        }*/

    }


    override fun getItemCount(): Int {
        return dataset.size
    }

}