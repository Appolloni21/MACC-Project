package com.example.macc.model

data class User(
    var name:String?=null,
    var surname:String?=null,
    var nickname:String?=null,
    var description:String?=null,
    var email:String?=null,
    var avatar:String?=null,
    var trips: Map<String,Boolean> ?= null
) {
}