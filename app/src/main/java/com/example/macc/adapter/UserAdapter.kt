package com.example.macc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.Compass.MyView
import com.example.macc.databinding.ItemUserBinding
import com.example.macc.model.User
import com.example.macc.utility.OnActivityStateChanged

private const val TAG = "User Adapter"

class UserAdapter(private val onActionCallback: (Int) -> Unit, private val context: Context) : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    private val usersList : ArrayList<User> = arrayListOf()
    lateinit var customView : MyView

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root){
        val userNameSurname : TextView = binding.userNameSurname
        val userAvatar: ImageView = binding.userAvatar
        val customView = binding.drawingViewInAdapter
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

        customView = holder.customView
    }

    fun registerActivityState()  = object : OnActivityStateChanged {
        override fun onResumed() {
            Log.d(TAG, "onResumed: ")
            //Register the rotation vector sensor to the listener
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sm.registerListener(
                customView,
                sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL)
        }

        override fun onPaused() {
            Log.d(TAG, "onPaused: ")
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            //Unregister as the app is pausing, so no compass is displayed
            sm.unregisterListener(customView,
                sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))
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