package com.ort.estacionarte.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel
import com.ort.estacionarte.viewmodels.VehiclesViewModel

class SplashFragment : Fragment() {

    lateinit var v: View

    private val loginVM: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()
    private val vehiclesVM: VehiclesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.splash_fragment, container, false)

        loginVM.getCurrentUser()

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", Context.MODE_PRIVATE)
        var userID = sharedPref.getString("userID", "default")

        Log.d("test", userID!!)

        Handler().postDelayed({
            if (userID != "") {
                Log.d("TestRedirect", userID)
                reservationsVM.getAllReservations(userID)
                vehiclesVM.getUserVehicles(userID)
                Navigation.findNavController(v).popBackStack(R.id.splashFragment, true)
                Navigation.findNavController(v).navigate(R.id.mapFragment)
            } else {
                Log.d("TestRedirect2", userID)

                Navigation.findNavController(v).popBackStack(R.id.splashFragment, true)
                Navigation.findNavController(v).navigate(R.id.loginFragment)
            }
        }, 1000)

        return v
    }

}