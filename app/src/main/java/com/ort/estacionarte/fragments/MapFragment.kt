package com.ort.estacionarte.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.*
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.activities.HomeActivity
import com.ort.estacionarte.activities.LoginActivity
import com.ort.estacionarte.entities.Parking
import com.ort.estacionarte.viewmodels.ParkingDetailsViewModel
import kotlinx.coroutines.*
import java.io.IOException


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    companion object {
        fun newInstance() = MapFragment()
        const val REQUEST_CODE_LOCATION = 0
    }

    private lateinit var parkingDetailsViewModel: ParkingDetailsViewModel
    lateinit var v: View

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    lateinit var btnProfile: FloatingActionButton
    lateinit var searchView: SearchView//search_address
    lateinit var listView: ListView
    var addresNamesList: MutableList<String> = mutableListOf()
    lateinit var adapter: ArrayAdapter<*>

    var lastSearchAddress: String? = null
    var lastSearchMarker: Marker? = null
    lateinit var userID: String
    private lateinit var map: GoogleMap

    private lateinit var parkingList: MutableList<Parking>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.map_fragment, container, false)

        btnProfile = v.findViewById(R.id.btnAdd)
        searchView = v.findViewById(R.id.search_address)
        listView = v.findViewById(R.id.list_view)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, addresNamesList)
        listView.adapter = adapter

        createMapFragment()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                if (searchView.query.toString() != null) {
                    lastSearchAddress = searchView.query.toString()

                    var geocoder = Geocoder(requireContext())

                    try {
                        var addressObtained: MutableList<Address> =
                            geocoder.getFromLocationName(lastSearchAddress, 5)

                        if (addressObtained.size > 0) {
                            lastSearchMarker?.remove()

                            var latLong = LatLng(addressObtained[0].latitude, addressObtained[0].longitude)

                            lastSearchMarker = map.addMarker(MarkerOptions().position(latLong).title(lastSearchAddress))
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15F))
                        } else {
                            Toast.makeText(requireContext(), "Dirección no encontrada", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: IOException) {
                        Log.d("Error maps location", e.message.toString())
                        e.printStackTrace()
                    } catch (iae: IllegalArgumentException) {
                        Log.d("Error maps location", iae.message.toString())
                    }
                }

                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })

        return v
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()

        parkingDetailsViewModel = ViewModelProvider(this).get(ParkingDetailsViewModel::class.java)

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", MODE_PRIVATE)
        userID = sharedPref.getString("userID", "default").toString()

        btnProfile.setOnClickListener {
            Navigation.findNavController(v).navigate(R.id.profileFragment)
        }
    }

    //CALLBACK DE GOOGLEMAPS
    @Suppress("DEPRECATION")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_json))

        putCameraOnCurrentLocation()

        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isTiltGesturesEnabled = true

        parkingDetailsViewModel.getParkings()

        if(map.isMyLocationEnabled){
            val locationButton= (v.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
            val rlp=locationButton.layoutParams as (RelativeLayout.LayoutParams)
            rlp.setMargins(0,40,30,0);

            val toolBar= (v.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("4"))
            val rlp2=toolBar.layoutParams as (RelativeLayout.LayoutParams)
            rlp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            rlp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            rlp2.setMargins(100,40,0,100)
        }

        parkingDetailsViewModel.parkingList.observe(viewLifecycleOwner, Observer { parkingList ->
            for (parking in parkingList) {
                val marker = LatLng(parking.location["lat"]!!, parking.location["long"]!!)
                map.addMarker(
                    MarkerOptions()
                        .position(marker)
                        .draggable(false)
                        .title(parking.parkingName)
                        .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.parking))
                        .snippet("Direccion: " + parking.address)
                )
            }
        })
        googleMap.setOnInfoWindowClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun putCameraOnCurrentLocation() {
        if (lastSearchMarker == null) {
            var flp = LocationServices.getFusedLocationProviderClient(requireContext())

            flp.lastLocation.addOnSuccessListener { location ->
                val latLong = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15F))
            }
        }
    }

    //EVENTO DE CLICK EN MARKER
    override fun onInfoWindowClick(marker: Marker) {
        val location =
            bundleOf("lat" to marker.position.latitude, "long" to marker.position.longitude)
        Navigation.findNavController(v).navigate(R.id.parkingFragment, location)
    }

    //INSTANCIAMIENTO DE GOOGLEMAPS
    private fun createMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapItem) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        var vectorDrawable = ContextCompat.getDrawable(context, vectorResId) as Drawable
        vectorDrawable.setTint(Color.BLACK);
        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        var bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) as Bitmap
        var canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    //PERMISOS DE UBICACION
    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            //Ubicacion denegada al abrir app
            Toast.makeText(
                requireContext(),
                "Ve a ajustes y acepta los permisos de ubicacion",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Log.d("TestLocation", "Pedir2")
                Toast.makeText(
                    requireContext(),
                    "Para activar la localización ve a ajustes y acepta los permisos de ubicacion",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
            }
        }
    }
}