package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
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
        var userID = arguments?.getString("userID")

        if (userID != null) {
            scope.launch {
                parkingViewModel.getFirebaseUserData(userID)
                delay(700)

            }
        }else{
            Toast.makeText(v.context, "Error: usted no esta logueado", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(v).backStack
        }

        //Log.d("TestArgs", user.toString())
        btnProfile.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("userID", userID)
            Navigation.findNavController(v).navigate(R.id.profileFragment, bundle)
        }
    }
    // TODO: Implement HomeFragment
}