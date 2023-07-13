package com.example.macc.repository


import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.User
import com.example.macc.utility.UIState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "Firebase Auth Repository"

class FirebaseAuthRepository {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth


    suspend fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String, password:String,
                    imgAvatar: Uri, userID: MutableLiveData<String?>): String =
        withContext(Dispatchers.IO){
            try {
                // Initialize Firebase Auth
                auth = Firebase.auth

                //Sign up user with email and password in Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser = result.user!!
                userID.postValue(result.user?.uid)

                //Carichiamo l'avatar sul Firebase Cloud Storage
                val userUid:String = firebaseUser.uid
                storageReference = Firebase.storage.getReference("users/$userUid")

                //Carichiamo l'avatar dell'utente nel Firebase storage
                val task = storageReference.putFile(imgAvatar).await().storage.downloadUrl.await()
                val avatar: String = task.toString()

                //register the user of Firebase Authenticator also on the Realtime Database
                val user = User(name,surname,nickname,description,email,avatar,null)
                databaseReference = Firebase.database.getReference("users")
                databaseReference.child(userUid).setValue(user).await()

                Log.d(TAG, "signUpUser: success")
                UIState.SUCCESS

            } catch(e: Exception){
                Log.d(TAG,"signUpUser exception: $e")
                UIState.FAILURE

            }
        }


    suspend fun logInUser(email: String, password: String, userID: MutableLiveData<String?>):String =
        withContext(Dispatchers.IO){
            try {
                // Initialize Firebase Auth
                auth = Firebase.auth

                //Log in user with email and password in Firebase Auth
                val result = auth.signInWithEmailAndPassword(email, password).await()
                userID.postValue(result.user?.uid)

                Log.d(TAG,"logInUser: success")
                UIState.SUCCESS

            } catch(e: Exception){
                Log.d(TAG,"logInUser exception: $e")
                UIState.FAILURE
            }
        }

    suspend fun logOutUser():String =
        withContext(Dispatchers.IO){
            try {
                //Log Out with Firebase Auth
                Firebase.auth.signOut()
                Log.d(TAG,"logOutUser: success")
                UIState.SUCCESS
            } catch(e: Exception){
                Log.d(TAG,"logOutUser exception: $e")
                UIState.FAILURE
        }
    }

    suspend fun forgotPassword(email: String): String =
        withContext(Dispatchers.IO){
            try {
                Firebase.auth.sendPasswordResetEmail(email).await()
                Log.d(TAG,"forgotPassword: success")
                UIState.SUCCESS
            } catch(e: Exception){
                Log.d(TAG,"forgotPassword exception: $e")
                UIState.FAILURE
            }
        }

    fun getUserMyProfile(userID: String, userMyProfile: MutableLiveData<User?>){
        databaseReference = Firebase.database.getReference("users")
        databaseReference.child(userID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)!!
                        userMyProfile.postValue(user)
                    }
                }catch(e: Exception){
                    Log.d(TAG,"getUserMyProfile exception: $e")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "getUserMyProfile:onCancelled", databaseError.toException())
            }
        })
    }
}