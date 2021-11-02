package com.ort.estacionarte.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ParkingDetailsViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel
import kotlinx.coroutines.*

class ParkingDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ParkingDetailsFragment()
    }

    //Declaro vistas, vm y variables aux
    private val loginVM: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()
    private val parkingDetailsVM: ParkingDetailsViewModel by activityViewModels()

    lateinit var v: View

    lateinit var btnReserve: Button
    lateinit var txtParkingName: TextView
    lateinit var txtParkingAddress: TextView
    lateinit var txtParkingPhoneNumber: TextView
    lateinit var txtParkingAvailableSpots: TextView

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

        //parkingDetailsVM = ViewModelProvider(this).get(ParkingDetailsViewModel::class.java)

        btnReserve = v.findViewById(R.id.btnReserve)
        txtParkingName = v.findViewById(R.id.txtParkingName)
        txtParkingAddress = v.findViewById(R.id.txtParkinAddress)
        txtParkingPhoneNumber = v.findViewById(R.id.txtParkingPhoneNumber)
        txtParkingAvailableSpots = v.findViewById(R.id.txtParkingAvailableSpots)

        btnReserve.isEnabled = false

        val sharedPref: SharedPreferences =
            requireContext().getSharedPreferences("Session", Context.MODE_PRIVATE)
        userID = sharedPref.getString("userID", "default").toString()

        //Coordenadas obtenidas del MapFragment del mapa
        var lat: Double? = arguments?.getDouble("lat")
        var long: Double? = arguments?.getDouble("long")

        Log.d("Test: ParkingFragment", "lat: $lat long: $long")

        //Busco el estacionamiento recibido y lo cargo en la vista
        parkingDetailsVM.getParkingInfo(lat!!, long!!)

        // parkingDetailsVM.getVehicles(userID)


        return v
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        if (loginVM.currentUser.value != null) { //userID != "default"
            btnReserve.setOnClickListener {
                reservationsVM.makeReservation(
                    userID.toString(),
                    parkingDetailsVM.parkingAct.value?.uid.toString(),
                    "6yeWT1Src7OgCbdvzjaf"
                )
            }

            parkingDetailsVM.parkingAct.observe(viewLifecycleOwner, Observer {
                txtParkingName.text = it.parkingName
                txtParkingAddress.text = it.address
                txtParkingPhoneNumber.text = it.phoneNumber
            })

            parkingDetailsVM.availableSpots.observe(viewLifecycleOwner, Observer {
                txtParkingAvailableSpots.text = it.size.toString()

                if (it.size > 0) btnReserve.isEnabled = true
                Log.d(
                    "Test: ParkingDetailsFragment",
                    "spots libres:${txtParkingAvailableSpots.text}"
                )
            })

            reservationsVM.msgToParkDetFrag.observe(viewLifecycleOwner, Observer { smsg ->
                //Toast.makeText(v.context, it, Toast.LENGTH_SHORT).show()
                if(smsg.isNew())
                    sendAlertMessage(smsg.readMsg(), "Atención")
            })
        }

    }

    private fun sendAlertMessage(msg: String, title: String) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }
        builder?.setMessage(msg)
            ?.setTitle(title)
        builder?.apply {
            setNegativeButton("Aceptar",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })
        }
        builder?.create()
        builder?.show()
    }
}