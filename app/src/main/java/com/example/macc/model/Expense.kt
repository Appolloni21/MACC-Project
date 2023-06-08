package com.example.macc.model

import java.util.Date

data class Expense(
    //TODO: inserire altri campi
    var name: String? = null,
    var amount: Int?= null,
    var place: String?= null,
    var date: Date?= null,
    var type: String?= null,        //Tipo: se è di gruppo o personale
    var owner: String?= null,       //Se è una spesa personale l'owner sarà un utente, se di gruppo il campo va lasciato null
    var travelID: String?= null
) {
}