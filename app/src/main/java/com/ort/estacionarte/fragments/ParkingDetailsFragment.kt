package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
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
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ParkingDetailsViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel
import com.ort.estacionarte.viewmodels.VehiclesViewModel
import kotlinx.coroutines.*

@Suppress("RedundantIf")
class ParkingDetailsFragment : Fragment(), AdapterView.OnItemClickListener {

    companion object {
        fun newInstance() = ParkingDetailsFragment()
    }

    //Declaro vistas, vm y variables aux
    private val loginVM: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()
    private val parkingDetailsVM: ParkingDetailsViewModel by activityViewModels()
    private val vehiclesVM: VehiclesViewModel by activityViewModels()

    lateinit var v: View

    lateinit var btnReserve: Button
    lateinit var txtParkingName: TextView
    lateinit var txtParkingAddress: TextView
    lateinit var txtParkingPhoneNumber: TextView
    lateinit var txtParkingAvailableSpots: TextView
    lateinit var autoCompleteVehicles: AutoCompleteTextView

    var selectedVehicle: String = ""

    lateinit var userID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.parking_details_fragment, container, false)

        btnReserve = v.findViewById(R.id.btnReserve)
        txtParkingName = v.findViewById(R.id.txtParkingName)
        txtParkingAddress = v.findViewById(R.id.txtParkinAddress)
        txtParkingPhoneNumber = v.findViewById(R.id.txtParkingPhoneNumber)
        txtParkingAvailableSpots = v.findViewById(R.id.txtParkingAvailableSpots)
        autoCompleteVehicles = v.findViewById(R.id.autoCompleteVehicles)

        btnReserve.isEnabled = false

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", Context.MODE_PRIVATE)
        userID = sharedPref.getString("userID", "default").toString()

        //Coordenadas obtenidas del MapFragment del mapa
        var lat: Double? = arguments?.getDouble("lat")
        var long: Double? = arguments?.getDouble("long")

        Log.d("Test: ParkingFragment", "lat: $lat long: $long")

        parkingDetailsVM.getParkingInfo(lat!!, long!!)

        vehiclesVM.getUserVehicles(userID)

        return v
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        vehiclesVM.vehiclesList.observe(viewLifecycleOwner, Observer { vehicles ->
            if(vehicles.size > 0){
                var items= arrayListOf<String>()
                for (v in vehicles){
                    items.add(v.licensePlate)
                }
                val adapter = ArrayAdapter(requireContext(), R.layout.vehicle_select_item, items)
                autoCompleteVehicles.setAdapter(adapter)
                autoCompleteVehicles.onItemClickListener = this@ParkingDetailsFragment
            }else{
                sendAlertMessage("Para reservas, debes agregar al menos un vehiculo en tu perfil", "Atencion")
            }
        })

        if (loginVM.currentUser.value != null) { //userID != "default"
            btnReserve.setOnClickListener {
                if(selectedVehicle != ""){
                    reservationsVM.makeReservation(userID.toString(),parkingDetailsVM.parkingAct.value?.uid.toString(),selectedVehicle)
                }else{
                    sendAlertMessage("Debe seleccionar un vehiculo","Atencion")
                }
            }

            parkingDetailsVM.parkingAct.observe(viewLifecycleOwner, Observer {
                txtParkingName.text = it.parkingName
                txtParkingAddress.text = it.address
                txtParkingPhoneNumber.text = it.phoneNumber
            })

            parkingDetailsVM.availableSpots.observe(viewLifecycleOwner, Observer {
                txtParkingAvailableSpots.text = it.size.toString()

                if (it.size > 0){
                    btnReserve.isEnabled = true
                }else{
                    btnReserve.isEnabled = false
                }
                Log.d(
                    "Test: ParkingDetailsFragment",
                    "spots libres:${txtParkingAvailableSpots.text}"
                )
            })

            reservationsVM.msgToParkDetFrag.observe(viewLifecycleOwner, Observer { smsg ->
                if(smsg.isNew())
                    sendAlertMessage(smsg.readMsg(), "Atenci??n")
            })
        }

    }

    @SuppressLint("RestrictedApi")
    private fun sendAlertMessage(msg: String, title: String) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }
        builder?.setMessage(msg)
            ?.setTitle(title)
        builder?.apply {
            setNegativeButton("Aceptar",
                DialogInterface.OnClickListener { dialog, id ->
                    if(msg == "Reserva realizada exitosamente"){
                        Navigation.findNavController(v).popBackStack()
                    }else{
                        dialog.cancel()
                    }
                    //dialog.cancel()
                })
        }
        builder?.create()
        builder?.show()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(vehiclesVM.vehiclesList.value != null){
            if(vehiclesVM.vehiclesList.value?.size!! > 0){
                selectedVehicle = vehiclesVM.vehiclesList.value!![position].uid
            }
        }
    }
}