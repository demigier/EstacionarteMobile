package com.ort.estacionarte.viewmodels

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.R
import com.ort.estacionarte.entitiescountry.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.os.Bundle




class LoginViewModel : ViewModel() {
    private var db = Firebase.firestore
    private val auth = Firebase.auth
    //var usersList: MutableList<User> = mutableListOf()
    var userActive: User? = null
   /* init {
        getFirebaseUsers()
    }*/

    fun loginUser(email: String, password: String, v: View) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d("LoginTest", auth.currentUser.toString())
                    Toast.makeText(v.context, "Usuario logueado exitosamente", Toast.LENGTH_SHORT).show()
                    getFirebaseUserData(auth.currentUser!!.uid)

                    val bundle = Bundle()
                    bundle.putString("userID", auth.currentUser!!.uid)
                    Navigation.findNavController(v).navigate(R.id.mapFragment, bundle)
                    //updateUI(user)
                } else {
                    Log.w("LoginTest", "signInWithEmail:failure", task.exception)
                    Toast.makeText(v.context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }

    fun registerUser(newUser: User, password: String, v: View){
        auth.createUserWithEmailAndPassword(newUser.email, password)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterTest", "createUserWithEmail:success")
                    newUser.uid = auth.currentUser!!.uid
                    this.registerFirebaseUser(newUser, v)

                    val bundle = Bundle()
                    bundle.putString("userID", auth.currentUser!!.uid)
                    Navigation.findNavController(v).navigate(R.id.mapFragment, bundle)
                    //updateUI(user)
                } else {
                    if(task.exception?.message == "The email address is already in use by another account."){
                        Toast.makeText(v.context, "Ese mail ya esta registrado", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(v.context, "No se pudo registrar al usuario debido a un error", Toast.LENGTH_SHORT).show()
                    }
                    Log.w("RegisterTest", "createUserWithEmail:failure", task.exception)
                    //updateUI(null)
                }
            }
    }

    private fun getFirebaseUserData(searchedID: String){
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
    }

    private fun registerFirebaseUser(newUser: User, v: View){
        db.collection("Users").document(newUser.uid).set(newUser)
            .addOnSuccessListener{ documentReference ->
                Log.d("RegisterTest", "Document created")
                Toast.makeText(v.context, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                userActive = newUser
            }
            .addOnFailureListener { e ->
                Log.w("RegisterTest", "Error adding document", e)
                Toast.makeText(v.context, "No se pudo registrar al usuario debido a un error", Toast.LENGTH_SHORT).show()
            }
    }

    fun logOut(){
        Firebase.auth.signOut()
        userActive = null
    }

}