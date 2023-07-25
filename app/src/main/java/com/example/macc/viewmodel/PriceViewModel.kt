package com.example.macc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PriceViewModel : ViewModel() {
    val selected = MutableLiveData<String>()

    fun selectedItem(item: String) {
        selected.value = item
    }
}