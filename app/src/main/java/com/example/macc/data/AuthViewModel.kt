package com.example.macc.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {

    private val repository = FirebaseAuthRepository()
    private var _userData: MutableLiveData<FirebaseUser> = MutableLiveData()
    val userData: LiveData<FirebaseUser> = _userData

    fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String,
                   password:String, trips:Map<String,Boolean>, imgAvatar: Uri, context: Context){

        viewModelScope.launch(Dispatchers.Main) {
            repository.signUpUser(name,surname,nickname,description,email,password,trips, imgAvatar, _userData, context)
        }
    }

    fun logInUser(email: String, password: String, context: Context){
        viewModelScope.launch(Dispatchers.Main) {
            repository.logInUser(email, password, _userData, context)
        }
    }
}