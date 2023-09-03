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
    private val userUID = Firebase.auth.currentUser?.uid.toString()

    @Volatile private var istance: FirebaseDatabaseRepository ?= null

    fun getIstance() : FirebaseDatabaseRepository{
        return istance ?: synchronized(this){
            val istance = FirebaseDatabaseRepository()
            this.istance = istance
            istance
        }
    }

    fun getTravels(travelArrayList: MutableLiveData<ArrayList<Travel>>){
        databaseReference = Firebase.database.getReference("travels")
        databaseReference.orderByChild("members/$userUID").equalTo(true).addValueEventListener(object: ValueEventListener{
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
                val members: Map<String,Boolean> = mapOf(userUID to true)
                val owner: String = userUID
                val travel = Travel(travelName,destination,startDate,endDate,imgUrl,members,null,travelID,owner)
                childUpdates["travels/$travelID"] = travel

                //Aggiungiamo il riferimento del viaggio anche nella lista "trips" dell'utente corrente
                childUpdates["users/$userUID/trips/$travelID"] = true
                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "addTravel: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "addTravel failure exception: $e")
                UIState.FAILURE
            }

    }

    suspend fun deleteTravel(travel: Travel): String =
        withContext(Dispatchers.IO){
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
                if(!travel.expenses.isNullOrEmpty()){
                    for(key in travel.expenses!!.keys){
                        childUpdates["expenses/$key"] = null
                    }
                }

                //Ora cancelliamo il viaggio dall'elenco principale
                childUpdates["travels/$travelID"] = null

                //Eseguiamo le query
                databaseReference.updateChildren(childUpdates).await()

                //Ora dobbiamo cancellare anche il riferimento su Firebase Storage
                storageReference = Firebase.storage.getReference("travels")
                storageReference.child("$travelID").delete().await()
                Log.d(TAG, "deleteTravel: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "deleteTravel: exception: $e")
                UIState.FAILURE
            }
        }

    suspend fun editTravel(travelID: String, travelName: String, destination: String, imgCover: Uri): String =
        withContext(Dispatchers.IO){
            try {
                databaseReference = Firebase.database.reference

                val travel = databaseReference.child("travels").child(travelID).get().await()
                if(!travel.exists()){
                    return@withContext UIState.FAILURE
                }

                val childUpdates = hashMapOf<String, Any?>()
                childUpdates["travels/$travelID/name"] = travelName
                childUpdates["travels/$travelID/destination"] = destination

                if(imgCover != Uri.EMPTY){
                    //Carichiamo la cover sul Firebase Cloud Storage sostituendola a quello vecchio
                    storageReference = Firebase.storage.getReference("travels/$travelID")
                    val task = storageReference.putFile(imgCover).await().storage.downloadUrl.await()
                    val coverRef: String = task.toString()
                    childUpdates["travels/$travelID/imgUrl"] = coverRef
                }

                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "editTravel: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "editTravel: exception: $e")
                UIState.FAILURE
            }
        }

    fun getSelectedTravels(travelID: String, travelSelected: MutableLiveData<Travel>, uiState: MutableLiveData<String?>){
        databaseReference = Firebase.database.getReference("travels/$travelID")
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    if(snapshot.exists()){
                        val travel = snapshot.getValue(Travel::class.java)!!
                        when(travel.members?.containsKey(userUID)){
                            true ->{
                                //postValue funziona correttamente insieme ai vari listener
                                travelSelected.postValue(travel)
                            }
                            false ->{
                                uiState.postValue(UIState.WARN_104)
                            }
                            null ->{

                            }
                        }
                    }
                }catch(e: Exception){
                    Log.d(TAG,"getSelectedTravels Exception: $e")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getSelectedTravels: onCancelled", databaseError.toException())
            }
        })
    }

    suspend fun quitFromTravel(travel: Travel): String =
        withContext(Dispatchers.IO){
            try {
                databaseReference = Firebase.database.reference
                val childUpdates = hashMapOf<String, Any?>()
                val travelID = travel.travelID.toString()

                //Leviamo dal travel il riferimento all'utente
                childUpdates["travels/$travelID/members/$userUID"] = null

                //Leviamo dall'utente il riferimento al travel
                childUpdates["users/$userUID/trips/$travelID"] = null

                //Cancelliamo le spese dell'utente associate al viaggio
                databaseReference = Firebase.database.getReference("expenses")
                val snapshot = databaseReference.orderByChild("travelID").equalTo(travelID).get().await()

                if(snapshot.exists()){
                    for(expenseSnapshot in snapshot.children){
                        val expense = expenseSnapshot.getValue(Expense::class.java)!!
                        if(expense.owner.equals(userUID)){
                            val expenseID = expense.expenseID.toString()
                            //Cancelliamo le spese dell'utente associate al viaggio, dall'elenco principale delle spese
                            childUpdates["expenses/$expenseID"] = null
                            //Cancelliamo il riferimento della spesa dallo user
                            childUpdates["users/$userUID/expenses/$expenseID"] = null
                            //Cancelliamo dal viaggio il riferimento della spesa
                            childUpdates["travels/$travelID/expenses/$expenseID"] = null
                        }
                    }
                }

                //Eseguiamo le query
                databaseReference = Firebase.database.reference
                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "exitFromTravel: success")
                UIState.SUCC_101

            } catch (e: Exception) {
                Log.d(TAG, "exitFromTravel: exception: $e")
                UIState.FAIL_105
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
                            if(expense.owner.equals(userUID) || expense.owner.equals("Group")){
                                expenseList.add(expense)
                            }
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
                            user.userID = userSnapshot.key
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

    fun getSelectedUser(userID: String, userSelected: MutableLiveData<User>){
        databaseReference = Firebase.database.getReference("users/$userID")
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)!!
                        user.userID = snapshot.key
                        //postValue funziona correttamente insieme ai vari listener
                        userSelected.postValue(user)
                    }
                }catch(e: Exception){
                    Log.d(TAG,"getSelectedExpense Exception: $e")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getSelectedExpense: onCancelled", databaseError.toException())
            }
        })
    }


    suspend fun addUser(userEmail: String, travelID: String): String =
        withContext(Dispatchers.IO) {
            databaseReference = Firebase.database.getReference("users")
            try {
                val snapshot = databaseReference.orderByChild("email").equalTo(userEmail).get().await()

                if(!snapshot.exists()){
                    Log.d(TAG,"addUser: user not found")
                    return@withContext UIState.FAIL_102
                }

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue<User>()

                    //Prima controlliamo che l' user non sia già stato aggiunto
                    if (!(user?.trips.isNullOrEmpty()) && user?.trips?.containsKey(travelID)!!) {
                        Log.d(TAG, "addUser: the user is already in this travel")
                        UIState.FAIL_101
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
                    }
                }
                Log.d(TAG, "addUser: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "addUser: exception: $e")
                UIState.FAILURE
            }
        }

    suspend fun removeUserFromTravel(user: User, travel: Travel): String =
        withContext(Dispatchers.IO){
            try {
                databaseReference = Firebase.database.reference
                val childUpdates = hashMapOf<String, Any?>()
                val travelID = travel.travelID.toString()
                val userID = user.userID

                //Leviamo dal travel il riferimento all'utente
                childUpdates["travels/$travelID/members/$userID"] = null

                //Leviamo dall'utente il riferimento al travel
                childUpdates["users/$userID/trips/$travelID"] = null

                //Cancelliamo le spese dell'utente associate al viaggio
                databaseReference = Firebase.database.getReference("expenses")
                val snapshot = databaseReference.orderByChild("travelID").equalTo(travelID).get().await()

                if(snapshot.exists()){
                    for(expenseSnapshot in snapshot.children){
                        val expense = expenseSnapshot.getValue(Expense::class.java)!!
                        if(expense.owner.equals(userID)){
                            val expenseID = expense.expenseID.toString()
                            //Cancelliamo le spese dell'utente associate al viaggio, dall'elenco principale delle spese
                            childUpdates["expenses/$expenseID"] = null
                            //Cancelliamo il riferimento della spesa dallo user
                            childUpdates["users/$userID/expenses/$expenseID"] = null
                            //Cancelliamo dal viaggio il riferimento della spesa
                            childUpdates["travels/$travelID/expenses/$expenseID"] = null
                        }
                    }
                }

                //Eseguiamo le query
                databaseReference = Firebase.database.reference
                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "removeUserFromTravel: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "removeUserFromTravel: exception: $e")
                UIState.FAILURE
            }
        }


    suspend fun addExpense(travelID:String, expenseName:String, expenseAmount: String , expenseDate: String, expensePlace:String, expenseNote: String, expenseCheck: Boolean): String=
        withContext(Dispatchers.IO){
            try {
                databaseReference = Firebase.database.getReference("expenses")
                val expenseID = databaseReference.push().key.toString()

                //Aggiungiamo l'expense nell'elenco principale sul Realtime db
                databaseReference = Firebase.database.reference
                val childUpdates = hashMapOf<String, Any?>()
                val name: String = expenseName
                val amount: String = expenseAmount
                val date: String = expenseDate
                val place: String = expensePlace
                var owner = "Group"
                val notes: String = expenseNote

                if(expenseCheck){
                    owner = userUID
                    //Aggiungiamo il riferimento della expense anche nella lista "expenses" dell'utente corrente
                    childUpdates["users/$owner/expenses/$expenseID"] = true
                }

                val expense = Expense(name, amount, date, place, owner, notes, travelID, expenseID)
                childUpdates["expenses/$expenseID"] = expense

                //Aggiungiamo il riferimento della expense anche nella lista "expenses" del viaggio
                childUpdates["travels/$travelID/expenses/$expenseID"] = true

                //Eseguiamo tutto
                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "addExpense: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "addExpense failure exception: $e")
                UIState.FAILURE
            }

        }

    suspend fun deleteExpense(expense: Expense): String =
        withContext(Dispatchers.IO){
            try {
                val expenseID = expense.expenseID
                val travelID = expense.travelID

                databaseReference = Firebase.database.reference
                val childUpdates = hashMapOf<String, Any?>()

                //Cancelliamo l'expense dall'elenco principale sul Realtime db
                childUpdates["expenses/$expenseID"] = null


                //Cancelliamo il riferimento della expense anche nella lista "expenses" del viaggio
                childUpdates["travels/$travelID/expenses/$expenseID"] = null

                if(expense.owner.equals(userUID)){
                    //Cancelliamo il riferimento della expense anche nella lista "expenses" dell'utente corrente
                    childUpdates["users/$userUID/expenses/$expenseID"] = null
                }

                //Eseguiamo tutto
                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "addExpense: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "addExpense failure exception: $e")
                UIState.FAILURE
            }

        }

    suspend fun editExpense(expenseID: String, expenseName:String, expenseAmount: String , expenseDate: String, expensePlace:String, expenseNotes: String): String =
        withContext(Dispatchers.IO){
            try {
                databaseReference = Firebase.database.reference

                val expense = databaseReference.child("expenses").child(expenseID).get().await()
                if(!expense.exists()){
                    return@withContext UIState.FAILURE
                }

                val childUpdates = hashMapOf<String, Any?>()
                childUpdates["expenses/$expenseID/name"] = expenseName
                childUpdates["expenses/$expenseID/amount"] = expenseAmount
                childUpdates["expenses/$expenseID/date"] = expenseDate
                childUpdates["expenses/$expenseID/place"] = expensePlace
                childUpdates["expenses/$expenseID/notes"] = expenseNotes

                databaseReference.updateChildren(childUpdates).await()

                Log.d(TAG, "editExpense: success")
                UIState.SUCCESS

            } catch (e: Exception) {
                Log.d(TAG, "editExpense: exception: $e")
                UIState.FAILURE
            }
        }

    fun getSelectedExpense(expenseID: String, expenseSelected: MutableLiveData<Expense>){
        databaseReference = Firebase.database.getReference("expenses/$expenseID")
        databaseReference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    if(snapshot.exists()){
                        val expense = snapshot.getValue(Expense::class.java)!!
                        //postValue funziona correttamente insieme ai vari listener
                        expenseSelected.postValue(expense)
                    }
                }catch(e: Exception){
                    Log.d(TAG,"getSelectedExpense Exception: $e")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getSelectedExpense: onCancelled", databaseError.toException())
            }
        })
    }


}