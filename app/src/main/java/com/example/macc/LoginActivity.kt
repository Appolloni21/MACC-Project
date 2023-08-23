package com.example.macc

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.databinding.LoginPageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.GoogleAuthProvider

private const val TAG = "Login Activity"
class LoginActivity : AppCompatActivity(){

    private val sharedViewModel: AuthViewModel by viewModels()

    //Login with Google
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true




    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        val binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton: Button = binding.loginBtn
        loginButton.setOnClickListener {

            val email: String = binding.emailText.text.toString().trim { it <= ' ' }
            val password: String = binding.passwordText.text.toString().trim { it <= ' ' }

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

        sharedViewModel.uiState.observe(this){
            when(it){
                UIState.SUCCESS -> {
                    //User is logged in, we send him to the homepage
                    Toast.makeText(this@LoginActivity, "You logged in, welcome back", Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //intent.putExtra("userID", sharedViewModel.userID.value)
                    startActivity(intent)
                    finish()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    Toast.makeText(this@LoginActivity, "Error in logging in", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }

        val signupButton:Button = binding.signUpBtn
        signupButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        val forgotPswTv: TextView = binding.forgotPassword
        forgotPswTv.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        val signInButtonGoogle: SignInButton = binding.signInButtonGoogle
        signInButtonGoogle.setOnClickListener {
            //sharedViewModel.logInUserWithGoogle()
            Log.d(TAG,"logo google tap")
            //test()
            oneTapClient = Identity.getSignInClient(this)
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.web_client_id2))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
                .build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    // No saved credentials found. Launch the One Tap sign-up flow, or
                    // do nothing and continue presenting the signed-out UI.
                    Log.d(TAG, "One Tap UI Failure: " + e.localizedMessage)
                }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            Log.d(TAG, "Got ID token.")

                            //Check se l'utente ha già usato in precedenza l'account google per accedere
                            sharedViewModel.checkGoogleUser(credential.id)

                            sharedViewModel.uiState.observe(this){
                                when(it){
                                    UIState.WARN_101 -> {
                                        Log.d(TAG, "Google user already exist, we just need to log in it")
                                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                        sharedViewModel.logInUserWithGoogle(firebaseCredential)
                                    }
                                    UIState.WARN_102 -> {
                                        Log.d(TAG, "Google user doesn't exist in the DB, we have to create it")
                                        sharedViewModel.signUpUserWithGoogle(credential)

                                    }
                                    UIState.WARN_103 ->{
                                        Log.d(TAG,"User already exist")
                                        Toast.makeText(this@LoginActivity, "Error in logging in using Google, email already used with normal password", Toast.LENGTH_SHORT).show()
                                        sharedViewModel.resetUiState()
                                    }
                                }
                            }
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d(TAG, "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
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

