package com.ort.estacionarte.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.VehiclesAdapter
import com.ort.estacionarte.entities.Vehicle
import com.ort.estacionarte.viewmodels.VehiclesViewModel
import kotlinx.coroutines.*

class VehiclesFragment : Fragment() {

    companion object {
        fun newInstance() = VehiclesFragment()
    }

    private lateinit var vehiclesViewModel: VehiclesViewModel
    lateinit var v: View

    private lateinit var recyclerViewVehiculos: RecyclerView
    private lateinit var vehiclesAdapter: VehiclesAdapter
    lateinit var btnAdd: FloatingActionButton

    public lateinit var userID: String

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.vehicles_fragment, container, false)
        recyclerViewVehiculos = v.findViewById(R.id.recyclerViewVehiculos)
        btnAdd = v.findViewById(R.id.btnAdd)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vehiclesViewModel = ViewModelProvider(this).get(VehiclesViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        userID = arguments?.getString("userID")!!
        if (userID != null) {
            Log.d("Hola", userID)
            scope.launch {
                vehiclesViewModel.getFirebaseUserVehicles(userID)
                delay(400)
                //enviar vehiculos al adapter

                vehiclesAdapter = VehiclesAdapter(vehiclesViewModel.vehiclesList!!, { item ->
                    onItemClick(item)
                }, requireContext())
            }

            recyclerViewVehiculos.setHasFixedSize(true)
            var linearLayoutManager = LinearLayoutManager(context)
            recyclerViewVehiculos.layoutManager = linearLayoutManager

            val handler = Handler()
            handler.postDelayed(java.lang.Runnable {
                recyclerViewVehiculos.adapter = vehiclesAdapter
            }, 600)

            btnAdd.setOnClickListener{
                addVehicle()
            }
        }else{
            Toast.makeText(v.context, "Error: usted no esta logueado", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(v).navigate(R.id.loginFragment)
        }
        //recyclerViewVehiculos
    }

    fun onItemClick ( position : Int ) {
        val bundle = Bundle()
        bundle.putParcelable("vehicle", vehiclesViewModel.vehiclesList!![position])
        Navigation.findNavController(v).navigate(R.id.vehicleDetailsFragment, bundle)
    }

    fun addVehicle () {
        val bundle = Bundle()
        bundle.putParcelable("vehicle", Vehicle("edit","edit","edit", userID))
        Navigation.findNavController(v).navigate(R.id.vehicleDetailsFragment, bundle)
    }

    fun initializeAdapter () {
        recyclerViewVehiculos.adapter = vehiclesAdapter
        Log.d("Hola", "HOla")
    }
}