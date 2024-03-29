package com.example.macc.model

data class Travel(
    var name: String? = null,
    var destination: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var imgUrl: String? = null,
    var members: Map<String,Boolean> ?= null,
    var expenses: Map<String,Boolean> ?= null,
    var travelID: String? = null,
    var owner: String? = null
) {
}