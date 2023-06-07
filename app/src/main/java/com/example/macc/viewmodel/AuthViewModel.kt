package com.example.macc.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc.repository.FirebaseAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {

    private val repository = FirebaseAuthRepository()

    private var _signUpState: MutableLiveData<String> = MutableLiveData()
    val signUpState: LiveData<String> = _signUpState

    private var _logInState: MutableLiveData<String> = MutableLiveData()
    val logInState: LiveData<String> = _logInState

    private var _logOutState: MutableLiveData<String> = MutableLiveData()
    val logOutState: LiveData<String> = _logOutState

    private var _forgotPswState: MutableLiveData<String> = MutableLiveData()
    val forgotPswState: LiveData<String> = _forgotPswState

    fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String,
                   password:String, trips:Map<String,Boolean>, imgAvatar: Uri){

        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.signUpUser(name,surname,nickname,description,email,password,trips, imgAvatar)
            _signUpState.postValue(state)
        }
    }

    fun logInUser(email: String, password: String){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.logInUser(email, password)
            _logInState.postValue(state)
        }
    }

    fun logOutUser(){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.logOutUser()
            _logOutState.postValue(state)
        }
    }

    fun forgotPassword(email:String){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.forgotPassword(email)
            _forgotPswState.postValue(state)
        }
    }
}