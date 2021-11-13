package com.ort.estacionarte.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.SingleMsg
import com.ort.estacionarte.entities.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.Exception
import kotlin.collections.mutableListOf

class VehiclesViewModel : ViewModel() {
    private var db = Firebase.firestore
    private val VEHICLES_COL = "Vehicles"

    var vehiclesList: MutableLiveData<MutableList<Vehicle>> = MutableLiveData(mutableListOf())

    var msgToVehiclesFrag = MutableLiveData<SingleMsg>()
    var msgToVehiclesDetFrag = MutableLiveData<SingleMsg>()
    //init {    }

    //Se llama desde VehiclesFragment
    fun getUserVehicles(userID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = db.collection(VEHICLES_COL)
                    .whereEqualTo("userID", userID)
                    .whereEqualTo("active", true)
                    .get()
                    .await()

                var tmpVList = mutableListOf<Vehicle>()

                for (vehicle in query) {
                    Log.d("VehicleTest", vehicle.toString())
                    tmpVList.add(vehicle.toObject())
                    tmpVList.last().uid = vehicle.id
                }

                vehiclesList.postValue(tmpVList)

            } catch (e: Exception) {
                sendMsgToFront(msgToVehiclesFrag, SingleMsg("No se pudo obtener la lista de vehiculos debido a un error"))
                Log.d("VehicleTest", "get failed with: ${e.message.toString()}")
            }
        }
    }

    //Se llama desde VehiclesDetailsFragment
    fun updateUserVehicle(vehicle: Vehicle) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = db.collection(VEHICLES_COL)
                    .whereEqualTo("licensePlate", vehicle.licensePlate)
                    .whereEqualTo("userID", vehicle.userID)
                    .whereEqualTo("active", true)
                    .get()
                    .await()

                var count = 0

                for (v in query) {
                    if (v.id == vehicle.uid) {
                        count++
                    }
                }

                if (query.documents.size == 0 || count == 1) {
                    val query2 = db.collection(VEHICLES_COL).document(vehicle.uid)
                        .update(
                            mapOf(
                                "brand" to vehicle.brand,
                                "model" to vehicle.model,
                                "licensePlate" to vehicle.licensePlate
                            )
                        )
                        .await()

                    Log.d("VehicleTest", "Document edited")
                    getUserVehicles(vehicle.userID)
                    sendMsgToFront(msgToVehiclesDetFrag, SingleMsg("Vehiculo editado exitosamente"))

                } else {
                    Log.d("VehicleTest", "Ya existe esa patente")
                }
            } catch (e: Exception) {
                Log.d("VehicleTest", "get failed with: ${e.message.toString()}")
                sendMsgToFront(
                    msgToVehiclesDetFrag,
                    SingleMsg("No se pudo editar el vehiculo debido a un error")
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public fun addUserVehicle(vehicle: Vehicle/*, v: View*/) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = db.collection(VEHICLES_COL)
                    .whereEqualTo("licensePlate", vehicle.licensePlate)
                    .whereEqualTo("userID", vehicle.userID)
                    .whereEqualTo("active", true)
                    .get()
                    .await()

                Log.d("VehicleTest", "Largo de vehicleList ${vehiclesList.value?.size}")
                /*var exist = false

                for(v in vehiclesList.value!!){
                    if(v.licensePlate==vehicle.licensePlate){
                        exist=true
                        break
                    }
                }*/
                Log.d("VehicleTest",query.documents.toString())
                Log.d("VehicleTest","cantidad de vehiculos repetidos:" + query.documents.toString())
                if (query.documents.size == 0) {
                    val query2 = db.collection(VEHICLES_COL).document()
                        .set(
                            mapOf(
                                "brand" to vehicle.brand,
                                "model" to vehicle.model,
                                "licensePlate" to vehicle.licensePlate,
                                "userID" to vehicle.userID,
                                "active" to true
                            )
                        )
                        .await()

                    //vehiclesList.value?.add(vehicle)
                    getUserVehicles(vehicle.userID)

                    Log.d("VehicleTest", "Document added")
                    sendMsgToFront(
                        msgToVehiclesDetFrag,
                        SingleMsg("Vehiculo añadido exitosamente")
                    )

                } else {
                    Log.w("VehicleTest", "Ya existe esa patente")
                    sendMsgToFront(
                        msgToVehiclesDetFrag,
                        SingleMsg("Ya tienes un vehículo con esa patente")
                    )
                }

            } catch (e: Exception) {
                Log.d("VehicleTest", "get failed with: ${e.message.toString()}")

                sendMsgToFront(
                    msgToVehiclesDetFrag,
                    SingleMsg("No se pudo añadir el vehiculo debido a un error")
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public fun deleteUserVehicle(vehicleID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("Vehicles").document(vehicleID)
                    .update("active", false)
                    .await()

                Log.d("VehicleTest", "Vehicle removed succesfully")
                sendMsgToFront(
                    msgToVehiclesDetFrag,
                    SingleMsg("Vehiculo eliminado exitosamente")
                )

            } catch (e: Exception) {
                Log.d("VehicleTest", "get failed with: ${e.message.toString()}")
                sendMsgToFront(
                    msgToVehiclesDetFrag,
                    SingleMsg("No se pudo eliminar el vehiculo debido a un error")
                )
            }
        }
    }

    private fun sendMsgToFront(mutableLiveData: MutableLiveData<SingleMsg>, smsg: SingleMsg) {
        mutableLiveData.postValue(smsg)
    }
}