package com.ort.estacionarte.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ort.estacionarte.R
import com.ort.estacionarte.entitiescountry.User
import com.ort.estacionarte.viewmodels.LoginViewModel

class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var loginViewModel: LoginViewModel
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

    //@Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        btnRegister.setOnClickListener{
            if(txtMail.text.isNotEmpty() && txtPassword.text.isNotEmpty() && txtName.text.isNotEmpty() && txtLastName.text.isNotEmpty() && txtPhoneNumber.text.isNotEmpty() && txtPassword2.text.isNotEmpty()){
                if(txtPassword.text.toString() == txtPassword2.text.toString()){
                    var newUser = User(txtMail.text.toString(), txtName.text.toString(), txtLastName.text.toString(), txtPhoneNumber.text.toString())
                    loginViewModel.registerUser(newUser, txtPassword.text.toString(), v, requireContext())
                }else{
                    Toast.makeText(v.context, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(v.context, "No deje campos vacios", Toast.LENGTH_SHORT).show()
            }
            /*val action = LoginDirections.actionLoginToProfile(txtEmail.text.toString())
            v.findNavController().navigate(action)*/
        }
    }
}