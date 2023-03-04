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
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)
        val tag = "SignUpActivity"

        //back navigation
        val backIcon : ImageView = findViewById(R.id.back_icon)
        backIcon.setOnClickListener {
            this@SignUpActivity.onBackPressed()
        }

        val signupButton: Button = findViewById(R.id.signup_btn)
        signupButton.setOnClickListener {
            when {
                TextUtils.isEmpty(
                    findViewById<EditText>(R.id.email).text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please enter email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(
                    findViewById<EditText>(R.id.password).text.toString()
                        .trim { it <= ' ' }) -> {
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

                    val email: String =
                        findViewById<EditText>(R.id.email).text.toString().trim { it <= ' ' }
                    val password: String =
                        findViewById<EditText>(R.id.password).text.toString().trim { it <= ' ' }

                    //Sign-up user with email and password
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this)
                        { task ->
                            //If the registration is successfully done
                            if (task.isSuccessful) {


                                //register the user on Firebase Authenticator also on the Realtime Database
                                //registerUser(task, getHashedUser())
                                //TODO: inserire user anche nel Realtime Database

                                Toast.makeText(
                                    this@SignUpActivity,
                                    "You are registered successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d(tag, "createUserWithEmail:success")

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
                                Log.w(tag, "createUserWithEmail:failure", task.exception)
                            }
                        }
                }
            }
        }

    }
}

fun passwordsAreEquals (registrationActivity: SignUpActivity): Boolean{
    return registrationActivity.findViewById<EditText>(R.id.password).text.toString() == registrationActivity.findViewById<EditText>(R.id.repeat_password).text.toString()
}