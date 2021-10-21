package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
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
import android.widget.Toast
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.vehicle_details_fragment, container, false)
        txtBrand = v.findViewById(R.id.txtBrand)
        txtModel = v.findViewById(R.id.txtTelefonoConfig)
        txtLicensePlate = v.findViewById(R.id.txtLicensePlate2)
        btnEvent = v.findViewById(R.id.btnUpdateConfig)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vehicleViewModel = ViewModelProvider(this).get(VehiclesViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()
        var vehicle = arguments?.getParcelable<Vehicle>("vehicle")

        if (vehicle != null) {
            if(vehicle.licensePlate == "edit"){
                txtBrand.setFocusableInTouchMode(true)
                txtModel.setFocusableInTouchMode(true)
                txtLicensePlate.setFocusableInTouchMode(true)
                btnEvent.text = "Agregar"

                btnEvent.setOnClickListener{
                    vehicleViewModel.addFirebaseUserVehicle(Vehicle(txtBrand.text.toString(), txtModel.text.toString(), txtLicensePlate.text.toString(), vehicle.userID), v)
                }
            }else{
                Log.d("VehicleDetailsTest", vehicle.model)
                txtBrand.setText(vehicle.brand, TextView.BufferType.EDITABLE);
                txtModel.setText(vehicle.model, TextView.BufferType.EDITABLE);
                txtLicensePlate.setText(vehicle.licensePlate, TextView.BufferType.EDITABLE);

                btnEvent.setOnClickListener{
                    if(btnEvent.text == "Editar"){
                        btnEvent.text = "Guardar"
                        //btnEvent.background.setTint("#3F51B5")
                        txtBrand.setFocusableInTouchMode(true)
                        txtModel.setFocusableInTouchMode(true)
                        txtLicensePlate.setFocusableInTouchMode(true)
                    }else if(btnEvent.text == "Guardar"){
                        vehicle.brand = txtBrand.text.toString()
                        vehicle.model = txtModel.text.toString()
                        vehicle.licensePlate = txtLicensePlate.text.toString()
                        vehicleViewModel.updateFirebaseUserVehicle(vehicle, v)
                    }
                }
            }
        }else{
            Toast.makeText(v.context, "Error", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(v).backStack
        }
    }
    // TODO: Implement VehicleDetailsFragment
}