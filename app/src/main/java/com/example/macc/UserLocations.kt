package com.example.macc

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.macc.Compass.MyView


class UserLocations : Fragment() {

    lateinit var view : MyView

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = MyView(context)

        return view
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = MyView(context)
        //setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        //Register the rotation vector sensor to the listener
        val sm = context?.getSystemService(SENSOR_SERVICE) as SensorManager
        sm.registerListener(
            view,
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL)
    }


    override fun onPause() {
        super.onPause()
        val sm = context?.getSystemService(SENSOR_SERVICE) as SensorManager
        //Unregister as the app is pausing, so no compass is displayed
        sm.unregisterListener(view,
            sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))
    }


}