package com.ort.estacionarte.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.SingleMsg
import com.ort.estacionarte.entitiescountry.User
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel
import com.ort.estacionarte.viewmodels.VehiclesViewModel

class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()
    private val vehiclesVM: VehiclesViewModel by activityViewModels()

    lateinit var v: View

    lateinit var txtMail: EditText
    lateinit var txtPassword: EditText
    lateinit var txtPassword2: EditText
    lateinit var txtName: EditText
    lateinit var txtLastName: EditText
    lateinit var txtPhoneNumber: EditText
    lateinit var btnRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.register_fragment, container, false)

        txtMail = v.findViewById(R.id.txtBrand)
        txtPassword = v.findViewById(R.id.txtPasswordNew)
        txtPassword2 = v.findViewById(R.id.txtPasswordNew2)
        txtName = v.findViewById(R.id.txtLicensePlate2)
        txtLastName = v.findViewById(R.id.txtLastNameConfig)
        txtPhoneNumber = v.findViewById(R.id.txtTelefonoConfig)
        btnRegister = v.findViewById(R.id.btnUpdateConfig)

        return v
    }

    override fun onStart() {
        super.onStart()

        loginViewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                saveInSharedPreferences("Session", mapOf("userID" to user.uid))

                reservationsVM.getAllReservations(user.uid)
                vehiclesVM.getUserVehicles(user.uid)

                Navigation.findNavController(v).popBackStack(R.id.loginFragment, true)
                Navigation.findNavController(v).navigate(R.id.mapFragment)
            }
        })

        loginViewModel.msgToRegister.observe(viewLifecycleOwner, Observer { smsg ->
            if(smsg.isNew())
                sendAlertMessage(smsg.readMsg(), "Atención")
        })


        btnRegister.setOnClickListener {
            if (validateInput()) {
                var newUser = User(
                    txtMail.text.toString(),
                    txtName.text.toString(),
                    txtLastName.text.toString(),
                    txtPhoneNumber.text.toString()
                )
                loginViewModel.registerUser(newUser, txtPassword.text.toString())
            }
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

    private fun validateInput(): Boolean {
        var validData: Boolean = false

        if (txtMail.text.isEmpty() || txtPassword.text.isEmpty() || txtName.text.isEmpty() || txtLastName.text.isEmpty() || txtPhoneNumber.text.isEmpty()) {
            loginViewModel.msgToRegister.value = SingleMsg("No deje campos vacios")

        } else if (txtPassword.text.toString() != txtPassword2.text.toString()) {
            loginViewModel.msgToRegister.value = SingleMsg("Las contraseñas deben coincidir")

        } else if(txtName.text.length > 15 || txtLastName.text.length > 15){
            loginViewModel.msgToRegister.value = SingleMsg("El maximo de caracteres de los nombres y apellidos es de 15")
        } else if(txtName.text.length < 3){
            loginViewModel.msgToRegister.value = SingleMsg("El nombre debe tener minimo tres caracteres")
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(txtMail.text).matches()){
            loginViewModel.msgToRegister.value = SingleMsg("El formato del mail es invalido")
        } else{
            validData = true
        }
        return validData
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

    /*private fun getFromSharedPreferences(tag: String): MutableMap<String, *>? {
        return requireContext().getSharedPreferences(tag, Context.MODE_PRIVATE).all
    }*/


}