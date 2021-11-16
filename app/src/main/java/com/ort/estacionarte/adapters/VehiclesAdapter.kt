package com.ort.estacionarte.adapters
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ort.estacionarte.R
import com.ort.estacionarte.entities.Vehicle

class VehiclesAdapter(private var vehicleList: MutableList<Vehicle>, val onItemClick: (Int) -> Unit, private val context: Context) :
    RecyclerView.Adapter<VehiclesAdapter.VehicleHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.vehicle_item,parent,false)
        return (VehicleHolder(view))
    }

    override fun getItemCount(): Int {
        return vehicleList.size
    }

    override fun onBindViewHolder(holder: VehicleHolder, position: Int) {
        holder.setModelBrand(vehicleList[position].model, vehicleList[position].brand)
        holder.setLicensePlate(vehicleList[position].licensePlate)
        holder.getCardLayout().setOnClickListener() {
            onItemClick(position)
        }
    }

    class VehicleHolder (v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        @SuppressLint("SetTextI18n")
        fun setModelBrand(model: String, brand: String) {
            val txt: TextView = view.findViewById(R.id.txtUserName)
            txt.text = "$model $brand"
        }
        fun setLicensePlate(licensePlate: String) {
            val txt: TextView = view.findViewById(R.id.txtLicensePlate)
            txt.text = licensePlate
        }
        fun getCardLayout (): CardView {
            return view.findViewById(R.id.vehicleCard)
        }
    }
}