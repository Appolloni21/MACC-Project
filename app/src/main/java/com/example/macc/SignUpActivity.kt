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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.macc.databinding.SignupPageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel



private const val TAG: String = "SignUp Activity"

class SignUpActivity : AppCompatActivity() {

    private lateinit var imageAvatarURI: Uri
    private val sharedViewModel: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        val binding = SignupPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back navigation
        val backIcon: ImageView = binding.backIcon
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d(TAG, "Selected URI: $uri")
                imageAvatarURI = uri
                binding.avatarImg.setImageURI(uri)
            } else {
                Log.d(TAG, "No media selected")
            }
        }

        val chooseAvatarButton = binding.chooseAvatarButton
        chooseAvatarButton.setOnClickListener{
            // Launch the photo picker and allow the user to choose only images.
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        val signupButton: Button = binding.signupBtn
        signupButton.setOnClickListener {

            val name: String = binding.name.toString().trim { it <= ' ' }
            val surname: String = binding.surname.toString().trim { it <= ' ' }
            val nickname: String = binding.nickname.text.toString().trim { it <= ' ' }
            val description = ""
            val email: String = binding.email.text.toString().trim { it <= ' ' }
            val password: String = binding.password.text.toString().trim { it <= ' ' }
            //val trips:Map<String,Boolean> = mapOf("null" to false)


            when {
                TextUtils.isEmpty(name) -> {
                    makeToast("Please enter name")
                }

                TextUtils.isEmpty(surname) -> {
                    makeToast("Please enter surname")
                }

                TextUtils.isEmpty(nickname) -> {
                    makeToast("Please enter nickname")
                }

                TextUtils.isEmpty(email) -> {
                    makeToast("Please enter email")
                }

                TextUtils.isEmpty(password) -> {
                    makeToast("Please enter password")
                }

                !passwordsAreEquals(this@SignUpActivity) -> {
                    makeToast("Passwords are not equals")
                }

                else -> {
                    sharedViewModel.signUpUser(name, surname, nickname, description, email, password, imageAvatarURI)
                }
            }
        }

        sharedViewModel.uiState.observe(this){
            when (it){
                UIState.SUCCESS -> {
                    //User is registered and so logged in, we send him to the homepage
                    Toast.makeText(this@SignUpActivity, "You signed up successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //intent.putExtra("userID", sharedViewModel.userID.value)
                    startActivity(intent)
                    finish()
                    sharedViewModel.resetUiState()
                }
                UIState.FAILURE -> {
                    Toast.makeText(this@SignUpActivity, "Error, couldn't sign up", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }
    }


    private fun makeToast(msg:String){
        Toast.makeText(
            this@SignUpActivity,
            msg,
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun passwordsAreEquals (registrationActivity: SignUpActivity): Boolean{
    return registrationActivity.findViewById<EditText>(R.id.password).text.toString() == registrationActivity.findViewById<EditText>(R.id.repeat_password).text.toString()
}


