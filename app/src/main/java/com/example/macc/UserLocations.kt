package com.example.macc

import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.macc.Compass.MyView


class UserLocations : Fragment() {

    //lateinit var view : MyView
     lateinit var view2: View
     lateinit var customView : MyView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //view = MyView(context)
        //return view

        view2 = inflater.inflate(R.layout.user_locations, container, false)

        return view2
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = MyView(context)
        //setContentView(view)
    }*/
    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customView = view2.findViewById<MyView>(R.id.drawingView)
        if (customView == null) { Log.d("FAIL", "fail"); }
        activity?.setContentView(R.id.drawingView)

    }


    override fun onResume() {
        super.onResume()
        //Register the rotation vector sensor to the listener
        val sm = context?.getSystemService(SENSOR_SERVICE) as SensorManager
        sm.registerListener(
            customView,
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun onPause() {
        super.onPause()
        val sm = context?.getSystemService(SENSOR_SERVICE) as SensorManager
        //Unregister as the app is pausing, so no compass is displayed
        sm.unregisterListener(customView,
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))
    }
}