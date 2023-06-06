package com.example.macc.repository


import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.Expense
import com.example.macc.model.Travel
import com.example.macc.model.User
import com.example.macc.utility.UIState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    suspend fun addTravel(travelName:String, destination:String, startDate:String, endDate:String, imgCover: Uri): String =
        withContext(Dispatchers.IO){
            try {
                databaseReference = Firebase.database.getReference("travels")

                //Creiamo la entry nel db con un nuovo ID
                val travelID = databaseReference.push().key.toString()

                storageReference = Firebase.storage.getReference("travels/$travelID")

                //Carichiamo l'immagine del viaggio in Firebase storage
                val task = storageReference.putFile(imgCover).await().storage.downloadUrl.await()

                val imgUrl: String = task.toString()

                //Aggiungiamo il viaggio nell'elenco principale sul Realtime db
                databaseReference = Firebase.database.reference
                val childUpdates = hashMapOf<String, Any?>()
                val members: Map<String,Boolean> = mapOf(userUid to true)
                val expenses: Map<String, Boolean> = mapOf("null" to false)
                val travel = Travel(travelName,destination, startDate,endDate, imgUrl, members, expenses)
                childUpdates["travels/$travelID"] = travel

                //Aggiungiamo il riferimento del viaggio anche nella lista "trips" dell'utente corrente
                childUpdates["users/$userUid/trips/$travelID"] = true
                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "addTravel: success")
                return@withContext UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "addTravel failure exception: $e")
                return@withContext UIState.FAILURE
            }

    }

    suspend fun deleteTravel(travel: Travel){
        return withContext(Dispatchers.IO){
            try {
                //Log.d(TAG,"deleteTravel: $travelID")
                val travelID = travel.travelID

                databaseReference = Firebase.database.reference
                val childUpdates = hashMapOf<String, Any?>()

                //Cancelliamo il riferimento del viaggio dagli utenti partecipanti
                for(key in travel.members!!.keys){
                    childUpdates["users/$key/trips/$travelID"] = null
                }
                //Cancelliamo le spese associate al viaggio
                for(key in travel.expenses!!.keys){
                    childUpdates["expenses/$key"] = null
                }
                //Ora cancelliamo il viaggio dall'elenco principale
                childUpdates["travels/$travelID"] = null

                //Eseguiamo le query
                databaseReference.updateChildren(childUpdates).await()

                //Ora dobbiamo cancellare anche il riferimento su Firebase Storage
                storageReference = Firebase.storage.getReference("travels")
                storageReference.child("$travelID").delete().await()
                Log.d(TAG, "deleteTravel: success")

            } catch (e: Exception) {
                Log.d(TAG, "deleteTravel: exception: $e")
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


    suspend fun addUser(userEmail: String, travelID: String): String =
        withContext(Dispatchers.IO) {
            databaseReference = Firebase.database.getReference("users")
            try {
                val snapshot = databaseReference.orderByChild("email").equalTo(userEmail).get().await()

                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue<User>()
                        //Prima controlliamo che l' user non sia già stato aggiunto
                        if (user?.trips?.containsKey(travelID)!!) {
                            Log.d(TAG, "addUser: the user is already in this travel")
                            return@withContext UIState._103

                        } else {
                            val userID = userSnapshot.key.toString()
                            val childUpdates = hashMapOf<String, Any?>()
                            databaseReference = Firebase.database.reference

                            //Prima aggiungiamo l' utente ai membri del viaggio
                            childUpdates["travels/$travelID/members/$userID"] = true

                            //Poi aggiungiamo il riferimento del viaggio anche nella lista "trips" dell'utente
                            childUpdates["users/$userID/trips/$travelID"] = true

                            //Eseguiamo le query
                            databaseReference.updateChildren(childUpdates).await()

                            Log.d(TAG, "addUser: success")
                            return@withContext UIState.SUCCESS
                        }
                    }
                }
                Log.d(TAG,"addUser: user not found")
                return@withContext UIState._104

            } catch (e: Exception) {
                Log.d(TAG, "addUser: exception: $e")
                return@withContext UIState.FAILURE
            }
        }


    //TODO: addExpense
    /*fun addExpense(){
        databaseReference = Firebase.database.getReference("expenses")
        val key = databaseReference.push().key.toString()
        Log.d(TAG, key)
    }*/

    //TODO: deleteExpense

}