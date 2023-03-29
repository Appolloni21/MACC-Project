package com.example.macc


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.model.Travel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage



private const val TAG: String = "AddTravel Fragment"

class AddTravel : Fragment() {

    private lateinit var realtimeDatabase: DatabaseReference
    private lateinit var imageCoverURI: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.add_travel_page, container,
            false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
                imageCoverURI = uri
                view.findViewById<ImageView>(R.id.travel_cover)?.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val chooseTravelCoverButton = view.findViewById<Button>(R.id.chooseTravelCoverButton)
        chooseTravelCoverButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val addTravelButton = view.findViewById<Button>(R.id.addTravelButton)
        addTravelButton.setOnClickListener{
            val userUid = Firebase.auth.currentUser?.uid.toString()
            val travelName: String = view.findViewById<TextInputLayout>(R.id.travelName)?.editText?.text.toString().trim { it <= ' ' }
            val destination: String = view.findViewById<TextInputLayout>(R.id.destination)?.editText?.text.toString().trim { it <= ' ' }
            val startDate: String = view.findViewById<EditText>(R.id.startDate)?.text.toString().trim { it <= ' ' }
            val endDate: String = view.findViewById<EditText>(R.id.endDate)?.text.toString().trim { it <= ' ' }
            val members: Map<String,Boolean> = mapOf(userUid to true)
            //TODO: gestire casistica in cui si inseriscano altri utenti
            //TODO: gestire casi in cui i campi sono vuoti e riordinare in generale il codice
            addTravel(userUid,travelName,destination,startDate,endDate,members)
            navController.navigateUp()
        }
    }


    private fun addTravel(userUid:String ,travelName:String, destination:String, startDate:String, endDate:String, members:Map<String,Boolean>){

        realtimeDatabase = Firebase.database.getReference("travels")
        val key = realtimeDatabase.push().key.toString()
        val storage = Firebase.storage.getReference("travels/$key/img")
        //Log.d(TAG, "Storage: $storage")
        //Carichiamo l'immagine del travel
        storage.putFile(imageCoverURI).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Upload travel cover on Firebase Storage: success")
                val downloadUri = task.result
                val imgUrl: String = downloadUri.toString()
                val travel = Travel(travelName,destination, startDate,endDate, imgUrl, members)

                //Aggiungiamo il travel nell'elenco del Realtime db
                realtimeDatabase.child(key).setValue(travel).addOnSuccessListener {
                    Log.d(TAG, "create travel in Realtime db: success")
                }.addOnFailureListener{
                    Log.d(TAG, "create travel in Realtime db: failure")
                }

                //Dobbiamo aggiungere il riferimento del travel anche nella lista "trips" dell'utente corrente
                realtimeDatabase = Firebase.database.getReference("users")
                realtimeDatabase.child(userUid).child("trips").child(key).setValue(true).addOnSuccessListener {
                    Log.d(TAG, "Add travel in user trips: success")
                }.addOnFailureListener{
                    Log.d(TAG, "Add travel in user trips: failure")
                }
            } else {
                // Handle failures
                Log.d(TAG, "Upload travel cover on Firebase Storage: failure")
            }
        }
    }
}