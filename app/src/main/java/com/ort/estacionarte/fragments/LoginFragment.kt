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
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val reservatiosViewModel: ReservationsViewModel by activityViewModels()

    //private lateinit var loginViewModel: LoginViewModel
    lateinit var v: View

    lateinit var txtMail: EditText
    lateinit var txtPassword: EditText
    lateinit var btnLogin: Button
    lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        //Esta linea obtiene el usuario logueado de la base, pero sigue mostrando un toque el login
        loginViewModel.getCurrentUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.login_fragment, container, false)

        txtMail = v.findViewById(R.id.txtMail)
        txtPassword = v.findViewById(R.id.txtPassword)
        btnLogin = v.findViewById(R.id.btnLogin)
        btnRegister = v.findViewById(R.id.btnRegister)

        return v
    }

    override fun onStart() {
        super.onStart()

        loginViewModel.currentUser.observe(viewLifecycleOwner, Observer { currentUser ->
            if (currentUser != null) {
                saveInSharedPreferences("Session", mapOf("userID" to currentUser.uid))
                //var map = getFromSharedPreferences("Session")
                reservatiosViewModel.getCurrentReservation(currentUser.uid)
                Navigation.findNavController(v).popBackStack(R.id.loginFragment, true)
                Navigation.findNavController(v).navigate(R.id.mapFragment)
            }
        })

        loginViewModel.msgToLogin.observe(viewLifecycleOwner, Observer { smsg ->
            //Toast.makeText(v.context, msg, Toast.LENGTH_SHORT).show()
            if (smsg.isNew())
                sendAlertMessage(smsg.readMsg(), "Atencion")
        })

        btnLogin.setOnClickListener {
            //if (validateInput()) //no hace falta, lo resuelve Auth
            loginViewModel.loginUser(txtMail.text.toString(), txtPassword.text.toString())
        }

        btnRegister.setOnClickListener {
            Navigation.findNavController(v).navigate(R.id.registerFragment)
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
        return txtMail.text.isNotEmpty() && txtPassword.text.isNotEmpty()
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