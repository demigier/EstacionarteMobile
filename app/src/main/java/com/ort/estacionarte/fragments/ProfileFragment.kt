package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ProfileViewModel
import kotlinx.coroutines.*

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    //private val profileViewModel: ProfileViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    //private lateinit var profileViewModel: ProfileViewModel

    lateinit var v: View

    lateinit var btnVehicles: FloatingActionButton
    lateinit var btnConfig: FloatingActionButton
    lateinit var txtUsername: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.profile_fragment, container, false)
        //profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        btnVehicles = v.findViewById(R.id.btnVehicles)
        btnConfig = v.findViewById(R.id.btnConfig)
        btnConfig = v.findViewById(R.id.btnConfig)
        txtUsername = v.findViewById(R.id.txtUsername)

        //loginViewModel.currentUser.value?.let { Log.d("ProfileF -> currnetUser:", it.name) }
        //var userID = getFromSharedPreferences("Session")?.get("userID").toString()

        //profileViewModel.currentUser.value = loginViewModel.currentUser.value

        loginViewModel.currentUser.observe(viewLifecycleOwner, Observer { currentUser ->

            if(currentUser != null){
                txtUsername.text = currentUser.lastName + " " + currentUser.name
            }
        })

        return v
    }

    override fun onStart() {
        super.onStart()

        btnVehicles.setOnClickListener{
            Navigation.findNavController(v).navigate(R.id.vehiclesFragment)
        }

        btnConfig.setOnClickListener{
            Navigation.findNavController(v).navigate(R.id.configurationFragment)
        }
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