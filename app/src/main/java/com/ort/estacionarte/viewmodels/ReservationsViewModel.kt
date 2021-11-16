package com.ort.estacionarte.viewmodels

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.R
import com.ort.estacionarte.activities.HomeActivity
import com.ort.estacionarte.adapters.SingleMsg
import com.ort.estacionarte.entities.ReservState
import com.ort.estacionarte.entities.Reservation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class ReservationsViewModel : ViewModel() {
    private var db = Firebase.firestore

    //private val auth = Firebase.auth
    private val RESERVATIONS_COL = "Reservations"
    private val SPOTS_COL = "ParkingSpots"
    private val PARKINGS_COL = "ParkingUsers"
    private val VEHICLES_COL = "Vehicles"

    var currentReservation: MutableLiveData<Reservation?> = MutableLiveData(null)
    var reservationsList: MutableLiveData<MutableList<Reservation>> =
        MutableLiveData(mutableListOf())
    var reservationState: MutableLiveData<ReservState> = MutableLiveData()
    lateinit var reservSnapshot: DocumentReference

    var msgToProfFrag = MutableLiveData<SingleMsg>()
    var msgToParkDetFrag = MutableLiveData<SingleMsg>()

    fun getAllReservations(userID: String) {
        viewModelScope.launch {
            var list: MutableList<Reservation> = mutableListOf()

            try {
                val query = db.collection(RESERVATIONS_COL)
                    .whereEqualTo("userID", userID)
                    .orderBy("active", Query.Direction.DESCENDING)
                    .get()
                    .await()

                for (reservation in query) {
                    list.add(reservation.toObject())
                    list.last().uid = reservation.id
                    completeParkingExtraData(list.last().parkingID, list.last())
                    completeVehicleExtraData(list.last().vehicleID, list.last())
                }
                reservationsList.postValue(list)

                if (list.isNotEmpty()) {
                    if (list[0].active) {
                        currentReservation.postValue(list[0])
                        reservationState.postValue(ReservState.PENDING)
                        createSnapshot(list[0].uid)
                    } else {
                        currentReservation.postValue(null)
                    }
                }

            } catch (e: java.lang.Exception) {
                sendMsgToFront(msgToProfFrag, SingleMsg("Error al intentar traer las reservas"))
            }
        }
    }

    private fun createSnapshot(reservationID: String) {
        reservSnapshot = db.collection(RESERVATIONS_COL).document(reservationID)
        reservSnapshot.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.d("Test", "Listen failed.", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val res: Reservation? = snapshot.toObject()

                if (res != null && !res.active) {
                    var auxList: MutableList<Reservation> = reservationsList.value!!
                    auxList[0].active = false

                    if (res.cancelationDate != null) {
                        auxList[0].cancelationDate = res.cancelationDate
                        reservationState.postValue(ReservState.CANCELED)

                    } else if (res.userLeftDate != null) {
                        auxList[0].userLeftDate = res.userLeftDate
                        reservationState.postValue(ReservState.FINALIZED)

                    }
                    reservationsList.postValue(auxList)

                    Log.d("Test", "DocumentSnapshot: ${auxList[0]}")
                }
            } else {
                Log.d("Test", "Current data: null")
            }
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

                        val res = mapOf(
                            "active" to true,
                            "userID" to userID,
                            "parkingID" to parkingID,
                            "vehicleID" to vehicleID,
                            "parkingSpotID" to spotID,  //Acá antes decía spotID en lugar de parkingSpotID
                            "reservationDate" to LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                            "userArrivedDate" to null,
                            "userLeftDate" to null,
                            "cancelationDate" to null
                        )

                        val doc = db.collection(RESERVATIONS_COL)
                            .document()

                        doc.set(res)
                            .await()

                        sendMsgToFront(
                            msgToParkDetFrag,
                            SingleMsg("Reserva realizada exitosamente")
                        )

                        //Ahora actualizo currentReservation y reservationsList
                        val auxRes = Reservation(
                            res["active"] as Boolean,
                            res["userID"] as String,
                            res["parkingID"] as String,
                            res["vehicleID"] as String,
                            res["parkingSpotID"] as String,
                            res["reservationDate"] as String,
                            res["userArrivedDate"] as String?,
                            res["userLeftDate"] as String?,
                            res["cancelationDate"] as String?
                        )
                        auxRes.uid = doc.id
                        completeParkingExtraData(auxRes.parkingID, auxRes)
                        completeVehicleExtraData(auxRes.vehicleID, auxRes)
                        currentReservation.postValue(auxRes)

                        var auxList: MutableList<Reservation> = reservationsList.value!!
                        auxList.add(0, auxRes)
                        // guardar reserva, en currentReservation y generar snapshot
                        reservationsList.postValue(auxList)
                        createSnapshot(auxRes.uid)
                        //getAllReservations(currentReservation.value!!.userID)

                        Log.d(
                            "TEST: ReservationsVM -> makeReservation($userID, $parkingID, $vehicleID): ",
                            "Reserva creada"
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
                sendMsgToFront(msgToParkDetFrag, SingleMsg("Error al intentar realizar la reserva"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cancelCurrentReservation() {
        //Esto debería implementarse en una transacción
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentReservation.value != null) {
                    val cancelDate =
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

                    //Inicio de la transacción
                    db.collection(RESERVATIONS_COL)
                        .document(currentReservation.value!!.uid)
                        .update(
                            mapOf(
                                "active" to false,
                                "cancelationDate" to cancelDate
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
                    //Fin de la transacción => actualizo los livedata
                    currentReservation.postValue(null)

                    sendMsgToFront(
                        msgToProfFrag,
                        SingleMsg("La reserva fue cancelada exitosamente")
                    )

                    //Ahora actualizo la lista de reservas
                    //Creo una lista temporal para acutalizar solo el primer item de la lista
                    val resList = reservationsList.value

                    if (resList!!.get(0).active) {
                        resList?.get(0)?.active = false
                        resList?.get(0)?.cancelationDate = cancelDate
                    }

                    //reservationsList.value!!.clear()
                    reservationsList.postValue(resList)

                    //Ahora vuelvo a actualizar la lista completa por las dudas.
                    //getAllReservations(currentReservation.value!!.userID)

                    Log.d(
                        "TEST: ReservationsVM -> cancelCurrentReservation(): ",
                        "Reserva cancelada"
                    )
                } else {
                    Log.d(
                        "TEST: ReservationsVM -> cancelCurrentReservation(): ",
                        "No hay una reserva activa"
                    )
                    sendMsgToFront(
                        msgToProfFrag, SingleMsg("No hay una reserva activa")
                    )
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

        val query = db.collection(SPOTS_COL)
            .limit(1)
            .whereEqualTo("parkingID", parkingID)
            .whereEqualTo("available", true)
            .whereEqualTo("active", true)
            .get()
            .await()

        if (query != null) {
            for (spot in query) {
                spotID = spot.id
            }
        }
        return spotID
    }

    private suspend fun hasReservations(userId: String): Boolean {
        var hasReservations = false

        var query = db.collection(RESERVATIONS_COL)
            .limit(1)
            .whereEqualTo("userID", userId)
            .whereEqualTo("active", true)
            .get()
            .await()

        if (query.size() > 0) {
            hasReservations = true

            for (d in query) {
                //Log.d("TEST: ReservationsVM -> hasReservations($userId)", d.toString())
            }
            Log.d("TEST: ReservationsVM -> hasReservations($userId): ", hasReservations.toString())
        }
        return hasReservations
    }

    private suspend fun completeParkingExtraData(parkingID: String, reservation: Reservation) {
        try {
            var parking = db.collection(PARKINGS_COL).document(parkingID).get()
                .await()

            reservation.parking = parking.toObject()!!
            reservation.parking.uid = parking.id

        } catch (e: Exception) {
            Log.d(
                "TEST: ReservationsVM -> getReservationExtraData(): ",
                "Error al traer los datos extra del estacionamiento: ${e.message}"
            )
            sendMsgToFront(
                msgToProfFrag, SingleMsg("Error al traer los datos extra del estacionamiento")
            )
        }
    }

    private suspend fun completeVehicleExtraData(vehicleID: String, reservation: Reservation) {
        try {
            var vehicle = db.collection(VEHICLES_COL).document(vehicleID).get()
                .await()

            reservation.vehicle = vehicle.toObject()!!
            reservation.vehicle.uid = vehicle.id

        } catch (e: Exception) {
            Log.d(
                "TEST: ReservationsVM -> getReservationExtraData(): ",
                "Error al traer los datos extra del vehículo: ${e.message}"
            )
            sendMsgToFront(
                msgToProfFrag, SingleMsg("Error al traer los datos extra del vehículo")
            )
        }
    }

    private fun sendMsgToFront(mutableLiveData: MutableLiveData<SingleMsg>, smsg: SingleMsg) {
        mutableLiveData.postValue(smsg)
    }
}