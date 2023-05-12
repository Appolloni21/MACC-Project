package com.example.macc.repository


import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.Expense
import com.example.macc.model.Travel
import com.example.macc.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
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

    fun getTravels(travelArrayList: MutableLiveData<ArrayList<Travel>>){
        databaseReference = Firebase.database.getReference("travels")
        databaseReference.orderByChild("members/$userUid").equalTo(true).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    val travelsList : ArrayList<Travel> = arrayListOf()
                    if(snapshot.exists()){
                        for(travelSnapshot in snapshot.children){
                            val travel = travelSnapshot.getValue(Travel::class.java)!!
                            travel.travelID = travelSnapshot.key
                            travelsList.add(travel)
                        }
                    }
                    //postValue funziona correttamente insieme ai vari listener
                    travelArrayList.postValue(travelsList)
                }catch(e: Exception){
                    Log.d(TAG,"getTravels Exception: $e")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getTravels:onCancelled", databaseError.toException())
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

    fun getExpenses(travelID: String, expenseArrayList: MutableLiveData<ArrayList<Expense>>){
        databaseReference = Firebase.database.getReference("expenses")
        databaseReference.orderByChild("travelID").equalTo(travelID).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    //Dobbiamo aggiungere le expense prima a questa lista e poi metterla dentro la MutableLiveData tutta insieme.
                    val expenseList : ArrayList<Expense> = arrayListOf()
                    if(snapshot.exists()){
                        for(expenseSnapshot in snapshot.children){
                            val expense = expenseSnapshot.getValue(Expense::class.java)!!
                            expenseList.add(expense)
                        }
                    }
                    //postValue funziona correttamente insieme ai vari listener
                    expenseArrayList.postValue(expenseList)
                }catch(e: Exception){
                    Log.d(TAG,"getExpenses exception: $e")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getExpenses:onCancelled", databaseError.toException())
            }
        })
    }

    fun getUsers(travelID: String, userArrayList: MutableLiveData<ArrayList<User>>){
        databaseReference = Firebase.database.getReference("users")
        databaseReference.orderByChild("trips/$travelID").equalTo(true).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    val userList : ArrayList<User> = arrayListOf()
                    if(snapshot.exists()){
                        for(userSnapshot in snapshot.children){
                            val user = userSnapshot.getValue(User::class.java)!!
                            userList.add(user)
                        }
                    }
                    //postValue funziona correttamente insieme ai vari listener
                    userArrayList.postValue(userList)
                }catch(e: Exception){
                    Log.d(TAG,"getUsers exception: $e")
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getUsers:onCancelled", databaseError.toException())
            }
        })
    }

    fun addUser(userEmail: String, travelID: String, userAdded: MutableLiveData<User>, context: Context?) {
        databaseReference = Firebase.database.getReference("users")
        databaseReference.orderByChild("email").equalTo(userEmail).get().addOnSuccessListener {

                if (it.exists()) {
                    for (userSnapshot in it.children) {
                        val user = userSnapshot.getValue<User>()
                        //Prima controlliamo che l' user non sia già stato aggiunto
                        if (user?.trips?.containsKey(travelID)!!) {
                            Log.d(TAG, "addUser: the user is already in this travel")
                            makeToast(context, "This user is already in this travel!")
                        }
                        else{
                            val userID = userSnapshot.key.toString()
                            //Prima aggiungiamo lo user ai membri del travel
                            databaseReference = Firebase.database.getReference("travels")
                            databaseReference.child(travelID).child("members").child(userID)
                                .setValue(true).addOnSuccessListener {
                                    Log.d(TAG, "addUser: Add user in travel members: success")

                                    //Aggiungiamo il riferimento del travel anche nella lista "trips" dell'utente
                                    databaseReference = Firebase.database.getReference("users")
                                    databaseReference.child(userID).child("trips").child(travelID)
                                        .setValue(true).addOnSuccessListener {
                                            Log.d(TAG, "addUser: Add travel in user trips: success")
                                            //L'utente è stato aggiunto correttamente, ora notifichiamo l'observer con postValue
                                            userAdded.postValue(user)
                                            makeToast(context, "User added")
                                        }.addOnFailureListener {
                                            Log.d(TAG, "addUser: Add travel in user trips: failure")
                                        }
                                }.addOnFailureListener {
                                    Log.d(TAG, "addUser: Add user in travel members: failure")
                                }
                        }
                    }
                } else{
                    Log.d(TAG, "addUser: the user doesn't exist")
                    makeToast(context, "User doesn't exist")
                }
             /*catch (e: Exception) {
                Log.d(TAG, "addUser: exception: $e")
            }*/
        }.addOnFailureListener {
            Log.e(TAG, "AddUser: failure in getting the correct user", it)
        }
    }

    //TODO: addExpense
    /*fun addExpense(){
        databaseReference = Firebase.database.getReference("expenses")
        val key = databaseReference.push().key.toString()
        Log.d(TAG, key)
    }*/

    //TODO: deleteExpense
    //TODO: deleteTravel

    private fun makeToast(context: Context?, msg:String){
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}