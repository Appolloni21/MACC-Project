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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.macc.Compass.MyView
import com.example.macc.databinding.ItemUserBinding
import com.example.macc.model.User
import com.example.macc.utility.OnActivityStateChanged
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "User Adapter"

class UserAdapter(private val onActionCallback: (String) -> Unit, private val onRemoveCallback: (User) -> Unit, private val context: Context) : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    private val usersList : ArrayList<User> = arrayListOf()
    private var travelOwner: String = ""
    lateinit var customView : MyView

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root){
        val userNameSurname : TextView = binding.userNameSurname
        val userAvatar: ImageView = binding.userAvatar
        val customView = binding.drawingViewInAdapter
        val removeUserIcon : ImageView = binding.removeUserIcon
        fun bind(userid : String, useremail: String){
            // TODO: Add bind data
            customView.setData(userid, useremail)
            customView.invalidate()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d(TAG, "user adapter on createviewholder")
        return UserViewHolder(binding)

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Log.d(TAG, "user adapter")
        val item = usersList[position]
        holder.userNameSurname.text = item.name
        val userID = item.userID.toString()
        val useremail = item.email.toString()

        //Loads the image from the url with Glide
        Glide.with(holder.binding.root)
            .load(item.avatar)
            .into(holder.userAvatar)

        holder.userAvatar.setOnClickListener{
            onActionCallback(userID)
        }


        //Remove user icon visibile solo se sei l'owner del travel, non visibile quando l'utente nella lista è uguale all'utente corrente
        if(Firebase.auth.currentUser?.uid != travelOwner){
            holder.removeUserIcon.isVisible = false
            Log.d(TAG,"if: ${item.name}")
        } else if(Firebase.auth.currentUser?.uid.equals(travelOwner) && Firebase.auth.currentUser?.uid.equals(item.userID)){
            holder.removeUserIcon.isVisible = false
            Log.d(TAG,"elsif: ${item.name}")
        } else{
            holder.removeUserIcon.isVisible = true
            Log.d(TAG,"else: ${item.name}")
        }

        holder.removeUserIcon.setOnClickListener {
            onRemoveCallback(item)
        }
        holder.bind(userID, useremail)

        customView = holder.customView
        Log.d("CUSV", customView.toString())
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.registerListener(
            customView,
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL)
    }


    fun registerActivityState()  = object : OnActivityStateChanged {
        override fun onResumed() {
            Log.d(TAG, "onResumed: ")
            //Register the rotation vector sensor to the listener
            /*val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sm.registerListener(
                customView,
                sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL)*/
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

    @SuppressLint("NotifyDataSetChanged")
    fun setTravelOwner(travelOwner: String){
        this.travelOwner = travelOwner
        notifyDataSetChanged()
    }
}