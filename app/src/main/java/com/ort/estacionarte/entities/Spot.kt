package com.ort.estacionarte.entities

class Spot(var available: Boolean, var idParking: String, var spotName: String) {
    constructor() : this(true, "", "")

    lateinit var uid: String
}