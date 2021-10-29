package com.ort.estacionarte.entities

class Parking(
    var uid: String,
    var parkingName: String,
    var address: String,
    var location: HashMap<String, Double>,
    var phoneNumber: String,
    var cuit: String
) {
    constructor() : this("", "", "", hashMapOf(), "","")
}