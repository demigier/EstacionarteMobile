package com.ort.estacionarte.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.adapters.SingleMsg
import com.ort.estacionarte.entities.Reservation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ReservationsViewModel : ViewModel() {
    private var db = Firebase.firestore

    //private val auth = Firebase.auth
    private val RESERVATIONS_COL = "Reservations"
    private val SPOTS_COL = "ParkingSpots"
    private val PARKINGS_COL = "ParkingUsers"
    private val VEHICLES_COL = "Vehicles"

    var currentReservation: MutableLiveData<Reservation?> = MutableLiveData(null)
    //var currentReservationID: String? = null

    var reservationsList: MutableLiveData<MutableList<Reservation?>> = MutableLiveData(mutableListOf())

    //"ParkingName", "ParkingAddress","ParkingPhoneNumber", "VehicleLicensePlate"
    var currentReservationExtraData: MutableMap<String, String> = mutableMapOf(
        "ParkingName" to "-",
        "ParkingAddress" to "-",
        "ParkingPhoneNumber" to "-",
        "VehicleLicensePlate" to "-",
    )

    var msgToProfFrag = MutableLiveData<SingleMsg>()
    var msgToParkDetFrag = MutableLiveData<SingleMsg>()

    fun getCurrentReservation(userID: String) {
        var hasReservations = false
        var docs = db.collection("Reservations")
            .limit(1)
            .whereEqualTo("userID", userID)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { docs ->
                for (reserv in docs) {
                    currentReservation.value = reserv.toObject()
                    currentReservation.value!!.uid = reserv.id
                }
                if (currentReservation.value != null) {
                    /*getReservationExtraData(
                        currentReservation.value!!.parkingID,
                        currentReservation.value!!.vehicleID
                    )*/
                }
            }
            .addOnFailureListener { e ->
                sendMsgToFront(msgToProfFrag, SingleMsg("Error al intentar traer la reserva"))
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun makeReservation(userID: String, parkingID: String, vehicleID: String) {
        //0) Previo a realizar la reserva hay que chequear que el ususarion no tenga una reserva vigente.
        //1) Despues, hay que obtener un spot del estacionamiento.
        //2) Pasar el spot a no disponible y
        //3) generar la reserva con el userID, parkingID, y spotID.
        //Todo_ esto debería hacerce en una transacción,
        //ver: https://firebase.google.com/docs/firestore/manage-data/transactions

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!hasReservations(userID)) {
                    val spotID = getAvailableSpot(parkingID)

                    if (spotID != null) {
                        // Paso el spot a no disponible.
                        db.collection(SPOTS_COL)
                            .document(spotID)
                            .update(
                                mapOf(
                                    "available" to false,
                                )
                            )
                            .await()

                        // Creo la reserva en la base.
                        /*val res = Reservation(
                            true,
                            userID,
                            parkingID,
                            vehicleID,
                            spotID,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                            null,
                            null,
                            null
                        )*/

                        val res = mapOf(
                            "active" to true,
                            "userID" to userID,
                            "parkingID" to parkingID,
                            "vehicleID" to vehicleID,
                            "parkingSpotID" to spotID,  //Acá antes decía spotID en lugar de parkingSpotID
                            "reservationDate" to  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                            "userArrivedDate" to null,
                            "userLeftDate" to null,
                            "cancelationDate" to null
                        )

                        val doc = db.collection(RESERVATIONS_COL)
                            .document()

                        doc.set(res)
                            .await()

                        //currentReservation.value!!.uid = doc.id

                        Log.d(
                            "TEST: ReservationsVM -> makeReservation($userID, $parkingID, $vehicleID): ",
                            "Reserva creada"
                        )
                        // guardar reserva, en currentReservation y generar snapshot
                        // https://firebase.google.com/docs/firestore/query-data/listen
                        //currentReservation.postValue(res)
                        //getReservationExtraData(res.parkingID, res.vehicleID)
                        //generarSnapshot(res.id)
                        sendMsgToFront(
                            msgToParkDetFrag,
                            SingleMsg("Reserva realizada exitosamente")
                        )
                    } else {
                        Log.d(
                            "TEST: ReservationsVM -> makeReservation($userID, $parkingID, $vehicleID): ",
                            "No hay spots disponibles."
                        )
                        sendMsgToFront(msgToParkDetFrag, SingleMsg("No hay spots disponibles"))
                    }
                } else {
                    Log.d(
                        "TEST: ReservationsVM -> makeReservation($userID, $parkingID, $vehicleID): ",
                        "Ya hay una reserva en curso."
                    )
                    sendMsgToFront(msgToParkDetFrag, SingleMsg("Ya tienes una reserva activa"))
                }
            } catch (e: Exception) {
                Log.d(
                    "TEST: ReservationsVM -> makeReservation($userID, $parkingID, $vehicleID): e ",
                    e.message.toString()
                )
                sendMsgToFront(msgToParkDetFrag, SingleMsg(e.message.toString()))
            }
        }
    }

    fun cancelCurrentReservation() {
        //Esto debería implementarse en una transacción
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentReservation.value != null && currentReservation.value!!.uid != null) {
                    db.collection(RESERVATIONS_COL)
                        .document(currentReservation.value!!.uid)
                        .update(
                            mapOf(
                                "active" to false,
                                "cancelationDate" to SimpleDateFormat("dd-MM-yyyy hh:mm").format(
                                    Calendar.getInstance().time
                                )
                            )
                        ).await()

                    db.collection(SPOTS_COL)
                        .document(currentReservation.value!!.parkingSpotID)
                        .update(
                            mapOf(
                                "available" to true,
                            )
                        )
                        .await()

                    currentReservation.postValue(null)
                    currentReservation.value!!.uid = ""
                    currentReservationExtraData = hashMapOf()

                    Log.d(
                        "TEST: ReservationsVM -> cancelCurrentReservation(): ",
                        "Reserva cancelada"
                    )
                    sendMsgToFront(msgToProfFrag, SingleMsg("La reserva fue cancelada"))
                }
            } catch (e: Exception) {
                Log.d(
                    "TEST: ReservationsVM -> cancelCurrentReservation(): ",
                    "Error al cancelar: ${e.message}"
                )
                sendMsgToFront(
                    msgToProfFrag, SingleMsg("Error, no fue posible cancelar la reserva")
                )
            }
        }
    }

    private suspend fun getAvailableSpot(parkingID: String): String? {
        var spotID: String? = null

        val docs = db.collection(SPOTS_COL)
            .limit(1)
            .whereEqualTo("parkingID", parkingID)
            .whereEqualTo("available", true)
            .whereEqualTo("active", true)
            .get()
            .await()

        if (docs != null) {
            for (spot in docs) {
                spotID = spot.id
            }
        }

        return spotID
    }

    private suspend fun hasReservations(userId: String): Boolean {
        var hasReservations = false

        var docs = db.collection(RESERVATIONS_COL)
            .limit(1)
            .whereEqualTo("userID", userId)
            .whereEqualTo("active", true)
            .get()
            .await()

        if (docs.size() > 0) {
            hasReservations = true

            for (d in docs) {
                //Log.d("TEST: ReservationsVM -> hasReservations($userId)", d.toString())
            }
            Log.d("TEST: ReservationsVM -> hasReservations($userId): ", hasReservations.toString())
        }
        return hasReservations
    }

    private suspend fun getReservationParking(parkingID: String/*, vehicleID: String*/, index: Int, list: MutableList<Reservation?>) {
        try {
            var it = db.collection(PARKINGS_COL).document(parkingID).get()
                .await()
                    list[index]!!.parking = it.toObject()!!
                    list[index]!!.parking.uid = it.id
                    Log.d("ReservationsTest", it.data.toString())
                    //currentReservation.value!!.parking = it.toObject()!!

            /*Log.d("ReservationsTest3", vehicleID)
            var it2 = db.collection(VEHICLES_COL).document(vehicleID).get()
                .await()
                    Log.d("ReservationsTest2", it2.data.toString())
                    list[index]!!.vehicle = it2.toObject()!!
                    list[index]!!.vehicle.uid = it2.id*/
                    //currentReservation.value!!.vehicle = it.toObject()!!

        } catch (e: Exception) {
            Log.d(
                "TEST: ReservationsVM -> getReservationExtraData(): ",
                "Error al traer los datos extra: ${e.message}"
            )
            sendMsgToFront(
                msgToProfFrag, SingleMsg("Error al traer los datos extra")
            )
        }
    }

    private suspend fun getReservationVehicle(/*parkingID: String, */vehicleID: String, index: Int, list: MutableList<Reservation?>) {
        try {
           /* var it = db.collection(PARKINGS_COL).document(parkingID).get()
                .await()
            list[index]!!.parking = it.toObject()!!
            list[index]!!.parking.uid = it.id
            Log.d("ReservationsTest", it.data.toString())*/
            //currentReservation.value!!.parking = it.toObject()!!

            Log.d("ReservationsTest3", vehicleID)
            var it2 = db.collection(VEHICLES_COL).document(vehicleID).get()
                .await()
            Log.d("ReservationsTest2", it2.data.toString())
            list[index]!!.vehicle = it2.toObject()!!
            list[index]!!.vehicle.uid = it2.id
            //currentReservation.value!!.vehicle = it.toObject()!!

        } catch (e: Exception) {
            Log.d(
                "TEST: ReservationsVM -> getReservationExtraData(): ",
                "Error al traer los datos extra: ${e.message}"
            )
            sendMsgToFront(
                msgToProfFrag, SingleMsg("Error al traer los datos extra")
            )
        }
    }

    fun getAllReservations(userID: String) {
        viewModelScope.launch {
            var list: MutableList<Reservation?> = mutableListOf()
            try {
                Log.d("ReservationsTest",userID)
                reservationsList.value!!.clear()
                var docs = db.collection("Reservations")
                    .whereEqualTo("userID", userID)
                    .orderBy("active", Query.Direction.DESCENDING)
                    .get()
                    .await()
                        for (reserv in docs) {
                            list.add(reserv.toObject())
                            list[list.size - 1]!!.uid = reserv.id
                            Log.d("ReservationID", reserv.id)
                            getReservationParking(list[list.size - 1]!!.parkingID, list.size - 1, list)
                            getReservationVehicle(list[list.size - 1]!!.vehicleID, list.size - 1, list)
                            if(list[list.size - 1]!!.active == true){
                                currentReservation.value = list[list.size - 1]
                            }
                        }
                reservationsList.postValue(list)
            }
            catch (e: java.lang.Exception){
                sendMsgToFront(msgToProfFrag, SingleMsg("Error al intentar traer la reserva"))
            }
        }

    }

    private fun sendMsgToFront(mutableLiveData: MutableLiveData<SingleMsg>, smsg: SingleMsg) {
        mutableLiveData.postValue(smsg)
    }
}