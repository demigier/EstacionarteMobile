package com.ort.estacionarte.entities

import android.os.Parcel
import android.os.Parcelable
import kotlin.String

class Vehicle(var model: String, var brand: String, var licensePlate: String, var userID: String): Parcelable {
    constructor() : this("","","","")

    lateinit var uid: String

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(model)
        parcel.writeString(brand)
        parcel.writeString(licensePlate)
        parcel.writeString(userID)
        parcel.writeString(uid)
    }

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        override fun createFromParcel(parcel: Parcel): Vehicle {
            return Vehicle(parcel)
        }

        override fun newArray(size: Int): Array<Vehicle?> {
            return arrayOfNulls(size)
        }
    }
}
