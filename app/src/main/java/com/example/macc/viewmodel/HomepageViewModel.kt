package com.example.macc.viewmodel


import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc.model.Expense
import com.example.macc.model.Travel
import com.example.macc.model.User
import com.example.macc.repository.FirebaseDatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "HomepageViewModel"

class HomepageViewModel : ViewModel() {

    private val repository: FirebaseDatabaseRepository

    private val _travelArrayList: MutableLiveData<ArrayList<Travel>> = MutableLiveData()
    val travelArrayList: LiveData<ArrayList<Travel>> = _travelArrayList

    private val _travelAdded: MutableLiveData<String> = MutableLiveData()
    val travelAdded: LiveData<String> = _travelAdded

    private val _expenses: MutableLiveData<ArrayList<Expense>> = MutableLiveData()
    val expenses: LiveData<ArrayList<Expense>> = _expenses

    private val _users: MutableLiveData<ArrayList<User>> = MutableLiveData()
    val users: LiveData<ArrayList<User>> = _users

    private val _userAdded: MutableLiveData<String> = MutableLiveData()
    val userAdded: LiveData<String> = _userAdded

    init {
        _travelArrayList.value = arrayListOf()
        repository = FirebaseDatabaseRepository().getIstance()
        repository.getTravels(_travelArrayList)
        repository.getUsers("", _users)
        Log.d(TAG, "init HomePageViewModel")
    }

    fun addTravel(travelName:String, destination:String, startDate:String, endDate:String, imgCover: Uri){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.addTravel(travelName, destination, startDate, endDate, imgCover)
            _travelAdded.postValue(state)
        }
    }

    fun deleteTravel(travel: Travel){
        viewModelScope.launch(Dispatchers.Main){
            repository.deleteTravel(travel)
        }
    }

    fun getExpenses(travelID: String){
        repository.getExpenses(travelID, _expenses)
    }

    fun getUsers(travelID: String){
        repository.getUsers(travelID, _users)
    }

    fun addUser(userEmail: String, travelID: String){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.addUser(userEmail, travelID)
            _userAdded.postValue(state)
        }
    }

    //Temp
    /*fun addExpense(){
        repository.addExpense()
    }*/
}