package com.ort.estacionarte.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.entities.Reservation

class ReservationsAdapter(
    private var reservationsList: MutableList<Reservation>,
    val onItemClick: (Int) -> Unit,
    private val context: Context
) :
    RecyclerView.Adapter<ReservationsAdapter.ReservationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.reservation_item, parent, false)
        return (ReservationHolder(view))
    }

    override fun getItemCount(): Int {
        return reservationsList.size
    }

    override fun onBindViewHolder(holder: ReservationHolder, position: Int) {
        holder.setParkingName(reservationsList[position]?.parking!!.parkingName)
        holder.setAddress(reservationsList[position]?.parking!!.address)
        holder.setVehicle(
            reservationsList[position]?.vehicle!!.model,
            reservationsList[position]!!.vehicle.brand
        )
        holder.setLicense(reservationsList[position]?.vehicle!!.licensePlate)
        holder.setReservationDate(reservationsList[position]?.reservationDate.toString())
        var type = "UNDEFINED"
        var altDate = ""
        if (reservationsList[position]?.userLeftDate != null) {
            type = "FINISHED"
            Log.d("test", type)
            altDate = reservationsList[position]?.userLeftDate.toString()
        } else if (reservationsList[position]?.cancelationDate != null) {
            type = "CANCELED"
            altDate = reservationsList[position]?.cancelationDate.toString()
        } else if (reservationsList[position]?.userArrivedDate != null) {
            type = "ARRIVAL"
            altDate = reservationsList[position]?.userArrivedDate.toString()
        }
        holder.setAlternativeDate(altDate, type)
        holder.getCancelationButton().setOnClickListener() {
            onItemClick(position)
        }
        if (reservationsList[position]?.active == true) {
            if (reservationsList[position]?.userArrivedDate == null) {
                holder.showCancelButton()
            }
            holder.setColorActive(true)
        } else {
            holder.hideCancelButton()
            holder.setColorActive(false)
        }
    }

    class ReservationHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        @SuppressLint("SetTextI18n")
        fun setParkingName(name: String) {
            val txt: TextView = view.findViewById(R.id.txtReservPName)
            txt.text = "Estacionamiento $name"
        }

        fun setAddress(address: String) {
            val txt: TextView = view.findViewById(R.id.txtReservPAddress)
            txt.text = "Dirección: $address"
        }

        fun setVehicle(vehicleModel: String, vehicleBrand: String) {
            val txt: TextView = view.findViewById(R.id.txtReservVehicle)
            txt.text = "Vehículo: $vehicleBrand $vehicleModel"
        }

        fun setLicense(licensePlate: String) {
            val txt: TextView = view.findViewById(R.id.txtReservLicense)
            txt.text = "Patente: $licensePlate"
        }

        fun setReservationDate(date: String) {
            val txt: TextView = view.findViewById(R.id.txtReservDate)
            txt.text = "Fecha de reserva: $date"
        }

        fun setReservationState(active: Boolean) {

        }

        fun setAlternativeDate(altDate: String, type: String) {
            val txt: TextView = view.findViewById(R.id.txtReservAltDate)
            /*if (type == "cancelacion") {

            } else if (type == "cancelacion") {
                txt.text = "Fecha cancelacion: $altDate"
            }*/
            when (type) {
                "CANCELED" -> txt.text = "Fecha cancelación: $altDate"
                "FINISHED" -> txt.text = "Fecha finalización: $altDate"
                "ARRIVAL" -> txt.text = "Fecha llegada: $altDate"
                "UNDEFINED" -> txt.text = "Aun se encuentra pendiente de su llegada"
            }
        }

        fun setColorActive(active: Boolean) {
            val card: CardView = view.findViewById(R.id.reservationCard)
            val txtState: TextView = view.findViewById(R.id.textView)
            val txtPName: TextView = view.findViewById(R.id.txtReservPName)
            val txtPAddres: TextView = view.findViewById(R.id.txtReservPAddress)
            val txtVehicle: TextView = view.findViewById(R.id.txtReservVehicle)
            val txtLicense: TextView = view.findViewById(R.id.txtReservLicense)
            val txtDate: TextView = view.findViewById(R.id.txtReservDate)
            val txtAltDate: TextView = view.findViewById(R.id.txtReservAltDate)


            when (active) {
                true -> {
                    card.setCardBackgroundColor(Color.parseColor("#6200EE")) //A379DF //6200EE
                    txtState.text = "Reserva Actual"
                    txtState.setTextColor(Color.WHITE)
                    txtPName.setTextColor(Color.WHITE)
                    txtPAddres.setTextColor(Color.WHITE)
                    txtVehicle.setTextColor(Color.WHITE)
                    txtLicense.setTextColor(Color.WHITE)
                    txtDate.setTextColor(Color.WHITE)
                    txtAltDate.setTextColor(Color.WHITE)
                }
                false -> {
                    card.setCardBackgroundColor(Color.LTGRAY)
                    txtState.text = "Reserva Finalizada"
                    txtState.setTextColor(Color.BLACK)
                    txtPName.setTextColor(Color.BLACK)
                    txtPAddres.setTextColor(Color.BLACK)
                    txtVehicle.setTextColor(Color.BLACK)
                    txtLicense.setTextColor(Color.BLACK)
                    txtDate.setTextColor(Color.BLACK)
                    txtAltDate.setTextColor(Color.BLACK)
                }
            }
        }

        fun showCancelButton() {
            val btn: FloatingActionButton = view.findViewById(R.id.btnReservCancel)
            btn.visibility = View.VISIBLE
        }

        fun hideCancelButton() {
            val btn: FloatingActionButton = view.findViewById(R.id.btnReservCancel)
            btn.visibility = View.INVISIBLE
        }

        fun getCancelationButton(): FloatingActionButton {
            val btn: FloatingActionButton = view.findViewById(R.id.btnReservCancel)
            return btn
        }
    }
}