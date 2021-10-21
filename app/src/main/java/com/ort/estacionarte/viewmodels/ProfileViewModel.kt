package com.ort.estacionarte.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.R
import com.ort.estacionarte.entitiescountry.User

class ProfileViewModel : ViewModel() {
    private var db = Firebase.firestore
    var userActive: User? = null

    fun getFirebaseUserData(searchedID: String){
        var usuarioEncontrado = db.collection("Users").document(searchedID)
        usuarioEncontrado.get()
            .addOnSuccessListener { usuarioEncontrado ->
                if (usuarioEncontrado != null) {
                    Log.d("LoginTest", usuarioEncontrado.toString())
                    userActive = usuarioEncontrado.toObject()!!
                } else {
                    Log.d("LoginTest", "Usuario no encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Test", "get failed with ", exception)
            }
    }//db.collection("cities").document("BJ")

    fun updateUser(name: String, lastName: String, phoneNumber: String, userID: String, v: View, c: Context){
        var usuarioEncontrado = db.collection("Users").document(userID)
        usuarioEncontrado.update(mapOf(
            "name" to name,
            "lastName" to lastName,
            "phoneNumber" to phoneNumber))
            .addOnSuccessListener { documentReference ->
                if (documentReference != null) {
                    Log.d("ConfigTest", "Document edited")
                    Toast.makeText(v.context, "Usuario editado exitosamente", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(v).popBackStack(R.id.mapFragment, false)
                    Navigation.findNavController(v).navigate(R.id.profileFragment)
                } else {
                    Log.d("ConfigTest", "Usuario no encontrado")
                    Toast.makeText(v.context, "No se pudo editar el usuario debido a un error", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Test", "get failed with ", exception)
            }
    }

    fun logOut(c: Context, v: View){
        Firebase.auth.signOut()
        userActive = null
        val sharedPref: SharedPreferences = c.getSharedPreferences("Session", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("userID")
        editor.apply()

        Navigation.findNavController(v).popBackStack(R.id.loginFragment, true)
        Navigation.findNavController(v).navigate(R.id.loginFragment)
    }
}