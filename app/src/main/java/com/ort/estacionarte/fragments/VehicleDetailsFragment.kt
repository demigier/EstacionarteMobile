package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.entities.Vehicle
import com.ort.estacionarte.viewmodels.VehiclesViewModel
import kotlinx.coroutines.*

class VehicleDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = VehicleDetailsFragment()
    }

    private lateinit var vehicleViewModel: VehiclesViewModel
    lateinit var v: View

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    lateinit var txtBrand: EditText
    lateinit var txtModel: EditText
    lateinit var txtLicensePlate: EditText
    lateinit var btnEvent: Button
    lateinit var btnDelete: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.vehicle_details_fragment, container, false)
        txtBrand = v.findViewById(R.id.txtBrand)
        txtModel = v.findViewById(R.id.txtTelefonoConfig)
        txtLicensePlate = v.findViewById(R.id.txtLicensePlate2)
        btnEvent = v.findViewById(R.id.btnUpdateConfig)
        btnDelete = v.findViewById(R.id.btnDelete)
        //btnDelete.setVisibility(View.INVISIBLE);

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vehicleViewModel = ViewModelProvider(this).get(VehiclesViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()
        var vehicle = arguments?.getParcelable<Vehicle>("VEHICLE")
        var mode = arguments?.getString("MODE")

        if (vehicle != null) {
            if (mode == "CREATE") {
                txtBrand.setFocusableInTouchMode(true)
                txtModel.setFocusableInTouchMode(true)
                txtLicensePlate.setFocusableInTouchMode(true)
                btnEvent.text = "Agregar"
                btnDelete.setVisibility(View.INVISIBLE);

                btnEvent.setOnClickListener {
                    if (txtBrand.text.toString().isNotEmpty() && txtModel.text.toString()
                            .isNotEmpty() && txtLicensePlate.text.toString().isNotEmpty()
                    ) {
                        vehicleViewModel.addUserVehicle(
                            Vehicle(
                                txtModel.text.toString(),
                                txtBrand.text.toString(),
                                txtLicensePlate.text.toString().toUpperCase(),
                                vehicle.userID
                            )
                        )

                        txtBrand.isFocusable = false
                        txtModel.isFocusable = false
                        txtLicensePlate.isFocusable = false
                        btnEvent.setVisibility(View.INVISIBLE)
                        btnDelete.setVisibility(View.INVISIBLE)

                    } else {
                        //Toast.makeText(v.context, "No deje campos vacios", Toast.LENGTH_SHORT).show()
                        sendAlertMessage("No deje campos vacios", "Atencion")
                    }
                }
            } else { //mode == "EDIT"
                Log.d("VehicleDetailsTest", vehicle.model)
                txtBrand.setText(vehicle.brand, TextView.BufferType.EDITABLE);
                txtModel.setText(vehicle.model, TextView.BufferType.EDITABLE);
                txtLicensePlate.setText(vehicle.licensePlate, TextView.BufferType.EDITABLE);
                btnDelete.setVisibility(View.VISIBLE);

                btnEvent.setOnClickListener {
                    if (btnEvent.text == "Editar") {
                        btnEvent.text = "Guardar"
                        btnDelete.setVisibility(View.VISIBLE);
                        //btnEvent.background.setTint("#3F51B5")
                        txtBrand.setFocusableInTouchMode(true)
                        txtModel.setFocusableInTouchMode(true)
                        txtLicensePlate.setFocusableInTouchMode(true)

                    } else if (btnEvent.text == "Guardar") {
                        vehicle.brand = txtBrand.text.toString()
                        vehicle.model = txtModel.text.toString()
                        vehicle.licensePlate = txtLicensePlate.text.toString()

                        if (txtBrand.text.toString().isNotEmpty() && txtModel.text.toString()
                                .isNotEmpty() && txtLicensePlate.text.toString().isNotEmpty()
                        ) {
                            vehicleViewModel.updateUserVehicle(vehicle)

                            //Vuelvo al estado de edicion
                            txtBrand.isFocusable = false
                            txtModel.isFocusable = false
                            txtLicensePlate.isFocusable = false
                            btnEvent.text == "Editar"

                        } else {
                            //Toast.makeText(v.context, "No deje campos vacios", Toast.LENGTH_SHORT).show()
                            sendAlertMessage("No deje campos vacios", "Atencion")
                        }

                    }
                }
                btnDelete.setOnClickListener {
                    val builder: AlertDialog.Builder? = activity?.let {
                        AlertDialog.Builder(it)
                    }
                    builder?.setMessage("Esta seguro que desea eliminar su vehiculo")
                        ?.setTitle("ELiminar vehiculo")
                    builder?.apply {
                        setPositiveButton("Aceptar",
                            DialogInterface.OnClickListener { dialog, id ->
                                vehicleViewModel.deleteUserVehicle(vehicle.uid/*, v*/)

                                txtBrand.isFocusable = false
                                txtModel.isFocusable = false
                                txtLicensePlate.isFocusable = false
                                btnEvent.setVisibility(View.INVISIBLE)
                                btnDelete.setVisibility(View.INVISIBLE)
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
        } else {
            //Toast.makeText(v.context, "Error", Toast.LENGTH_SHORT).show()
            sendAlertMessage("Error no identificado", "Error")
            Navigation.findNavController(v).backStack
        }

        vehicleViewModel.msgToVehiclesDetFrag.observe(viewLifecycleOwner, Observer { smsg ->
            //Toast.makeText(v.context, msg, Toast.LENGTH_SHORT).show()
            if (smsg.isNew())
                sendAlertMessage(smsg.readMsg(), "Atencion")
        })
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