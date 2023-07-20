package com.example.macc.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.databinding.ItemUserBinding
import com.example.macc.model.User

private const val TAG = "User Adapter"

class UserAdapter(private val onActionCallback: (Int) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    private val usersList : ArrayList<User> = arrayListOf()

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root){
        val userNameSurname : TextView = binding.userNameSurname
        val userAvatar: ImageView = binding.userAvatar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Log.d(TAG, "user adapter")
        val item = usersList[position]
        holder.userNameSurname.text = item.name

        //Loads the image from the url with Glide
        Glide.with(holder.binding.root)
            .load(item.avatar)
            .into(holder.userAvatar)

        holder.userAvatar.setOnClickListener{
            onActionCallback(position)
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUsersList(users : List<User>){
        this.usersList.clear()
        this.usersList.addAll(users)
        notifyDataSetChanged()
    }
}