package com.example.macc.model


data class Expense(
    var name: String? = null,
    var amount: String?= null,
    var date: String?= null,
    var place: String?= null,
    var owner: String?= null,       //Se è una spesa personale l'owner sarà un utente, se di gruppo il campo è uguale a "group"
    var notes: String?= null,
    var travelID: String?= null,
    var expenseID: String?=null
) {
}