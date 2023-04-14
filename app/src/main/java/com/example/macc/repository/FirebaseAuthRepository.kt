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

private const val TAG = "Firebase Auth Repository"

class FirebaseAuthRepository {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth


    fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String, password:String,
                   trips:Map<String,Boolean>, imgAvatar: Uri, userData: MutableLiveData<FirebaseUser>, context: Context){

        // Initialize Firebase Auth
        auth = Firebase.auth

        //Sign up user with email and password in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener()
            { task ->

                //If the registration is successfully done
                if (task.isSuccessful) {

                    //Aggiorniamo il MutableLiveData
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    userData.postValue(firebaseUser)

                    //Carichiamo l'avatar sul Firebase Cloud Storage
                    val userUid:String = firebaseUser.uid
                    storageReference = Firebase.storage.getReference("users/$userUid/avatar")

                    //Carichiamo l'avatar dell'utente nel Firebase storage
                    storageReference.putFile(imgAvatar).continueWithTask { taskStorage ->
                        if (!taskStorage.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        storageReference.downloadUrl
                    }.addOnCompleteListener { taskStorage ->
                        if (taskStorage.isSuccessful) {

                            Log.d(TAG, "Upload user avatar on Firebase Storage: success")
                            val downloadUri = task.result
                            val avatar: String = downloadUri.toString()

                            //register the user of Firebase Authenticator also on the Realtime Database
                            val user = User(name,surname,nickname,description,email,avatar,trips)
                            databaseReference = Firebase.database.getReference("users")
                            databaseReference.child(userUid).setValue(user).addOnSuccessListener {
                                Log.d(TAG, "create user in Realtime db: success")
                            }.addOnFailureListener{
                                Log.d(TAG, "create user in Realtime db: failure")
                            }

                        } else {
                            // Handle failures
                            Log.d(TAG, "Upload user avatar on Firebase Storage: failure")
                        }
                    }

                    makeToast(context,"You are registered successfully")
                    Log.d(TAG, "createUserWithEmail:success")

                }
                else {
                    //If the registration was not successful, then show the error message
                    makeToast(context, "Error in the registration")
                    Log.d(TAG, task.exception!!.message.toString() )
                    Log.d(TAG, "createUserWithEmail: failure", task.exception)
                }
            }
    }

    fun logInUser(email: String, password: String, userData: MutableLiveData<FirebaseUser>, context: Context){

        // Initialize Firebase Auth
        auth = Firebase.auth

        //Log in user with email and password in Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->

                //If the login is successfully done
                if (task.isSuccessful) {

                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    userData.postValue(firebaseUser)
                    makeToast(context,"You are logged in successfully")

                } else {

                    //If the login was not successful, then show the error message
                    Log.d(TAG, task.exception!!.message.toString())
                    Log.d(TAG, "signInWithEmailAndPassword: failure", task.exception)
                    makeToast(context, "Error in logging in")
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