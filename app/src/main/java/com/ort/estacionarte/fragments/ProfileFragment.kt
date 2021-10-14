package com.ort.estacionarte.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.ProfileViewModel

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var profileViewModel: ProfileViewModel
    lateinit var v: View

    lateinit var btnVehicles: FloatingActionButton
    lateinit var btnConfig: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.profile_fragment, container, false)
        btnVehicles = v.findViewById(R.id.btnVehicles)
        btnConfig = v.findViewById(R.id.btnConfig)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ProfileViewModel
    }

    override fun onStart() {
        super.onStart()
        var userID = arguments?.getString("userID")

        btnVehicles.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("userID", userID)
            Navigation.findNavController(v).navigate(R.id.vehiclesFragment, bundle)
        }
    }
}