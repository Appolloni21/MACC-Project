package com.example.macc.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc.model.User
import com.example.macc.repository.FirebaseAuthRepository
//import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {

    private val repository = FirebaseAuthRepository()

    private val _uiState: MutableLiveData<String?> = MutableLiveData()
    val uiState: LiveData<String?> = _uiState

    private val _userMyProfile: MutableLiveData<User?> = MutableLiveData()
    val userMyProfile: LiveData<User?> = _userMyProfile

    //private val _userID: MutableLiveData<String?> = MutableLiveData()
    //val userID: LiveData<String?> = _userID

    fun signUpUser(name:String, surname:String, nickname:String, description: String, email:String,
                   password:String, imgAvatar: Uri){

        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.signUpUser(name,surname,nickname,description,email,password,imgAvatar)
            _uiState.postValue(state)
        }
    }

    fun signUpUserWithGoogle(credential: GoogleSignInAccount){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.signUpUserWithGoogle(credential)
            _uiState.postValue(state)
        }
    }

    fun logInUser(email: String, password: String){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.logInUser(email, password)
            _uiState.postValue(state)
        }
    }

    fun logInUserWithGoogle(firebaseCredential: AuthCredential){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.logInUserWithGoogle(firebaseCredential)
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

    fun getUserMyProfile(){
        viewModelScope.launch(Dispatchers.Main){
            repository.getUserMyProfile(_userMyProfile)
        }
    }

    fun editUserMyProfile(name:String, surname:String, nickname:String, description:String, avatar: Uri){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.editUserMyProfile(name,surname,nickname,description, avatar)
            _uiState.postValue(state)
        }
    }

    fun changePasswordUser(currentPassword: String, newPassword: String){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.changePasswordUser(currentPassword, newPassword)
            _uiState.postValue(state)
        }
    }

    fun checkGoogleUser(email: String) {
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.checkGoogleUser(email)
            _uiState.postValue(state)
        }
    }

    fun resetUiState(){
        _uiState.value = null
    }
}