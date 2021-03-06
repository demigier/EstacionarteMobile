package com.ort.estacionarte.viewmodels

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.ort.estacionarte.entitiescountry.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.ort.estacionarte.R
import com.ort.estacionarte.adapters.SingleMsg
import kotlinx.coroutines.Dispatchers

class LoginViewModel : ViewModel() {
    private var db = Firebase.firestore
    private val auth = Firebase.auth
    private val USERS_COLLECTION = "Users"

    var msgToLogin = MutableLiveData<SingleMsg>()
    var msgToRegister = MutableLiveData<SingleMsg>()
    var msgToConfFrag = MutableLiveData<SingleMsg>()

    var currentUser: MutableLiveData<User?> = MutableLiveData(null)

    fun loginUser(email: String, password: String) {
        var userID: String?
        var user: User?

        viewModelScope.launch(Dispatchers.IO) {
            try {
                userID = auth.signInWithEmailAndPassword(email, password).await().user!!.uid
                Log.d("LoginVM -> loginUser:", "UserID: " + userID.toString())

                //Ahora busco que el usuario se encuentre en la base de AppUsers...
                user = getFirebaseUserData(userID!!)

                if (user != null) {
                    currentUser.postValue(user)
                } else {
                    sendMsgToFront(msgToLogin, SingleMsg("Usuario sin permisos"))
                }

            } catch (ia: IllegalArgumentException) {
                //IllegalArgumentException: Given String is empty or null
                sendMsgToFront(msgToLogin, SingleMsg("No puede haber campos vac??os"))

            } catch (ic: FirebaseAuthInvalidCredentialsException) {
                //FirebaseAuthInvalidCredentialsException: The email address is badly formatted.
                //FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.
                Log.d("LoginVM -> loginUser:", ic.toString())
                sendMsgToFront(msgToLogin, SingleMsg("Verifique email y contrase??a"))

            } catch (uc: FirebaseAuthUserCollisionException) {
                //FirebaseAuthUserCollisionException: The email address is already in use by another account.
                //FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password.
                Log.d("LoginVM -> loginUser:", uc.toString())
                sendMsgToFront(msgToLogin, SingleMsg("Verifique la contrase??a"))

            } catch (iu: FirebaseAuthInvalidUserException) {
                //FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted.
                Log.d("LoginVM -> loginUser:", iu.toString())
                sendMsgToFront(msgToLogin, SingleMsg("Usuario inexistente"))

            } catch (tmr: FirebaseTooManyRequestsException) {
                //FirebaseTooManyRequestsException: We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]
                Log.d("LoginVM -> loginUser:", tmr.toString())
                sendMsgToFront(
                    msgToLogin,
                    SingleMsg("Se ha bloqueado la cuenta por intentos reiterados")
                )

            } catch (ne: FirebaseNetworkException) {
                //FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host) has occurred.
                Log.d("LoginVM -> loginUser:", ne.toString())
                sendMsgToFront(msgToLogin, SingleMsg("Error de red, sin conexi??n"))
            } catch (e: Exception) {
                // Otras excepciones:
                Log.d("LoginVM -> loginUser:", e.toString())
                sendMsgToFront(msgToLogin, SingleMsg("Error "))
            }
        }
    }

    fun registerUser(newUser: User, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var result = auth.createUserWithEmailAndPassword(newUser.email, password).await()
                // En caso de que el usuario ya se encuentre registrado,
                // o exista un error en el formato de email o pass, emite una excepci??n.

                newUser.uid = result.user!!.uid

                registerFirebaseUserData(newUser)

                currentUser.postValue(newUser)

            } catch (ic: FirebaseAuthInvalidCredentialsException) {
                //FirebaseAuthInvalidCredentialsException: The email address is badly formatted.
                Log.d("LoginVM -> loginUser:", ic.toString())
                sendMsgToFront(msgToRegister, SingleMsg("Verifique el formato del email"))

            } catch (wp: FirebaseAuthWeakPasswordException) {
                //com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]
                Log.d("LoginVM -> loginUser:", wp.toString())
                sendMsgToFront(msgToRegister,SingleMsg("La contrase??a debe tener al menos 6 caracteres"))

            } catch (uc: FirebaseAuthUserCollisionException) {
                //com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account.
                Log.d("LoginVM -> loginUser:", uc.toString())
                sendMsgToFront(msgToRegister, SingleMsg("Email no disponible"))

            } catch (ne: FirebaseNetworkException) {
                //FirebaseNetworkException: A network error (such as timeout, interrupted connection or unreachable host) has occurred.
                Log.d("LoginVM -> loginUser:", ne.toString())
                sendMsgToFront(msgToRegister, SingleMsg("Error de red, sin conexi??n"))
            } catch (e: Exception) {
                // Otras excepciones:
                Log.d("LoginVM -> registerUser:", e.toString())
                sendMsgToFront(msgToRegister, SingleMsg("Error"))
            }
        }
    }

    private suspend fun getFirebaseUserData(userID: String): User? {
        var doc = db.collection(USERS_COLLECTION).document(userID).get().await()

        return doc?.toObject()
    }

    private suspend fun registerFirebaseUserData(newUser: User) {
        db.collection(USERS_COLLECTION).document(newUser.uid).set(newUser).await()
    }

    fun logOut() {
        try {
            Firebase.auth.signOut()
            currentUser.value = null

        } catch (e: Exception) {
            Log.d("LoginVM -> logOut:", e.toString())
            sendMsgToFront(msgToConfFrag,SingleMsg("Error"))
        }
    }

    fun updateUserData(userEdit: User) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection(USERS_COLLECTION).document(userEdit.uid).update(
                    mapOf(
                        "name" to userEdit.name,
                        "lastName" to userEdit.lastName,
                        "phoneNumber" to userEdit.phoneNumber
                    )
                ).addOnSuccessListener {
                    currentUser.postValue(userEdit)
                    sendMsgToFront(msgToConfFrag, SingleMsg("Los datos se actualizaron"))
                }.addOnFailureListener { e ->
                    Log.w("LoginVM -> updateUserData:", "Error: Datos no actualizados")
                    throw e
                }

            } catch (e: Exception) {
                // Otras excepciones:
                Log.d("LoginVM -> updateUserData:", e.toString())
                sendMsgToFront(
                    msgToConfFrag,
                    SingleMsg("Error al intentar actualizar los datos", true)
                )
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var userID = auth.currentUser?.uid

                if (userID != null) {
                    currentUser.postValue(getFirebaseUserData(userID))
                }
                //Sino queda en null
            } catch (e: Exception) {
                Log.d("LoginVM -> getCurrentUser:", e.toString())
            }
        }
    }

    private fun sendMsgToFront(mutableLiveData: MutableLiveData<SingleMsg>, smsg: SingleMsg) {
        mutableLiveData.postValue(smsg)
    }

}