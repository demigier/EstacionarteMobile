package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.ProfileViewModel
import kotlinx.coroutines.*

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var profileViewModel: ProfileViewModel
    lateinit var v: View

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

    lateinit var btnVehicles: FloatingActionButton
    lateinit var btnConfig: FloatingActionButton
    lateinit var txtUsername: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.profile_fragment, container, false)
        btnVehicles = v.findViewById(R.id.btnVehicles)
        btnConfig = v.findViewById(R.id.btnConfig)
        btnConfig = v.findViewById(R.id.btnConfig)
        txtUsername = v.findViewById(R.id.txtUsername)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ProfileViewModel
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session",
            Context.MODE_PRIVATE
        )
        var userID = sharedPref.getString("userID","default")
        //var userID = arguments?.getString("userID")

        if(userID != null){
            scope.launch {
                profileViewModel.getFirebaseUserData(userID)
            }

            val handler = Handler()
            handler.postDelayed(java.lang.Runnable {
                txtUsername.text = profileViewModel.userActive!!.lastName + " " + profileViewModel.userActive!!.name
            }, 600)

        }
        btnVehicles.setOnClickListener{
            /*val bundle = Bundle()
            bundle.putString("userID", userID)*/
            Navigation.findNavController(v).navigate(R.id.vehiclesFragment)
        }

        btnConfig.setOnClickListener{
            Navigation.findNavController(v).navigate(R.id.configurationFragment)
        }
    }
}