package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.ParkingViewModel
import kotlinx.coroutines.*

class MapFragment : Fragment() {

    companion object {
        fun newInstance() = MapFragment()
    }

    private lateinit var parkingViewModel: ParkingViewModel
    lateinit var v: View

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    lateinit var btnProfile: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.map_fragment, container, false)
        btnProfile = v.findViewById(R.id.btnAdd)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parkingViewModel = ViewModelProvider(this).get(ParkingViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", MODE_PRIVATE)
        var userID = sharedPref.getString("userID","default")

        //var userID = arguments?.getString("userID")
        Log.d("Prueba", userID!!)
        if (userID != "default") {
            scope.launch {
                parkingViewModel.getFirebaseUserData(userID)
                delay(700)

                parkingViewModel.getFirebaseParkingsByCoords("-34.5740708197085", "-58.48696861970849")
            }
        }else{
            Toast.makeText(v.context, "Error: usted no esta logueado", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(v).backStack
        }

        btnProfile.setOnClickListener{
            /*val bundle = Bundle()
            bundle.putString("userID", userID)*/
            Navigation.findNavController(v).navigate(R.id.profileFragment)
        }
    }
    // TODO: Implement HomeFragment
}