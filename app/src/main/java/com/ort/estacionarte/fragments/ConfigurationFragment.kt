package com.ort.estacionarte.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.SingleMsg
import com.ort.estacionarte.entitiescountry.User
import com.ort.estacionarte.viewmodels.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ConfigurationFragment : Fragment() {

    companion object {
        fun newInstance() = ConfigurationFragment()
    }

    private val loginVM: LoginViewModel by activityViewModels()

    //private lateinit var profileViewModel: ProfileViewModel
    lateinit var v: View

    private val parentJob = Job()
    val scope = CoroutineScope(Dispatchers.Default + parentJob)

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

/*      txtName.setBackgroundColor(Color.LTGRAY);
        txtLastName.setBackgroundColor(Color.LTGRAY);
        txtPhoneNumber.setBackgroundColor(Color.LTGRAY);
*/
        return v
    }

/*    //@Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    } */

    override fun onStart() {
        super.onStart()
/*
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("Session", Context.MODE_PRIVATE)
        var userID = sharedPref.getString("userID","default")

        if(userID != "default"){
            scope.launch {
                profileViewModel.getFirebaseUserData(userID.toString())
            }

            val handler = Handler()
            handler.postDelayed(java.lang.Runnable {
                txtName.setText(profileViewModel.currentUser!!.name, TextView.BufferType.EDITABLE);
                txtLastName.setText(profileViewModel.currentUser!!.lastName, TextView.BufferType.EDITABLE);
                txtPhoneNumber.setText(profileViewModel.currentUser!!.phoneNumber, TextView.BufferType.EDITABLE);
*/
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

/*              txtName.setBackgroundColor(Color.TRANSPARENT)
                txtLastName.setBackgroundColor(Color.TRANSPARENT)
                txtPhoneNumber.setBackgroundColor(Color.TRANSPARENT)
*/
                txtName.isFocusableInTouchMode = editMode
                txtLastName.isFocusableInTouchMode = editMode
                txtPhoneNumber.isFocusableInTouchMode = editMode
            }

        }

        loginVM.msgToConfFrag.observe(viewLifecycleOwner, Observer { smsg ->
            //Toast.makeText(v.context, msg, Toast.LENGTH_SHORT).show()
            if(smsg.isNew())
                sendAlertMessage(smsg.readMsg(), "Atención")
        })

        txtLogout.setOnClickListener {
            loginVM.logOut()
            //Falta navegar hasta el login
            Navigation.findNavController(v).popBackStack(R.id.configurationFragment, true)
            Navigation.findNavController(v).navigate(R.id.loginFragment)
        }

    }

    private fun validateInput(): Boolean {
        var validData: Boolean = false

        if (txtName.text.isEmpty() || txtLastName.text.isEmpty() || txtPhoneNumber.text.isEmpty()) {
            loginVM.msgToConfFrag.value = SingleMsg("No deje campos vacios")

        } else {
            //Faltaría chequear que el telefono cumpla con un formato específico.
            //Y que los nombres no contengan nros. y caracteres especiales, etc.
            validData = true
        }

        return validData
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
}