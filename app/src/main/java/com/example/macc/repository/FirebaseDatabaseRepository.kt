package com.example.macc.repository


import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.Travel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

private const val TAG = "FirebaseDatabaseRepository"

class FirebaseDatabaseRepository {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private val userUid = Firebase.auth.currentUser?.uid.toString()

    @Volatile private var ISTANCE: FirebaseDatabaseRepository ?= null

    fun getIstance() : FirebaseDatabaseRepository{
        return ISTANCE ?: synchronized(this){
            val istance = FirebaseDatabaseRepository()
            ISTANCE = istance
            istance
        }
    }

    fun loadTravelsHome(travelArrayList: MutableLiveData<ArrayList<Travel>>){
        databaseReference = Firebase.database.getReference("travels")
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    //Dobbiamo aggiungere i viaggi prima a questa lista e poi metterla dentro la MutableLiveData tutta insieme.
                    //Se aggiungessimo i viaggi direttamente a travelArrayList non funzionerebbe
                    val travelsList : ArrayList<Travel> = arrayListOf()
                    if(snapshot.exists()){
                        for(travelSnapshot in snapshot.children){
                            val travel = travelSnapshot.getValue(Travel::class.java)

                            //Dall'elenco dei travel nel db selezioniamo solo quelli che matchano con i travel effettivamente fatti dall'utente
                            if(travel?.members?.containsKey(userUid) == true){
                                travelsList.add(travel)
                            }
                        }
                    }
                    //postValue funziona correttamente insieme ai vari listener
                    travelArrayList.postValue(travelsList)
                }catch(e: Exception){
                    Log.d(TAG,"$e")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getTravelsHome:onCancelled", databaseError.toException())
            }

        })
    }

    fun addTravel(travelName:String, destination:String, startDate:String, endDate:String, imgCover: Uri){
        databaseReference = Firebase.database.getReference("travels")

        val key = databaseReference.push().key.toString()
        storageReference = Firebase.storage.getReference("travels/$key/img")

        //Carichiamo l'immagine del travel nel Firebase storage
        storageReference.putFile(imgCover).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Upload travel cover on Firebase Storage: success")
                val downloadUri = task.result
                val imgUrl: String = downloadUri.toString()

                //Aggiungiamo il travel nell'elenco del Realtime db
                val members: Map<String,Boolean> = mapOf(userUid to true)
                val travel = Travel(travelName,destination, startDate,endDate, imgUrl, members)
                databaseReference.child(key).setValue(travel).addOnSuccessListener {
                    Log.d(TAG, "create travel in Realtime db: success")
                }.addOnFailureListener{
                    Log.d(TAG, "create travel in Realtime db: failure")
                }

                //Dobbiamo aggiungere il riferimento del travel anche nella lista "trips" dell'utente corrente
                databaseReference = Firebase.database.getReference("users")
                databaseReference.child(userUid).child("trips").child(key).setValue(true).addOnSuccessListener {
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