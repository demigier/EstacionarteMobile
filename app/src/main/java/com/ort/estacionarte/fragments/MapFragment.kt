package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.ParkingDetailsViewModel
import kotlinx.coroutines.*




class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() = MapFragment()
    }

    private lateinit var parkingDetailsViewModel: ParkingDetailsViewModel
    lateinit var v: View

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    lateinit var btnProfile: FloatingActionButton
    lateinit var btnToParking: FloatingActionButton

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.map_fragment, container, false)
        btnProfile = v.findViewById(R.id.btnAdd)
        btnToParking = v.findViewById(R.id.btnToParking)
        //createMapFragment()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapItem) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //parkingDetailsViewModel = ViewModelProvider(this).get(ParkingDetailsViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", MODE_PRIVATE)
        var userID = sharedPref.getString("userID","default")

        btnProfile.setOnClickListener {
            Navigation.findNavController(v).navigate(R.id.profileFragment)
        }

        btnToParking.setOnClickListener {

            //location = MapViewModel.addressToLocation(addres)

            val bundle = bundleOf("lat" to "100","long" to "100")

            Navigation.findNavController(v).navigate(R.id.parkingFragment,bundle)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("TestMap", "HOla")
        //map = googleMap
        map = googleMap
    }

    /*private fun createMapFragment() {
        //val mapFragment = (activity as FragmentActivity).supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment?
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapItem) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*val mapFragment = childFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment?

        mapFragment!!.getMapAsync(this)*/
    }*/
}