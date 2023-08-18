package com.example.macc.Compass
//import android.os.Handler
import android.content.Context
import android.graphics.Bitmap
//import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.location.Location
import android.util.AttributeSet
import android.util.Log
//import android.view.Display
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import com.example.macc.R
//import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.PI
import kotlin.math.atan2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

data class ResponseData(val altitude: Double, val email: String, val latitude: Double, val longitude: Double, val value: Double) {}



const val TAG = "MYDEBUG"
const val TAG2 = "POST"
class MyView(context: Context? , attrs: AttributeSet) : View(context, attrs), SensorEventListener2 {

    var size = 2f  //Absolute size of the compass in inches
    val a = 0.5f //Low-pass filter parameter, higher is smoother

    var mLastRotationVector = FloatArray(3) //The last value of the rotation vector
    var mRotationMatrix = FloatArray(9)
    var yaw = 0f
    var compass : Bitmap
    //var webApi : PostOrientation
    var myLocation : Location
    var targetLocation : Location
    var rotationangle = 0f
    var respData = ResponseData(0.0,"",0.0,0.0,0.0)
    val delayMillis = 10000L
    //val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.baseline_arrow_upward_24)






    init {
        // GET TARGET LOCATION FROM THE SERVER
        Log.i("VIEWWW", "provalog")

        targetLocation = Location("target-location") // provider name is unnecessary

        myLocation = Location("my-location") // provider name is unnecessary
        myLocation.latitude = 40.51490 // your coordinates here
        myLocation.longitude = 15.49879
        myLocation.altitude = 495.1

        //val distanceInMeters = myLocation.distanceTo(targetLocation)

        size*=160*resources.displayMetrics.density
        val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Log.i(TAG,""+resources.displayMetrics.density)
        //Read .svg compass
        compass = ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_upward_24,
            null)?.
        toBitmap(size.toInt(),size.toInt())!!
        //get the arrow instead of compass
        //webApi = WebApi().retrofit.create(PostOrientation::class.java)

        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                getPositionFromServer("prova_token", "test_gmail_com")
                //UPDATE THE TARGET LOCATION EVERY 10 SECONDS
                targetLocation.latitude = respData.latitude // your coordinates here
                targetLocation.longitude = respData.longitude
                delay(delayMillis)
                print("Position received from server")
            }
        }

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.i(TAG,"drawing"+System.currentTimeMillis())
        /*with(canvas) {
            drawColor(Color.YELLOW)
            withRotation (-rotationangle,width/2f,height/2f) {
                drawBitmap(compass, (width - size) / 2f, (height - size) / 2f, null)
            }
        }*/

        with(canvas) {
            drawColor(Color.YELLOW)
            val originalImageSize = size // assuming size is the size of the bitmap
            val numberOfBitmaps = 8
            val scaleFactor = 0.5f // Adjust this scaling factor as needed
            val imageSize = originalImageSize * scaleFactor
            val bitmapsPerRow = 2 // Number of bitmaps per row
            val numberOfRows = ceil(numberOfBitmaps.toFloat() / bitmapsPerRow).toInt()
            val horizontalSpacing = width / (bitmapsPerRow + 1)

            for (row in 0 until numberOfRows) {
                for (col in 0 until bitmapsPerRow) {
                    val totalIndex = row * bitmapsPerRow + col
                    if (totalIndex >= numberOfBitmaps) {
                        break
                    }

                    val x = horizontalSpacing * (col + 1) - imageSize / 2
                    val y = row * imageSize
                    val centerX = x + (imageSize / 2)
                    val centerY = y + (imageSize / 2)
                    withRotation(-rotationangle, centerX.toFloat(), centerY.toFloat()) {
                        drawBitmap(compass, x.toFloat(), y.toFloat(), null)
                    }
                }
            }
        }



    }


    //Implementation of Event Listener Interface
    override fun onSensorChanged(p0: SensorEvent?) {

        mLastRotationVector = p0?.values?.clone()!! //Get last rotation vector

        Log.i(TAG,""+mLastRotationVector[0]+""+mLastRotationVector[1]+" "+mLastRotationVector[2])

        //Compute the rotation matrix from the rotation vector
        SensorManager.getRotationMatrixFromVector(mRotationMatrix,mLastRotationVector)

        //Calculate the yaw angle, see slides of the lesson——
        yaw = a*yaw+(1-a)* atan2(mRotationMatrix[1],mRotationMatrix[4]) *180f/ PI.toFloat()
        //Log.i(TAG2, "YAW angle" + yaw)
        var bearing = myLocation.bearingTo(targetLocation)
        //Log.i(TAG2, "bearing to" + bearing)
        rotationangle= yaw

        val geoField = GeomagneticField(
            myLocation.latitude.toFloat(),
            myLocation.longitude.toFloat(),
            myLocation.altitude.toFloat(),
            System.currentTimeMillis()
        )

        rotationangle += geoField.declination;
        rotationangle = (bearing - rotationangle) * -1;
        //Math.round(-heading / 360 + 180)

        invalidate()

        GlobalScope.launch(IO) {
            //Make a post
            //val a = async {  webApi.doPost(""+System.currentTimeMillis(),yaw) }
            //Log.i(TAG2,"REPLY FROM SERVER: "+a.await().body())
        }




    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
    }

    override fun onFlushCompleted(p0: Sensor?) {
        //      TODO("Not yet implemented")
    }

    private fun getPositionFromServer(token: String, user_email: String) {
        /*GlobalScope.launch {
            val url = "https://androidproject.pythonanywhere.com/get_position"
            val json = """
                {
                    "token": "$token",
                    "user_email": "$user_email"
                }
            """.trimIndent()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string() // response
            if (responseBody != null) {
                Log.d("NET", responseBody)
            }

            val gson = Gson()

            try {
                //write on a global variable, because Coroutine does not allow to return value
                respData = gson.fromJson(responseBody, ResponseData::class.java)
                println("Response email: ${respData.email}")
            } catch (e: Exception) {
                e.printStackTrace()}


            //println("Response Body: $responseBody")
        }*/
    }


}