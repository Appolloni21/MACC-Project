package com.example.macc.model


data class Travel(
    var tid: String? = null,
    var name: String? = null,
    var destination: String? = null,
    var start_date: String? = null,
    var end_date: String? = null,
    var img_url: String? = null,
    var members: Map<String,Boolean> ?= null
) {
}