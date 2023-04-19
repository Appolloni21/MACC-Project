package com.example.macc.repository


import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.Expense
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

    fun addTravel(travelName:String, destination:String, startDate:String, endDate:String, imgCover: Uri, travelAdded: MutableLiveData<Travel>, context: Context?){
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

                    //Aggiorniamo il MutableLiveData per triggherare il cambio pagina dell'UI
                    travelAdded.postValue(travel)
                    makeToast(context,"The travel has been added!")

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

    fun loadTravelExpenses(position: Int, travelArrayList: MutableLiveData<ArrayList<Travel>>, expenseArrayList: MutableLiveData<ArrayList<Expense>>){
        val travel = travelArrayList.value!![position]
        val expensesRef = travel.expenses
        databaseReference = Firebase.database.getReference("expenses")

        //da qui si fa la get (onDataChange) e si selezionano solo le expenses con id uguali a quelli contenuti in expenseList
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    //Dobbiamo aggiungere le expense prima a questa lista e poi metterla dentro la MutableLiveData tutta insieme.
                    val expenseList : ArrayList<Expense> = arrayListOf()
                    if(snapshot.exists()){
                        for(expenseSnapshot in snapshot.children){
                            val expense = expenseSnapshot.getValue(Expense::class.java)

                            //Dall'elenco delle expense nel db selezioniamo solo quelli che matchano con i riferimenti expense contenuti nel travel
                            if(expensesRef!!.containsKey(expenseSnapshot.key)){
                                if (expense != null) {
                                    expenseList.add(expense)
                                }
                            }
                        }
                    }
                    //postValue funziona correttamente insieme ai vari listener
                    expenseArrayList.postValue(expenseList)
                }catch(e: Exception){
                    Log.d(TAG,"$e")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadTravelExpenses:onCancelled", databaseError.toException())
            }
        })
    }

    //Temp
    /*fun addExpense(){
        databaseReference = Firebase.database.getReference("expenses")
        val key = databaseReference.push().key.toString()
        Log.d(TAG, key)
    }*/

    private fun makeToast(context: Context?, msg:String){
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}