package com.ort.estacionarte.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.LoadingDialog
import com.ort.estacionarte.adapters.ReservationsAdapter
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val loginVM: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()

    lateinit var v: View

    lateinit var btnVehicles: FloatingActionButton
    lateinit var btnConfig: FloatingActionButton
    lateinit var txtUsername: TextView

    private lateinit var recyclerViewReservations: RecyclerView
    private lateinit var reservationsAdapter: ReservationsAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.profile_fragment, container, false)

        btnVehicles = v.findViewById(R.id.btnVehicles)
        btnConfig = v.findViewById(R.id.btnConfig)
        txtUsername = v.findViewById(R.id.txtUserName)
        recyclerViewReservations = v.findViewById(R.id.recyclerViewReservas)

        val loadinDialog = LoadingDialog(requireActivity())
        loadinDialog.startDialog()

        loginVM.currentUser.observe(viewLifecycleOwner, Observer { currentUser ->
            if (currentUser != null) {
                //txtUsername.text = currentUser.lastName + " " + currentUser.name
                txtUsername.text = currentUser.name[0].uppercase() + currentUser.name.subSequence(1,currentUser.name.length) + " " + currentUser.lastName[0].uppercase()
            }
        })

        reservationsVM.msgToProfFrag.observe(viewLifecycleOwner, Observer { smsg ->
            if (smsg.isNew()) sendAlertMessage(smsg.readMsg(), "Atencion")
        })

        //Seteos del RecyclerView de reservas
        reservationsAdapter = ReservationsAdapter(reservationsVM.reservationsList.value!!, { item ->
            onItemClick(item)
        }, requireContext())
        recyclerViewReservations.setHasFixedSize(false) //Se cambi?? esto a false
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewReservations.layoutManager = linearLayoutManager
        recyclerViewReservations.adapter = reservationsAdapter

        reservationsVM.reservationsList.observe(viewLifecycleOwner, Observer { reservationsList ->
            if (reservationsList.size > 0) {
                reservationsAdapter = ReservationsAdapter(reservationsList, { item ->
                    onItemClick(item)
                }, requireContext())
                recyclerViewReservations.adapter = reservationsAdapter
            }
        })

        reservationsVM.msgToLoadinDialog.observe(viewLifecycleOwner, Observer { smsg ->
            if (smsg.isNew()){
                //smsg.readMsg()
                loadinDialog.dismissDialog()
            }
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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onItemClick(item: Int) {
        if (reservationsVM.reservationsList.value!![item].active) {
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage("Esta seguro que desea cancelar la reserva?")
                ?.setTitle("Cancelar reserva")
            builder?.apply {
                setPositiveButton("Aceptar",
                    DialogInterface.OnClickListener { dialog, id ->
                        reservationsVM.cancelCurrentReservation()
                    })
                setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            }
            builder?.create()
            builder?.show()
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

    /*private fun saveInSharedPreferences(tag: String, values: Map<String, Any>) {
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
    }*/
}