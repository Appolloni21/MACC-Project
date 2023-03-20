package com.example.macc


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


private const val TAG: String = "SignUp Activity"

class SignUpActivity : AppCompatActivity() {

    private lateinit var realtimeDatabase: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        //back navigation
        val backIcon: ImageView = findViewById(R.id.back_icon)
        backIcon.setOnClickListener {
            this@SignUpActivity.onBackPressed()
        }


        val signupButton: Button = findViewById(R.id.signup_btn)
        signupButton.setOnClickListener {

            val name: String = findViewById<EditText>(R.id.name).text.toString().trim { it <= ' ' }
            val surname: String = findViewById<EditText>(R.id.surname).text.toString().trim { it <= ' ' }
            val nickname: String = findViewById<EditText>(R.id.nickname).text.toString().trim { it <= ' ' }
            val email: String = findViewById<EditText>(R.id.email).text.toString().trim { it <= ' ' }
            val password: String = findViewById<EditText>(R.id.password).text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(name) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please enter name",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(surname) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please enter surname",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(nickname) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please enter surname",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(email) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please enter email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(password) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please enter password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !passwordsAreEquals(this@SignUpActivity) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Passwords are not equals",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                    signUpUser(name, surname, nickname, email, password)
                }
            }
        }

    }

    private fun signUpUser(name:String, surname:String, nickname:String, email:String, password:String){
        //Sign-up user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this)
            { task ->
                //If the registration is successfully done
                if (task.isSuccessful) {

                    //register the user on Firebase Authenticator also on the Realtime Database
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val user = User(name,surname,nickname,email)
                    realtimeDatabase = Firebase.database.getReference("users")
                    realtimeDatabase.child(firebaseUser.uid).setValue(user).addOnSuccessListener {
                        Log.d(TAG, "create user in Realtime db: success")
                    }.addOnFailureListener{
                        Log.d(TAG, "create user in Realtime db: failure")
                    }

                    Toast.makeText(
                        this@SignUpActivity,
                        "You are registered successfully",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    Toast.makeText(
                        this@SignUpActivity,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }
}

private fun passwordsAreEquals (registrationActivity: SignUpActivity): Boolean{
    return registrationActivity.findViewById<EditText>(R.id.password).text.toString() == registrationActivity.findViewById<EditText>(R.id.repeat_password).text.toString()
}


