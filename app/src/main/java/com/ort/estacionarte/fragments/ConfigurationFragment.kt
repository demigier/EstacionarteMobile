package com.ort.estacionarte.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.entitiescountry.User
import com.ort.estacionarte.viewmodels.ProfileViewModel

class ConfigurationFragment : Fragment() {

    companion object {
        fun newInstance() = ConfigurationFragment()
    }

    private lateinit var profileViewModel: ProfileViewModel
    lateinit var v: View

    lateinit var txtName: EditText
    lateinit var txtLastName: EditText
    lateinit var txtPhoneNumber: EditText
    lateinit var btnUpdate: Button
    lateinit var txtLogout: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.configuration_fragment, container, false)
        txtName = v.findViewById(R.id.txtNameConfig)
        txtLastName = v.findViewById(R.id.txtLastNameConfig)
        txtPhoneNumber = v.findViewById(R.id.txtTelefonoConfig)
        btnUpdate = v.findViewById(R.id.btnUpdateConfig)
        txtLogout =  v.findViewById(R.id.txtLogout)

        return v
    }

    //@Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onStart() {
        super.onStart()

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", Context.MODE_PRIVATE)
        var userID = sharedPref.getString("userID","default")

        if(userID != "default"){
            txtName.setText(profileViewModel.userActive!!.name, TextView.BufferType.EDITABLE);
            txtLastName.setText(profileViewModel.userActive!!.lastName, TextView.BufferType.EDITABLE);
            txtPhoneNumber.setText(profileViewModel.userActive!!.phoneNumber, TextView.BufferType.EDITABLE);

            btnUpdate.setOnClickListener{
                if(txtName.text.isNotEmpty() && txtLastName.text.isNotEmpty() && txtPhoneNumber.text.isNotEmpty()){
                    profileViewModel.updateUser(txtName.text.toString(), txtLastName.text.toString(), txtPhoneNumber.text.toString(), userID!!, v, requireContext())
                }else{
                    Toast.makeText(v.context, "No deje campos vacios", Toast.LENGTH_SHORT).show()
                }
                /*val action = LoginDirections.actionLoginToProfile(txtEmail.text.toString())
                v.findNavController().navigate(action)*/
            }

            txtLogout.setOnClickListener{
                profileViewModel.logOut(requireContext(), v)
            }
        }else{
            Toast.makeText(v.context, "Error: usted no esta logueado", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(v).backStack
        }

    }
}