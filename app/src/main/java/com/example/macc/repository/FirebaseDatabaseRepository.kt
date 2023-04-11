package com.example.macc.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.Travel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private const val TAG = "FirebaseDatabaseRepository"

class FirebaseDatabaseRepository {
    private var databaseReference: DatabaseReference = Firebase.database.getReference("travels")
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
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
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


}