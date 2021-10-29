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

    var userAct: User? = null
    var parkingAct = MutableLiveData<Parking>()
    var availableSpots = MutableLiveData<MutableList<String>>()
    var parkingList = MutableLiveData<MutableList<Parking>>()
    var toastMessage = MutableLiveData<kotlin.String>()
    //Variables para corrutinas
    //private val parentJob = Job()
    //val scope = CoroutineScope(Dispatchers.Default + parentJob)

    init {
        //Recupero el Usuario del SharedPreferences, para utilizarlo al momento de realizar la reserva.

    }

    //Métodos
    @SuppressLint("NewApi")
    fun makeReservation(userId: String, parkingUid: String) {
        //0) En algún momento hay que chequear que el ususarion no tenga una reserva vigente.
        //1) Obtener un spot del estacionamiento: Uso la lista ya obtenida o la armo nuevamente?
        //2) Pasar el spot a no disponible y generar la reserva con el userID, parkingID, y spotID
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var reserved = hasReservations(userId)
                if(reserved == false){
                    var spots = getParkingSpots(parkingAct.value!!.uid)
                    var vehiclesList = getFirebaseUserVehicles(userId)

                    db.collection("Reservations")
                        .document()
                        .set(mapOf(
                        "parkingID" to parkingUid,
                        "parkingSpotID" to spots!![0],
                        "userID" to userId,
                        "vehicleID" to vehiclesList[0].uid,
                        "reservationDate" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                        "userArrivedDate" to null,
                        "userLeftDate" to null,
                        "cancelationDate" to null,
                        "active" to true))
                        .await()

                    Log.d("ParkingDetailsVM", "Reserva creada")
                    db.collection("ParkingSpots")
                        .document(spots[0])
                        .update(mapOf(
                            "available" to false,
                            "parkingID" to parkingUid
                        ))
                        .await()

                    sendToast("Reserva realizada exitosamente")
                }

                //var spot=reserveSpot(spots)
            }catch (e: Exception){
                Log.d("ParkingDetailsVM", e.message.toString())
            }
        }

        // Pasando el slot a no disponible... => update
        //db.

    }

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
        //A definir: Buscar varios? o buscar uno solo y guardarlo?
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
            //parkingSpots.value = mutableListOf<Spot>()
            for (spot in docs) {
                spotsAux.add(spot.id)
            }

            Log.d("Test: ParkingDetailsVM", "spots libres:${spotsAux.size}")

        } else {
            Log.d("Test: ParkingDetailsVM", "No hay lugares libres.")
        }
        return spotsAux
    }

    suspend fun getFirebaseUserData(searchedID: kotlin.String): User? {
        return db.collection("Users").document(searchedID).get().await().toObject()
    }

    private fun sendToast(msg: String) {
        //toastMessage.value = msg ?: ""
        toastMessage.postValue(msg ?: "")
    }

    private suspend fun getFirebaseUserVehicles(searchedID: String): MutableList<Vehicle> {
        var vehiclesList = mutableListOf<Vehicle>()

        var docs = db.collection("Vehicles")
            .whereEqualTo("userID", searchedID)
            .get()
            .await()

        if (docs != null) {
            for (vehicle in docs) {
                var vAux: Vehicle = vehicle.toObject()
                vAux.uid = vehicle.id

                vehiclesList.add(vAux)
            }
        } else {
            Log.d("VehicleTest", "No tiene vehiculos")
        }
        return vehiclesList
    }

    private suspend fun hasReservations(userId: String): Boolean? {
        var hasReservations = false
        var docs = db.collection("Reservations")
            .limit(1)
            .whereEqualTo("userID", userId)
            .whereEqualTo("active", true)
            .get()
            .await()


        if (docs.size() > 0) {
            hasReservations = true
            Log.d("Test: ParkingDetailsVM", "has reservations")
            sendToast("Ya tienes una reserva activa")
            for (d in docs){
                Log.d("ParkingDetailsVM", d.toString())
            }
        }

        return hasReservations
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
                        /*pAux = parking.toObject()
                        pAux.uid = parking.id*/
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

