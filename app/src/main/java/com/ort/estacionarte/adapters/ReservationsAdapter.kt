package com.ort.estacionarte.adapters
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ort.estacionarte.R
import com.ort.estacionarte.entities.Reservation

class ReservationsAdapter(private var reservationsList: MutableList<Reservation?>, val onItemClick: (Int) -> Unit, private val context: Context) :
    RecyclerView.Adapter<ReservationsAdapter.ReservationHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.reservation_item,parent,false)
        return (ReservationHolder(view))
    }

    override fun getItemCount(): Int {
        return reservationsList.size
    }

    override fun onBindViewHolder(holder: ReservationHolder, position: Int) {
        holder.setParkingName(reservationsList[position]?.parking!!.parkingName)
        holder.setAddress(reservationsList[position]?.parking!!.address)
        holder.setVehicle(reservationsList[position]?.vehicle!!.model, reservationsList[position]!!.vehicle.brand)
        holder.setLicense(reservationsList[position]?.vehicle!!.licensePlate)
        holder.setReservationDate(reservationsList[position]?.reservationDate.toString())
        var type = "nada"
        var altDate = ""
        if(reservationsList[position]?.userLeftDate != null){
            type = "finalizacion"
            Log.d("test",type)
            altDate = reservationsList[position]?.userLeftDate.toString()
        }else if(reservationsList[position]?.cancelationDate != null){
            type = "cancelacion"
            altDate = reservationsList[position]?.cancelationDate.toString()
        }else if(reservationsList[position]?.userArrivedDate != null){
            type = "llegada"
            altDate = reservationsList[position]?.userArrivedDate.toString()
        }
        holder.setAlternativeDate(altDate, type)
        holder.getCancelationButton().setOnClickListener() {
            onItemClick(position)
        }
        if(reservationsList[position]?.active == true){
            holder.showCancelButton()
        }
    }

    class ReservationHolder (v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        @SuppressLint("SetTextI18n")
        fun setParkingName(name: String) {
            val txt: TextView = view.findViewById(R.id.txtReservPName)
            txt.text = "${txt.text} $name"
        }
        fun setAddress(address: String) {
            val txt: TextView = view.findViewById(R.id.txtReservPAddress)
            txt.text = "${txt.text} $address"
        }
        fun setVehicle(vehicleModel: String, vehicleBrand: String) {
            val txt: TextView = view.findViewById(R.id.txtReservVehicle)
            txt.text = "${txt.text} $vehicleBrand $vehicleModel"
        }
        fun setLicense(licensePlate: String) {
            val txt: TextView = view.findViewById(R.id.txtReservLicense)
            txt.text = "${txt.text} $licensePlate"
        }
        fun setReservationDate(date: String) {
            val txt: TextView = view.findViewById(R.id.txtReservDate)
            txt.text = "${txt.text} $date"
        }
        fun setAlternativeDate(altDate: String, type: String) {
            val txt: TextView = view.findViewById(R.id.txtReservAltDate)
            /*if (type == "cancelacion") {

            } else if (type == "cancelacion") {
                txt.text = "Fecha cancelacion: $altDate"
            }*/
            when (type) {
                "cancelacion" -> txt.text = "Fecha cancelacion: $altDate"
                "finalizacion" ->  txt.text = "Fecha finalizacion: $altDate"
                "llegada" -> txt.text = "Fecha llegada: $altDate"
                "nada" -> txt.text = "Aun se encuentra pendiente de su llegada"
            }
        }
        /*fun getCardLayout (): CardView {
            return view.findViewById(R.id.reservationCard)
        }*/
        fun showCancelButton() {
            val btn: FloatingActionButton = view.findViewById(R.id.btnReservCancel)
            btn.visibility = View.VISIBLE
        }
        fun getCancelationButton(): FloatingActionButton {
            val btn: FloatingActionButton = view.findViewById(R.id.btnReservCancel)
            return btn
        }
    }
}