package com.ort.estacionarte.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    //private val profileViewModel: ProfileViewModel by activityViewModels()
    private val loginVM: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()
    //private lateinit var profileViewModel: ProfileViewModel

    lateinit var v: View

    lateinit var btnVehicles: FloatingActionButton
    lateinit var btnConfig: FloatingActionButton
    lateinit var btnCancel: Button
    lateinit var txtUsername: TextView
    lateinit var txtReservPName: TextView
    lateinit var txtReservPAddress: TextView
    lateinit var txtReservPPhone: TextView
    lateinit var txtReservVehicle: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.profile_fragment, container, false)
        //profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        btnVehicles = v.findViewById(R.id.btnVehicles)
        btnConfig = v.findViewById(R.id.btnConfig)
        btnCancel = v.findViewById(R.id.btnReservCancel)
        txtUsername = v.findViewById(R.id.txtUserName)
        txtReservPName = v.findViewById(R.id.txtUserName)
        txtReservPAddress = v.findViewById(R.id.txtReservPAddress)
        txtReservPPhone = v.findViewById(R.id.txtReservPPhone)
        txtReservVehicle= v.findViewById(R.id.txtReservVehicle)

        //Reservation Card
        txtReservPName = v.findViewById(R.id.txtReservPName)

        //loginViewModel.currentUser.value?.let { Log.d("ProfileF -> currnetUser:", it.name) }
        //var userID = getFromSharedPreferences("Session")?.get("userID").toString()
        //profileViewModel.currentUser.value = loginViewModel.currentUser.value

        loginVM.currentUser.observe(viewLifecycleOwner, Observer { currentUser ->

            if (currentUser != null) {
                txtUsername.text = currentUser.lastName + " " + currentUser.name
            }
        })

        reservationsVM.currentReservation.observe(viewLifecycleOwner, Observer { cr ->
            btnCancel.isEnabled = (cr != null)
            /*txtReservPName.text = "Estacionamiento: "+reservationsVM.currentReservationExtraData["ParkingName"].toString()
            txtReservPAddress.text = "Dirección: "+ reservationsVM.currentReservationExtraData["ParkingAddress"].toString()
            txtReservPPhone.text = "Teléfono: "+reservationsVM.currentReservationExtraData["ParkingPhoneNumber"].toString()
            txtReservVehicle.text = "Vehículo: "+reservationsVM.currentReservationExtraData["VehicleLicensePlate"].toString()
        */
        })


        reservationsVM.msgToProfFrag.observe(viewLifecycleOwner, Observer{ smsg ->
            if (smsg.isNew())
                sendAlertMessage(smsg.readMsg(), "Atencion")
        })

        return v
    }

    override fun onStart() {
        super.onStart()

        btnVehicles.setOnClickListener {
            Navigation.findNavController(v).navigate(R.id.vehiclesFragment)
        }

        btnConfig.setOnClickListener {
            Navigation.findNavController(v).navigate(R.id.configurationFragment)
        }

        btnCancel.setOnClickListener{
                reservationsVM.cancelCurrentReservation()
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

    //Funciones para manejo de las SP
    private fun saveInSharedPreferences(tag: String, values: Map<String, Any>) {
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(
            tag,
            Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()

        values.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                else -> editor.putString(key, value.toString())
            }
        }
        editor.apply()
    }

    private fun getFromSharedPreferences(tag: String): MutableMap<String, *>? {
        return requireContext().getSharedPreferences(tag, Context.MODE_PRIVATE).all
    }
}