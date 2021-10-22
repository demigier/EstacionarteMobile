package com.ort.estacionarte.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.entities.Parking
import com.ort.estacionarte.entitiescountry.User

class ParkingViewModel : ViewModel() {
    private var db = Firebase.firestore
    var userActive: User? = null
    var parkingList: MutableList<Parking>? = null
    var parkingActual: Parking? = null

    public fun getFirebaseUserData(searchedID: String){
        var usuarioEncontrado = db.collection("Users").document(searchedID)
        usuarioEncontrado.get()
            .addOnSuccessListener { usuarioEncontrado ->
                if (usuarioEncontrado != null) {
                    Log.d("MapTest", usuarioEncontrado.toString())
                    userActive = usuarioEncontrado.toObject()!!
                } else {
                    Log.d("LoginTest", "Usuario no encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Test", "get failed with ", exception)
            }
    }

    public fun getFirebaseParkingsByCoords(lat: String, long: String){
        var parkingEncontrados = db.collection("ParkingUsers").limit(1).whereEqualTo("location.lat", lat).whereEqualTo("location.long", long)
        parkingEncontrados.get()
            .addOnSuccessListener { parkingFounded ->
                if (parkingFounded != null) {
                    for(parking in parkingFounded){
                        Log.d("ParkingTest", parking.toString())
                        parkingActual = parking.toObject()
                        parkingActual!!.uid = parking.id
                    }
                } else {
                    Log.d("ParkingTest", "No existe ese parking")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ParkingTest", "get failed with ", exception)
            }
    }


    /*public fun getFirebaseParkings(){
        var i = 0
        var parkingEncontrados = db.collection("ParkingUsers").limit(10)
        parkingEncontrados.get()
            .addOnSuccessListener { parkingFounded ->
                if (parkingFounded != null) {
                    for(parking in parkingFounded){
                        Log.d("VehicleTest", parking.toString())
                        parkingList?.add(parking.toObject())
                        parkingList!![i].uid = parking.id
                        i = i + 1
                    }
                } else {
                    Log.d("VehicleTest", "No tiene vehiculos")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("VehicleTest", "get failed with ", exception)
            }
    }*/
    // TODO: Implement ParkingViewModel
}