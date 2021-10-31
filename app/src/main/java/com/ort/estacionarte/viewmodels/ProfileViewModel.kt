package com.ort.estacionarte.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.R
import com.ort.estacionarte.entitiescountry.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
/*    private var db = Firebase.firestore
    private val auth = Firebase.auth
    private val USERS_COLLECTION = "Users"
    var currentUser: MutableLiveData<User?> = MutableLiveData(null)

    private suspend fun getFirebaseUserData(userID: String): User? {
        var doc = db.collection(USERS_COLLECTION).document(userID).get().await()

        return doc?.toObject()
    }


    fun getUser(userID: String){
        viewModelScope.launch(Dispatchers.IO) {
            try{

            }catch(e: Exception){

            }
        }
    }
    /*
    fun getFirebaseUserData2(searchedID: String){
        // busca el usuasrio
        var usuarioEncontrado = db.collection("Users").document(searchedID)
        usuarioEncontrado.get()
            .addOnSuccessListener { usuarioEncontrado ->
                // si lo encuentra lo guarda en el ususario actual
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
*/
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

    fun logOut() {
        try {
            Firebase.auth.signOut()
            currentUser.postValue(null)
        } catch (e: Exception) {
            Log.d("LoginVM -> registerUser:", e.toString())
            //sendMsgToFront(,"Error")
        }
    }

/*    fun logOut(c: Context, v: View){
        Firebase.auth.signOut()
        userActive = null
        val sharedPref: SharedPreferences = c.getSharedPreferences("Session", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("userID")
        editor.apply()

        //Navigation.findNavController(v).popBackStack(R.id.loginFragment, true)
        Navigation.findNavController(v).popBackStack(R.id.mapFragment, true)
        Navigation.findNavController(v).navigate(R.id.loginFragment)
    } */

*/
}