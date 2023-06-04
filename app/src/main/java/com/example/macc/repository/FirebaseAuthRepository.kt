package com.example.macc.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.macc.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
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
                   trips:Map<String,Boolean>, imgAvatar: Uri, userData: MutableLiveData<FirebaseUser>, context: Context){

        return withContext(Dispatchers.IO){
            try {
                // Initialize Firebase Auth
                auth = Firebase.auth

                //Sign up user with email and password in Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser: FirebaseUser = result.user!!

                //Carichiamo l'avatar sul Firebase Cloud Storage
                val userUid:String = firebaseUser.uid
                storageReference = Firebase.storage.getReference("users/$userUid")

                //Carichiamo l'avatar dell'utente nel Firebase storage
                val task = storageReference.putFile(imgAvatar).await().storage.downloadUrl.await()
                val avatar: String = task.toString()

                //register the user of Firebase Authenticator also on the Realtime Database
                val user = User(name,surname,nickname,description,email,avatar,trips)
                databaseReference = Firebase.database.getReference("users")
                databaseReference.child(userUid).setValue(user).await()
                withContext(Dispatchers.Main){
                    makeToast(context,"You are registered successfully")
                }

                Log.d(TAG, "signUpUser: success")
                //Aggiorniamo il MutableLiveData
                userData.postValue(firebaseUser)

            } catch(e: Exception){
                Log.d(TAG,"signUpUser exception: $e")
                withContext(Dispatchers.Main){
                    makeToast(context, "Error in the registration")
                }
            }
        }
    }

    suspend fun logInUser(email: String, password: String, userData: MutableLiveData<FirebaseUser>, context: Context){
        return withContext(Dispatchers.IO){
            try {
                // Initialize Firebase Auth
                auth = Firebase.auth

                //Log in user with email and password in Firebase Auth
                val result = auth.signInWithEmailAndPassword(email, password).await()

                val firebaseUser: FirebaseUser = result.user!!
                userData.postValue(firebaseUser)
                withContext(Dispatchers.Main){
                    makeToast(context,"You are logged in successfully")
                }
                Log.d(TAG,"logInUser: success")

            } catch(e: Exception){
                Log.d(TAG,"logInUser exception: $e")
                withContext(Dispatchers.Main){
                    makeToast(context, "Error in logging in")
                }
            }
        }
    }

    private fun makeToast(context: Context, msg:String){
        Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}