package com.ort.estacionarte.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.SingleMsg
import com.ort.estacionarte.entitiescountry.User
import com.ort.estacionarte.viewmodels.LoginViewModel
import com.ort.estacionarte.viewmodels.ReservationsViewModel
import com.ort.estacionarte.viewmodels.VehiclesViewModel

class ConfigurationFragment : Fragment() {

    companion object {
        fun newInstance() = ConfigurationFragment()
    }

    private val loginVM: LoginViewModel by activityViewModels()
    private val reservationsVM: ReservationsViewModel by activityViewModels()
    private val vehiclesVM: VehiclesViewModel by activityViewModels()

    lateinit var v: View

    lateinit var txtName: EditText
    lateinit var txtLastName: EditText
    lateinit var txtPhoneNumber: EditText
    lateinit var btnUpdate: Button
    lateinit var txtLogout: TextView
    var editMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.configuration_fragment, container, false)

        txtName = v.findViewById(R.id.txtNameConfig)
        txtLastName = v.findViewById(R.id.txtLastNameConfig)
        txtPhoneNumber = v.findViewById(R.id.txtTelefonoConfig)
        btnUpdate = v.findViewById(R.id.btnUpdateConfig)
        txtLogout = v.findViewById(R.id.txtLogout)

        txtName.setText(loginVM.currentUser.value?.name, TextView.BufferType.EDITABLE);
        txtLastName.setText(loginVM.currentUser.value?.lastName, TextView.BufferType.EDITABLE);
        txtPhoneNumber.setText(
            loginVM.currentUser.value?.phoneNumber,
            TextView.BufferType.EDITABLE
        );

        //Configuración inicial del formulario
        btnUpdate.text = if (editMode) "Guardar" else "Editar"
        btnUpdate.setBackgroundColor(Color.RED);
        txtName.isFocusableInTouchMode = editMode
        txtLastName.isFocusableInTouchMode = editMode
        txtPhoneNumber.isFocusableInTouchMode = editMode
        return v
    }

    override fun onStart() {
        super.onStart()

        btnUpdate.setOnClickListener {
            if (editMode) {
                if (validateInput()) {
                    var userEdit = User()
                    userEdit.uid = loginVM.currentUser.value?.uid.toString()
                    userEdit.name = txtName.text.toString()
                    userEdit.lastName = txtLastName.text.toString()
                    userEdit.phoneNumber = txtPhoneNumber.text.toString()

                    loginVM.updateUserData(userEdit)

                    //Si sale mal emitirá una alerta
                    //Después habría que navegar a la pantalla anterior.
                    editMode = !editMode
                    btnUpdate.text = "Editar"
                    btnUpdate.setBackgroundColor(Color.RED);

                }
            } else {
                editMode = !editMode
                btnUpdate.text = "Guardar"
                btnUpdate.setBackgroundColor(Color.BLUE);

                txtName.isFocusableInTouchMode = editMode
                txtLastName.isFocusableInTouchMode = editMode
                txtPhoneNumber.isFocusableInTouchMode = editMode
            }
        }

        loginVM.msgToConfFrag.observe(viewLifecycleOwner, Observer { smsg ->
            if (smsg.isNew()) {
                if(!smsg.isErrorMsg()){
                    //Si no hubo error, informo y navego al profileFragment
                    sendAlertMessage(smsg.readMsg(), "Atención", { navigateToProfile() })
                }else{
                    sendAlertMessage(smsg.readMsg(), "Atención")
                }
            }
        })

        txtLogout.setOnClickListener {
            sendConfirmAlert("¿Desea cerrar la sesión?", "Atención") { logOut() }
        }

    }

    private fun validateInput(): Boolean {
        var validData = false

        if (txtName.text.isEmpty() || txtLastName.text.isEmpty() || txtPhoneNumber.text.isEmpty()) {
            loginVM.msgToConfFrag.value = SingleMsg("No deje campos vacios", true)

        }  else if(txtName.text.length > 15 || txtLastName.text.length > 15){
            loginVM.msgToConfFrag.value = SingleMsg("El maximo de caracteres de los nombres y apellidos es de 15", true)
        } else if(txtName.text.length < 3){
            loginVM.msgToConfFrag.value = SingleMsg("El nombre debe tener minimo tres caracteres", true)
        } else {
            validData = true
        }

        return validData
    }

    private fun logOut() {
        //Se resetean la info del ususario
        loginVM.logOut()
        reservationsVM.reservationsList.value = mutableListOf()
        reservationsVM.currentReservation.value = null
        vehiclesVM.vehiclesList.value = mutableListOf()
        reservationsVM.msgToLoadinDialog = MutableLiveData<SingleMsg>() //Limpia el loading dialog

        val sharedPref: SharedPreferences = requireContext().getSharedPreferences(
            "Session",
            Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()

        editor.remove("userID")
        editor.apply()

        //Navegar hasta loginFragment
        Navigation.findNavController(v).popBackStack(R.id.mapFragment, true)
        Navigation.findNavController(v).navigate(R.id.loginFragment)
    }

    private fun sendAlertMessage(msg: String, title: String, callback: (() -> Unit)? = null) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }
        builder?.setMessage(msg)
            ?.setTitle(title)
        builder?.apply {
            setNegativeButton("Aceptar",
                DialogInterface.OnClickListener { dialog, id ->
                    if (callback != null) {
                        callback()
                    }
                    dialog.cancel()
                })
        }
        builder?.create()
        builder?.show()
    }

    private fun sendConfirmAlert(msg: String, title: String,callback: () -> Unit) {
        val builder: AlertDialog.Builder? = activity?.let {
            AlertDialog.Builder(it)
        }
        builder?.setMessage(msg)
            ?.setTitle(title)
        builder?.apply {
            setPositiveButton("Aceptar",
                DialogInterface.OnClickListener { dialog, id ->
                    callback()
                })
            setNegativeButton("Cancelar",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })
        }
        builder?.create()
        builder?.show()
    }

    fun navigateToProfile(){
        Navigation.findNavController(v).popBackStack(R.id.profileFragment, true)
        Navigation.findNavController(v).navigate(R.id.profileFragment)
    }

}