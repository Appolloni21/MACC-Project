package com.example.macc.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.macc.model.Travel
import com.example.macc.repository.FirebaseDatabaseRepository

private const val TAG = "HomepageViewModel"

class HomepageViewModel : ViewModel() {

    private val repository: FirebaseDatabaseRepository

    private val _travelArrayList: MutableLiveData<ArrayList<Travel>> = MutableLiveData()
    val travelArrayList: LiveData<ArrayList<Travel>> = _travelArrayList

    private val _travelAdded: MutableLiveData<Travel> = MutableLiveData()
    val travelAdded: LiveData<Travel> = _travelAdded


    init {
        _travelArrayList.value = arrayListOf()
        repository = FirebaseDatabaseRepository().getIstance()
        repository.loadTravelsHome(_travelArrayList)
    }

    fun addTravel(travelName:String, destination:String, startDate:String, endDate:String, imgCover: Uri, context: Context?){
        repository.addTravel(travelName, destination, startDate, endDate, imgCover, _travelAdded, context)
    }

}