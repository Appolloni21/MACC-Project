package com.example.macc



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


private const val TAG: String = "SignUp Activity"

class SignUpActivity : AppCompatActivity() {

    private lateinit var realtimeDatabase: DatabaseReference
    private lateinit var imageAvatarURI: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        //back navigation
        val backIcon: ImageView = findViewById(R.id.back_icon)
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
                imageAvatarURI = uri
                findViewById<ImageView>(R.id.avatarImg)?.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val chooseTravelCoverButton = findViewById<Button>(R.id.chooseAvatarButton)
        chooseTravelCoverButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val signupButton: Button = findViewById(R.id.signup_btn)
        signupButton.setOnClickListener {

            val name: String = findViewById<EditText>(R.id.name).text.toString().trim { it <= ' ' }
            val surname: String = findViewById<EditText>(R.id.surname).text.toString().trim { it <= ' ' }
            val nickname: String = findViewById<EditText>(R.id.nickname).text.toString().trim { it <= ' ' }
            val description: String = ""
            val email: String = findViewById<EditText>(R.id.email).text.toString().trim { it <= ' ' }
            val password: String = findViewById<EditText>(R.id.password).text.toString().trim { it <= ' ' }
            val trips:Map<String,Boolean> = mapOf()


            when {
                TextUtils.isEmpty(name) -> {
                    makeToast("name")
                }

                TextUtils.isEmpty(surname) -> {
                    makeToast("surname")
                }

                TextUtils.isEmpty(nickname) -> {
                    makeToast("nickname")
                }

                TextUtils.isEmpty(email) -> {
                    makeToast("email")
                }

                TextUtils.isEmpty(password) -> {
                    makeToast("password")
                }

                !passwordsAreEquals(this@SignUpActivity) -> {
                    makeToast("Passwords are not equals")
                }

                else -> {
                    signUpUser(name, surname, nickname, description, email, password, trips)
                }
            }
        }

    }

    private fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String, password:String, trips:Map<String,Boolean>){
        //Sign-up user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this)
            { task ->
                //If the registration is successfully done
                if (task.isSuccessful) {

                    //register the user on Firebase Authenticator also on the Realtime Database
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val userUid:String = firebaseUser.uid

                    val storage = Firebase.storage.getReference("users/$userUid/avatar")

                    //Carichiamo l'avatar dell'utente nel Firebase storage
                    storage.putFile(imageAvatarURI).continueWithTask { taskStorage ->
                        if (!taskStorage.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        storage.downloadUrl
                    }.addOnCompleteListener { taskStorage ->
                        if (taskStorage.isSuccessful) {
                            Log.d(TAG, "Upload user avatar on Firebase Storage: success")
                            val downloadUri = task.result
                            val avatar: String = downloadUri.toString()


                            //Adesso aggiungiamo l'utente nel Realtime database
                            val user = User(name,surname,nickname,description,email,avatar,trips)
                            realtimeDatabase = Firebase.database.getReference("users")
                            realtimeDatabase.child(userUid).setValue(user).addOnSuccessListener {
                                Log.d(TAG, "create user in Realtime db: success")
                            }.addOnFailureListener{
                                Log.d(TAG, "create user in Realtime db: failure")
                            }

                        } else {
                            // Handle failures
                            Log.d(TAG, "Upload user avatar on Firebase Storage: failure")
                        }
                    }

                    makeToast("You are registered successfully")
                    Log.d(TAG, "createUserWithEmail:success")

                    //User is registered and so logged in, we send him to the homepage
                    val intent = Intent(
                        this@SignUpActivity,
                        MainActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                else {
                    //If the registration was not successful, then show the error message
                    makeToast(task.exception!!.message.toString())
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }
    private fun makeToast(msg:String){
        Toast.makeText(
            this@SignUpActivity,
            "Please enter $msg",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun passwordsAreEquals (registrationActivity: SignUpActivity): Boolean{
    return registrationActivity.findViewById<EditText>(R.id.password).text.toString() == registrationActivity.findViewById<EditText>(R.id.repeat_password).text.toString()
}


