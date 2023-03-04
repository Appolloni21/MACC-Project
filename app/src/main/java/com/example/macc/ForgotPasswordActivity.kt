package com.example.macc

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.forgot_password)

        //back navigation
        val backIcon : ImageView = findViewById(R.id.back_icon2)
        backIcon.setOnClickListener {
            this@ForgotPasswordActivity.onBackPressed()
        }

        val submitButton: Button = findViewById(R.id.submit_btn)
        submitButton.setOnClickListener {

            val email = findViewById<EditText>(R.id.email_fp).text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Please enter email",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Firebase.auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("ForgotPasswordActivity", "Email sent successfully to reset your password.")
                            Toast.makeText(
                                baseContext,
                                "Email sent successfully to reset your password.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Log.w(
                                "ForgotPasswordActivity",
                                "sendPasswordResetEmail:failure",
                                task.exception
                            )
                            Toast.makeText(
                                baseContext,
                                "Password reset failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}