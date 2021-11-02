package com.ort.estacionarte.entities

import java.util.*

class Reservation(
    var active: Boolean,
    var userID: String,
    var parkingID: String,
    var vehicleID: String,
    var parkingSpotID: String,
    var reservationDate: String,
    var userArrivedDate: String?,
    var userLeftDate: String?,
    var cancelationDate: String?,
) {
    constructor() : this(
        false, "", "", "", "", "", null, null, null
    )

    lateinit var uid: String
    lateinit var parking: Parking
    lateinit var vehicle: Vehicle
}