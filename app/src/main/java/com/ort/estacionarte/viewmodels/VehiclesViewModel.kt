package com.ort.estacionarte.viewmodels

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.R
import com.ort.estacionarte.entities.Vehicle
import com.ort.estacionarte.entitiescountry.User

class VehiclesViewModel : ViewModel() {
    private var db = Firebase.firestore
    var vehiclesList: MutableList<Vehicle>? = mutableListOf()

    public fun getFirebaseUserVehicles(searchedID: String){
        vehiclesList!!.clear()

        var i = 0
        var vehiculosEncontrados = db.collection("Vehicles")
            .whereEqualTo("userID", searchedID)
            .whereEqualTo("active", true)
        vehiculosEncontrados.get()
            .addOnSuccessListener { vehiclesFounded ->
                if (vehiclesFounded != null) {
                    for(vehicle in vehiclesFounded){
                        Log.d("VehicleTest", vehicle.toString())
                        vehiclesList?.add(vehicle.toObject())
                        vehiclesList!![i].uid = vehicle.id
                        i = i + 1
                    }
                } else {
                    Log.d("VehicleTest", "No tiene vehiculos")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("VehicleTest", "get failed with ", exception)
            }
    }

    @SuppressLint("RestrictedApi")
    public fun updateFirebaseUserVehicle(vehicle: Vehicle, v: View){
        db.collection("Vehicles").whereEqualTo("licensePlate", vehicle.licensePlate).whereEqualTo("userID", vehicle.userID).get()
            .addOnSuccessListener { vehiclesFounded ->
                var count = 0
                for(v in vehiclesFounded){
                    if(v.id == vehicle.uid){
                        count++
                    }
                }
                if (vehiclesFounded.documents.size == 0 || count == 1) {
                    db.collection("Vehicles").document(vehicle.uid).update(
                        mapOf(
                            "brand" to vehicle.brand,
                            "model" to vehicle.model,
                            "licensePlate" to vehicle.licensePlate
                        )
                    )
                        .addOnSuccessListener { documentReference ->
                            Log.d("VehicleTest", "Document edited")
                            Toast.makeText(
                                v.context,
                                "Vehiculo editado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            /*val bundle = Bundle()
                    bundle.putString("userID", vehicle.userID)*/
                            Navigation.findNavController(v).popBackStack(R.id.mapFragment, false)
                            Navigation.findNavController(v).navigate(R.id.profileFragment)
                        }
                        .addOnFailureListener { e ->
                            Log.w("VehicleTest", "Error editting document", e)
                            Toast.makeText(
                                v.context,
                                "No se pudo editar el vehiculo debido a un error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }else{
                    Log.w("VehicleTest", "Ya existe esa patente")
                    Toast.makeText(v.context, "Ya tienes un vehiculo con esa patente", Toast.LENGTH_SHORT).show()
                }

            }
    }

    @SuppressLint("RestrictedApi")
    public fun addFirebaseUserVehicle(vehicle: Vehicle, v: View){
        db.collection("Vehicles").whereEqualTo("licensePlate", vehicle.licensePlate).whereEqualTo("userID", vehicle.userID).get()
            .addOnSuccessListener { vehiclesFounded ->
                if (vehiclesFounded.documents.size == 0) {
                    db.collection("Vehicles").document().set(mapOf(
                        "brand" to vehicle.brand,
                        "model" to vehicle.model,
                        "licensePlate" to vehicle.licensePlate,
                        "userID" to vehicle.userID,
                        "active" to true))
                        .addOnSuccessListener{ documentReference ->
                            Log.d("VehicleTest", "Document added")
                            Toast.makeText(v.context, "Vehiculo añadido exitosamente", Toast.LENGTH_SHORT).show()
                            /*val bundle = Bundle()
                            bundle.putString("userID", vehicle.userID)*/
                            Navigation.findNavController(v).popBackStack(R.id.mapFragment, false)
                            Navigation.findNavController(v).navigate(R.id.profileFragment)
                        }
                        .addOnFailureListener { e ->
                            Log.w("VehicleTest", "Error adding document", e)
                            Toast.makeText(v.context, "No se pudo añadir el vehiculo debido a un error", Toast.LENGTH_SHORT).show()
                        }
                }else{
                    Log.w("VehicleTest", "Ya existe esa patente")
                    Toast.makeText(v.context, "Ya tienes un vehiculo con esa patente", Toast.LENGTH_SHORT).show()
                }
            }

    }

    @SuppressLint("RestrictedApi")
    public fun deleteFirebaseUserVehicle(vehicleID: String, v: View){
        /*db.collection("Vehicles").document(vehicleID).delete()
            .addOnSuccessListener{ documentReference ->
                Log.d("VehicleTest", "Vehicle removed succesfully")
                Toast.makeText(v.context, "Vehiculo eliminado exitosamente", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(v).popBackStack(R.id.mapFragment, false)
                Navigation.findNavController(v).navigate(R.id.profileFragment)
            }
            .addOnFailureListener { e ->
                Log.w("VehicleTest", "Error deletting document", e)
                Toast.makeText(v.context, "No se pudo eliminar el vehiculo debido a un error", Toast.LENGTH_SHORT).show()
            }*/

        db.collection("Vehicles").document(vehicleID).update("active", false)
            .addOnSuccessListener{ documentReference ->
                Log.d("VehicleTest", "Vehicle removed succesfully")
                Toast.makeText(v.context, "Vehiculo eliminado exitosamente", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(v).popBackStack(R.id.mapFragment, false)
                Navigation.findNavController(v).navigate(R.id.profileFragment)
            }
            .addOnFailureListener { e ->
                Log.w("VehicleTest", "Error deletting document", e)
                Toast.makeText(v.context, "No se pudo eliminar el vehiculo debido a un error", Toast.LENGTH_SHORT).show()
            }
    }
}