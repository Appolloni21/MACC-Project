package com.example.macc

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)


        val passwordEditText = findViewById<EditText>(R.id.password_text)

        val loginButton: Button = findViewById(R.id.login_btn)
        loginButton.setOnClickListener {
            when {
                TextUtils.isEmpty(
                    findViewById<EditText>(R.id.email_text).text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter email",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(
                    findViewById<EditText>(R.id.password_text).text.toString()
                        .trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {


                    val email: String =
                        findViewById<EditText>(R.id.email_text).text.toString().trim { it <= ' ' }
                    val password: String =
                        passwordEditText.text.toString().trim { it <= ' ' }

                    //Login user with email and password
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener{ task ->
                            //If the login is successfully done
                            if (task.isSuccessful) {

                                Toast.makeText(
                                    this@LoginActivity,
                                    "You are logged in successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                //User is logged in, we send him to the homepage
                                val intent = Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                //If the login was not successful, then show the error message
                                Toast.makeText(
                                    this@LoginActivity,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                }
            }
        }

        val signupButton:Button = findViewById(R.id.sign_up_btn)
        signupButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        val forgotPswTv: TextView = findViewById(R.id.forgot_password)
        forgotPswTv.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }


    }

}

