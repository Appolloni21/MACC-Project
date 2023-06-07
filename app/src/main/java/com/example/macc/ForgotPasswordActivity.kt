package com.example.macc

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel


private const val TAG = "Forgot Password Activity"

class ForgotPasswordActivity : AppCompatActivity() {

    private val sharedViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.forgot_password)

        //back navigation
        val backIcon : ImageView = findViewById(R.id.back_icon2)
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val submitButton: Button = findViewById(R.id.submit_btn)
        submitButton.setOnClickListener {

            val email = findViewById<EditText>(R.id.email_fp).text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(email) -> {
                    makeToast("Please enter email")
                }
                else -> {
                    //logOutUser(email)
                    sharedViewModel.forgotPassword(email)
                }
            }
        }

        sharedViewModel.forgotPswState.observe(this){
            when(it){
                UIState.SUCCESS -> {
                    makeToast("Email sent successfully to reset your password")
                    finish()
                }
                UIState.FAILURE -> {
                    makeToast("Password reset failed.")
                }
            }
        }
    }

    private fun makeToast(msg:String){
        Toast.makeText(
            this@ForgotPasswordActivity,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}