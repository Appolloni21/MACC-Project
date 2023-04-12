package com.example.macc.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.macc.model.Travel
import com.example.macc.repository.FirebaseDatabaseRepository

private const val TAG = "HomepageViewModel"

class HomepageViewModel : ViewModel() {

    private val repository: FirebaseDatabaseRepository
    //private val repository = FirebaseDatabaseRepository()

    private val _travelArrayList: MutableLiveData<ArrayList<Travel>> = MutableLiveData()
    val travelArrayList: LiveData<ArrayList<Travel>> = _travelArrayList


    init {
        _travelArrayList.value = arrayListOf()
        repository = FirebaseDatabaseRepository().getIstance()
        repository.loadTravelsHome(_travelArrayList)
    }

    fun addTravel(travelName:String, destination:String, startDate:String, endDate:String, imgCover: Uri){
        repository.addTravel(travelName, destination, startDate, endDate, imgCover)
    }

}