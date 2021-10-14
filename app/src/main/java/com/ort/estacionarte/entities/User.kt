package com.ort.estacionarte.entitiescountry

import android.os.Parcel
import android.os.Parcelable

/*public class User(var email: String, var name: String, var lastName: String, var Location: MutableList<String>, var phoneNumber: String, var vehicles: MutableList<String>) {
    constructor() : this("","","", mutableListOf(),"",mutableListOf())
    
}*/

class User(var email: String, var name: String, var lastName: String, var phoneNumber: String) {
    constructor() : this("","","","")

    lateinit var uid: String
    var active: Boolean = true
    //lateinit var location: HashMap<String,String>
}