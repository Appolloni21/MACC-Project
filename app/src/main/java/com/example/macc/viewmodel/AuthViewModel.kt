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

    private val _uiState: MutableLiveData<String?> = MutableLiveData()
    val uiState: LiveData<String?> = _uiState

    fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String,
                   password:String, imgAvatar: Uri){

        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.signUpUser(name,surname,nickname,description,email,password,imgAvatar)
            _uiState.postValue(state)
        }
    }

    fun logInUser(email: String, password: String){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.logInUser(email, password)
            _uiState.postValue(state)
        }
    }

    fun logOutUser(){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.logOutUser()
            _uiState.postValue(state)
        }
    }

    fun forgotPassword(email:String){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.forgotPassword(email)
            _uiState.postValue(state)
        }
    }

    fun resetUiState(){
        _uiState.value = null
    }
}