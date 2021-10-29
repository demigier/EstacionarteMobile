package com.ort.estacionarte.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        const val REQUEST_CODE_LOCATION = 0
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

        createMapFragment()

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

    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isPermissionsGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Ubicacion denegada al abrir app
            Toast.makeText(requireContext(), "Ve a ajustes y acepta los permisos de ubicacion", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                map.isMyLocationEnabled = true
            }else{
                Log.d("TestLocation", "Pedir2")
                Toast.makeText(requireContext(), "Para activar la localización ve a ajustes y acepta los permisos de ubicacion", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        //map = googleMap
        map = googleMap
        enableLocation()
    }

    private fun createMapFragment() {
                val mapFragment = childFragmentManager.findFragmentById(R.id.mapItem) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }
}