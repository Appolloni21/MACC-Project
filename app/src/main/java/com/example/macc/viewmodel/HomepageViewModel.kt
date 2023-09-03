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
import com.example.macc.utility.UIState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "HomepageViewModel"

class HomepageViewModel : ViewModel() {

    private val repository: FirebaseDatabaseRepository
    private val currentUserID = Firebase.auth.currentUser?.uid.toString()

    private val _travelArrayList: MutableLiveData<ArrayList<Travel>> = MutableLiveData()
    val travelArrayList: LiveData<ArrayList<Travel>> = _travelArrayList

    private val _travelSelected: MutableLiveData<Travel> = MutableLiveData()
    val travelSelected: LiveData<Travel> = _travelSelected

    private val _travelToDelete: MutableLiveData<Travel> = MutableLiveData()
    val travelToDelete: LiveData<Travel> = _travelToDelete

    private val _expenses: MutableLiveData<ArrayList<Expense>> = MutableLiveData()
    val expenses: LiveData<ArrayList<Expense>> = _expenses

    private val _expenseSelected: MutableLiveData<Expense> = MutableLiveData()
    val expenseSelected: LiveData<Expense> = _expenseSelected

    private val _expenseToDelete: MutableLiveData<Expense> = MutableLiveData()
    val expenseToDelete: LiveData<Expense> = _expenseToDelete

    private val _users: MutableLiveData<ArrayList<User>> = MutableLiveData()
    val users: LiveData<ArrayList<User>> = _users

    private val _userSelected: MutableLiveData<User> = MutableLiveData()
    val userSelected: LiveData<User> = _userSelected

    private val _userToRemove: MutableLiveData<User> = MutableLiveData()
    val userToRemove: LiveData<User> = _userToRemove

    private val _uiState: MutableLiveData<String?> = MutableLiveData()
    val uiState: LiveData<String?> = _uiState

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
            _uiState.postValue(state)
        }
    }

    fun deleteTravel(){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.deleteTravel(travelToDelete.value!!)
            _uiState.postValue(state)
        }
    }

    fun editTravel(travelName: String, destination: String, imgCover: Uri){
        viewModelScope.launch(Dispatchers.Main){
            val travelID = _travelSelected.value?.travelID.toString()
            val state = repository.editTravel(travelID,travelName,destination,imgCover)
            _uiState.postValue(state)
        }
    }

    fun selectTravel(travelID:String) {
        repository.getSelectedTravels(travelID, _travelSelected, _uiState)
        getExpenses(travelID)
        getUsers(travelID)
    }

    fun selectTravelToDelete(travel: Travel){
        _travelToDelete.postValue(travel)
    }

    fun quitFromTravel(){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.quitFromTravel(travelSelected.value!!)
            _uiState.postValue(state)
        }
    }

    private fun getExpenses(travelID:String){
        repository.getExpenses(travelID, _expenses)
    }

    private fun getUsers(travelID:String){
        repository.getUsers(travelID, _users)
    }

    fun selectUser(userID:String){
        repository.getSelectedUser(userID,_userSelected)
    }

    fun addUser(userEmail: String){
        viewModelScope.launch(Dispatchers.Main) {
            val travelID = _travelSelected.value?.travelID.toString()
            val state = repository.addUser(userEmail, travelID)
            _uiState.postValue(state)
        }
    }

    fun selectUserToRemoveFromTravel(user: User){
        _userToRemove.postValue(user)
    }

    fun removeUserFromTravel(){
        viewModelScope.launch(Dispatchers.Main){
            val state = repository.removeUserFromTravel(userToRemove.value!!,travelSelected.value!!)
            _uiState.postValue(state)
        }
    }
    fun checkCurrentUserInTravel(){
        if(_travelSelected.value?.members?.containsKey(currentUserID) == false){
            _uiState.postValue(UIState.WARN_104)
        }
    }

    fun resetUiState(){
        _uiState.value = null
    }

    fun selectExpense(expenseID:String){
        repository.getSelectedExpense(expenseID,_expenseSelected)
    }

    fun selectExpenseToDelete(expense: Expense){
        _expenseToDelete.postValue(expense)
    }

    fun addExpense(expenseName:String, expenseAmount: String , expenseDate: String, expensePlace:String, expenseNote: String, expenseCheck: Boolean){
        viewModelScope.launch(Dispatchers.Main) {
            val travelID = _travelSelected.value?.travelID.toString()
            val state = repository.addExpense(travelID, expenseName, expenseAmount, expenseDate, expensePlace, expenseNote, expenseCheck)
            _uiState.postValue(state)
        }
    }

    fun deleteExpense(){
        viewModelScope.launch(Dispatchers.Main) {
            val state = repository.deleteExpense(expenseToDelete.value!!)
            _uiState.postValue(state)
        }
    }

    fun editExpense(expenseName:String, expenseAmount: String , expenseDate: String, expensePlace:String, expenseNotes: String){
        viewModelScope.launch(Dispatchers.Main) {
            val expenseID = _expenseSelected.value?.expenseID.toString()
            val state = repository.editExpense(expenseID, expenseName, expenseAmount, expenseDate, expensePlace, expenseNotes)
            _uiState.postValue(state)
        }
    }

    fun checkExpenseInTravel(expenseID: String){
        if(_travelSelected.value?.expenses?.containsKey(expenseID) == false){
            _uiState.postValue(UIState.WARN_105)
        }
    }
}