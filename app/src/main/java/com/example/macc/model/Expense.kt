package com.example.macc.model


data class Expense(
    var name: String? = null,
    var amount: Int?= null,
    var place: String?= null,
    var date: String?= null,
    var type: String?= null,        //Tipo: se è di gruppo o personale
    var owner: String?= null,       //Se è una spesa personale l'owner sarà un utente, se di gruppo il campo va lasciato null
    var travelID: String?= null,
    var expenseID: String?=null
) {
}