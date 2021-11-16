package com.ort.estacionarte.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.VehiclesAdapter
import com.ort.estacionarte.entities.Vehicle
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.VehiclesViewModel
import kotlinx.coroutines.*

class VehiclesFragment : Fragment() {

    companion object {
        fun newInstance() = VehiclesFragment()
    }

    private val vehiclesVM: VehiclesViewModel by activityViewModels()
    private val loginVM: LoginViewModel by activityViewModels()

    lateinit var v: View

    private lateinit var recyclerViewVehiculos: RecyclerView
    private lateinit var vehiclesAdapter: VehiclesAdapter

    lateinit var btnAdd: FloatingActionButton
    lateinit var userID: String

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.vehicles_fragment, container, false)
        recyclerViewVehiculos = v.findViewById(R.id.recyclerViewVehiculos)
        btnAdd = v.findViewById(R.id.btnAdd)

        vehiclesVM.getUserVehicles(loginVM.currentUser.value!!.uid)

        recyclerViewVehiculos.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(context)
        recyclerViewVehiculos.layoutManager = linearLayoutManager

        vehiclesVM.vehiclesList.observe(viewLifecycleOwner, Observer { vehicleList ->
            vehiclesAdapter = VehiclesAdapter(vehicleList, { item ->
                onItemClick(item)
            }, requireContext())
            recyclerViewVehiculos.adapter = vehiclesAdapter
        })

        return v
    }

    override fun onStart() {
        super.onStart()

        btnAdd.setOnClickListener {
            addVehicle()
        }
    }

    fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("VEHICLE", vehiclesVM.vehiclesList.value?.get(position))
        bundle.putString("MODE","EDIT")
        Navigation.findNavController(v).navigate(R.id.vehicleDetailsFragment, bundle)
    }

    fun addVehicle() {
        val bundle = Bundle()
        bundle.putParcelable("VEHICLE", Vehicle("", "", "", loginVM.currentUser.value!!.uid))
        bundle.putString("MODE","CREATE")
        Navigation.findNavController(v).navigate(R.id.vehicleDetailsFragment, bundle)
    }

    fun initializeAdapter() {
        recyclerViewVehiculos.adapter = vehiclesAdapter
        Log.d("Hola", "HOla")
    }
}
