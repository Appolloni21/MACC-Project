package com.example.macc.LocationService

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import android.util.Log
import com.example.macc.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    var user_email = ""
    var user_token = ""
    var user_id = ""

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        //TODO add handling request to server if fails to get the token
        //GET TOKEN FROM USER SESSION
        val mUser = FirebaseAuth.getInstance().currentUser
        Log.d("USERLOG", mUser.toString())
        user_email = mUser?.email.toString()
        user_id=mUser?.uid.toString()
        mUser!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user_token= task.result.token.toString()
                    if (user_token != null) {
                        Log.d("TOKENPRINT", user_token)
                    }
                } else {
                    // Handle error -> task.getException();
                }
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Sharing location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                // here is what happens when a new location is produced
                val lat = location.latitude
                val long = location.longitude
                val alt = location.altitude
                try {
                    // Call the function that might throw an exception
                    performNetworkRequest(user_id, user_token, user_email, lat.toFloat(), long.toFloat(), alt.toFloat())
                } catch (e: Exception) {
                    // Handle the exception here
                    println("Unable to contact network: ${e.message}")
                }

                val updatedNotification = notification.setContentText(
                    "Location: ($lat, $long, $alt)"
                )
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    fun performNetworkRequest (user_id: String, token: String, user_email: String, latitude : Float , longitude : Float, altitude : Float) {
        GlobalScope.launch {
            val url = "https://androidproject.pythonanywhere.com/update_position"
            val json = JSONObject()
            json.put("user_id", "$user_id")
            json.put("token", "$token")
            json.put("user_email", "$user_email")
            json.put("latitude", "$latitude")
            json.put("longitude", "$longitude")
            json.put("altitude", "$altitude")

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)

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

            println("Response Code: ${response.code}")
            println("Response Body: $responseBody")
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}