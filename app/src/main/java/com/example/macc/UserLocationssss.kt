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
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.Compass.MyView
import com.example.macc.databinding.UserLocationsBinding


class UserLocationssss : Fragment() {


    lateinit var customView : MyView

    private var _binding: UserLocationsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserLocationsBinding.inflate(inflater, container, false)
        val view: View = binding.root

        customView = binding.drawingView

        return view
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = MyView(context)
        //setContentView(view)
    }*/
    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (customView == null) { Log.d("FAIL", "fail"); }
        //activity?.setContentView(R.id.drawingView)

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbarUserLocation.toolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)
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