package com.aman.chatapp.models



class user {
    var name: String ?= null
    var email: String ?= null
    var uid: String ?= null
    var image:String?=null

    constructor(){}
    constructor(name: String?, email: String?, uid:String? ){
        this.name = name
        this.email = email
        this.uid = uid
        this.image= null
    }
    constructor( uid:String?, name: String?, email: String?,image:String?){
        this.name = name
        this.email = email
        this.uid = uid
        this.image= image
    }

}