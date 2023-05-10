package com.example.macc.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.R
import com.example.macc.UsersListDirections
import com.example.macc.model.User

private const val TAG = "User Adapter"

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    private val usersList : ArrayList<User> = arrayListOf()

    class UserViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val userNameSurname : TextView = view.findViewById(R.id.user_name_surname)
        val userAvatar: ImageView = view.findViewById(R.id.user_avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = usersList[position]
        holder.userNameSurname.text = item.name

        //Loads the image from the url with Glide
        Glide.with(holder.view)
            .load(item.avatar)
            .into(holder.userAvatar)

        holder.userAvatar.setOnClickListener{
            val action = UsersListDirections.actionUsersListToUserProfile(position)
            holder.view.findNavController().navigate(action)
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