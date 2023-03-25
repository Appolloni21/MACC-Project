package com.example.macc.model


data class Travel(
    //var tid: String? = null,
    var name: String? = null,
    var destination: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var imgUrl: String? = null,
    var members: Map<String,Boolean> ?= null
) {
}