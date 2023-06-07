package com.example.macc

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity(){

    private val sharedViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        val loginButton: Button = findViewById(R.id.login_btn)
        loginButton.setOnClickListener {

            val email: String = findViewById<EditText>(R.id.email_text).text.toString().trim { it <= ' ' }
            val password: String = findViewById<EditText>(R.id.password_text).text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(email) -> {
                    makeToast("email")
                }
                TextUtils.isEmpty(password) -> {
                    makeToast("password")
                }
                else -> {

                    //Login user with email and password
                    sharedViewModel.logInUser(email,password)
                }
            }
        }

        sharedViewModel.logInState.observe(this){
            when(it){
                UIState.SUCCESS -> {
                    //User is logged in, we send him to the homepage
                    Toast.makeText(this@LoginActivity, "You logged in, welcome back", Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                UIState.FAILURE -> {
                    Toast.makeText(this@LoginActivity, "Error in logging in", Toast.LENGTH_SHORT).show()
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

    private fun makeToast(msg:String){
        Toast.makeText(
            this@LoginActivity,
            "Please enter $msg",
            Toast.LENGTH_SHORT
        ).show()
    }

}

