package com.example.macc.Compass
//import android.os.Handler
//import android.graphics.BitmapFactory
//import android.view.Display
import android.content.Context
import android.graphics.Bitmap
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
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.macc.R
import com.example.macc.viewmodel.HomepageViewModel
import com.example.macc.viewmodel.LocationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.atan2
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings


data class ResponseData(val altitude: Double, val email: String, val latitude: Double, val longitude: Double, val value: Double) {}



const val TAG = "MYDEBUG"
const val TAG2 = "POST"
class MyView(context: Context? , attrs: AttributeSet) : View(context, attrs), SensorEventListener2 {

    var size = 1f  //Absolute size of the compass in inches
    val a = 0.5f //Low-pass filter parameter, higher is smoother

    var mLastRotationVector = FloatArray(3) //The last value of the rotation vector
    var mRotationMatrix = FloatArray(9)
    var yaw = 0f
    lateinit var compass : Bitmap
    //var webApi : PostOrientation
    var myLocation : Location
    var targetLocation : Location
    var rotationangle = 0f
    var ot_respData = ResponseData(0.0,"",0.0,0.0,0.0)
    var cur_respData = ResponseData(0.0,"",0.0,0.0,0.0)
    val delayMillis = 1000L
    val mUser = FirebaseAuth.getInstance().currentUser
    lateinit var uid : String
    lateinit var ot_user_email : String
    //val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.baseline_arrow_upward_24)






    init {
        // GET TARGET LOCATION FROM THE SERVER
        Log.i("VIEWWW", "provalog")

        targetLocation = Location("target-location") // provider name is unnecessary

        myLocation = Location("my-location") // provider name is unnecessary
        myLocation.latitude = 0.0 // your coordinates here
        myLocation.longitude = 0.0
        myLocation.altitude = 0.0

        //val distanceInMeters = myLocation.distanceTo(targetLocation)

        size*=160*resources.displayMetrics.density
        val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Log.i(TAG,""+resources.displayMetrics.density)
        //Read .svg compass

        //get the arrow instead of compass
        //webApi = WebApi().retrofit.create(PostOrientation::class.java)


        this.setWillNotDraw(true)
        //invalidate()

    }

    fun setData (user_id: String, email: String){
        uid = user_id
        ot_user_email = email
        Log.d("SETDATACALLED", uid)
        // ONCE WE GOT THE USERID we start looking for position from server
        if (ot_user_email != mUser?.email.toString())
            this.setWillNotDraw(false)
        getPositionFromServerCoo()


    }


    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun getPositionFromServerCoo () {
        //GET TOKEN FROM USER SESSION
        Log.d("USERLOG", mUser.toString())
        val curUserEmail = mUser?.email.toString()
        val curUserId=mUser?.uid.toString()
        mUser!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user_token= task.result.token.toString()
                    if (user_token != null) {
                        Log.d("TOKENPRINT", user_token)
                        //PERFORMING NETWORK REQUEST
                        GlobalScope.launch(Dispatchers.Default) {
                            while (true) {
                                Log.i("USERID: ", uid)
                                //GET AND UPDATE POSITION OF THE OTHER USER
                                getPositionFromServer(user_token, uid, ot_user_email)
                                //HARD CODED THE USER I WANT TO CHECK POSITION
                                //Log.i("ASKING FOR OTHER USER POS: ", "coco")
                                //getPositionFromServer(user_token, "a0a0a0a0a0", "coco@email.com")
                                //UPDATE THE TARGET LOCATION EVERY "DELAYMILLIS"
                                targetLocation.latitude = ot_respData.latitude // your coordinates here
                                targetLocation.longitude = ot_respData.longitude
                                //GET AND UPDATE OWN POSITION
                                //Log.i("ASKING FOR CURRENT USER POS: ", "coco")
                                getPositionFromServer(user_token, curUserId, curUserEmail)
                                //UPDATE THE TARGET LOCATION EVERY "DELAYMILLIS"
                                myLocation.latitude = cur_respData.latitude // your coordinates here
                                myLocation.longitude = cur_respData.longitude
                                myLocation.altitude = cur_respData.altitude

                                delay(delayMillis)
                                print("Position received from server")
                            }
                        }

                    }
                } else {
                    // Handle error -> task.getException();
                }
            }
    }
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // No child views to position in this example
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(350, 350)
        Log.i("ONMCAL","onm")


    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val originalImageSize = size // assuming size is the size of the bitmap
        val scaleFactor = 0.60f // Adjust this scaling factor as needed
        val imageSize = originalImageSize * scaleFactor
        compass = ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_upward_24,
            null)?.
        toBitmap(imageSize.toInt(),imageSize.toInt())!!
        Log.i("CONTROLONDRAWCALLED","drawing"+System.currentTimeMillis())
        if (isLocationEnabled(context)){
        with(canvas) {
            drawColor(Color.YELLOW)
                    //add minus to location angle
                    withRotation(-rotationangle, imageSize/2, imageSize/2) {
                        //drawBitmap(compass, x.toFloat(), y.toFloat(), null)
                        drawBitmap(compass, 0f, 0f, null)

            }
        }}
        else
            with(canvas) {
                drawColor(Color.RED)
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
        Log.d("LOCATIONSDATA: ", "mylocation: "+ myLocation.latitude+ " --- "+ myLocation.longitude + " targetlocation " + targetLocation.latitude + " --- " + targetLocation.longitude )
        if(myLocation.latitude>0) {
            var bearing = myLocation.bearingTo(targetLocation)
            //Log.i(TAG2, "bearing to" + bearing)
            rotationangle = yaw

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
        }



    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
    }

    override fun onFlushCompleted(p0: Sensor?) {
        //      TODO("Not yet implemented")
    }

    private fun getPositionFromServer(token: String, user_id: String, user_email: String) {
        GlobalScope.launch {
            val url = "https://androidproject.pythonanywhere.com/get_position"
            val json = JSONObject()
            json.put("user_id", "$user_id")
            json.put("token", "$token")
            json.put("user_email", "$user_email")


            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                val responseBody = response.body?.string() // response
                if (responseBody != null) {
                    Log.d("FETCHEDPOSITION", responseBody)
                }

                val gson = Gson()


                    //write on a global variable, because Coroutine does not allow to return value
                    if(mUser?.email.toString()==user_email){
                        cur_respData = gson.fromJson(responseBody, ResponseData::class.java)
                    }
                    else
                        ot_respData = gson.fromJson(responseBody, ResponseData::class.java)

                    //println("Response email: ${respData.email}")
            } catch (e: Exception) {
                //Log.d("ERRORHERE", "errorhere")

                //e.printStackTrace()}


            //println("Response Body: $responseBody")
        }
    }


}}