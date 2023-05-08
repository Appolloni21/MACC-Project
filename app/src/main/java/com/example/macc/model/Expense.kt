package com.example.macc.model

data class Expense(
    //TODO: inserire altri campi
    var name: String? = null,
    var amount: Int?= null,
    var place: String?= null,
    var travelID: String?= null
) {
}