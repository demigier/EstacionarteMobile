package com.ort.estacionarte.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.entities.Parking
import com.ort.estacionarte.entitiescountry.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ParkingDetailsViewModel : ViewModel() {
    private val db = Firebase.firestore

    var userAct: User? = null
    var parkingAct = MutableLiveData<Parking>()
    var availableSpots = MutableLiveData<MutableList<String>>()
    var toastMessage = MutableLiveData<kotlin.String>()

    //Variables para corrutinas
    //private val parentJob = Job()
    //val scope = CoroutineScope(Dispatchers.Default + parentJob)

    init {
        //Recupero el Usuario del SharedPreferences, para utilizarlo al momento de realizar la reserva.

    }

    //Métodos
    fun makeReservation(userId: kotlin.String, parkingUid: kotlin.String) {
        //0) En algún momento hay que chequear que el ususarion no tenga una reserva vigente.
        //1) Obtener un spot del estacionamiento: Uso la lista ya obtenida o la armo nuevamente?
        //2) Pasar el spot a no disponible y generar la reserva con el userID, parkingID, y spotID
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var spots = getParkingSpots(parkingAct.value!!.uid)
                //var spot=reserveSpot(spots)
            }catch (e: Exception){

            }
        }

        // Pasando el slot a no disponible... => update
        //db.

    }

    fun getParkingInfo(lat: String, long: String) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var parking = getParkingByLocation(lat, long)
                parkingAct.postValue(parking!!)
                Log.d("ParkingDetailsVM", parking.parkingName.toString())

                var spots = getParkingSpots(parking.uid)
                availableSpots.postValue(spots!!)

                Log.d(
                    "Test: ParkingDetailsVM",
                    "spots libres:${spots.size}"
                )
            } catch (e: Exception) {
                e.message?.let { Log.d("Test: ParkingDetailsVM", it) }
            }

        }
    }

    suspend fun getParkingByLocation(lat: String, long: String): Parking? {
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
            Log.d(
                "Test: ParkingDetailsVM",
                "No existe un Estacionameinto en esa localización."
            )
        }
        return pAux
    }

    suspend fun getParkingSpots(parkingUid: kotlin.String?): MutableList<String>? {
        //A definir: Buscar varios? o buscar uno solo y guardarlo?
        var spotsAux = mutableListOf<String>()

        Log.d(
            "Test: ParkingDetailsVM",
            "parkingID:$parkingUid"
        )

        var docs = db.collection("ParkingSpots")
            .limit(10)
            .whereEqualTo("idParking", parkingUid)
            .whereEqualTo("available", true)
            .get()
            .await()

        if (docs != null) {
            //parkingSpots.value = mutableListOf<Spot>()
            for (spot in docs) {
                spotsAux.add(spot.id)
            }

            Log.d(
                "Test: ParkingDetailsVM",
                "spots libres:${spotsAux.size}"
            )

        } else {
            Log.d(
                "Test: ParkingDetailsVM",
                "No hay lugares libres."
            )
        }
        return spotsAux
    }

    suspend fun getFirebaseUserData(searchedID: kotlin.String): User? {
        return db.collection("Users").document(searchedID).get().await().toObject()
    }

    private fun sendToast(msg: kotlin.String?) {
        toastMessage.value = msg ?: ""
        //toastMessage.postValue(msg ?: "")
    }


}

