package com.ort.estacionarte.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.ort.estacionarte.R

class LoadingDialog (private var activity: Activity ){

    private lateinit var dialog: AlertDialog

    fun startDialog() {
        var builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.progress_dialog,null))
        //bitmapDescriptorFromVector(requireContext(), R.drawable.car_loader)
        //builder.setView(inflater.inflate(bitmapDescriptorFromVector(activity.applicationContext, R.drawable.car_loader).hashCode(),null))
        builder.setCancelable(false)

        dialog= builder.create()
        dialog.show()
    }

    fun dismissDialog(){
        dialog.dismiss()
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        var vectorDrawable = ContextCompat.getDrawable(context, vectorResId) as Drawable
        vectorDrawable.setTint(Color.BLACK);
        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        var bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) as Bitmap
        var canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}