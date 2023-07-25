package com.example.macc

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.databinding.ForgotPasswordBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel


private const val TAG = "Forgot Password Activity"

class ForgotPasswordActivity : AppCompatActivity() {

    private val sharedViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val binding = ForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back navigation
        val backIcon : ImageView = binding.backIcon2
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val submitButton: Button = binding.submitBtn
        submitButton.setOnClickListener {

            val email = binding.emailFp.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(email) -> {
                    makeToast("Please enter email")
                }
                else -> {
                    sharedViewModel.forgotPassword(email)
                }
            }
        }

        sharedViewModel.uiState.observe(this){
            when(it){
                UIState.SUCCESS -> {
                    makeToast("Email sent successfully to reset your password")
                    finish()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    makeToast("Password reset failed.")
                    sharedViewModel.resetUiState()
                }
            }
        }

        Log.d(TAG,"Forgot Password Page")
    }

    private fun makeToast(msg:String){
        Toast.makeText(
            this@ForgotPasswordActivity,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}