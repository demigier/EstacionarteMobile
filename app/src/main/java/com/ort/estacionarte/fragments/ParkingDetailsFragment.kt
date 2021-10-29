package com.ort.estacionarte.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.ParkingDetailsViewModel
import kotlinx.coroutines.*

class ParkingDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ParkingDetailsFragment()
    }

    //Declaro vistas, vm y variables aux
    private lateinit var parkingDetailsVM: ParkingDetailsViewModel
    lateinit var v: View

    lateinit var btnReserve: Button
    lateinit var txtParkingName: TextView
    lateinit var txtParkingAddress: TextView
    lateinit var txtParkingPhoneNumber: TextView
    lateinit var txtParkingAvailableSpots: TextView
    lateinit var spinnerVehicles: Spinner

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    lateinit var userID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.parking_details_fragment, container, false)

        parkingDetailsVM = ViewModelProvider(this).get(ParkingDetailsViewModel::class.java)

        btnReserve = v.findViewById(R.id.btnReserve)
        txtParkingName = v.findViewById(R.id.txtParkingName)
        txtParkingAddress = v.findViewById(R.id.txtParkinAddress)
        txtParkingPhoneNumber = v.findViewById(R.id.txtParkingPhoneNumber)
        txtParkingAvailableSpots = v.findViewById(R.id.txtParkingAvailableSpots)
        spinnerVehicles = v.findViewById(R.id.spinnerVehicle)

        btnReserve.isEnabled = false

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session",Context.MODE_PRIVATE)
        userID = sharedPref.getString("userID", "default").toString()

        //Coordenadas obtenidas del MapFragment del mapa
        var lat: Double? = arguments?.getDouble("lat")
        var long: Double? = arguments?.getDouble("long")

        Log.d("Test: ParkingFragment", "lat: $lat long: $long")

        //Busco el estacionamiento recibido y lo cargo en la vista
        parkingDetailsVM.getParkingInfo(lat!!, long!!)

       /* parkingDetailsVM.getVehicles(userID)

        ArrayAdapter.createFromResource(
            requireContext(),
            parkingDetailsVM.vehiclesList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }*/



        return v
    }

    /*    //@Suppress("DEPRECATION")
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

        }
    */
    override fun onStart() {
        super.onStart()

        //1) Traer datos del estacionamiento seleccionado para mostrar en pantalla
        //      Nombre, direccion, precios, horarios, puntaje.
        //2) Buscar si tiene lugares disponible -> habilitar boton de reservar.
        //      Parking.getSlots().size() >0
        // Procesos de reserva:
        //3) Pasar el Slot a reservado, Crear una reserva y cargar datos del usuario, del estacionamiento y horarios
        //  Parking.takeSlot().addUser()
        //  addReserveDocument()
        //4) Por ahora hasta ahí...
        if(userID != "default"){
            btnReserve.setOnClickListener {
                parkingDetailsVM.makeReservation(userID.toString(), parkingDetailsVM.parkingAct.value?.uid.toString())
            }

            parkingDetailsVM.parkingAct.observe(viewLifecycleOwner, Observer {
                txtParkingName.text = it.parkingName
                txtParkingAddress.text = it.address
                txtParkingPhoneNumber.text = it.phoneNumber
            })

            parkingDetailsVM.availableSpots.observe(viewLifecycleOwner, Observer {
                txtParkingAvailableSpots.text = it.size.toString()

                if (it.size > 0) btnReserve.isEnabled = true
                Log.d("Test: ParkingDetailsFragment", "spots libres:${txtParkingAvailableSpots.text}")
            })

            parkingDetailsVM.toastMessage.observe(viewLifecycleOwner, Observer {
                Toast.makeText(v.context, it, Toast.LENGTH_SHORT).show()
            })
        }

    }

}