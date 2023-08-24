package com.example.macc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PriceViewModel : ViewModel() {
    private val _selected: MutableLiveData<String> = MutableLiveData()
    val selected: LiveData<String> = _selected

    fun selectedItem(item: String) {
        _selected.postValue(item)
    }
}