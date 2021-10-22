package com.ort.estacionarte.entities

class Parking(var adress: String, var location: HashMap<String,String>, var name: String, var phoneNumber: String) {
    constructor() : this("", hashMapOf(),"","")

    lateinit var uid: String
}