package com.ort.estacionarte.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.entities.Parking
import com.ort.estacionarte.entities.Vehicle
import com.ort.estacionarte.entitiescountry.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ParkingDetailsViewModel : ViewModel() {
    private val db = Firebase.firestore

    var parkingAct = MutableLiveData<Parking>()
    var availableSpots = MutableLiveData<MutableList<String>>()
    var parkingList = MutableLiveData<MutableList<Parking>>()

    fun getParkingInfo(lat: Double, long: Double) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var parking = getParkingByLocation(lat, long)
                parkingAct.postValue(parking!!)
                Log.d("ParkingDetailsVM", parking.parkingName.toString())

                var spots = getParkingSpots(parking.uid)

                availableSpots.postValue(spots!!)

                Log.d("Test: ParkingDetailsVM", "spots libres:${spots.size}")
            } catch (e: Exception) {
                e.message?.let { Log.d("Test: ParkingDetailsVM", it) }
            }

        }
    }

    suspend fun getParkingByLocation(lat: Double, long: Double): Parking? {
        var pAux: Parking? = null

        var docs = db.collection("ParkingUsers")
            .whereEqualTo("location.lat", lat)
            .whereEqualTo("location.long", long)
            .limit(1)
            .get()
            .await()

        if (docs != null) {
            for (parking in docs) {
                pAux = parking.toObject()
                pAux.uid = parking.id
            }

        } else {
            Log.d("Test: ParkingDetailsVM", "No existe un Estacionameinto en esa localización.")
        }
        return pAux
    }

    suspend fun getParkingSpots(parkingUid: kotlin.String?): MutableList<String>? {
        var spotsAux = mutableListOf<String>()

        Log.d("Test: ParkingDetailsVM", "parkingID:$parkingUid")

        var docs = db.collection("ParkingSpots")
            .limit(10)
            .whereEqualTo("parkingID", parkingUid)
            .whereEqualTo("available", true)
            .whereEqualTo("active", true)
            .get()
            .await()

        if (docs != null) {
            for (spot in docs) {
                spotsAux.add(spot.id)
            }

            Log.d("Test: ParkingDetailsVM", "spots libres:${spotsAux.size}")

        } else {
            Log.d("Test: ParkingDetailsVM", "No hay lugares libres.")
        }
        return spotsAux
    }

    fun getParkings() {
        viewModelScope.launch(Dispatchers.IO) {
            try{
                var pList: MutableList<Parking> = mutableListOf()

                var docs = db.collection("ParkingUsers")
                    .get()
                    .await()
                var i = 0
                if (docs != null) {
                    for (parking in docs) {
                        pList.add(parking.toObject())
                        pList[i].uid = parking.id
                        i++
                    }
                    parkingList.postValue(pList)
                } else {
                    Log.d("Test: ParkingDetailsVM", "No existe un Estacionameinto en esa localización.")
                }
            }catch (e: Exception){
                Log.d("ParkingDetailsVM", e.message.toString())
            }
        }
    }

}

